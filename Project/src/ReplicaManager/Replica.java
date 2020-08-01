package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import Servers.AsianServerImpl;
import Servers.EuropeanServerImpl;
import Servers.NorthAmericanServerImpl;
import Utilities.Ports;

public class Replica {
	
	// Flag Variables
	boolean Flag = true;
	// Leader Variables
	boolean isLeader = false;
	// Socket Variables
	DatagramSocket socket;
	// Ports
	int RE_PORT;
	int RM_PORT;
	int AS_PORT;
	int EU_PORT;
	int NA_PORT;
	// Servers' Variables
	AsianServerImpl AS;
	EuropeanServerImpl EU;
	NorthAmericanServerImpl NA;
	
	public Replica(boolean isLeader, int RM_PORT, int RE_PORT, int AS_PORT,int EU_PORT, int NA_PORT) {
		
		this.isLeader = isLeader;
		this.RE_PORT  = RE_PORT;
		this.RM_PORT  = RM_PORT;
		this.AS_PORT  = AS_PORT;
		this.EU_PORT  = EU_PORT;
		this.NA_PORT  = NA_PORT;
		
		// Starting UDP Server
		Thread thread = new Thread(new Runnable() {
			public void run() {
				UDPServer();
			}
		});
		thread.start();
	}
	
	public void start() throws InterruptedException {
		
		//System.out.println("\n>>> Starting a new Cluster...\n");
		AS = new AsianServerImpl(AS_PORT, EU_PORT, NA_PORT);
		EU = new EuropeanServerImpl(AS_PORT, EU_PORT, NA_PORT);
		NA = new NorthAmericanServerImpl(AS_PORT, EU_PORT, NA_PORT);
	}
	
	public void stop() {
		// Kill all servers 
		AS.kill();
		EU.kill();
		NA.kill();
		Flag = false;
		socket.close();
		
	}
	
	public void UDPFIFO() {
		
		if(isLeader) {
			// Client Code
		} else {
			// Server Code
		}
	}
	
	// It will listen for the requests coming from FA
	public void UDPServer() {
		
		// UDP server
		DatagramPacket requestPacket;
		DatagramPacket responsePacket;
		String reciveDataString, status = "";
	
		try {
	
			// Socket
			socket = new DatagramSocket(RE_PORT);
	
			while (Flag) {
				
				byte[] sendData = new byte[Ports.MAX_PACKET_SIZE];
				byte[] reciveData = new byte[Ports.MAX_PACKET_SIZE];
	
				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(), requestPacket.getLength());
	
				String methodName 		= reciveDataString.split(":", 2)[0];
				String data 	  		= reciveDataString.split(":", 2)[1];
				
				
//				System.out.println("methodAction : "+methodName);
//				System.out.println("data : "+data);
			
				// Follow Leader Actions
				if(isLeader) {
					status = leaderActions(methodName, data);
				}
				// wait for request which are coming from Leader
				else {
					status = replicaActions(methodName, data);
					
					// Create an array of random Strings
					if(RE_PORT == Ports.R3_PORT){
						String[] randomResponse = {status, "Something went wrong with the server !"};
						status = randomResponse[new Random().nextInt(randomResponse.length)];
					}
				}
				
				// Get Client's IP & Port
				InetAddress IPAddress = requestPacket.getAddress();
				int port = requestPacket.getPort();
				// Converting Message into Bytes
				sendData = status.getBytes();
				responsePacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				socket.send(responsePacket);
	
			}
			
		} catch (SocketException e) {
			//System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}
	}
	
