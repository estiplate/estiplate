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

@WebServlet(urlPatterns="/user", asyncSupported = true)
public class UserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private ArrayList<String> mGuestTokens = new ArrayList<String>();
	private HelpingsDatabase mDatabase;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
			mDatabase = new HelpingsDatabase();
		try {
			mDatabase.init();
		} catch (ClassNotFoundException e) {

		}
	}

	private void handleNewUser(JSONObject requestJSON, HttpServletResponse response) throws IOException{

		PrintWriter out = response.getWriter();
		String token = null;
		String username = (String) requestJSON.optString("username");
		String password = (String) requestJSON.optString("password");
		String email = (String) requestJSON.optString("email");
		try {
			token = mDatabase.createNewUser(username, email, password);
		} catch (NoSuchAlgorithmException e){
		}
		if ( token == null ) {
			response.sendError(401);
			return;
		}
		out.write(token);
		out.flush();
	}

	private void handleLogin(JSONObject requestJSON, HttpServletResponse response) throws IOException{
		String email = (String) requestJSON.optString("email");
		String password = (String) requestJSON.optString("password");
		try {
			User p = mDatabase.login(email, password);
			if ( p != null && p.token != null){
				PrintWriter out = response.getWriter();
				try {
					JSONObject responseObject = new JSONObject();
					responseObject.put("token", p.token);
					responseObject.put("username", p.name);
					out.write(responseObject.toString());
				} catch (JSONException  e){
				}
				out.flush();
				out.flush();
			} else {
				response.sendError(401);
			}
		} catch (NoSuchAlgorithmException e){

		}
	}

	private void handleRequestToken(JSONObject requestJSON, HttpServletResponse response) throws IOException{

		String token = (String) requestJSON.optString("token");
		if ( token == null || mGuestTokens.indexOf(token) < 0 ) {
			try {
				token = HelpingsDatabase.getSalt();
			} catch (NoSuchAlgorithmException e){}
			mGuestTokens.add(token);
		}
		PrintWriter out = response.getWriter();
		out.write(token);
		out.flush();
	}

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		BufferedReader reader = request.getReader();
		String line = reader.readLine();
		JSONObject requestJSON = null;
		try {
			requestJSON = new JSONObject(line);
		} catch ( Exception e ) {}

		if ( requestJSON == null ) {
			return;
		}
		response.setContentType("text/html");

		String command = requestJSON.optString("command");
		if ( command == null ) return;

		if ( command.equals("new_user")){

			handleNewUser(requestJSON, response);

		} else if ( command.equals("login")){

			handleLogin(requestJSON, response);

		} else if ( command.equals("request_token")) {

			handleRequestToken(requestJSON, response);

		}
	}

}
