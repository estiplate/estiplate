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
public class GuessServlet extends EstiplateServlet {

	private static final long serialVersionUID = 1L;

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

		if ( !verifyUserToken(request, false) ) {
			return;
		}

		BufferedReader reader = request.getReader();
		String line = reader.readLine();
		JSONObject requestJSON = null;
		long post = 0;
		int guess = 0;
		String username = "";
		try {
			requestJSON = new JSONObject(line);
			username = (String) requestJSON.optString("username");
			post = requestJSON.optLong("post");
			guess = requestJSON.optInt("calories");			
		} catch ( Exception e ) {}

		if ( requestJSON == null ) {
			return;
		}

		Exception ex;
		try {
			mDatabase.createGuess(post, username, guess);
		} catch ( Exception e ) {
			ex = e;
		}


		// Error checking here!!
		int cals = 0;
		int guesscount = 0;
		try {
			Average ave = mDatabase.getAverageGuessForPost(post);
			if ( ave != null ) {
				cals = ave.average;
				guesscount = ave.count;
			}
		} catch ( Exception e ) {
			ex = e;
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		JSONObject obj = new JSONObject();

		try {
			obj.put("rowid", post);
			obj.put("average", cals);
			obj.put("userguess", guess);
			obj.put("guesscount", guesscount);
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
