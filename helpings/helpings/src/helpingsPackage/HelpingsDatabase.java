package helpingsPackage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;

public class HelpingsDatabase
{
	static final String CREATE_USER_TABLE = "create table if not exists users (username string, email string, salt string, hash string, token string)";
	static final String CREATE_USER = "insert into users (username,email,salt,hash,token) values (?,?,?,?,?)";
	static final String GET_USER = "select * from users where username=?";
	static final String GET_USERS = "select * from users";
	static final String GET_USER_BY_EMAIL = "select * from users where email=?";
	static final String GET_USER_BY_TOKEN = "select * from users where token=?";
	static final String UPDATE_USER_PASSWORD = "update users set token=?, hash=?, salt=? where username=?";
	static final String CREATE_POST_TABLE = "create table if not exists posts (rowid integer primary key autoincrement, username string, title string, beforeimage string, afterimage string, calories int, guesses int, date int, tags string)";
	static final String CREATE_POST = "insert into posts (username, title, beforeimage, afterimage, calories, guesses, date, tags) values (?,?,?,?,?,?,?,?)";
	static final String LAST_POST = "select last_insert_rowid() from posts";
	static final String GET_POSTS = "select rowid, username, title, beforeimage, afterimage, date, tags from posts;";
	static final String GET_POSTS_SINCE = "select rowid, username, title, beforeimage, afterimage, date, tags from posts where date > ?;";
	static final String GET_POSTS_IN_RANGE = "select rowid, username, title, beforeimage, afterimage, date, tags from posts order by rowid desc limit ? offset ?;";
	static final String GET_USER_POSTS_IN_RANGE = "select rowid, username, title, beforeimage, afterimage, date, tags from posts where username=? order by rowid desc limit ? offset ?;";
	static final String CREATE_GUESS_TABLE = "create table if not exists guesses (post int, username string, calories int)";
	static final String CREATE_GUESS = "insert into guesses (post, username, calories) values (?,?,?)";
	static final String GET_GUESSES_FOR_POST = "select * from guesses where post=?";
	static final String GET_AVERAGE_FOR_POST = "select avg(calories), count(calories) from guesses where post=?;";
	static final String GET_USER_GUESS_FOR_POST = "select calories from guesses where post=? and username=?;";
	static final String CREATE_COMMENT_TABLE = "create table if not exists comments (post int, username string, comment string, date int)";
	static final String CREATE_COMMENT = "insert into comments (post, username, comment, date) values (?,?,?,?)";
	static final String GET_COMMENTS_FOR_POST = "select * from comments where post=? limit ? offset ?;";
	static final String DELETE_POST = "delete from posts where rowid=? and username=?";
	static final String CREATE_TAG_TABLE = "create table if not exists tags (post int, tag string)";
	static final String CREATE_TAG = "insert into tags (post, tag) values (?,?)";
	static final String GET_POSTS_FOR_TAG_IN_RANGE = "select rowid, username, title, beforeimage, afterimage, date, tags from posts where rowid in (select post from tags where tag=?)  order by rowid desc limit ? offset ? ";
	static final String DATABASE_CONNECTION_STRING = "jdbc:sqlite:/var/www/helpings.db";

	public void init() throws ClassNotFoundException
	{
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			statement.executeUpdate(CREATE_USER_TABLE);
			statement.executeUpdate(CREATE_POST_TABLE);
			statement.executeUpdate(CREATE_GUESS_TABLE);
			statement.executeUpdate(CREATE_COMMENT_TABLE);
			statement.executeUpdate(CREATE_TAG_TABLE);
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
	}

