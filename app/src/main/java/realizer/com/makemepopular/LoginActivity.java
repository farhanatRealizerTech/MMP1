package realizer.com.makemepopular;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import realizer.com.makemepopular.asynctask.CheckUserAsyntask;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 10/01/2017.
 */
public class LoginActivity extends AppCompatActivity implements OnTaskCompleted
{
    ProgressWheel loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        final EditText editphoneno= (EditText) findViewById(R.id.edt_mob_no);
        TextView phone_ico= (TextView) findViewById(R.id.ico_mobileno);
        TextView signupbtn= (TextView) findViewById(R.id.login_signup_btn);
        TextView loginmapimg= (TextView) findViewById(R.id.login_mapimg);
        Button btn_Submit= (Button) findViewById(R.id.btn_Submit);
        loading =(ProgressWheel) findViewById(R.id.loading);

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                String getValueBack = sharedpreferences.getString("ContactNo", "");*/
                if (Config.isConnectingToInternet(LoginActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    CheckUserAsyntask check=new CheckUserAsyntask(editphoneno.getText().toString(),LoginActivity.this,LoginActivity.this);
                    check.execute();
                }
                else
                {
                    Config.alertDialog(LoginActivity.this,"Network Error","No Internet connection");
                }
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg=new Intent(LoginActivity.this,RegistrationActivity.class);
                startActivity(reg);
            }
        });

        getSupportActionBar().hide();
        phone_ico.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));

        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        editphoneno.setText(mPhoneNumber.toString());



       /* String simID = tMgr.getSimSerialNumber();
        if (simID != null)
            Toast.makeText(this, "SIM card ID: " + simID,
                    Toast.LENGTH_LONG).show();

        String telNumber = tMgr.getLine1Number();
        if (telNumber != null)
            Toast.makeText(this, "Phone number: " + telNumber,
                    Toast.LENGTH_LONG).show();

        String IMEI = tMgr.getDeviceId();
        if (IMEI != null)
            Toast.makeText(this, "IMEI number: " + IMEI,
                    Toast.LENGTH_LONG).show();*/

    }

    @Override
    public void onTaskCompleted(String s) {
        if (!s.equalsIgnoreCase(""))
        {
            try {
                JSONObject json = new JSONObject(s);
                String userId=json.getString("userId");

                if (!userId.equalsIgnoreCase("00000000-0000-0000-0000-000000000000"))
                {
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("UserId", userId);
                    edit.putString("ContactNo", userId);
                    edit.putString("Login", "true");
                    edit.commit();

                    Intent interest=new Intent(LoginActivity.this,DashboardActivity.class);
                    startActivity(interest);

                }
                else
                {
                    showLoginFailedAlert();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            loading.setVisibility(View.GONE);
        }
        else
        {
            showLoginFailedAlert();
            loading.setVisibility(View.GONE);
        }
    }

    public void showLoginFailedAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("Error!");

        // Setting Dialog Message
        alertDialog.setMessage("Login Unsuccesefull");
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
}
