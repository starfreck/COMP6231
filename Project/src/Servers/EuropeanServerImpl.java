package Servers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import Utilities.Database;
import Utilities.FileLogger;
import Utilities.Constants;




public class EuropeanServerImpl {
	
	// Flag var
	boolean Flag = true;
	// Socket Var
	DatagramSocket socket;
	// Server Name
	static final String serverName = "EuropeanServer";
	static final String serverShortName = "EU";
	// Replica Name
	String replicaName;
	// Loggers
	FileLogger logger;
	FileLogger userLogger;
	FileLogger adminLogger;
	// Logger Path
	String loggerPath = "./logs/";
	// UDP Server Ports
	int AS_PORT;
	int EU_PORT;
	int NA_PORT;
	// Contains All Players information
	ConcurrentHashMap<String, ArrayList<HashMap<String, String>>> players = new ConcurrentHashMap<String, ArrayList<HashMap<String, String>>>();

	public EuropeanServerImpl(String replicaName, int AS_PORT, int EU_PORT, int NA_PORT) {
				
		//Init replicaName
		this.replicaName = replicaName;
				
		// Init Ports
		this.AS_PORT = AS_PORT;
		this.EU_PORT = EU_PORT;
		this.NA_PORT = NA_PORT;
		
		// Initialize Server Logger
		this.logger = new FileLogger(loggerPath+replicaName+"/ServerLogs/"+ serverName + "/", serverName + ".log");
		
		// Check if the Database exists
		if(new File(loggerPath+replicaName+"/ServerLogs/"+ serverName + "/"+"database.ser").exists()) {
			players = loadDB();
		} else {
			this.addUsers();
		}
				
		// Starting UDP Server
		Thread thread = new Thread(new Runnable() {
			public void run() {
				UDPServer();
			}
		});
		
		thread.start();
		
		if(Constants.DEBUG) System.out.println(serverName + " ready and waiting ...");
		
	}

	private void addUsers() {

		String[] firstname = { "Fabienne", "Darcie", "Philipa", "Davinia", "Gyles" };
		String[] lastname  = { "Tapani", "Sammie", "Alphonzo", "Amsel", "Key" };
		String[] usernames = { "Fabienne123", "Darcie123", "Philipa123", "Davinia123", "Gyles123" };
		String[] password  = { "Fabienne123", "Darcie123", "Philipa123", "Davinia123", "Gyles123" };
		String[] ipaddress = { "93.5.4.1", "93.5.4.2", "93.5.4.3", "93.5.4.4", "93.5.4.5" };
		String[] age 	   = { "19", "15", "18", "20", "23" };

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

		String EU = "";
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

			EU = getOwnStatus();

			this.logger.write(">>> getPlayerStatus >>> getOwnStatus >>> " + EU);
			this.adminLogger.write(">>> getPlayerStatus >>> getOwnStatus >>> " + EU);

			response = EU;
		} else {

			this.logger.write(">>> getPlayerStatus >>> Wrong username or password...");
			this.adminLogger.write(">>> getPlayerStatus >>> Wrong username or password...");

			return "Wrong username or password...";
		}

		// Get status from Asian Server
		response = response + ", " + this.UDPServerTunnel("AS", "getPlayerStatus", "");

		// Get status from North American Server
		response = response + ", " + this.UDPServerTunnel("NA", "getPlayerStatus", "") + ".";

