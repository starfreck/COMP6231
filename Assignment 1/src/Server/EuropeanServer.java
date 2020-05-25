package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Logger.FileLogger;

public class EuropeanServer {

	// Registry URL
	static final String registryURL = "Europe";
	// Registry Port
	static final int REGISTRY_PORT = 1099;
	// Server Name
	static final String serverName = "EuropeanServer";
	static final String serverShortName = "EU";
	// Loggers
	FileLogger logger;
	// Logger Path
	static final String loggerPath = "./logs/ServerLogs/";
	// UDP Server Ports
	static final int NA_PORT = 5001;
	static final int EU_PORT = 5002;
	static final int AS_PORT = 5003;
	// Max Packet Size
	static final int MAX_PACKET_SIZE = 1024;

	public EuropeanServer() {
		super();
		// Initialize Server Logger
		this.logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");
	}

	public static void main(String[] args) {

		try {

			startRegistry(REGISTRY_PORT);
			EuropeanServerImpl EUServer = new EuropeanServerImpl();

			Naming.rebind(registryURL, EUServer);
			System.out.println(serverName + " is started...");

			// UDP server will be here

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

				if (reciveDataString.equals("getPlayerStatus")) {
					status = EUServer.getOwnStatus();
				}

				// Get Client's IP & Port
				InetAddress IPAddress = requestPacket.getAddress();
				int port = requestPacket.getPort();
				// Converting Message into Bytes
				sendData = status.getBytes();
				responsePacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(responsePacket);
				socket.close();

			}

		} catch (Exception e) {
			System.out.println("Exception" + e);
		}

	}

	private static void startRegistry(int port) throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(port);
			registry.list();
		} catch (RemoteException e) {
			// No valid registry at given port.
			System.out.println("RMI registry cannot be found at port " + port);
			LocateRegistry.createRegistry(port);
			System.out.println("RMI registry created at port " + port);
		}
	}
}
