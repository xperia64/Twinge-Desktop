package com.xperia64.twinge;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * This class is the GUI controller for Twinge.
 */
@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener, ClipboardOwner {

	final String regex1 = "^(http://|)([w]{3}\\.|)(twitch.tv/).*(/v/)\\d{7,8}$";
	final String regex2 = "^\\d{7,8}$";
	private JButton load;
	private JTextField url;
	private JCheckBox copyTo;
	private JLabel command;
	private JTextField out;

	private Downloader downloader;

	/**
	 * It's a constructor. It does things.
	 */
	public GUI() {
		getContentPane().setLayout(null);
		setupGUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Adds a downloader object. The downloader handles the actual downloading...
	 * 
	 * @param downloader Downloader object to be added.
	 */
	public void addDownloader(Downloader downloader){
		this.downloader = downloader;
	}
	
	/**
	 * Sets the output textbox to a message.
	 * 
	 * @param msg Message to be output.
	 */
	public void setOutputText(String msg){
		out.setText(msg);
	}
	
	/**
	 * Gets the state of the checkbox for copyTo.
	 * 
	 * @return Is copyTo checked?
	 */
	public boolean copyToIsSelected(){
		return copyTo.isSelected();
	}

	/**
	 * This sets up the GUI... Needs to be run.
	 */
	void setupGUI() {
		Font f = new Font(Font.SANS_SERIF, 3, 10);
		command = new JLabel();
		command.setLocation(10, 5);
		command.setSize(300, 20);
		command.setText("Please enter a twitch.tv /v/ URL:");
		getContentPane().add(command);
		url = new JTextField();
		url.setSize(500, 25);
		url.setLocation(10, 25);
		getContentPane().add(url);
		out = new JTextField();
		out.setSize(600, 25);
		out.setLocation(10, 75);
		out.setEditable(false);
		getContentPane().add(out);
		load = new JButton();
		load.setSize(100, 25);
		load.setLocation(520, 25);
		load.setFont(f);
		load.setText("Load VODs");
		load.addActionListener(this);
		getContentPane().add(load);
		copyTo = new JCheckBox();
		copyTo.setSize(300, 25);
		copyTo.setLocation(10, 55);
		copyTo.setFont(f);
		copyTo.setText("Copy playlist URL to clipboard");
		getContentPane().add(copyTo);
		setLocation(50, 50);
		setTitle("Twinge");
		setSize(630, 130);
		setVisible(true);
		setResizable(false);
	}

	/**
	 * Adds a URL to the clipboard. Used to automatically copy the downloaded URL to the clipboard.
	 * 
	 * @param aString URL to be added.
	 */
	public void setClipboardContents(String aString) {
		StringSelection stringSelection = new StringSelection(aString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	/**
	 * Update logic...
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String s = url.getText();
		if (s.toLowerCase().matches(regex1))
			downloader.attemptToDownload(s.substring(s.lastIndexOf('/') + 1));
		else if (s.matches(regex2))
			downloader.attemptToDownload(s);
		else
			JOptionPane.showMessageDialog(this,"This doesn't look like a new twitch video system VOD URL/ID");
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {

	}

}
