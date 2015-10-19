package com.xperia64.twinge;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JOptionPane;

public class Downloader {

	private GUI associatedGUI;
	private CLI associatedCLI;
	final String regex = "^(http://).*(/v1/AUTH_system).*(m3u8)$";
	final String apiTmplUrl = "https://api.twitch.tv/api/vods/%s/access_token";
	final String usherTmplUrl = "http://usher.twitch.tv/vod/%s?nauthsig=%s&allow_source=true&nauth=%s";

	public void addAssociatedGui(GUI associatedGUI) {
		this.associatedGUI = associatedGUI;
	}

	public void addAssociatedCli(CLI associatedCli) {
		this.associatedCLI = associatedCli;
	}

	public static String convertStreamToString(InputStream is) {
		@SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

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
									.println("This does not appear to be a valid twitch video system VOD URL/ID...");
						}
					}
					input = connection.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String result = convertStreamToString(input);
				if (result == null
						|| result.isEmpty()
						|| !((result.length() == 183 || result.length() == 184)
								&& result.startsWith("{\"token\":\"{") && (result
									.endsWith("\"}")))) {
					if (associatedGUI != null) {
						JOptionPane
								.showMessageDialog(associatedGUI,
										"Bad twitch API result (Is this a valid video ID?)");
					} else {
						System.err
								.println("Bad twitch API result (Is this a valid video ID?)\n");
					}
				}
				System.out.println("Result: " + result);
				String token = result.substring(10,
						result.length() == 184 ? 133 : 132);
				token = token.replace("\\", "");
				String tokenSig = result.substring(result.length() == 184 ? 142
						: 141, result.length() == 184 ? 182 : 181);
				String vodId = result.substring(39, result.length() == 184 ? 47
						: 46);
				String usherUrl = String.format(usherTmplUrl, vodId, tokenSig,
						token);
				url = null;
				try {
					url = new URL(usherUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				HttpURLConnection connection;
				try {
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();
					if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
						if (associatedGUI != null) {
							JOptionPane.showMessageDialog(associatedGUI,
									"Bad VOD connection");
						} else {
							System.err.println("Bad VOD connection!\n");
						}
					}
					input = connection.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String resolute = convertStreamToString(input);
				System.out.println("Result: " + resolute);
				if (resolute == null || resolute.isEmpty()
						|| !resolute.startsWith("#EXTM3U")) {
					if (associatedGUI != null) {
						JOptionPane.showMessageDialog(associatedGUI,
								"Bad twitch VOD result");
						return;
					} else {
						System.err.println("Bad twitch VOD result!\n");
					}
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
				if (!(qualities.size() == urls.size() && qualities.size() != 0))
					return;
				String[] uaaa = new String[qualities.size()];
				for (int i = 0; i < qualities.size(); i++) {
					System.out.println(qualities.get(i));
					uaaa[i] = qualities.get(i);
				}
				String choice;

				if (associatedGUI != null) {
					choice = (String) JOptionPane.showInputDialog(null,
							"Choose quality", "Twinge",
							JOptionPane.QUESTION_MESSAGE, null, uaaa, null);
				} else{
					choice = associatedCLI.getLine();
				}
				int x = -1;
				for (int i = 0; i < qualities.size(); i++) {
					if (qualities.get(i).toLowerCase().trim().contains(choice.toLowerCase().trim())) {
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
