package realizer.com.makemepopular;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import realizer.com.makemepopular.addfriend.AddFriendModel;
import realizer.com.makemepopular.addfriend.AddFriendModelAdapter;
import realizer.com.makemepopular.asynctask.GetFriendListByNameAsyncTask;
import realizer.com.makemepopular.asynctask.GetInterestListAsyncTask;
import realizer.com.makemepopular.asynctask.SendEmergencyAlertAsyncTask;
import realizer.com.makemepopular.friendlist.FriendListActivity;
import realizer.com.makemepopular.friendnear.FriendNearActivity;
import realizer.com.makemepopular.neaybyplaces.NearyByPlacesActivity;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.GradientBackgroundPainter;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 12/01/2017.
 */
public class DashboardActivity extends AppCompatActivity implements OnTaskCompleted,LocationListener
{

    BarChart barChart;
    private GradientBackgroundPainter gradientBackgroundPainter;
    TextView ico_emergency,ico_friendnear,ico_addfrnd,ico_nearbyplace,ico_invitefrnd,ico_frndlist;
    ListView lvFriendLIst,lvfriendlistbyinterest;
    ArrayList<AddFriendModel> searchnameFrndlist=new ArrayList<>();
    String[] spnlist=new String[20];
    static String getInterest;
    LocationManager locationmanager;
    static Double currentLat,currentLog;
    String provider;
    ProgressWheel loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        getSupportActionBar().setTitle("Make Me Popular");

        ico_emergency= (TextView) findViewById(R.id.txt_dash_emergencyico);
        ico_friendnear= (TextView) findViewById(R.id.txt_dash_friendnearico);
        ico_addfrnd= (TextView) findViewById(R.id.txt_dash_addfriendico);
        ico_nearbyplace= (TextView) findViewById(R.id.txt_dash_nearybyplaceico);
        ico_invitefrnd= (TextView) findViewById(R.id.txt_dash_invitefrndico);
        ico_frndlist = (TextView) findViewById(R.id.txt_dash_frndlistico);

        ico_emergency.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_friendnear.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_addfrnd.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_nearbyplace.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_invitefrnd.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_frndlist.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        barChart = (BarChart) findViewById(R.id.chart);
        HorizontalData();


        Criteria cri = new Criteria();
        locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationmanager.getBestProvider(cri, false);





