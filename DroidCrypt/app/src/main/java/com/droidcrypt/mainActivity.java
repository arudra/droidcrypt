package com.droidcrypt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;


public class mainActivity extends ActionBarActivity
{

    private EmbedCaller embedCaller;
    private ExtractCaller extractCaller;
    private HUGO hugo;

    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;
    private boolean select = false;
    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Embed fragment = new Embed();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    public void onClickImage (View view)
    {
        //Read account + password input (new fragment)
        String name = ((EditText)findViewById(R.id.account)).getText().toString();
        String pass = ((EditText)findViewById(R.id.password)).getText().toString();

        //Set name + pass in global class
        AccountInfo accountInfo = AccountInfo.getInstance();
        accountInfo.setName(name);
        accountInfo.setPassword(pass);

        //Send intent to Gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);

        select = true;
    }

    public void onClickHugoStart (View view)
    {
        if (select)
        {
            Toast.makeText(this,"Starting Embedding!",Toast.LENGTH_SHORT).show();
            embedCaller = new EmbedCaller();
            embedCaller.execute();

            //Switch to main fragment
            main fragment = new main();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        }
        else
            Toast.makeText(this,"Image must be selected first!",Toast.LENGTH_SHORT).show();
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
                AccountInfo accountInfo = AccountInfo.getInstance();
                accountInfo.setBitmap(bitmap);

                ((TextView)findViewById(R.id.account)).setText("Account: " + accountInfo.getName());
                ((TextView)findViewById(R.id.password)).setText("Password: " + accountInfo.getPassword());

            } catch (Exception e) { e.printStackTrace(); }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        public ProgressDialog pdLoading = new ProgressDialog(mainActivity.this);

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
            //opt.inSampleSize = 8;
            AccountInfo accountInfo = AccountInfo.getInstance();
            Bitmap input = accountInfo.getBitmap();

            hugo = new HUGO(accountInfo.getName() + " " + accountInfo.getPassword(), input);
            hugo.embed();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            pdLoading.dismiss();
            Toast.makeText(mainActivity.this,"Finished Embedding!",Toast.LENGTH_SHORT).show();
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
