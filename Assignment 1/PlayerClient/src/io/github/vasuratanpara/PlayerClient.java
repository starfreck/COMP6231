package io.github.vasuratanpara;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PlayerClient {
    
    static boolean status = true;
    static int PASSWORD_LENGTH = 6;
    static int USERNAME_MIN_LENGTH = 6;
    static int USERNAME_MAX_LENGTH = 15;
    static String[] validIPs = {"132","93","183"};
    static Scanner input = new Scanner(System.in);
    
    
    public PlayerClient()
    {

    }

    // Return menu.
	public static void showMenu()
	{
		System.out.println("\n****Welcome Client****\n");
		System.out.println("Please select an option (1-4)");
		System.out.println("1. Create an Account");
		System.out.println("2. SignIn");
		System.out.println("3. SignOut");
        System.out.println("4. Exit");
        
    }

    //Return basic menu.
	public void selectMenu(int choice, PlayerClient player )
	{
		switch (choice) {
            case 1:
                player.createAccount();
                break;
            case 2:
            	player.SignIn();
                break;
            case 3:
                player.SignOut();
                break;
            case 4:
            	System.out.println("\nGood Bye.");
            	PlayerClient.status = false;
                System.exit(0);
                break;
            default:
                System.out.println("\nInvalid Choice.");
                break;
        }
        
    }
    
    public static void main(String[] args) {
        
        try {
            
        	PlayerClient player = new PlayerClient();

            while(PlayerClient.status){

                PlayerClient.showMenu();
            	int choice = inputChoice();
            	System.out.println(choice);
            	player.selectMenu(choice,player);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void createAccount() {    	

    	try {
    		String firstname = inputFirstName();
    		String lastname  = inputLastName();
    		int age          = inputAge();
    		String username  = inputUsername();
    		String password  = inputPassword();
    		String ipaddress = inputIPAddress();
    		
    		if(this.createPlayerAccount(firstname,lastname,age,username,password,ipaddress)) {
    			System.out.println("\nAccount created successfully");
    		}else {
    			System.out.println("\nError while creating an account");
    		}
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	} 
    }
    
    public void SignIn() {

    	try {
    		String username  = inputUsername();
    		String password  = inputPassword();
    		String ipaddress = inputIPAddress();
    		
    		if(this.playerSignIn(username,password,ipaddress)) {
    			System.out.println("\nUser Sign in successfully");
    		}else {
    			System.out.println("\nError while signin an account");
    		}
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    
    public void SignOut() {

    	try {
    		String username  = inputUsername();
    		String ipaddress = inputIPAddress();
    		
    		if(this.playerSignOut(username,ipaddress)){
    			System.out.println("\nAccount Signed Out successfully");
    		}else {
    			System.out.println("\nError while Siging Out an account");
    		}
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }

	// Method : createPlayerAccount()
    
    // When a player invokes this method from his/her geo-location through a client
    // program called PlayerClient, the server associated with this player (determined by
    // the IPAddress) attempts to create an account with the information passed if the
    // username does not exist and that the passed information is valid according to the
    // problem description, and inserts the account at the appropriate location in the hash
    // table. The server returns information to the player whether the operation was
    // successful or not and both the server and the player store this information in their
    // logs.

	public boolean createPlayerAccount(String FirstName,String LastName,int Age,String Username,String Password,String IPAddress)
    {
    	//Player player = new Player(FirstName,LastName,Age,Username,Password,IPAddress);
    	return true;
    }

    // Method : playerSignIn()

    // When a player invokes this method from his/her geo-location through a client
    // program called PlayerClient, the server associated with this player (determined by
    // the IPAddress) attempts to verify if the account exists, that the password matches
    // the account password and that the account is not currently signed-in. If these
    // conditions are met, the server sets the account status to online and returns a
    // confirmation to the player. Otherwise a descriptive error is returned. Both the
    // server and the player store this information in their logs. (There are many ways
    // this can be done. You are encouraged to design your system in such a way that it
    // is simple to distinguish between online and offline accounts without impacting the
    // performance of the system.)

    public boolean playerSignIn (String Username, String Password, String IPAddress)
    {
    	return true;
    }

    // Method : playerSignOut()

    // When a player invokes this method from his/her geo-location through a client
    // program called PlayerClient, the server associated with this player (determined by
    // the IPAddress) attempts to verify if the account exists and that the account is
    // currently signed-in. If these conditions are met, the server sets the account status
    // to offline and returns a confirmation to the player. Otherwise a descriptive error is
    // returned. Both the server and the player store this information in their logs.
    public boolean playerSignOut (String Username, String IPAddress)
    {
    	return true;
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
    
    private String inputFirstName() {
  	
    	String firstname;
    	
    	do{
    		System.out.print("\nEnter first name: ");
    		firstname = input.nextLine();
    		
    		if(firstname.isEmpty()) System.out.print("\nFirst name cannot be empty\n");
    	
    	}while(firstname.isEmpty());
    	
		return firstname;
    }
    
    private String inputLastName() {
      	
    	String lastname;
    	
    	do{
    		System.out.print("\nEnter last name: ");
    		lastname = input.nextLine();
    		
    		if(lastname.isEmpty()) System.out.print("\nLast name cannot be empty\n");
    	
    	}while(lastname.isEmpty());
    	
		return lastname;
    }
    
    private int inputAge() {
      	
    	int age = 0;
    	boolean inputError = true;
		
		do {
            try {
            	System.out.print("\nEnter age: ");
                age = Integer.parseInt(input.nextLine());        
                inputError = false;
            } 
            catch (Exception e) {
                System.out.println("\nPlease input Integer only");
            }
        }while(inputError);
		
		return age;
    }
    
    private String inputUsername() 
    {
    	String username;
    	
    	do {
			System.out.print("\nEnter username: ");
    		username = input.nextLine();
		}while(isValidUsername(username));
		
		return username;
	}
    
    private String inputPassword() {
      	
    	String password;
    	
    	do {
			System.out.print("\nEnter password: ");
    		password = input.nextLine();
		}while(isValidPassword(password));
    	
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
    
    private boolean isValidPassword(String password) {

            if (password.length() < PASSWORD_LENGTH) {
            	
            	System.out.println("\nA password must have at least six characters");
            	return true;
            }
            return false;
    }
    
    private boolean isValidUsername(String username) {

		if (username.length() < USERNAME_MIN_LENGTH) {
        	
        	System.out.println("\nA username must have at least 6 characters");
        	return true;
        }
		if (username.length() > USERNAME_MAX_LENGTH) {
        	
        	System.out.println("\nA username can be maximum length of 15 characters");
        	return true;
        }
        return false;
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