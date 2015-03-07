package com.droidcrypt;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class mainActivity extends ActionBarActivity {

    private AsyncCaller async;
    private HUGO hugo;
    private ImageView originalImage;
    private ImageView newImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        async = new AsyncCaller();
        async.execute();
        originalImage = (ImageView)findViewById(R.id.imageView);
        newImage = (ImageView)findViewById(R.id.imageView2);
        Button toggle = (Button) findViewById(R.id.btn_toggle);
        toggle.setTag(Integer.valueOf(0));
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
    @Override
     public void onDestroy() {
        super.onDestroy();
        async.pdLoading.dismiss();
        async = null;
    }

    public void onClick_btnToggle(View v) {
        Integer tag = (Integer) v.getTag();
        if (tag == 0) {
            originalImage.setImageBitmap(hugo.origImage);
            tag = 1;
        } else {
            originalImage.setImageBitmap(hugo.grayImage);
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
            newImage.setImageBitmap(hugo.grayImage);
            pdLoading.dismiss();

        }

    }
}
