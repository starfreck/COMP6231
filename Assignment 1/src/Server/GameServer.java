package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameServer extends Remote{

	String createPlayerAccount(String FirstName,String LastName,int Age,String Username,String Password,String IPAddress) throws RemoteException;
	
	String playerSignIn (String Username, String Password, String IPAddress) throws RemoteException;
	
	String playerSignOut (String Username, String IPAddress) throws RemoteException;
	
	String getPlayerStatus (String AdminUsername,String  AdminPassword,String IPAddress) throws RemoteException;
	
}
