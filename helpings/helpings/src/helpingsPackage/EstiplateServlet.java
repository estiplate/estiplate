package helpingsPackage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

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
			User user = mDatabase.getUserForToken(token);
			if ( user != null ) {
				String name = user.name;
				if ( name != null && name.length() > 0 ) {
					if ( name.equals(username) ) {
						success = true;
					}
				}
			}
		}
		return success;
	}
	
	public String verifyUserToken(HttpServletRequest request, HttpServletResponse response, boolean admin, boolean allowGuest) throws IOException {
		return verifyUserToken(request, response, admin, allowGuest, true);
	}

	public String verifyUserToken(HttpServletRequest request, HttpServletResponse response, boolean admin, boolean allowGuest, boolean createGuest) throws IOException {
		boolean success = false;

		String username = "";
		String token = "";

		Cookie[] cookies = request.getCookies();
		if ( cookies != null ) {
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
		}

		if ( token != null && token.length() > 0 ) {
			User user = mDatabase.getUserForToken(token);
			if ( user != null ) {
				String name = user.name;
				if ( user.guest && !allowGuest ) {
					// Don't reset the token if they were just trying something
					// they can't do as a guest
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
					return null;
				}

				if ( name != null && name.length() > 0 &&  name.equals(username) ) {
					if ( ( user.admin && admin ) || ( user.guest && allowGuest ) || (!admin && !user.guest) ) {
						success = true;
					}
				}
			}

		} else if ( allowGuest && !admin ) {

			// We don't want to set cookies unless the user takes an
			// action, so this lets us avoid this for the feed case.
			if ( createGuest ) {
				// No token? No problem.  Guest mode to the rescue!
				User user;
				try {
					user = mDatabase.createNewGuestUser();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return null;
				}
				if ( user == null ) {
					return null;
				}

				addGuestCookie(response, true);
				addUsernameCookie(response, user.name);
				addTokenCookie(response, user.token);
				return user.name;
			} else {
				return null;
			}
		}

		// If they sent the wrong token, log them out.
		if ( !success && !admin ){
			deleteCookie("token",response);
			deleteCookie("username",response);
		}
		return username;
	}

	protected String getCookie(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		for( int i = 0; i < cookies.length; i++ ) {
			if ( cookies[i].getName().equals(key)) {
				return cookies[i].getValue();
			}
		}
		return null;
	}

	protected void deleteCookie(String name, HttpServletResponse response) {
		Cookie tokenCookie = new Cookie(name, "");
		tokenCookie.setMaxAge(0);
		response.addCookie(tokenCookie);
	}
	
	protected void addCookie(HttpServletResponse response, String key, String value, int maxAge) {
		Cookie cookie;
		try {
			cookie = new Cookie(key, URLEncoder.encode(value, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		cookie.setMaxAge(maxAge);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	protected void addGuestCookie(HttpServletResponse response, boolean guest) {
		addCookie( response, "guest", Boolean.toString(guest), Integer.MAX_VALUE);
	}

	protected void addUsernameCookie(HttpServletResponse response, String username) {
		addCookie( response, "username", username, Integer.MAX_VALUE);
	}

	protected void addTokenCookie(HttpServletResponse response, String token) {
		addCookie( response, "token", token, TOKEN_EXPIRY);
	}
}
