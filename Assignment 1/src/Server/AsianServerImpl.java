package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import Logger.FileLogger;

public class AsianServerImpl extends UnicastRemoteObject implements GameServer {

	private static final long serialVersionUID = 1L;

	// Total User Count in this the server
	static int accountCount = 0;
	// Server Name
	static final String serverName = "AsianServer";
	static final String serverShortName = "AS";
	// Loggers
	FileLogger logger;
	FileLogger userLogger;
	FileLogger adminLogger;
	// Logger Path
	static final String loggerPath = "./logs/ServerLogs/";
	// UDP Server Ports
	static final int NA_PORT = 5001;
	static final int EU_PORT = 5002;
	static final int AS_PORT = 5003;
	// Max Packet Size
	static final int MAX_PACKET_SIZE = 1024;
	// Contains All Players information
	static HashMap<String, ArrayList<HashMap<String, String>>> players = new HashMap<String, ArrayList<HashMap<String, String>>>();

	protected AsianServerImpl() throws RemoteException {
		super();
		// Initialize Server Logger
		this.logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");
		this.addUsers();

	}

	private void addUsers() {

		String[] firstname  = { "Bruce", "Charles", "Ada", "Varun", "Kevin" };
		String[] lastname   = { "Nguyen", "Lee", "Kim", "Patel", "Tran" };
		String[] usernames  = { "bruce15", "lee45", "akim", "varun97", "kt0012" };
		String[] password   = { "Bruce123", "Charles123", "Ada123", "Varun123", "Kevin123" };
		String[] ipaddress  = { "182.34.2.1", "182.34.2.2", "182.34.2.3", "182.34.2.4", "182.34.2.5" };
		String[] age 		= { "19", "15", "18", "20", "23" };

		for (int i = 0; i <= usernames.length - 1; i++) {
			
			// Init User logs
			this.userLogger = initUserLogger(usernames[i]);

			this.logger.write(">>> createPlayerAccount");
			this.logger.write(">>> createPlayerAccount >>> username >>> " + usernames[i]);
			this.logger.write(">>> createPlayerAccount >>> firstname >>> " + firstname[i]);
			this.logger.write(">>> createPlayerAccount >>> lastname >>> " + lastname[i]);
			this.logger.write(">>> createPlayerAccount >>> password >>> " + password[i]);
			this.logger.write(">>> createPlayerAccount >>> age >>> " + age[i]);
			this.logger.write(">>> createPlayerAccount >>> ipadddress >>> " + ipaddress[i]);

			// Check if user already exist
			ArrayList<HashMap<String, String>> playerList = players.get(usernames[i].substring(0, 1).toUpperCase());

			if (playerList != null) {
				// Find in list
				for (HashMap<String, String> player : playerList) {

					if (player.get("username").equals(usernames[i])) {
						this.logger.write(">>> createPlayerAccount >>> A player already exixts with given username");
					}

				}
			}

			// Create User Account

			// Create Log folder for new user
			this.userLogger.write(">>> createPlayerAccount");
			this.userLogger.write(">>> createPlayerAccount >>> username >>> " + usernames[i]);
			this.userLogger.write(">>> createPlayerAccount >>> firstname >>> " + firstname[i]);
			this.userLogger.write(">>> createPlayerAccount >>> lastname >>> " + lastname[i]);
			this.userLogger.write(">>> createPlayerAccount >>> password >>> " + password[i]);
			this.userLogger.write(">>> createPlayerAccount >>> age >>> " + age[i]);
			this.userLogger.write(">>> createPlayerAccount >>> ipadddress >>> " + ipaddress[i]);

			// Adding User info in HashMap
			HashMap<String, String> player = new HashMap<String, String>();

			player.put("username", usernames[i]);
			player.put("password", password[i]);
			player.put("firstname", firstname[i]);
			player.put("lastname", lastname[i]);
			player.put("age", age[i]);
			player.put("ipaddress", ipaddress[i]);
			player.put("status", "offline");

			// Adding Player into Player's List
			if (playerList != null) {
				playerList.add(player);
			} else {
				ArrayList<HashMap<String, String>> newPlayerList = new ArrayList<HashMap<String, String>>();
				newPlayerList.add(player);
				players.put(usernames[i].substring(0, 1).toUpperCase(), newPlayerList);
			}

			// Update player count
			accountCount = accountCount + 1;

			this.logger.write(">>> createPlayerAccount >>> User information stored");
			this.userLogger.write(">>> createPlayerAccount >>> User information stored");

		}

	}