	public String leaderActions(String methodName, String data) {
		
		String response = "";
		String RMRequestData = "";
		String R1Response = "",R2Response = "",R3Response = "";
		
		String ClientIPAddress  = data.split("\\|")[0];
		
		// 1. Send the message to Server via UDP
		R1Response = UDPServerTunnel(ClientIPAddress, methodName+"Leader", data);
		System.out.println("R1Response: "+R1Response);
		
		// 2. Send Message to Other Replicas via UDP FIFO & take the majority and send the result to client
		if(RE_PORT == Ports.R1_PORT) {
			
			R2Response = sendMessageToReplica(Ports.R2_PORT, methodName, data);
			System.out.println("R2Response: "+R2Response);
			R3Response = sendMessageToReplica(Ports.R3_PORT, methodName, data);
			System.out.println("R3Response: "+R3Response);
		} else if(RE_PORT == Ports.R2_PORT) {
			
			R2Response = sendMessageToReplica(Ports.R1_PORT, methodName, data);
			System.out.println("R2Response: "+R2Response);
			R3Response = sendMessageToReplica(Ports.R3_PORT, methodName, data);
			System.out.println("R3Response: "+R3Response);
		
		} else if(RE_PORT == Ports.R3_PORT) {
			
			R2Response = sendMessageToReplica(Ports.R1_PORT, methodName, data);
			System.out.println("R2Response: "+R2Response);
			R3Response = sendMessageToReplica(Ports.R2_PORT, methodName, data);
			System.out.println("R3Response: "+R3Response);
		}
		
		// 3. Compare the results
		
		// R1 == R2 == R3 -> T|T|T
		if(R1Response.equals(R2Response) && R1Response.equals(R3Response)){
			
			// all are same
			// send this to RM => "T|T|T"
			RMRequestData = "T|T|T";
			// Send outputR1/R2/R3 to Front-End 
			response      =  R1Response;
		}
		// R1 != R2 == R3 -> F|T|T
		else if(!R1Response.equals(R2Response) && R1Response.equals(R3Response)) {
			// Leader(R1) is wrong
			// send this to RM => "F|T|T"
			RMRequestData = "F|T|T";
			// Send outputR2/R3 to Front-End
			response      =  R2Response;
		}
		// R1 == R2 != R3 -> T|T|F
		else if(R1Response.equals(R2Response) && !R1Response.equals(R3Response)){

			// R1 == R2
			// R3 is wrong
			// send this to RM => "T|T|F"
			RMRequestData = "T|T|F";
			// Send outputR1/R2 to Front-End
			response      =  R3Response;
			
		}
		// R1 != R2 != R3 (R1 == R3) -> T|F|T
		else if(R1Response.equals(R3Response) && !R1Response.equals(R2Response) && !R3Response.equals(R2Response) ){
			// R2 is wrong
			// leader, R1 right (Send this to the client)
			// send this to RM => "T|F|T"
			RMRequestData = "F|T|T";
			// Send outputR1/R3 to Front-End 
			response      =  R3Response;
		} 
		
		// 4. send results to RM
		SendResultsToRM(RMRequestData);
		
		return response;
	}
	
	public String replicaActions(String methodName, String data) {
		
		String ClientIPAddress  = data.split("\\|")[0];
		
		return UDPServerTunnel(ClientIPAddress, methodName+"Leader", data);
	}
	
	private String sendMessageToReplica(int Port,String methodName, String Data) {

		String response = "";

		// UDP client
		try {

			String methodAction = methodName + ":" + Data;
			
			DatagramSocket socket;
			DatagramPacket requestData;
			DatagramPacket responseData;
			InetAddress host = InetAddress.getLocalHost();

			byte[] sendMessage = methodAction.getBytes();
			byte[] recivedMessage = new byte[Ports.MAX_PACKET_SIZE];

			// Get status from Given Server
			socket = new DatagramSocket();

			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, Port);
			socket.send(requestData);

			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);

			// Retrieving Data
			response = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());

			socket.close();

		} catch (Exception e) {
			System.err.println(e);
		}

		return response;
	}
	
	
	private String UDPServerTunnel(String IPAddress, String methodName, String Data) {

		String response = "";
		int UDP_PORT;

		if (IPAddress.split("\\.")[0].equals("132")) {
			UDP_PORT = NA_PORT;
		} else if (IPAddress.split("\\.")[0].equals("93")) {
			UDP_PORT = EU_PORT;
		} else if (IPAddress.split("\\.")[0].equals("182")) {
			UDP_PORT = AS_PORT;
		} else {
			return "Unknown server name";
		}

		// UDP client
		try {

			String methodAction = methodName + ":" + Data;
			DatagramSocket socket;
			DatagramPacket requestData;
			DatagramPacket responseData;
			InetAddress host = InetAddress.getLocalHost();

			byte[] sendMessage = methodAction.getBytes();
			byte[] recivedMessage = new byte[Ports.MAX_PACKET_SIZE];

			// Get status from Given Server
			socket = new DatagramSocket();

			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, UDP_PORT);
			socket.send(requestData);

			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			socket.receive(responseData);

			// Retrieving Data
			response = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());

			socket.close();

		} catch (Exception e) {
			System.err.println(e);
		}

		return response;

	}
	
	// One Way Message Passing To RM
	private void SendResultsToRM(String request) {
		
		try {
			
			DatagramSocket socket;
			DatagramPacket requestData;
			InetAddress host = InetAddress.getLocalHost();

			byte[] sendMessage = request.getBytes();

			// Get status from Given Server
			socket = new DatagramSocket();

			// Request Data
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, RM_PORT);
			socket.send(requestData);
			
			socket.close();

		} catch (Exception e) {
			System.err.println(e);
		}
	}
	
	public boolean isLeader() {
		return isLeader;
	}

}
