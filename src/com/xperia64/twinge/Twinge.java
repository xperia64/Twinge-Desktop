package com.xperia64.twinge;

import java.util.ArrayList;

public class Twinge {

	public static void main(String[] args) {

		boolean useGUI = true;

		ArrayList<String> argList = new ArrayList<>();
		for (String s : args) {
			argList.add(s);
			if (s.equals("-h")) {
				FlagHelper fh = new FlagHelper();
				System.out.println(fh.listFlags());
				return;
			}
		}

		if (argList.contains("-c") || argList.contains("-u")) {
			useGUI = false;
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

			if (argList.contains("-u")) {
				cli.downloadFrom(argList.get(argList.indexOf("-u") + 1));
				return;
			}
			
			Thread t = new Thread(cli);
			t.start();
			
		}
	}
}
