package com.mwr.dz.helpers;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IntentProxyToContentProvider extends Activity {
    
    String filename = "yayoutputyay";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = Uri.parse(getIntent().getDataString());
        if (getIntent().getStringExtra("filename") != null) {
            filename = getIntent().getStringExtra("filename");
        }
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), filename);
            FileOutputStream output = new FileOutputStream(file);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = input.read(buf)) > 0) {
                    output.write(buf, 0, len);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null)
                        input.close();
                    if (output != null)
                        output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
