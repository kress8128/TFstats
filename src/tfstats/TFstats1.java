package tfstats;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
/**
 * Stats Shower for Tetris Friends
 * Created by kress since 2017/11/14
 */
public class TFstats1 {

	/** プレイ時間[s] */
	private int vTime;

	/** 総ラインセント数 */
	private int vLine;

	/** 総ミノ数 */
	private int vMino;

	/** URL読み込みのリトライ許容数 */
	private static final int RETRY_NUM = 100;

	/**
	 * コンストラクタ
	 */
	public TFstats1() {
		vTime = 0;
		vLine = 0;
		vMino = 0;
	}

	/**
	 * userIDを手動入力する
	 * HardDropのサイトが動いていないときはこれを使う
	 * @param scan Scanner
	 * @return userID
	 */
	public int setID(Scanner scan) {
		while(true) {
			try {
				System.out.println("Input userID");
				int id = scan.nextInt();
				return id;
			} catch (Exception e) {
			}
		}
	}

	/**
	 * スタッツが記録されている位置を確認する
	 * アカウントを取得した時期等に応じて変化するため
	 * @param data 戦績
	 * @param place このメソッドで調整する位置
	 * @return placeが現在の数値で合っているかどうか
	 */
	public boolean checkPlaceofStats(String[] data, int place) {
		try {
			Integer.parseInt(data[35 + place]);
		} catch (NumberFormatException e) {
			return true;
		}
		return false;
	}

	/**
	 * 練習モードをプレイしたかどうかを判断する
	 * ノックアウトされて、まだ試合が続いている間に練習モードをプレイするとその試合の戦績がリセットされるため
	 * @param line 試合で記録されたラインセント数
	 * @param scan Scanner
	 * @return 練習モードをプレイしていないかどうか
	 */
	public static boolean checknotPractice(int line, Scanner scan) {
		if(line != 0) {
			return true;
		}else {
			boolean bool = true;
			while(true) {
				System.out.println("practice?(true/false)");
				try {
					bool = !scan.nextBoolean();
					break;
				} catch(Exception e) {}
			}
			return bool;
		}
	}

	/**
	 * 時間を入力してもらう
	 * 30秒未満にノックアウトor試合終了した場合記録されないため
	 * @param scan Scanner
	 * @return 試合時間
	 */
	public static int timeInput(Scanner scan) {
		int time = 0;
		while(true) {
			System.out.println("Input time");
			try {
				time = scan.nextInt();
				break;
			} catch(Exception e) {}
		}
		return time;
	}

