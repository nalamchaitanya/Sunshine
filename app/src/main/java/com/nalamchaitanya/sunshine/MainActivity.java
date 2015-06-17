package com.nalamchaitanya.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import java.io.Serializable;


public class MainActivity extends ActionBarActivity {

    private final String Log_Tag = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.v(Log_Tag,"I am in OnCreate ff");
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForeCastFragment())
                    .commit();
        }
    }


/*    protected void onStart()
    {
        super.onStart();
        Log.v(Log_Tag,"I am in OnStart ff");
    }

    protected void onStop()
    {
        super.onStop();
        Log.v(Log_Tag,"I am in OnStop ff");
    }

    protected void onDestroy()
    {
        super.onDestroy();
        Log.v(Log_Tag,"I am in OnDestroy ff");
    }

    protected void onPause()
    {
        super.onPause();
        Log.v(Log_Tag,"I am in OnPause ff");
    }

    protected void onResume()
    {
        super.onResume();
        Log.v(Log_Tag,"I am in OnResume ff");
    }*/
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

            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }



}
