package com.xperia64.twinge;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;

/**
 * This class handles the logic to download URLs.
 */
public class Downloader {

	private GUI associatedGUI;
	private CLI associatedCLI;
	final String regex = "^(http://).*(m3u8)$";
	final String apiTmplUrl = "https://api.twitch.tv/api/vods/%s/access_token";
	final String usherTmplUrl = "https://usher.ttvnw.net/vod/%s.m3u8?nauthsig=%s&allow_source=true&nauth=%s";

	/**
	 * Adds a GUI to the downloader. NOTE: A GUI and CLI should not be added to the same downloader object!
	 * If both have been added, the downloader will not know what to do!
	 * 
	 * @param associatedGUI GUI to be added.
	 */
	public void addAssociatedGui(GUI associatedGUI) {
		this.associatedGUI = associatedGUI;
	}

	/**
	 * Adds a CLI to the downloader. NOTE: A GUI and CLI should not be added to the same downloader object!
	 * If both have been added, the downloader will not know what to do!
	 * 
	 * @param associatedCli CLI to be added
	 */
	public void addAssociatedCLI(CLI associatedCli) {
		this.associatedCLI = associatedCli;
	}

	/**
	 * This method converts a stream into a string.
	 * 
	 * @param is Stream to be converted.
	 * @return Contents of the stream.
	 */
	public static String convertStreamToString(InputStream is) {
		@SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/**
	 * This method attempts to download from the URL provided. If it is able to,
	 * it will prompt the user for attributes such as quality so long as the user
	 * has not manually set those attributes.
	 * 
	 * @param url URL to download from.
	 */
	public void attemptToDownload(String url) {
		final String apiUrl = String.format(apiTmplUrl, url);
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				URL url = null;
				try {
					url = new URL(apiUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				InputStream input = null;
				try {
					HttpsURLConnection connection = (HttpsURLConnection) url
							.openConnection();
					connection.connect();
					if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
						if (associatedGUI != null) {
							JOptionPane
									.showMessageDialog(associatedGUI,
											"This doesn't look like a new twitch video system VOD URL/ID");
						} else {
							System.err
									.println("[Twinge-Downloader] This does not appear to be a valid twitch video system VOD URL/ID...");
						}
						return;
					}
					input = connection.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String result = convertStreamToString(input);
				if (result == null
						|| result.isEmpty()
						|| !(result.startsWith("{\"token\":\"{") && (result
									.endsWith("\"}")))) {
					if (associatedGUI != null) {
						JOptionPane
								.showMessageDialog(associatedGUI,
										"Bad twitch API result (Is this a valid video ID?) "+result);
					} else {
						System.err
								.println("[Twinge-Downloader] Bad twitch API result (Is this a valid video ID?)\n"+result);
					}
					return;
				}

				if (Twinge.VERBOSE) {
					System.out.println("[Twinge-Downloader] Result: " + result);
				}	
				String token = result.substring(result.indexOf("\"token\"")+9, result.indexOf("\",\"sig\""));	
				token = token.replace("\\", "");
				String tokenSig = result.substring(result.indexOf("\"sig\":")+7,result.lastIndexOf("\"}")); //result.substring();
				String vodId = result.substring(result.indexOf("\\\"vod_id\\\":")+11, result.indexOf(",",result.indexOf("\\\"vod_id\\\":"))); //result.substring();
				String usherUrl = String.format(usherTmplUrl, vodId, tokenSig,
						token);
				url = null;

				try {
					url = new URL(usherUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				HttpsURLConnection connection;
				try {
					connection = (HttpsURLConnection) url.openConnection();
					connection.connect();
					if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
						if (associatedGUI != null) {
							JOptionPane.showMessageDialog(associatedGUI,
									"Bad VOD connection");
						} else {
							System.err
									.println("[Twinge-Downloader] Bad VOD connection!\n");
						}
						return;
					}
					input = connection.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String resolute = convertStreamToString(input);
				if (Twinge.VERBOSE) {
					System.out.println("[Twinge-Downloader] Result: "
							+ resolute.replaceAll("\n",
									"\n[Twinge-Downloader] ") + "\n");
				}
				if (resolute == null || resolute.isEmpty()
						|| !resolute.startsWith("#EXTM3U")) {
					if (associatedGUI != null) {
						JOptionPane.showMessageDialog(associatedGUI,
								"Bad twitch VOD result");
						return;
					} else {
						System.err
								.println("[Twinge-Downloader] Bad twitch VOD result!\n");
					}
					return;
				}
				String[] lines = resolute.split("\n");
				final ArrayList<String> qualities = new ArrayList<String>();
				final ArrayList<String> urls = new ArrayList<String>();
				for (String line : lines) {
					if (line.matches(regex)) {
						// Video playlist line
						int i = line.lastIndexOf('/');
						if (i > 0)
							i = line.lastIndexOf('/', i - 1);
						String qual = (i >= 0) ? line.substring(i + 1) : null;
						qual = qual.substring(0, qual.lastIndexOf('/'));
						if (qual == null)
							continue;
						qual = Character.toUpperCase(qual.charAt(0))
								+ qual.substring(1);
						if (qual.equals("Chunked"))
							qual = "Chunked (Raw)";
						qualities.add(qual);
						urls.add(line);
					}
				}
				if (!(qualities.size() == urls.size() && qualities.size() != 0)) {
					return;
				}

				String choice;

				if (Twinge.QUALITY != null) {
					choice = Twinge.QUALITY;
				} else {
					String[] uaaa = new String[qualities.size()];
					System.out
							.println("[Twinge-Downloader] Select a quality: ");
					for (int i = 0; i < qualities.size(); i++) {
						System.out.println("[Twinge-Downloader] "
								+ qualities.get(i));
						uaaa[i] = qualities.get(i);
					}

					if (associatedGUI != null) {
						choice = (String) JOptionPane.showInputDialog(null,
								"Choose quality", "Twinge",
								JOptionPane.QUESTION_MESSAGE, null, uaaa, null);
					} else {
						choice = associatedCLI.getLine();
					}
				}
				int x = -1;
				for (int i = 0; i < qualities.size(); i++) {
					if (qualities.get(i).toLowerCase().trim()
							.contains(choice.toLowerCase().trim())) {
						x = i;
						break;
					}
				}

				// This is GUI stuff
				// TODO: Get this out!
				if (x != -1) {
					if (associatedGUI != null) {
						associatedGUI.setOutputText(urls.get(x));
						if (associatedGUI.copyToIsSelected()) {
							associatedGUI.setClipboardContents(urls.get(x));
						}
					} else {
						associatedCLI.setURL(urls.get(x));
					}
				} else {
					if (associatedGUI != null) {
						associatedGUI.setOutputText("Failed");
					} else {
						associatedCLI.setURL("Failed");
					}
					return;
				}

			}
		});

		if (associatedGUI != null) {
			t.start();
		} else {
			t.run();
		}

	}

}
