package com.example.marekwieciech.myapplication;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user taps the Send button */
    public void wyswietlText(View view) {
        List<String> lstSms = new ArrayList<String>();
        ContentResolver cr = this.getContentResolver();
        TextView textView = (TextView) findViewById(R.id.textSmsCount);
        TextView textViewsSms = (TextView) findViewById(R.id.textSMS);

        try {


            Cursor c = cr.query(Telephony.Sms.Inbox.CONTENT_URI, // Official CONTENT_URI from docs
                    new String[] { Telephony.Sms.Inbox.BODY }, // Select body text
                    null,
                    null,
                    Telephony.Sms.Inbox.DEFAULT_SORT_ORDER); // Default sort order


            int totalSMS = c.getCount();

            Cursor cNew = cr.query(Telephony.Sms.Inbox.CONTENT_URI,
                    null,
                    null,
                    null,
                    Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);

            textView.setText(Integer.toString(totalSMS));

            if (c.moveToFirst()) {
                for (int i = 0; i < 2; i++) {
                    lstSms.add(c.getString(0));
                    c.moveToNext();
                }
            } else {
                throw new RuntimeException("You have no SMS in Inbox");
            }
            c.close();
            cNew.close();


            textViewsSms.setText(lstSms.toString());

        } catch (Exception e){
            textViewsSms.setText(e.toString());
        }




    }


    public List<String> getAllSmsFromProvider() {
        List<String> lstSms = new ArrayList<String>();
        ContentResolver cr = this.getContentResolver();

        Cursor c = cr.query(Telephony.Sms.Inbox.CONTENT_URI, // Official CONTENT_URI from docs
                new String[] { Telephony.Sms.Inbox.BODY }, // Select body text
                null,
                null,
                Telephony.Sms.Inbox.DEFAULT_SORT_ORDER); // Default sort order


        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < 2; i++) {
                lstSms.add(c.getString(i));
                c.moveToNext();
            }
        } else {
            throw new RuntimeException("You have no SMS in Inbox");
        }
        c.close();

        return lstSms;
    }

}
