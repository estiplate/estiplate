package helpingsPackage;

import java.io.*; 
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(urlPatterns="/stats", asyncSupported = true)
public class StatsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static final long FIVE_MINUTES = 5 * 60 * 1000;
	static StatsTimerTask sStatsTimerTask;

	static {
		Timer timer = new Timer();
		sStatsTimerTask = new StatsTimerTask();
		timer.schedule(sStatsTimerTask, 0, FIVE_MINUTES);
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println(sStatsTimerTask.getOutput());
	}
}