        //get interest list
        if (Config.isConnectingToInternet(DashboardActivity.this))
        {
            GetInterestListAsyncTask check=new GetInterestListAsyncTask(DashboardActivity.this,DashboardActivity.this);
            check.execute();
        }
        else
        {
            Config.alertDialog(DashboardActivity.this,"Network Error","No Internet connection");
        }

    }

    public void HorizontalData()
    {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(12f, 0));
        entries.add(new BarEntry(4f, 1));
        entries.add(new BarEntry(10f, 2));
        entries.add(new BarEntry(18f, 3));

        BarDataSet dataset = new BarDataSet(entries, "# of Calls");

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Today");
        labels.add("Yesterday");
        labels.add("Last Week");
        labels.add("Last Month");

        BarData data = new BarData(labels, dataset);
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.setData(data);
        barChart.animateY(5000);
    }
    public void EmergencyClick(View v)
    {
        EmergencyDialog();
    }
    public void EmergencyDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.emergency_alertdialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setView(dialoglayout);
        Button emergency= (Button) dialoglayout.findViewById(R.id.btn_emergency);
        final RadioButton rd_trouble= (RadioButton) dialoglayout.findViewById(R.id.rd_trouble);
        rd_trouble.setChecked(true);
        final EditText message= (EditText) dialoglayout.findViewById(R.id.edt_msg);
        emergency.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));

        final AlertDialog alertDialog = builder.create();

        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (canGetLocation() == true) {
                    if (provider != null & !provider.equals(""))

                    {
                        Location locatin= UtilLocation.getLastKnownLoaction(true, DashboardActivity.this);
                        if (locatin!=null)
                        {
                            currentLat=locatin.getLatitude();
                            currentLog=locatin.getLongitude();
                        }
                        else{
                            Toast.makeText(DashboardActivity.this,"location not found", Toast.LENGTH_LONG ).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(DashboardActivity.this,"Provider is null",Toast.LENGTH_LONG).show();
                    }

                    //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
                } else {
                    //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
                    showSettingsAlert();

                }

                Toast.makeText(DashboardActivity.this, "Emergency Message Send", Toast.LENGTH_SHORT).show();
                String msg=message.getText().toString();

                if (!msg.equalsIgnoreCase(""))
                {
                    if (Config.isConnectingToInternet(DashboardActivity.this))
                    {
                        SendEmergencyAlertAsyncTask sendalert=new SendEmergencyAlertAsyncTask(msg,String.valueOf(currentLat),String.valueOf(currentLog),DashboardActivity.this,DashboardActivity.this);
                        sendalert.execute();
                        alertDialog.dismiss();
                    }
                    else
                    {
                        Config.alertDialog(DashboardActivity.this,"Network Error","No Internet connection");
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please enter message...",Toast.LENGTH_LONG).show();
                }
            }
        });

        alertDialog.show();
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
        /*Intent invite=new Intent(DashboardActivity.this, InviteToOthersActivity.class);
        startActivity(invite);*/
        InviteFriendDialog();
    }
    public void NearByPlaceClick(View v)
    {
        Intent nearbyplace=new Intent(DashboardActivity.this, NearyByPlacesActivity.class);
        startActivity(nearbyplace);
    }
    public void FriendNearClick(View v)
    {
        Intent friendnear=new Intent(DashboardActivity.this, FriendNearActivity.class);
        startActivity(friendnear);
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
        closebtn.setTypeface(FontManager.getTypeface(getApplicationContext(),FontManager.FONTAWESOME));
        final RadioButton addfrndbyname= (RadioButton) dialoglayout.findViewById(R.id.rdo_addfriend_byname);
        final RadioButton addfrndbyinterest= (RadioButton) dialoglayout.findViewById(R.id.rdo_addfriend_byinterest);

        /*ADD Friend by Name controls */
        final EditText edtFriendName= (EditText) dialoglayout.findViewById(R.id.edt_addfrnd_frndname);
        final EditText edtFriendAddress= (EditText) dialoglayout.findViewById(R.id.edt_addfrnd_frndaddress);
        TextView searchbyName= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_searchfrndbyname);
        searchbyName.setTypeface(FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME));

        lvFriendLIst= (ListView) dialoglayout.findViewById(R.id.list_addfrndbynamelist);
        loading =(ProgressWheel) dialoglayout.findViewById(R.id.loading);
        /*Add Friend by Interest controls*/
        TextView searchbyNameInterest= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_searchfrndbyinterest);
        searchbyNameInterest.setTypeface(FontManager.getTypeface(getApplicationContext(),FontManager.FONTAWESOME));
        final Spinner spnInterestList= (Spinner) dialoglayout.findViewById(R.id.spn_addfrnd_interestlist);
        //String[] spnlist={"Biker","Trekking","Music"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, spnlist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnInterestList.setAdapter(adapter);

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
                String city=edtFriendAddress.getText().toString();
                if (!username.equalsIgnoreCase("") || !city.equalsIgnoreCase(""))
                {
                    if (Config.isConnectingToInternet(DashboardActivity.this))
                    {
                        loading.setVisibility(View.VISIBLE);
                        lvFriendLIst.setVisibility(View.GONE);
                        ArrayList<String> list = new ArrayList<String>();
                        GetFriendListByNameAsyncTask getListbyname=new GetFriendListByNameAsyncTask(username,city,list,DashboardActivity.this,DashboardActivity.this);
                        getListbyname.execute();
                    }
                    else
                    {
                        Config.alertDialog(DashboardActivity.this,"Network Error","No Internet connection");
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please enter name or address for search...",Toast.LENGTH_LONG).show();
                }
            }
        });

        addfrndbyname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (addfrndbyname.isChecked() == true) {
                    addfriendbynamelinear.setVisibility(View.VISIBLE);
                } else {
                    addfriendbynamelinear.setVisibility(View.GONE);
                }
            }
        });


        /*Friend by Interest*/
        //lvfriendlistbyinterest.setAdapter(new AddFriendModelAdapter(this, searchnameFrndlist));

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
                        ArrayList<String> list = new ArrayList<String>();
                        list.add(getInterest);
                        GetFriendListByNameAsyncTask getListbyname=new GetFriendListByNameAsyncTask(username,city,list,DashboardActivity.this,DashboardActivity.this);
                        getListbyname.execute();
                    }
                    else
                    {
                        Config.alertDialog(DashboardActivity.this,"Network Error","No Internet connection");
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please enter name or address for search...",Toast.LENGTH_LONG).show();
                }
            }
        });
        addfrndbyinterest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (addfrndbyinterest.isChecked() == true) {
                    if (spnlist.length >0)
                    addfriendbyInterestlinear.setVisibility(View.VISIBLE);
                    else
                        Toast.makeText(getApplicationContext(),"No Interest Found...",Toast.LENGTH_LONG).show();
                } else {
                    addfriendbyInterestlinear.setVisibility(View.GONE);
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
        TextView textView=(TextView) dialoglayout.findViewById(R.id.txtappLink);

        TextView tvClosebtn= (TextView) dialoglayout.findViewById(R.id.txt_invitfrnd_closebtn);
        tvClosebtn.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));

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
                    Toast.makeText(DashboardActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(inviteMsg))
                {
                    Toast.makeText(DashboardActivity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/html");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml("<p>"+inviteMsg.toString()+"</p>"));
                    startActivity(Intent.createChooser(sharingIntent,"Share using"));
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
    public void onLocationChanged(Location location) {
        currentLat=location.getLatitude();
        currentLog=location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
                        String fname = jsonobject.getString("FName");
                        String lname = jsonobject.getString("LName");
                        String userId = jsonobject.getString("userId");
                        afm.setFriendName(fname+" "+lname);
                        afm.setFriendid(userId);
                        afm.setStatus(jsonobject.getString("requestStatus"));
                        searchnameFrndlist.add(afm);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (arr[2].equalsIgnoreCase("Name"))
                {
                    lvFriendLIst.setAdapter(new AddFriendModelAdapter(this, searchnameFrndlist));
                    lvFriendLIst.setVisibility(View.VISIBLE);
                }
                else
                {
                    lvfriendlistbyinterest.setAdapter(new AddFriendModelAdapter(this, searchnameFrndlist));
                    lvfriendlistbyinterest.setVisibility(View.VISIBLE);

                }
                loading.setVisibility(View.GONE);
            }
            else {
                Toast.makeText(DashboardActivity.this, "No Search Found", Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
            }
        }
        else  if (arr[1].equalsIgnoreCase("InterestList"))
        {
            ArrayList<String> searchnameFrndlist=new ArrayList<>();
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
                    searchnameFrndlist.add(interestName);
                }

                spnlist = new String[searchnameFrndlist.size()];
                spnlist = searchnameFrndlist.toArray(spnlist);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else  if (arr[1].equalsIgnoreCase("SendAlert"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                Toast.makeText(DashboardActivity.this, "Message send successfully", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(DashboardActivity.this, "Message Not Send", Toast.LENGTH_SHORT).show();
                EmergencyDialog();
            }
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
                        finish();
                    }
                });

        alertDialog.show();
    }
    public void showConnectionAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Please Connect to Internet");

        // On pressing Settings button
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                       finish();
                    }
                });

        alertDialog.show();
    }
}
