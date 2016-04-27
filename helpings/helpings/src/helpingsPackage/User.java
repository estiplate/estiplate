package helpingsPackage;

import java.net.URLDecoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class User {

	public static final int NOTIFY_ALL = 0xffff;
	public static final int NOTIFY_NONE = 0;

	public String name;
	public String token;
	public String email;
	public boolean admin;
	public int notifySetting;
	public boolean guest;
}
