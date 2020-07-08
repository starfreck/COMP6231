package server;

import logger.FileLogger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;

@WebService(endpointInterface = "server.GameServer")
public class NorthAmericanServerImpl implements GameServer{

	// Total User Count in this the server
	static int accountCount = 0;
	// Server Name
	static final String serverName = "NorthAmericanServer";
	static final String serverShortName = "NA";
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
	static ConcurrentHashMap<String, ArrayList<HashMap<String, String>>> players = new ConcurrentHashMap<String, ArrayList<HashMap<String, String>>>();

	public NorthAmericanServerImpl() {
		// Initialize Server Logger
		this.logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");
		this.addUsers();

	}

	private void addUsers() {

		String[] firstname = { "Agnes", "Daly", "Lorena", "Bailee", "Scout" };
		String[] lastname = { "Siddall", "Morce", "Seabrook", "Upton", "Garfield" };
		String[] usernames = { "Siddall123", "Morce123", "Seabrook123", "Upton123", "Garfield123" };
		String[] password = { "Siddall123", "Morce123", "Seabrook123", "Upton123", "Garfield123" };
		String[] ipaddress = { "132.34.2.1", "132.34.2.2", "132.34.2.3", "132.34.2.4", "132.34.2.5" };
		String[] age = { "19", "15", "18", "20", "23" };

		for (int i = 0; i <= usernames.length - 1; i++) {

			this.createPlayerAccount(firstname[i], lastname[i], Integer.parseInt(age[i]), usernames[i], password[i],
					ipaddress[i]);
		}

	}

