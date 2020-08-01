package Servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import Utilities.FileLogger;
import Utilities.Ports;

public class AsianServerImpl {

	// Flag variable
	boolean Flag = true;
	// Socket variable
	DatagramSocket socket;
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
	int AS_PORT;
	int EU_PORT;
	int NA_PORT;
	// Contains All Players information
	static ConcurrentHashMap<String, ArrayList<HashMap<String, String>>> players = new ConcurrentHashMap<String, ArrayList<HashMap<String, String>>>();
	
	public AsianServerImpl(int AS_PORT, int EU_PORT, int NA_PORT) throws InterruptedException {
		
		// Init Ports
		this.AS_PORT = AS_PORT;
		this.EU_PORT = EU_PORT;
		this.NA_PORT = NA_PORT;
		
		// Initialize Server Logger
		this.logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");
		this.addUsers();
		
		// Starting UDP Server
		Thread thread = new Thread(new Runnable() {
			public void run() {
				UDPServer();
			}
		});
		thread.start();
		
		System.out.println(serverName + " ready and waiting ...");

	}

	private void addUsers() {

		String[] firstname = { "Bruce", "Charles", "Ada", "Varun", "Kevin" };
		String[] lastname = { "Nguyen", "Lee", "Kim", "Patel", "Tran" };
		String[] usernames = { "Bruce123", "Charles123", "Adak123", "Varun123", "Kevin123" };
		String[] password = { "Bruce123", "Charles123", "Adak123", "Varun123", "Kevin123" };
		String[] ipaddress = { "182.34.2.1", "182.34.2.2", "182.34.2.3", "182.34.2.4", "182.34.2.5" };
		String[] age = { "19", "15", "18", "20", "23" };

		for (int i = 0; i <= usernames.length - 1; i++) {

			this.createPlayerAccount(firstname[i], lastname[i], Integer.parseInt(age[i]), usernames[i], password[i],
					ipaddress[i]);
		}

	}

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

		this.logger.write(">>> createPlayerAccount >>> User information stored");
		this.userLogger.write(">>> createPlayerAccount >>> User information stored");

