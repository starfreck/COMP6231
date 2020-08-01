package Clients;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import FrontEndApp.FrontEnd;
import FrontEndApp.FrontEndHelper;
import Utilities.FileLogger;

public class Test {
	// Orb Port
	static final String ORB_PORT = "1050";

	static int PASSWORD_LENGTH = 6;
	static int USERNAME_MIN_LENGTH = 6;
	static int USERNAME_MAX_LENGTH = 15;

	static String[] TestArgs;
	static String[] validIPs = { "132", "93", "182" };
	static Scanner input = new Scanner(System.in);

	// Initialize Logger
	static FileLogger logger = new FileLogger("./logs/Test/", "TestLogs.log");

	static HashMap<String, String> gameServers = new HashMap<String, String>();
	
	static int CHOICE = 0;
	
	Test() throws IOException {
		// Init args
		TestArgs = Test.getConfig();

		// Initialize Servers List
		gameServers.put("132", "NorthAmerica");
		gameServers.put("93", "Europe");
		gameServers.put("182", "Asia");
	}

	public static void main(String[] args) throws IOException, InterruptedException, InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

		System.out.println(">>> Concurrency Test");
		System.out.println("Case 1 : suspendAccount() will run first and then transferAccout()");
		System.out.println("Case 2 : transferAccout() will run first and then suspendAccount()");
		System.out.println("Case 3 : transferAccout() and suspendAccount() will run together");
		
		Test.CHOICE = inputChoice();
		String Username = inputUsername();
		String Password = inputPassword();
		String IPAddress = inputIPAddress("Enter IPAddress");
		String newIPAddress = inputIPAddress("Enter New IPAddress");
		String AdminUsername = "Admin";
		String AdminPassword = "Admin";
		String oldIPAddress = IPAddress;
		String UsernameToSuspend = Username;

		logger.write(">>> Concurrency Test >>> Username >>> " + Username);
		logger.write(">>> Concurrency Test >>> Password >>> " + Password);
		logger.write(">>> Concurrency Test >>> IPAddress >>> " + IPAddress);
		logger.write(">>> Concurrency Test >>> newIPAddress >>> " + newIPAddress);
		logger.write(">>> Concurrency Test >>> AdminPassword >>> " + AdminPassword);
		logger.write(">>> Concurrency Test >>> oldIPAddress >>> " + oldIPAddress);
		logger.write(">>> Concurrency Test >>> UsernameToSuspend >>> " + UsernameToSuspend);
		
		Test TestCase = new Test();
		
		Thread Thread1 = new Thread("Thread1") {

			public void run() {
				try {
					// calling transferAccount()
					
					if(Test.CHOICE == 1) {
						logger.write(">>> Concurrency Test >>> transferAccount() >>> going to sleep 1 second");
						sleep(1000);
					}
					String status2 =  TestCase.transferAccount(Username, Password, oldIPAddress, newIPAddress);
					System.out.println("\nResult of transferAccount() : "+ status2);
				} catch (InvalidName | NotFound | CannotProceed
						| org.omg.CosNaming.NamingContextPackage.InvalidName | InterruptedException e) {
					e.printStackTrace();
				}

			}
		};

		Thread Thread2 = new Thread("Thread2") {

			public void run() {

				try {
					// calling suspendAccount()
					
					if(Test.CHOICE == 2) {

						logger.write(">>> Concurrency Test >>> suspendAccount() >>> going to sleep 1 second");
						sleep(1000);
					}
					
					String status2 = TestCase.suspendAccount(AdminUsername, AdminPassword, IPAddress, UsernameToSuspend);
					System.out.println("\nResult of suspendAccount() :"+ status2);
				} catch (InvalidName | NotFound | CannotProceed
						| org.omg.CosNaming.NamingContextPackage.InvalidName | InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		Thread1.start();
		Thread2.start();

		Thread1.join();
		Thread2.join();
		
		String status = TestCase.getPlayerStatus(AdminUsername, AdminPassword, IPAddress);
		System.out.println("\nFinal State =>"+ status);
		
	}

	public String transferAccount(String Username, String Password, String oldIPAddress, String newIPAddress)
			throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		String status = "false";
		
		// find the remote object and cast it to an interface object
		FrontEnd server = getServerObj();

		if (server == null) {
			return status;
		}

		status = server.transferAccount(Username, Password, oldIPAddress, newIPAddress);

		return "\n" + status;
	}
	
	public String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress)
			throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

		String status = "false";
		// find the remote object and cast it to an interface object
		FrontEnd server = getServerObj();

		if (server == null) {
			logger.write(">>> Concurrency Test >>> Server not found");
			return status;
		}

		status = server.getPlayerStatus(AdminUsername, AdminPassword, IPAddress);

		return "\n" + status;
	}
	
	public String suspendAccount(String AdminUsername, String AdminPassword, String IPAddress, String UsernameToSuspend)
			throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

		String status = "false";

		FrontEnd server = getServerObj();
		
		if (server == null) {
			logger.write(">>>  Suspend Account >>> Server not found");
			return status;
		}

		status = server.suspendAccount(AdminUsername, AdminPassword, IPAddress, UsernameToSuspend);

		return "\n" + status;
	}

	private FrontEnd getServerObj()
			throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

		ORB orb = ORB.init(TestArgs, null);
		Object objRef = orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		FrontEnd server = (FrontEnd) FrontEndHelper.narrow(ncRef.resolve_str("FrontEnd"));

		return server;
	}
	
	static String[] getConfig() throws IOException {

		String[] orbarg = new String[4];

		// Creating args array for ORB.init()
		orbarg[0] = "-ORBInitialPort";
		orbarg[1] = ORB_PORT;
		orbarg[2] = "-ORBInitialHost";
		orbarg[3] = "localhost";

		return orbarg;
	}

	
	static int inputChoice() {

		int choice = 0;
		boolean inputError = true;

		do {
			try {
				System.out.print("\nSelect CASE >>> ");
				choice = Integer.parseInt(input.nextLine());
				inputError = false;
			} catch (Exception e) {
				System.err.println("\nPlease input Integer only");
			}
			
			if (choice > 3) {
				System.err.println("\nPlease input valid choice");
				inputError = true;
			}
			
			if (choice < 1) {
				System.err.println("\nPlease input valid choice");
				inputError = true;
			}
			
		} while (inputError);

		return choice;
	}
	
	static String inputUsername() {
		String username;

		do {
			System.out.print("\nEnter username: ");
			username = input.nextLine();
		} while (isValidUsername(username));

		return username;
	}

	static String inputPassword() {

		String password;

		do {
			System.out.print("\nEnter password: ");
			password = input.nextLine();
		} while (isValidPassword(password));

		return password;
	}

	static String inputIPAddress(String message) {

		String ipaddress;

		do {
			System.out.print("\n" + message + ": ");
			ipaddress = input.nextLine();

		} while (isValidIPAddress(ipaddress));

		return ipaddress;
	}

	private static boolean isValidPassword(String password) {

		if (password.length() < PASSWORD_LENGTH) {

			System.err.println("\nA password must have at least six characters");
			return true;
		}
		return false;
	}

	private static boolean isValidUsername(String username) {

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

	private static boolean isValidIPAddress(String ipaddress) {

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
