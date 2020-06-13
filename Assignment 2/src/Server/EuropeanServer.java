package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import GameServerApp.GameServer;
import GameServerApp.GameServerHelper;

import Logger.FileLogger;

public class EuropeanServer {

	// Registry URL
	static final String registryURL = "Europe";
	// Server Name
	static final String serverName = "EuropeanServer";
	static final String serverShortName = "EU";
	// UDP Server Ports
	static final int NA_PORT = 5001;
	static final int EU_PORT = 5002;
	static final int AS_PORT = 5003;
	// Registry Ports
	static final int AS_REGISTRY_PORT = 52575;
	static final int EU_REGISTRY_PORT = 52576;
	static final int NA_REGISTRY_PORT = 52577;
	// Max Packet Size
	static final int MAX_PACKET_SIZE = 1024;
	// Logger Path
	static final String loggerPath = "./logs/ServerLogs/";
	// Initialize Server Logger
	static FileLogger logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");
	// Server Args
	static String[] ServerArgs;
	// CORBA Server Var
	static EuropeanServerImpl EuropeanServerObj;

	public static void main(String[] args) throws InterruptedException, IOException {

		ORB orb = ORB.init(EuropeanServer.getConfig(), null);

		Thread threadOne = new Thread(new Runnable() {
			public void run() {
				CORBAServer(orb);
			}
		});

		Thread threadTwo = new Thread(new Runnable() {
			public void run() {

				try {
					UDPServer();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		threadOne.start();
		threadTwo.start();

		Thread.sleep(1000);

	}

	public static void CORBAServer(ORB orb) {

		try {

			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			EuropeanServerObj = new EuropeanServerImpl();
			EuropeanServerObj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(EuropeanServerObj);
			GameServer href = GameServerHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name(registryURL);

			ncRef.rebind(path, href);

			System.out.println(serverName + " ready and waiting ...");

			orb.run();
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println(serverName + " Exiting ...");
	}

	public static void UDPServer() throws IOException {

		// UDP server

		while (true) {

			DatagramSocket socket;
			DatagramPacket requestPacket;
			DatagramPacket responsePacket;

			String reciveDataString, status = "";

			byte[] sendData = new byte[MAX_PACKET_SIZE];
			byte[] reciveData = new byte[MAX_PACKET_SIZE];

			// Socket
			socket = new DatagramSocket(EU_PORT);

			// Client Request Data
			requestPacket = new DatagramPacket(reciveData, reciveData.length);
			socket.receive(requestPacket);
			reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(),
					requestPacket.getLength());

			String data = reciveDataString.split(":",2)[1];

			if (reciveDataString.contains("getPlayerStatus")) {
				
				logger.write(">>> Recived UDP request");
				status = EuropeanServerObj.getOwnStatus();
				logger.write(">>> getOwnStatus >>> " + status);
			
			} else if (reciveDataString.contains("transferAccount")) {
				
				logger.write(">>> Recived UDP request");
				
				String[] accountInfo = data.split("\\|");
				
				if(EuropeanServerObj.validateAccount(accountInfo[0])) {
					// Account with Given Name is already present
					logger.write(">>> transferAccount >>> Account with" + status + " username is already present");
					status = "false";
				} else {
					// Account Can transfer
					// FirstName, LastName, Age, Username, Password, IPAddress
					EuropeanServerObj.createPlayerAccount(accountInfo[2],accountInfo[3],Integer.parseInt(accountInfo[4]),accountInfo[0],accountInfo[1],accountInfo[5]);
					status = "true";
				}
				
			}

			// Get Client's IP & Port
			InetAddress IPAddress = requestPacket.getAddress();
			int port = requestPacket.getPort();
			// Converting Message into Bytes
			sendData = status.getBytes();
			responsePacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			socket.send(responsePacket);
			logger.write(">>> Sending response of UDP request");
			socket.close();

		}

	}

	static String[] getConfig() throws IOException {

		String[] orbarg = new String[4];

		// Creating args array for ORB.init()
		orbarg[0] = "-ORBInitialPort";
		orbarg[1] = "1050";
		orbarg[2] = "-ORBInitialHost";
		orbarg[3] = "localhost";

		return orbarg;
	}
}
