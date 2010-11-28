package ru.gennad.commenter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Commenter {
	
	public static String fileSeparator;
	public static Logger logger = Logger.getLogger("MyLog");
	public static FileHandler fh;
	public static String stringDelimeter;
	
	public static void main(String[] args) {
		
		System.out.println("Hello!");
		
		fileSeparator = System.getProperty("file.separator");				
		
		try {
			fh = new FileHandler("MyLogFile.log", true);
		} catch (SecurityException e) {
			logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {			
			logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		}
	    logger.addHandler(fh);
	    //logger.setLevel(Level.ALL);	    
	    SimpleFormatter formatter = new SimpleFormatter();
	    fh.setFormatter(formatter);
		
		String path = null;		
		if (args.length == 1) {
			if (args[0].equals("C:\\") || args[0].equals("C:/")) {
				System.out.println("Wrong directory");
				System.exit(0);
			}
			path = args[0];
		}
		else {
			logger.warning("Num of arguments: "+args.length);
			//logger.info(args[0]);
			System.out.println("Usage: jdcom <path to project root dir>");			
		}
		
		if (path == null) System.exit(0);
			
		listFiles(path);
		
		//String os = System.getProperty("os.name");
		//TODO stub
		stringDelimeter = "\r\n";

	}
	
	public static void listFiles(String path) {
		File f = new File(path);
		
		if (!f.exists()) 
			System.out.println("This path is invalid. Please check it and repeat.");
		
		if (f.isDirectory()) {
			String[] children = f.list();
			for (int i = 0; i < children.length; i++) {
				children[i] = path + fileSeparator + children[i];
				System.out.println(children[i]);
				listFiles(children[i]);
			}			
		}
		else {
			int unixSeparator = path.lastIndexOf("/");
			int winSeparator = path.lastIndexOf("\\");
			char separator;
			String fileName;
			
			if (unixSeparator > winSeparator) {
				
				separator = '/';
				logger.log(Level.INFO, "Separator: "+separator);
			}
			else if (winSeparator > unixSeparator) {
				separator = '\\';
				logger.log(Level.INFO, "Separator: win");
			}
			else {
				separator = '\u0000';
				logger.log(Level.INFO, "Separator: "+separator);
			}
						
			if (separator != '\u0000') {
				
				String finalSep;
				if (separator == '\\') {
					finalSep = "\\\\";
				}
				else {
					finalSep = String.valueOf(separator);
				}
				
				String[] pieces = path.split(finalSep);
				fileName = pieces[pieces.length-1];
			}
			else {
				fileName = path;
			}
			
			if (fileName.endsWith(".java")) {
				logger.log(Level.INFO, "opening: "+fileName);
				try {
					FileInputStream fstream = new FileInputStream(path);
				    // Get the object of DataInputStream
				    DataInputStream in = new DataInputStream(fstream);
				    BufferedReader br = new BufferedReader(new InputStreamReader(in));
				    String strLine;
				    String fullContent = "";
				    int lineNumber = 0;
				    boolean inMethod = false;
				    
				    //Creating temp file
				    FileWriter fout = new FileWriter("testfile.txt", false);
				    PrintWriter fileout = new PrintWriter(fout,true);
				    
				    String prevLine = "";
				    
				    //Read File Line By Line
				    while ((strLine = br.readLine()) != null)   {
				        // Print the content on the console
				        //System.out.println (strLine);
				        fullContent.concat(strLine);
				        
				        if (lineNumber == 0 && !strLine.contains("/**")) {
				        	//then write comment
				        	String commentSequence = "/**" + "\r\n" +
				        	" * "+fileName + "\r\n" +
				        	" * " + "\r\n" +
				        	" * Copyright (c) 2010, TopCoder, Inc. All rights reserved"+ "\r\n" +
				        	" */";
				        	fileout.println(commentSequence);
				        	fileout.flush();
				        }
				        
				        //check if current line is class and previous is not ending of comment
				        if (strLine.contains("class") && !prevLine.contains("*/")) {
				        	String commentSequence = "/**" + "\r\n" +
				        	" * "+ "\r\n" +
				        	" * @author TCSDEVELOPER"+ "\r\n" +
				        	" * @version 1.0 "+ "\r\n" +
				        	" */";
				        	fileout.println(commentSequence);
				        	fileout.flush();
				        }
				        				        				        
				        //check if current line is variable
				        if (isLineVariable(strLine) && !inMethod) {
				        	logger.info("Line "+strLine+" is variable.");
				        	String spaces = getFirstSpaces(strLine);
				        	String commentSequence = 
				        		spaces + "/**" + "\r\n" + 
					        	spaces + " * "+ "\r\n" +
					        	spaces + " */";
				        	fileout.println(commentSequence);
				        	fileout.flush();				        	
				        }
				        
				        //check if current line is public and not class and is not ending by comment				        
				        if (strLine.matches(".*\\(.*\\).*") && !prevLine.contains("*/") 
				        			&& !strLine.contains("protected") 
				        			&& !strLine.contains("private")
				        			&& !strLine.contains("for ")
				        			&& !strLine.contains("if ")
				        			&& !strLine.contains("if(")
				        			&& !strLine.contains("for(")
				        			&& !strLine.contains("while(")
				        			&& !strLine.contains("while (")
				        			&& !strLine.contains("do {")
				        			&& !strLine.contains("do{(")
				        			&& !strLine.trim().endsWith(";")) {
				        	
				        	inMethod = true;
				        	String spaces = getFirstSpaces(strLine);
				        	String commentSequence = 
				        	spaces + "/**" + "\r\n" + 
				        	spaces + " * "+ "\r\n";
				        	
				        	//get all params
				        	HashMap<String, String> params = argumentParser(strLine);
				        	Set<String> keys = params.keySet();
				        	
				        	for (String key: keys) {
				        		commentSequence += spaces + " * @param " + params.get(key) + "\r\n";
				        	}
				        	
				        	//get all throws
				        	String[] thrs = getAllThrows(strLine);
				        	String throwsMessage = "";
				        	int iterator = 0;
				        	for (String t: thrs) {
				        		throwsMessage +=
				        		spaces + " * @throws " +thrs[iterator] + "\r\n";
				        		iterator++;
				        	}
				        	
				        	commentSequence += 				        					        					        	
				        	spaces + " * @return "+ getReturnType(strLine) + "\r\n" +				        	
				        	throwsMessage +				        					        	
				        	spaces + " */";
				        	fileout.println(commentSequence);
				        	fileout.flush();
				        }
				        
				        fileout.println(strLine);
					    fileout.flush();					    
					    
					    lineNumber++;
				        prevLine = strLine;				        
				    }
				    //Close the input stream
				    in.close();
				    fileout.close();
				}
				catch (FileNotFoundException fnfe) {
					logger.log(Level.WARNING, fnfe.getMessage());
				}
				catch (IOException ioe) {
					logger.log(Level.WARNING, ioe.getMessage());
				}
				
				try {
					viceversa(path);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				
			}			
		}			 
	}
	
	
	
	public static void viceversa(String path) throws IOException {
		//now open for reading temp,
		//delete all in source,
		//write all to source
		//delete all in temp
		FileInputStream fstream = new FileInputStream("testfile.txt");
	    // Get the object of DataInputStream
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    String fullContent = "";
	    int lineNumber = 0;
	    boolean inMethod = false;
	    
	    //Creating temp file
	    FileWriter fout = new FileWriter(path, false);
	    PrintWriter fileout = new PrintWriter(fout,true);
	    
	    String prevLine = "";
	    
	    //Read File Line By Line
	    while ((strLine = br.readLine()) != null)   {
	        // Print the content on the console
	        //System.out.println (strLine);
	        fullContent.concat(strLine);	        	        
	        fileout.println(strLine);
	        fileout.flush();	        	        				    		    		    
	        				        
	    }
	    //Close the input stream
	    in.close();
	    fileout.close();
	    File file1 = new File("testfile.txt");
	    file1.delete();
	}
	
	public static HashMap<String, String> argumentParser(String signature) {
		
		HashMap<String, String> resHashMap = new HashMap<String, String>();
		
		int first = signature.indexOf("(");
		String substring = signature.substring(first+1);
		int second = substring.indexOf(")");
		substring = substring.substring(0, second).trim();
		
		if (!substring.contains(",")) {			
			
			if (substring.matches("[A-Za-z].*")) {
				
				//one var
				String[] typeAndVar = substring.split(" ");
				if (typeAndVar.length > 1) {
					resHashMap.put(typeAndVar[1], typeAndVar[0]);
					logger.info("Logging: "+typeAndVar[1]+" "+typeAndVar[0]);
				}
				return resHashMap;
			}
			else {
				//no vars
				return resHashMap;
			}
		}
		
		
		String args[] = substring.split("\\,");
		
		if (args.length == 1 && args[0].trim() == "") ;
		
		logger.info("Splitting "+substring+": ");
		
		logger.info("Arguments length = "+args.length);
		
		for (int i = 0; i < args.length; i++) {
			
			logger.info("in the cycle");
			
			args[i] = args[i].trim();
			logger.info("Processing "+args[i]);
			
			String[] typeAndVar = args[i].split(" ");
			if (typeAndVar.length > 1) {
				resHashMap.put(typeAndVar[1], typeAndVar[0]);
				logger.info("Logging: "+typeAndVar[1]+" "+typeAndVar[0]);
			}
			else {
				logger.log(Level.SEVERE, "Num of type and var < 2");				
			}
			
		}
		
		return resHashMap;		
	}
	
	public static String getReturnType(String line) {
		int posLeftBracket = line.lastIndexOf("(");
		String subString = line.substring(0, posLeftBracket);
		
		List<String> lines = Arrays.asList(subString.split(" "));
		int len = lines.size();
		int num = len - 2;
		
		try {
			String type = lines.get(num);
			logger.info("Return type: "+type);
			return type;
		}
		catch (IndexOutOfBoundsException e) {
			return "";
		}						
	}
	
	public static String[] getAllThrows(String line) {
		if (line.contains("throws")) {
			line = line.substring(line.indexOf("throws"));
			int space = line.indexOf(' ');
			line = line.substring(space);
			
			String[] allExceptions = line.split("\\,");
			
			int i = 0;
			for (String exception: allExceptions) {
				allExceptions[i] = allExceptions[i].trim();
				i++;
			}
			return allExceptions;
		}
		else {
			String[] r = new String[0];
			return r;
		}
	}
	
	/**
	 * Returns the number of spaces or tabs in the beginning of the string
	 */
	public static String getFirstSpaces(String line) {
				
		char firstChar = line.charAt(0);
		char space;
		
		if (firstChar == ' ') {
			space = ' ';
		}
		else if (firstChar == '\t') {
			space = '\t';
		}
		else {
			return "";
		}
		
		Pattern pattern = Pattern.compile("\\w");				
		Matcher matcher = pattern.matcher(line);
		
		if (matcher.find()) {
			int start = matcher.start();
			logger.info("Starting pos: "+start);
			
			String res = line.substring(0, start);
			return res;
		}
		else {
			logger.info("In string "+line+" there is not matching to space");
			return "";
		}
		
		
		
	}
	
	public static boolean isLineVariable(String line) {
		
		if (line.contains("class")
				|| line.contains("import")
				|| line.contains("*")
				|| line.trim().equals("")
				|| line.contains("{")
				|| line.contains("}")) {
			return false;
		}
		
		int eq = line.indexOf('=');
		if (eq != -1) {
			//it exists
			//line = line.substring(0, eq);
			return true;
		}
		else {
			int lbr = line.indexOf('(');
			int rbr = line.indexOf(')');
			
			if (lbr != -1 || rbr != -1) {
				//if there are brackets
				return false;
			}
			else {
				//if there aren't brackets
				return true;
			}
		}
		
						
	}
}

