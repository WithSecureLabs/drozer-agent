package com.mwr.jdiesel.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class Shell {
	
	private Process fd = null;
	private int[] id = new int[1];
	InputStream stdin = null;
	InputStream stderr = null;
	OutputStream stdout = null;
	
	public Shell() throws IOException, InterruptedException {
		this.fd = Runtime.getRuntime().exec("/system/bin/sh -i");
		this.stdin = this.fd.getInputStream();
		this.stderr = this.fd.getErrorStream();
		this.stdout = this.fd.getOutputStream();
		this.write(String.format("cd %s", System.getProperty("user.dir")));
		this.read();
		
	}
	
    public void close() {
    	this.fd.destroy();
    }

	public String read() throws IOException, InterruptedException {
		StringBuffer value = new StringBuffer();
		
		while(this.stdin.available() > 0) {
			for(int i=0; i<this.stdin.available(); i++) {
				int c = this.stdin.read();

				value.append((char)c);
			}
			
			Thread.sleep(15);
		}
		while(this.stderr.available() > 0) {
			for(int i=0; i<this.stderr.available(); i++) {
				int c = this.stderr.read();

				value.append((char)c);
			}
			
			Thread.sleep(15);
		}

		return value.toString();
	}
	
	public boolean valid() {

			try{
				this.fd.exitValue();
				return false;
			}catch(IllegalThreadStateException e){
				return true;
			}
			
			/*
		try{
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec("ps " + this.id[0]);
			pr.waitFor();
			
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			while((line=buf.readLine()) != null) {
				if(line.contains("" + this.id[0])) {
					if(line.split("\\s+")[7].equals("Z"))
						return true;
				}
			}
			
		}
		catch(IOException e) {Log.e("JDIESEL : SHELL", String.format("IO ERROR: %s", e.getMessage()));}
		catch (InterruptedException e) {Log.e("JDIESEL : SHELL", String.format("INTERRUPTED ERROR: %s", e.getMessage()));}
		
		return true;
		*/
	}
    public void write(String value) throws IOException, InterruptedException {
    	this.stdout.write((value + "\n").getBytes());
		this.stdout.flush();
		Thread.sleep(100);

	}
    
}
