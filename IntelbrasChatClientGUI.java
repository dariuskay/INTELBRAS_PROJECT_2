import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class IntelbrasChatClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel label;
	// For username and text
	private JTextField tf;
	private JLabel label2;
	// For password, then it disappears
	private JTextField tf2;
	// For server and port number
	private JTextField tfServer, tfPort;
	// Buttons
	private JButton login, logout, whoIsIn;
	// Chat area
	private JTextArea ta;
	private boolean connected;
	private IntelbrasChatClient client;
	private int defaultPort;
	private String defaultHost;

	IntelbrasChatClientGUI(String host, int port) {

		super("Chat Client");
		defaultPort = port;
		defaultHost = host;
		
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		northPanel.add(serverAndPort);

		label = new JLabel("Enter your username here", SwingConstants.LEFT);
		northPanel.add(label);
		tf = new JTextField("");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);
		label2 = new JLabel("Enter your password here", SwingConstants.LEFT);
		northPanel.add(label2);
		tf2 = new JTextField("");
		tf2.setBackground(Color.WHITE);
		northPanel.add(tf2);
		add(northPanel, BorderLayout.NORTH);

		// Chat room
		ta = new JTextArea("Welcome to IntelbrasChat!\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	// Use TextArea
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}

	// Reset if connection fails
	void connectionFailed() {
		System.out.println("Here's another flag for the disconnect: 4");
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		label.setText("Enter your username below");
		tf2 = new JTextField("");
		tf2.setBackground(Color.WHITE);
		tf2.setEditable(true);
		label2.setVisible(true);
		tf.setEnabled(true);
		label.setVisible(true);
		login.setEnabled(true);
		logout.setEnabled(true);
		connected = false;
		System.out.println("Here's another flag for the disconnect: 5");
	}


	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// Logout
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			tf2.setVisible(true);
			label2.setVisible(true);
			tf.setEnabled(true);
			label.setText("Enter your username below");
			label.setVisible(true);
			login.setEnabled(true);
			logout.setEnabled(true);
			connected = false;
			return;
		}
		// Who is in
		if(o == whoIsIn) {
			client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			return;
		}

		// Text field
		if(connected) {
			// Send message
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));				
			tf.setText("");
			return;
		}
		

		if(o == login) {
			// Login
			String username = tf.getText().trim();
			String password = tf2.getText().trim();
			if(username.length() == 0) {
				System.out.println("No username. Try again.");
				return;
			}
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			// empty or invalid port numer, ignore it
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;
			}

			client = new IntelbrasChatClient(server, port, username, password, this);
			// If it fails...
			if(!client.start()) {
				label.setText("Username / password incorrect. Try again, with your username below.");
				tf.setText("");
				tf2.setText("");
				connected = false;
				return;
			}
			// Success. Set up chat.
			tf.setText("");
			label.setText("Enter your message below");
			tf2.setVisible(false);
			label2.setVisible(false);
			connected = true;
			
			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			// disable the Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener for when the user enter a message
			tf.addActionListener(this);
		}

	}

	public static void main(String[] args) {
		new IntelbrasChatClientGUI("localhost", 1500);
	}

}

