package helpingsPackage;

import java.io.*; 
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(urlPatterns="/user", asyncSupported = true)
public class UserServlet extends EstiplateServlet {

	private static final long serialVersionUID = 1L;

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
		addUsernameCookie(response, username);
		addTokenCookie(response, token);
	}

	private void handleLogin(JSONObject requestJSON, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		String email = (String) requestJSON.optString("email");
		String password = (String) requestJSON.optString("password");
		try {
			User p = mDatabase.login(email, password);
			if ( p != null && p.token != null){
				addTokenCookie(response, p.token);
				addUsernameCookie(response, p.name);
			} else {
				response.sendError(401);
				return;
			}
		} catch (NoSuchAlgorithmException e){

		}
	}

	private void handleChangePassword(JSONObject requestJSON, HttpServletResponse response) throws IOException{
		String username = (String) requestJSON.optString("username");
		String password = (String) requestJSON.optString("password");
		String new_password = (String) requestJSON.optString("new_password");
		try {
			User p = mDatabase.changePassword(username, password, new_password);
			if ( p != null && p.token != null){
				addTokenCookie(response, p.token);
			} else {
				response.sendError(401);
			}
		} catch (NoSuchAlgorithmException e){

		}
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

			handleLogin(requestJSON, request, response);

		} else if ( command.equals("change_password")) {

			handleChangePassword(requestJSON, response);

		}
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		String token = request.getParameter("token");
		String username = request.getParameter("username");

		if ( verifyUrlToken(token, username) ) {
			String action = request.getParameter("action");
			if ( action != null ) {
				if ( action.equals("set_notify_setting")){
					String setting = request.getParameter("notifysetting");
					if ( username != null || setting != null ) {
						try {
							mDatabase.setNotifySetting(username, Integer.decode(setting));
							RequestDispatcher view = request.getRequestDispatcher("/success.html");
							view.forward(request, response);
							return;
						} catch (NoSuchAlgorithmException e){}
					}
				}
			}
		}

		RequestDispatcher view = request.getRequestDispatcher("/failure.html");
		view.forward(request, response);
	}
}
