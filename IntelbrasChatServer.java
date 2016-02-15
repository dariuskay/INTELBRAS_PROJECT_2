import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class IntelbrasChatServer {
	// Each connection has a unique id. 
	private static int uniqueId;
	// A list of all the clients who are connected to the server.
	private ArrayList<ClientThread> al;
	// The sever GUI.
	private IntelbrasChatServerGUI sg;
	// To display the time and date of messages and connections.
	private SimpleDateFormat sdf;
	// Port number.
	private int port;
	// Control of the server.
	private boolean keepGoing;

	public HashMap<String, String> users;
	

	public IntelbrasChatServer(int port) {
		this(port, null);
	}

	// Server instructor needs a port number.	
	public IntelbrasChatServer(int port, IntelbrasChatServerGUI sg) {
		// GUI
		this.sg = sg;
		this.port = port;
		// Time
		sdf = new SimpleDateFormat("HH:mm:ss");
		// Client list
		al = new ArrayList<ClientThread>();
		// User to password Map
		users = new HashMap<String, String>();
	}
	
	public void start() {
		keepGoing = true;
		// Create socket, listen for requests
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port);

			// Wait for connection
			while(keepGoing) 
			{
				display("Waiting for Clients on  " + port + ".");
				Socket socket = serverSocket.accept();
				if(!keepGoing)
					break;
				try {
					// Try to make a Client of the connection
					ClientThread t = new ClientThread(socket);
					al.add(t);
					t.run();
				} catch (Exception ioE) {
					System.out.println("Username incorrect. Try again.");
				}
			}
			// If stopped...
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
						// Close everything
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = sdf.format(System.currentTimeMillis()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		

	// To stop the server
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}

	// Shows up on Server GUI
	private void display(String msg) {
		String time = sdf.format(System.currentTimeMillis()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	
	// Broadcast to all clients
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(System.currentTimeMillis());
		String messageLf = time + " " + message + "\n";
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);
		
		// Loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// Try to send to Client
			if(!ct.writeMsg(messageLf)) {
				// If not, remove it from the current users list
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	// Logoff using the LOGOUT message
	synchronized void remove(int id) {
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		// Automatic port number 1500 
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					// Custom server number
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		IntelbrasChatServer server = new IntelbrasChatServer(portNumber);
		server.start();
	}

	// Thred for each Client
	class ClientThread extends Thread {
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		String username;
		String password;
		ChatMessage cm;
		String date;
		boolean passwordCorrect;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			passwordCorrect = true;
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());

				String thisusername = (String) sInput.readObject();
				String thispassword = (String) sInput.readObject();
				System.out.println("This is what I think the username is: " + thisusername);
				System.out.println("This is what I think the password is: " + thispassword);
				// If the user is new...
				if (!users.containsKey(thisusername)) {
					// Add them to the map.
					addUser(thisusername, thispassword);

				} else if (!users.get(thisusername).equals(thispassword)) {
						String establishedPassword = users.get(thisusername);
						System.out.println("Here is the password for " + thisusername + ": " + establishedPassword);
						// If the user isn't new, and the password is incorrect...
						if (!(establishedPassword.equals(thispassword))) {
							System.out.println("Incorrect password flag 1");
							display("password " + thispassword + " is incorrect for username " + thisusername + ".");
							sOutput.writeObject("Password incorrect");
							passwordCorrect = false;
							// Close the thread, because it shouldn't be a client.
							close();
							return;
						}
				}
				username = thisusername;
				password = thispassword;
				display(username + " just tried to connect.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = String.valueOf(System.currentTimeMillis()) + "\n";
		}

		// Runs forever, for each client, listening for messages.
		public void run() {
			boolean keepGoing = true;
			while(keepGoing && passwordCorrect) {
				try {
					cm = (ChatMessage) sInput.readObject();
					System.out.println("Here is the message from what I'm reading: " + cm.getMessage());
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// ChatMessage contains a message
				String message = cm.getMessage();

				System.out.println("This is how my type is represented: " + cm.getType());

				// Depending on what kind of ChatMessage it is...
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + sdf.format(System.currentTimeMillis()) + "\n");
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			remove(id);
			close();
		}

		private void addUser(String adduser, String addpword) {
			users.put(adduser, addpword);
		}
		
		private void close() {
			// Close everything
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		private boolean writeMsg(String msg) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// Write the message and send it.
			try {
				sOutput.writeObject(msg);
			}
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}
