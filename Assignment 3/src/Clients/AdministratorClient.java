package Clients;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import java.util.HashMap;

import Logger.FileLogger;
import Services.GameServer;

public class AdministratorClient {

	static boolean status = true;
	static int PASSWORD_LENGTH = 6;
	static int USERNAME_MIN_LENGTH = 6;
	static int USERNAME_MAX_LENGTH = 15;
	
	// Web Service Ports
	static final String AS_WS_PORT = "8081";
	static final String EU_WS_PORT = "8082";
	static final String NA_WS_PORT = "8083";

	static String[] validIPs = { "132", "93", "182" };
	static Scanner input = new Scanner(System.in);
	static String[] AdminClientArgs;

	static HashMap<String, String> gameServers = new HashMap<String, String>();

	// Initialize Logger
	static FileLogger logger = new FileLogger("./logs/AdministratorClient/", "AdministratorClientLogs.log");

	public AdministratorClient(String[] args) throws IOException {

		// Initialize Servers List
		gameServers.put("132", "NorthAmerica");
		gameServers.put("93", "Europe");
		gameServers.put("182", "Asia");
	}

	// Return menu.
	public static void showMenu() {

		System.out.println("\n****Welcome Admin****\n");
		System.out.println("Please select an option (1-2)");
		System.out.println("1. Get Players Status");
		System.out.println("2. Suspend Account");
		System.out.println("3. Exit");
	}

	// Return basic menu.
	public void selectMenu(int choice, AdministratorClient admin) throws MalformedURLException
	{
		switch (choice) {
		case 1:
			admin.getStatus();
			break;
		case 2:
			admin.suspendPlayer();
			break;
		case 3:
			System.out.println("\nGood Bye.");
			AdministratorClient.status = false;
			System.exit(0);
			break;
		default:
			System.out.println("\nInvalid Choice.");
			break;
		}

	}

	private void getStatus() throws MalformedURLException {

		logger.write(">>> Get Status");
		String username = inputUsername();
		String password = inputPassword();
		String ipaddress = inputIPAddress();

		logger.write(">>> Get Status >>> username >>>  " + username);
		logger.write(">>> Get Status >>> password >>> " + password);
		logger.write(">>> Get Status >>> ipaddress >>> " + ipaddress);

		logger.write(">>> Get Status >>> Sending request to server");

		String response = getPlayerStatus(username, password, ipaddress);
		// Print Response
		System.out.println(response);
		logger.write(">>> Get Status >>> Response >>> " + response);

	}
	
	private void suspendPlayer() throws MalformedURLException {

		logger.write(">>> Suspend Account");
		
		String username 		 = inputUsername();
		String password 		 = inputPassword();
		String ipaddress 		 = inputIPAddress();
		String usernameToSuspend = inputPlayerUsername();
		
		logger.write(">>> Suspend Account >>> username >>>  " + username);
		logger.write(">>> Suspend Account >>> password >>> " + password);
		logger.write(">>> Suspend Account >>> ipaddress >>> " + ipaddress);
		logger.write(">>> Suspend Account >>> Username To Suspend >>> " + usernameToSuspend);

		logger.write(">>> Suspend Account >>> Sending request to server");

		String response = suspendAccount(username, password, ipaddress, usernameToSuspend);
		// Print Response
		System.out.println(response);
		logger.write(">>> Suspend Account >>> Response >>> " + response);

	}

	public static int inputChoice() {

		int choice = 0;
		boolean inputError = true;

		do {
			try {
				System.out.print("\nSelect >>> ");
				choice = Integer.parseInt(input.nextLine());
				inputError = false;
			} catch (Exception e) {
				logger.write(">>> inputChoice >>> Please input Integer only");
				System.err.println("\nPlease input Integer only");
			}
		} while (inputError);

		return choice;
	}

