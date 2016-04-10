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
public class DeleteServlet extends EstiplateServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		if ( !verifyUserToken(request, response, false) ) {
			return;
		}

		BufferedReader reader = request.getReader();
		String line = reader.readLine();
		JSONObject requestJSON = null;
		long post = 0;
		String username = "";
		try {
			requestJSON = new JSONObject(line);
			username = (String) requestJSON.optString("username");
			post = requestJSON.optLong("post");

		} catch ( Exception e ) {}

		if ( requestJSON == null ) {
			return;
		}

		try {
			mDatabase.deletePost(post, username);
		} catch ( Exception e ) {
			return;
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
