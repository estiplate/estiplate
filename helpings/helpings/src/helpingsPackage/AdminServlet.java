package helpingsPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;
import com.amazonaws.regions.*;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@WebServlet(urlPatterns="/admin", asyncSupported = true)
public class AdminServlet extends EstiplateServlet {

	private static final long serialVersionUID = 1L;
	static final String FROM = "\"Estiplate\" <contact@estiplate.com>";

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		if ( !verifyUserToken(request, response, true) ) {
			response.sendError(403);
			return;
		}

		ServletContext ctx = getServletContext();
		InputStream docStream = ctx.getResourceAsStream("/WEB-INF/admin.html");
		OutputStream os = response.getOutputStream();
		IOUtils.copy(docStream, os);
		docStream.close();
		os.close();
	}

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		BufferedReader reader = request.getReader();
		String line = reader.readLine();
		JSONObject requestJSON = null;
		String action = "";
		String message = "";
		String subject = "";
		try {
			requestJSON = new JSONObject(line);
		} catch ( Exception e) {}

		action = requestJSON.optString("action");

		if ( !verifyUserToken(request, response, true) ) {
			response.sendError(403);
			return;
		}

		if ( action.equals("massmail")) {
			subject =  requestJSON.optString("subject");
			message = requestJSON.optString("message");
			ServletContext ctx = getServletContext();
			InputStream docStream = ctx.getResourceAsStream("/WEB-INF/email.html");
			StringWriter writer = new StringWriter();
			IOUtils.copy(docStream, writer, "UTF-8");
			String template = writer.toString();
			ArrayList<User> users;
			try {
				users = mDatabase.getUsers();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return;
			}
			for ( User user: users ) {
				if ( user.notifySetting >= 0 ) {
					String token = URLEncoder.encode(user.token, "UTF-8");
					String unsubscribeLink = "<p><a href = \"http://estiplate.com/user?action=set_notify_setting&username=" + 
							user.name + "&token=" + token + "&notifysetting=-1\">Unsubscribe</a>"; 
					String finalMessage = template.replace("{content}", message + unsubscribeLink).replace("{username}", user.name);
					sendMessage(subject, finalMessage, user.email, FROM);
				}
			}
		}

	}

	public void sendMessage(String sub, String html, String to, String from) throws IOException {    	

		Destination destination = new Destination().withToAddresses(to);

		Content subject = new Content().withData(sub);
		Content htmlBody = new Content().withData(html); 
		Body body = new Body().withHtml(htmlBody);

		Message message = new Message().withSubject(subject).withBody(body);
		SendEmailRequest request = new SendEmailRequest().withSource(from).withDestination(destination).withMessage(message);

		try
		{        
			System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");

			AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
			Region REGION = Region.getRegion(Regions.US_EAST_1);
			client.setRegion(REGION);

			// Send the email.
			client.sendEmail(request);  
			System.out.println("Email sent!");
		}
		catch (Exception ex) 
		{
			System.out.println("The email was not sent.");
			System.out.println("Error message: " + ex.getMessage());
		}
	}
}
