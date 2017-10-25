package prashushi.vrsamples;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
//Manifest.permission.CHANGE_CONFIGURATION,
    static String[] permissions={ Manifest.permission.READ_PHONE_STATE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.MODIFY_AUDIO_SETTINGS};
    private String fNmae = "four.mp33";
    private String fPAth = "android.resource://";
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 9;
    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "Hello", Toast.LENGTH_LONG);
        b = (Button) findViewById(R.id.bt_set);
        fPAth = "android.resource://" + getApplicationContext().getPackageName() + "/"+R.raw.four;
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                intent1.setType("audio/*");
                startActivityForResult(
                        Intent.createChooser(intent1, "Choose Sound File"), 6);
            }
        });
        checkPerm();
    }
    boolean checkPerm(){
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(this);
            for(String perm:permissions) {
                permission = ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
                if(!permission) {
                    System.out.println("XXX not allowed:"+perm);
                    ActivityCompat.requestPermissions(this, permissions,
                            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    return false;
                }
            }
            if(!permission)
            {

                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MainActivity.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            return false;
            }

        }else
            registerCallReceiver();
    return  true;
    }
    private void setRingtoneFirst() {
        if(firstTime()){
            fPAth = "android.resource://" + getPackageName() + "/"+R.raw.one;
            System.out.println("fPath:"+fPAth);
            Uri uri = Uri.parse(fPAth);
            RingtoneManager.setActualDefaultRingtoneUri(this,
                    RingtoneManager.TYPE_RINGTONE, uri);

        }
    }

    private boolean firstTime() {
        SharedPreferences sPref=getSharedPreferences(getString(R.string.S_PREFS), MODE_PRIVATE);
        return !sPref.contains("ringtone");
    }

    private void registerCallReceiver() {
        System.out.println("XXXX inside111");
        Toast.makeText(this, "Registered", Toast.LENGTH_LONG);
        IncomingCallReceiver broadcast_receiver = new IncomingCallReceiver();
                    IntentFilter filter1 = new IntentFilter();
                    filter1.addAction("android.intent.action.PHONE_STATE");
        try {
            registerReceiver(broadcast_receiver, filter1);
        }catch(Exception e){
            System.out.println("Already registered");
        }
        System.out.println("XXXX inside222");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPerm()){
                    setRingtoneFirst();
                    registerCallReceiver();
                    }
                } else {
                }
                return;
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 6) {
            Uri i = data.getData();  // getData
            String s = i.getPath(); // getPath
            File k = new File(s);  // set File from path
            if (s != null) {      // file.exists

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, "ring");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                values.put(MediaStore.MediaColumns.SIZE, k.length());
                values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                values.put(MediaStore.Audio.Media.IS_ALARM, true);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                Uri uri = MediaStore.Audio.Media.getContentUriForPath(k
                        .getAbsolutePath());
                getContentResolver().delete(
                        uri,
                        MediaStore.MediaColumns.DATA + "=\""
                                + k.getAbsolutePath() + "\"", null);
                Uri newUri = getContentResolver().insert(uri, values);
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                            MainActivity.this, RingtoneManager.TYPE_RINGTONE,
                            newUri);
                    Toast.makeText(this, "Ringtone set", Toast.LENGTH_LONG).show();
                } catch (Throwable t) {

                }
            }
        }
    }
    private void setRingtone() {
        AssetFileDescriptor openAssetFileDescriptor;
        ((AudioManager) getSystemService(AUDIO_SERVICE)).setRingerMode(2);
        File file = new File(Environment.getExternalStorageDirectory() + "/appkeeda", this.fNmae);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri parse = Uri.parse(this.fPAth);
        ContentResolver contentResolver = getContentResolver();
        try {
            openAssetFileDescriptor = contentResolver.openAssetFileDescriptor(parse, "r");
        } catch (FileNotFoundException e2) {
            openAssetFileDescriptor = null;
        }
        try {
            byte[] bArr = new byte[1024];
            FileInputStream createInputStream = openAssetFileDescriptor.createInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for (int read = createInputStream.read(bArr); read != -1; read = createInputStream.read(bArr)) {
                fileOutputStream.write(bArr, 0, read);
            }
            fileOutputStream.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_data", file.getAbsolutePath());
        contentValues.put("title", "nkDroid ringtone");
        contentValues.put("mime_type", "audio/mp3");
        contentValues.put("_size", Long.valueOf(file.length()));
        contentValues.put("artist", Integer.valueOf(R.string.app_name));
        contentValues.put("is_ringtone", Boolean.valueOf(true));
        contentValues.put("is_notification", Boolean.valueOf(false));
        contentValues.put("is_alarm", Boolean.valueOf(false));
        contentValues.put("is_music", Boolean.valueOf(false));
        try {
            Toast.makeText(this, new StringBuilder().append("Ringtone set successfully"), Toast.LENGTH_LONG).show();
            RingtoneManager.setActualDefaultRingtoneUri(getBaseContext(), 1, contentResolver.insert(MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath()), contentValues));
        } catch (Throwable th) {
            Toast.makeText(this, new StringBuilder().append("Ringtone feature is not working"), Toast.LENGTH_LONG).show();
        }
    }
}
