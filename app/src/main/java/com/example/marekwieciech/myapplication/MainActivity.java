package com.example.marekwieciech.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.provider.MediaStore;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    private FirebaseAnalytics mFirebaseAnalytics;

    //Remote Config keys
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static final long cacheExpiration = 0;      //indicating the next fetch request
                                                        // will use fetch data from the Remote Config service, rather than cached parameter values,
                                                        // if cached parameter values are more than cacheExpiration seconds old.
    
    private static final String PARAM_STRING_1 = "param_string_1";
    private static final String PARAM_STRING_2 = "param_string_2";
    private static final String PARAM_STRING_1_CAPS = "param_string_1_caps";
    private static final String PARAM_STRING_2_CAPS = "param_string_2_caps";
    private static final String PARAM_BTNSHOWSMS_ISVISIBLE = "btnShowSms_isVisible";
    private static final String PARAM_BTNSHOWSMS_ISENABLED = "btnShowSms_isEnabled";
    private static final String PARAM_BTNSETPERMISSIONS_ISVISIBLE = "btnSetPermissions_isVisible";
    private static final String PARAM_BTNSETPERMISSIONS_ISENABLED = "btnSetPermissions_isEnabled";
    private static final String PARAM_BTNSHOWLOCATION_ISVISIBLE = "btnShowLocation_isVisible";
    private static final String PARAM_BTNSHOWLOCATION_ISENABLED = "btnShowLocation_isEnabled";
    private static final String PARAM_BTNSHOWMAP_ISVISIBLE = "btnShowMap_isVisible";
    private static final String PARAM_BTNSHOWMAP_ISENABLED = "btnShowMap_isEnabled";
    private static final String PARAM_BTNWRITEFILE_ISVISIBLE = "btnWriteFile_isVisible";
    private static final String PARAM_BTNWRITEFILE_ISENABLED = "btnWriteFile_isEnabled";
    private static final String PARAM_BTNREADFIlE_ISVISIBLE = "btnReadFile_isVisible";
    private static final String PARAM_BTNREADFIlE_ISENABLED = "btnReadFile_isEnabled";
    private static final String PARAM_TEXTVIEWSMS_TEXTSIZE_SIZE = "textViewSms_size_size";
    private static final String PARAM_TEXTVIEWSMS_TEXTSIZE_UNIT = "textViewSms_size_unit";
    private static final String PARAM_IMAGES_IMAGEVIEW_URL = "images_imageView_url";

    //Buttons
    Button btnShowSms;
    Button btnSetPermissions;
    Button btnShowLocation;
    Button btnShowMap;
    Button btnWriteFile;
    Button btnReadFile;

    //TextViewz
    TextView textViewSmsCount;
    TextView textViewSms;


    private GoogleSignInClient mGoogleSignInClient;
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private Bitmap mBitmapToSave;
    private DriveResourceClient mDriveResourceClient;
    private DriveClient mDriveClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //((TextView) findViewById(R.id.textSms)).setKeyListener(null);           //make TextView readonly
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        btnShowSms = findViewById(R.id.btnShowSms);
        btnSetPermissions = findViewById(R.id.btnSetPermissions);
        btnShowLocation = findViewById(R.id.btnShowLocation);
        btnShowMap = findViewById(R.id.btnShowMap);
        btnWriteFile = findViewById(R.id.btnWriteFile);
        btnReadFile = findViewById(R.id.btnReadFile);

        textViewSmsCount = findViewById(R.id.textSmsCount);
        textViewSms = findViewById(R.id.textSms);

        textViewSms.setHorizontallyScrolling(true);
        textViewSms.setMovementMethod(new ScrollingMovementMethod());


        //Firebase
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




        //Data from notifcation
        boolean dontChangeTextViewSmsCount = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            textViewSmsCount.setText(bundle.getString("text", ""));
            dontChangeTextViewSmsCount = true;
        }

        showRemoteCofingParameters(dontChangeTextViewSmsCount);

        //signIn();
    }

    public void setPermissions(View view) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            this.requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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

    public void showImage(View view) {
        Intent intent = new Intent(this, Images.class);
        intent.putExtra("imageUrl", mFirebaseRemoteConfig.getString(PARAM_IMAGES_IMAGEVIEW_URL));
        startActivity(intent);
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

        try {

            Cursor conversations = cr.query(Telephony.Sms.Inbox.CONTENT_URI,
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

            textViewSms.setText(lstSms.size() + "\n\n" + lstSms.toString());


            Toast.makeText(this, "Jadziem panie na baranie......by Goliat....really by Goliat.....wtf?? :D", Toast.LENGTH_SHORT).show();
            conversations.close();

        } catch (Exception e) {
            Writer w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            textViewSms.setText(w.toString());
        }

        logBtnClicked(btnShowSms);

        //For testing only
        showRemoteCofingParameters(false);
    }

    public void writeFile(View view){
        String fileName = "test_file_MW";

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

    private void showRemoteCofingParameters(final boolean dontChangeTextViewSmsCount){

        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this, new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task){
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                } else {
                    Toast.makeText(MainActivity.this, "Parameters fetch Failed",
                            Toast.LENGTH_SHORT).show();
                }

                btnShowSms.setVisibility(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSHOWSMS_ISVISIBLE) ? View.VISIBLE : View.GONE);
                btnSetPermissions.setVisibility(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSETPERMISSIONS_ISVISIBLE) ? View.VISIBLE : View.GONE);
                btnShowLocation.setVisibility(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSHOWLOCATION_ISVISIBLE) ? View.VISIBLE : View.GONE);
                btnShowMap.setVisibility(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSHOWMAP_ISVISIBLE) ? View.VISIBLE : View.GONE);
                btnWriteFile.setVisibility(mFirebaseRemoteConfig.getBoolean(PARAM_BTNWRITEFILE_ISVISIBLE) ? View.VISIBLE : View.GONE);
                btnReadFile.setVisibility(mFirebaseRemoteConfig.getBoolean(PARAM_BTNREADFIlE_ISVISIBLE) ? View.VISIBLE : View.GONE);

                btnShowSms.setEnabled(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSHOWSMS_ISENABLED));
                btnSetPermissions.setEnabled(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSETPERMISSIONS_ISENABLED));
                btnShowLocation.setEnabled(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSHOWLOCATION_ISENABLED));
                btnShowMap.setEnabled(mFirebaseRemoteConfig.getBoolean(PARAM_BTNSHOWMAP_ISENABLED));
                btnWriteFile.setEnabled(mFirebaseRemoteConfig.getBoolean(PARAM_BTNWRITEFILE_ISENABLED));
                btnReadFile.setEnabled(mFirebaseRemoteConfig.getBoolean(PARAM_BTNREADFIlE_ISENABLED));

                if (!dontChangeTextViewSmsCount) {
                    textViewSmsCount.setAllCaps(mFirebaseRemoteConfig.getBoolean(PARAM_STRING_1_CAPS));
                    textViewSmsCount.setText(mFirebaseRemoteConfig.getString(PARAM_STRING_1));
                }


                String paramString2 = mFirebaseRemoteConfig.getString(PARAM_STRING_2);
                if (!paramString2.isEmpty() && paramString2.length() > 0){
                    textViewSms.setText(paramString2.replaceAll("<n_l>", "\n"));
                    textViewSms.setAllCaps(mFirebaseRemoteConfig.getBoolean(PARAM_STRING_2_CAPS ));

                    try {
                        int unit = Integer.parseInt(mFirebaseRemoteConfig.getString(PARAM_TEXTVIEWSMS_TEXTSIZE_UNIT));
                        float size = Float.parseFloat(mFirebaseRemoteConfig.getString(PARAM_TEXTVIEWSMS_TEXTSIZE_SIZE));
                        textViewSms.setTextSize(unit, size);
                    } catch (NumberFormatException e){

                    }
                }
            }
        });
    }

    private void logBtnClicked(Button btn){
        Bundle bundle = new Bundle();
        bundle.putString("button_id", getResources().getResourceEntryName(btn.getId()));
        mFirebaseAnalytics.logEvent("BUTTON_CLCICKED_MW", bundle);

        //DELETE firebase defined events logging
//        Bundle bundle1 = new Bundle();
//        bundle1.putString(FirebaseAnalytics.Param.ITEM_NAME, getResources().getResourceEntryName(btn.getId()));
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle1);
    }

    /** Start sign in activity. */
    private void signIn() {
        Log.i("drive by G", "Start sign in");
        mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /** Build a Google SignIn client. */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    public void saveGdriveFile(View view){
        saveFileToDrive();
    }

    /** Create a new file and save it to Drive. */
    public void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i("drive by G", "Creating new contents.");
        final Bitmap image = mBitmapToSave;

        Log.i("drive by G", "Signed in successfully.");
        // Use the last signed in account here since it already have a Drive scope.


        Task<GoogleSignInAccount> task = mGoogleSignInClient.silentSignIn();
        if (task.isSuccessful()) {
            // There's immediate result available.
            GoogleSignInAccount signInAccount = task.getResult();
            Log.i("drive by G", "Udalo sie.");
        } else {
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            Log.i("drive by G", "Nie udalo sie.");
        }



        GoogleSignInAccount asd = GoogleSignIn.getLastSignedInAccount(this);

        mDriveClient = Drive.getDriveClient(this, asd);
        // Build a drive resource client.
        mDriveResourceClient =
                Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
        // Start camera.
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);


        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        new Continuation<DriveContents, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                return createFileIntentSender(task.getResult(), image);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("drive by G", "Failed to create new contents.", e);
                            }
                        });
    }

    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new photo in Drive.
     */
    private Task<Void> createFileIntentSender(DriveContents driveContents, Bitmap image) {
        Log.i("drive by G", "New contents created.");
        // Get an output stream for the contents.
        OutputStream outputStream = driveContents.getOutputStream();
        // Write the bitmap data from it.
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        try {
            outputStream.write(bitmapStream.toByteArray());
        } catch (IOException e) {
            Log.w("drive by G", "Unable to write file contents.", e);
        }

        // Create the initial metadata - MIME type and title.
        // Note that the user will be able to change the title later.
        MetadataChangeSet metadataChangeSet =
                new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg")
                        .setTitle("Android Photo.png")
                        .build();
        // Set up options to configure and display the create file activity.
        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        new Continuation<IntentSender, Void>() {
                            @Override
                            public Void then(@NonNull Task<IntentSender> task) throws Exception {
                                startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                                return null;
                            }
                        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i("drive by G", "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i("drive by G", "Signed in successfully.");
                    // Use the last signed in account here since it already have a Drive scope.
                    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Build a drive resource client.
                    mDriveResourceClient =
                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Start camera.
                    startActivityForResult(
                            new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
            case REQUEST_CODE_CAPTURE_IMAGE:
                Log.i("drive by G", "capture image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("drive by G", "Image captured successfully.");
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    saveFileToDrive();
                }
                break;
            case REQUEST_CODE_CREATOR:
                Log.i("drive by G", "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i("drive by G", "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    startActivityForResult(
                            new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
        }
    }

}
