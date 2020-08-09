package Utilities;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPFIFO extends Thread{

	//ConcurrentQueue
	// {receiver,data,action}
	static ConcurrentLinkedQueue<String[]> queue = new ConcurrentLinkedQueue<String[]>();
	// UDP Variables
	DatagramSocket socket;
	DatagramPacket requestData;
	DatagramPacket responseData;
	
    
    public static void  add(String receiver, String action, String data) {
    	
    	String[] request = { receiver, action, data };
    	queue.add(request);
    }
    
    public void run(){
    	
        System.out.println("UDP FIFO queue is running...");
     		
     		try {

     			// Socket
     			socket = new DatagramSocket();

     			while (true) {
     				
     				// if queue has a request then process the request
     				if(!queue.isEmpty()) {
     					
     					String[] request = queue.remove();
     					
     					int UDP_PORT      = Integer.parseInt(request[0]);
     					String methodName = request[1];
     					String data 	  = request[2];
     					
     					String requestString = methodName+":"+data;
	     				
	     				byte[] sendData = requestString.getBytes();
	     	
	     				// Request Data
	     				requestData = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), UDP_PORT);
	     				
	     				socket.send(requestData);
     				}
     			}
     			
     		} catch (SocketException e) {
     			//System.out.println("Socket: " + e.getMessage());
     		} catch (IOException e) {
     			System.out.println("IO: " + e.getMessage());
     		}
        
    }
}
