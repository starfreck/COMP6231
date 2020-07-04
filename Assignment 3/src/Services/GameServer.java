package Services;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface GameServer {

	String createPlayerAccount(String FirstName, String LastName, int Age, String Username, String Password, String IPAddress);

	String playerSignIn(String Username, String Password, String IPAddress);

	String playerSignOut(String Username, String IPAddress);

	String transferAccount(String Username, String Password, String OldIPAddress, String NewIPAddress);

	String getPlayerStatus(String AdminUsername, String AdminPassword, String AdminIPAddress);

	String suspendAccount(String AdminUsername, String AdminPassword, String AdminIPAddress, String UsernameToSuspend);

}
