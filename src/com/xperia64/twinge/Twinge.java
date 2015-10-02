package com.xperia64.twinge;


public class Twinge {
	
	public static void main(String[] args)
	{
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new GUI();
            }
        });
	}
}
