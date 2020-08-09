package FrontEndApp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import Utilities.FileLogger;
import Utilities.Constants;

public class FrontEndImpl extends FrontEndPOA{
	
	private ORB orb;
	//
	static int LE_PORT;
	// CORBA Var
	static FrontEndImpl FrontEndObj;
	// Registry URL
	static final String registryURL = "FrontEnd";
	// Name
	static final String name = "FrontEnd";
	// Logger
	FileLogger logger = new FileLogger(loggerPath,"FrontEnd.log");;
	// Logger Path
	static final String loggerPath = "./logs/FrontEnd/";
	
	// Orb Port
	static final String ORB_PORT = "1050";
	// Max Packet Size
	static final int MAX_PACKET_SIZE = 1024;
	static HashMap<String, String> gameServers = new HashMap<String, String>();
	
	protected FrontEndImpl() {
		super(); 
		
		// Initialize Servers List
		gameServers.put("132", "NorthAmerica");
		gameServers.put("93", "Europe");
		gameServers.put("182", "Asia");
	}

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	
	@Override
	public synchronized String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password,
			String IPAddress) {
		
		this.logger.write(">>> Client >>> Frontend >>> createPlayerAccount");
		
		// createPlayerAccount
		String methodName = "createPlayerAccount";
		// IPAddress|FirstName|LastName|Age|Username|Password
		String Data = IPAddress+"|"+FirstName+"|"+LastName+"|"+String.valueOf(Age)+"|"+Username+"|"+Password;
		String response = sendMessageToLeader(methodName,Data);
		
		this.logger.write(">>> Client >>> Frontend >>> createPlayerAccount >>> Response from Leader >>> "+response);
		return response;
	}

	@Override
	public synchronized String playerSignIn(String Username, String Password, String IPAddress) {
		
		this.logger.write(">>> Client >>> Frontend >>> playerSignIn");

		// playerSignIn
		String methodName = "playerSignIn";
		// IPAddress|Username|Password
		String Data = IPAddress+"|"+Username+"|"+Password;
		
		String response = sendMessageToLeader(methodName,Data);
		
		this.logger.write(">>> Client >>> Frontend >>> playerSignIn >>> Response from Leader >>> "+response);
		return response;
	}

	@Override
	public synchronized String playerSignOut(String Username, String IPAddress) {
		
		this.logger.write(">>> Client >>> Frontend >>> playerSignOut");
		
		// playerSignOut
		String methodName = "playerSignOut";
		// IPAddress|Username
		String Data = IPAddress+"|"+Username;
		
		String response = sendMessageToLeader(methodName,Data);
		
		this.logger.write(">>> Client >>> Frontend >>> playerSignOut >>> Response from Leader >>> "+response);
		return response;
	}

	@Override
	public synchronized String transferAccount(String Username, String Password, String OldIPAddress, String NewIPAddress) {
		
		this.logger.write(">>> Client >>> Frontend >>> transferAccount");
		
		// transferAccount
		String methodName = "transferAccount";
		// OldIPAddress|Username|Password|NewIPAddress
		String Data = OldIPAddress+"|"+Username+"|"+Password+"|"+NewIPAddress;
		
		String response = sendMessageToLeader(methodName,Data);
		
		this.logger.write(">>> Client >>> Frontend >>> transferAccount >>> Response from Leader >>> "+response);
		return response;
	}

	@Override
	public synchronized String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress) {
		
		this.logger.write(">>> Client >>> Frontend >>> getPlayerStatus");
		
		// getPlayerStatus
		String methodName = "getPlayerStatus";
		// IPAddress|AdminUsername|AdminPassword
		String Data = IPAddress+"|"+AdminUsername+"|"+AdminPassword;
		
		String response = sendMessageToLeader(methodName,Data);
		
		this.logger.write(">>> Client >>> Frontend >>> getPlayerStatus >>> Response from Leader >>> "+response);
		return response;
	}

	@Override
	public synchronized String suspendAccount(String AdminUsername, String AdminPassword, String AdminIPAddress, String UsernameToSuspend) {
		
		this.logger.write(">>> Client >>> Frontend >>> suspendAccount");
		// suspendAccount
		String methodName = "suspendAccount";
		// AdminIPAddress|AdminUsername|AdminPassword|UsernameToSuspend
		String Data = AdminIPAddress+"|"+AdminUsername+"|"+AdminPassword+"|"+UsernameToSuspend;
		
		String response = sendMessageToLeader(methodName,Data);
		
		this.logger.write(">>> Client >>> Frontend >>> suspendAccount >>> Response from Leader >>> "+response);
		return response;
	}

	@Override
	public void shutdown() {
		orb.shutdown(false);
	}
	
	public static void main(String[] args) throws IOException {

		
		FrontEndImpl.LE_PORT = Integer.parseInt(getConfig()[4]);
		
		ORB orb = ORB.init(getConfig(), null);
		
		// Run CORBA FrontEnd Server
		Thread threadOne = new Thread(new Runnable() {
			public void run() {
				CORBAServer(orb);
			}
		});

		threadOne.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void CORBAServer(ORB orb) {

		try {

			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			FrontEndObj = new FrontEndImpl();
			FrontEndObj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(FrontEndObj);
			FrontEnd href = FrontEndHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name(registryURL);

			ncRef.rebind(path, href);

			System.out.println(name + " is ready and waiting ...");

			orb.run();
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println(name + " is Exiting ...");
	}

	static String[] getConfig() throws IOException {

		String[] orbarg = new String[5];

		// Creating args array for ORB.init()
		orbarg[0] = "-ORBInitialPort";
		orbarg[1] = ORB_PORT;
		orbarg[2] = "-ORBInitialHost";
		orbarg[3] = "localhost";
		orbarg[4] = String.valueOf(Constants.R1_PORT);
		return orbarg;
	}
	
	
	private String sendMessageToLeader(String methodName, String Data) {

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
			requestData = new DatagramPacket(sendMessage, sendMessage.length, host, LE_PORT);
			this.logger.write(">>> Client >>> Frontend >>> sendMessageToLeader >>> Sending a UDP request to Leader");
			socket.send(requestData);

			// Response Data
			responseData = new DatagramPacket(recivedMessage, recivedMessage.length);
			this.logger.write(">>> Client >>> Frontend >>> sendMessageToLeader >>> Receiving a UDP request to Leader");
			socket.receive(responseData);

			// Retrieving Data
			response = new String(responseData.getData(), responseData.getOffset(), responseData.getLength());

			socket.close();

		} catch (Exception e) {
			this.logger.write(">>> Client >>> Frontend >>> sendMessageToLeader >>> Exception >>> "+e);
			System.err.println(e);
		}

		return response;
	}
}