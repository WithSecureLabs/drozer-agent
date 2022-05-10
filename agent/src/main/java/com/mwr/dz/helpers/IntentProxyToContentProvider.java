package com.mwr.dz.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IntentProxyToContentProvider extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = Uri.parse(getIntent().getDataString());
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            String yayuriyay = MediaStore.Images.Media.insertImage(getContentResolver(),
                    bitmap,
                    "yaytitleyay",
                    "yaydescriptionyay");

            InputStream input = getContentResolver().openInputStream(Uri.parse(yayuriyay));
            File file = new File(getFilesDir(), "yayoutputyay.jpg");
            FileOutputStream output = new FileOutputStream(file);
            try{
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


            Log.d("yaytagyay", "Result: " + yayuriyay);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
