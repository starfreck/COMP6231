package Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class NorthAmericanServerImpl extends UnicastRemoteObject implements GameServer {

	// Define Data structure here
	
	// Total User Count in this the server
	static int accountCount = 0;
	// Server Name
	static final String serverName  = "NorthAmerica";
	
	// Contains All Players information
	static HashMap<String,ArrayList<HashMap<String, String>>> players = new HashMap<String,ArrayList<HashMap<String, String>>>();    
   

    private static final long serialVersionUID = 1L;

	protected NorthAmericanServerImpl() throws RemoteException {
		super();
	}

	@Override
	public String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password,
			String IPAddress) throws RemoteException {

		// Check if user already exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());
		
		if(playerList != null)
		{
			// Find in list
			for (  HashMap<String, String> player : playerList) {
				
				if(player.get("username").equals(Username)) {
					return "A player already exixts with given username";
				}
	            
	        }
		}
		
		// Create User Account
		
		// Create Log folder for new user	
		
		// Adding User info in HashMap
		HashMap<String, String> player = new HashMap<String, String>();
		
		player.put("username",Username);
		player.put("password",Password);
		player.put("firstname",FirstName); 
		player.put("lastname",LastName); 
		player.put("age",String.valueOf(Age));
		player.put("ipaddress",IPAddress);
		player.put("status","offline");
	    
		// Adding Player into Player's List
		if(playerList != null)
		{
			playerList.add(player);
		}
		else
		{
			ArrayList<HashMap<String, String>> newPlayerList = new ArrayList<HashMap<String, String>>();
			newPlayerList.add(player);
			players.put(Username.substring(0, 1).toUpperCase(),newPlayerList);
		}

		// Update player count
		accountCount = accountCount +1;
		return "New player account created successfully";
	}

	@Override
	public String playerSignIn(String Username, String Password, String IPAddress) throws RemoteException {
		
		String message = null;
		
		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());
		
		if(playerList != null)
		{
			// Find in list
			for (HashMap<String, String> player : playerList) {
				
				// Account exists
				if(player.get("username").equals(Username)) {
					
					// Account is valid and signed
					if(player.get("password").equals(Password) && player.get("status").equals("offline")) {
						// Update Account status
						player.replace("status","online");
						message = "Account signed in successfully...";
						
					}else if (player.get("password").equals(Password) && player.get("status").equals("online")) {
						// Already signed in
						message = "Account already signed in...";
					}else {
						// you entered wrong password
						message = "Wrong password...";
					}
				}
				else {
					message = "A player doesn't exixts with given username";
				}
	        }
			
		}else {
			message = "A player doesn't exixts with given username";
		}
		
		return message;
		
	}

	@Override
	public String playerSignOut(String Username, String IPAddress) throws RemoteException {
		
		String message = null;
		
		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());
		
		if(playerList != null)
		{
			// Find in list
			for (HashMap<String, String> player : playerList) {
				
				// Account exists
				if(player.get("username").equals(Username)) {
					
					// Account is valid and signed
					if(player.get("status").equals("online")) {
						// Update Account status
						player.replace("status","offline");
						message = "Account signed out successfully...";
						
					}else {
						// Not signed in
						message = "Account not signed in...";
					}
				}
				else {
					message = "A player doesn't exixts with given username";
				}
	        }
			
		}else {
			message = "A player doesn't exixts with given username";
		}
		
		return message;
	}

	@Override
	public String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) throws RemoteException {
		
		String response = null;
		
		// Check The Admin UserName and Password
        if("Admin".equals(AdminUsername)  && "Admin".equals(AdminPassword)){
        	String NA = getOwnStatus();
        	response = NA;
        }
        else {
        	response = "Wrong username or password...";
        }
		
		
		// UDP code will be here
		
		
		//NA: 6 online, 1 offline, EU: 7 online, 1 offline, AS: 8 online, 1 offline.
		
		return response;
	}
	
	private String getOwnStatus() {
		
		int online = 0, offline = 0;
		
		for (Entry<String, ArrayList<HashMap<String, String>>> playerList : players.entrySet()) {
			
			// Find in list
			for (HashMap<String, String> player : playerList.getValue()) {
				
				if(player.get("status").equals("online")){
					online++;
				} else {
					offline++;
				}
	        }
	    }
		
		return "NA: "+online+" online, "+offline+" offline";
	}
}