	/**
	 * URLのソースコードを得る
	 * @param url URL
	 * @return ソースコード
	 * @throws IOException
	 */
	public ArrayList<String> getSourceText(URL url) throws IOException {
		InputStream in = null;
		for(int i = 0;i < RETRY_NUM;++i) {
			try {
				in = url.openStream();
				break;
			} catch(SocketException e) {
			}
		}
		ArrayList<String> strarray = new ArrayList<String>();
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(in));
			String s;
			while ((s=bf.readLine())!=null) {
				strarray.add(s);
			}
		} finally {
			in.close();
		}
		return strarray;
	}

	/**
	 * ユーザーIDを得る
	 * Tetris Friendsのサイト上ではユーザーIDをユーザー名から獲得することは出来ないので、HardDropのサイトで得る
	 * @param username ユーザー名
	 * @return ユーザーID
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String[] readID(String username) throws MalformedURLException, IOException {
		ArrayList<String> strarray = this.getSourceText(new URL(
				"http://harddrop.com/file/tfstats.php?username=" + username));
		String str = strarray.get(22);
		Pattern p = Pattern.compile("[=\"]+");
		String[] dataID = p.split(str);
		return dataID;
	}

	/**
	 * 戦績が入っている文字列を得る
	 * @param username ユーザー名
	 * @param userID ユーザーID
	 * @return 戦績
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String[] readStats(String username, int userID) throws MalformedURLException, IOException {
		ArrayList<String> strarray = this.getSourceText(new URL(
				"https://www.tetrisfriends.com/leaderboard/ajax/mp_user_stats.php?username=" + username
				+ "&productId=3&userId=" + userID));
		String str = strarray.get(19) + strarray.get(69);
		Pattern p = Pattern.compile("[<>]+");
		String[] data = p.split(str);
		return data;
	}

	/**
	 * 戦績を出力する
	 * @param time プレイ時間
	 * @param line ラインセント
	 * @param mino 置いたミノ数
	 */
	public void printStats(int time, int line, int mino) {
		int min = time / 60;
		int sec = time % 60;
		String SEC = String.format("%02d", sec);
		assert min * 60 + sec == time;
		double apm = (double)line * 60 / time;
		double tpm = (double)mino * 60 / time;
		double apt = (double)line / mino;
		String APM = String.format("%.2f", apm);
		String TPM = String.format("%.2f", tpm);
		String APT = String.format("%.3f", apt);
		System.out.println("time:" + min + ":" + SEC + " APM:" + APM + " TPM:" + TPM + " APT:" + APT);
	}

	/**
	 * 戦績を保存する
	 * @param time プレイ時間
	 * @param line ラインセント
	 * @param mino 置いたミノ数
	 */
	public void add(int time, int line, int mino) {
		vTime += time;
		vLine += line;
		vMino += mino;
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		TFstats1 stats = new TFstats1();
		Scanner scan = null;
		scan = new Scanner(System.in);
		String username;
		int userID;
		int place = 0;
		String[] data;
		while(true) {
			System.out.println("Input username");
			username = scan.next();
			try{
				String[] str = stats.readID(username);
				userID = Integer.parseInt(str[6]);
				if(userID == 0) {
					System.out.println("This username is invalid");
				} else {
					break;
				}
			}catch(Exception e) {
				System.out.println("Sorry, cannot read userID");
				userID = stats.setID(scan);
			}
		}
		data = stats.readStats(username, userID);
		while(stats.checkPlaceofStats(data, place)) {
			place += 9;
		}
		int games, mino, line;
		games = Integer.parseInt(data[35 + place]);
		mino = Integer.parseInt(data[67 + place]);
		line = Integer.parseInt(data[59 + place]);
		Pattern p = Pattern.compile(":");
		String[] timestr;
		timestr = p.split(data[43 + place]);
		int time = Integer.parseInt(timestr[1]) * 60 + Integer.parseInt(timestr[2]);
		double start = System.currentTimeMillis();
		boolean gameend;
		boolean notPractice;
		System.out.println("Start");

		while(System.currentTimeMillis() - start < 300000) {
			notPractice = true;
			gameend = false;
			data = stats.readStats(username, userID);
			gameend = games != Integer.parseInt(data[35 + place]);
			if(gameend) {
				start = System.currentTimeMillis();
				int thismino, thisline;
				thisline = Integer.parseInt(data[59 + place]) - line;
				line += thisline;
				notPractice = checknotPractice(thisline, scan);
				thismino = Integer.parseInt(data[67 + place]) - mino;
				mino += thismino;
				timestr = p.split(data[43 + place]);
				int timetemp = Integer.parseInt(timestr[1]) * 60 + Integer.parseInt(timestr[2]);
				int thistime;
				if(timetemp < time) {
					thistime = timetemp + 3600 - time;
				} else if(timetemp == time && notPractice) {
					thistime = timeInput(scan);
				} else {
					thistime = timetemp - time;
				}
				if(notPractice) {
					stats.printStats(thistime, thisline, thismino);
					stats.add(thistime, thisline, thismino);
				}
				time = timetemp;
				games++;
			}
		}
		System.out.println("END");
		stats.printStats(stats.vTime, stats.vLine, stats.vMino);
		scan.close();
	}
}
