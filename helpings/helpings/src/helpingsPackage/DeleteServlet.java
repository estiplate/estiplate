package helpingsPackage;

import java.io.*; 
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(urlPatterns="/delete", asyncSupported = true)
public class DeleteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private HelpingsDatabase mDatabase;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		mDatabase = new HelpingsDatabase();
		try {
			mDatabase.init();
		} catch (ClassNotFoundException e) {

		}
	}

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		BufferedReader reader = request.getReader();
		String line = reader.readLine();
		JSONObject requestJSON = null;
		long post = 0;
		String username = "";
		String token = null;
		try {
			requestJSON = new JSONObject(line);
			username = (String) requestJSON.optString("username");
			post = requestJSON.optLong("post");
			token = (String) requestJSON.optString("token");
			
		} catch ( Exception e ) {}

		if ( requestJSON == null ) {
			return;
		}

		boolean success = false;
		if ( token != null && token.length() > 0 ) {
			String name = mDatabase.getUserForToken(token);
			if ( name != null && name.length() > 0 ) {
				if ( name.equals(username) ) {
					success = true;
				}
			}
		}

		// Only save guesses from logged in users (for now)
		if ( success ) {
			try {
				mDatabase.deletePost(post, username);
			} catch ( Exception e ) {
				return;
			}
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		JSONObject obj = new JSONObject();

		try {
			obj.put("postId", post);
		} catch (JSONException e){}

		String errorString = obj.toString();
		out.println(errorString);
		out.flush();
	}

}
