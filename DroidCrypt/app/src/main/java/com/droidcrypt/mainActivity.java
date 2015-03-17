package com.droidcrypt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
    private HUGO hugo;

    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private String info;
    private AccountInfo accountInfo;

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

                InputStream stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();

                //Set Image in global class
                accountInfo.setBitmap(bitmap);

                //Find File Path
                Uri selectedImageURI = data.getData();
                accountInfo.setFilePath(getRealPathFromURI(selectedImageURI));


            } catch (Exception e) { e.printStackTrace(); }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Convert URI into Full File Path
    private String getRealPathFromURI(Uri contentURI)
    {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(contentURI);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();

        Log.d("File","Full File Path: " + filePath);
        return filePath;
    }

    public void onClickExtract (View view)
    {
        extractCaller = new ExtractCaller();
        extractCaller.execute();

        //Switch to Display fragment
        AccountDisplay fragment = new AccountDisplay();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    @Override
     public void onDestroy() {
        super.onDestroy();
        embedCaller.pdLoading.dismiss();
        embedCaller = null;
        extractCaller.loader.dismiss();
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
            pdLoading.setMessage("\tLoading...");
            pdLoading.show();
        }
        @Override
        protected Void doInBackground(Void... params)
        {
            BitmapFactory.Options opt= new BitmapFactory.Options();
            opt.inScaled = false;
            opt.inSampleSize = 8;
            Bitmap input = accountInfo.getBitmap();
            //Bitmap = BitmapFactory.decodeResource(getParent().getResources().R.)
            hugo = new HUGO(accountInfo.getName() + "####" + accountInfo.getPassword(), input);
            hugo.embed();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            pdLoading.dismiss();

            new AlertDialog.Builder(mainActivity.this)
                    .setTitle("Save Image")
                    .setMessage("Do you want to overwrite the original image?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Overwrite file
                            filename = accountInfo.getFilePath();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Create New file
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                            String date = dateFormat.format(new Date());
                            String file = accountInfo.getFilePath();
                            filename = file.substring(0, file.length() - 4) + date + ".jpg";

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert).show();

            //Save File
            Bitmap save = HUGO.convertColorHSVColor(accountInfo.getBitmap());
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filename);
                save.compress(Bitmap.CompressFormat.PNG, 100, out); // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
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

    }

    private class ExtractCaller extends AsyncTask<Void, Void, Void>
    {
        public ProgressDialog loader = new ProgressDialog(mainActivity.this);

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
            info = hugo.extract();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            ((TextView)findViewById(R.id.account)).setText(info);
            loader.dismiss();
        }
    }
}
