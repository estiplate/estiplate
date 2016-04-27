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

@WebServlet(urlPatterns="/comment", asyncSupported = true)
public class CommentServlet extends EstiplateServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		String username = verifyUserToken(request,response, false, true);
		if ( username == null ) {
			return;
		}

		BufferedReader reader = request.getReader();
		String line = reader.readLine();
		JSONObject requestJSON = null;
		long post = 0;
		String comment = "";
		long date = System.currentTimeMillis();
		try {
			requestJSON = new JSONObject(line);
			post = requestJSON.optLong("post");
			comment = requestJSON.optString("comment");
		} catch ( Exception e ) {}

		if ( requestJSON == null ) {
			return;
		}

		// Only save guesses from logged in users (for now)
		try {
			// Strip html.  FIXME use Jsoup
			comment = comment.replaceAll("\\<[^>]*>","");
			mDatabase.createComment(post, username, comment, date);
		} catch ( Exception e ) {}


		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		JSONObject commentJson = new JSONObject();
		JSONObject commentResponse = new JSONObject();
		JSONArray commentArray = new JSONArray();

		try {
			commentJson.put("comment", comment);
			commentJson.put("username", username);
			commentJson.put("date", date);

			commentArray.put(commentJson);

			commentResponse.put("postId", post);
			commentResponse.put("comments", commentArray);
		} catch (JSONException e){}


		String commentString = commentResponse.toString();
		out.println(commentString);
		out.flush();
	}

}
