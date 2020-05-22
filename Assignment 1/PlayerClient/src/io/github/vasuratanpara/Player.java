package io.github.vasuratanpara;

public class Player {
	
	String firstname;
	String lastname;
	int age;
	String username;
	String password;
	String ipaddress;
	
	public Player(String firstname,String lastname,int age,String username,String password,String ipaddress){
		
		this.firstname =firstname;
		this.lastname=lastname;
		this.age=age;
		this.username=username;
		this.password=password;
		this.ipaddress=ipaddress;
	}
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
}
