package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import GameServerApp.GameServer;
import GameServerApp.GameServerHelper;

import Logger.FileLogger;

public class AsianServer {

	// Registry URL
	static final String registryURL = "Asia";
	// Server Name
	static final String serverName = "AsianServer";
	static final String serverShortName = "AS";
	// UDP Server Ports
	static final int NA_PORT = 5001;
	static final int EU_PORT = 5002;
	static final int AS_PORT = 5003;
	// Orb Port
	static final String ORB_PORT = "1050";
	// Max Packet Size
	static final int MAX_PACKET_SIZE = 1024;
	// Logger Path
	static final String loggerPath = "./logs/ServerLogs/";
	// Initialize Server Logger
	static FileLogger logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");
	// CORBA Server Var
	static AsianServerImpl AsianServerObj;

	public static void main(String[] args) throws InterruptedException, IOException {

		ORB orb = ORB.init(AsianServer.getConfig(), null);

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
			AsianServerObj = new AsianServerImpl();
			AsianServerObj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(AsianServerObj);
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
		DatagramSocket socket;
		DatagramPacket requestPacket;
		DatagramPacket responsePacket;
		String reciveDataString, status = "";

		try {
			
			// Socket
			socket = new DatagramSocket(AS_PORT);
			
			while (true) {

				byte[] sendData = new byte[MAX_PACKET_SIZE];
				byte[] reciveData = new byte[MAX_PACKET_SIZE];

				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(), requestPacket.getLength());

				String data = reciveDataString.split(":", 2)[1];

				if (reciveDataString.contains("getPlayerStatus")) {

					logger.write(">>> Recived UDP request");
					status = AsianServerObj.getOwnStatus();
					logger.write(">>> getOwnStatus >>> " + status);

				} else if (reciveDataString.contains("transferAccount")) {

					logger.write(">>> Recived UDP request");

					String[] accountInfo = data.split("\\|");

					if (AsianServerObj.validateAccount(accountInfo[0])) {
						// Account with Given Name is already present
						logger.write(">>> transferAccount >>> Account with" + status + " username is already present");
						status = "false";
					} else {
						// Account Can transfer
						// FirstName, LastName, Age, Username, Password, IPAddress
						AsianServerObj.createPlayerAccount(accountInfo[2], accountInfo[3],
								Integer.parseInt(accountInfo[4]), accountInfo[0], accountInfo[1], accountInfo[5]);
						status = "true";
					}

				} else if (reciveDataString.contains("deleteTransferedAccount")) {


					logger.write(">>> Recived UDP request");

					String[] accountInfo = data.split("\\|");
					String Username = accountInfo[0];
					if (AsianServerObj.validateAccount(accountInfo[0])) {
						// Account with Given Name is present
						logger.write(">>> deleteTransferedAccount >>> Account with" + status + " username is present");
						// Delete Account
						status = Boolean.toString(AsianServerObj.deleteAccount(Username));
					} else {
						// Account with Given Name is present
						logger.write(">>> deleteTransferedAccount >>> Account with" + status + " username is not present");
						status = "false";
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
				//socket.close();

			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}

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
}
