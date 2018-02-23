package com.grigg.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import com.grigg.common.*;

public class Client {
		public static Socket soc;
		public static BufferedReader reader;
		public static OutputStreamWriter writer;
		public static Boolean isRunning;
		public static ConfigConnection config;
		
	public static void main(String[] args) {
		try {
			//Creates an instance of the client
			Client client = new Client();
			
			//Creates an instance of the config class which is used when opening a connection with the server
			config = new ConfigConnection();
			client.openConnection();
			
			//Allocates the required resources for the client to function correctly
			client.resourceInit();
			
			//Starts the threads required for incoming and outgoing communications
			client.startReader();
			client.startWriter();
		}catch(Exception e) {
			System.out.print("Error: " + e.getMessage());
		}
	}
	
	private void openConnection() {
		
		//Defaults the values
		String ip = "127.0.0.1";
		int port = 50005;
		
		//Gives user prompts for data input
		System.out.println("Do you wish to use the default connection settings?(Y/N)");
		if(!config.confirmInput()) {
			//Gives user prompts for data input
			System.out.println("Please provide connection details for the server you are attmepting to connect to(if you wish to use defaults please enter \"0\")");
			ip = config.getIP();
			port = config.getPort();
		}
		
		//Restores settings to default if there is an error detected with the custom configuration settings
		if(!ValidateData.validIP(ip)) {
			ip = "127.0.0.1";
		}
		if(!ValidateData.validPort(port)) {
			port = 50005;
		}
		
		//opens the connection, either on user specified values, or defaulted values if the users value were incompatible
		establishConnection(ip,port);
	}
	
	private void establishConnection(String ip, int port) {
		try {
			System.out.println("Trying to establish connection to " + ip + ":" + port);
			soc = new Socket(ip ,port);
		}
		catch(Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	//Allocates the required resources for the client to function correctly
	private void resourceInit() {
		try {
			isRunning = true;
			reader = new BufferedReader(new InputStreamReader(soc.getInputStream(), "UTF-8"));
			writer = new OutputStreamWriter(soc.getOutputStream());
		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
		}
	}

	//Runs at the end of the program to close all the resources used during the duration of the program
	protected static void closeResources() {
		try {
			writer.close();
			reader.close();
			soc.close();
		}catch(Exception e){
			System.out.println("Error : " + e.getMessage());
		}
	}

	//Starts client writer thread
	private void startWriter() {
		ClientWriter writer = new ClientWriter();
		Thread writerThread = new Thread(writer);
		writerThread.start();
	}
	
	//Starts client reader thread
	private void startReader() {
		ClientReader reader = new ClientReader();
		Thread readerThread = new Thread(reader);
		readerThread.start();
	}
}

class ClientReader implements Runnable{
	
	@Override
	public void run() {
		while(Client.isRunning) {
			try {
				if(Client.reader.ready() == true) {
					//Converts any server respond to 7-Bit ASCII and then prints it out
					String line = ValidateData.convertToAscii(Client.reader.readLine()) + System.getProperty("line.separator");
					System.out.println(line);
					
					//Closes the clients connection upon receiving a closure response from the server
					if(line.equals("221 Service closing transmission channel" + System.getProperty("line.separator"))) {
						Client.isRunning = false;
						Client.closeResources();
					}
				}
			}catch(Exception e) {
				System.out.println("Error : " + e.getMessage());
			}
		}
	}
}

class ClientWriter implements Runnable{
	
	@Override
	public void run() {
		//Used to receive user input
		Scanner scanner = new Scanner(System.in);
		while(Client.isRunning){
			try {
				//Gets user input and converts it to 7-Bit ASCII
				String input = scanner.nextLine();
				input = ValidateData.convertToAscii(input);
				
				//Sends the converted user input to the server
				Client.writer.write(input + System.getProperty("line.separator"));
				Client.writer.flush();
			}catch(Exception e) {
				System.out.println("Error : " + e.getMessage());
			}
		}
		//Closes the scanner upon the connection terminating
		scanner.close();
		System.out.println("Server connection lost");
	}
}