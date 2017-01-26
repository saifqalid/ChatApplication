package MultiClient;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class MultiEchoServer{
	private static ServerSocket serverSocket;
	private static  int PORT;
	public static HashMap hmCurrentClients = new HashMap();    
	public static ArrayList<Socket> ConnectionArray = new ArrayList<Socket>();   
	public static Scanner scanName;	
	public static String  userName; 
	public static Socket client = null;
	public static ClientHandler handler = null;

	public static void main(String[] args) throws IOException{

		try{
			getData();
			// server socket, to accept connection from client
			serverSocket = new ServerSocket(PORT);
			do{
				acceptConnection();
			}while (true);
		}
		catch (IOException ioEx){
			System.out.println("\nUnable to set up port!"+ioEx.getMessage());
			System.exit(1); 
		}
	}
	public static void getData()throws IOException{
		DataInputStream dis=new DataInputStream(System.in);
		System.out.println("Please enter port number:\n");
		PORT=(Integer.parseInt(dis.readLine()));
	}
	public static void acceptConnection() throws IOException {
		//Wait for client...
		client = serverSocket.accept();
		System.out.println("\nNew client accepted.\n");
		ConnectionArray.add(client);            
		System.out.println("Client Connected from :"+client.getLocalAddress().getHostName());
		scanName = new Scanner(client.getInputStream());
		userName = scanName.nextLine();
		System.out.println("Username is "+userName);            
		hmCurrentClients.put(userName, client);
		System.out.println(hmCurrentClients);            
		handler = new ClientHandler(client);
		handler.start();//As usual, method calls run.
	}

	public static void CheckConnection(Socket sock) throws IOException{
		System.out.println("Checking connection");
		int connectionArraySize = (MultiEchoServer.ConnectionArray!=null?MultiEchoServer.ConnectionArray.size():0); 
		if(sock != null && !sock.isConnected()){	
			for(int i = 1; i<= MultiEchoServer.ConnectionArray.size(); i++){	
				if( MultiEchoServer.ConnectionArray.get(i) == sock) {
					System.out.println("Removing one socket at index" +i);
					MultiEchoServer.ConnectionArray.remove(i);
				}
			}
		}
	}     
}


class ClientHandler extends Thread{
	private Socket client;
	private Scanner input;
	public String received = null;
	public String words[];
	public String sender[];
	public Socket recepient = null;
	public Socket temp_sock = null;
	public PrintWriter temp_out = null;
	int connectionArraySize = 0;

	public ClientHandler(Socket socket){
		//Set up reference to associated socket...
		client = socket;
		try{           
			input = new Scanner(client.getInputStream());            
		}
		catch(IOException ioEx){
			ioEx.printStackTrace();
		}
	}

	public void run(){    

		do{
			try{
				//Accept message from client on
				//the socket's input stream
				connectionArraySize = (MultiEchoServer.ConnectionArray!=null?MultiEchoServer.ConnectionArray.size():0);

				if(input.hasNextLine()) {        
					received    = input.nextLine(); 
					System.out.println("recieved output !!"+received);

					if(received!=null){
						words   = received.split(" ");
						sender  = received.split("]");
						sender[0] = sender[0].substring(1);
						System.out.println("sender[0]--->:"+sender[0]);
					}
					else
						words   = null;
					if(words!=null && words.length>1 && words[0]!=null && words[0].toLowerCase().contains("whisper")){
						whisper();
					}
					else if(words[0].toUpperCase().contains("QUIT")){
						disconnectConnection();
					}
					else{
						System.out.println("Broadcasting message to"+MultiEchoServer.ConnectionArray.size()+" users\n");
						broadcastMessage();
					}
				}
			}
			catch (Exception E){
				System.out.println(E.getMessage());            
			}
			finally{
				received    = null;
				words       = null;
				recepient   = null;
				temp_sock   = null;
				temp_out    = null;
			}
			//Repeat above until 'QUIT' sent by client...
		}while (true); 
	}

	public void disconnectConnection(){
		Socket client;
		System.out.println("In disconnectConnection method");
		client = (Socket) MultiEchoServer.hmCurrentClients.get(words[1]);
		if(client != null && !client.isConnected()){	
			for(int i = 1; i<= MultiEchoServer.ConnectionArray.size(); i++){	
				if( MultiEchoServer.ConnectionArray.get(i) == client) {
					System.out.println("Removing one socket at index" +i);
					MultiEchoServer.ConnectionArray.remove(i);
				}
			}
		}
	}

	public void whisper()throws IOException{
		recepient = (Socket) MultiEchoServer.hmCurrentClients.get(words[1]);
		for(int i=0;i<MultiEchoServer.ConnectionArray.size();i++){   
			temp_sock = (Socket) MultiEchoServer.ConnectionArray.get(i); 
			MultiEchoServer.CheckConnection(temp_sock);
			if(temp_sock!=null && temp_sock.equals(recepient)){
				temp_out = new PrintWriter(temp_sock.getOutputStream(),true);            
				temp_out.println("whisper Message: " + received.substring(received.indexOf(words[1])+words[1].length()));            
				temp_out.flush();
				System.out.println("Sent to:"+temp_sock.getLocalAddress().getHostName());
			}
		}
	}
	public void broadcastMessage()throws IOException{
		for(int i=0;i<MultiEchoServer.ConnectionArray.size();i++){
			temp_sock = (Socket) MultiEchoServer.ConnectionArray.get(i); 
			if(sender!=null && sender.length>0 && sender[0]!=null && 
					(((Socket) MultiEchoServer.hmCurrentClients.get(sender[0]))).equals(temp_sock)){
				continue;
			}
			System.out.println("Broadcasting to :"+temp_sock+"\n");
			MultiEchoServer.CheckConnection(temp_sock);
			temp_out = new PrintWriter(temp_sock.getOutputStream(),true);
			temp_out.println(received);
			temp_out.flush();
			System.out.println("Sent to:"+temp_sock.getLocalAddress().getHostName());
		}
	}
} 