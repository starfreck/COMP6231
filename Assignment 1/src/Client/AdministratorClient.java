package Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Server.GameServer;
import Logger.FileLogger;

public class AdministratorClient {

	static boolean status = true;
	static int PASSWORD_LENGTH = 6;
	static int USERNAME_MIN_LENGTH = 6;
	static int USERNAME_MAX_LENGTH = 15;
	// Registry Ports
	static final int AS_REGISTRY_PORT = 52575;
	static final int EU_REGISTRY_PORT = 52576;
	static final int NA_REGISTRY_PORT = 52577;

	static String[] validIPs = { "132", "93", "182" };
	static Scanner input = new Scanner(System.in);

	static HashMap<String, String> gameServers = new HashMap<String, String>();

	// Initialize Logger
	static FileLogger logger = new FileLogger("./logs/AdministratorClient/", "AdministratorClientLogs.log");

	public AdministratorClient() {

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
		System.out.println("2. Exit");
	}

	// Return basic menu.
	public void selectMenu(int choice, AdministratorClient admin) {
		switch (choice) {
		case 1:
			admin.getStatus();
			break;
		case 2:
			System.out.println("\nGood Bye.");
			AdministratorClient.status = false;
			System.exit(0);
			break;
		default:
			System.out.println("\nInvalid Choice.");
			break;
		}

	}

	private void getStatus() {

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

			AdministratorClient admin = new AdministratorClient();

			while (AdministratorClient.status) {

				AdministratorClient.showMenu();
				int choice = inputChoice();
				admin.selectMenu(choice, admin);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) {

		String status = "false";

		String serverName = gameServers.get(IPAddress.split("\\.")[0]);

		logger.write(">>> Get Player Status >>> Sending request to " + serverName);
		// find the remote object and cast it to an interface object
		GameServer server = getRMIObject(serverName);

		if (server == null) {
			logger.write(">>> Get Player Status >>> Server not found");
			return status;
		}

		try {
			status = server.getPlayerStatus(AdminUsername, AdminPassword, IPAddress);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}

		return "\n"+status;
	}

	private GameServer getRMIObject(String serverName) {

		GameServer server = null;
		int port = 0;

		// find the remote object and cast it to an interface object
		try {

			if (serverName.equals("Asia")) {
				port = AS_REGISTRY_PORT;
			} else if (serverName.equals("Europe")) {
				port = EU_REGISTRY_PORT;
			} else if (serverName.equals("NorthAmerica")) {
				port = NA_REGISTRY_PORT;
			}

			Registry registry = LocateRegistry.getRegistry(port);
			server = (GameServer) registry.lookup(serverName);

		} catch (NotBoundException e) {
			logger.write(">>> NotBoundException Exception >>> " + e);
			e.printStackTrace();
		} catch (RemoteException e) {
			logger.write(">>> RemoteException Exception >>> " + e);
			e.printStackTrace();
		}

		return server;
	}

	private String inputUsername() {
		System.out.print("\nEnter username: ");
		String username = input.nextLine();
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

			for (int i = 0; i < ipaddress.split("\\.").length ; i++) {

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
			logger.write(">>> Error >>> Invalid IP address >>> "+e);
			System.err.println(e + "\nInvalid IP address");
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