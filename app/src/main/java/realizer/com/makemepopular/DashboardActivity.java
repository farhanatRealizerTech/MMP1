package realizer.com.makemepopular;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import realizer.com.makemepopular.addfriend.AddFriendModel;
import realizer.com.makemepopular.addfriend.AddFriendModelAdapter;
import realizer.com.makemepopular.asynctask.AcceptRejectFriendRequestAsyntask;
import realizer.com.makemepopular.asynctask.DeregisterUserAsyncTask;
import realizer.com.makemepopular.asynctask.EmergencyAcknowledgementAsynctask;
import realizer.com.makemepopular.asynctask.GetEmergencyContactsAsynctask;
import realizer.com.makemepopular.asynctask.GetFriendListAsynTask;
import realizer.com.makemepopular.asynctask.GetFriendListByNameAsyncTask;
import realizer.com.makemepopular.asynctask.GetInterestListAsyncTask;
import realizer.com.makemepopular.asynctask.SendEmergencyAlertAsyncTask;
import realizer.com.makemepopular.asynctask.TrackFriendAsynctask;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.ChatThreadListActivity;
import realizer.com.makemepopular.chat.asynctask.ChatUserListAsyncTaskGet;
import realizer.com.makemepopular.chat.model.NewFriendListModel;
import realizer.com.makemepopular.emergency.EmergencyActivity;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.friendlist.FriendListActivity;
import realizer.com.makemepopular.friendlist.adapter.FriendListModelAdapter;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.friendnear.NearFriendSpinnerAdapter;
import realizer.com.makemepopular.models.NearByFriends;
import realizer.com.makemepopular.neaybyplaces.NearyByPlacesActivity;
import realizer.com.makemepopular.notifications.Notification_activity;
import realizer.com.makemepopular.service.TrackShowMap;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.GradientBackgroundPainter;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 12/01/2017.
 */
public class DashboardActivity extends AppCompatActivity implements OnTaskCompleted,com.google.android.gms.location.LocationListener ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,ActivityCompat.OnRequestPermissionsResultCallback
{

    DatabaseQueries qr;
    BarChart barChart;
    private GradientBackgroundPainter gradientBackgroundPainter;
    TextView ico_emergency,ico_friendnear,ico_addfrnd,ico_nearbyplace,ico_invitefrnd,ico_frndlist,noName,noIntrest,txt_dash_chatico,txt_dash_albumico,txt_dash_notifico;
    ListView lvFriendLIst,lvfriendlistbyinterest;
    ArrayList<AddFriendModel> searchnameFrndlist=new ArrayList<>();
    String[] spnlist;
    static String getInterest;
    static FriendListModel selectedView;
    LocationManager locationmanager;
    static Double currentLat,currentLog;
    String provider;
    TextView tvbackbtn,txt_nearfrnd_no;
    ProgressWheel loading,loadingemergeny,loadingtrack,loadingDashboard;
    Spinner spnfrndlist;
    LinearLayout friendsouter,possibilitesouter,friendbtnouter,linear_nearfriends_nofriends;
    ListView list_addEmergncyfrnd;
    ArrayList<FriendListModel> friendlist=new ArrayList<>();
    FriendListModelAdapter friendadapter;
    int progressDistance=25;
    String todaysTrackingCount="0";
    String lastWeekTrackingCount="0";
    String lastMonthTrackingCount="0";
    MessageResultReceiver resultReceiver;
    ArrayList<String> selectedInterestedList;
    String UserId="";
    ArrayList<FriendListModel> newfrdlist;
    ArrayList<NewFriendListModel> newfrdlist1;

    //test location
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final static int MY_PERMISSION_REQUEST_READ_FINE_LOCATION= 100;

    @Override
    protected void onResume() {
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
        todaysTrackingCount=sharedpreferences.getString("TodaysTrackingCount", "0");
        lastWeekTrackingCount=sharedpreferences.getString("LastWeekTrackingCount", "0");
        lastMonthTrackingCount=sharedpreferences.getString("LastMonthTrackingCount", "0");
        HorizontalData();
        //getaddress();
        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
        mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.dashboard_activity);

        getSupportActionBar().setTitle("Find Me Friends");

        qr=new DatabaseQueries(this);
        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);
        loadingDashboard= (ProgressWheel)findViewById(R.id.loadingDashboard);
        ico_emergency= (TextView) findViewById(R.id.txt_dash_emergencyico);
        ico_friendnear= (TextView) findViewById(R.id.txt_dash_friendnearico);
        ico_addfrnd= (TextView) findViewById(R.id.txt_dash_addfriendico);
        ico_nearbyplace= (TextView) findViewById(R.id.txt_dash_nearybyplaceico);
        ico_invitefrnd= (TextView) findViewById(R.id.txt_dash_invitefrndico);
        ico_frndlist = (TextView) findViewById(R.id.txt_dash_frndlistico);
        txt_dash_chatico= (TextView) findViewById(R.id.txt_dash_chatico);
        txt_dash_albumico= (TextView) findViewById(R.id.txt_dash_albumico);
        txt_dash_notifico = (TextView) findViewById(R.id.txt_dash_notifico);

        ico_emergency.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_friendnear.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_addfrnd.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_nearbyplace.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_invitefrnd.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_frndlist.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        txt_dash_chatico.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        txt_dash_albumico.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        txt_dash_notifico.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));


        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission())
            {
                requestForSpecificPermission();
            }
        }


        barChart = (BarChart) findViewById(R.id.chart);
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
        todaysTrackingCount=sharedpreferences.getString("TodaysTrackingCount", "0");
        lastWeekTrackingCount=sharedpreferences.getString("LastWeekTrackingCount", "0");
        lastMonthTrackingCount=sharedpreferences.getString("LastMonthTrackingCount", "0");
        HorizontalData();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("AppStatus", "Open");
        edit.commit();


        checkLocationService chklocation=new checkLocationService(DashboardActivity.this);
        chklocation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

