import java.net.*;
import java.io.*;
import java.util.*;

public class IntelbrasChatClient  {


	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;
	private IntelbrasChatClientGUI cg;
	
	private String server, username, password;
	private int port;
	private IntelbrasChatServer myserver;

	IntelbrasChatClient(String server, int port, String username, String password) {
		this(server, port, username, password, null);
	}

	IntelbrasChatClient(String server, int port, String username, String password, IntelbrasChatClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg;
		this.password = password;
	}
	

	public boolean start() {
		// Connect to server
		try {
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		// Data stream with server
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			new FromServer().start();
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// Send server username and password to see if they're correct
		try
		{
			sOutput.writeObject(username);
			sOutput.writeObject(password);
			System.out.println("Sending username: " + username + " and password: " + password);
		} 
		catch (Exception eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	// Displays content in ChatRoom field
	private void display(String msg) {
		if(cg == null)
			System.out.println(msg);
		else
			cg.append(msg + "\n");
	}
	
	// Send a ChatMessage to the server (an action after username and password)
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
			System.out.println("sendMessaging " + msg.getMessage());
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	protected void disconnect() {
		try { 
			if(sInput != null) 
				System.out.println("Here's another flag for the disconnect: 2");
				sInput.close();
		}
		catch(Exception e) {}
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}

		System.out.println("Here's another flag for the disconnect: 3");
		
		if(cg != null)
			cg.connectionFailed();
	}

	public static void main(String[] args) {
		// default values
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "";
		String pword = "";

		// Argument switch
		switch(args.length) {
			// > javac Client username password portNumber serverAddr
			case 4:
				serverAddress = args[3];
			// > javac Client username password portNumber
			case 3:
				try {
					portNumber = Integer.parseInt(args[2]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [password] [portNumber] [serverAddress]");
					return;
				}
			// > javac Client username password
			case 2: 
				userName = args[0];
				pword = args[1];
				break;
			// invalid number of arguments
			default:
				System.out.println("Usage is: > java Client [username] [password] [portNumber] [serverAddress]");
				return;
			}
		// create the Client object
		IntelbrasChatClient client = new IntelbrasChatClient(serverAddress, portNumber, userName, pword);
		if(!client.start()) {
			client.disconnect();
		}
		
		Scanner scan = new Scanner(System.in);
		while(true) {
			System.out.print("> ");
			String msg = scan.nextLine();
			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				break;
			}
			// message WhoIsIn
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			}
			else {				
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		System.out.println("Here's a flag for the disconnect: 1");
		client.disconnect();	
	}

	// Get messages from server through I/O
	class FromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					if (msg.equals("Password incorrect")) {
						display("The password was incorrect. Please try again.");
						System.out.println("The password was incorrect. Please try again.");
						disconnect();
						break;
					}
					else {
						cg.append(msg);
					}
				}
				catch(IOException e) {
					display("Server has to close the connection: " + e);
					if(cg != null) 
						cg.connectionFailed();
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