		return "New player account created successfully";
	}

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
							this.logger.write(">>> transferAccount >>> New IPAdddress is Asian Server IP >>> Account is already in Asian Server");
							return "Your Account is already in Asian Server";
						}
						// European IP
						else if (NewIPAddress.split("\\.")[0].equals("93")) {
							
							this.logger.write(">>> transferAccount >>> New IPAdddress is European Server IP");
							
							String Data = getPlayerAccountInfo(Username);
							String status = this.UDPServerTunnel("EU", "transferAccount", Data + NewIPAddress);

							if ("true".equals(status)) {
								// Account is transfer
								// Delete Account
								if (this.deleteAccount(Username)) {
									this.logger.write(">>> transferAccount >>> Account is succesfully transfered");
									return "Account is succesfully transfered";
								} else {

									// Delete Account from remote server
									status = UDPServerTunnel("EU", "deleteTransferedAccount", Data + NewIPAddress);
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
						// North American IP
						else if (NewIPAddress.split("\\.")[0].equals("132")) {
							
							this.logger.write(">>> transferAccount >>> New IPAdddress is North American Server IP");
							
							String Data = getPlayerAccountInfo(Username);
							String status = this.UDPServerTunnel("NA", "transferAccount", Data + NewIPAddress);

							if ("true".equals(status)) {
								// Account is transfer
								// Delete Account
								if (this.deleteAccount(Username)) {
									this.logger.write(">>> transferAccount >>> Account is succesfully transfered");
									return "Account is succesfully transfered";
								} else {
									// Delete Account from remote server
									status = UDPServerTunnel("NA", "deleteTransferedAccount", Data + NewIPAddress);
									String msg = "Something went wrong during account transfer rollback started...";
									if ("true".equals(status)) {
										msg = msg + "\nRollback successfully finshed...";
									} else {
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

	public synchronized String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) {

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

			this.logger.write(">>> getPlayerStatus >>> getOwnStatus >>> " + AS);
			this.adminLogger.write(">>> getPlayerStatus >>> getOwnStatus >>> " + AS);

			response = AS;
		} else {

			this.logger.write(">>> getPlayerStatus >>> Wrong username or password...");
			this.adminLogger.write(">>> getPlayerStatus >>> Wrong username or password...");

			return "Wrong username or password...";
		}

		// Get status from Europe Server
		response = response + ", " + this.UDPServerTunnel("EU", "getPlayerStatus", "");

		// Get status from North American Server
		response = response + ", " + this.UDPServerTunnel("NA", "getPlayerStatus", "") + ".";

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
			byte[] recivedMessage = new byte[Ports.MAX_PACKET_SIZE];

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
	
	public void UDPServer() {

		// UDP server
		DatagramPacket requestPacket;
		DatagramPacket responsePacket;
		String reciveDataString, status = "";

		try {

			// Socket
			socket = new DatagramSocket(AS_PORT);

			while (Flag) {
				
				byte[] sendData = new byte[Ports.MAX_PACKET_SIZE];
				byte[] reciveData = new byte[Ports.MAX_PACKET_SIZE];

				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(),
						requestPacket.getLength());
				
				String methodName = reciveDataString.split(":", 2)[0];
				String data = reciveDataString.split(":", 2)[1];
				
//				System.out.println("methodName : "+methodName);
//				System.out.println("data : "+data);
				
				// Process requests coming from Leader
				status = processLeaderRequests(methodName, data);
				// Process requests coming from other servers
				if(status == "")
					status = processServersRequests(methodName, data);
				
				// Get Client's IP & Port
				InetAddress IPAddress = requestPacket.getAddress();
				int port = requestPacket.getPort();
				// Converting Message into Bytes
				sendData = status.getBytes();
				responsePacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(responsePacket);
				logger.write(">>> Sending response of UDP request");

			}
			
		} catch (SocketException e) {
			//System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}
	}
	
	public String processLeaderRequests(String methodName, String data) {
		
		String response = "";
		
		if(methodName.equals("createPlayerAccount"+"Leader")) {
			// Format Data
			// IPAddress|FirstName|LastName|Age|Username|Password
			
			String IPAddress = data.split("\\|")[0];
			String FirstName = data.split("\\|")[1];
			String LastName  = data.split("\\|")[2];
			int Age 		 = Integer.parseInt(data.split("\\|")[3]);
			String Username  = data.split("\\|")[4];
			String Password  = data.split("\\|")[5];
	
			response = createPlayerAccount(FirstName, LastName, Age, Username, Password, IPAddress);
		
		} else if(methodName.equals("playerSignIn"+"Leader")) {
			// Format Data
			// IPAddress|Username|Password
			
			String IPAddress = data.split("\\|")[0];
			String Username  = data.split("\\|")[1];
			String Password  = data.split("\\|")[2];
			
			response = playerSignIn(Username, Password, IPAddress);
			
		} else if(methodName.equals("playerSignOut"+"Leader")) {
			// Format Data
			// IPAddress|Username
			
			String IPAddress = data.split("\\|")[0];
			String Username  = data.split("\\|")[1];
			
			response = playerSignOut(Username, IPAddress);
			
		} else if(methodName.equals("transferAccount"+"Leader")) {
			// Format Data
			// OldIPAddress|Username|Password|NewIPAddress
			
			String OldIPAddress = data.split("\\|")[0];
			String Username  	= data.split("\\|")[1];
			String Password  	= data.split("\\|")[2];
			String NewIPAddress = data.split("\\|")[3];
	
			response = transferAccount(Username, Password, OldIPAddress, NewIPAddress);
			
		} else if(methodName.equals("getPlayerStatus"+"Leader")) {
			// Format Data
			// IPAddress|AdminUsername|AdminPassword
			
			String IPAddress 		= data.split("\\|")[0];
			String AdminUsername  	= data.split("\\|")[1];
			String AdminPassword  	= data.split("\\|")[2];
	
			response = getPlayerStatus(AdminUsername, AdminPassword, IPAddress);
			
		} else if(methodName.equals("suspendAccount"+"Leader")) {
			// Format Data
			// AdminIPAddress|AdminUsername|AdminPassword|UsernameToSuspend
			
			String AdminIPAddress 		= data.split("\\|")[0];
			String AdminUsername  		= data.split("\\|")[1];
			String AdminPassword  		= data.split("\\|")[2];
			String UsernameToSuspend  	= data.split("\\|")[3];
	
			response = suspendAccount(AdminUsername, AdminPassword, AdminIPAddress, UsernameToSuspend);
		}
		
		return response;
	}
	
	public String processServersRequests(String methodName, String data) {
		
		String response = "";
		
		// getPlayerStatus
		if(methodName.equals("getPlayerStatus")) {

			logger.write(">>> Recived UDP request");
			response = getOwnStatus();
			logger.write(">>> getOwnStatus >>> " + response);

		}
		// transferAccount
		else if (methodName.equals("transferAccount")) {

			logger.write(">>> Recived UDP request");

			String[] accountInfo = data.split("\\|");

			if (validateAccount(accountInfo[0])) {
				// Account with Given Name is already present
				logger.write(">>> transferAccount >>> Account with" + response + " username is already present");
				response = "false";
			
			} else{
				// Account Can transfer
				// FirstName, LastName, Age, Username, Password, IPAddress
				createPlayerAccount(accountInfo[2], accountInfo[3], Integer.parseInt(accountInfo[4]), accountInfo[0], accountInfo[1], accountInfo[5]);
				response = "true";
			}
			
			logger.write(">>> transferAccountStatus >>> " + response);

		}
		// deleteTransferedAccount
		else if (methodName.equals("deleteTransferedAccount")) {

			logger.write(">>> Recived UDP request");

			String[] accountInfo = data.split("\\|");
			
			String Username = accountInfo[0];
			
			if (validateAccount(accountInfo[0])) {
				// Account with Given Name is present
				logger.write(">>> deleteTransferedAccount >>> Account with" + response + " username is present");
				// Delete Account
				response = Boolean.toString(deleteAccount(Username));
			} else {
				// Account with Given Name is present
				logger.write(
						">>> deleteTransferedAccount >>> Account with" + response + " username is not present");
				response = "false";
			}
			
			logger.write(">>> deleteTransferedAccountStatus >>> " + response);					
		}
		
		return response;
	}
	
	public void kill() {
		Flag = false;
		socket.close();
		System.out.println(serverName+" is killed...");
	}

}