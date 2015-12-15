package helpingsPackage;

import java.sql.ResultSet;

import org.json.JSONObject;

public class Comment {

	private static final String USERNAME = "username";
	private static final String COMMENT = "comment";
	private static final String DATE = "date";

	public String username;
	public long date;
	public String comment;

	static Comment creatComment(ResultSet result){

		Comment comment = new Comment();
		try {
			comment.username = result.getString(USERNAME);
			comment.date = result.getLong(DATE);
			comment.comment = result.getString(COMMENT);
		} catch ( Exception SQLExeption ){

		}
		return comment;
	}

	public String toString(){
		return toJson().toString();
	}

	public JSONObject toJson(){

		JSONObject object = new JSONObject();
		try {
			object.put(USERNAME, username);
			object.put(COMMENT, comment);
			object.put(DATE, date);
		} catch (Exception e ) {			
		}

		return object;
	}
}
