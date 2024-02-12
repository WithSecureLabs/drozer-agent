package com.WithSecure.jsolar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// No idea what this class is used for yet
public class Verify {
    private static final int BUFFER_SIZE = 4096;

    //I hope to god this is only being used for a checksum
    //Also pretty sure this is deserialise vuln vulnerable
    //TODO move away from MD5sum if it doesnt break functionality
    public static String md5sum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        FileInputStream file_stream = new FileInputStream(file);

        byte[] buf = new byte[BUFFER_SIZE];
        int count = 0;
        while((count = file_stream.read(buf, 0, BUFFER_SIZE)) != -1){
            digest.update(buf, 0, count);
        }

        return new BigInteger(1, digest.digest()).toString(16);
    }
}