//        Criteria cri = new Criteria();
//        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        provider = locationmanager.getBestProvider(cri, false);

        UserId=sharedpreferences.getString("UserId", "");
        String isFriestLogin = sharedpreferences.getString("FirstTimeLoginThread", "");
        if (isFriestLogin.equals("true"))
        {
            edit.putString("FirstTimeLoginThread", "false");
            edit.commit();
            if (Config.isConnectingToInternet(DashboardActivity.this))
            {
                String time="";
                int count=qr.chekFriendTable();
                if(count>0)
                {
                    time=qr.getCreateTsFriend();
                }
                else{
                    time="";
                }
                ChatUserListAsyncTaskGet getUserList = new ChatUserListAsyncTaskGet(DashboardActivity.this, DashboardActivity.this,time);
                getUserList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                Utility.CustomToast(DashboardActivity.this, "No Internet Connection..!");
                //Config.alertDialog(DashboardActivity.this, "Network Error", "No Internet Connection..!");
            }
        }

        if (Config.isConnectingToInternet(DashboardActivity.this)) {
            GetInterestListAsyncTask check=new GetInterestListAsyncTask(DashboardActivity.this,DashboardActivity.this);
            check.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            Utility.CustomToast(DashboardActivity.this, "No Internet Connection..!");
            //Config.alertDialog(DashboardActivity.this, "Network Error", "No Internet Connection..!");
        }

        String notType=sharedpreferences.getString("Type", "");
        if (notType.equalsIgnoreCase("FriendRequest"))
        {
            String reqstName=sharedpreferences.getString("RequsetByName", "");
            String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
            Config.showacceptrejectFriendRequest(reqstName,thumbnail,DashboardActivity.this);
        }
        else if (notType.equalsIgnoreCase("Emergency"))
        {
            String msg=sharedpreferences.getString("Message", "");
            String newMsg="Hey Buddy,"+""+msg;
            String trobler=sharedpreferences.getString("TroublerName", "");
            String troblerid=sharedpreferences.getString("TroublerUserId", "");
            Config.showEmergencyAcceptReject(newMsg,trobler,troblerid,DashboardActivity.this);
            new MyFirebaseMessagingService().setCountZero("Emergency");
        }
        else if (notType.equalsIgnoreCase("EmergencyRecipt"))
        {
            String msg=sharedpreferences.getString("Message", "");
            String helpername=sharedpreferences.getString("HelperUserName", "");
            String isResch=sharedpreferences.getString("isReaching", "");
            if (isResch.equalsIgnoreCase("true"))
                Config.showEmergencyAckAlert(msg, helpername,DashboardActivity.this);
        }
        else if (notType.equalsIgnoreCase("TrackingStarted"))
        {
            new MyFirebaseMessagingService().setCountZero("TrackingStarted");
        }
        else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
        {
            String msg=sharedpreferences.getString("Message", "");
            String helpername=sharedpreferences.getString("AcceptByName", "");

            Config.showAccptedRequestAlert(msg,helpername,DashboardActivity.this);
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(DashboardActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(DashboardActivity.this,
                        new String[]{ android.Manifest.permission.READ_CONTACTS},
                        MY_PERMISSION_REQUEST_READ_FINE_LOCATION);

                // MY_PERMISSION_REQUEST_READ_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

}
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void HorizontalData()
    {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(Integer.valueOf(todaysTrackingCount), 0));
        //entries.add(new BarEntry(0, 1));
        entries.add(new BarEntry(Integer.valueOf(lastWeekTrackingCount), 1));
        entries.add(new BarEntry(Integer.valueOf(lastMonthTrackingCount), 2));

        BarDataSet dataset = new BarDataSet(entries, "");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Today");
        //labels.add("Yesterday");
        labels.add("Last Week");
        labels.add("Last Month");

        BarData data = new BarData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.setData(data);
        barChart.setDescription("");
        barChart.animateY(5000);
    }

    public void EmergencyClick(View v)
    {
        if (currentLat == null )
        {
            Location locatin= UtilLocation.getLastKnownLoaction(true, DashboardActivity.this);
            if (locatin!=null)
            {
                currentLat=locatin.getLatitude();
                currentLog=locatin.getLongitude();

                Intent intent=new Intent(DashboardActivity.this, EmergencyActivity.class);
                Bundle bundle=new Bundle();
                bundle.putDouble("CURRENTLAT",currentLat);
                bundle.putDouble("CURRENTLOG",currentLog);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            else
            {
                showSettingsAlert();
            }
        }
        else
        {
            Intent intent=new Intent(DashboardActivity.this, EmergencyActivity.class);
            Bundle bundle=new Bundle();
            bundle.putDouble("CURRENTLAT",currentLat);
            bundle.putDouble("CURRENTLOG",currentLog);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void Chatting(View v)
    {
        Intent intent=new Intent(DashboardActivity.this, ChatThreadListActivity.class);
        startActivity(intent);
    }

    public void FriendListClick(View v)
    {
        Intent friendlist=new Intent(DashboardActivity.this, FriendListActivity.class);
        startActivity(friendlist);
    }

    public void AddFriendClick(View v)
    {
        AddFriendAlertDialog();
    }

    public void InviteToFriendClick(View v)
    {
        InviteFriendDialog();
    }

    public void NearByPlaceClick(View v)
    {
        Intent nearbyplace=new Intent(DashboardActivity.this, NearyByPlacesActivity.class);
        startActivity(nearbyplace);
    }

    public void PhotoAlbumClick(View v)
    {
        Utility.CustomToast(DashboardActivity.this, "In Progress....!");
    }
    public void FriendNearClick(View v)
    {
        LayoutInflater inflater= this.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.nearfriend_alertdialog, null);
        final Button btnFriends= (Button) dialoglayout.findViewById(R.id.btn_nearfrnds_friend);
        final Button btnPossibilition= (Button) dialoglayout.findViewById(R.id.btn_nearfrnds_possibilities);
        friendsouter= (LinearLayout) dialoglayout.findViewById(R.id.linear_nearfriends_friends);
        possibilitesouter= (LinearLayout) dialoglayout.findViewById(R.id.linear_nearfriends_posibilities);
        linear_nearfriends_nofriends= (LinearLayout) dialoglayout.findViewById(R.id.linear_nearfriends_nofriends);
        txt_nearfrnd_no= (TextView) dialoglayout.findViewById(R.id.txt_nearfrnd_no);
        friendbtnouter= (LinearLayout) dialoglayout.findViewById(R.id.linear_btnOuter);
        tvbackbtn= (TextView) dialoglayout.findViewById(R.id.txt_nearby_backbtn);
        loadingtrack =(ProgressWheel) dialoglayout.findViewById(R.id.loadingtrack);
        final TextView track= (TextView) dialoglayout.findViewById(R.id.btn_nearfrnd_all);
        track.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        TextView tvClosebtn= (TextView) dialoglayout.findViewById(R.id.txt_invitfrnd_closebtn);
        tvClosebtn.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        TextView btn_interst= (TextView) dialoglayout.findViewById(R.id.btn_nearfrnd_interest);
        btn_interst.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        tvbackbtn.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        final Spinner spninterestlst= (Spinner) dialoglayout.findViewById(R.id.spn_nearfrnd_interestlist);
        SeekBar spinrangelst= (SeekBar) dialoglayout.findViewById(R.id.seekbar);
        final TextView SeekCount= (TextView) dialoglayout.findViewById(R.id.seek_count);

        spnfrndlist= (Spinner) dialoglayout.findViewById(R.id.spn_nearfrnd_frndlist);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        final AlertDialog alertDialog = builder.create();

        newfrdlist=new ArrayList<>();
        newfrdlist1=new ArrayList<>();

        newfrdlist1=qr.getAllFriendsData();


        if (newfrdlist1!=null) {
            for (int i = 0; i < newfrdlist1.size(); i++) {
                FriendListModel model = new FriendListModel();
                if (newfrdlist1.get(i).getStatus().equalsIgnoreCase("Accepted")) {
                    model.setFriendName(newfrdlist1.get(i).getFriendName());
                    model.setStatus(newfrdlist1.get(i).getStatus());
                    model.setFriendId(newfrdlist1.get(i).getFriendsId());
                    newfrdlist.add(model);
                }
            }
            if (newfrdlist.size()>0)
            {
                FriendListModel model=new FriendListModel();
                model.setFriendName("All");
                newfrdlist.add(model);
                NearFriendSpinnerAdapter near=new NearFriendSpinnerAdapter(DashboardActivity.this,newfrdlist);
                spnfrndlist.setAdapter(near);
                spnfrndlist.setSelection(0);

                linear_nearfriends_nofriends.setVisibility(View.GONE);
                friendsouter.setVisibility(View.VISIBLE);
                possibilitesouter.setVisibility(View.GONE);

                tvbackbtn.setVisibility(View.VISIBLE);
                friendbtnouter.setVisibility(View.VISIBLE);
            }
            else
            {
                friendsouter.setVisibility(View.GONE);
                possibilitesouter.setVisibility(View.GONE);
                tvbackbtn.setVisibility(View.GONE);
                friendbtnouter.setVisibility(View.VISIBLE);
                linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
            }
        }
        else
        {
            friendsouter.setVisibility(View.GONE);
            linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
            txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
            possibilitesouter.setVisibility(View.GONE);
            tvbackbtn.setVisibility(View.GONE);
            friendbtnouter.setVisibility(View.VISIBLE);
        }

        spinrangelst.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressDistance=progress;
                seekBar.setProgress(progress);
                SeekCount.setText(String.valueOf(progress)+ " KM");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        spninterestlst.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getInterest = spninterestlst.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btn_interst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spnlist !=null && spnlist.length >0) {
                    Intent intent = new Intent(DashboardActivity.this, FriendNearActivity.class);
                    Bundle bu = new Bundle();
                    bu.putInt("Distance", progressDistance);
                    bu.putString("Interest", getInterest);
                    bu.putString("Flag", "");
                    bu.putBoolean("isFromDashboard", true);
                    bu.putBoolean("isInterestSearch",true);
                    intent.putExtras(bu);
                    startActivity(intent);
                    alertDialog.dismiss();
                }
                else
                {
                    Config.alertDialog(DashboardActivity.this,"Near Friend","Please Add Interest First.");
                }
            }
        });


        spnfrndlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //getInterest = spnfrndlist.getSelectedItem().toString();
                Object o = parent.getItemAtPosition(position);
                FriendListModel obj = (FriendListModel) o;
                selectedView=obj;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newfrdlist != null && newfrdlist.size()>0)
                {
                    Intent intent = new Intent(DashboardActivity.this, FriendNearActivity.class);
                    Bundle bu=new Bundle();
                    bu.putInt("Distance",0);
                    bu.putString("Interest","");

                    if (!selectedView.getFriendName().equalsIgnoreCase("All")) {
                        bu.putString("Flag","Single");
                        bu.putString("FriendId",selectedView.getFriendId());
                    } else {

                        bu.putString("Flag","");
                        bu.putBoolean("isFromDashboard",true);
                        bu.putString("FriendId","");
                    }
                    intent.putExtras(bu);
                    startActivity(intent);
                    finish();
                    alertDialog.dismiss();
                }
                else
                {
                    linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                    txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
                    friendsouter.setVisibility(View.GONE);
                    //Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
                }
            }
        });

        btnFriends.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFriends.setBackgroundResource(R.drawable.friends_icon);
                btnPossibilition.setBackgroundResource(R.drawable.unselect_possibilities);

                if (newfrdlist.size()>0)
                {

                    NearFriendSpinnerAdapter near=new NearFriendSpinnerAdapter(DashboardActivity.this,newfrdlist);
                    spnfrndlist.setAdapter(near);
                    spnfrndlist.setSelection(0);

                    linear_nearfriends_nofriends.setVisibility(View.GONE);
                    friendsouter.setVisibility(View.VISIBLE);
                    possibilitesouter.setVisibility(View.GONE);

                    tvbackbtn.setVisibility(View.VISIBLE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                }
                else
                {
                    friendsouter.setVisibility(View.GONE);
                    possibilitesouter.setVisibility(View.GONE);
                    tvbackbtn.setVisibility(View.GONE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                    linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                    txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
                }
            }
        });

        btnPossibilition.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        btnPossibilition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPossibilition.setBackgroundResource(R.drawable.possibilities_icon);
                btnFriends.setBackgroundResource(R.drawable.unselect_friends);
                if (spnlist !=null && spnlist.length >0)
                {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(DashboardActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, spnlist);
                    //adapter.setDropDownViewResource(R.layout.spinner_list_item);
                    spninterestlst.setAdapter(adapter);
                    friendsouter.setVisibility(View.GONE);
                    linear_nearfriends_nofriends.setVisibility(View.GONE);
                    possibilitesouter.setVisibility(View.VISIBLE);

                    tvbackbtn.setVisibility(View.VISIBLE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                }
                else
                {
                    friendsouter.setVisibility(View.GONE);
                    linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                    possibilitesouter.setVisibility(View.GONE);
                    txt_nearfrnd_no.setText("You have selected no interests. Please select interest first.");
                    tvbackbtn.setVisibility(View.GONE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                }
            }
        });

        tvbackbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvbackbtn.setVisibility(View.GONE);
                friendbtnouter.setVisibility(View.VISIBLE);
                friendsouter.setVisibility(View.VISIBLE);
                linear_nearfriends_nofriends.setVisibility(View.GONE);
                possibilitesouter.setVisibility(View.GONE);
            }
        });

        tvClosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void AddFriendAlertDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.addfriend_alertdialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setView(dialoglayout);

        TextView closebtn= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_closebtn);
        final LinearLayout addfriendbynamelinear= (LinearLayout) dialoglayout.findViewById(R.id.linearlayout_addfriendbyname);
        final LinearLayout addfriendbyInterestlinear= (LinearLayout) dialoglayout.findViewById(R.id.linearlayout_addfriendbyInterest);
        closebtn.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));
        final RadioButton addfrndbyname= (RadioButton) dialoglayout.findViewById(R.id.rdo_addfriend_byname);
        final RadioButton addfrndbyinterest= (RadioButton) dialoglayout.findViewById(R.id.rdo_addfriend_byinterest);

        /*ADD Friend by Name controls */
        final EditText edtFriendName= (EditText) dialoglayout.findViewById(R.id.edt_addfrnd_frndname);
        final EditText edtFriendAddress= (EditText) dialoglayout.findViewById(R.id.edt_addfrnd_frndaddress);
        TextView searchbyName= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_searchfrndbyname);
        searchbyName.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));
        noIntrest= (TextView) dialoglayout.findViewById(R.id.notxtInterstet);
        noName= (TextView) dialoglayout.findViewById(R.id.notxtName);
        lvFriendLIst= (ListView) dialoglayout.findViewById(R.id.list_addfrndbynamelist);
        //lvFriendLIst.setVisibility(View.GONE);
        loading =(ProgressWheel) dialoglayout.findViewById(R.id.loading);
        /*Add Friend by Interest controls*/
        TextView searchbyNameInterest= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_searchfrndbyinterest);
        searchbyNameInterest.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));
        final Spinner spnInterestList= (Spinner) dialoglayout.findViewById(R.id.spn_addfrnd_interestlist);

        spnInterestList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getInterest = spnInterestList.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lvfriendlistbyinterest= (ListView) dialoglayout.findViewById(R.id.list_addfrndbyinterestlist);

        final AlertDialog alertDialog = builder.create();


        searchbyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(DashboardActivity.this, "Search Button by name Clicked", Toast.LENGTH_SHORT).show();
                //alertDialog.dismiss();
                String username=edtFriendName.getText().toString();
                int length = edtFriendName.getText().length();
                String city=edtFriendAddress.getText().toString();
                if (length >=4)
                {
                    if (!username.equalsIgnoreCase("") || !city.equalsIgnoreCase(""))
                    {
                        if (Config.isConnectingToInternet(DashboardActivity.this))
                        {
                            loading.setVisibility(View.VISIBLE);
                            lvFriendLIst.setVisibility(View.GONE);
                            noName.setVisibility(View.INVISIBLE);
                            ArrayList<String> list = new ArrayList<String>();
                            GetFriendListByNameAsyncTask getListbyname=new GetFriendListByNameAsyncTask(username,city,list,DashboardActivity.this,DashboardActivity.this);
                            getListbyname.execute();
                        }
                        else
                        {
                            Utility.CustomToast(DashboardActivity.this, "No Internet Connection..!");
                        }
                    }
                    else
                    {
                        //Toast.makeText(getApplicationContext(),"Please enter name or address for search...",Toast.LENGTH_LONG).show();
                        Config.alertDialog(DashboardActivity.this, "Error", "Please enter name or address for search.");
                    }
                }
                else
                {
                    Config.alertDialog(DashboardActivity.this, "Suggestion", "Type Atleast Four Characters.");
                }
            }
        });

        addfrndbyname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (addfrndbyname.isChecked() == true) {
                    addfriendbynamelinear.setVisibility(View.VISIBLE);
                    lvFriendLIst.setVisibility(View.GONE);
                    noName.setVisibility(View.INVISIBLE);
                } else {
                    addfriendbynamelinear.setVisibility(View.GONE);
                    lvfriendlistbyinterest.setVisibility(View.GONE);
                    noIntrest.setVisibility(View.INVISIBLE);
                }
            }
        });


        searchbyNameInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(DashboardActivity.this, "Search Button by interest Clicked", Toast.LENGTH_SHORT).show();

                String username="";
                String city="";
                if (username.equalsIgnoreCase("") || city.equalsIgnoreCase(""))
                {
                    if (Config.isConnectingToInternet(DashboardActivity.this))
                    {
                        loading.setVisibility(View.VISIBLE);
                        lvfriendlistbyinterest.setVisibility(View.GONE);
                        noIntrest.setVisibility(View.INVISIBLE);
                        ArrayList<String> list = new ArrayList<String>();
                        list.add(getInterest);
                        GetFriendListByNameAsyncTask getListbyname=new GetFriendListByNameAsyncTask(username,city,list,DashboardActivity.this,DashboardActivity.this);
                        getListbyname.execute();
                    }
                    else
                    {
                        Utility.CustomToast(DashboardActivity.this, "No Internet Connection..!");
                    }
                }
                else
                {
                    //Toast.makeText(getApplicationContext(),"Please enter name or address for search...",Toast.LENGTH_LONG).show();
                }
            }
        });

        if (spnlist==null)
        {
            Config.alertDialog(DashboardActivity.this,"Add Friend","No Interest Added.");
        }
        else
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(DashboardActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, spnlist);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnInterestList.setAdapter(adapter);
        }


        addfrndbyinterest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Config.hideSoftKeyboardWithoutReq(DashboardActivity.this,edtFriendName);
                if (addfrndbyinterest.isChecked() == true) {

                    if (Config.isConnectingToInternet(DashboardActivity.this)) {
                        if(spnlist != null && spnlist.length >0)
                        {
                            addfriendbyInterestlinear.setVisibility(View.VISIBLE);
                            addfriendbynamelinear.setVisibility(View.GONE);
                            lvfriendlistbyinterest.setVisibility(View.GONE);
                            noIntrest.setVisibility(View.INVISIBLE);
                        }
                        else
                            Config.alertDialog(DashboardActivity.this, "Error", "No Interest Found");
                    } else {
                        Utility.CustomToast(DashboardActivity.this, "No Internet Connection..!");
                    }
                } else {
                    addfriendbyInterestlinear.setVisibility(View.GONE);
                    addfriendbynamelinear.setVisibility(View.VISIBLE);
                }
            }
        });

        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void InviteFriendDialog()
    {
        LayoutInflater inflater= this.getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.invite_message_dialog, null);
        Button send = (Button) dialoglayout.findViewById(R.id.btn_send);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText edtmessage=(EditText)dialoglayout.findViewById(R.id.edtmessage);
        final TextView textView=(TextView) dialoglayout.findViewById(R.id.txtappLink);

        TextView tvClosebtn= (TextView) dialoglayout.findViewById(R.id.txt_invitfrnd_closebtn);
        tvClosebtn.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        final AlertDialog alertDialog = builder.create();


        tvClosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inviteMsg = edtmessage.getText().toString();
                if (inviteMsg.equals("")) {
                    //Toast.makeText(DashboardActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                    Config.alertDialog(DashboardActivity.this, "Suggestion", "Please enter message");

                }
                else if (TextUtils.isEmpty(inviteMsg))
                {
                    //Toast.makeText(DashboardActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                    Config.alertDialog(DashboardActivity.this, "Suggestion", "Please enter message");
                }
                else
                {
                   /* Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("<p>"+inviteMsg.toString()+"/n"+textView.getText().toString()+"</p>"));
                    startActivity(Intent.createChooser(sharingIntent,"Share using"));*/
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteMsg.toString() + "\n" + textView.getText().toString());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void onTaskCompleted(String s) {
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("searchbyName"))
        {
            if (!arr[0].equalsIgnoreCase("[]"))
            {
                ArrayList<AddFriendModel> searchnameFrndlist=new ArrayList<>();
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        AddFriendModel afm=new AddFriendModel();
                        String fname = jsonobject.getString("fName");
                        String lname = jsonobject.getString("lName");
                        String userId = jsonobject.getString("userId");
                        afm.setFriendName(fname+" "+lname);
                        afm.setFriendid(userId);
                        afm.setThumbnailUrl(jsonobject.getString("thumbnailUrl"));
                        afm.setStatus(jsonobject.getString("requestStatus"));
                        afm.setRequestSent(jsonobject.getBoolean("isRequestSent"));
                        afm.setGender(jsonobject.getString("gender"));
                        afm.setAge(jsonobject.getInt("age"));
                        searchnameFrndlist.add(afm);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (arr[2].equalsIgnoreCase("Name"))
                {
                    lvFriendLIst.setAdapter(new AddFriendModelAdapter(this, searchnameFrndlist));
                    lvFriendLIst.setVisibility(View.VISIBLE);
                    noName.setVisibility(View.GONE);
                }
                else
                {
                    lvfriendlistbyinterest.setAdapter(new AddFriendModelAdapter(this, searchnameFrndlist));
                    lvfriendlistbyinterest.setVisibility(View.VISIBLE);
                    noIntrest.setVisibility(View.GONE);
                }
                loading.setVisibility(View.GONE);
            }
            else {
                //Toast.makeText(DashboardActivity.this, "No Search Found", Toast.LENGTH_SHORT).show();
                Config.alertDialog(DashboardActivity.this, "Add Friend", "No Search Found.");
                loading.setVisibility(View.GONE);
                if (arr[2].equalsIgnoreCase("Name"))
                {
                    lvFriendLIst.setVisibility(View.GONE);
                    noName.setVisibility(View.VISIBLE);
                }
                else
                {
                    lvfriendlistbyinterest.setVisibility(View.GONE);
                    noIntrest.setVisibility(View.VISIBLE);
                }
            }
        }
        else  if (arr[1].equalsIgnoreCase("InterestList"))
        {
           selectedInterestedList=new ArrayList<>();
            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(arr[0]);

                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    //InterestModel afm=new InterestModel();
                    String interestId = jsonobject.getString("interestId");
                    String interestName = jsonobject.getString("interestName");

                    //afm.a
                    //afm.setInterestid(interestId);
                    selectedInterestedList.add(interestName);
                }

                realizer.com.makemepopular.utils.Singleton.setAlreadyselectedInterestList(selectedInterestedList);
                spnlist = new String[selectedInterestedList.size()];
                spnlist = selectedInterestedList.toArray(spnlist);
                realizer.com.makemepopular.utils.Singleton.setSpnlist(spnlist);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else  if (arr[1].equalsIgnoreCase("Emergency"))
        {
            if (! arr[0].equalsIgnoreCase("[]"))
            {
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);
                    friendlist=new ArrayList<>();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);

                        FriendListModel model=new FriendListModel();
                        model.setFriendName(jsonobject.getString("friendName"));
                        model.setIsEmergency(jsonobject.getBoolean("isEmergencyAlert"));
                        model.setIsmessaging(jsonobject.getBoolean("isMessagingAllowed"));
                        model.setIstracking(jsonobject.getBoolean("isTrackingAllowed"));
                        model.setThumbnailUrl(jsonobject.getString("friendThumbnailUrl"));
                        model.setFriendId(jsonobject.getString("friendsId"));
                        friendlist.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                friendadapter= new FriendListModelAdapter(this,friendlist,"Emergency");
                list_addEmergncyfrnd.setAdapter(friendadapter);
                list_addEmergncyfrnd.setVisibility(View.VISIBLE);
                loadingemergeny.setVisibility(View.GONE);
            }
            else
            {
                list_addEmergncyfrnd.setVisibility(View.GONE);
                loadingemergeny.setVisibility(View.GONE);
                //Toast.makeText(DashboardActivity.this,"You have no any Emergency Friend...",Toast.LENGTH_LONG).show();
                Config.alertDialog(DashboardActivity.this, "Emergency", "You Have No Any Emergency Friend.");
            }
        }
        else if (arr[1].equalsIgnoreCase("AcceptReject"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest"))
                    Config.alertDialog(DashboardActivity.this, "Friend Request", "Friend Request Accepted Successfully.");
                    //Toast.makeText(DashboardActivity.this, "Friend Request Accepted Successfully", Toast.LENGTH_SHORT).show();
                else
                    Config.alertDialog(DashboardActivity.this, "Friend Request", "Friend Request Rejected Successfully.");
                    //Toast.makeText(DashboardActivity.this, "Friend Request Rejected Successfully", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest"))
                    Config.alertDialog(DashboardActivity.this, "Friend Request", "Friend Request Not Accepted.");
                    //Toast.makeText(DashboardActivity.this, "Friend Request Not Accepted", Toast.LENGTH_SHORT).show();
                else
                    Config.alertDialog(DashboardActivity.this, "Friend Request", "Friend Request Not Rejected.");
                    //Toast.makeText(DashboardActivity.this, "Friend Request Not Rejected", Toast.LENGTH_SHORT).show();
                //EmergencyDialog();
            }
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", "");
            edit.putString("RequsetByName", "");
            edit.putString("ThumbnailUrl","");
            edit.putString("RequsetByUserId","");
            edit.commit();
        }
        else  if (arr[1].equalsIgnoreCase("FriendList")) {
            friendlist=new ArrayList<>();
            if (!arr[0].equalsIgnoreCase("[]")) {
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(arr[0]);

                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);

                        FriendListModel model = new FriendListModel();
                        model.setUserId(jsonobject.getString("userId"));
                        model.setFriendName(jsonobject.getString("friendName"));
                        model.setIsEmergency(jsonobject.getBoolean("isEmergencyAlert"));
                        model.setIsmessaging(jsonobject.getBoolean("isMessagingAllowed"));
                        model.setIstracking(jsonobject.getBoolean("isTrackingAllowed"));
                        model.setThumbnailUrl(jsonobject.getString("friendThumbnailUrl"));
                        model.setFriendId(jsonobject.getString("friendsId"));
                        model.setStatus(jsonobject.getString("status"));
                        model.setSentRequest(jsonobject.getBoolean("isRequestSent"));
                        friendlist.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Singleton.setFriendListModels(friendlist);

                if (arr[2].equalsIgnoreCase("FriendNear"))
                {
                    ArrayList<FriendListModel> frdlist=new ArrayList<>();
                    newfrdlist=new ArrayList<>();

                    frdlist= Singleton.getFriendListModels();

                    if (frdlist!=null) {
                        for (int i = 0; i < frdlist.size(); i++) {
                            FriendListModel model = new FriendListModel();
                            if (frdlist.get(i).getStatus().equalsIgnoreCase("Accepted")) {
                                model.setFriendName(frdlist.get(i).getFriendName());
                                model.setStatus(frdlist.get(i).getStatus());

                                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
                                String userID=sharedpreferences.getString("UserId","");
                                String friendId="";
                                if (userID.equalsIgnoreCase(frdlist.get(i).getUserId()))
                                {
                                    friendId=frdlist.get(i).getFriendId();
                                }
                                else
                                {
                                    friendId=frdlist.get(i).getUserId();
                                }
                                model.setFriendId(friendId);

                                newfrdlist.add(model);
                            }
                        }
                        if (newfrdlist.size()>0)
                        {
                            if (Config.isConnectingToInternet(this))
                            {
                                FriendListModel model=new FriendListModel();
                                model.setFriendName("All");
                                newfrdlist.add(model);
                                NearFriendSpinnerAdapter near=new NearFriendSpinnerAdapter(DashboardActivity.this,newfrdlist);
                                spnfrndlist.setAdapter(near);
                                spnfrndlist.setSelection(0);

                                friendsouter.setVisibility(View.VISIBLE);
                                linear_nearfriends_nofriends.setVisibility(View.GONE);
                                possibilitesouter.setVisibility(View.GONE);

                                tvbackbtn.setVisibility(View.VISIBLE);
                                friendbtnouter.setVisibility(View.VISIBLE);
                            }
                        }
                        else
                        {
                            //Toast.makeText(DashboardActivity.this,"Add Friend First",Toast.LENGTH_LONG).show();
                            //Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
                            friendsouter.setVisibility(View.GONE);
                            linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                            txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
                            possibilitesouter.setVisibility(View.GONE);
                            tvbackbtn.setVisibility(View.GONE);
                            friendbtnouter.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        //Toast.makeText(DashboardActivity.this,"Add Friend First",Toast.LENGTH_LONG).show();
                        //Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
                        friendsouter.setVisibility(View.GONE);
                        possibilitesouter.setVisibility(View.GONE);
                        linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                        txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
                        tvbackbtn.setVisibility(View.GONE);
                        friendbtnouter.setVisibility(View.VISIBLE);
                    }
                    //loadingtrack.setVisibility(View.GONE);
                }
            }
            else
            {
                if (arr[2].equalsIgnoreCase("FriendNear"))
                {
                    //Config.alertDialog(DashboardActivity.this,"Near Friend","You have added no friends. Please add friends first.");
                    friendsouter.setVisibility(View.GONE);
                    possibilitesouter.setVisibility(View.GONE);
                    linear_nearfriends_nofriends.setVisibility(View.VISIBLE);
                    txt_nearfrnd_no.setText("You have added no friends. Please add friends first.");
                    tvbackbtn.setVisibility(View.GONE);
                    friendbtnouter.setVisibility(View.VISIBLE);
                }
            }
            if (arr[2].equalsIgnoreCase("FriendNear"))
            {
                loadingtrack.setVisibility(View.GONE);
            }
        }
        else if (arr[1].equalsIgnoreCase("TrackFriend"))
        {
            if(!arr[0].equalsIgnoreCase("302"))
            {
                JSONObject json = null;
                try {
                    json = new JSONObject(arr[0]);

                    if (!json.getString("friendName").equalsIgnoreCase("null"))
                    {
                        ArrayList<NearByFriends> nearFrndlist=new ArrayList<>();

                        NearByFriends near=new NearByFriends();
                        near.setGender(json.getString("gender"));
                        near.setLatitude(String.valueOf(json.getDouble("latitude")));
                        near.setLongitude(String.valueOf(json.getDouble("longitude")));
                        near.setThumbnailUrl(json.getString("thumbnailUrl"));
                        near.setFriendName(json.getString("friendName"));
                        near.setAge(json.getString("age"));
                        near.setFriendUserId(json.getString("friendId"));
                        near.setLastupdate(json.getString("lastUpdatedOn"));
                        nearFrndlist.add(near);

                        realizer.com.makemepopular.utils.Singleton.setSingleNearFriendList(nearFrndlist);
                        Intent intent = new Intent(DashboardActivity.this, FriendNearActivity.class);
                        Bundle bu=new Bundle();
                        bu.putInt("Distance",0);
                        bu.putString("Interest","");
                        bu.putString("Flag","Single");
                        intent.putExtras(bu);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        // Toast.makeText(FriendListActivity.this,"No data available for this friend",Toast.LENGTH_LONG).show();
                        Config.alertDialog(this, "Friend List", "No data available for this friend.");
                    }
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("TrackFriendName", json.getString("friendName"));
                    edit.putInt("TrackFriendLatitude", json.getInt("latitude"));
                    edit.putInt("TrackFriendLongitude", json.getInt("longitude"));
                    edit.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                //Toast.makeText(FriendListActivity.this,"You have no permission to track this friend",Toast.LENGTH_LONG).show();
                Config.alertDialog(this, "Friend List", "You have no permission to track this friend.");
            }
            loadingtrack.setVisibility(View.GONE);
        }
        else if (arr[1].equalsIgnoreCase("AcceptReject"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("Type", "");
                edit.putString("RequsetByName", "");
                edit.putString("ThumbnailUrl","");
                edit.putString("RequsetByUserId","");
                edit.commit();
            }
        }
        else if (arr[1].equalsIgnoreCase("EmergencyAck"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
                if (arr[2].equalsIgnoreCase("ACK & Map")) {
                    Intent intent = new Intent(DashboardActivity.this, TrackShowMap.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("FriendName", sharedpreferences.getString("TroublerName", ""));
                    bundle.putDouble("LATITUDE", Double.valueOf(sharedpreferences.getString("Latitude", "")));
                    bundle.putDouble("LONGITUDE", Double.valueOf(sharedpreferences.getString("Longitude", "")));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("Type", "");
                edit.putString("ContactNo", "");
                edit.putString("Message","");
                edit.putString("Latitude","");
                edit.putString("Longitude","");
                edit.putString("ThumbnailUrl","");
                edit.putString("TroublerName","");
                edit.commit();
            }
        }
        else if (arr[1].equalsIgnoreCase("DeRegisterAccount"))
        {
            if (arr[0].equalsIgnoreCase("true")) {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("Login", "false");
                edit.putString("TockenID", "");
                edit.putString("Success", "");
                edit.putString("TodaysTrackingCount", "");
                edit.putString("LastWeekTrackingCount","");
                edit.putString("LastMonthTrackingCount","");
                edit.commit();
                Singleton.setFriendListModels(null);
                realizer.com.makemepopular.utils.Singleton.setAlreadyselectedInterestList(null);
                realizer.com.makemepopular.utils.Singleton.setSingleNearFriendList(null);
                realizer.com.makemepopular.utils.Singleton.setNotificationList(null);
                realizer.com.makemepopular.utils.Singleton.setSpnlist(null);
                FirebaseInstanceId.getInstance().getToken();
                qr.deleteAllData();
                Intent intent1 = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent1);
                finish();
            }
            else
            {
                //Toast.makeText(DashboardActivity.this,"Logout Error",Toast.LENGTH_LONG).show();
                Config.alertDialog(this, "Error", "Logout Error...Please Try Again Later.");
            }
            loadingDashboard.setVisibility(View.GONE);
        }
        if (arr[1].equals("UserList"))
        {
            JSONArray rootObj = null;
            Log.d("String", arr[0].toString());
            try {

                rootObj = new JSONArray(arr[0]);
                String userId="",firstName="",thumbnailurl="",friendid="",chkuserid;
                int i=rootObj.length();

                friendlist=new ArrayList<>();
                for(int j=0;j<i;j++)
                {
                    JSONObject obj = rootObj.getJSONObject(j);

                    FriendListModel model = new FriendListModel();
                    model.setUserId(obj.getString("userId"));
                    model.setFriendName(obj.getString("friendName"));
                    model.setIsEmergency(obj.getBoolean("isEmergencyAlert"));
                    model.setIsmessaging(obj.getBoolean("isMessagingAllowed"));
                    model.setIstracking(obj.getBoolean("isTrackingAllowed"));
                    model.setThumbnailUrl(obj.getString("friendThumbnailUrl"));
                    model.setFriendId(obj.getString("friendsId"));
                    model.setStatus(obj.getString("status"));
                    model.setSentRequest(obj.getBoolean("isRequestSent"));
                    friendlist.add(model);

                    String ismessaging=obj.getString("isMessagingAllowed");
                    String status=obj.getString("status");

                    boolean frndChk = qr.ChekFriendinFrndList(obj.getString("friendsUserId"));
                    if (!frndChk) {
                        qr.insertFriendList(obj.getString("friendsUserId"), obj.getString("friendName"), String.valueOf(obj.getBoolean("isEmergencyAlert")), String.valueOf(obj.getBoolean("isMessagingAllowed")), String.valueOf(obj.getBoolean("isTrackingAllowed")), obj.getString("friendThumbnailUrl"), obj.getString("status"), String.valueOf(obj.getBoolean("isRequestSent")), obj.getString("createTS"), obj.getString("allowTrackingTillDate"), obj.getString("trackingStatusChangeDate"), obj.getString("isDeleted"));
                    } else {
                        qr.updateFriendLIst(obj.getString("friendsUserId"), obj.getString("friendName"), String.valueOf(obj.getBoolean("isEmergencyAlert")), String.valueOf(obj.getBoolean("isMessagingAllowed")), String.valueOf(obj.getBoolean("isTrackingAllowed")), obj.getString("friendThumbnailUrl"), obj.getString("status"), String.valueOf(obj.getBoolean("isRequestSent")), obj.getString("createTS"), obj.getString("allowTrackingTillDate"), obj.getString("trackingStatusChangeDate"), obj.getString("isDeleted"));
                    }
                }
                Singleton.setFriendListModels(friendlist);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {

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

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Please Activate GPS Service");

        // On pressing Settings button
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        //finish();
                    }
                });

        alertDialog.show();
    }

    public void showEmergencyAckAlert(final String msg,String troubler) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Emergency Acknowledge From "+troubler);

        // Setting Dialog Message
        alertDialog.setMessage(msg);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
                        SharedPreferences.Editor edit = sharedpreferences.edit();
                        edit.putString("Type", "");
                        edit.putString("HelperUserId", "");
                        edit.putString("HelperUserName","");
                        edit.putString("Thumbnailurl","");
                        edit.putString("Message","");
                        edit.putString("isReaching","");
                        edit.commit();

                        dialog.dismiss();
                    }
                });


        alertDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dash_setting, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.user_profile:
                Intent intent=new Intent(DashboardActivity.this, UserProfileActivity.class);
                startActivity(intent);
                return true;

            case R.id.add_interest:
                realizer.com.makemepopular.utils.Singleton.setAlreadyselectedInterestList(selectedInterestedList);
                Bundle b=new Bundle();
                Intent interest = new Intent(DashboardActivity.this, InterestActivity.class);
                b.putString("FromWhere","Dashboard");
                interest.putExtras(b);
                startActivity(interest);
                return true;

            case R.id.user_logout:


                if (Config.isConnectingToInternet(DashboardActivity.this)) {
                    loadingDashboard.setVisibility(View.VISIBLE);
                    DeregisterUserAsyncTask deregietr=new DeregisterUserAsyncTask(DashboardActivity.this,DashboardActivity.this);
                    deregietr.execute();
                } else {
                    Utility.CustomToast(DashboardActivity.this, "No Internet Connection..!");
                }
                return true;

            case R.id.about_us:
                Intent i=new Intent(DashboardActivity.this, AboutApp.class);
                startActivity(i);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void NotificationListClick(View v)
    {
        Bundle bundle = new Bundle();
        Intent friendlist=new Intent(DashboardActivity.this, Notification_activity.class);
        bundle.putString("FriendID","");
        friendlist.putExtras(bundle);
        startActivity(friendlist);
    }

    public class GetAddressAyncTask extends  AsyncTask<Void,Void,StringBuilder> {
        ProgressDialog dialog;
        Context context;

        public GetAddressAyncTask(Context con)
        {
            this.context=con;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            dialog=ProgressDialog.show(DashboardActivity.this,"","Finding Geographical Location...!");
        }


        @Override
        protected StringBuilder doInBackground(Void... params) {
            if (canGetLocation() == true) {
                if (provider != null & !provider.equals(""))
                {
                    Location locatin= UtilLocation.getLastKnownLoaction(true, context);
                    if (locatin!=null)
                    {
                        currentLat=locatin.getLatitude();
                        currentLog=locatin.getLongitude();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(StringBuilder stringBuilder) {
            super.onPostExecute(stringBuilder);

            if (currentLat == null )
            {
                showSettingsAlert();
            }
            else {
                Intent intent=new Intent(DashboardActivity.this, EmergencyActivity.class);
                Bundle bundle=new Bundle();
                bundle.putDouble("CURRENTLAT",currentLat);
                bundle.putDouble("CURRENTLOG",currentLog);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            dialog.dismiss();
        }
    }
    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,DashboardActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,DashboardActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,DashboardActivity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,DashboardActivity.this);
                }

            }

            else if(update.equals("RefreshThreadList")) {

            }
        }
    }
    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 300){
                DashboardActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                DashboardActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }

