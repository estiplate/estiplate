package helpingsPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
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

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		
		if ( !verifyUserToken(request, true) ) {
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
		try {
			requestJSON = new JSONObject(line);
		} catch ( Exception e) {}

		action = requestJSON.optString("action");

		if ( !verifyUserToken(request, false) ) {
			response.sendError(403);
			return;
		}

		if ( action.equals("massemail")) {
			message = requestJSON.optString("message");
			sendMessage(message);
		}

	}

	static final String FROM = "SENDER@EXAMPLE.COM";  // Replace with your "From" address. This address must be verified.
	static final String TO = "RECIPIENT@EXAMPLE.COM"; // Replace with a "To" address. If your account is still in the
	// sandbox, this address must be verified.
	static final String BODY = "This email was sent through Amazon SES by using the AWS SDK for Java.";
	static final String SUBJECT = "Amazon SES test (AWS SDK for Java)";


	public void sendMessage(String msg) throws IOException {    	

		// Construct an object to contain the recipient address.
		Destination destination = new Destination().withToAddresses(new String[]{TO});

		// Create the subject and body of the message.
		Content subject = new Content().withData(SUBJECT);
		Content textBody = new Content().withData(BODY); 
		Body body = new Body().withText(textBody);

		// Create a message with the specified subject and body.
		Message message = new Message().withSubject(subject).withBody(body);

		// Assemble the email.
		SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message);

		try
		{        
			System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");

			// Instantiate an Amazon SES client, which will make the service call. The service call requires your AWS credentials. 
			// Because we're not providing an argument when instantiating the client, the SDK will attempt to find your AWS credentials 
			// using the default credential provider chain. The first place the chain looks for the credentials is in environment variables 
			// AWS_ACCESS_KEY_ID and AWS_SECRET_KEY. 
			// For more information, see http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html
			AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();

			// Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your sandbox 
			// status, sending limits, and Amazon SES identity-related settings are specific to a given AWS 
			// region, so be sure to select an AWS region in which you set up Amazon SES. Here, we are using 
			// the US West (Oregon) region. Examples of other regions that Amazon SES supports are US_EAST_1 
			// and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html 
			Region REGION = Region.getRegion(Regions.US_WEST_2);
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
