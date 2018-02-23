package com.grigg.common;

import java.util.Scanner;

public class ConfigConnection {
	
	public static Scanner scanner;
	
	public ConfigConnection() {
		scanner = new Scanner(System.in);
	}
	
	//Returns a parsed Int based upon user input
	public int getPort() {
		return Integer.parseInt(getConnectionInput("port"));
	}
	
	//Returns an IP from user input
	public String getIP() {
		return getConnectionInput("IP");
	}
	
	//Gets user input, looping the process until the user confirms their input is correct
	private String getConnectionInput(String parameter) {
		String value;
		do {
			System.out.print(parameter + " : ");
			value = scanner.nextLine();
			System.out.print("Do you wish to connect to "+ parameter + " " + value + " ?(Y/N)");
		}while(!confirmInput());
		return value;
	}
	
	//Confirms the user input
	public boolean confirmInput() {
		String input = scanner.nextLine().toLowerCase();
		return (input.equals("y"));
	}
}
