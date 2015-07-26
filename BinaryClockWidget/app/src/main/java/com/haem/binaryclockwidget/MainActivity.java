package com.haem.binaryclockwidget;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.concurrent.Semaphore;


public class MainActivity extends Activity {

    private Thread mThread;
    private Semaphore semaphore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        semaphore=new Semaphore(0);
        mThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(1000);
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Calendar c=Calendar.getInstance();
                    BinaryClockView v= (BinaryClockView) findViewById(R.id.clockview);
                    v.setTime ( c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),c.get(Calendar.SECOND));
                    v.postInvalidate();
                    semaphore.release();
                }
            }
        });
        mThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        semaphore.acquireUninterruptibly();
    }

    @Override
    protected void onResume() {
        super.onResume();
        semaphore.release();
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
}