	@Override
	public synchronized String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password,
			String IPAddress) throws RemoteException {

		this.logger.write(">>> createPlayerAccount");
		this.logger.write(">>> createPlayerAccount >>> username >>> " + Username);
		this.logger.write(">>> createPlayerAccount >>> firstname >>> " + FirstName);
		this.logger.write(">>> createPlayerAccount >>> lastname >>> " + LastName);
		this.logger.write(">>> createPlayerAccount >>> password >>> " + Password);
		this.logger.write(">>> createPlayerAccount >>> age >>> " + Age);
		this.logger.write(">>> createPlayerAccount >>> ipadddress >>> " + IPAddress);

		// Check if user already exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null) {
			// Find in list
			for (HashMap<String, String> player : playerList) {

				if (player.get("username").equals(Username)) {
					this.logger.write(">>> createPlayerAccount >>> A player already exixts with given username");
					return "A player already exixts with given username";
				}

			}
		}

		// Create User Account

		// Init User logs
		this.userLogger = initUserLogger(Username);
		// Create Log folder for new user
		this.userLogger.write(">>> createPlayerAccount");
		this.userLogger.write(">>> createPlayerAccount >>> username >>> " + Username);
		this.userLogger.write(">>> createPlayerAccount >>> firstname >>> " + FirstName);
		this.userLogger.write(">>> createPlayerAccount >>> lastname >>> " + LastName);
		this.userLogger.write(">>> createPlayerAccount >>> password >>> " + Password);
		this.userLogger.write(">>> createPlayerAccount >>> age >>> " + Age);
		this.userLogger.write(">>> createPlayerAccount >>> ipadddress >>> " + IPAddress);

		// Adding User info in HashMap
		HashMap<String, String> player = new HashMap<String, String>();

		player.put("username", Username);
		player.put("password", Password);
		player.put("firstname", FirstName);
		player.put("lastname", LastName);
		player.put("age", String.valueOf(Age));
		player.put("ipaddress", IPAddress);
		player.put("status", "offline");

		// Adding Player into Player's List
		if (playerList != null) {
			playerList.add(player);
		} else {
			ArrayList<HashMap<String, String>> newPlayerList = new ArrayList<HashMap<String, String>>();
			newPlayerList.add(player);
			players.put(Username.substring(0, 1).toUpperCase(), newPlayerList);
		}

		// Update player count
		accountCount = accountCount + 1;

		this.logger.write(">>> createPlayerAccount >>> User information stored");
		this.userLogger.write(">>> createPlayerAccount >>> User information stored");

		return "New player account created successfully";
	}

	@Override
	public synchronized String playerSignIn(String Username, String Password, String IPAddress) throws RemoteException {

		String message = null;
		
		this.logger.write(">>> playerSignIn");
		this.logger.write(">>> playerSignIn >>> username >>> " + Username);
		this.logger.write(">>> playerSignIn >>> password >>> " + Password);
		this.logger.write(">>> playerSignIn >>> ipadddress >>> " + IPAddress);
		
		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null) {
			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Account is valid and signed
					if (player.get("password").equals(Password) && player.get("status").equals("offline")) {
						// Update Account status
						player.replace("status", "online");
						this.logger.write(">>> playerSignIn >>> "+Username+" signed in successfully...");
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignIn >>> "+Username+" signed in successfully...");
						message = "Account signed in successfully...";

					} else if (player.get("password").equals(Password) && player.get("status").equals("online")) {
						// Already signed in
						this.logger.write(">>> playerSignIn >>> "+Username+" is already signed in...");
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignIn >>> "+Username+" is already signed in...");
						message = "Account already signed in...";
					} else {
						// you entered wrong password
						this.logger.write(">>> playerSignIn >>> "+Username+" entered wrong password...");
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignIn >>> "+Username+" entered wrong password...");
						message = "Wrong password...";
					}
				} else {
					this.logger.write(">>> playerSignIn >>> A player doesn't exixts with "+Username+" username");
					message = "A player doesn't exixts with given username";
				}
			}

		} else {
			this.logger.write(">>> playerSignIn >>> A player doesn't exixts with "+Username+" username");
			message = "A player doesn't exixts with given username";
		}

		return message;

	}

	@Override
	public synchronized String playerSignOut(String Username, String IPAddress) throws RemoteException {

		String message = null;
	
		this.logger.write(">>> playerSignOut");
		this.logger.write(">>> playerSignOut >>> username >>> " + Username);
		this.logger.write(">>> playerSignOut >>> ipadddress >>> " + IPAddress);

		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null) {
			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Account is valid and signed
					if (player.get("status").equals("online")) {
						
						// Update Account status
						player.replace("status", "offline");
						
						this.logger.write(">>> playerSignOut >>> "+Username+" signed out successfully...");
						
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignOut >>> "+Username+" signed out successfully...");
						
						message = "Account signed out successfully...";

					} else { // Not signed in
						
						this.logger.write(">>> playerSignOut >>> "+Username+" is not signed in...");
						
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignOut >>> "+Username+" is not signed in...");
						
						message = "Account not signed in...";
					}
				} else {
					this.logger.write(">>> playerSignOut >>> A player doesn't exixts with "+Username+" username");
					message = "A player doesn't exixts with given username";
				}
			}

		} else {
			this.logger.write(">>> playerSignOut >>> A player doesn't exixts with "+Username+" username");
			message = "A player doesn't exixts with given username";
		}

		return message;
	}

	@Override
	public synchronized String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) throws RemoteException {

		String NA = "";
		String EU = "";
		String AS = "";
		String response = "";

		// Init User logs
		this.adminLogger = initAdminLogger(AdminUsername);
		
		this.logger.write(">>> getPlayerStatus");
		this.logger.write(">>> getPlayerStatus >>> username >>> " + AdminUsername);
		this.logger.write(">>> getPlayerStatus >>> password >>> " + AdminPassword);
		this.logger.write(">>> getPlayerStatus >>> ipadddress >>> " + IPAddress);
		
		this.adminLogger.write(">>> getPlayerStatus");
		this.adminLogger.write(">>> getPlayerStatus >>> username >>> " + AdminUsername);
		this.adminLogger.write(">>> getPlayerStatus >>> password >>> " + AdminPassword);
		this.adminLogger.write(">>> getPlayerStatus >>> ipadddress >>> " + IPAddress);


		// Check The Admin UserName and Password
		if ("Admin".equals(AdminUsername) && "Admin".equals(AdminPassword)) {
			
			this.logger.write(">>> getPlayerStatus >>> getOwnStatus");
			this.adminLogger.write(">>> getPlayerStatus >>> getOwnStatus");
			
			AS = getOwnStatus();
			
			this.logger.write(">>> getPlayerStatus >>> getOwnStatus >>> "+AS);
			this.adminLogger.write(">>> getPlayerStatus >>> getOwnStatus >>> "+AS);
			
			response = AS;
		} else {
			
			this.logger.write(">>> getPlayerStatus >>> Wrong username or password...");
			this.adminLogger.write(">>> getPlayerStatus >>> Wrong username or password...");
			
			return "Wrong username or password...";
		}

		// UDP clients
		try {

			String methodAction = "getPlayerStatus";
			DatagramSocket socket;
			DatagramPacket requestData;
			DatagramPacket responseData;
			InetAddress host = InetAddress.getLocalHost();

			byte[] sendMessage = methodAction.getBytes();
			byte[] recivedMessage = new byte[MAX_PACKET_SIZE];

			// Get status from Europe Server
			socket = new DatagramSocket();
			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, EU_PORT);
			socket.send(requestData);
			
			this.logger.write(">>> getPlayerStatus >>> Sending request to European Server");
			this.adminLogger.write(">>> getPlayerStatus >>> Sending request to European Server");
			
			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);
			
			this.logger.write(">>> getPlayerStatus >>> Reciving response from European Server");
			this.adminLogger.write(">>> getPlayerStatus >>> Reciving response from European Server");
			
			// Retrieving Data
			EU = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());
			
			this.logger.write(">>> getPlayerStatus >>> Response from European Server >>> "+EU);
			this.adminLogger.write(">>> getPlayerStatus >>> Response from European Server >>> "+EU);
			
			// Appending to response
			response = response + ", " + EU;
			socket.close();

			// Get status from North American Server
			socket = new DatagramSocket();
			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, NA_PORT);
			socket.send(requestData);
			
			this.logger.write(">>> getPlayerStatus >>> Sending request to North American Server");
			this.adminLogger.write(">>> getPlayerStatus >>> Sending request to North American Server");
			
			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);
			
			this.logger.write(">>> getPlayerStatus >>> Reciving response from North American Server");
			this.adminLogger.write(">>> getPlayerStatus >>> Reciving response from North American Server");
			
			// Retrieving Data
			NA = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());
			
			this.logger.write(">>> getPlayerStatus >>> Response from North American Server >>> "+NA);
			this.adminLogger.write(">>> getPlayerStatus >>> Response from North American Server >>> "+NA);
			
			// Appending to response
			response = response + ", " + NA + ".";
			socket.close();

		} catch (Exception e) {
			
			this.logger.write(">>> getPlayerStatus >>> Exception >>> "+e);
			this.adminLogger.write(">>> getPlayerStatus >>> Exception >>> "+e);
			System.err.println(e);
		}

		// NA: 6 online, 1 offline, EU: 7 online, 1 offline, AS: 8 online, 1 offline.
		this.logger.write(">>> getPlayerStatus >>> Sending response to Admin >>> "+response);
		this.adminLogger.write(">>> getPlayerStatus >>> Sending response to Admin >>> "+response);
		
		return response;
	}

	public synchronized String getOwnStatus() {

		int online = 0, offline = 0;

		for (Entry<String, ArrayList<HashMap<String, String>>> playerList : players.entrySet()) {

			// Find in list
			for (HashMap<String, String> player : playerList.getValue()) {

				if (player.get("status").equals("online")) {
					online++;
				} else {
					offline++;
				}
			}
		}

		return serverShortName + ": " + online + " online, " + offline + " offline";
	}

	private FileLogger initUserLogger(String username) {

		// Initialize User Logger
		return new FileLogger(loggerPath + serverName + "/UserLogs/" + username + "/", username + ".log");
	}

	private FileLogger initAdminLogger(String username) {
		
		// Initialize Admin Logger
		return new FileLogger(loggerPath + serverName + "/AdminLogs/" + username + "/", username + ".log");
	}
}