import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class IntelbrasChatServerGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;
	private JButton stopStart;
	private JTextArea chat, event;
	private JTextField tPortNumber;
	private IntelbrasChatServer server;
	
	
	// GUI server uses port number
	IntelbrasChatServerGUI(int port) {
		super("Chat Server");
		server = null;
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		add(north, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new GridLayout(2,1));
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("Chat room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));	
		add(center);
		
		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}		

	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		// Stop the server for a minute
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopStart.setText("Start");
			return;
		}
      	// Restart the server
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Invalid port number");
			return;
		}
		// New server
		server = new IntelbrasChatServer(port, this);
		new ServerRunning().start();
		stopStart.setText("Stop");
		tPortNumber.setEditable(false);
	}
	
	public static void main(String[] arg) {
		// Default port is 1500
		new IntelbrasChatServerGUI(1500);
	}

	public void windowClosing(WindowEvent e) {
		// Stop the server
		if(server != null) {
			try {
				server.stop();
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		// Close
		dispose();
		System.exit(0);
	}
	// Irrelevant methods for the WindowEvent
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}

	class ServerRunning extends Thread {
		public void run() {
			server.start();
			// If the server stops...
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed\n");
			server = null;
		}
	}

}