	public User login(String email, String password) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_USER_BY_EMAIL);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, email);
			ResultSet rs = statement.executeQuery();
			if(rs.next())
			{
				String hash = rs.getString("hash");
				String salt = rs.getString("salt");
				String name = rs.getString("username");
				String token = rs.getString("token");

				String calculatedHash = get_SHA_1_SecurePassword(salt,password);
				if ( calculatedHash != null && calculatedHash.equals(hash) ) {
					// Do we ever update the token?  When should we?
/*					String token = getSalt();
					statement = connection.prepareStatement(UPDATE_USER_TOKEN);
					statement.setQueryTimeout(30);  // set timeout to 30 sec.
					statement.setString(1, token);
					statement.setString(2, email);
					statement.executeUpdate();*/
					User user = new User();
					user.email = email;
					user.token = token;
					user.name = name;
					return user;
				} 
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return null;
	}

	public User changePassword(String username, String password, String new_password) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_USER);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			if(rs.next())
			{
				String hash = rs.getString("hash");
				String salt = rs.getString("salt");
				String email = rs.getString("email");
				String token = rs.getString("token");

				String calculatedHash = get_SHA_1_SecurePassword(salt,password);
				if ( calculatedHash != null && calculatedHash.equals(hash) ) {
					salt = getSalt();
					token = getSalt();
					hash = get_SHA_1_SecurePassword(salt,new_password);
					statement = connection.prepareStatement(UPDATE_USER_PASSWORD);
					statement.setQueryTimeout(30);  // set timeout to 30 sec.
					statement.setString(1, token);
					statement.setString(2, hash);
					statement.setString(3, salt);
					statement.setString(4, username);
					statement.executeUpdate();
					User user = new User();
					user.email = email;
					user.token = token;
					user.name = username;
					return user;
				} 
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return null;
	}

	public String createNewUser(String username, String email, String password) throws NoSuchAlgorithmException{

		String salt = getSalt();
		String token = getSalt();
		String hash = get_SHA_1_SecurePassword(salt,password);
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);

			PreparedStatement statement = connection.prepareStatement(GET_USER);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			if( rs.next() ){
				return null;
			}

			statement = connection.prepareStatement(GET_USER_BY_EMAIL);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, email);
			rs = statement.executeQuery();
			if( rs.next() ){
				return null;
			}	
			
			statement = connection.prepareStatement(CREATE_USER);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, username);
			statement.setString(2, email);
			statement.setString(3, salt);
			statement.setString(4, hash);
			statement.setString(5, token);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
			return null;
		}
		finally
		{
			closeConnection(connection);
		}
		return token;
	}

	synchronized public int createPost(String username, String title, String beforeimage, String afterimage, long date, String tags) throws NoSuchAlgorithmException{

		int result = 0;

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);

			PreparedStatement statement = connection.prepareStatement(CREATE_POST);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, username);
			statement.setString(2, title);
			statement.setString(3, beforeimage);
			statement.setString(4, afterimage);
			statement.setInt(5, 0);
			statement.setInt(6, 0);
			statement.setLong(7, date);
			statement.setString(8, tags);

			statement.executeUpdate();

			statement = connection.prepareStatement(LAST_POST);
			ResultSet rs = statement.executeQuery();
			if ( rs.next() )  {
				result = rs.getInt("last_insert_rowid()");
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return result;

	}

	public ArrayList<Post> getPostsInRange(int offset, int limit) throws NoSuchAlgorithmException{

		ArrayList<Post> posts = new ArrayList<Post>();
		if ( limit > 100 ) {
			return null;
		}

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_POSTS_IN_RANGE);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setInt(1, limit);
			statement.setInt(2, offset);
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				posts.add( Post.creatPost(rs) );
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return posts;
	}

	public ArrayList<Post> getPostsSince(long date) throws NoSuchAlgorithmException{

		ArrayList<Post> posts = new ArrayList<Post>();

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_POSTS_SINCE);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, date);
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				posts.add( Post.creatPost(rs) );
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return posts;
	}

	public ArrayList<Post> getPosts() throws NoSuchAlgorithmException{

		ArrayList<Post> posts = new ArrayList<Post>();

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_POSTS);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				posts.add( Post.creatPost(rs) );
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return posts;
	}

	public ArrayList<String> getUsers() throws NoSuchAlgorithmException{

		ArrayList<String> users = new ArrayList<String>();

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_USERS);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				users.add( rs.getString("username") );
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return users;
	}

	public ArrayList<Post> getUserPostsInRange(int offset, int limit, String username) throws NoSuchAlgorithmException{

		ArrayList<Post> posts = new ArrayList<Post>();
		if ( limit > 100 ) {
			return null;
		}

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_USER_POSTS_IN_RANGE);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, username);
			statement.setInt(2, limit);
			statement.setInt(3, offset);
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				posts.add( Post.creatPost(rs) );
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return posts;
	}

	public ArrayList<Post> getTagPostsInRange(int offset, int limit, String tag) throws NoSuchAlgorithmException{

		ArrayList<Post> posts = new ArrayList<Post>();
		if ( limit > 100 ) {
			return null;
		}

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_POSTS_FOR_TAG_IN_RANGE);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, tag);
			statement.setInt(2, limit);
			statement.setInt(3, offset);
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				posts.add( Post.creatPost(rs) );
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return posts;
	}

	// We should store the averages in the table along with the post.  So much faster.
	// Just need to synchronize properly.
	public ArrayList<Average> getAveragesForPosts(ArrayList<Post> posts){
		ArrayList<Average> averages = new ArrayList<Average>();
		try {
			for(Post post: posts) {
				averages.add(getAverageGuessForPost(post.id));
			}
		} catch (NoSuchAlgorithmException e) {

		}
		return averages;
	}

	public void createGuess(long post, String username, int cal) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// In future, this should keep a user from guessing for the same post multiple times.
			// For now, nope.
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);

			PreparedStatement statement = connection.prepareStatement(CREATE_GUESS);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			statement.setString(2, username);
			statement.setInt(3, cal);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
			return;
		}
		finally
		{
			closeConnection(connection);
		}
	}

	public Average getAverageGuessForPost(long post) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_AVERAGE_FOR_POST);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				return new Average(rs.getInt("avg(calories)"), rs.getInt("count(calories)"));
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return null;
	}

	public int getUserGuessForPost(long post, String username) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_USER_GUESS_FOR_POST);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			statement.setString(2, username);
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				return rs.getInt("calories");
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return -1;
	}

	public ArrayList<Integer> getGuessesForPost(long post) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_GUESSES_FOR_POST);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			ResultSet rs = statement.executeQuery();
			ArrayList<Integer> guesses = new ArrayList<Integer>();
			while( rs.next() ) {
				guesses.add(rs.getInt("calories"));
			}	
			return guesses;
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return null;
	}

	public ArrayList<Integer> getUserGuessesForPosts(ArrayList<Post> posts, String username){
		ArrayList<Integer> guesses = new ArrayList<Integer>();
		try {
			for(Post post: posts) {
				guesses.add(getUserGuessForPost(post.id, username));
			}
		} catch (NoSuchAlgorithmException e) {

		}
		return guesses;
	}


	public String getUserForToken(String token){

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_USER_BY_TOKEN);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setString(1, token);
			ResultSet rs = statement.executeQuery();
			if(rs.next())
			{
				return rs.getString("username");
			}
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return null;
	}

	public void createComment(long post, String username, String comment, long date) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// In future, this should keep a user from guessing for the same post multiple times.
			// For now, nope.
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);

			PreparedStatement statement = connection.prepareStatement(CREATE_COMMENT);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			statement.setString(2, username);
			statement.setString(3, comment);
			statement.setLong(4, date);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
			return;
		}
		finally
		{
			closeConnection(connection);
		}
	}

	public ArrayList<Comment> getCommentsForPost(long post, int offset, int limit) {

		ArrayList<Comment> comments = new ArrayList<Comment>();

		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(GET_COMMENTS_FOR_POST);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			statement.setInt(2, limit);
			statement.setInt(3, offset);
			ResultSet rs = statement.executeQuery();
			while( rs.next() ) {
				comments.add( Comment.creatComment(rs) );
			}	
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}
		return comments;
	}

	public boolean deletePost( long post, String username ) {
		boolean success = false;
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);
			PreparedStatement statement = connection.prepareStatement(DELETE_POST);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			statement.setString(2, username);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			closeConnection(connection);
		}

		return success;
	}

	public void createTag(int post, String tag) throws NoSuchAlgorithmException{

		Connection connection = null;
		try
		{
			// In future, this should keep a user from guessing for the same post multiple times.
			// For now, nope.
			connection = DriverManager.getConnection(DATABASE_CONNECTION_STRING);

			PreparedStatement statement = connection.prepareStatement(CREATE_TAG);
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.setLong(1, post);
			statement.setString(2, tag);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
			return;
		}
		finally
		{
			closeConnection(connection);
		}
	}

	private void closeConnection(Connection connection){
		try
		{
			if(connection != null)
				connection.close();
		}
		catch(SQLException e)
		{
			// connection close failed.
			System.err.println(e);
		}
	}

	private static String get_SHA_1_SecurePassword(String passwordToHash, String salt)
	{
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			// Should use Base64 decode here.
			md.update(salt.getBytes());
			byte[] bytes = md.digest(passwordToHash.getBytes());
			StringBuilder sb = new StringBuilder();
			// Should use Base64 encode here.
			for(int i=0; i< bytes.length ;i++)
			{
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		return generatedPassword;
	}

	//Add salt
	static String getSalt() throws NoSuchAlgorithmException
	{
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt.toString();
//		return Base64.getEncoder().encodeToString(salt);
	}

}
