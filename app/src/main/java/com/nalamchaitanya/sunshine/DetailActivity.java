package com.nalamchaitanya.sunshine;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private final String Log_Tag = DetailActivity.class.getSimpleName();
        private static final String Forecast_Share_Hashing = " #SunshineApp";
        private String forecast ;
        public DetailFragment()
        {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if(intent!=null&intent.hasExtra(Intent.EXTRA_TEXT))
            {
                forecast = intent.getStringExtra(Intent.EXTRA_TEXT);
                TextView textView = (TextView)rootView.findViewById(R.id.detail_forecast);
                textView.setText(forecast);
            }
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
        {
            inflater.inflate(R.menu.detail_fragment,menu);
            MenuItem item = menu.findItem(R.id.action_share);
            ShareActionProvider detailShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            if(detailShareActionProvider!=null)
                detailShareActionProvider.setShareIntent(createShareIntent());
            else
                Log.d(Log_Tag,"Share action provider is null");

        }
        private Intent createShareIntent()
        {
            Intent intentShare = new Intent(Intent.ACTION_SEND);
            intentShare.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intentShare.setType("text/plain");
            intentShare.putExtra(Intent.EXTRA_TEXT,forecast+Forecast_Share_Hashing);
            return intentShare;
        }
    }
}
