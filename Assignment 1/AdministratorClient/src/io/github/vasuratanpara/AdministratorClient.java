package io.github.vasuratanpara;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class AdministratorClient {
    
	
	static boolean status = true;
    static int PASSWORD_LENGTH = 6;
    static int USERNAME_MIN_LENGTH = 6;
    static int USERNAME_MAX_LENGTH = 15;
    static String[] validIPs = {"132","93","183"};
    static Scanner input = new Scanner(System.in);
    
    public AdministratorClient()
    {

    }
    
 // Return menu.
 	public static void showMenu()
 	{
 		System.out.println("\n****Welcome Admin****\n");
 		System.out.println("Please select an option (1-2)");
 		System.out.println("1. Get Players Status");
        System.out.println("2. Exit");
         
     }

     //Return basic menu.
 	public void selectMenu(int choice, AdministratorClient admin )
 	{
 		switch (choice) {
             case 1:
                 admin.getStatus();
                 break;
             case 2:
             	System.out.println("\nGood Bye.");
             	AdministratorClient.status = false;
                 System.exit(0);
                 break;
             default:
                 System.out.println("\nInvalid Choice.");
                 break;
         }
         
     }
 	
 	 private void getStatus() {
 		 
 		String username = inputUsername();
 		String password = inputPassword();
 		String ipaddress = inputIPAddress();
 		
 		 if(getPlayerStatus(username, password, ipaddress)){
 			 System.out.println("\nNA: 6 online, 1 offline. EU: 7 online, 1 offline, AS: 8 online, 1 offline.");
 		 }else {
 			 System.out.println("Wrong Password or Username");
 		 }
		
	}

	public static int inputChoice() {
    	
 		int choice = 0;
    	boolean inputError = true;		
		
    	do {
            try {
            	System.out.print("\nSelect >>> ");
            	choice = Integer.parseInt(input.nextLine());
            	inputError = false;
            } 
            catch (Exception e) {
                System.out.println("\nPlease input Integer only");
            }
        }while(inputError);

		return choice;
    }

 	// When an administrator invokes this method from his/her geo-location through a
    // client program called AdministratorClient, the server associated with this
    // administrator (determined by the IPAddress) attempts to, if the credentials are
    // valid, concurrently find out the number of online players and offline players in the
    // other geo-locations using UDP/IP sockets and returns the result to the
    // administrator. Please note that it only returns the player counts (a number) and not
    // the player information. For example, if NA has 6 players online, EU has 7 players
    // online and AS has 8 players online and they each have 1 player offline it should
    // return the following (which should also be stored in the server and administrator
    // log): NA: 6 online, 1 offline. EU: 7 online, 1 offline, AS: 8 online, 1 offline.
    // Thus, this application has a number of GameServers (one per geo-location) each
    // implementing the above operations for that geo-location, PlayersClient (one per
    // player) invoking the player's operations at the associated GameServer as
    // necessary and AdministratorsClient (one per administrator) invoking the
    // administrator's operations at the associated GameServer. When a GameServer is
    // started, it registers its address and related/necessary information with a central
    // repository. For each operation, the PlayersClient/AdministratorClient finds the
    // required information about the associated GameServer from the central repository
    // and invokes the corresponding operation.
    
    
    // Output : NA: 6 online, 1 offline. EU: 7 online, 1 offline, AS: 8 online, 1 offline.
    // Also store in server and administrator log
    public static void main(String[] args) {
		
    	try {
            
        	AdministratorClient admin = new AdministratorClient();

            while(AdministratorClient.status){

            	AdministratorClient.showMenu();
            	int choice = inputChoice();
            	System.out.println(choice);
            	admin.selectMenu(choice,admin);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
	}
    
    public boolean getPlayerStatus (String AdminUsername, String AdminPassword, String IPAddress)
    {
        // Check The Admin UserName and Password
        if("Admin".equals(AdminUsername)  && "Admin".equals(AdminPassword)){
        	return true;
        }
        else {
        	return false;
        }
    }
    
    private String inputUsername() 
    { 
		System.out.print("\nEnter username: ");
		String username = input.nextLine();		
		return username;
	}
    
    private String inputPassword() {
      	
    	System.out.print("\nEnter password: ");
    	String password = input.nextLine();;
		return password;
    }
    
    private String inputIPAddress() {
		
    	String ipaddress;
    	
    	do{
    		System.out.print("\nEnter IP address: ");
    		ipaddress = input.nextLine();
    	
    	}while(isValidIPAddress(ipaddress));
    	
		return ipaddress;
	}
    
    private boolean isValidIPAddress(String ipaddress){
    
    	try {
    		
    		String ipv4Part = ipaddress.split("\\.")[0];
	    	
	    	// Convert String Array to List
	        List<String> list = Arrays.asList(validIPs);
	    	
	    	if (ipaddress.isEmpty()) { 
	    		System.out.print("\nIP address cannot be empty\n");
	        	return true;
	        }
	    	
	        if(!list.contains(ipv4Part)){
	        	System.out.println("\n1. 132.xxx.xxx.xxx : IP-addresses starting with 132 indicate a North-American geo-location.");
	        	System.out.println("2. 93.xxx.xxx.xxx  : IP-addresses starting with 93 indicate an European geo-location.");
	        	System.out.println("3. 182.xxx.xxx.xxx : IP-addresses starting with 182 indicate an Asian geo-location.");
	        	System.out.println("\nInvalid IP address");
	        	return true;
	        }
    	}catch(Exception e) {
    		System.out.println(e+"\nInvalid IP address");
        	return true;
    	}
    	
    	return false;
    }
}