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

@WebServlet(urlPatterns="/guess", asyncSupported = true)
public class GuessServlet extends HttpServlet {

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
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println(request.getRequestURI());
	}

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		BufferedReader reader = request.getReader();
		String line = reader.readLine();
		JSONObject requestJSON = null;
		long post = 0;
		int guess = 0;
		String username = "";
		String token = null;
		try {
			requestJSON = new JSONObject(line);
			username = (String) requestJSON.optString("username");
			post = requestJSON.optLong("post");
			guess = requestJSON.optInt("calories");
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
				mDatabase.createGuess(post, username, guess);
			} catch ( Exception e ) {}
		}

		// Error checking here!!
		int cals = 0;
		try {
			cals = mDatabase.getAverageGuessForPost(post);
		} catch ( Exception e ) {}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		JSONObject obj = new JSONObject();

		try {
			obj.put("post", post);
			obj.put("calories", cals);
			ArrayList<Comment>comments = mDatabase.getCommentsForPost(post, 0, 100); // FIXME
			JSONArray commentsJson = new JSONArray();
			for( Comment comment: comments ){
				commentsJson.put(comment.toJson());
			}
			obj.put("comments", commentsJson);

		} catch (JSONException e){}
		String errorString = obj.toString();
		out.println(errorString);
		out.flush();
	}

}
