package com.xperia64.twinge;

import java.io.BufferedReader;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class CLI implements Runnable {

	final String regex1 = "^(http://|)([w]{3}\\.|)(twitch.tv/).*(/v/)\\d{7,8}$";
	final String regex2 = "^\\d{7,8}$";

	private String URL;
	private Downloader downloader;
	
	private Scanner stdin;

	public CLI() {
		URL = "";
		stdin = new Scanner(System.in);
	}

	public String getLine(){
		return stdin.nextLine();
	}
	
	public String promptForInput(String msg){
		
		System.out.print(msg);
		return getLine();
		
	}
	
	public void setURL(String URL) {
		this.URL = URL;
	}

	public void addDownloader(Downloader downloader) {
		this.downloader = downloader;
	}

	public void downloadFrom(String url) {
		if (url.toLowerCase().matches(regex1)) {
			downloader
					.attemptToDownload(url.substring(url.lastIndexOf('/') + 1));
		} else if (url.matches(regex2)) {
			downloader.attemptToDownload(url);
		} else {
			System.out
					.println("[Twinge-CLI] The entered text does not appear to be a twitch video system VOD URL/ID\n");
		}
		System.out.println("\n\n[Twinge-CLI] Your url is: " + URL + "\n\n");
	}

	/*
	 * String s = url.getText(); if (s.toLowerCase().matches(regex1))
	 * downloader.attemptToDownload(s.substring(s.lastIndexOf('/') + 1)); else
	 * if (s.matches(regex2)) downloader.attemptToDownload(s); else
	 * JOptionPane.showMessageDialog
	 * (this,"This doesn't look like a new twitch video system VOD URL/ID");
	 */

	@Override
	public void run() {
		
		System.out.println("[Twinge-CLI] Welcome to the CLI for Twinge Desktop!\n\n");
		
		while (true) {
			
			String choice = promptForInput("[Twinge-CLI] Enter a VOD URL/ID or type \"exit\" to quit: ");
			
			if(choice.toLowerCase().equals("exit")){
				break;
			} else{
				downloadFrom(choice);
			}
			
		}
	}

}
