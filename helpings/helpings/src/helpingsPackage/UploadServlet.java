package helpingsPackage;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.*;

@WebServlet(urlPatterns = "/upload")
public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final int THUMB_HEIGHT = 200;
	static final String UPLOAD_PATH = "/var/www/uploads/";
	private HelpingsDatabase mDatabase;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		mDatabase = new HelpingsDatabase();
		try {
			mDatabase.init();
		} catch (ClassNotFoundException e) {

		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException {
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// Configure a repository (to ensure a secure temp location is used)
		ServletContext servletContext = this.getServletConfig()
				.getServletContext();
		File repository = new File(UPLOAD_PATH);
		factory.setRepository(repository);

		Date date = new Date();
		String salt = null;
		try {
			salt = HelpingsDatabase.getSalt();
		} catch (Exception e) {

		}
		String filename = Long.toString(date.getTime()) + salt + ".png";
/*
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				request.getInputStream()));
		for (String line; (line = reader.readLine()) != null;) {
			System.out.println(line);
		}
*/
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		String title = null, username = null, token = null;
		// Parse the request
		try {
			List<FileItem> items = upload.parseRequest(request);
			for (FileItem item : items) {
				if (item.isFormField()) {
					String key = item.getFieldName();
					if (key.equals("title")) {
						title = item.getString();
					} else if (key.equals("username")) {
						username = item.getString();
					} else if (key.equals("token")) {
						token = item.getString();
					}
				} else {
					// Write the file
					File file = new File(UPLOAD_PATH + filename);
					item.write(file);
				}
			}
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(UPLOAD_PATH + filename));
			BufferedImage thumb = createResizedCopy(img);
			File outputfile = new File(UPLOAD_PATH + "thumb" + filename);
			ImageIO.write(thumb, "png", outputfile);
		} catch (IOException e) {
		}

		// FIXME this needs to come *before* we do the file upload
		boolean success = false;
		if (token != null && token.length() > 0) {
			String name = mDatabase.getUserForToken(token);
			if (name != null && name.length() > 0) {
				if (name.equals(username)) {
					success = true;
				}
			}
		}
		if (!success) {
			response.sendError(401);
			return;
		}

		try {
			mDatabase.createPost(username, title, filename, "", date.getTime());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.sendRedirect("feed");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException {

		RequestDispatcher view = request.getRequestDispatcher("upload.html");
		view.forward(request, response);
	}

	BufferedImage createResizedCopy(Image originalImage) {
		float width = originalImage.getWidth(null);
		float height = originalImage.getHeight(null);
		int scaledHeight = THUMB_HEIGHT;
		float scale = ((float) scaledHeight) / height;
		int scaledWidth = (int) (width * scale);
		int imageType = BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight,
				imageType);
		Graphics2D g = scaledBI.createGraphics();
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}
}