	public static void main(String[] args) {

		try {

			AdministratorClient admin = new AdministratorClient(args);

			while (AdministratorClient.status) {

				AdministratorClient.showMenu();
				int choice = inputChoice();
				admin.selectMenu(choice, admin);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) throws MalformedURLException {

		String status = "false";

		String serverName = gameServers.get(IPAddress.split("\\.")[0]);

		logger.write(">>> Get Player Status >>> Sending request to " + serverName);
		// find the remote object and cast it to an interface object
		GameServer server = getServerObj(serverName);

		if (server == null) {
			logger.write(">>> Get Player Status >>> Server not found");
			return status;
		}

		status = server.getPlayerStatus(AdminUsername, AdminPassword, IPAddress);

		return "\n" + status;
	}
	
	public String suspendAccount(String AdminUsername, String AdminPassword, String IPAddress, String UsernameToSuspend) throws MalformedURLException{
		
		String status = "false";

		String serverName = gameServers.get(IPAddress.split("\\.")[0]);

		logger.write(">>> Suspend Account >>> Sending request to " + serverName);
		
		GameServer server = getServerObj(serverName);

		if (server == null) {
			logger.write(">>>  Suspend Account >>> Server not found");
			return status;
		}

		status = server.suspendAccount(AdminUsername, AdminPassword, IPAddress, UsernameToSuspend);

		return "\n" + status;
	}

	private static GameServer getServerObj(String serverName) throws MalformedURLException {
		
		GameServer server  = null;
		String serverPort  = null;
		String serverTitle = null;
		String serviceName = null;
		
		if (serverName.equals("Asia")) {
			
			serverTitle = gameServers.get("182");
			serverPort  = AS_WS_PORT;
			serviceName = "AsianServerImplService";
			
		} else if (serverName.equals("Europe")) {
			
			serverTitle = gameServers.get("93");
			serverPort  = EU_WS_PORT;
			serviceName = "AsianServerImplService";
			
		} else if (serverName.equals("NorthAmerica")) {
			
			serverTitle = gameServers.get("132");
			serverPort  = NA_WS_PORT;
			serviceName = "AsianServerImplService";
		}
		
		
		URL url = new URL("http://localhost:"+serverPort+"/"+serverTitle+"?wsdl");
		QName qName = new QName("http://Services/", serviceName);
		Service service = Service.create(url, qName);
		server = service.getPort(GameServer.class);
		

		return server;
	}

	private String inputUsername() {
		System.out.print("\nEnter username: ");
		String username = input.nextLine();
		return username;
	}
	
	private String inputPlayerUsername() {
		String username;

		do {
			System.out.print("\nEnter player's username: ");
			username = input.nextLine();
		} while (isValidUsername(username));

		return username;
	}

	private String inputPassword() {

		System.out.print("\nEnter password: ");
		String password = input.nextLine();
		;
		return password;
	}

	private String inputIPAddress() {

		String ipaddress;

		do {
			System.out.print("\nEnter IP address: ");
			ipaddress = input.nextLine();

		} while (isValidIPAddress(ipaddress));

		return ipaddress;
	}

	private boolean isValidIPAddress(String ipaddress) {

		try {

			String ipv4Part = ipaddress.split("\\.")[0];

			if (ipaddress.split("\\.").length != 4) {
				System.err.print("\nInvalid IP address formate\n");
				logger.write(">>> Error >>> Invalid IP address formate");
				return true;
			}

			for (int i = 0; i < ipaddress.split("\\.").length; i++) {

				if (!isNumeric(ipaddress.split("\\.")[i])) {

					System.err.print("\nInvalid IP address\n");
					logger.write(">>> Error >>> Invalid IP address");
					return true;
				}
			}

			// Convert String Array to List
			List<String> list = Arrays.asList(validIPs);

			if (ipaddress.isEmpty()) {
				logger.write(">>> Error >>> IP address cannot be empty");
				System.err.print("\nIP address cannot be empty\n");
				return true;
			}

			if (!list.contains(ipv4Part)) {

				System.err.println("\n1. 132.xxx.xxx.xxx : a North-American geo-location.");
				System.err.println("2. 93.xxx.xxx.xxx  : an European geo-location.");
				System.err.println("3. 182.xxx.xxx.xxx :  an Asian geo-location.");

				logger.write(">>> Error >>> Invalid IP address");
				System.err.println("\nInvalid IP address");

				return true;
			}
		} catch (Exception e) {
			logger.write(">>> Error >>> Invalid IP address >>> " + e);
			System.err.println(e + "\nInvalid IP address");
			return true;
		}

		return false;
	}
	
	private boolean isValidUsername(String username) {

		if (username.length() < USERNAME_MIN_LENGTH) {

			System.err.println("\nA player's username must have at least 6 characters\n");
			logger.write(">>> Error >>> A player's username must have at least 6 characters");
			return true;
		}
		if (username.length() > USERNAME_MAX_LENGTH) {

			System.err.println("\nA player's username can be maximum length of 15 characters\n");
			logger.write(">>> Error >>> A player's username can be maximum length of 15 characters");
			return true;
		}
		return false;
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}