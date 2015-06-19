package com.nalamchaitanya.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLong ll;
    private String imei ;
    private List<LatLong> flist ;
    private static final String LogTag = MapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        Intent intent = this.getIntent();
        boolean temp =true;//intent.hasExtra("Extra");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        imei = sharedPreferences.getString(getString(R.string.pref_imei_key),getString(R.string.pref_imei_default));
        if(intent!=null&temp)
            ll = (LatLong) intent.getParcelableExtra("Extra");
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(17.45, 78.47)).zoom(9).build()));
    }

    @Override
    public void onMapReady(GoogleMap mMap)
    {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        drawPath();
        //showLocation();

    }

    private void showLocation()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = pref.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(ll.Lat,ll.Long)).title(location).snippet("Weather Location"));
        marker.showInfoWindow();
        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(ll.Lat,ll.Long))
                .zoom(9)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }
    private void drawPath()
    {
        FetchLocationTask task = new FetchLocationTask();
        task.execute(imei);
    }

    public class FetchLocationTask extends AsyncTask<String, Void, ArrayList<LatLong>> {

        @Override
        protected ArrayList<LatLong> doInBackground(String... params) {
            if (params.length == 0)
                return null;
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String trackStringJSON = null;

            String format = "json";
            String units = "metric";
            int numInstants = 10000;

            try {
                final String FORECAST_BASE_URL = "http://webapitest.delivision.in/API/GPS/?";
                final String QUERY_PARAM = "imei";
                final String INSTANTS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(INSTANTS_PARAM, Integer.toString(numInstants))
                        .build();
                URL url = new URL(builtUri.toString());
                //Log.v(LogTag,"URL built is:"+builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null)
                    return null;
                StringBuffer buffer = new StringBuffer();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null)
                    buffer.append(line + "\n");
                if (buffer.length() == 0)
                    return null;
                trackStringJSON = buffer.toString();
                //Log.v(LogTag,"ForeCastJSONstring"+forecastStringJSON);
                ArrayList<LatLong> resultLoc = getLocationDataFromJson(trackStringJSON, numInstants);
                return resultLoc;
            } catch (IOException e) {
                Log.e(LogTag, "Error", e);
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            } finally {
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
        protected void onPostExecute(ArrayList<LatLong> resultStrs)
        {
            flist = resultStrs;
            LatLong llt = flist.get(0);
            LatLng temp = new LatLng(llt.Lat,llt.Long);
            CameraPosition camPos = new CameraPosition.Builder()
                    .target(temp)
                    .zoom(9)
                    .build();
            //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
            Marker marker = mMap.addMarker(new MarkerOptions().position(temp).title("Start").snippet("Started Here"));
            marker.showInfoWindow();
            /*String directionsUrl = getDirectionsUrl(resultStrs);
            PlotRoute plotRoute = new PlotRoute();
            plotRoute.execute(directionsUrl);*/
            String snapToRoadsUrl = getSnapToRoadsUrl(resultStrs);
            SnapToRoad snapToRoad = new SnapToRoad();
            snapToRoad.execute(snapToRoadsUrl);
            /*PolylineOptions rectOptions = new PolylineOptions()
                    .color(Color.BLUE)
                    .width(2)
                    .geodesic(true);
            for(LatLong iter : flist)
                rectOptions.add(new LatLng(iter.Lat,iter.Long));

            Polyline polyline = mMap.addPolyline(rectOptions);
*/
        }

        private class SnapToRoad extends AsyncTask<String,Void,String>
        {

            @Override
            protected String doInBackground(String... params)
            {
                if(params.length==0)
                    return null;

                HttpURLConnection connection = null;
                BufferedReader bufferedReader = null;
                String route = "";

                try
                {
                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    if(inputStream == null)
                        return null;
                    StringBuffer buffer = new StringBuffer();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line ;
                    while ((line=bufferedReader.readLine())!=null)
                        buffer.append(line+"\n");
                    if(buffer.length()==0)
                        return null;
                    route = buffer.toString();
                    String temp = route.trim();
                    //Log.v(LogTag,"Route uri built is"+temp);
                    return temp;
                }
                catch (IOException e)
                {
                    Log.e(LogTag, "Error", e);
                    return null;
                }
                finally
                {
                    if (connection != null)
                        connection.disconnect();
                    if (bufferedReader != null)
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            Log.e(LogTag, "Error in Closing Stream", e);
                        }
                }
            }

            @Override
            protected void onPostExecute(String route)
            {
                new ParseSnapRoad().execute(route);
            }

            private class ParseSnapRoad extends AsyncTask<String,Void,ArrayList<LatLng>>
            {

                @Override
                protected ArrayList<LatLng> doInBackground(String... jsonData)
                {
                    if(jsonData.length==0)
                        return null;
                    JSONObject object = null;
                    JSONArray listOfPoints = null;
                    ArrayList<LatLng> listOfLatLng = new ArrayList<LatLng>();
                    try
                    {
                        object = new JSONObject(jsonData[0]);
                        listOfPoints = object.getJSONArray("snappedPoints");
                        for(int i =0;i<listOfPoints.length();i++)
                        {
                            JSONObject location = listOfPoints.getJSONObject(i);
                            JSONObject ll = location.getJSONObject("location");
                            listOfLatLng.add(new LatLng(ll.getDouble("latitude"),ll.getDouble("longitude")));
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    return listOfLatLng;
                }

                @Override
                protected void onPostExecute(ArrayList<LatLng> finalPoints)
                {
                    PolylineOptions rectOptions = new PolylineOptions()
                            .color(Color.BLUE)
                            .width(2)
                            .geodesic(true);
                    rectOptions.addAll(finalPoints);
                    Polyline polyline = mMap.addPolyline(rectOptions);
                }
            }
        }
        private String getSnapToRoadsUrl(ArrayList<LatLong> resultStrs)
        {
            int len = resultStrs.size();
            int breaks = len/100;
            String baseUrl = "https://roads.googleapis.com/v1/snapToRoads";
            String strIter ;
            LatLong llIter;
            String pathParam = "path=";
            for(int i = 0;i<99;i++)
            {
                llIter = resultStrs.get(i);
                strIter = llIter.Lat+","+llIter.Long+"|";
                pathParam+=strIter;
            }
            llIter = resultStrs.get(len-1);
            strIter = llIter.Lat+","+llIter.Long;
            pathParam+=strIter;
            String interpolateParam = "interpolate=true";
            String keyParam = "key="+getString(R.string.google_maps_key);

            String url = baseUrl + "?" + pathParam + "&" + interpolateParam + "&" + keyParam;
            return url;
        }
        private ArrayList<LatLong> getLocationDataFromJson(String trackJsonStr, int numInstants)
                throws JSONException
        {

            // These are the names of the JSON objects that need to be extracted.
            final String INSTANT = "Instant";
            final String LAT = "Lat";
            final String LONG = "Long";
            final String SPEED = "Speed";
            final String ACCURACY = "Accuracy";

            //JSONObject forecastJson = new JSONObject(trackJsonStr);
            JSONArray weatherArray = new JSONArray(trackJsonStr);

            ArrayList<LatLong> resultStrs = new ArrayList<LatLong>(numInstants);
            for (int i = 0; i < weatherArray.length(); i++) {
                JSONObject location = weatherArray.getJSONObject(i);

                double lat = location.getDouble(LAT);
                double lng = location.getDouble(LONG);

                resultStrs.add(new LatLong(lat,lng));
            }

            return resultStrs;
        }

        private String getDirectionsUrl(ArrayList<LatLong> resultStrs)
        {
            int len = resultStrs.size();
            int breaks = len/7;
            String wayPoints = "waypoints=optimize:true|";
            for(int i = 1;i<7;i++) {
                LatLong temp = resultStrs.get(i * breaks);
                wayPoints += temp.Lat+","+temp.Long;
                mMap.addMarker(new MarkerOptions().position(new LatLng(temp.Lat,temp.Long)).title(""+i));
                if(i!=6)
                    wayPoints+="|";
            }
            LatLong start = resultStrs.get(0);
            LatLong end = resultStrs.get(len-1);
            mMap.addMarker(new MarkerOptions().position(new LatLng(end.Lat,end.Long)).title("End")).showInfoWindow();
            String origin = "origin="+start.Lat+","+start.Long;
            String destination = "destination="+end.Lat+","+end.Long;
            String sensor = "sensor=false";
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+origin+"&"+destination+"&"+wayPoints+"&"+sensor;
            Log.v(LogTag,"uri built is : "+url);
            return url;
        }

        public class PlotRoute extends AsyncTask<String,Void,String>
        {

            @Override
            protected String doInBackground(String... params)
            {
                if(params.length==0)
                    return null;

                HttpURLConnection connection = null;
                BufferedReader bufferedReader = null;
                String route = "";

                try
                {
                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    if(inputStream == null)
                        return null;
                    StringBuffer buffer = new StringBuffer();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line ;
                    while ((line=bufferedReader.readLine())!=null)
                        buffer.append(line+"\n");
                    if(buffer.length()==0)
                        return null;
                    route = buffer.toString();
                    String temp = route.trim();
                    //Log.v(LogTag,"Route uri built is"+temp);
                    return temp;
                }
                catch (IOException e)
                {
                    Log.e(LogTag, "Error", e);
                    return null;
                }
                finally
                {
                    if (connection != null)
                        connection.disconnect();
                    if (bufferedReader != null)
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            Log.e(LogTag, "Error in Closing Stream", e);
                        }
                }
            }

            @Override
            protected void onPostExecute(String route)
            {
                new ParseRoute().execute(route);
            }

            private class ParseRoute extends AsyncTask<String,Void,List<List<HashMap<String,String>>>>
            {

                @Override
                protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
                {
                    if(jsonData.length==0)
                        return null;
                    JSONObject jsonObject = null;
                    List<List<HashMap<String,String>>> points = null;

                    try
                    {
                        jsonObject = new JSONObject(jsonData[0]);
                        PathJSONParser parser = new PathJSONParser();
                        points = parser.parse(jsonObject);
                    } catch (JSONException e) {
                        Log.e(LogTag,e.getMessage());
                    }
                    return points;
                }

                @Override
                protected void onPostExecute(List<List<HashMap<String, String>>> routes)
                {
                    ArrayList<LatLng> points = new ArrayList<LatLng>();
                    PolylineOptions polylineOptions = new PolylineOptions();
                    int len = routes.size();
                    for(int i = 0;i<len-1;i++)
                    {
                        List<HashMap<String,String>> path = routes.get(i);
                        int pathLen = path.size();
                        for(int j = 0;j<pathLen;j++)
                        {
                            HashMap<String,String> hm = path.get(j);
                            double lat = Double.parseDouble(hm.get("lat"));
                            double lng = Double.parseDouble(hm.get("lng"));
                            LatLng point = new LatLng(lat,lng);
                            points.add(point);
                        }
                    }
                    polylineOptions.addAll(points);
                    polylineOptions.width(2);
                    polylineOptions.color(Color.BLUE);
                    mMap.addPolyline(polylineOptions);
                }
            }
        }
    }
}
