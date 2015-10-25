package com.xperia64.twinge;

import java.util.ArrayList;
import java.util.List;

public class Twinge {

	public static boolean VERBOSE = false;
	public static String QUALITY;

	public static void printFlags() {
		System.out.println("Usage:");
		System.out.println("java -jar Twinge <arguments> <URLs>\n");
		System.out.println("\tArguments:");
		System.out.println("\t-c                use the command line interface.");
		System.out.println("\t-q [QUALITY]      set the quality of downloads.");
		System.out.println("\t-v                be verbose.");
	}

	public static void main(String[] args) {

		boolean useGUI = true;
		List<String> URLs = new ArrayList<>();

		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.equals("-h")) {
				printFlags();
				return;
			} else if (s.equals("-c")) {
				useGUI = false;
			} else if (s.equals("-v")) {
				VERBOSE = true;
			} else if (s.equals("-q")) {
				try {
					QUALITY = args[++i];
				} catch (Exception e) {
					System.err.println("Invalid use of flag '-q'");
					printFlags();
					return;
				}
			} else {
				URLs.add(args[i]);
			}
		}

		if (useGUI) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					GUI gui = new GUI();
					Downloader downloader = new Downloader();

					gui.addDownloader(downloader);
					downloader.addAssociatedGui(gui);
				}
			});
		} else {

			CLI cli = new CLI();
			Downloader downloader = new Downloader();

			cli.addDownloader(downloader);
			downloader.addAssociatedCli(cli);

			if (URLs.size() > 0) {
				for (String url : URLs) {
					cli.downloadFrom(url, Twinge.VERBOSE);
				}
				return;
			}

			Thread t = new Thread(cli);
			t.start();

		}
	}
}
