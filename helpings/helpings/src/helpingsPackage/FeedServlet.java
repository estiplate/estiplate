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

@WebServlet(urlPatterns = {"","/feed/*", "/users/*", "/tag/*"}, asyncSupported = true)
public class FeedServlet extends EstiplateServlet {

	private static final long serialVersionUID = 1L;

	private static final String FILTER_USERS = "users";
	private static final String FILTER_TAG = "tag";
	private static final String FILTER_FEED = "feed";
	private static final int PLATES_PER_PAGE = 18;
	private static final int INITIAL_COMMENTS = 10;

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache");

		String uri = request.getRequestURI();
		String[] pathSegments = uri.split("/");
		String filter = "";
		String filterParam = "";
		int page = 1;
		if ( pathSegments.length > 1 ) {
			filter = pathSegments[1];
			if ( filter.equals(FILTER_FEED) ) {
				if ( pathSegments.length > 2 ) {
					String pageString = pathSegments[2];
					page = Integer.parseInt(pageString);
				}
			} else {
				if ( pathSegments.length > 2 ) {
					filterParam = pathSegments[2];
				}
				if ( pathSegments.length > 3 ) {
					String pageString = pathSegments[3];
					page = Integer.parseInt(pageString);
				}
			}
		}
		Map<String, String[]>params = request.getParameterMap();
		int offset = (page - 1) * PLATES_PER_PAGE;
		int limit = PLATES_PER_PAGE;
		boolean json = false;
		String username = null;
		// FIXME
		for (Entry<String, String[]> entry: params.entrySet()){
			if( entry.getKey().equals("offset")) {
				offset = Integer.valueOf(entry.getValue()[0]);
			}
			if( entry.getKey().equals("limit")) {
				limit = Integer.valueOf(entry.getValue()[0]);
			}
			if( entry.getKey().equals("json")) {
				json = Boolean.valueOf(entry.getValue()[0]);
			}
			if( entry.getKey().equals("username")) {
				username = entry.getValue()[0];
			}
		}

		if ( json ) {

			ArrayList<Post> posts = null;
			ArrayList<Average> averages = null;
			ArrayList<Integer> userGuesses = null;
			try {
				if ( filter.equals(FILTER_USERS)) {
					posts = mDatabase.getUserPostsInRange(offset, limit, filterParam);
				} else if ( filter.equals(FILTER_TAG) ) {
					String tag = java.net.URLDecoder.decode(filterParam, "UTF-8");
					posts = mDatabase.getTagPostsInRange(offset, limit, tag);
				} else {
					posts = mDatabase.getPostsInRange(offset, limit);					
				}
				averages = mDatabase.getAveragesForPosts(posts);
				if ( username != null && username.length() > 0 && verifyUserToken(request, false)) {
					userGuesses = mDatabase.getUserGuessesForPosts(posts, username);
				}
			} catch (Exception e){
			}
			if ( posts == null ) {
				return;
			} else {
				ArrayList<Comment> comments = null;
				JSONArray array = new JSONArray();
				int count = 0;
				for ( Post post: posts) {
					JSONObject postData = new JSONObject();
					try {
						postData.put( "post", post.toJson());
						Average ave = averages.get(count);
						int average = 0;
						int guesscount = 0;
						if ( ave != null ) {
							average = ave.average;
							guesscount = ave.count;
						}
						postData.put( "average", average);
						postData.put( "guesscount", guesscount);
						if ( userGuesses != null ) {
							postData.put( "userguess", userGuesses.get(count));

							// Only add the comments if the user has made a guess
							if ( userGuesses.get(count) > 0 ) {
								comments = mDatabase.getCommentsForPost(post.id, 0, INITIAL_COMMENTS);
								JSONArray commentsJson = new JSONArray();
								for( Comment comment: comments ){
									commentsJson.put(comment.toJson());
								}
								postData.put("comments", commentsJson);
							}
						}
						array.put(postData);
					} catch (JSONException e) {
					}
					count++;
				}
				out.println(array.toString());
			}
		} else {
			response.setContentType("text/html");
			RequestDispatcher view = request.getRequestDispatcher("/index.html");
			view.forward(request, response);
		}
	}

}
