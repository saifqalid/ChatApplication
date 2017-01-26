/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MultiClient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 
 * @author Qalid
 */


public class MultiEchoClientMod implements Runnable{
	public static Socket socket ;
	public static String userName;
	Scanner input;
	PrintWriter out;
	public static String IPAddress;
	public static String message = null;
	public static    int PORT;
	public static  MultiEchoClientMod multiEchoClientMod = null;
	public static    Thread thread = null;
	public static    PrintWriter networkOutput = null;
	public static    Scanner userEntry = null;

	public MultiEchoClientMod(Socket X){	
		this.socket = X;
	}

	@Override
	public void run(){

		try{
			try{
				input = new Scanner(socket.getInputStream());	
				CheckStream();
			}
			finally{	
				socket.close();
			}

		}
		catch(Exception X){
			System.out.println(X);
		}
	}

	public void CheckStream(){	
		while(true){	
			receive();
		}
	}


	public void receive(){
		if(input.hasNext()){
			String message = input.nextLine();
			System.out.println(message);
		}
	}


	public static void disconnect() throws IOException{
		System.out.println("Closing the connection");
		socket.close();
		System.exit(0);
	}   
	public static void main(String[] args) throws Exception{

		try{
			getData();
			establishConnection();
			System.out.println("Enter the username:");
			userName = userEntry.nextLine();
			networkOutput.println(userName);
			send();     
		}
		catch(Exception IO){
			IO.printStackTrace();
		}
		finally{
			message = null;
		}
	}
	public static void getData()throws IOException{
		DataInputStream dis=new DataInputStream(System.in);
		System.out.println("Please enter IP Address and port number:\n"); 
		IPAddress = dis.readLine();
		PORT=(Integer.parseInt(dis.readLine()));
	}
	public static void establishConnection() throws IOException{

		MultiEchoClientMod.socket = new Socket(IPAddress,PORT);
		MultiEchoClientMod m1 = new MultiEchoClientMod(MultiEchoClientMod.socket);
		Thread X = new Thread(m1);
		X.start();
		networkOutput = new PrintWriter(socket.getOutputStream(),true);
		userEntry = new Scanner(System.in);
	}
	public static void send() throws IOException{
		do{
			System.out.println( "Enter message ('QUIT' to exit): ");
			message = userEntry.nextLine();
			networkOutput.println("["+userName+"]"+message);
			if(message.equals("QUIT")){
				disconnect();
			}
		}while(true); 
	}

}