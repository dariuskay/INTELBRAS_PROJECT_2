import java.io.*;

public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// WHOISIN is everyone chatting on the server
	// MESSAGE is a message
	// LOGOUT to log off the server
	// INCORRECT if the password for a user is incorrect (later revised)
	static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2; 
	private int type;
	private String message;
	
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	int getType() {
		return type;
	}
	String getMessage() {
		return message;
	}
}