		// EU: 6 online, 1 offline, AS: 7 online, 1 offline, NA: 8 online, 1 offline.
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
			byte[] recivedMessage = new byte[Constants.MAX_PACKET_SIZE];

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
		return new FileLogger(loggerPath+replicaName+"/ServerLogs/"+serverName+"/UserLogs/"+username+"/",username+".log");
	}

	private FileLogger initAdminLogger(String username) {

		// Initialize Admin Logger
		return new FileLogger(loggerPath+replicaName+"/ServerLogs/"+serverName+"/AdminLogs/"+username+"/",username+".log");
	}
	
	public void UDPServer() {

		// UDP server
		DatagramPacket requestPacket;
		DatagramPacket responsePacket;
		String reciveDataString, status = "";

		try {

			// Socket
			socket = new DatagramSocket(EU_PORT);

			while (Flag) {
				
				byte[] sendData = new byte[Constants.MAX_PACKET_SIZE];
				byte[] reciveData = new byte[Constants.MAX_PACKET_SIZE];

				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				this.logger.write(">>> UDPServer >>> Reciving request");
				
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(),requestPacket.getLength());
				
				String methodName = reciveDataString.split(":", 2)[0];
				String data 	  = reciveDataString.split(":", 2)[1];
				
//				System.out.println("methodName : "+methodName);
//				System.out.println("data : "+data);
				
				// HeartBeat Checker
				if(methodName.equals("UDPHeartBeat"))
				{
					// data has "UDPHeartBeat:just a message to check server pulse"
					this.logger.write(">>> UDPServer >>> Reciving request >>> UDP Heart Beat");
					status = "UDPHeartBeat:i am alive";
				} 
				// Other Requests
				else
				{
					this.logger.write(">>> UDPServer >>> Request is coming from leader or other servers");	
					// Process requests coming from Leader
					status = processLeaderRequests(methodName, data);
					// Process requests coming from other servers
					if(status 	== "")status = processServersRequests(methodName, data);
				}
				
				
				
				// Get Client's IP & Port
				InetAddress IPAddress = requestPacket.getAddress();
				int port = requestPacket.getPort();
				// Converting Message into Bytes
				sendData = status.getBytes();
				responsePacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(responsePacket);
				this.logger.write(">>> UDPServer >>> Sending response of UDP request");

			}
			
		} catch (SocketException e) {
			//System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}
	}
	
	public String processLeaderRequests(String methodName, String data) {
		
		this.logger.write(">>> processLeaderRequests >>> Start processing UDP request");
		
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
		
		this.logger.write(">>> processLeaderRequests >>> Finish processing UDP request");
		
		return response;
	}
	
	public String processServersRequests(String methodName, String data) {
		
		this.logger.write(">>> processServersRequests >>> Start processing UDP request");
		
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
		
		this.logger.write(">>> processServersRequests >>> Finish processing UDP request");
		
		return response;
	}
	
	public void kill() {
		// Write DB to File
		storeDB();
		
		Flag = false;
		this.logger.write(">>> kill >>> Closing socket");
		socket.close();
		System.out.println(serverName+" is killed...");
		this.logger.write(">>> kill >>> "+serverName+" is killed...");
	}
	
	public boolean storeDB() {
		
		this.logger.write(">>> storeDB >>> Write DB to File");
		
		try
        {
			Database DB = new Database(players);
			FileOutputStream fileOut = new FileOutputStream(loggerPath+replicaName+"/ServerLogs/"+ serverName + "/"+"database.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
	        out.writeObject(DB);
	        out.close();
	        fileOut.close();
	        
        } catch (Exception e) {
			e.printStackTrace();
		}
		
		this.logger.write(">>> storeDB >>> DB is stored in File");
        return true;
	}
	
	public ConcurrentHashMap<String, ArrayList<HashMap<String, String>>> loadDB() {
		
		this.logger.write(">>> loadDB >>> Read DB from File");
		
		Database DB = null;
		
        try
        {
    		FileInputStream fileIn = new FileInputStream(loggerPath+replicaName+"/ServerLogs/"+ serverName + "/"+"database.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            DB = (Database) in.readObject();
            in.close();
			fileIn.close();
			
			this.logger.write(">>> loadDB >>> Read finished");
			// Delete DB File
	        new File(loggerPath+replicaName+"/ServerLogs/"+ serverName + "/"+"database.ser").delete();
	        
	        this.logger.write(">>> loadDB >>> Delete DB File");
		
        } catch (Exception e) {
			e.printStackTrace();
		}
        
        return DB.getDB();   
	}
}