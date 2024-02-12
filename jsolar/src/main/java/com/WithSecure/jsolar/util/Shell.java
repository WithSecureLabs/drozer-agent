package com.WithSecure.jsolar.util;

import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

//This class implements a shell by reading and writing to stdin and stdout buffers
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
                value.append((char) c);
            }
            //Why, why are we sleeping former MWR developers
            Thread.sleep(15);
        }
        while(this.stderr.available() > 0) {
            for (int i = 0; i < this.stderr.available(); i++) {
                int c = this.stderr.read();
                value.append((char) c);
            }

            Thread.sleep(15);
        }

        return value.toString();
    }


    public boolean valid() {
        try {
            this.fd.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }

        // Original function in JDiesel had a large commented out block of code here
    }

    public void write(String value) throws IOException, InterruptedException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.stdout.write((value + "\n").getBytes(StandardCharsets.UTF_8));
        } else {
            this.stdout.write((value + "\n").getBytes());
        }
        this.stdout.flush();
        Thread.sleep(100);
    }
}
