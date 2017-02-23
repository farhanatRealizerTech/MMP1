package realizer.com.makemepopular.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.introscreen.WelcomeActivity;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.utils.Utility;

/**
 * Created by shree on 1/23/2017.
 */
public class AutoSyncService extends Service implements OnTaskCompleted,com.google.android.gms.location.LocationListener ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    SharedPreferences sharedpreferences;
    String userid;
    LocationManager locationmanager;
    static Double currentLat,currentLog;
    String provider;
    String CityName="";

    //test location
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userid = sharedpreferences.getString("UserId", "");

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();


        String firstTime = intent.getStringExtra("FirstTime");
        int sec=10000;
        if (firstTime.equals("1"))
        {
            sec=1000;
        }

        int total_min=30000;
        Timer timer = new Timer();

//        Criteria cri = new Criteria();
//        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        provider = locationmanager.getBestProvider(cri, false);

        timer.scheduleAtFixedRate(new AutoSyncServerDataTrack(), sec, total_min);
        return START_NOT_STICKY;
    }

    @Override
     public void onTaskCompleted(String s) {
        if (!s.equalsIgnoreCase(""))
        {
            try {
                JSONObject json = new JSONObject(s);
                String success=json.getString("success");
                String todaysTrackingCount=json.getString("todaysTrackingCount");
                String lastWeekTrackingCount=json.getString("lastWeekTrackingCount");
                String lastMonthTrackingCount=json.getString("lastMonthTrackingCount");

                if (success.equalsIgnoreCase("true"))
                {
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(AutoSyncService.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("Success", success);
                    edit.putString("TodaysTrackingCount", todaysTrackingCount);
                    edit.putString("LastWeekTrackingCount",lastWeekTrackingCount);
                    edit.putString("LastMonthTrackingCount",lastMonthTrackingCount);
                    edit.commit();
                    Log.d("AutoService","End");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
       // mGoogleApiClient.connect();
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
        startLocationUpdate();
    }

    public void startLocationUpdate() {
        initLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void handleNewLocation(Location location) {
        currentLat = location.getLatitude();
        currentLog = location.getLongitude();
        getLocationCityName(currentLat,currentLog);
        //String cityName = getLocationCityName(currentLat,currentLog);
        Log.d("LAtLog=",""+currentLat+","+currentLog);

        //Toast.makeText(this,""+currentLat+","+currentLog,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        if (connectionResult.hasResolution()) {
//            if (mGoogleApiClient.isConnected()) {
//                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,AutoSyncService.this);
//                mGoogleApiClient.disconnect();
//            }
//        } else {
//            /*
//             * If no resolution is available, display a dialog to the
//             * user with the error.
//             */
//        }
    }



   /* @Override
    public void onResume() {

        mGoogleApiClient.connect();
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,RegistrationActivity.this);
            mGoogleApiClient.disconnect();
        }
    }*/

    class AutoSyncServerDataTrack extends TimerTask
    {
        @Override
        public void run() {
            if(isConnectingToInternet()) {
                Log.d("AutoService","Start");
                initializeTimerTask();
            }
        }
    }

    public boolean isConnectingToInternet(){

        ConnectivityManager connectivity =
                (ConnectivityManager) getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    public void  initializeTimerTask() {
        Log.d("AutoSync", "Start");

        if (currentLat != null) {
            Log.d("latitude",String.valueOf(currentLat));
            Log.d("longitude",String.valueOf(currentLog));
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(AutoSyncService.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("CurrentLatitudeEmergency",String.valueOf(currentLat));
            edit.putString("CurrentLongitudeEmergency",String.valueOf(currentLog));
            edit.commit();
            AutoSynckSetCordinatesTask auto=new AutoSynckSetCordinatesTask(currentLat,currentLog,AutoSyncService.this,AutoSyncService.this);
            auto.execute();
            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        }
    }

    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm=null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)

            lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    /*public String getLocationName(double latitude, double longitude) {

        String cityName = "";

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();
            cityName = address+","+addresses.get(0).getAddressLine(1)+","+city+","+country;
        } catch (IOException e) {
            if (Config.isConnectingToInternet(AutoSyncService.this))
            {
                //cityName=getLocationCityName(latitude,longitude);
            }
            else
            {
               // Utility.CustomToast(EmergencyActivity.this, "No Internet Connection..!");
            }

            e.printStackTrace();
        }


        return cityName;

    }*/

    public void getLocationCityName( double lat, double lon ){
        getLocationAsyncTask getlocation=new getLocationAsyncTask(lat,lon);
        getlocation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class getLocationAsyncTask extends AsyncTask<Void,Void,JSONObject>
    {
        double lat;
        double lon;

        public getLocationAsyncTask(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject result = getLocationFormGoogle(lat + "," + lon );
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject s) {
            super.onPostExecute(s);
            CityName=getCityAddress(s);
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(AutoSyncService.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("CurrentLatitudeTrackNot",String.valueOf(currentLat));
            edit.putString("CurrentLongitudeTrackNot",String.valueOf(currentLog));
            edit.putString("CurrentCity",CityName);
            edit.commit();
        }
    }
    protected static JSONObject getLocationFormGoogle(String placesName)
    {
        String apiRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + placesName; //+ "&ka&sensor=false"
        HttpGet httpGet = new HttpGet(apiRequest);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }

        return jsonObject;
    }

    protected static String getCityAddress( JSONObject result ){
        if( result.has("results") ){
            try {
                JSONArray array = result.getJSONArray("results");
                if( array.length() > 0 ){
                    JSONObject place = array.getJSONObject(0);
                    JSONArray components = place.getJSONArray("address_components");
                    for( int i = 0 ; i < components.length() ; i++ ){
                        JSONObject component = components.getJSONObject(i);
                        JSONArray types = component.getJSONArray("types");
                        for( int j = 0 ; j < types.length() ; j ++ ){
                            if( types.getString(j).equals("locality") ){
                                return component.getString("long_name");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


}
