package com.droidcrypt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.OpenCVLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class mainActivity extends ActionBarActivity implements AccountFragment.ActivityFragmentCallback
{

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private static final int REQUEST_CODE = 1;

    //APP STATES
    private static final int SETUP = 0;     //First time app is opened
    private static final int INIT = 1;      //Default state
    private static final int EMBED = 2;     //Embed state
    private static final int EXTRACT = 3;   //Extract state


    private EmbedCaller embedCaller;
    private ExtractCaller extractCaller;
    private Bitmap bitmap;
    private AccountInfo accountInfo;
    private boolean valid;
    private int state;

    /* Android State Functions */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountInfo = AccountInfo.getInstance();

        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(R.color.indigo_500));

        //Initially No Picture selected
        valid = false;

        //Authentication
        SharedPreferences sharedPreferences = getSharedPreferences("Auth", MODE_PRIVATE);
        String hugoBits = sharedPreferences.getString("HugoBits", null);

        if (hugoBits == null)
        {
            state = SETUP;
            //Start Setup Fragment
            Setup fragment = new Setup();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
        else
        {
            state = INIT;
            //Send intent to Gallery
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE);

            Login fragment = new Login();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        setTitle("Stegosaurus");

        if (valid && state == SETUP)
        {
            //Setup
            valid = false;
            //Switch to Login fragment
            Log.d("Setup", "Switching to Login fragment");
            Login fragment = new Login();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        else if (valid && state != INIT) {
            //Main to Account Fragment
            Log.d("onResume","Switching to Account Fragment");
            AccountFragment accountList = new AccountFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, accountList).addToBackStack(null).commit();
            valid = false;
        }
        else if (!valid)
        {
            Log.d("RESUME", "No Picture Returned");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (embedCaller!= null && embedCaller.pdLoading != null) {
            embedCaller.pdLoading.dismiss();
        }
        embedCaller = null;
        if (extractCaller != null && extractCaller.loader != null) {
            extractCaller.loader.dismiss();

        }
        extractCaller = null;
        Bitmap b = AccountInfo.getInstance().getBitmap();
        if (b!= null && !b.isRecycled()) {
            b.recycle();
            b = null;
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }


    /* Buttons Clicked */
    public void onClickEmbed (View view)
    {
        state = EMBED;
        //Send intent to Gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void onClickHugoStart (View view)
    {
        //Read account + password input (new fragment)
        String name = ((EditText)findViewById(R.id.account)).getText().toString();
        String pass = ((EditText)findViewById(R.id.password)).getText().toString();
        String confirm = ((EditText)findViewById(R.id.editText_confirmPass)).getText().toString();

        if (pass.equals(confirm)) { //Passwords match
            //Set name + pass in global class
            accountInfo.setName(name);
            accountInfo.setPassword(pass);

            Log.d("EMBED", "Embedding started");
            embedCaller = new EmbedCaller();
            embedCaller.execute();

            //Embed to Main fragment
            main fragment = new main();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        else    //Passwords don't match
        {
            Toast.makeText(mainActivity.this, "Passwords don't match!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onClickExtract (View view)
    {
        state = EXTRACT;
        //Send intent to Gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void onClickBegin (View view)
    {
        //Send intent to Gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void onClickLogin (View view)
    {
        boolean login = true;
        String password = ((EditText)findViewById(R.id.userPassword)).getText().toString();

        if(state == SETUP)  //First time login, embed info
        {
            //Prep for Embed
            accountInfo.setAccountType("login");    //dummy value
            accountInfo.setName("user");            //dummy value
            accountInfo.setPassword(password);

            //Save Auth file
            SharedPreferences.Editor sharedPrefs = getSharedPreferences("Auth", MODE_PRIVATE).edit();
            String file = accountInfo.getFilePath();
            String filename = file.substring(file.lastIndexOf('/') + 1);
            sharedPrefs.putString("file", filename);
            sharedPrefs.apply();

            Log.d("Setup","Auth file: " + filename);
            embedCaller = new EmbedCaller();
            embedCaller.execute();
            embedCaller = null;
            //Login to Main fragment
            main fragment = new main();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        else    //Default Login, extract info
        {
            SharedPreferences sharedPreferences = getSharedPreferences("Auth", MODE_PRIVATE);
            String file = sharedPreferences.getString("file", null);


            String choosePath = accountInfo.getFilePath();

            if(file != null && choosePath != null)
            {
                String chosenFile = choosePath.substring(choosePath.lastIndexOf('/') + 1);

                if(file.equals(chosenFile)) {
                    accountInfo.setMasterPassword(password);
                    accountInfo.setAccountType("login");

                    Log.d("Login", "Extracting password");

                    extractCaller = new ExtractCaller();
                    extractCaller.execute();
                }
                else
                {
                    login = false;
                }
            }
            else
            {
                login = false;
            }

            if (!login) {
                Toast.makeText(mainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();

                //Send intent to Gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

//    public void onClickRetry (View view)
//    {
//        findViewById(R.id.retry).setVisibility(View.GONE);
//        findViewById(R.id.login).setVisibility(View.VISIBLE);
//
//        //Send intent to Gallery
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        startActivityForResult(intent, REQUEST_CODE);
//    }

    public void onClickCopy (View view)
    {
        CheckBox checkBox = ((CheckBox)findViewById(R.id.copy));

        if(checkBox.isChecked())
        {
            //Copy password to clipboard
            try {
                String text = ((TextView)findViewById(R.id.pass)).getText().toString();
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("password", text);
                clipboard.setPrimaryClip(clip);
            } catch (Exception e) { e.printStackTrace(); }
        }
        else
        {
            //Copy Empty string to clipboard
            try{
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("password", "");
                clipboard.setPrimaryClip(clip);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void onClickShow (View view)
    {
        CheckBox checkBox = ((CheckBox)findViewById(R.id.show));

        if(checkBox.isChecked())
        {
            ((TextView) findViewById(R.id.pass)).setTransformationMethod(null);
        }
        else
        {
            ((TextView) findViewById(R.id.pass)).setTransformationMethod(new PasswordTransformationMethod());
        }

    }


    /* Gallery Intent */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            try {
                if (bitmap!=null)
                    bitmap.recycle();

                Log.d("ActivityResult","Return from Gallery");

                //Picture Valid
                valid = true;

                //Find File Path
                Uri imageURI = data.getData();
                String result = "";
                result = FullPath.getPath(this, imageURI);

                Log.d("ActivityResult", "Full Path: " + result);
                accountInfo.setFilePath(result);

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                bitmap = BitmapFactory.decodeFile(result);

                //Set Image in global class
                accountInfo.setBitmap(bitmap);

            } catch (Exception e) { e.printStackTrace(); }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityFragmentCallback()
    {
        if (state == EMBED)
        {
            // Embed
            Embed fragment = new Embed();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        }
        else if (state == EXTRACT)
        {
            // Extract
            extractCaller = new ExtractCaller();
            extractCaller.execute();
        }
    }


    /* Async Tasks */
    private class EmbedCaller extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog pdLoading = new ProgressDialog(mainActivity.this);
        private String filename;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pdLoading.setMessage("\tEmbedding...");
            pdLoading.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pdLoading.setProgress(0);
            //pdLoading.setIndeterminate(true);
            pdLoading.show();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            BitmapFactory.Options opt= new BitmapFactory.Options();
            opt.inScaled = false;
//            opt.inSampleSize = 8;
            Bitmap input = accountInfo.getBitmap();
            final int maxTime = input.getWidth()* input.getHeight()/4000 + 1;
            //pdLoading.setMax(maxTime);
            final Thread t = new Thread(){
                @Override
                public void run(){
                    float jumpTime = 0;
                    while(jumpTime < 100){
                        try {
                            sleep(1000);
//                            pdLoading.incrementProgressBy(100.0f/(float)maxTime);
                            jumpTime += 100.0f/(float)maxTime;
                            pdLoading.setProgress((int)jumpTime);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            t.start();

//            Bitmap bitmap1= BitmapFactory.decodeResource(getResources(), R.drawable.image5, opt);

            int[] num_bits = new int[2];
            HUGO hugo = new HUGO(accountInfo.getName() + "#" + accountInfo.getPassword() + "#" + accountInfo.getAccountType(), input, num_bits);
            hugo.embed();

            //Save num_bits
            accountInfo.setHugoBits(hugo.num_bits_used);
            hugo = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            pdLoading.dismiss();

            if(state != SETUP) {
                new AlertDialog.Builder(mainActivity.this)
                        .setTitle("Saving Data")
                        .setMessage("Do you want to save your data?")
                        .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Overwrite file
                                filename = accountInfo.getFilePath();
                                SaveFile(filename, accountInfo.getBitmap());
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mainActivity.this, "Data was NOT saved", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
            else
            {
                filename = accountInfo.getFilePath();
                SaveFile(filename, accountInfo.getBitmap());
            }
        }

    }

    public void SaveFile (String file, Bitmap bitmap)
    {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);
            // PNG is a lossless format, the compression factor (100) is ignored
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            //Scan file so it shows up in Gallery
            File check = new File(file);
            SingleMediaScanner singleMediaScanner = new SingleMediaScanner(mainActivity.this, check);

            //Save Filename + bits to SharedPrefs
            SharedPreferences.Editor sharedPrefs;
            if(state == SETUP)
            {
                //Authentication
                sharedPrefs = getSharedPreferences("Auth", MODE_PRIVATE).edit();
                sharedPrefs.putString("HugoBits", accountInfo.getHugoBits()[0] + "#" + accountInfo.getHugoBits()[1]);
                sharedPrefs.apply();
                Log.d("Setup", "Logged in!");
            }
            else
            {
                sharedPrefs = getSharedPreferences("EmbedInfo", MODE_PRIVATE).edit();
                String filename = file.substring(file.lastIndexOf('/') + 1);
                String value = accountInfo.getHugoBits()[0] + "#" + accountInfo.getHugoBits()[1];
                sharedPrefs.putString(filename, value);
                sharedPrefs.apply();
                Toast.makeText(mainActivity.this, "Data saved in file: " + filename, Toast.LENGTH_LONG).show();
                Log.d("EMBED", "SharedPref: " + filename + " " + value);
            }

        } catch (Exception e) {
            Log.d("EMBED", "Bitmap File not saved!");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ExtractCaller extends AsyncTask<Void, Void, Void>
    {
        public ProgressDialog loader = new ProgressDialog(mainActivity.this);
        private boolean error = false;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loader.setMessage("\tExtracting...");
            loader.show();
        }

        @Override
        protected Void doInBackground(Void ... params)
        {
            int[] bits = new int[2];
            SharedPreferences sharedPrefs;
            String result;

            if(state == INIT)
            {
                sharedPrefs = getSharedPreferences("Auth", MODE_PRIVATE);
                result = sharedPrefs.getString("HugoBits", null);
            }
            else
            {
                sharedPrefs = getSharedPreferences("EmbedInfo", MODE_PRIVATE);
                String file = accountInfo.getFilePath();
                String filename = file.substring(file.lastIndexOf('/') + 1);
                result = sharedPrefs.getString(filename, null);
            }

            //File found
            if(result != null)
            {
                bits[0] = Integer.parseInt(result.split("#")[0]);
                bits[1] = Integer.parseInt(result.split("#")[1]);
            }
            else
            {
                error = true;
            }

            //Extract
            if (!error) {
                HUGO hugo = new HUGO("", accountInfo.getBitmap(), bits);
                String output = hugo.extract();
                if (output == null) {
                    error = true;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if(error) {
                Toast.makeText(mainActivity.this, "No Data Found!", Toast.LENGTH_SHORT).show();
            }
            else {

                if(state == INIT)
                {
                    //Compare master password to extracted password
                    if(accountInfo.getPassword().equals(accountInfo.getMasterPassword()))
                    {
                        //Switch to Main
                        main fragment = new main();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                    }
                    else
                    {
                        Toast.makeText(mainActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();

                        //Send intent to Gallery
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                }
                else
                {
                    //Main to Display fragment
                    AccountDisplay fragment = new AccountDisplay();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                }
            }

            loader.dismiss();
            accountInfo.getBitmap().recycle();
        }
    }
}
