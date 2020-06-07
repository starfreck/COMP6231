package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Logger.FileLogger;

public class NorthAmericanServer {

	// Registry URL
	static final String registryURL = "NorthAmerica";
	// Server Name
	static final String serverName = "NorthAmericanServer";
	static final String serverShortName = "NA";
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

	public NorthAmericanServer() {
		super();
	}

	public static void main(String[] args) {

		try {

			startRegistry(NA_REGISTRY_PORT);

			logger.write(">>> Create North American Server Object");
			NorthAmericanServerImpl NAServer = new NorthAmericanServerImpl();

			logger.write(">>> Binding " + registryURL + " at " + NA_REGISTRY_PORT);
			logger.write(">>> rmi://localhost:" + NA_REGISTRY_PORT + "/" + registryURL);
			Naming.rebind("rmi://localhost:" + NA_REGISTRY_PORT + "/" + registryURL, NAServer);

			System.out.println(serverName + " is started...");
			logger.write(">>> " + serverName + " is started...");

			// UDP server

			while (true) {

				DatagramSocket socket;
				DatagramPacket requestPacket;
				DatagramPacket responsePacket;

				String reciveDataString, status = "";

				byte[] sendData = new byte[MAX_PACKET_SIZE];
				byte[] reciveData = new byte[MAX_PACKET_SIZE];

				// Socket
				socket = new DatagramSocket(NA_PORT);

				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(),
						requestPacket.getLength());

				if (reciveDataString.equals("getPlayerStatus")) {
					logger.write(">>> Recived UDP request");
					status = NAServer.getOwnStatus();
					logger.write(">>> getOwnStatus >>> " + status);
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

		} catch (Exception e) {
			logger.write(">>> Exception >>> " + e);
			System.out.println("Exception" + e);
		}

	}

	private static void startRegistry(int port) throws RemoteException {
		try {

			// location, port
			Registry registry = LocateRegistry.getRegistry(serverName, port);
			registry.list();

		} catch (RemoteException e) {

			// No valid registry at given port.

			logger.write(">>> RMI registry cannot be found at port " + port);
			System.out.println("RMI registry cannot be found at port " + port);

			LocateRegistry.createRegistry(port);

			logger.write(">>> RMI registry created at port " + port);
			System.out.println("RMI registry created at port " + port);
		}
	}
}
