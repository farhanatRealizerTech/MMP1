package realizer.com.makemepopular;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import realizer.com.makemepopular.asynctask.RegistrationAsyncTaskPost;
import realizer.com.makemepopular.models.RegisterUserRequestModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.UtilLocation;
import realizer.com.makemepopular.view.ProgressWheel;
import realizer.com.makemepopular.view.RoundedImageView;

/**
 * Created by Win on 10/01/2017.
 */
public class RegistrationActivity extends AppCompatActivity implements OnTaskCompleted,LocationListener
{
    TextView ico_user,ico_email,ico_mobile,ico_calendar,ico_gender,ico_list,edt_reg_dob;
    Button registration;
    SharedPreferences sharedpreferences;
    RegisterUserRequestModel registerModel;
    RoundedImageView roundedImageView;
    EditText edt_reg_fname,edt_reg_lname,edt_reg_email,edt_mob_no;
    RadioButton rB_male,rB_female;
    Spinner spn_reg_user_acc;
    String fireBasetoken;
    LocationManager locationmanager;
    static Double currentLat,currentLog;
    DatePickerDialog.OnDateSetListener date;
    Calendar myCalendar;
    ProgressWheel loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registratin_activity);

        getSupportActionBar().hide();

        initializeComponants();

        sharedpreferences= PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
        String[] items = new String[] {"Select Account Type", "Personal", "Official","Both"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_reg_user_acc.setAdapter(adapter);

        ico_user.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_email.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_mobile.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_calendar.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_gender.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_list.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));

        FireBaseInitialization();//initilaize firabase
        Criteria cri = new Criteria();
        locationmanager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        String provider = locationmanager.getBestProvider(cri, false);


        if (canGetLocation() == true) {
            if (provider != null & !provider.equals(""))

            {
                Location locatin= UtilLocation.getLastKnownLoaction(true, this);
                if (locatin!=null)
                {
                    currentLat=locatin.getLatitude();
                    currentLog=locatin.getLongitude();
                }
                else{
                    Toast.makeText(this,"location not found", Toast.LENGTH_LONG ).show();
                }
            }
            else
            {
                Toast.makeText(this,"Provider is null",Toast.LENGTH_LONG).show();
            }

            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        } else {
            //SHOW OUR SETTINGS ALERT, AND LET THE USE TURN ON ALL THE GPS PROVIDERS
            showSettingsAlert();

        }

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                registerModel=new RegisterUserRequestModel();
                registerModel.setfName(edt_reg_fname.getText().toString());
                registerModel.setlName(edt_reg_lname.getText().toString());
                registerModel.setEmailId(edt_reg_email.getText().toString());
                registerModel.setContactNo(edt_mob_no.getText().toString());
                registerModel.setDob(edt_reg_dob.getText().toString());
                registerModel.setAccountType(spn_reg_user_acc.getItemAtPosition(spn_reg_user_acc.getSelectedItemPosition()).toString());
                registerModel.setFcmRegId(fireBasetoken);

                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("DeviceId", telephonyManager.getDeviceId());
                edit.commit();
                registerModel.setDeviceId(telephonyManager.getDeviceId());

                String gender;
                if (rB_male.isChecked())
                {
                    gender=rB_male.getText().toString();
                }
                else if (rB_female.isChecked())
                {
                    gender=rB_female.getText().toString();
                }
                else {
                    gender = "";
                }
                registerModel.setGender(gender);

                registerModel.setLastCity(getLocationName(currentLat,currentLog));

                /*roundedImageView.buildDrawingCache();
                Bitmap bitmap = roundedImageView.getDrawingCache();

                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                byte[] image=stream.toByteArray();
                System.out.println("byte array:"+image);

                String img_str = Base64.encodeToString(image, 0);
                System.out.println("string:"+img_str);*/
                registerModel.setThumbnailUrl("");

                if (Config.isConnectingToInternet(RegistrationActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    RegistrationAsyncTaskPost register=new RegistrationAsyncTaskPost(registerModel,RegistrationActivity.this,RegistrationActivity.this);
                    register.execute();
                }
                else
                {
                    Config.alertDialog(RegistrationActivity.this,"Network Error","No Internet connection");
                }
            }
        });

        myCalendar = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                upDateLable();
            }

        };

    }

    public void initializeComponants()
    {
        ico_user= (TextView) findViewById(R.id.reg_ico_user);
        ico_email= (TextView) findViewById(R.id.reg_ico_email);
        ico_mobile= (TextView) findViewById(R.id.ico_mobile_no);
        ico_calendar= (TextView) findViewById(R.id.reg_ico_dob);
        ico_gender= (TextView) findViewById(R.id.reg_ico_gendar);
        ico_list= (TextView) findViewById(R.id.reg_ico_accinfo);
        spn_reg_user_acc= (Spinner) findViewById(R.id.spn_reg_user_acc);
        registration= (Button) findViewById(R.id.btn_registration);
        edt_reg_fname= (EditText) findViewById(R.id.edt_reg_fname);
        edt_reg_lname= (EditText) findViewById(R.id.edt_reg_lname);
        edt_reg_email= (EditText) findViewById(R.id.edt_reg_email);
        edt_mob_no= (EditText) findViewById(R.id.edt_mob_no);
        edt_reg_dob= (TextView) findViewById(R.id.edt_reg_dob);
        rB_male= (RadioButton) findViewById(R.id.rB_male);
        rB_female= (RadioButton) findViewById(R.id.rB_female);
        roundedImageView=(RoundedImageView) findViewById(R.id.setting_user_icon);
        loading =(ProgressWheel) findViewById(R.id.loading);
    }

    public void FireBaseInitialization()
    {
        fireBasetoken = FirebaseInstanceId.getInstance().getToken();
        //FirebaseInstanceId.getInstance().deleteToken();
//        String msg2 = getString(R.string.msg_token_fmt, token);
//        Log.d(TAG, msg2);
        //Toast.makeText(LoginActivity.this, msg2, Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("TockenID", fireBasetoken);
        edit.commit();

        FIreBaseCreateTopic("TrackMMP");
    }

    public void FIreBaseCreateTopic(String topicName)
    {
        FirebaseMessaging.getInstance().subscribeToTopic(topicName);
//        String msg = getString(R.string.msg_subscribed);
//        Log.d(TAG, msg);
        //Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskCompleted(String s) {
        if (!s.equalsIgnoreCase(""))
        {
            try {
                JSONObject json = new JSONObject(s);
                String userId=json.getString("userId");
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("UserId", userId);
                edit.putString("ContactNo", userId);
                edit.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Intent interest=new Intent(RegistrationActivity.this,InterestActivity.class);
            startActivity(interest);
        }
        else
        {
            showRegistationFailedAlert();
        }
        loading.setVisibility(View.GONE);

    }


    public String getLocationName(double lattitude, double longitude) {

        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {

            List<Address> addresses = gcd.getFromLocation(lattitude, longitude,
                    10);

            for (Address adrs : addresses) {
                if (adrs != null) {

                    String city = adrs.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                        System.out.println("city ::  " + cityName);
                    } else {

                    }
                    // // you should also try with addresses.get(0).toSring();

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;

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


    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm=null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)

            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

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

    public void showRegistationFailedAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegistrationActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Registration Unsuccesefull");
        final AlertDialog builder = alertDialog.create();
        // On pressing Settings button
        alertDialog.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        builder.dismiss();
                    }
                });

        alertDialog.show();
    }

    public void regdobclick(View view)
    {
        new DatePickerDialog(RegistrationActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void upDateLable()
    {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        edt_reg_dob.setText(sdf.format(myCalendar.getTime()));
        edt_reg_dob.getText().toString();
    }

}

