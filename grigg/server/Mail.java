package com.grigg.server;
import java.util.ArrayList;
import java.util.List;

import com.grigg.common.ValidateData;

public class Mail {
	//Values used to store data within the mail
	private String reversePath;
	private ArrayList<String> forwardPaths;
	private String subject;
	private ArrayList<String> body;
	private Boolean isDeleted = false;
	private String suggestedPath = "Unknown";
	
	//Default constructor used to initialize the used array lists
	public Mail() {
		forwardPaths = new ArrayList<String>();
		body = new ArrayList<String>();
	}
	
	public void setReversePath(String s) {
		reversePath = s;
	}
	
	public void setSubject(String s) {
		subject = s;
	}
	
	public void setIsDeleted(boolean deleted) {
		isDeleted = deleted;
	}
	
	public void addData(String s) {
		body.add(s);
	}
	
	public void addRecipient(String s) {
		forwardPaths.add(s);
	}
	
	public String getSuggestedPath() {
		return suggestedPath;
	}
	
	public void setSuggestedPath(String s) {
		suggestedPath = s;
	}
	
	//Returns the last inserted mailbox
	public String getForwardMailbox() {
		if(forwardPaths.size() > 0) {
			//Selects the last added mailbox
			String s = forwardPaths.get(forwardPaths.size() - 1);
			return ValidateData.getMailbox(s);
		}
		//Returns unknown if no mailbox is stored
		return "Unknown";
	}
	
	//Checks if their is a valid recipient for the mail to be sent to
	public boolean canSend() {
		return forwardPaths.size() != 0;
	}
	
	//Method to return all the critical data stored in the class as a list of strings to be written to file
	public List<String> returnData() {
		List<String> data = new ArrayList<String>();
		
		data.add("<Reverse Path>");
		data.add(reversePath);
		
		data.add("<Forward Paths>");
		for(String forwardPath : forwardPaths) {
			data.add(forwardPath);
		}
		
		data.add("<Subject>");
		data.add(subject);
		
		data.add("<IsDeleted>");
		data.add(isDeleted.toString());
		
		data.add("<Body>");
		for(String line : body) {
			data.add(line);
		}
		
		return data;
	}
}