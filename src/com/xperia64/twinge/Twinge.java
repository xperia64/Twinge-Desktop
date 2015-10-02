package com.xperia64.twinge;


public class Twinge {
	
	public static void main(String[] args)
	{
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	GUI gui = new GUI();
            	Downloader downloader = new Downloader();
            	
            	gui.addDownloader(downloader);
            	downloader.addAssociatedGui(gui);
            }
        });
	}
}
