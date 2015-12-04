package helpingsPackage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;

@WebServlet(urlPatterns = "/upload")
public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final int THUMB_WIDTH = 400;
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

		File imagefile = new File(UPLOAD_PATH + filename);
		int rotation = getRotation(imagefile);

		BufferedImage img = null;
		try {
			img = ImageIO.read(imagefile);
			BufferedImage thumb = fixRotationAndScale(img, rotation);
			File outputfile = new File(UPLOAD_PATH + "thumb" + filename);
			ImageIO.write(thumb, "png", outputfile);
		} catch (IOException e) {
			PrintWriter out = response.getWriter();
			out.println("WRONG! " + e.getMessage());
			out.flush();
			return;
		}

		// FIXME this needs to come *before* we do the file upload
		boolean success = false;
		String name = "";
		if (token != null && token.length() > 0) {
			name = mDatabase.getUserForToken(token);
			if (name != null && name.length() > 0) {
				if (name.equals(username)) {
					success = true;
				}
			}
		}
		if (!success) {
			PrintWriter out = response.getWriter();
			out.println("WRONG! " + name + " " + token);
			out.flush();
			return;
		}

		int result = 0;
		try {
			result = mDatabase.createPost(username, title, filename, "", date.getTime());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// We could send the result back here somehow?

		response.sendRedirect("feed");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, java.io.IOException {

		RequestDispatcher view = request.getRequestDispatcher("upload.html");
		view.forward(request, response);
	}

	BufferedImage fixRotationAndScale(BufferedImage image, int orientation ){

		float width = image.getWidth(null);
		int scaledWidth = THUMB_WIDTH;
		float scale = ((float) scaledWidth) / width;

		AffineTransform transform = getExifTransformation(orientation, image.getHeight(), image.getWidth(), scale);
	    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

	    BufferedImage destinationImage = op.createCompatibleDestImage(image,  (image.getType() == BufferedImage.TYPE_BYTE_GRAY)? image.getColorModel() : null );
	    Graphics2D g = destinationImage.createGraphics();
	    g.setBackground(Color.WHITE);
	    g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
	    destinationImage = op.filter(image, destinationImage);

	    return destinationImage;
	}

	int getRotation(File imageFile){

		int orientation = 1;

		Metadata metadata;
		try {
			metadata = ImageMetadataReader.readMetadata(imageFile);
		} catch (ImageProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return orientation;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return orientation;
		}

		Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		try {
			orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
		} catch (MetadataException me) {
		}
		return orientation;

	}

	public static AffineTransform getExifTransformation(int orientation, int height, int width, float scale) {

	    AffineTransform t = new AffineTransform();

	    switch (orientation) {
	    case 1:
	        break;
	    case 2: // Flip X
	        t.scale(-scale, scale);
	        t.translate(-width, 0);
	        break;
	    case 3: // PI rotation 
	        t.translate(width, height);
	        t.rotate(Math.PI);
	        break;
	    case 4: // Flip Y
	        t.scale(scale, -scale);
	        t.translate(0, -height);
	        break;
	    case 5: // - PI/2 and Flip X
	        t.rotate(-Math.PI / 2);
	        t.scale(-scale, scale);
	        break;
	    case 6: // -PI/2 and -width
	        t.translate(height, 0);
	        t.rotate(Math.PI / 2);
	        break;
	    case 7: // PI/2 and Flip
	        t.scale(-scale, scale);
	        t.translate(-height, 0);
	        t.translate(0, width);
	        t.rotate(  3 * Math.PI / 2);
	        break;
	    case 8: // PI / 2
	        t.translate(0, width);
	        t.rotate(  3 * Math.PI / 2);
	        break;
	    }

	    return t;
	}
}
