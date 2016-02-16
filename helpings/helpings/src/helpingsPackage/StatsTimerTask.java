package helpingsPackage;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimerTask;

public class StatsTimerTask extends TimerTask {

	private HelpingsDatabase mDatabase;
	private static final long ONE_WEEK = 3600 * 24 * 7 * 1000;
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
		mUsers.clear();
		try {
			ArrayList<Post> posts = mDatabase.getPostsSince(date.getTime() -  ONE_WEEK );
			ArrayList<Average> averages = mDatabase.getAveragesForPosts(posts);
			ArrayList<String> usernames = mDatabase.getUsers();
			int mostGuesses = 0;
			float bestAccuracy = -1;
			for ( String username: usernames ) {
					ArrayList<Integer> userGuesses = mDatabase.getUserGuessesForPosts(posts, username);
					ArrayList<Float> percentErrors = new ArrayList<Float>();
					ArrayList<Float> calorieErrors = new ArrayList<Float>();
					int i = 0;
					for ( Integer userGuess: userGuesses ) {
						if ( userGuess != null && userGuess > 0 ){
							float guess = (float) userGuess;
							Average a = averages.get(i);
							float average = (float) a.average;
							if ( average > 0 && a.count > 2) {
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
						mMostGuessesUser = username;
					}
					if(bestAccuracy < 0 || totalPercentError < bestAccuracy) {
						bestAccuracy = totalPercentError;
						mMostAccurateUser = username;
					}
					StatsUser user = new StatsUser();
					user.accuracy = totalPercentError;
					user.username = username;
					user.guessCount = percentErrors.size();
					mUsers.add(user);

			}
		} catch (Exception e) {
			
		}
		Collections.sort(mUsers);
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
