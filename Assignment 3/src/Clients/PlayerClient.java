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

public class PlayerClient {

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
	static String[] PlayerClientArgs;

	// Initialize Logger
	static FileLogger logger = new FileLogger("./logs/PlayerClient/", "PlayerClientLogs.log");

	static HashMap<String, String> gameServers = new HashMap<String, String>();

	public PlayerClient(String[] args) throws IOException {

		// Initialize Servers List
		gameServers.put("132", "NorthAmerica");
		gameServers.put("93", "Europe");
		gameServers.put("182", "Asia");

	}

	// Return menu.
	public static void showMenu() {
		System.out.println("\n****Welcome Client****\n");
		System.out.println("Please select an option (1-4)");
		System.out.println("1. Create an Account");
		System.out.println("2. SignIn");
		System.out.println("3. SignOut");
		System.out.println("4. Transfer Account");
		System.out.println("5. Exit");

	}

	public void selectMenu(int choice, PlayerClient player) {
		switch (choice) {
		case 1:
			player.createAccount();
			break;
		case 2:
			player.SignIn();
			break;
		case 3:
			player.SignOut();
			break;
		case 4:
			player.transferAccount();
			break;
		case 5:
			System.out.println("\nGood Bye.");
			PlayerClient.status = false;
			System.exit(0);
			break;
		default:
			System.out.println("\nInvalid Choice.");
			break;
		}

	}

