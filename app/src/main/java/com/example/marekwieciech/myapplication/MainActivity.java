package com.example.marekwieciech.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    // Remote Config keys
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static final long cacheExpiration = 0;      //indicating the next fetch request
                                                        // will use fetch data from the Remote Config service, rather than cached parameter values,
                                                        // if cached parameter values are more than cacheExpiration seconds old.
    private static final String PARAM_STRING_1 = "param_string_1";
    private static final String PARAM_STRING_2 = "param_string_2";
    private static final String PARAM_STRING_1_CAPS = "param_string_1_caps";
    private static final String PARAM_STRING_2_CAPS = "param_string_2_caps";
    private static final String PARAM_BOOLEAN = "param_boolean";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //((TextView) findViewById(R.id.textSms)).setKeyListener(null);           //make TextView readonly
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        showRemoteCofingParameters();
    }

    public void setPermissions(View view) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            this.requestPermissions(new String[]{Manifest.permission.READ_SMS}, 1);
        }
    }

    public void getLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            TextView textViewsSms = (TextView) findViewById(R.id.textSms);
                            textViewsSms.setText(location.toString() + "\n\n Mock? : " + location.isFromMockProvider());
                        }
                    }
                });


        //DELETE  this is for removing mock localization provider apps
        /*LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        TextView textViewsSms = (TextView) findViewById(R.id.textSms);
        try {
            Log.d("by Goliat", "Removing Test providers");
            textViewsSms.setText("Removing Test providers");
            Toast.makeText(this, "Removing Test providers", Toast.LENGTH_LONG).show();
            lm.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
            lm.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
            lm.clearTestProviderStatus(LocationManager.GPS_PROVIDER);
            lm.removeTestProvider(LocationManager.GPS_PROVIDER);
        } catch (IllegalArgumentException error) {
            textViewsSms.setText(error.toString());
            Toast.makeText(this, "Got exception in removing test  provider", Toast.LENGTH_LONG).show();
            Log.d("by Goliat", "Got exception in removing test  provider");
        } catch (Exception error) {
            textViewsSms.setText(error.toString());
        }*/


    }

    public void showMap(View view) {
        Intent intent = new Intent(this, MWMapsActivity.class);
        startActivity(intent);
        Toast.makeText(this, "....ma.....pa.....Cie.....pcha....", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 1 && permissions.length > 0 && permissions[0] == Manifest.permission.READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                (findViewById(R.id.btnSetPermissions)).setEnabled(false);
            }
        }
    }

    /**
     * Called when the user taps the Send button
     */
    public void wyswietlText(View view) {
        List<String> lstSms = new ArrayList<String>();
        ContentResolver cr = this.getContentResolver();
        TextView textViewsSms = (TextView) findViewById(R.id.textSms);

        try {

            Cursor conversations = cr.query(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI,
                    new String[]{Telephony.Sms.BODY,
                            Telephony.Sms.THREAD_ID,
                            Telephony.Sms.PERSON,
                            Telephony.Sms.ADDRESS,
                            Telephony.Sms.DATE,
                            Telephony.Sms.TYPE
                    },
                    null,
                    null,
                    "thread_id ASC, date DESC");


            lstSms.clear();

            int columnBody = conversations.getColumnIndexOrThrow(Telephony.Sms.BODY);
            int columnThreadId = conversations.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID);
            int columnPerson = conversations.getColumnIndexOrThrow(Telephony.Sms.PERSON);
            int columnAddres = conversations.getColumnIndexOrThrow(Telephony.Sms.ADDRESS);
            int columnDate = conversations.getColumnIndexOrThrow(Telephony.Sms.DATE);
            int columnType = conversations.getColumnIndexOrThrow(Telephony.Sms.TYPE);


            if (conversations.moveToFirst()) {
                for (int i = 0; i < 20; i++) {
                    lstSms.add(Telephony.Sms.THREAD_ID + ":" + conversations.getString(columnThreadId) + "\n"
                            + Telephony.Sms.TYPE + ":" + conversations.getString(columnType) + "\n"
                            + Telephony.Sms.ADDRESS + ":" + conversations.getString(columnAddres) + "\n"
                            + Telephony.Sms.PERSON + ":" + conversations.getString(columnPerson) + "\n"
                            + Telephony.Sms.DATE + ":" + conversations.getString(columnDate) + "\n"
                            + Telephony.Sms.BODY + ":" + conversations.getString(columnBody) + "\n"
                            + "--------------------\n"
                    );
                    conversations.moveToNext();
                }
            } else {
                throw new RuntimeException("You have no SMS in Inbox");
            }

            textViewsSms.setText(lstSms.size() + "\n\n" + lstSms.toString());
            //textViewSmsCount.setText(conversations.getCount());

            Toast.makeText(this, "Jadziem panie na baranie......by Goliat....really by Goliat.....wtf?? :D", Toast.LENGTH_SHORT).show();
            conversations.close();

        } catch (Exception e) {
            Writer w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            textViewsSms.setText(w.toString());
        }

        //For testing only
        showRemoteCofingParameters();
    }

    public void writeFile(View view){
        String fileName = "test_file_MW";
        TextView textViewSms = findViewById(R.id.textSms);

        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(textViewSms.getText().toString().getBytes());
            fos.close();
        } catch (Exception e) {
            textViewSms.setText(e.toString());
        }
    }

    public void readFile(View view){
        String fileName = "test_file_MW";
        TextView textViewSms = findViewById(R.id.textSms);

        try {
            FileInputStream fis = openFileInput(fileName);

            if (fis != null) {
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null){
                sb.append(line).append("\n");
            }

            fis.close();
            textViewSms.setText(sb.toString());

            }
        } catch (Exception e) {
            textViewSms.setText(e.toString());
        }
    }

    private void showRemoteCofingParameters(){
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. See Best Practices in the
        // README for more information.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        // [END enable_dev_mode]

        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        // [END set_default_values]

        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this, new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task){
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Fetch Succeeded",
                            Toast.LENGTH_SHORT).show();

                    // After config data is successfully fetched, it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();
                } else {
                    Toast.makeText(MainActivity.this, "Fetch Failed",
                            Toast.LENGTH_SHORT).show();
                }

                TextView textSmsCount = findViewById(R.id.textSmsCount);
                textSmsCount.setAllCaps(mFirebaseRemoteConfig.getBoolean(PARAM_STRING_1_CAPS));
                textSmsCount.setText(mFirebaseRemoteConfig.getString(PARAM_STRING_1));
            }
        });
    }

}
