package asianServerServices;


public class ASTest {

	public static void main(String[] args) {
		
		AsianServerImplService asia = new AsianServerImplService();
		GameServer asianServer = asia.getAsianServerImplPort();
		System.out.println(asianServer.suspendAccount("Admin", "Admin", "182.56.3.4", "Kevin123"));

	}

}
