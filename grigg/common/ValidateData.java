package com.grigg.common;

import java.nio.charset.Charset;
import java.nio.ByteBuffer;

public class ValidateData {
	
	/*Public utility functions used by the client*/
	
	//Checks the provided string is a valid IP
	public static boolean validIP (String ip) {
	    try {
	        if ( ip == null || ip.isEmpty() ) {
	            return false;
	        }

	        String[] parts = ip.split( "\\." );
	        if ( parts.length != 4 ) {
	            return false;
	        }

	        for ( String s : parts ) {
	            int i = Integer.parseInt( s );
	            if ( (i < 0) || (i > 255) ) {
	                return false;
	            }
	        }
	        if ( ip.endsWith(".") ) {
	            return false;
	        }

	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	//Checks the provided integer value is without the valid port range
	public static boolean validPort (int port) {
		return(port <= 65535 && port > 0);
	}
	
	/*Public conversion functions*/
	
	//Converts a string to 7-Bit ASCII
	public static String convertToAscii(String s) {
		//Uses a character set to encode to US 7-Bit ASCII before decoding the buffer back into a string
		Charset asciiEncoder = Charset.forName("US-ASCII");
		ByteBuffer buffer = asciiEncoder.encode(s);
		return asciiEncoder.decode(buffer).toString();
	}

	//Returns the Mailbox from a provided path
	public static String getMailbox(String path) {
		String localString = path.substring(1, path.length());
		String components[] = localString.split(":");
		return components[components.length - 1];
	}
	
	//Returns the local part from a provided mailbox
	public static String getLocalPart(String mailbox) {
		String components[] = mailbox.split("@");
		return components[0].substring(1);
	}
	
	//Returns the domain from a provided mailbox
	public static String getDomain(String mailbox) {
		String components[] = mailbox.split("@");
		return components[1].substring(0,components[1].length());
	}
		
	//Privately used data validation
	
	//Provides try parse functionality
	private static Integer tryParse(String s) {
		try {
			return Integer.parseInt(s);
		}catch(Exception e) {
			return -1;
		}
	}

	//Syntax checking
	
	//Checks if the provided string is in the format specified in RFC821 for a Reverse Path
	public static boolean isReversePath(String s) {
		if(s.length() < 6) {
			return false;
		}
		if(!(s.substring(0, 5).toUpperCase().equals("FROM:"))) {
			return false;
		}
		return isPath(s.substring(5));
		
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Forward path
	public static boolean isForwardPath(String s) {
		if(s.length() < 4) {
			return false;
		}
		if(!(s.substring(0, 3).toUpperCase().equals("TO:"))) {
			return false;
		}
		return isPath(s.substring(3));
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Path
	private static boolean isPath(String s) {
		char characters[] = s.toCharArray();
		if(s.length() < 5) {
			return false;
		}
		if(!(characters[0] == '<' && characters[characters.length - 1] == '>')) {
			return false;
		}
		String localString = s.substring(1, characters.length - 1);
		String components[] = localString.split(":");
		if(components.length > 1){
			if(components.length > 2) {
				return false;
			}
			if(!isADL(components[0])) {
				return false;
			}
			if(!isMailbox(components[1])) {
				return false;
			}
		}else {
			if(!isMailbox(localString)) {
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Address
	private static boolean isADL(String s) {
		String elements[] = s.split(",");
		for (String element : elements) {
			if(!isAtDomain(element)) {
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a @Domain
	private static boolean isAtDomain(String s) {
		if(s.length() < 2) {
			return false;
		}
		if(!(s.charAt(0) == '@')) {
			return false;
		}
		if(!(isDomain(s.substring(1)))) {
			return false;
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Domain
	public static boolean isDomain(String s) {
		String elements[] = s.split("\\.");
		for (String element : elements){
			if(!isElement(element)){
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Element
	private static boolean isElement(String s) {
		if(isName(s) || isNumber(s) || isDotnum(s)){
			return true;
		}
		return false;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Mailbox
	private static boolean isMailbox(String s) {
		String components[] = s.split("@");
		if(components.length != 2) {
			return false;
		}
		if(!isDomain(components[1])) {
			return false;
		}
		if(!isLocalPart(components[0])) {
			return false;
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Local name of a mailbox
	private static boolean isLocalPart(String s) {
		if(isDotString(s) || isQuotedString(s)) {
			return true;
		}
		return false;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Name
	private static boolean isName(String s) {
		
		//Checks the first character is a letter
		if(!Character.isLetter(s.charAt(0))) {
			return false;
		}
		
		//Checks if the last character is a letter or digit
		if(!isLetterDigit(s.charAt(s.length() - 1))){
			return false;
		}
		
		//Checks if all other characters are either a: letter, digit, or hyphen
		if(!isLDHString(s.substring(1,s.length() - 1))) {
			return false;
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a Letter/Digit/Hyphen string
	private static boolean isLDHString(String s) {
		char chars[] = s.toCharArray();
		for(char c : chars) {
			if(!isLDH(c)) {
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided character is in the format specified in RFC821 for a Letter/Digit
	private static boolean isLetterDigit(char c) {
		return Character.isLetter(c) || Character.isDigit(c);
	}
	
	//Checks if the provided character is in the format specified in RFC821 for a Letter/Digit/Hyphen
	private static boolean isLDH(char c) {
		return Character.isLetter(c) || Character.isDigit(c) || c == '-';
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a dot separated string
	private static boolean isDotString(String s) {
		String components[] = s.split(".");
		for(String component : components) {
			if(!(isCompatibleString(component))) {
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a string
	private static boolean isCompatibleString(String s) {
		char characters[] = s.toCharArray();
		for (char character : characters) {
			if(!(isCharacter(character))) {
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a quoted string
	private static boolean isQuotedString(String s) {
		if(!(s.length() >= 3)) {
			return false;
		}
		if(!(s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"')) {
			return false;
		}
		if(!isQuotedText(s.substring(1, s.length() - 2))){
			return false;
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a quoted text
	private static boolean isQuotedText(String s) {
		String localString = s;
		
		while(localString.length() > 0) {
			if(localString.charAt(0) == '\\') {
				if(!(isAscii(localString.charAt(1)))){
					return false;
				}
				localString = localString.substring(2);
			}else {
				if(!(isQ(localString.charAt(0)))){
					return false;
				}
				localString = localString.substring(1);
			}
		}
		return true;
	}
	
	//Checks if the provided character is in the format specified in RFC821 for a 7-Bit ASCII character excluding space and any special characters
	private static boolean isCharacter(char c) {
		if(!(isAscii(c))) {
			return false;
		}
		if(isSpecial(c) || isSpace(c)) {
			return false;
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a dot separated number
	private static boolean isDotnum(String s) {
		String snums[] = s.split("\\.");
		
		if(snums.length != 4) {
			return false;
		}
		
		for(int i = 0; i < 4; i++){
			if(!isSNum(snums[i])) {
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a number
	private static boolean isNumber(String s) {
		char chars[] = s.toCharArray();
		int length = chars.length;
		
		//Checks the first character is a hashtag
		if(s.charAt(0) != '#') {
			return false;
		}
		
		//Checks all other characters are digits
		for(int i = 1; i < length; i++) {
			if(!Character.isDigit(chars[i])) {
				return false;
			}
		}
		return true;
	}
	
	//Checks if the provided character is in the format specified in RFC821 for a space
	private static boolean isSpace(char c) {
		return (int) c == 32;
	}
	
	//Checks if the provided string is in the format specified in RFC821 for a small number (8 BIT)
	private static boolean isSNum(String s) {
		int snum = tryParse(s);
		if(snum < 0 || snum > 255) {
			return false;
		}
		return true;
	}
	
	//Checks if the provided character is in the format specified in RFC821 for a Q character
	private static boolean isQ(char c) {
		if(!isAscii(c)) {
			return false;
		}
		int i = (int) c;
		return i != 13 && i != 10 && i != 34 && i != 92;
	}
	
	//Checks if the provided character is in the format specified in RFC821 for any valid ASCII character
	private static boolean isAscii(char c) {
		return (int) c <= 128 && (int) c >= 0;
	}
	
	//Checks if the provided character is in the format specified in RFC821 for a special character
	private static boolean isSpecial(char c) {
		int i = (int) c;
		return 
				i == 60 ||
				i == 62 ||
				i == 40 ||
				i == 41 ||
				i == 91 ||
				i == 93 ||
				i == 92 ||
				i == 46 ||
				i == 44 ||
				i == 58 ||
				i == 59 ||
				i == 64 ||
				i == 34 ||
				i <= 31 ||
				i == 127;
	}
}
