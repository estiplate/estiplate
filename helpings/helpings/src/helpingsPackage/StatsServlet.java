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
	static final long ONE_WEEK = 3600 * 24 * 7 * 1000;
	static final long TEN_MINUTES = 10 * 60 * 1000;
	static StatsTimerTask sStatsTimerTask;

	static {
		Timer timer = new Timer();
		sStatsTimerTask = new StatsTimerTask();
		timer.schedule(sStatsTimerTask, 0, TEN_MINUTES);
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.println(sStatsTimerTask.getOutput());
	}
}
