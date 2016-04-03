package helpingsPackage;

import java.awt.image.BufferedImage;
import java.io.*; 
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(urlPatterns="/image/*", asyncSupported = true)
public class ImageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.setContentType("image/png");
		String path = request.getRequestURL().toString();
		String filename = path.substring(path.lastIndexOf('/'));
		File f = new File(UploadServlet.UPLOAD_PATH + "/" + filename);
		BufferedImage bi = ImageIO.read(f);
		OutputStream outStream = response.getOutputStream();
		ImageIO.write(bi, "png", outStream);
		outStream.close();
	}

}
