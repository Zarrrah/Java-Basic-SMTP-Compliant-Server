package com.grigg.server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;

import com.grigg.client.Client;
import com.grigg.common.ConfigConnection;
import com.grigg.common.ValidateData;

public class Server {
	public static int port = 50005;
	public static boolean isRunning = true;
	public static ServerSocket soc;
	public static ConfigConnection config;
	public static String ServerName = "SMTP.com";
	
	public static void main(String[] args) {
		try {
			config = new ConfigConnection();
			openConnection();
			
			while(isRunning) {
				//Accepts incoming connections
				Socket client = soc.accept();
				
				//Creates thread to handle incoming connections
				ConnectionHandler connection = new ConnectionHandler(client);
				Thread connectionThread = new Thread(connection);
				connectionThread.start();
			}
		}catch(Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	private static void openConnection(){
		try {
			//Opens the specified server port
			port = config.getPort();
			soc = new ServerSocket(port);
			System.out.println("Server online");
		}catch(Exception e) {
			System.out.println("Error : " + e.getMessage());
		}
	}
}

class ConnectionHandler implements Runnable{
	
	enum ConnectionState{
		NC,
		CE,
		HELO,
		MAIL,
		RCPT,
		DATA,
		CQ,
		ERR
	}
	
	private BufferedReader reader;
	private OutputStreamWriter writer;
	private ConnectionState state = ConnectionState.NC;
	private Mail currentMail;
	private boolean isRunning = true;
	
	//Constructor that initializes the required resources for the thread and responds to the client
	public ConnectionHandler(Socket soc) {
		try {
			reader = new BufferedReader(new InputStreamReader(soc.getInputStream(), "UTF-8"));
			writer = new OutputStreamWriter(soc.getOutputStream());
			state = ConnectionState.CE;
			//Allows time for the client to begin listening on the port
			Thread.sleep(100);
			respond(220);
		}catch(Exception e) {
			System.out.println("Error : " + e.getMessage());
			state = ConnectionState.ERR;
			respond(421);
		}
	}

	public void run() {
		try {
			while(isRunning) {
				if(reader.ready() == true) {
					//Converts the data into UTF-7
					String line = ValidateData.convertToAscii(reader.readLine());
					//Pass the input from the client into the main flow control method
					parseInput(line);
				}
				//Only reads for new data every 10th of a second to reduce the recourse demands of each connection
				Thread.sleep(100);
			}
		}catch(Exception e) {
			e.printStackTrace();
			state = ConnectionState.ERR;
			respond(451);
		}
	}
	
	private void parseInput(String s) {
		//Checks if the server is in an error state
		if(state == ConnectionState.ERR) {
			respond(421);
			state = ConnectionState.CQ;
		//Checks if the server is receiving message data
		}else if(state == ConnectionState.DATA){
			parseMessage(s);
		}else {
			//Splits the input into separate arguments
			String args[] = s.split(" ");
			String cmd = args[0].toUpperCase();
			switch(cmd) {
			case "HELO": 
				helo(args);
				break;
			case "MAIL": 
				mail(args);
				break;
			case "RCPT": 
				rcpt(args);
				break;
			case "DATA": 
				data(args);
				break;
			case "QUIT": 
				quit(args);
				break;
			case "ERROR":
				state = ConnectionState.ERR;
				break;
			default:
				//Command not recognized
				respond(500);
				break;
			}
		}
	}
	
	private void parseMessage(String s) {
		String localString = "";
		if(s.equals(".")) {
			//Ends data stream, saves the email, and resets the server for additional connections
			MailManager.saveMail();
			state = ConnectionState.HELO;
			respond(250);
		}else {
			//Appends a dot to the start of any line beginning with a dot
			if(s.charAt(0) == '.') {
				localString = "." + s;
			}else {
				localString = s;
			}
			
			if(!MailManager.hasAllocatedStorage()) {
				respond(552);
			}else if(!MailManager.hasSystemStorage()){
				respond(452);
			}else if(!currentMail.canSend()) {
				respond(554);
			}else {
				currentMail.addData(localString);
			}
		}
	}
	
	private void helo(String args[]) {
		//Checks for the right number of arguments
		if(args.length != 2){
			respond(501);
		//Checks if the parameter is a valid domain
		}else if(!ValidateData.isDomain(args[1])){
			respond(501);
		//Checks the program is in the right state
		}else if(state == ConnectionState.CE) {
			state = ConnectionState.HELO;
			respond(250);
		}
		//The RFC doesn't allow a command out of sequence response if the server is in the wrong state
	}

	private void mail(String args[]) {
		//Checks if there's the correct number of arguments
		if(args.length != 2){
			respond(501);
		//Checks the given reverse path is valid
		}else if(!ValidateData.isReversePath(args[1])) {
			respond(501);
		//Checks if the user has enough storage space
		}else if(!MailManager.hasAllocatedStorage()){
			respond(552);
		//Checks if the server has enough storage space
		}else if(!MailManager.hasSystemStorage()){
			respond(452);
		//Checks the program is in the right state
		}else if(state == ConnectionState.HELO) {
			state = ConnectionState.MAIL;
			currentMail = new Mail();
			currentMail.setReversePath(args[1]);
			respond(250);
		}else {
			respond(451);
		}
	}
	
	private void rcpt(String args[]) {
		String mailbox;
		//Checks if there's the correct number of arguments
		if(args.length != 2){
			respond(501);
		//Checks if the argument is in the correct forward
		}else if(!ValidateData.isForwardPath(args[1])){
			respond(501);
		}else {
			//Stores the mailbox format locally
			mailbox = ValidateData.getMailbox(args[1]);
			mailbox = mailbox.substring(1, mailbox.length() - 1);
			
			//Checks if the mailbox is a valid mailbox
			if(!MailManager.hasMailbox(mailbox)) {
				respond(550);
			//Checks if the targeted mailbox is busy
			}else if(MailManager.isBusy(mailbox)) {
				respond(450);
			//Checks if the targeted mailbox is local
			}else if(!ValidateData.getDomain(mailbox).equals(Server.ServerName)) {
				currentMail.setSuggestedPath("<" + ValidateData.getLocalPart(args[1]) + "@" + Server.ServerName + ">");
				respond(551);
			//Checks if the user has the required storage space
			}else if(!MailManager.hasAllocatedStorage()){
				respond(552);
			//Checks if the server has the required storage space
			}else if(!MailManager.hasSystemStorage()){
				respond(452);
			//Checks the mailbox isn't banned
			}else if(!MailManager.mailboxAllowed(mailbox)) {
				respond(553);
			//Checks the mailbox is in the correct state
			}else if(state == ConnectionState.MAIL || state == ConnectionState.RCPT) {
				state = ConnectionState.RCPT;
				if(MailManager.hasForwardingAddress(mailbox)) {
					String forwardingAddress = MailManager.getForwardingAddress(mailbox);
					currentMail.addRecipient(forwardingAddress);
					currentMail.setSuggestedPath(forwardingAddress);
					respond(251);
				}else {
					currentMail.addRecipient(mailbox);
					respond(250);
				}
			//Checks if the command is out of sequence
			}else if(state == ConnectionState.NC || state == ConnectionState.CE || state == ConnectionState.HELO || state == ConnectionState.DATA || state == ConnectionState.CQ) {
				respond(503);
			//Something went wrong in processing
			}else {
				respond(451);
			}
		}
	}
	
	private void data(String args[]) {
		//Checks there are no additional arguments
		if(args.length != 1) {
			respond(501);
		//Checks the server is in the correct state
		}else if(state == ConnectionState.RCPT) {
			//Checks the current mail transaction can be completed
			if(!currentMail.canSend()) {
				respond(554);
			}else {
				//Begins data transaction
				state = ConnectionState.DATA;
				respond(354);
			}
		//Checks if the command has arrived out of order
		}else if(state == ConnectionState.NC || state == ConnectionState.CE || state == ConnectionState.HELO || state == ConnectionState.MAIL || state == ConnectionState.CQ){
			respond(503);
		//Something went wrong
		}else {
			respond(451);
		}
	}
	
	//Quits the program
	private void quit(String args[]) {
		state = ConnectionState.CQ;
		respond(221);
		isRunning = false;
	}
	
	private void respond(int responseCode) {
		String output = "Something went very badly";
		switch(responseCode) {
		case 211: output = "211 " + Server.ServerName + " online";
			break;
		case 214: output = "214 Help";
			break;
		case 220: output = "220 " + Server.ServerName + " Service ready";
			break;
		case 221: output = "221 Service closing transmission channel";
			break;
		case 250: output = "250 Requested mail action okay, completed";
			break;
		case 251: output = "251 User not local: will forward to " + currentMail.getSuggestedPath();
			break;
		case 354: output = "354 Start mail input; end with" + System.getProperty("line.separator") + ".";
			break;
		case 421: output = "421 " + Server.ServerName + " Service not avalible, closing transmission channel";
			break;
		case 450: output = "450 Requested mail action not taken: Mailbox unavailable";
			break;
		case 451: output = "451 Request action aborted: Error in processing";
			break;
		case 452: output = "452 Requested action not taken: insufficient system storage";
			break;
		case 500: output = "500 Syntax error : command unrecognised";
			break;
		case 501: output = "501 Syntax error : parameters or arguments unrecognised";
			break;
		case 502: output = "502 Command not implemented";
			break;
		case 503: output = "503 Bad sequence of commands";
			break;
		case 504: output = "504 Command parameter not implemented";
			break;
		case 550: output = "550 Requested action not taken: Mailbox unavailble";
			break;
		case 551: output = "551 User not local; please try " + currentMail.getSuggestedPath();
			break;
		case 552: output = "552 Requested mail action aborted: Exceeded storage allocation";
			break;
		case 553: output = "553 Requested action not taken: Mailbox name not allowed";
			break;
		case 554: output = "554 Transaction failed";
			break;
		default:
				output = "";
			break;
		}
		//Returns a 7-Bit formatted string to the client
		output = ValidateData.convertToAscii(output);
		try {
			writer.write(output + System.getProperty("line.separator"));
			writer.flush();
		}catch(Exception e) {
			System.out.println("Error : " + e.getMessage());
			state = ConnectionState.ERR;
		}
	}
}