	public static void main(String[] args) {

		try {

			PlayerClient player = new PlayerClient(args);

			while (PlayerClient.status) {

				PlayerClient.showMenu();
				int choice = inputChoice();
				player.selectMenu(choice, player);
			}
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public void createAccount() {

		logger.write(">>> Create Account");

		try {
			String firstname = inputFirstName();
			logger.write(">>> Create Account >>> firstname : " + firstname);
			String lastname = inputLastName();
			logger.write(">>> Create Account >>> lastname : " + lastname);
			int age = inputAge();
			logger.write(">>> Create Account >>> age : " + age);
			String username = inputUsername();
			logger.write(">>> Create Account >>> username : " + username);
			String password = inputPassword();
			logger.write(">>> Create Account >>> password : " + password);
			String ipaddress = inputIPAddress();
			logger.write(">>> Create Account >>> ipaddress : " + ipaddress);

			// Print Response
			logger.write(">>> Create Account >>> Sending data to createPlayerAccount()");
			String response = this.createPlayerAccount(firstname, lastname, age, username, password, ipaddress);
			System.out.println(response);
			logger.write(">>> Create Account >>> Respone from remote method >>> " + response);

		} catch (Exception e) {
			System.out.println(e);
			logger.write(">>> Create Account >>> Error >>> " + e);
		}
	}

	public void SignIn() {

		try {

			String username = inputUsername();
			String password = inputPassword();
			String ipaddress = inputIPAddress();

			// Print Response
			System.out.println(this.playerSignIn(username, password, ipaddress));

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void SignOut() {

		try {
			String username = inputUsername();
			String ipaddress = inputIPAddress();

			// Print Response
			System.out.println(this.playerSignOut(username, ipaddress));
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void transferAccount() {

		try {
			// username, Password, OldIPAddress, NewIPAddress
			String username = inputUsername();
			String password = inputPassword();
			String oldIPAddress = inputIPAddress();
			String newIPAddress = inputNewIPAddress();

			// Print Response
			System.out.println(this.PlayerTransferAccount(username, password, oldIPAddress, newIPAddress));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password,String IPAddress) throws MalformedURLException {
		String status = "false";
		String serverName = gameServers.get(IPAddress.split("\\.")[0]);

		// find the remote object and cast it to an interface object
		GameServer server = getServerObj(serverName);

		if (server == null) {
			return status;
		}

		status = server.createPlayerAccount(FirstName, LastName, Age, Username, Password, IPAddress);

		return "\n" + status;

	}

	public String playerSignIn(String Username, String Password, String IPAddress) throws MalformedURLException{
		String status = "false";
		String serverName = gameServers.get(IPAddress.split("\\.")[0]);

		// find the remote object and cast it to an interface object
		GameServer server = getServerObj(serverName);

		if (server == null) {
			return status;
		}

		status = server.playerSignIn(Username, Password, IPAddress);

		return "\n" + status;
	}

	public String playerSignOut(String Username, String IPAddress) throws MalformedURLException {
		String status = "false";
		String serverName = gameServers.get(IPAddress.split("\\.")[0]);

		// find the remote object and cast it to an interface object
		GameServer server = getServerObj(serverName);

		if (server == null) {
			return status;
		}

		status = server.playerSignOut(Username, IPAddress);

		return "\n" + status;
	}
	
	public String PlayerTransferAccount(String Username, String Password, String oldIPAddress, String newIPAddress) throws MalformedURLException {
		String status = "false";
		String serverName = gameServers.get(oldIPAddress.split("\\.")[0]);

		// find the remote object and cast it to an interface object
		GameServer server = getServerObj(serverName);

		if (server == null) {
			return status;
		}

		status = server.transferAccount(Username, Password, oldIPAddress, newIPAddress);

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

	public static int inputChoice() {

		int choice = 0;
		boolean inputError = true;

		do {
			try {
				System.out.print("\nSelect >>> ");
				choice = Integer.parseInt(input.nextLine());
				inputError = false;
			} catch (Exception e) {
				System.err.println("\nPlease input Integer only");
			}
		} while (inputError);

		return choice;
	}

	private String inputFirstName() {

		String firstname;

		do {
			System.out.print("\nEnter first name: ");
			firstname = input.nextLine();

			if (firstname.isEmpty())
				System.err.print("\nFirst name cannot be empty\n");

		} while (firstname.isEmpty());

		return firstname;
	}

	private String inputLastName() {

		String lastname;

		do {
			System.out.print("\nEnter last name: ");
			lastname = input.nextLine();

			if (lastname.isEmpty())
				System.err.print("\nLast name cannot be empty\n");

		} while (lastname.isEmpty());

		return lastname;
	}

	private int inputAge() {

		int age = 0;
		boolean inputError = true;

		do {
			try {
				System.out.print("\nEnter age: ");
				age = Integer.parseInt(input.nextLine());
				inputError = false;
			} catch (Exception e) {
				System.err.println("\nPlease input Integer only");
			}
		} while (inputError);

		return age;
	}

	private String inputUsername() {
		String username;

		do {
			System.out.print("\nEnter username: ");
			username = input.nextLine();
		} while (isValidUsername(username));

		return username;
	}

	private String inputPassword() {

		String password;

		do {
			System.out.print("\nEnter password: ");
			password = input.nextLine();
		} while (isValidPassword(password));

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
	
	private String inputNewIPAddress() {

		String ipaddress;

		do {
			System.out.print("\nEnter New IP address: ");
			ipaddress = input.nextLine();

		} while (isValidIPAddress(ipaddress));

		return ipaddress;
	}

	private boolean isValidPassword(String password) {

		if (password.length() < PASSWORD_LENGTH) {

			System.err.println("\nA password must have at least six characters");
			return true;
		}
		return false;
	}

	private boolean isValidUsername(String username) {

		if (username.length() < USERNAME_MIN_LENGTH) {

			System.err.println("\nA username must have at least 6 characters");
			return true;
		}
		if (username.length() > USERNAME_MAX_LENGTH) {

			System.err.println("\nA username can be maximum length of 15 characters");
			logger.write(">>> Error >>> A username can be maximum length of 15 characters");
			return true;
		}
		return false;
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
				System.err.print("\nIP address cannot be empty\n");
				logger.write(">>> Error >>> IP address cannot be empty");
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
			System.err.println(e + "\nInvalid IP address");
			logger.write(">>> Error >>> Invalid IP address " + e);
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
