package server;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface GameServer {

	@WebMethod
	String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password, String IPAddress);
	@WebMethod
	String playerSignIn(String Username, String Password, String IPAddress);
	@WebMethod
	String playerSignOut(String Username, String IPAddress);
	@WebMethod
	String transferAccount(String Username, String Password, String OldIPAddress, String NewIPAddress);
	@WebMethod
	String getPlayerStatus(String AdminUsername, String AdminPassword, String AdminIPAddress);
	@WebMethod
	String suspendAccount(String AdminUsername, String AdminPassword, String AdminIPAddress, String UsernameToSuspend);

}
