package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import Utilities.Ports;

public class ReplicaManager {

	int R1Count = 0;
	int R2Count = 0;
	int R3Count = 0;
	
	static Replica R1, R2,R3;
	
	public ReplicaManager() {
		
		// Creating Replicas
		R1 = new Replica("Replica1",true, Ports.RM_PORT, Ports.R1_PORT, Ports.AS_PORT_1, Ports.EU_PORT_1, Ports.NA_PORT_1);
		R2 = new Replica("Replica2",false, Ports.RM_PORT, Ports.R2_PORT, Ports.AS_PORT_2, Ports.EU_PORT_2, Ports.NA_PORT_2);
		R3 = new Replica("Replica3",false, Ports.RM_PORT, Ports.R3_PORT, Ports.AS_PORT_3, Ports.EU_PORT_3, Ports.NA_PORT_3);
		
		R1.start();
		R2.start();
		R3.start();
		
		// Creating a thread for UDP HeartBeat Checker
		Thread heartBeat = new Thread(new Runnable() {
			public void run() {
				UDPHeartBeat();
			}
		});
		
		heartBeat.start();
	}
	
	public static void main(String[] args) {
		
		// Create RM Object
		ReplicaManager RM = new ReplicaManager();
		
		// UDP Server
		DatagramSocket socket;
		DatagramPacket requestPacket;
		String reciveDataString = "";

		try {

			// Socket
			socket = new DatagramSocket(Ports.RM_PORT);
			
			if(Ports.DEBUG) System.out.println("\nRM UDP Server is running...\n");
			
			while (true) {
				
				byte[] reciveData = new byte[Ports.MAX_PACKET_SIZE];

				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(),requestPacket.getLength());

				// Send the Data to UDP request processor
				if (!reciveDataString.isEmpty())
					RM.UDPServerRequestProcessor(reciveDataString);

				// Get Client's IP & Port
//				InetAddress IPAddress = requestPacket.getAddress();
//				int port = requestPacket.getPort();
				// Converting Message into Bytes
//				sendData = status.getBytes();
//				responsePacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
//				socket.send(responsePacket);
			}
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}		
	}
	
	public void UDPServerRequestProcessor(String request) {
		
		// R1, R2, R3
		// T | T | F
		
		String[] results = request.trim().split("\\|");
		
		for(int i = 0; i < 3; i++) {
			
			if(results[i].equals("T")) {
				// Skip it
				if(i == 0) { R1Count=0; }
				if(i == 1) { R2Count=0; }
				if(i == 2) { R3Count=0; }
			} else if(results[i].equals("F")) {
				// Increment by 1
				if(i == 0) { R1Count++; }
				if(i == 1) { R2Count++; }
				if(i == 2) { R3Count++; }
			}
			
		} // loop is ending here
		
		// Final State
		if(Ports.DEBUG) System.out.println("\nR1:"+R1Count+" R2:"+R2Count+" R3:"+R3Count+"\n");
		
		// Check if any counter is 3 or > 3
		if(R1Count >= 3) {
			stoppingOldReplica("R1");
			// Start new Replica 1 with startNewReplica() as leader;
			startNewReplica("R1");
		}
		
		if(R2Count >= 3) {
			stoppingOldReplica("R2");
			// Start new replica startNewReplica() as Replica 2;
			startNewReplica("R2");
		}
	
		if(R3Count >= 3) {
			stoppingOldReplica("R3");
			// Start new replica startNewReplica() as Replica 3;
			startNewReplica("R3");
		}
		
	}
	
	public void stoppingOldReplica(String name) {
		
		if(name.equals("R1")) {
			if(Ports.DEBUG) System.out.println("\nStopping R1... \n");
			R1.stop();
			R1Count = 0;
		} else if (name.equals("R2")) {
			if(Ports.DEBUG) System.out.println("\nStopping R2... \n");
			R2.stop();
			R2Count = 0;
		} else if (name.equals("R3")) {
			if(Ports.DEBUG) System.out.println("\nStopping R3... \n");
			R3.stop();
			R3Count = 0;
		}
	}

	public void startNewReplica(String name) {
		
		if(name.equals("R1")) {
			if(Ports.DEBUG) System.out.println("\nStarting a new R1... \n");
			R1 = new Replica("Replica1", true, Ports.RM_PORT, Ports.R1_PORT, Ports.AS_PORT_1, Ports.EU_PORT_1, Ports.NA_PORT_1);
			R1.start();
		} else if (name.equals("R2")) {
			if(Ports.DEBUG) System.out.println("\nStarting a new R2... \n");
			R2 = new Replica("Replica2", false, Ports.RM_PORT, Ports.R2_PORT, Ports.AS_PORT_2, Ports.EU_PORT_2, Ports.NA_PORT_2);
			R2.start();
		} else if (name.equals("R3")) {
			if(Ports.DEBUG) System.out.println("\nStarting a new R3... \n");
			R3 = new Replica("Replica3", false, Ports.RM_PORT, Ports.R3_PORT, Ports.AS_PORT_3, Ports.EU_PORT_3, Ports.NA_PORT_3);
			R3.start();
		}
	}
	
	public void UDPHeartBeat() {
		
		while(true) {
			// List the all Ports here and start sending UDP Heart Beats
			int servers[] = {Ports.R1_PORT, Ports.R2_PORT, Ports.R3_PORT, Ports.NA_PORT_1, Ports.NA_PORT_2, Ports.NA_PORT_3, Ports.EU_PORT_1, Ports.EU_PORT_2, Ports.EU_PORT_3, Ports.AS_PORT_1, Ports.AS_PORT_2, Ports.AS_PORT_3};
			
			for (int server : servers) {
				sendHeartBeatMessage(server);
			}
		}
	}
	
	private void sendHeartBeatMessage(int port) {

		// UDP client
		try {

			String message = "UDPHeartBeat:just a message to check server pulse";
			
			DatagramSocket socket;
			DatagramPacket requestData;
			DatagramPacket responseData;
			InetAddress host = InetAddress.getLocalHost();

			byte[] sendMessage = message.getBytes();
			byte[] recivedMessage = new byte[Ports.MAX_PACKET_SIZE];

			// Get status from Given Server
			socket = new DatagramSocket();

			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, port);
			socket.send(requestData);

			// Socket Timeout to 10 Seconds
			socket.setSoTimeout(Ports.UDP_REQUEST_TIMEOUT);
			
			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);
			socket.close();
			
			// Retrieving Data
			String reciveDataString = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());

			String methodName = reciveDataString.split(":", 2)[0];
			String data 	  = reciveDataString.split(":", 2)[1];
			
			// Validate the response
			if(!methodName.equals("UDPHeartBeat") && !data.equals("i am alive")) {
				throw new SocketTimeoutException("UDPHeartBeat is hijacked");
			} else {
				if(Ports.HEART_BEAT_DEBUG) System.out.println("Recived UDP HeartBeat from Port "+port);
			}

		} catch (SocketTimeoutException e) {
            // SocketTimeoutException inform RM
            UDPHeartBeatTimeout(port);
            
        } catch (Exception e) {
			System.err.println(e);
		}
	}
	
	private void UDPHeartBeatTimeout(int Port) {
		
		// Check the Port Group
		// Something is wrong with Replica 1 Restart it.
		if(Port == Ports.R1_PORT || Port == Ports.AS_PORT_1 || Port == Ports.EU_PORT_1 || Port == Ports.NA_PORT_1) {
			if(Ports.DEBUG) System.out.println("\nUDPHeartBeatTimeout occurred in R1... \n");
			stoppingOldReplica("R1");
			startNewReplica("R1");
		}
		
		// Something is wrong with Replica 2 Restart it.
		if(Port == Ports.R2_PORT || Port == Ports.AS_PORT_2 || Port == Ports.EU_PORT_2 || Port == Ports.NA_PORT_2) {
			if(Ports.DEBUG) System.out.println("\nUDPHeartBeatTimeout occurred in R2... \n");
			stoppingOldReplica("R2");
			startNewReplica("R2");
		}
		
		// Something is wrong with Replica 3 Restart it.
		if(Port == Ports.R3_PORT || Port == Ports.AS_PORT_3 || Port == Ports.EU_PORT_3 || Port == Ports.NA_PORT_3) {
			if(Ports.DEBUG) System.out.println("\nUDPHeartBeatTimeout occurred in R3... \n");
			stoppingOldReplica("R3");
			startNewReplica("R3");
		}
	}
	
}
