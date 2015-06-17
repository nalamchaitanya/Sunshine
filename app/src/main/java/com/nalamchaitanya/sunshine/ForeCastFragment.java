package com.nalamchaitanya.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Chaitanya on 5/22/2015.
 * A placeholder fragment containing a simple view.
 */

public class ForeCastFragment extends Fragment {

    private ArrayAdapter<String> forecastAdapter;
    LatLong ll = new LatLong(17,78);
    private static final String Log_Tag = MainActivity.class.getSimpleName()+"Fragment";
    public ForeCastFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(Log_Tag,"I am in OnCreate ff2");
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater menuInflater)
    {
        menuInflater.inflate(R.menu.forecastfragment,menu);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        forecastAdapter =
                new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);
        listView.setOnItemClickListener(
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    String message = forecastAdapter.getItem(position);
                    Context context = getActivity();
                    /*int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context,message,duration);
                    toast.show();*/
                    Intent intent = new Intent(context,DetailActivity.class).putExtra(Intent.EXTRA_TEXT,message);
                    startActivity(intent);
                }
            });
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        int id = menuItem.getItemId();
        if(id==R.id.action_refresh)
        {
            updateWeather();
            return true;
        }
        else if(id==R.id.action_map)
        {
            showLocation();
            return true;
        }

       return super.onOptionsItemSelected(menuItem);
    }

    private void updateWeather()
    {
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        fetchWeatherTask.execute(location);
    }

    private void showLocation()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        /*Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if(intent.resolveActivity(getPackageManager())!=null)
            startActivity(intent);
        else
            Log.v(Log_Tag, "Couldn't show " + location + ", Relevant apps not installed");*/

        Intent intent = new Intent(getActivity(),MapActivity.class).putExtra("Extra",(Parcelable)ll);
        startActivity(intent);
    }




    @Override
    public void onStart()
    {
        super.onStart();
        Log.v(Log_Tag,"I am in OnStart ff2");
        updateWeather();
        return;
    }
    public class FetchWeatherTask extends AsyncTask<String, Void, ArrayList<String>>
    {
        private final String LogTag = FetchWeatherTask.class.getSimpleName();

        @Override
        protected ArrayList<String> doInBackground(String... params)
        {
            if(params.length==0)
                return null;
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String forecastStringJSON = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try
            {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v(LogTag,"URL built is:"+builtUri.toString());
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if(inputStream == null)
                    return null;
                StringBuffer buffer = new StringBuffer();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line=bufferedReader.readLine())!=null)
                    buffer.append(line+"\n");
                if(buffer.length()==0)
                    return null;
                forecastStringJSON = buffer.toString();
                //Log.v(LogTag,"ForeCastJSONstring"+forecastStringJSON);
                ArrayList<String> resultStrs = getWeatherDataFromJson(forecastStringJSON,numDays);
                return resultStrs;
            }
            catch (IOException e)
            {
                Log.e(LogTag, "Error", e);
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } finally
            {
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (bufferedReader != null)
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(LogTag, "Error in Closing Stream", e);
                    }
            }
        }
        @Override
        protected void onPostExecute(ArrayList<String> resultStrs)
        {
            if(resultStrs!=null)
            {
                forecastAdapter.clear();
                for(String dayForecast : resultStrs)
                    forecastAdapter.add(dayForecast);
            }
        }
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = pref.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));
            if(unitType.equals(getString(R.string.pref_units_imperical)))
            {
                low = low*1.8 +32;
                high = high*1.8 + 32;
            }
            else if(!unitType.equals(getString(R.string.pref_units_metric)))
                Log.d(LogTag,"Unit type not Found");
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<String> getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";
            final String OWM_COORD = "coord";
            final String OWM_CITY = "city";


            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            JSONObject cityDetails = forecastJson.getJSONObject(OWM_CITY);
            JSONObject location = cityDetails.getJSONObject(OWM_COORD);
            double lat = location.getDouble("lat");
            double lon = location.getDouble("lon");
            ll = new LatLong(lat,lon);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            ArrayList<String> resultStrs = new ArrayList<String>(numDays);
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);
                highAndLow = formatHighLows(high, low);

                resultStrs.add(day + " - " + description + " - " + highAndLow);
            }

            for (String s : resultStrs) {
                //Log.v(LogTag, "Forecast entry: " + s);
            }
            return resultStrs;
        }
    }
}