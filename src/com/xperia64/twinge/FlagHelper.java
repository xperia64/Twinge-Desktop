package com.xperia64.twinge;

/**
 * 
 * @author Michael Hrcek <hrcekmj@clarkson.edu>
 * 
 */
public class FlagHelper {

	Flag[] flags = {new Flag("-h",0,"Shows this help text."),
			new Flag("-u",1,"Defines the url to grab the video playlist from. Use: \"--url something.something/location\""),
			new Flag("-c",0,"Opens the program in CLI mode.")};
	
	public String listFlags(){
		String str = "";
		
		for(Flag f: flags){
			str += f + "\n\n";
		}
		
		return str;
	}
	
	private class Flag {
		String flag;
		int expectedArgs;
		String description;

		public String toString(){
			return flag + "\n" + description;
		}
		
		private Flag(String flag, int expectedArgs, String description) {
			this.flag = flag;
			this.expectedArgs = expectedArgs;
			this.description = description;
		}
	}

}
