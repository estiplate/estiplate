package helpingsPackage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EstiplateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int TOKEN_EXPIRY = 60 * 60 * 24 * 30; // Thirty days

	protected HelpingsDatabase mDatabase;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		mDatabase = new HelpingsDatabase();
		try {
			mDatabase.init();
		} catch (ClassNotFoundException e) {

		}
	}

	public boolean verifyUrlToken(String token, String username) throws UnsupportedEncodingException {
		
		if ( token == null || token.length() == 0 || username == null || username.length() == 0 ) {
			return false;
		}

		boolean success = false;
		if ( token != null && token.length() > 0 ) {
			token = URLDecoder.decode(token, "UTF-8");
			String name = mDatabase.getUserForToken(token);
			if ( name != null && name.length() > 0 ) {
				if ( name.equals(username) ) {
					success = true;
				}
			}
		}
		return success;
	}

	public boolean verifyUserToken(HttpServletRequest request, HttpServletResponse response, boolean admin) {
		boolean success = false;

		String username = "";
		String token = "";

		Cookie[] cookies = request.getCookies();
		for( int i = 0; i < cookies.length; i++ ) {
			if ( cookies[i].getName().equals("username")) {
				username = cookies[i].getValue();
			}
			if ( cookies[i].getName().equals("token")) {
				try {
					token = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if ( token != null && token.length() > 0 ) {
			String name = admin ? mDatabase.getAdminForToken(token): mDatabase.getUserForToken(token);
			if ( name != null && name.length() > 0 ) {
				if ( name.equals(username) ) {
					success = true;
				}
			}
		}

		// If they sent the wrong token, log them out.
		if ( !success && !admin){
			deleteTokenCookie(response);
		}
		return success;
	}

	protected void deleteTokenCookie(HttpServletResponse response) {
		Cookie tokenCookie = new Cookie("token", "");
		tokenCookie.setMaxAge(0);
		response.addCookie(tokenCookie);
	}
	
	protected void addTokenCookie(HttpServletResponse response, String token) {
		Cookie cookie;
		try {
			cookie = new Cookie("token", URLEncoder.encode(token, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		cookie.setMaxAge(TOKEN_EXPIRY);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	protected void addUsernameCookie(HttpServletResponse response, String username) {
		Cookie cookie = new Cookie("username", username);
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
}