	@Override
	public synchronized String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password,
			String IPAddress) {

		this.logger.write(">>> createPlayerAccount");
		this.logger.write(">>> createPlayerAccount >>> username >>> " + Username);
		this.logger.write(">>> createPlayerAccount >>> firstname >>> " + FirstName);
		this.logger.write(">>> createPlayerAccount >>> lastname >>> " + LastName);
		this.logger.write(">>> createPlayerAccount >>> password >>> " + Password);
		this.logger.write(">>> createPlayerAccount >>> age >>> " + Age);
		this.logger.write(">>> createPlayerAccount >>> ipadddress >>> " + IPAddress);

		// Check if user already exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null && !playerList.isEmpty()) {
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
		if (playerList != null && !playerList.isEmpty()) {
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
	public synchronized String playerSignIn(String Username, String Password, String IPAddress) {

		String message = null;

		this.logger.write(">>> playerSignIn");
		this.logger.write(">>> playerSignIn >>> username >>> " + Username);
		this.logger.write(">>> playerSignIn >>> password >>> " + Password);
		this.logger.write(">>> playerSignIn >>> ipadddress >>> " + IPAddress);

		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null && !playerList.isEmpty()) {
			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Account is valid and signed
					if (player.get("password").equals(Password) && player.get("status").equals("offline")) {
						// Update Account status
						player.replace("status", "online");
						this.logger.write(">>> playerSignIn >>> " + Username + " signed in successfully...");
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignIn >>> " + Username + " signed in successfully...");
						message = "Account signed in successfully...";

					} else if (player.get("password").equals(Password) && player.get("status").equals("online")) {
						// Already signed in
						this.logger.write(">>> playerSignIn >>> " + Username + " is already signed in...");
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignIn >>> " + Username + " is already signed in...");
						message = "Account already signed in...";
					} else {
						// you entered wrong password
						this.logger.write(">>> playerSignIn >>> " + Username + " entered wrong password...");
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignIn >>> " + Username + " entered wrong password...");
						message = "Wrong password...";
					}

					return message;

				} else {
					this.logger.write(">>> playerSignIn >>> A player doesn't exixts with " + Username + " username");
					message = "A player doesn't exixts with given username";
				}
			}

		} else {
			this.logger.write(">>> playerSignIn >>> A player doesn't exixts with " + Username + " username");
			message = "A player doesn't exixts with given username";
		}

		this.logger.write(">>> playerSignIn >>> Final message >>>" + message);
		return message;

	}

	@Override
	public synchronized String playerSignOut(String Username, String IPAddress) {
		String message = null;

		this.logger.write(">>> playerSignOut");
		this.logger.write(">>> playerSignOut >>> username >>> " + Username);
		this.logger.write(">>> playerSignOut >>> ipadddress >>> " + IPAddress);

		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null && !playerList.isEmpty()) {
			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Account is valid and signed
					if (player.get("status").equals("online")) {

						// Update Account status
						player.replace("status", "offline");

						this.logger.write(">>> playerSignOut >>> " + Username + " signed out successfully...");

						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignOut >>> " + Username + " signed out successfully...");

						message = "Account signed out successfully...";

					} else { // Not signed in

						this.logger.write(">>> playerSignOut >>> " + Username + " is not signed in...");

						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> playerSignOut >>> " + Username + " is not signed in...");

						message = "Account not signed in...";
					}

					return message;

				} else {
					this.logger.write(">>> playerSignOut >>> A player doesn't exixts with " + Username + " username");
					message = "A player doesn't exixts with given username";
				}
			}

		} else {
			this.logger.write(">>> playerSignOut >>> A player doesn't exixts with " + Username + " username");
			message = "A player doesn't exixts with given username";
		}

		return message;
	}

	@Override
	public synchronized String transferAccount(String Username, String Password, String OldIPAddress, String NewIPAddress) {
		String message = null;

		this.logger.write(">>> transferAccount");
		this.logger.write(">>> transferAccount >>> username >>> " + Username);
		this.logger.write(">>> transferAccount >>> password >>> " + Password);
		this.logger.write(">>> transferAccount >>> ipadddress >>> " + OldIPAddress);
		this.logger.write(">>> transferAccount >>> NewIPAdddress >>> " + NewIPAddress);

		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null && !playerList.isEmpty()) {
			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Account is valid and signed
					if (player.get("password").equals(Password)) {

						// Asian IP
						if (NewIPAddress.split("\\.")[0].equals("182")) {

							this.logger.write(">>> transferAccount >>> New IP is for an Asian Server");
							
							String Data = getPlayerAccountInfo(Username);
							String status = this.UDPServerTunnel("AS", "transferAccount", Data + NewIPAddress);

							if ("true".equals(status)) {
								// Account is transfer
								// Delete Account
								if (this.deleteAccount(Username)) {
									this.logger.write(">>> transferAccount >>> Account is succesfully transfered");
									return "Account is succesfully transfered";
								} else {

									// Delete Account from remote server
									status = UDPServerTunnel("AS", "deleteTransferedAccount", Data);
									String msg = "Something went wrong during account transfer rollback started...";

									if ("true".equals(status)) {
										msg = msg + "\nRollback successfully finshed...";
									} else if ("false".equals(status)) {
										msg = msg + "\nRollback failed...";
									}
									this.logger.write(">>> transferAccount >>> "+msg);
									return msg;
								}
							} else if ("false".equals(status)) {
								// Account with Given Name is already present on Remote server
								this.logger.write(">>> transferAccount >>> Account with Given Name is already present on remote server");
								return "Account with Given Name is already present on remote server";
							}
						}
						// European IP
						else if (NewIPAddress.split("\\.")[0].equals("93")) {

							this.logger.write(">>> transferAccount >>>  New IP is for European Server");
							
							String Data = getPlayerAccountInfo(Username);
							String status = this.UDPServerTunnel("EU", "transferAccount", Data + NewIPAddress);

							if ("true".equals(status)) {
								// Account is transfer
								// Delete Account
								if (this.deleteAccount(Username)) {
									this.logger.write(">>> transferAccount >>>  Account is succesfully transfered");
									return "Account is succesfully transfered";
								} else {
									// Delete Account from remote server
									status = UDPServerTunnel("EU", "deleteTransferedAccount", Data);
									String msg = "Something went wrong during account transfer rollback started...";
									if ("true".equals(status)) {
										msg = msg + "\nRollback successfully finshed...";
									} else {
										msg = msg + "\nRollback failed...";
									}
									this.logger.write(">>> transferAccount >>>  "+msg);
									return msg;
								}
							} else if ("false".equals(status)) {
								// Account with Given Name is already present on Remote server
								this.logger.write(">>> transferAccount >>>  Account with Given Name is already present on remote server");
								return "Account with Given Name is already present on remote server";
							}
						}
						// North American IP
						else if (NewIPAddress.split("\\.")[0].equals("132")) {
							this.logger.write(">>> transferAccount >>>  Your Account is already in North American Server");
							return "Your Account is already in North American Server";
						}

						this.logger.write(">>> transferAccount >>>  New IP Address is invalid");
						return "New IP Address is invalid";

					} else {
						// you entered wrong password
						this.logger.write(">>> transferAccount >>> " + Username + " entered wrong password...");
						// Init User logs
						this.userLogger = initUserLogger(Username);
						this.userLogger.write(">>> transferAccount >>> " + Username + " entered wrong password...");
						message = "Wrong password...";
					}

					return message;

				} else {
					this.logger.write(">>> transferAccount >>> A player doesn't exixts with " + Username + " username");
					message = "A player doesn't exixts with given username";
				}
			}

		} else {
			this.logger.write(">>> transferAccount >>> A player doesn't exixts with " + Username + " username");
			message = "A player doesn't exixts with given username";
		}

		return message;

	}

	@Override
	public synchronized String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) {

		String NA = "";
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

			NA = getOwnStatus();

			this.logger.write(">>> getPlayerStatus >>> getOwnStatus >>> " + NA);
			this.adminLogger.write(">>> getPlayerStatus >>> getOwnStatus >>> " + NA);

			response = NA;
		} else {

			this.logger.write(">>> getPlayerStatus >>> Wrong username or password...");
			this.adminLogger.write(">>> getPlayerStatus >>> Wrong username or password...");

			return "Wrong username or password...";
		}

		// Get status from Europe Server
		response = response + ", " + this.UDPServerTunnel("EU", "getPlayerStatus", "");

		// Get status from Asian Server
		response = response + ", " + this.UDPServerTunnel("AS", "getPlayerStatus", "") + ".";

		// NA: 6 online, 1 offline, EU: 7 online, 1 offline, AS: 8 online, 1 offline.
		this.logger.write(">>> getPlayerStatus >>> Sending response to Admin >>> " + response);
		this.adminLogger.write(">>> getPlayerStatus >>> Sending response to Admin >>> " + response);

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

	@Override
	public synchronized String suspendAccount(String AdminUsername, String AdminPassword, String AdminIPAddress,
			String UsernameToSuspend) {

		// Init Admin logs
		this.adminLogger = initAdminLogger(AdminUsername);

		this.logger.write(">>> suspendAccount");
		this.logger.write(">>> suspendAccount >>> Admin Username >>> " + AdminUsername);
		this.logger.write(">>> suspendAccount >>> Admin Password >>> " + AdminPassword);
		this.logger.write(">>> suspendAccount >>> Admin IPAddress >>> " + AdminIPAddress);
		this.logger.write(">>> suspendAccount >>> Username To Suspend >>> " + UsernameToSuspend);

		this.adminLogger.write(">>> suspendAccount");
		this.adminLogger.write(">>> suspendAccount >>> Admin Username >>> " + AdminUsername);
		this.adminLogger.write(">>> suspendAccount >>> Admin Password >>> " + AdminPassword);
		this.adminLogger.write(">>> suspendAccount >>> Admin IPAddress >>> " + AdminIPAddress);
		this.adminLogger.write(">>> suspendAccount >>> Username To Suspend >>> " + UsernameToSuspend);

		if ("Admin".equals(AdminUsername) && "Admin".equals(AdminPassword)) {

			if (this.deleteAccount(UsernameToSuspend)) {

				this.logger.write(">>> suspendAccount >>> A player account with \"" + UsernameToSuspend
						+ "\" username is suspended");
				this.adminLogger.write(">>> suspendAccount >>> A player account with \"" + UsernameToSuspend
						+ "\" username is suspended");

				return "A player account with \"" + UsernameToSuspend + "\" username is suspended";

			} else {

				this.logger.write(
						">>> suspendAccount >>> A player doesn't exixts with " + UsernameToSuspend + " username");
				this.adminLogger.write(
						">>> suspendAccount >>> A player doesn't exixts with " + UsernameToSuspend + " username");

				return "A player doesn't exixts with given username";
			}

		} else {

			this.logger.write(">>> suspendAccount >>> Wrong username or password...");
			this.adminLogger.write(">>> suspendAccount >>> Wrong username or password...");

			return "Wrong username or password...";
		}
	}

	public synchronized boolean validateAccount(String Username) {

		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null && !playerList.isEmpty()) {

			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Account is valid
					return true;
				}
			}
		}

		return false;
	}

	public synchronized boolean deleteAccount(String Username) {

		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null && !playerList.isEmpty()) {

			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Account is valid and should suspended
					playerList.remove(player);
					// Update HashMap
					players.put(Username.substring(0, 1).toUpperCase(), playerList);

					return true;
				}
			}
		}

		return false;
	}

	private synchronized String getPlayerAccountInfo(String Username) {

		// Check if user exist
		ArrayList<HashMap<String, String>> playerList = players.get(Username.substring(0, 1).toUpperCase());

		if (playerList != null && !playerList.isEmpty()) {

			// Find in list
			for (HashMap<String, String> player : playerList) {

				// Account exists
				if (player.get("username").equals(Username)) {

					// Combine User info with '|'
					return player.get("username") + "|" + player.get("password") + "|" + player.get("firstname") + "|"
							+ player.get("lastname") + "|" + player.get("age").toString() + "|";
				}
			}
		}

		return "User with given name doesn't exist";
	}

	private String UDPServerTunnel(String serverName, String methodName, String Data) {

		String response = "";
		int UDP_PORT;

		if (serverName.equals("NA")) {
			UDP_PORT = NA_PORT;
		} else if (serverName.equals("EU")) {
			UDP_PORT = EU_PORT;
		} else if (serverName.equals("AS")) {
			UDP_PORT = AS_PORT;
		} else {
			return "Unknown server name";
		}

		// UDP client
		try {

			String methodAction = methodName + ":" + Data;
			DatagramSocket socket;
			DatagramPacket requestData;
			DatagramPacket responseData;
			InetAddress host = InetAddress.getLocalHost();

			byte[] sendMessage = methodAction.getBytes();
			byte[] recivedMessage = new byte[MAX_PACKET_SIZE];

			// Get status from Given Server
			socket = new DatagramSocket();

			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, UDP_PORT);
			socket.send(requestData);

			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);

			// Retrieving Data
			response = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());

			socket.close();

		} catch (Exception e) {
			System.err.println(e);
		}

		return response;

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
