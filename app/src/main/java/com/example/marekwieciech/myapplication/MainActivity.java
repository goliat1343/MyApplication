package com.example.marekwieciech.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.textSms)).setKeyListener(null);           //make TextView readonly
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                            textViewsSms.setText(location.toString());
                        }
                    }
                });
    }

    public void showMap(View view){
        Intent intent = new Intent(this, MWMapsActivity.class);
        startActivity(intent);
        Toast.makeText(this, "....ma.....pa.....Cie.....pcha....", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        if(requestCode == 1 && permissions.length > 0 && permissions[0] == Manifest.permission.READ_SMS){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                (findViewById(R.id.btnSetPermissions)).setEnabled(false);
            }
        }
    }

    /** Called when the user taps the Send button */
    public void wyswietlText(View view) {
        List<String> lstSms = new ArrayList<String>();
        ContentResolver cr = this.getContentResolver();
        TextView textViewSmsCount = (TextView) findViewById(R.id.textSmsCount);
        TextView textViewsSms = (TextView) findViewById(R.id.textSms);

        try {

            Cursor conversations = cr.query(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI,
                    new String[]{ Telephony.Sms.BODY,
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
            int columnThreadId =  conversations.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID);
            int columnPerson = conversations.getColumnIndexOrThrow(Telephony.Sms.PERSON);
            int columnAddres = conversations.getColumnIndexOrThrow(Telephony.Sms.ADDRESS);
            int columnDate =  conversations.getColumnIndexOrThrow(Telephony.Sms.DATE);
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

        } catch (Exception e){
            Writer w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            textViewsSms.setText(w.toString());
        }


    }


}
