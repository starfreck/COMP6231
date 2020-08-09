package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import Servers.AsianServerImpl;
import Servers.EuropeanServerImpl;
import Servers.NorthAmericanServerImpl;
import Utilities.FileLogger;
import Utilities.Constants;


public class Replica {
	
	// Flag Variables
	boolean Flag = true;
	// Leader Variables
	boolean isLeader = false;
	// isWrongCounter Variables
	int isWrongCounter = 0;
	// Socket Variables
	DatagramSocket socket;
	// Ports
	int RE_PORT;
	int RM_PORT;
	int AS_PORT;
	int EU_PORT;
	int NA_PORT;
	// Name Variables
	String replicaName;
	// Loggers
	FileLogger logger;
	// Logger Path
	String loggerPath = "./logs/ReplicaLogs/";
	// Servers' Variables
	AsianServerImpl AS;
	EuropeanServerImpl EU;
	NorthAmericanServerImpl NA;
	// UDP FIFO Queue
	Queue<String[]> queue = new LinkedList<>();
	
	public Replica(String replicaName, boolean isLeader,int RM_PORT, int RE_PORT, int AS_PORT,int EU_PORT, int NA_PORT) {
		
		this.replicaName = replicaName;
		this.isLeader 	 = isLeader;
		this.RE_PORT  	 = RE_PORT;
		this.RM_PORT  	 = RM_PORT;
		this.AS_PORT  	 = AS_PORT;
		this.EU_PORT  	 = EU_PORT;
		this.NA_PORT  	 = NA_PORT;
		
		// Initialize Server Logger
		this.logger = new FileLogger(loggerPath + replicaName + "/", replicaName + ".log");
		
		// Starting UDP Server
		Thread thread = new Thread(new Runnable() {
			public void run() {
				UDPServer();
			}
		});
		thread.start();
	}
	
	public void start() {
		
		//System.out.println("\n>>> Starting a new Cluster...\n");
		AS = new AsianServerImpl(replicaName, AS_PORT, EU_PORT, NA_PORT);
		EU = new EuropeanServerImpl(replicaName, AS_PORT, EU_PORT, NA_PORT);
		NA = new NorthAmericanServerImpl(replicaName, AS_PORT, EU_PORT, NA_PORT);
	}
	
	public void stop() {
		// Kill all servers 
		AS.kill();
		EU.kill();
		NA.kill();
		// Killing Own UDP Server
		Flag = false;
		socket.close();
		
	}
	
	public String UDPFIFO() {
		
		String response = "";
		
		while(!queue.isEmpty()) {
			
			// Take out request from the queue
			String[] request = queue.remove();
			
			int port		  = Integer.parseInt(request[0]);
			String methodName = request[1];
			String data 	  = request[2];
			
			response = response+"|"+sendMessageToReplica(port, methodName, data);
			
		}
		
		return response;
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
				
				byte[] sendData = new byte[Constants.MAX_PACKET_SIZE];
				byte[] reciveData = new byte[Constants.MAX_PACKET_SIZE];
	
				// Client Request Data
				requestPacket = new DatagramPacket(reciveData, reciveData.length);
				socket.receive(requestPacket);
				reciveDataString = new String(requestPacket.getData(), requestPacket.getOffset(), requestPacket.getLength());
	
				// methodName:data1|data2|data3.........
				
				String methodName 		= reciveDataString.split(":", 2)[0];
				String data 	  		= reciveDataString.split(":", 2)[1];
				
				
//				System.out.println("methodAction : "+methodName);
//				System.out.println("data : "+data);
			
				// HeartBeat Checker
				if(methodName.equals("UDPHeartBeat")) {
					// data has "UDPHeartBeat:just a message to check server pulse"
					status = "UDPHeartBeat:i am alive";
				}
				
				// Other Requests
				else {
						// Follow Leader Actions
						if(isLeader) {
							
							// Adding Requests to UDP FIFO Queue
							if(RE_PORT == Constants.R1_PORT) {
								queue.add(new String[]{String.valueOf(Constants.R2_PORT),methodName,data});
								queue.add(new String[]{String.valueOf(Constants.R3_PORT),methodName,data});
							} else if(RE_PORT == Constants.R2_PORT) {
								queue.add(new String[]{String.valueOf(Constants.R1_PORT),methodName,data});
								queue.add(new String[]{String.valueOf(Constants.R3_PORT),methodName,data});
							} else if(RE_PORT == Constants.R3_PORT) {
								queue.add(new String[]{String.valueOf(Constants.R1_PORT),methodName,data});
								queue.add(new String[]{String.valueOf(Constants.R2_PORT),methodName,data});			
							}
							
							status = leaderActions(methodName, data);
						}
						// wait for request which are coming from Leader
						else {
							status = replicaActions(methodName, data);
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
		String R1Response = "", R2Response = "", R3Response = "";
		
		String ClientIPAddress  = data.split("\\|")[0];
		
		// 1. Send the message to Server via UDP
		R1Response = UDPServerTunnel(ClientIPAddress, methodName+"Leader", data);
		
		// 2. Receive Messages from Other Replicas via UDP FIFO & take the majority and send the result to client
		String Responses = UDPFIFO();
		R2Response = Responses.split("\\|")[1];
		R3Response = Responses.split("\\|")[2];
		
		// creating Wrong Output		
		if(isWrongCounter < 3) {

			// Produce Wrong Output
			R3Response = "Something went wrong with the server !";
			isWrongCounter++;
		}
		
		if(Constants.DEBUG) {
			// Printing Results
			System.out.println();
			System.out.println("R1Response: "+R1Response);
			System.out.println("R2Response: "+R2Response);
			System.out.println("R3Response: "+R3Response);
			System.out.println();
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
			response      =  R2Response;
			
		}
		// R1 != R2 != R3 AND (R1 == R3) -> T|F|T
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
			byte[] recivedMessage = new byte[Constants.MAX_PACKET_SIZE];

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
			byte[] recivedMessage = new byte[Constants.MAX_PACKET_SIZE];

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