package helpingsPackage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class EstiplateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected HelpingsDatabase mDatabase;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		mDatabase = new HelpingsDatabase();
		try {
			mDatabase.init();
		} catch (ClassNotFoundException e) {

		}
	}

	public boolean verifyUserToken(HttpServletRequest request, boolean admin) {
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
		return success;
	}
}
