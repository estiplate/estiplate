package helpingsPackage;

import java.sql.ResultSet;

import org.json.JSONObject;

public class Post {

	private static final String BEFORE_IMAGE_URL = "beforeimage";
	private static final String AFTER_IMAGE_URL = "afterimage";
	private static final String TITLE = "title";
	private static final String USERNAME = "username";
	private static final String ID = "rowid";
	private static final String DATE = "date";

	public String beforeImageUrl;
	public String afterImageUrl;
	public String title;
	public String tags;
	public String username;
	public long date;
	public int id;

	static Post creatPost(ResultSet result){

		Post post = new Post();
		try {
			post.beforeImageUrl = result.getString(BEFORE_IMAGE_URL);
			post.afterImageUrl = result.getString(AFTER_IMAGE_URL);
			post.title = result.getString(TITLE);
			post.username = result.getString(USERNAME);
			post.id = result.getInt(ID);
			post.date = result.getLong(DATE);
		} catch ( Exception SQLExeption ){

		}
		return post;
	}

	public String toString(){
		return toJson().toString();
	}

	public JSONObject toJson(){

		JSONObject object = new JSONObject();
		try {
			object.put(BEFORE_IMAGE_URL, beforeImageUrl);
			object.put(AFTER_IMAGE_URL, afterImageUrl);
			object.put(TITLE, title);
			object.put(USERNAME, username);
			object.put(ID, id);
			object.put(DATE, date);
		} catch (Exception e ) {			
		}

		return object;
	}

	public String getBadHtml(){
		StringBuilder builder = new StringBuilder();
		builder.append("\n<div class=\"post\">");
		builder.append("<img src=\"image/" + beforeImageUrl +"\" class=\"foodThumb\">\n");
		//		builder.append("<img src=\"image/" + afterImageUrl +"\" class=\"foodThumb\">");
		builder.append("<span class=\"posttitle\">" + title + "</span><br>\n");
		builder.append("<span class=\"username\">User: " + username + "</span><br>\n");
		builder.append("<span  class=\"posttitle\" id=\"calories_" + id + "\" style=\"display:none;\"></span>\n");
		builder.append("<span  class=\"posttitle\" id=\"guess_" + id + "\" style=\"display:none;\"></span><p>\n");
		builder.append("<form class=\"calorieform\" name=\"calform_" + id + "\" onsubmit=\"return sendGuess(this)\">");
		builder.append("<input type=\"text\" name=\"calories\" placeholder=\"?????\">\n");
		builder.append("<input type=\"text\" name=\"post_id\" value=\"" + id + "\"  style=\"display:none;\"/>\n");
		builder.append("<input type=\"submit\" value=\"Guess Calories\"/></form>\n");
		builder.append("</div><p>\n");
		return builder.toString();
	}
}
