package com.grigg.server;
import java.util.ArrayList;

public class MailManager {
	
	private static ArrayList<String> mailboxes; 
	private static ArrayList<String> busyMailboxes; 
	private static ArrayList<String> bannedMailboxes; 
	private static ArrayList<ForwardingData> forwardingAddresses; 
	
	static{
		//Initializes all lists
		mailboxes = new ArrayList<String>();
		busyMailboxes = new ArrayList<String>();
		bannedMailboxes = new ArrayList<String>();
		forwardingAddresses = new ArrayList<ForwardingData>();
		
		//Adds example mailboxes to demonstrate functionality
		mailboxes.add("1@SMTP.com"); //Valid mailbox
		mailboxes.add("2@SMTP.com"); //Busy mailbox
		mailboxes.add("3@SMTP.com"); //Banned mailbox(Can't receive any mail)
		mailboxes.add("4@SMTP.com"); //Mailbox with forwarding set up
		mailboxes.add("5@SMTP.com"); //Destination of a forwarding action
		mailboxes.add("1@derby"); //Valid non-local mailbox
		
		busyMailboxes.add("2@SMTP.com");
		
		bannedMailboxes.add("3@SMTP.com");
		
		forwardingAddresses.add(new ForwardingData("4@SMTP.com","5@SMTP.com"));
	}
	
	//Checks if a mailbox is busy
	public static boolean isBusy(String mailbox) {
		return busyMailboxes.contains(mailbox);
	}
	
	//Sets a mailbox as busy
	public static void setIsBusy(String mailbox, boolean busy) {
		if(busy) {
			if(!isBusy(mailbox)) {
				busyMailboxes.add(mailbox);	
			}
		}else {
			busyMailboxes.remove(mailbox);
		}
	}
	
	//Checks if the user has the required storage space for a mail action
	public static boolean hasAllocatedStorage() {
		//NOT IMPLIMENTED
		return true;
	}

	//Checks if the server has the required storage space and resources to allow the requested mail action
	public static boolean hasSystemStorage() {
		//NOT IMPLIMENTED
		return true;
	}

	//Checks if the provided mailbox can be found
	public static boolean hasMailbox(String mailbox) {
		return mailboxes.contains(mailbox);
	}

	//Checks if the provided mailbox can be mailed
	public static boolean mailboxAllowed(String mailbox) {
		return !bannedMailboxes.contains(mailbox);
	}

	//Checks if the provided mailbox has a forwarding address set up
	public static boolean hasForwardingAddress(String mailbox) {
		for(ForwardingData forwardedMailbox : forwardingAddresses) {
			if(forwardedMailbox.mailboxEquals(mailbox)) {
				return true;
			}
		}
		return false;
	}
	
	//Returns the forwarding address of a provided mailbox
	public static String getForwardingAddress(String mailbox) {
		for(ForwardingData forwardedMailbox : forwardingAddresses) {
			if(forwardedMailbox.mailboxEquals(mailbox)) {
				return forwardedMailbox.getForwardingAddress();
			}
		}
		return mailbox;
	}

	//Saves a finished mail item to file and closes it from memory
	public static boolean saveMail() {
		//NOT IMPLIMENTED
		return true;
	}
}
