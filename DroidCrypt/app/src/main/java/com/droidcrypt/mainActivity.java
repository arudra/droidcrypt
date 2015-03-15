package com.droidcrypt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;


public class mainActivity extends ActionBarActivity {

    /*
    private AsyncCaller async;
    private HUGO hugo;
    private ImageView originalImage;
    private ImageView newImage;
    */

    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main MainFragment = new main();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, MainFragment).commit();

        /*
        async = new AsyncCaller();
        async.execute();
        originalImage = (ImageView)findViewById(R.id.imageView);
        newImage = (ImageView)findViewById(R.id.imageView2);
        Button toggle = (Button) findViewById(R.id.btn_toggle);
        toggle.setTag(Integer.valueOf(0)); */
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

        //Switch to display fragment (call constructor with image + info)
        AccountDisplay fragment = new AccountDisplay();
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
                AccountInfo accountInfo = AccountInfo.getInstance();
                accountInfo.setBitmap(bitmap);

                //Get info from global class
                String name = accountInfo.getName();
                String pass = accountInfo.getPassword();

                ImageView view = (ImageView)findViewById(R.id.display);
                view.setImageBitmap(bitmap);

                ((TextView)findViewById(R.id.account)).setText("Account: " + name);
                ((TextView)findViewById(R.id.password)).setText("Password: " + pass);

            } catch (Exception e) { e.printStackTrace(); }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickExtract (View view)
    {
        AccountDisplay fragment = new AccountDisplay();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }


    /*
    @Override
     public void onDestroy() {
        super.onDestroy();
        //async.pdLoading.dismiss();
        //async = null;
    }

    public void onClick_btnToggle(View v) {
        Integer tag = (Integer) v.getTag();
        if (tag == 0) {
            originalImage.setImageBitmap(hugo.origImage);
            tag = 1;
        } else {
            originalImage.setImageBitmap(hugo.convertColorHSVColor(hugo.origImage));
            tag = 0;
        }
        v.setTag(tag);
    }


    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        public ProgressDialog pdLoading = new ProgressDialog(mainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            BitmapFactory.Options opt= new BitmapFactory.Options();
            opt.inScaled = false;
            //opt.inSampleSize = 8;
            Bitmap input = BitmapFactory.decodeResource(getResources(), R.drawable.image5, opt);
            hugo = new HUGO("", "Hello World!", input, getParent());
//            hugo.execute();
            hugo.testNdkCall();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //this method will be running on UI thread
            originalImage.setImageBitmap(hugo.origImage);
            newImage.setImageBitmap(hugo.convertColorHSVColor(hugo.origImage));
            pdLoading.dismiss();

        }

    } */
}
