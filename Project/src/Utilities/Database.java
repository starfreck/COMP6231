package Utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Database implements Serializable {

	private static final long serialVersionUID = 2608873464640833234L;
	
	// Contains All Players information
	ConcurrentHashMap<String, ArrayList<HashMap<String, String>>> players = new ConcurrentHashMap<String, ArrayList<HashMap<String, String>>>();
	
	public Database(ConcurrentHashMap<String, ArrayList<HashMap<String, String>>> players) {
	  this.players = players;
	}
	
	public ConcurrentHashMap<String, ArrayList<HashMap<String, String>>> getDB(){
		return players;
	}	
}
