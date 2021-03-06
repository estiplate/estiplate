package helpingsPackage;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimerTask;

public class StatsTimerTask extends TimerTask {

	private HelpingsDatabase mDatabase;
	private static final long ONE_WEEK = 3600 * 24 * 7 * 1000;
	private static final long TWO_WEEKS = ONE_WEEK * 2;
	private static final long MONTH = ONE_WEEK * 4;
	static private int mRuns = 0;
	static private String mMostGuessesUser = "";
	static private String mMostAccurateUser = "";
	static private ArrayList<StatsUser> mUsers = new ArrayList<StatsUser>();

	public class StatsUser implements Comparable<StatsUser> {

		String username;
		float accuracy;
		int guessCount;

		@Override
		public int compareTo(StatsUser o) {
			return (int) (accuracy - o.accuracy);
		}
	}

	static public boolean isMostAccurate(String username){
		return username.equals(mMostAccurateUser);
	}

	static public boolean mMostGuessesUser(String username){
		return username.equals(mMostGuessesUser);
	}

	@Override
	public void run() {
		if ( mDatabase == null ) {
			mDatabase = new HelpingsDatabase();
			try {
				mDatabase.init();
			} catch (ClassNotFoundException e) {
			}
		}
		Date date = new Date();
		ArrayList<StatsUser> statsUsers = new ArrayList<StatsUser>();
		String mostAccurateUser = "";
		String mostGuessesUser = "";
		try {
			ArrayList<Post> posts = mDatabase.getPostsSince(date.getTime() -  ONE_WEEK );
			ArrayList<Average> averages = mDatabase.getAveragesForPosts(posts);
			ArrayList<User> users = mDatabase.getUsers();
			int mostGuesses = 0;
			float bestAccuracy = -1;
			for ( User user: users ) {

				// No guests in our leader board
				if ( user.guest ) {
					continue;
				}
				System.out.println( "Gathering stats for " + user.name);
				ArrayList<Integer> userGuesses = mDatabase.getUserGuessesForPosts(posts, user.name);
				ArrayList<Float> percentErrors = new ArrayList<Float>();
				ArrayList<Float> calorieErrors = new ArrayList<Float>();
				int i = 0;
				for ( Integer userGuess: userGuesses ) {
					if ( userGuess != null && userGuess > 0 ){
						float guess = (float) userGuess;
						Average a = averages.get(i);
						float average = (float) a.average;
						if ( average > 0 && a.count >= 2) {
							calorieErrors.add( (guess - average) );
							percentErrors.add( ((guess - average) / average) * 100 );
						}
					}
					i++;
				}
				float summedPercentError = 0;
				if (percentErrors.size() == 0 ) {
					continue;
				}
				for( Float error: percentErrors) {
					summedPercentError += Math.abs(error);
				}

				float totalPercentError = (summedPercentError / (float) percentErrors.size() );
				if(percentErrors.size() > mostGuesses) {
					mostGuesses = percentErrors.size();
					mostGuessesUser = user.name;
				}
				if(bestAccuracy < 0 || totalPercentError < bestAccuracy) {
					bestAccuracy = totalPercentError;
					mostAccurateUser = user.name;
				}
				StatsUser statsuser = new StatsUser();
				statsuser.accuracy = totalPercentError;
				statsuser.username = user.name;
				statsuser.guessCount = percentErrors.size();
				statsUsers.add(statsuser);

			}
		} catch (Exception e) {

		}
		System.out.println( "Doing sort");

		Collections.sort(statsUsers);
		mUsers = statsUsers;
		mMostAccurateUser = mostAccurateUser;
		mMostGuessesUser = mostGuessesUser;
		mRuns++;
	}

	static public String getOutput() {
		String output = "";
		int rank = 1;
		for ( StatsUser user: mUsers ) {
			output += "#" + rank + " " + user.username + " - " + user.accuracy + " on " + user.guessCount + " guesses<br>";
			rank++;
		}
		return output;
	}
}
