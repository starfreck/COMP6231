package Server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NorthAmericanServer {
	
	static final int REGISTRY_PORT = 1099;
	static final int UDP_PORT = 9878;
	static final int MAX_PACKET_SIZE = 1024;
	static final String serverName  = "NorthAmerica";
	static final String registryURL = "NorthAmerica";
	
	public static void main(String[] args) {
		
		
		try {
			
            startRegistry(NorthAmericanServer.REGISTRY_PORT);
            NorthAmericanServerImpl NAServer = new NorthAmericanServerImpl();

            
            Naming.rebind(NorthAmericanServer.registryURL, NAServer);
            System.out.println("NorthAmerican Server is started...");
            
            
            // Communicate Between Servers with UDP
            
            

        } catch (Exception e) {
        	System.out.println("Exception" + e);
        }
	}
	
	private static void startRegistry(int port)throws RemoteException {
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
