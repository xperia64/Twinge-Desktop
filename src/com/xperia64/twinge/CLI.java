package com.xperia64.twinge;

import java.util.Scanner;

/**
 * A class for Command Line operations. This handles basic command line logic.
 */
public class CLI implements Runnable {

	private String URL;
	private Downloader downloader;

	private Scanner stdin;

	/**
	 * Standard constructor. Ensures all values are in a state that they may be used.
	 */
	public CLI() {
		URL = "";
		stdin = new Scanner(System.in);
	}

	/**
	 * Gets the next line from the standard input stream (System.in).
	 * 
	 * @return One line from System.in
	 */
	public String getLine() {
		return stdin.nextLine();
	}

	/**
	 * Gets user input from the standard input stream (System.in). It prompts the user with a message first.
	 * Note that this does not do anything fancy to user input...
	 * 
	 * @param msg Message to be displayed.
	 * @return User's response.
	 */
	public String promptForInput(String msg) {

		System.out.print(msg);
		return getLine();

	}

	/**
	 * Sets the URL to download from.
	 * 
	 * @param URL URL to be downloaded from.
	 */
	public void setURL(String URL) {
		this.URL = URL;
	}

	/**
	 * Add a downloader object. The downloader object handles the logic used when downloading.
	 * 
	 * @param downloader Download object to be used.
	 */
	public void addDownloader(Downloader downloader) {
		this.downloader = downloader;
	}

	/**
	 * This method prompts the downloader to attempt a download. This will first confirm if the URL is valid using regex.
	 * Spaces are added by default.
	 * 
	 * @param url URL to attempt downloading from.
	 */
	public void downloadFrom(String url) {
		if (url.toLowerCase().matches(Globals.urlRegex)) {
			downloader
					.attemptToDownload(url.substring(url.lastIndexOf('/') + 1));
		} else if (url.matches(Globals.idRegex)) {
			downloader.attemptToDownload(url);
		} else {
			System.out
					.println("[Twinge-CLI] The entered text does not appear to be a twitch video system VOD URL/ID\n");
		}
		System.out.println("\n\n[Twinge-CLI] Your url is: " + URL + "\n\n");
	}

	/**
	 * This method prompts the downloader to attempt a download. This will first confirm if the URL is valid using regex.
	 * The boolean value can be used to choose if spaces are applied.
	 * 
	 * @param url URL to attempt downloading from.
	 * @param space Add spaces between lines.
	 */
	public void downloadFrom(String url, boolean space) {
		if (url.toLowerCase().matches(Globals.urlRegex)) {
			downloader
					.attemptToDownload(url.substring(url.lastIndexOf('/') + 1));
		} else if (url.matches(Globals.idRegex)) {
			downloader.attemptToDownload(url);
		} else {
			System.out
					.println("[Twinge-CLI] The entered text does not appear to be a twitch video system VOD URL/ID\n");
		}
		if (space) {
			System.out.println("\n\n[Twinge-CLI] Your url is: " + URL + "\n\n");
		} else {
			System.out.println(URL);
		}
	}

	/**
	 * This runs the program until the user quits.
	 */
	@Override
	public void run() {

		System.out
				.println("[Twinge-CLI] Welcome to the CLI for Twinge Desktop!\n\n");

		while (true) {

			String choice = promptForInput("[Twinge-CLI] Enter a VOD URL/ID or type \"exit\" to quit: ");

			if (choice.toLowerCase().equals("exit")) {
				break;
			} else {
				downloadFrom(choice);
			}

		}
	}

}
