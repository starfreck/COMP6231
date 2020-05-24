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

public class EuropeanServerImpl extends UnicastRemoteObject implements GameServer {

	private static final long serialVersionUID = 1L;

	// Total User Count in this the server
	static int accountCount = 0;
	// Server Name
	static final String serverName = "EuropeanServer";
	static final String serverShortName = "EU";
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

	protected EuropeanServerImpl() throws RemoteException {
		super();
		// Initialize Server Logger
		this.logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");

	}

	@Override
	public String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password,
			String IPAddress) throws RemoteException {

		// Init User logs
		this.userLogger = initUserLogger(Username);

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
	public String playerSignIn(String Username, String Password, String IPAddress) throws RemoteException {

		String message = null;

		// Init User logs
		this.userLogger = initUserLogger(Username);

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
						message = "Account signed in successfully...";

					} else if (player.get("password").equals(Password) && player.get("status").equals("online")) {
						// Already signed in
						message = "Account already signed in...";
					} else {
						// you entered wrong password
						message = "Wrong password...";
					}
				} else {
					message = "A player doesn't exixts with given username";
				}
			}

		} else {
			message = "A player doesn't exixts with given username";
		}

		return message;

	}

	@Override
	public String playerSignOut(String Username, String IPAddress) throws RemoteException {

		String message = null;

		// Init User logs
		this.userLogger = initUserLogger(Username);

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
						message = "Account signed out successfully...";

					} else {
						// Not signed in
						message = "Account not signed in...";
					}
				} else {
					message = "A player doesn't exixts with given username";
				}
			}

		} else {
			message = "A player doesn't exixts with given username";
		}

		return message;
	}

	@Override
	public String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) throws RemoteException {

		String response = null;

		// Init User logs
		this.adminLogger = initAdminLogger(AdminUsername);

		// Check The Admin UserName and Password
		if ("Admin".equals(AdminUsername) && "Admin".equals(AdminPassword)) {
			String EU = getOwnStatus();
			response = EU;
		} else {
			response = "Wrong username or password...";
		}

		// UDP client side code will be here
		try {

			String methodAction = "getPlayerStatus";
			DatagramSocket socket;
			DatagramPacket requestData;
			DatagramPacket responseData;
			InetAddress host = InetAddress.getLocalHost();

			byte[] sendMessage = methodAction.getBytes();
			byte[] recivedMessage = new byte[MAX_PACKET_SIZE];

			// Get status from Asian Server
			socket = new DatagramSocket();
			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, AS_PORT);
			socket.send(requestData);
			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);
			// Retrieving Data
			String AS = new String(responseData.getData());
			// Appending to response
			response = response + ',' + AS;
			socket.close();

			// Get status from North American Server
			socket = new DatagramSocket();
			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, NA_PORT);
			socket.send(requestData);
			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);
			// Retrieving Data
			String NA = new String(responseData.getData());
			// Appending to response
			response = response + ',' + NA + '.';
			socket.close();

		} catch (Exception e) {
			System.err.println(e);
		}
		System.out.println(response);
		// NA: 6 online, 1 offline, EU: 7 online, 1 offline, AS: 8 online, 1 offline.
		return response;
	}

	public String getOwnStatus() {

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

		return new FileLogger(loggerPath + serverName + "/AdminLogs/" + username + "/", username + ".log");
	}
}