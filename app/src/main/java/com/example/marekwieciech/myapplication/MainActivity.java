package com.example.marekwieciech.myapplication;

import android.Manifest;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setPermissions(View view){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            this.requestPermissions(new String[]{Manifest.permission.READ_SMS}, 1);
        }
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
        TextView textView = (TextView) findViewById(R.id.textSmsCount);
        TextView textViewsSms = (TextView) findViewById(R.id.textSMS);

        try {

            /*
            Cursor c = cr.query(Telephony.Sms.Inbox.CONTENT_URI, // Official CONTENT_URI from docs
                    new String[] { Telephony.Sms.Inbox.BODY }, // Select body text
                    null,
                    null,
                    Telephony.Sms.Inbox.DEFAULT_SORT_ORDER); // Default sort order

            int totalSMS = c.getCount();

            textView.setText(Integer.toString(totalSMS));

            if (c.moveToFirst()) {
                for (int i = 0; i < 20; i++) {
                    lstSms.add(c.getString(0));
                    lstSms.add("\n----------\n");
                    c.moveToNext();
                }
            } else {
                //throw new RuntimeException("You have no SMS in Inbox");
            }
            c.close();

            textViewsSms.setText(lstSms.size() + "\n\n||||||||||||||||||\n\n" + lstSms.toString());
            */

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
            conversations.close();

            textViewsSms.setText(lstSms.size() + "\n\n" + lstSms.toString());

        } catch (Exception e){
            Writer w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            textViewsSms.setText(w.toString());
        }


    }


}
