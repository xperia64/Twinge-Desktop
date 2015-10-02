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

@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener, ClipboardOwner {
	final String regex1 = "^(http://|)([w]{3}\\.|)(twitch.tv/).*(/v/)\\d{7,8}$";
	final String regex2 = "^\\d{7,8}$";
	final String regex3 = "^(http://).*(/v1/AUTH_system).*(m3u8)$";
	final String apiTmplUrl = "https://api.twitch.tv/api/vods/%s/access_token";
	final String usherTmplUrl = "http://usher.twitch.tv/vod/%s?nauthsig=%s&allow_source=true&nauth=%s";
	private JButton load;
	private JTextField url;
	private JCheckBox copyTo;
	private JLabel command;
	private JTextField out;
	public GUI()
	{
		getContentPane().setLayout(null);
	     setupGUI();
	     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	void setupGUI()
	{
		Font f = new Font(Font.SANS_SERIF, 3, 10);
		command = new JLabel();
		command.setLocation(10,5);
		command.setSize(300,20);
		command.setText("Please enter a twitch.tv /v/ URL:"); 
		getContentPane().add(command);
		url = new JTextField();
		url.setSize(500,25);
		url.setLocation(10,25);
		getContentPane().add(url);
		out = new JTextField();
		out.setSize(600,25);
		out.setLocation(10,75);
		out.setEditable(false);
		getContentPane().add(out);
		load = new JButton();
		load.setSize(100,25);
		load.setLocation(520, 25);
		load.setFont(f);
		load.setText("Load VODs");
		load.addActionListener(this);
		getContentPane().add(load);
		copyTo = new JCheckBox();
		copyTo.setSize(300,25);
		copyTo.setLocation(10, 55);
		copyTo.setFont(f);
		copyTo.setText("Copy playlist URL to clipboard");
		getContentPane().add(copyTo);
		setLocation(50,50);
		setTitle("Twinge");
		setSize(630,130);
		setVisible(true);
		setResizable(false);
	}
	public static String convertStreamToString(InputStream is) {
	    @SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	public void attemptToDownload(String url)
	{
		final String apiUrl = String.format(apiTmplUrl,url);
		Thread t = new Thread(new Runnable(){

			@Override
			public void run()
			{
				URL url = null;
				try
				{
					url = new URL(apiUrl);
				} catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
				InputStream input = null;
		        try
				{
		        	HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
					connection.connect();
					if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
			        	JOptionPane.showMessageDialog(GUI.this, "This doesn't look like a new twitch video system VOD URL/ID");
			        }
			        input = connection.getInputStream();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
		        
		        String result = convertStreamToString(input);
		        if(result==null||result.isEmpty()||!((result.length()==183||result.length()==184)&&result.startsWith("{\"token\":\"{")&&(result.endsWith("\"}"))))
				{
		        	JOptionPane.showMessageDialog(GUI.this, "Bad twitch API result (Is this a valid video ID?)");
				}
		        System.out.println("Result: "+result);
				String token = result.substring(10,result.length()==184?133:132);
				token = token.replace("\\", "");
				String tokenSig = result.substring(result.length()==184?142:141,result.length()==184?182:181);
				String vodId = result.substring(39,result.length()==184?47:46);
				String usherUrl = String.format(usherTmplUrl, vodId, tokenSig, token);
				url = null;
				try
				{
					url = new URL(usherUrl);
				} catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
				HttpURLConnection connection;
				try
				{
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();
			        if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
			        	JOptionPane.showMessageDialog(GUI.this, "Bad VOD connection");
			        }
			        input = connection.getInputStream();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
		        
		        String resolute = convertStreamToString(input);
		        System.out.println("Result: "+resolute);
		        if(resolute==null||resolute.isEmpty()||!resolute.startsWith("#EXTM3U"))
				{
		        	JOptionPane.showMessageDialog(GUI.this,"Bad twitch VOD result");
					return;
				}
				String[] lines = resolute.split("\n");
				final ArrayList<String> qualities = new ArrayList<String>();
				final ArrayList<String> urls = new ArrayList<String>();
				for(String line : lines)
				{
					if(line.matches(regex3))
					{
						//Video playlist line
						int i = line.lastIndexOf('/');
						if (i > 0) i = line.lastIndexOf('/', i - 1);
						String qual = (i >= 0) ? line.substring(i + 1) : null;
						qual = qual.substring(0,qual.lastIndexOf('/'));
						if(qual==null)
							continue;
						qual = Character.toUpperCase(qual.charAt(0)) + qual.substring(1);
						if(qual.equals("Chunked"))
							qual = "Chunked (Raw)";
						qualities.add(qual);	
						urls.add(line);
					}
				}
				if(!(qualities.size()==urls.size()&&qualities.size()!=0))
					return;
				String[] uaaa = new String[qualities.size()];
				for(int i = 0 ; i<qualities.size(); i++)
				{
					System.out.println(qualities.get(i));
					uaaa[i] = qualities.get(i);
				}
				String choice = (String) JOptionPane.showInputDialog(null, "Choose quality", "Twinge",
				        JOptionPane.QUESTION_MESSAGE, null, uaaa, null );
				int x = -1;
				for(int i = 0; i<qualities.size(); i++)
				{
					if(qualities.get(i).equals(choice))
					{
						x=i;
						break;
					}
				}
				if(x!=-1)
				{
					out.setText(urls.get(x));
					if(copyTo.isSelected())
					{
						setClipboardContents(urls.get(x));
					}
				}else{
					out.setText("Failed");
				}
				
			}});
			
		t.start();
	}
	public void setClipboardContents(String aString){
	    StringSelection stringSelection = new StringSelection(aString);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(stringSelection, this);
	  }
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		String s = url.getText();
		if(s.toLowerCase().matches(regex1))
			attemptToDownload(s.substring(s.lastIndexOf('/')+1));
		else if(s.matches(regex2))
			attemptToDownload(s);
		else
			JOptionPane.showMessageDialog(this, "This doesn't look like a new twitch video system VOD URL/ID");
	}
	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1)
	{
		
	}

}
