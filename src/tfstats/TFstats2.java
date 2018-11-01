package tfstats;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
/**
 * Stats Shower for Tetris Friends 1on1
 * Created by kress since 2017/11/14
 */
public class TFstats2 {

	/** プレイ時間[s] */
	private int vTime;

	/** プレイヤー1の総ラインセント数 */
	private int vLine1;

	/** プレイヤー1の総ミノ数 */
	private int vMino1;

	/** プレイヤー1の勝利数 */
	private int vWin1;

	/** プレイヤー2の総ラインセント数*/
	private int vLine2;

	/** プレイヤー2の総ミノ数 */
	private int vMino2;

	/** プレイヤー2の勝利数 */
	private int vWin2;

	/** スタッツ取得の始まり */
	private int vFirstGame;

	/**
	 * コンストラクタ
	 */
	public TFstats2() {
		vTime = 0;
		vLine1 = 0;
		vMino1 = 0;
		vWin1 = 0;
		vLine2 = 0;
		vMino2 = 0;
		vWin2 = 0;
		vFirstGame = vWin1 + vWin2 + 1;
	}

	/**
	 * URLのソースコードを得る
	 * @param url URL
	 * @return ソースコード
	 * @throws IOException
	 */
	public ArrayList<String> getSourceText(URL url) throws IOException {
		InputStream in = url.openStream();
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
	 * @param line1 プレイヤー1のラインセント
	 * @param mino1 プレイヤー1の置いたミノ数
	 * @param line2 プレイヤー2のラインセント
	 * @param mino2 プレイヤー2の置いたミノ数
	 */
	public void printStats(int time, int line1, int mino1, int line2, int mino2) {
		int min = time / 60;
		int sec = time % 60;
		String SEC = String.format("%02d", sec);
		assert min * 60 + sec == time;
		double apm1 = (double)line1 * 60 / time;
		double tpm1 = (double)mino1 * 60 / time;
		double apm2 = (double)line2 * 60 / time;
		double tpm2 = (double)mino2 * 60 / time;
		String APM1 = String.format("%5.1f", apm1);
		String TPM1 = String.format("%5.1f", tpm1);
		String APM2 = String.format("%5.1f", apm2);
		String TPM2 = String.format("%5.1f", tpm2);
		String WIN1 = String.format("%2d", vWin1);
		String WIN2 = String.format("%2d", vWin2);
		System.out.println("|" + APM1 + "|" + TPM1 + "|" + WIN1 + "|" + min + ":" + SEC + "|" + WIN2
				+ "|" + TPM2 + "|" + APM2 + "|");
	}

	/**
	 * 戦績を保存する
	 * @param time プレイ時間
	 * @param line1 プレイヤー1のラインセント
	 * @param mino1 プレイヤー1の置いたミノ数
	 * @param line2 プレイヤー2のラインセント
	 * @param mino2 プレイヤー2の置いたミノ数
	 */
	public void add(int time, int line1, int mino1, int line2, int mino2) {
		vTime += time;
		vLine1 += line1;
		vMino1 += mino1;
		vLine2 += line2;
		vMino2 += mino2;
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

	public static void main(String[] args) throws MalformedURLException, IOException {
		TFstats2 stats = new TFstats2();
		String username1 = "kazukazu07";
		String username2 = "CrazyHIKO";
		int userID1 = 0;
		int userID2 = 0;
		boolean HD_exist = true;
		int place1 = 0;
		int place2 = 0;
		if(HD_exist) {
			String[] str = stats.readID(username1);
			userID1 = Integer.parseInt(str[6]);
			str = stats.readID(username2);
			userID2 = Integer.parseInt(str[6]);
		}
		int firstto = 15;
		boolean duce = false;
		boolean TTO = false;
		String[] data1, data2;
		data1 = stats.readStats(username1, userID1);
		data2 = stats.readStats(username2, userID2);
		while(stats.checkPlaceofStats(data1, place1)) {
			place1 += 9;
		}
		while(stats.checkPlaceofStats(data2, place2)) {
			place2 += 9;
		}
		//System.out.println(place1 + " : " + place2);
		int i = 0;
		int games1, games2, mino1, mino2, line1, line2, win1, win2;
		games1 = Integer.parseInt(data1[35 + place1]);
		mino1 = Integer.parseInt(data1[67 + place1]);
		line1 = Integer.parseInt(data1[59 + place1]);
		win1 = Integer.parseInt(data1[5 + place1]);
		games2 = Integer.parseInt(data2[35 + place2]);
		mino2 = Integer.parseInt(data2[67 + place2]);
		line2 = Integer.parseInt(data2[59 + place2]);
		win2 = Integer.parseInt(data2[5 + place2]);
		Scanner scan = null;
		Pattern p = Pattern.compile(":");
		String[] timestr;
		timestr = p.split(data1[43 + place1]);
		int time = Integer.parseInt(timestr[1]) * 60 + Integer.parseInt(timestr[2]);
		System.out.println(username1 + " vs " + username2);
		double start = System.currentTimeMillis();
		double millitime = start;
		double gametime = 0;
		boolean gameend;
		while(i < 300) {
			if(stats.vWin1 >= firstto || stats.vWin2 >= firstto) {
				if(duce) {
					if(Math.abs(stats.vWin1 - stats.vWin2) >= 2) {
						break;
					}
				} else {
					break;
				}
			}
			if(TTO) {
				if((stats.vWin1 == 10 && stats.vWin2 == 0) || (stats.vWin1 == 0 && stats.vWin2 == 10)) {
					break;
				}
			}
			if(System.currentTimeMillis() - millitime > 1000) {
				gameend = false;
				millitime = System.currentTimeMillis();
				data1 = stats.readStats(username1, userID1);
				data2 = stats.readStats(username2, userID2);
				if(games1 != Integer.parseInt(data1[35 + place1]) && games2 != Integer.parseInt(data2[35 + place2])) {
					if(win1 != Integer.parseInt(data1[5 + place1])) {
						gameend = true;
					}
					if(win2 != Integer.parseInt(data2[5 + place2])) {
						gameend = true;
					}
				}
				if(gameend) {
					games1++;
					games2++;
					gametime = System.currentTimeMillis() - start;
					start = System.currentTimeMillis();
					int thismino1, thismino2, thisline1, thisline2;
					thismino1 = Integer.parseInt(data1[67 + place1]) - mino1;
					thisline1 = Integer.parseInt(data1[59 + place1]) - line1;
					thismino2 = Integer.parseInt(data2[67 + place2]) - mino2;
					thisline2 = Integer.parseInt(data2[59 + place2]) - line2;
					timestr = p.split(data1[43 + place1]);
					mino1 += thismino1;
					line1 += thisline1;
					mino2 += thismino2;
					line2 += thisline2;
					int timetemp = Integer.parseInt(timestr[1]) * 60 + Integer.parseInt(timestr[2]);
					int thistime;
					if(win1 != Integer.parseInt(data1[5 + place1])) {
						stats.vWin1++;
						win1++;
					}
					if(win2 != Integer.parseInt(data2[5 + place2])) {
						stats.vWin2++;
						win2++;
					}
					if(timetemp < time) {
						thistime = timetemp + 3600 - time;
					} else if(timetemp == time) {
						if(stats.vWin1 + stats.vWin2 == stats.vFirstGame) {
							System.out.println("input time");
							scan = new Scanner(System.in);
							thistime = scan.nextInt();
							scan.close();
						} else {
							thistime = (int)(gametime / 1000.0 - 24.5);
							if(thistime > 30) {
								thistime = 30;
							}
						}
					} else {
						thistime = timetemp - time;
					}
					if(stats.vWin1 + stats.vWin2 == stats.vFirstGame) {
						System.out.println("| APM | TPM |  |time|  | TPM | APM |");
					}
					stats.printStats(thistime, thisline1, thismino1, thisline2, thismino2);
					stats.add(thistime, thisline1, thismino1, thisline2, thismino2);
					time = timetemp;
					i = 0;
				} else {
					i++;
				}
			}
		}
		System.out.println("END");
		int min = stats.vTime / 60;
		int sec = stats.vTime % 60;
		double apm1 = (double)stats.vLine1 * 60 / stats.vTime;
		double tpm1 = (double)stats.vMino1 * 60 / stats.vTime;
		double apm2 = (double)stats.vLine2 * 60 / stats.vTime;
		double tpm2 = (double)stats.vMino2 * 60 / stats.vTime;
		String SEC = String.format("%02d", sec);
		String APM1 = String.format("%5.1f", apm1);
		String TPM1 = String.format("%5.1f", tpm1);
		String APM2 = String.format("%5.1f", apm2);
		String TPM2 = String.format("%5.1f", tpm2);
		System.out.println("TOTAL TIME " + min + ":" + SEC);
		System.out.println(username1 + " APM:" + APM1 + " TPM:" + TPM1);
		System.out.println(username2 + " APM:" + APM2 + " TPM:" + TPM2);
		System.out.println();
		System.out.println(username1 + " " + stats.vWin1 + " - " + stats.vWin2 + " " + username2);
		if(stats.vWin1 > stats.vWin2) {
			System.out.println(username1 + " win");
		} else if(stats.vWin1 < stats.vWin2) {
			System.out.println(username2 + " win");
		} else {
			System.out.println("draw");
		}
	}
}
