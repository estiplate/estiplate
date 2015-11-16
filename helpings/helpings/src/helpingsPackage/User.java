package helpingsPackage;

import javax.servlet.AsyncContext;

public class User {
	String name;
	String token;
	String opponent;
	AsyncContext context;
	int score;
	long lastHeartbeat;
}
