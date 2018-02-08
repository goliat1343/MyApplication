package com.example.marekwieciech.myapplication;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class Images extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        LoadImageFromWebOperations(getIntent().getStringExtra("imageUrl"));
    }

    public void LoadImageFromWebOperations(String url) {
        try {

            RetrieveImage retrievedImage = new RetrieveImage();
            retrievedImage.execute(url);
        } catch (Exception e) {

        }
    }


    private class RetrieveImage extends AsyncTask<String, Void, Drawable> {


        @Override
        protected Drawable doInBackground(String... urls) {

            try {
                InputStream is;
                URL urlTemp = new URL(urls[0]);
                is = (InputStream) urlTemp.getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                return d;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            ImageView iv = findViewById(R.id.imageView);
            iv.setImageDrawable(drawable);
        }
    }

}
