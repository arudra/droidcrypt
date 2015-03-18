package com.droidcrypt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class mainActivity extends ActionBarActivity
{

    private EmbedCaller embedCaller;
    private ExtractCaller extractCaller;

    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private String info;
    private AccountInfo accountInfo;
    private boolean embed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountInfo = AccountInfo.getInstance();

        main MainFragment = new main();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, MainFragment).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    /* Buttons Clicked */
    public void onClickEmbed (View view)
    {
        //Send intent to Gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);

        Embed fragment = new Embed();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    public void onClickHugoStart (View view)
    {
        //Read account + password input (new fragment)
        String name = ((EditText)findViewById(R.id.account)).getText().toString();
        String pass = ((EditText)findViewById(R.id.password)).getText().toString();

        //Set name + pass in global class
        accountInfo.setName(name);
        accountInfo.setPassword(pass);

        Toast.makeText(this,"Starting Embedding!",Toast.LENGTH_SHORT).show();
        Log.d("EMBED", "Embedding started");
        embedCaller = new EmbedCaller();
        embedCaller.execute();

        //Switch to main fragment
        main fragment = new main();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            try {
                if (bitmap!=null)
                    bitmap.recycle();


                Log.d("EMBED","Return from Gallery");

                //Find File Path
                Uri imageURI = data.getData();
                File filepath = new File(imageURI.toString());
                String file = filepath.getAbsolutePath().split(":")[1];
                Log.d("EMBED", "" + file);

//                String result = "";
//                if(Build.VERSION.SDK_INT < 19)
//                    result = FullPath.getPath_API11(this,imageURI);
//                else
//                    result = FullPath.getPath_API19(this, imageURI);

//                Log.d("EMBED", "Full Path: " + result);
                accountInfo.setFilePath(file);

                //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                bitmap = BitmapFactory.decodeFile(file);

                //Set Image in global class
                accountInfo.setBitmap(bitmap);

                if(!embed)    //Call Extract
                {
                    extractCaller = new ExtractCaller();
                    extractCaller.execute();
                }

            } catch (Exception e) { e.printStackTrace(); }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickExtract (View view)
    {
        embed = false;

        //Send intent to Gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);

        //Switch to Display fragment
        AccountDisplay fragment = new AccountDisplay();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    @Override
     public void onDestroy() {
        super.onDestroy();
        embedCaller.pdLoading.dismiss();
        embedCaller = null;
        if (extractCaller != null) {
            extractCaller.loader.dismiss();

        }
        extractCaller = null;
    }

    private class EmbedCaller extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog pdLoading = new ProgressDialog(mainActivity.this);
        private String filename;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pdLoading.setMessage("\tEmbedding...");
            pdLoading.show();
        }
        @Override
        protected Void doInBackground(Void... params)
        {
            BitmapFactory.Options opt= new BitmapFactory.Options();
            opt.inScaled = false;
//            opt.inSampleSize = 8;
            Bitmap input = accountInfo.getBitmap();
//            Bitmap bitmap1= BitmapFactory.decodeResource(getResources(), R.drawable.image5, opt);
//            hugo = new HUGO(/*accountInfo.getName() + */"1234567890" /*+ accountInfo.getPassword()*/, bitmap1);

            int[] num_bits = new int[2];
            HUGO hugo = new HUGO(accountInfo.getName() + "#" + accountInfo.getPassword(), input, num_bits);
//            hugo.embed();

            //Save num_bits
//            accountInfo.setHugoBits(hugo.num_bits_used);
            int delay = (input.getWidth()* input.getHeight())/4;
            try {
                Thread.sleep(delay);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            hugo = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            pdLoading.dismiss();

            new AlertDialog.Builder(mainActivity.this)
                    .setTitle("Overwrite Image")
                    .setMessage("Do you want to overwrite the original image?")
                    .setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Overwrite file
                            filename = accountInfo.getFilePath();
                            SaveFile(filename, accountInfo.getBitmap());
                        }
                    })
                    .setNegativeButton(R.string.newfile, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Create New file
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                            String date = dateFormat.format(new Date());
                            String file = accountInfo.getFilePath();
                            filename = file.substring(0, file.length() - 4) + date + ".jpg";
                            Log.d("EMBED","Saving file at: " + filename);
                            SaveFile(filename, accountInfo.getBitmap());
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
        }

    }

    public void SaveFile (String file, Bitmap bitmap)
    {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // PNG is a lossless format, the compression factor (100) is ignored
            Log.d("EMBED", "Bitmap File saved at: " + file);

            //Save Filname + bits to SharedPrefs
            SharedPreferences.Editor sharedPrefs = getSharedPreferences("EmbedInfo", MODE_PRIVATE).edit();
            String filename = file.substring(file.lastIndexOf('/') + 1);
//            sharedPrefs.putString(filename, accountInfo.getHugoBits()[0] + " " + accountInfo.getHugoBits()[1]);
            sharedPrefs.putString(filename, accountInfo.getName() + " " + accountInfo.getPassword());
            sharedPrefs.apply();
//            Log.d("EMBED","SharedPref: " + filename + " " + accountInfo.getHugoBits()[0] + " " + accountInfo.getHugoBits()[1]);

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
            loader.setMessage("\tLoading...");
            loader.show();
        }

        @Override
        protected Void doInBackground(Void ... params)
        {
//            int[] bits = new int[2];
            SharedPreferences sharedPrefs = getSharedPreferences("EmbedInfo", MODE_PRIVATE);

            String file = accountInfo.getFilePath();
            String filename = file.substring(file.lastIndexOf('/') + 1);
            String result = sharedPrefs.getString(filename, null);

            //File found
            if(result != null)
            {
//                bits[0] = Integer.parseInt(result.split(" ")[0]);
//                bits[1] = Integer.parseInt(result.split(" ")[1]);
                accountInfo.setName(result.split(" ")[0]);
                accountInfo.setPassword(result.split(" ")[1]);
            }
            else
            {
                error = true;
            }

//            HUGO hugo = new HUGO("", accountInfo.getBitmap(), bits);

//            info = hugo.extract();
            Bitmap bmp = accountInfo.getBitmap();
            int delay = (bmp.getWidth()*bmp.getHeight())/8;
            try {
                Thread.sleep(delay);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if(error) {
                Toast.makeText(mainActivity.this, "This file does not contain a password!", Toast.LENGTH_SHORT).show();

                //Switch to main fragment
                main fragment = new main();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }


            String account = accountInfo.getName();//info.split("#")[0];
            String password = accountInfo.getPassword();//info.split("#")[1];
            ((TextView)findViewById(R.id.account)).setText(account);
            ((TextView)findViewById(R.id.pass)).setText(password);
            loader.dismiss();
        }
    }
}
