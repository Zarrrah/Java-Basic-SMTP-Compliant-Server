package com.grigg.server;

public class ForwardingData {
	
	//Locally stored values
	private String mailbox;
	private String forwardingAddress;
	
	//Constructor for getting values into the forwarding data
	public ForwardingData(String mailbox, String forwardingAddress) {
		this.mailbox = mailbox;
		this.forwardingAddress = forwardingAddress;
	}
	
	//Checks if a provided value matches the locally stored mailbox
	public boolean mailboxEquals(String target) {
		return mailbox.equals(target);
	}
	
	//Returns the forwarding address
	public String getForwardingAddress() {
		return forwardingAddress;
	}
}
