package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import Utilities.Ports;

public class ReplicaManager {

	int R1Count = 0;
	int R2Count = 0;
	int R3Count = 0;
	
	static Replica R1, R2,R3;
	
	public ReplicaManager() throws InterruptedException {
		
		R1 = new Replica("Replica1",true, Ports.RM_PORT, Ports.R1_PORT, Ports.AS_PORT_1, Ports.EU_PORT_1, Ports.NA_PORT_1);
		R2 = new Replica("Replica2",false, Ports.RM_PORT, Ports.R2_PORT, Ports.AS_PORT_2, Ports.EU_PORT_2, Ports.NA_PORT_2);
		R3 = new Replica("Replica3",false, Ports.RM_PORT, Ports.R3_PORT, Ports.AS_PORT_3, Ports.EU_PORT_3, Ports.NA_PORT_3);
		
		R1.start();
		R2.start();
		R3.start();	
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		// Create RM Object
		ReplicaManager RM = new ReplicaManager();
		
		// UDP Server
		DatagramSocket socket;
		DatagramPacket requestPacket;
		String reciveDataString = "";

		try {

			// Socket
			socket = new DatagramSocket(Ports.RM_PORT);
			
			System.out.println("\nRM UDP Server is running...\n");
			
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
	
	public void UDPServerRequestProcessor(String request) throws InterruptedException {
		
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
		System.out.println("\nR1:"+R1Count+" R2:"+R2Count+" R3:"+R3Count+"\n");
		
		// Check if any counter is 3 or > 3
		if(R1Count >= 3) {
			System.out.println("\nStopping R1... \n");
			R1.stop();
			R1Count = 0;
			// Start new Replica 1 with startNewReplica() as leader;
			System.out.println("\nStarting a new R1... \n");
			startNewReplica("R1");
		}
		
		if(R2Count >= 3) {
			System.out.println("\nStopping R2... \n");
			R2.stop();
			R2Count = 0;
			// Start new replica startNewReplica() as Replica 2;
			System.out.println("\nStarting a new R2... \n");
			startNewReplica("R2");
		}
	
		if(R3Count >= 3) {
			System.out.println("\nStopping R3... \n");
			R3.stop();
			R3Count = 0;
			// Start new replica startNewReplica() as Replica 3;
			System.out.println("\nStarting a new R3... \n");
			startNewReplica("R3");
		}
		
	}
	
	public void startNewReplica(String name) throws InterruptedException {
		
		if(name.equals("R1")) {
			R1 = new Replica("Replica1", true, Ports.RM_PORT, Ports.R1_PORT, Ports.AS_PORT_1, Ports.EU_PORT_1, Ports.NA_PORT_1);
			R1.start();
		} else if (name.equals("R2")) {
			R2 = new Replica("Replica2", false, Ports.RM_PORT, Ports.R2_PORT, Ports.AS_PORT_2, Ports.EU_PORT_2, Ports.NA_PORT_2);
			R2.start();
		} else if (name.equals("R3")) {
			R3 = new Replica("Replica3", false, Ports.RM_PORT, Ports.R3_PORT, Ports.AS_PORT_3, Ports.EU_PORT_3, Ports.NA_PORT_3);
			R3.start();
		}
	}
}
