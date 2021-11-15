package com.mwr.dz.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IntentProxyToContentProvider extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = Uri.parse(getIntent().getDataString());
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            Log.d("yaytagyay", String.valueOf(bitmap.getByteCount()));
            String yayuriyay = MediaStore.Images.Media.insertImage(getContentResolver(),
                    bitmap,
                    "yaytitleyay",
                    "yaydescriptionyay");

            String File_Name = "yayuriyay.txt";
            FileOutputStream fileobj = openFileOutput(File_Name, Context.MODE_PRIVATE);
            String targetUri = yayuriyay + "\n";
            byte[] ByteArray = targetUri.getBytes();
            fileobj.write(ByteArray);
            fileobj.close();

            Log.d("yaytagyay", "Result: " + yayuriyay);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