/*        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION

                    }, 101);

        } else {

        }*/

    }

    private void handleNewLocation(Location location) {
        currentLat = location.getLatitude();
        currentLog = location.getLongitude();
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
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,DashboardActivity.this);
            mGoogleApiClient.disconnect();
        }
    }


    public void showTrackAgainAlert(final String msg,final String friendid,final ArrayList<NearByFriends> nearFrndlist) {

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(DashboardActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Tracking Alert");

        // Setting Dialog Message
        alertDialog.setMessage(msg);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (Config.isConnectingToInternet(DashboardActivity.this))
                        {
                            //loading.setVisibility(View.VISIBLE);
                            TrackFriendAsynctask getListbyname=new TrackFriendAsynctask(friendid,DashboardActivity.this,DashboardActivity.this);
                            getListbyname.execute();
                        }
                        else
                        {
                            Utility.CustomToast(DashboardActivity.this, "No Internet Connection..!");
                        }
                        dialog.dismiss();
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realizer.com.makemepopular.utils.Singleton.setSingleNearFriendList(nearFrndlist);
                        Intent intent = new Intent(DashboardActivity.this, FriendNearActivity.class);
                        Bundle bu=new Bundle();
                        bu.putInt("Distance",0);
                        bu.putString("Interest","");
                        bu.putString("Flag","Single");
                        intent.putExtras(bu);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    public class checkLocationService extends AsyncTask<Void,Void,Boolean> {

        StringBuilder resultbuilder;
        Context mycontext;
        Boolean location;

        public checkLocationService(Context mycontext) {
            this.mycontext=mycontext;
            loadingDashboard.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            location=canGetLocation();

            return location;
        }

        @Override
        protected void onPostExecute(Boolean string) {
            super.onPostExecute(string);
            loadingDashboard.setVisibility(View.GONE);
            if (string)
            {
            }
            else
            {
                showSettingsAlert();
            }
        }
    }
    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED || result1 == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, 101);
    }

}
