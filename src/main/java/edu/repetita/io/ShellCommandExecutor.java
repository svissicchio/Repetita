package edu.repetita.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellCommandExecutor {
	private static ShellCommandExecutor instance = null;
	   
	private ShellCommandExecutor() {
		// Exists only to defeat instantiation.
	}

	public static ShellCommandExecutor getInstance() {
		if(instance == null) {
			instance = new ShellCommandExecutor();
		}
		return instance;
	}

	public String executeCommand(String command) {

	    StringBuffer output = new StringBuffer();

        String[] cmd = {"/bin/sh", "-c", command};

	    Process p;
	    try {
	        p = Runtime.getRuntime().exec(cmd);
	        p.waitFor();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

	        String line = "";           
	        while ((line = reader.readLine())!= null) {
	            output.append(line + "\n");
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return output.toString();
	}

	public boolean isCommandExecutable(String command) {
		String outCheck = this.executeCommand("hash " + command + " 2>/dev/null || echo \"does not work\"");
		return outCheck.isEmpty();
	}
}
