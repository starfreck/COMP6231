package Servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import javax.xml.ws.Endpoint;

import Logger.FileLogger;
import Services.NorthAmericanServerImpl;

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
	// Web Service Ports
	static final String AS_WS_PORT = "8081";
	static final String EU_WS_PORT = "8082";
	static final String NA_WS_PORT = "8083";
	// Max Packet Size
	static final int MAX_PACKET_SIZE = 1024;
	// Logger Path
	static final String loggerPath = "./logs/ServerLogs/";
	// Initialize Server Logger
	static FileLogger logger = new FileLogger(loggerPath + serverName + "/", serverName + ".log");
	// CORBA Server Var
	static NorthAmericanServerImpl NorthAmericanServerObj;

	public static void main(String[] args) {

		NorthAmericanServerObj = new NorthAmericanServerImpl();
		Endpoint endpoint = Endpoint.publish("http://localhost:" + NA_WS_PORT + "/" + registryURL, NorthAmericanServerObj);
		
		if(endpoint.isPublished()) {
			System.out.println(serverName + " ready and waiting ...");
		} else {
			System.out.println(serverName + " isn't ready ...");
		}
		
		// UDP server
		DatagramSocket socket;
		DatagramPacket requestPacket;
		DatagramPacket responsePacket;
		String reciveDataString, status = "";

		try {

			// Socket
			socket = new DatagramSocket(NA_PORT);

			while (true) {

				byte[] sendData = new byte[MAX_PACKET_SIZE];
				byte[] reciveData = new byte[MAX_PACKET_SIZE];

				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(),
						requestPacket.getLength());

				String data = reciveDataString.split(":", 2)[1];

				if (reciveDataString.contains("getPlayerStatus")) {

					logger.write(">>> Recived UDP request");
					status = NorthAmericanServerObj.getOwnStatus();
					logger.write(">>> getOwnStatus >>> " + status);

				} else if (reciveDataString.contains("transferAccount")) {

					logger.write(">>> Recived UDP request");

					String[] accountInfo = data.split("\\|");

					if (NorthAmericanServerObj.validateAccount(accountInfo[0])) {
						// Account with Given Name is already present
						logger.write(">>> transferAccount >>> Account with" + status + " username is already present");
						status = "false";
					} else {
						// Account Can transfer
						// FirstName, LastName, Age, Username, Password, IPAddress
						NorthAmericanServerObj.createPlayerAccount(accountInfo[2], accountInfo[3],
								Integer.parseInt(accountInfo[4]), accountInfo[0], accountInfo[1], accountInfo[5]);
						status = "true";
					}

					logger.write(">>> transferAccountStatus >>> " + status);

				} else if (reciveDataString.contains("deleteTransferedAccount")) {

					logger.write(">>> Recived UDP request");

					String[] accountInfo = data.split("\\|");
					String Username = accountInfo[0];
					if (NorthAmericanServerObj.validateAccount(accountInfo[0])) {
						// Account with Given Name is present
						logger.write(">>> deleteTransferedAccount >>> Account with" + status + " username is present");
						// Delete Account
						status = Boolean.toString(NorthAmericanServerObj.deleteAccount(Username));
					} else {
						// Account with Given Name is present
						logger.write(
								">>> deleteTransferedAccount >>> Account with" + status + " username is not present");
						status = "false";
					}

					logger.write(">>> deleteTransferedAccountStatus >>> " + status);

				}

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
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}
	}
}