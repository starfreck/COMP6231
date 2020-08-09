package Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.util.concurrent.ConcurrentHashMap;

public class UDPFIFO extends Thread {

	//ConcurrentQueue
	// {sender,receiver,data,action}
	ConcurrentLinkedQueue<HashMap<Integer,String[]>> clq = new ConcurrentLinkedQueue<HashMap<Integer,String[]>>(); 
 
    public void run() {
        
    	System.out.println("UDP FIFO Server started on " + 1234);
	       
        try {
        	
            DatagramSocket socket 			= new DatagramSocket(1234);
            MulticastSocket multicastSocket = new MulticastSocket();
            InetAddress group 				= InetAddress.getLocalHost();
            DatagramPacket requestData;
			DatagramPacket responseData;
			
            byte[] sendMessage    = new byte[Ports.MAX_PACKET_SIZE];
            byte[] receiveMessage = new byte[Ports.MAX_PACKET_SIZE];
            
            int i = 0;
           
            while(true) {
               
                requestData = new DatagramPacket(receiveMessage, receiveMessage.length);
                socket.receive(requestData);
                
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(receiveMessage));
                
                // Request
                input = (List) in.readObject();
                input.add(new String(""+i));
                System.out.println("request" + input.toString());
                
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
                ObjectOutputStream out = new ObjectOutputStream(baos);
               
                out.writeObject(input);
               
                sendMessage = baos.toByteArray();
                
                DatagramPacket p_out = new DatagramPacket(sendMessage, sendMessage.length, group, 3456);
                multicastSocket.send(p_out);
                
                i++;
               
            }
            
        } catch (SocketException e) {
            e.printStackTrace();
        }
         catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
}
