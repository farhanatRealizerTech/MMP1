package realizer.com.makemepopular.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import realizer.com.makemepopular.MyFirebaseMessagingService;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.AcceptRejectFriendRequestAsyntask;
import realizer.com.makemepopular.asynctask.EmergencyAcknowledgementAsynctask;
import realizer.com.makemepopular.asynctask.GetInterestListAsyncTask;
import realizer.com.makemepopular.asynctask.OverAllAcceptRejectFriendRequestAsyntask;
import realizer.com.makemepopular.asynctask.OverAllEmergencyAcknowledgementAsynctask;
import realizer.com.makemepopular.asynctask.TrackFriendAsynctask;
import realizer.com.makemepopular.friendlist.adapter.FriendListModelAdapter;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.service.TrackShowMap;
import realizer.com.makemepopular.view.RoundedImageView;

/**
 * Created by Win on 11/3/2015.
 */
public class Config{
    // File upload url (replace the ip with your server address)
    public static final String URL = "http://45.35.4.250/Pathology/api/Path/";
    public static Context configContext;
//    public static final String URL = "http://104.217.254.180/SJRestWCF/svcEmp.svc/";
    // public static final String URL = "http://192.168.1.14/SJRestWCF/svcEmp.svc/";

    // File upload url (replace the ip with your server address)
    public static final String FILE_UPLOAD_URL = "http://192.168.0.104/AndroidFileUpload/fileUpload.php";

    public static final String URL_Account ="http://45.35.4.250/MMP/api/Account/";
    public static final String URL_Chat ="http://45.35.4.250/MMP/api/Chat/";
    //mmp uri
    public static final String URL_Mmp ="http://45.35.4.250/MMP/api/Mmp/";

    public static final String URL_Tracking ="http://45.35.4.250/MMP/api/Tracking/";

    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "MMP";

    public static final String SENDER_ID = "817406839541";

    public static final int DRAWABLE_RIGHT = 2;
    /**
     * Tag used on log messages.
     */
    public  static final String TAG = "GCMDemo";

    /**
     * Intent used to display a message in the screen.
     */

    /**
     * Intent's extra that contains the message to be displayed.
     */
    public static final String EXTRA_MESSAGE = "message";

    public static SpannableString actionBarTitle(String title,Context context) {
        SpannableString s = new SpannableString(title);
        Typeface face= Typeface.createFromAsset(context.getAssets(), "fonts/font.ttf");
        s.setSpan(new CustomTypefaceSpan("", face), 0, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return s;
    }

    public static void hideSoftKeyboardWithoutReq(Context context, View view) {
        try {
            if (view != null) {
                final InputMethodManager inputMethodManager =
                        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {

        }
    }

    /**
     * @param context
     * @param view
     */
    public static void showSoftKeyboard(Context context, View view) {
        try {
            if (view.requestFocus()) {
                InputMethodManager imm = (InputMethodManager)
                        context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getDate(String date, String FLAG) {
        String datetimevalue = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Calendar c = Calendar.getInstance();
            String Currentdate = df.format(c.getTime());
            SimpleDateFormat df1 = new SimpleDateFormat("dd MMM yyyy");
            String outdate = date.split(" ")[0];
            Date outDateinput = df.parse(outdate);
            String outtime ="";
            if(date.split(" ").length>1) {
                String time[] = date.split(" ")[1].split(":");
                int t1 = Integer.valueOf(time[0]);
                String tp = "";
                if (t1==12)
                {
                    tp = "PM";
                    outtime = "" + t1 + ":" + time[1] + " " + tp;
                }
                else if (t1 > 12) {
                    int t2 = t1 - 12;
                    tp = "PM";
                    outtime = "" + t2 + ":" + time[1] + " " + tp;
                } else {
                    tp = "AM";
                    outtime = time[0] + ":" + time[1] + " " + tp;
                }
            }

            if (FLAG.equals("D") || FLAG.equalsIgnoreCase("DT")) {

                //Current Date Message
                if (outdate.equals(Currentdate)) {
                    datetimevalue = "Today";

                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);

                    //Yesterdays Message
                    if (outdate.equals(df.format(cal.getTime()))) {
                        datetimevalue = "Yesterday";

                    } else {

                        cal.add(Calendar.DATE, -1);

                        for (int i = 0; i < 5; i++) {
                            if (outdate.equals(df.format(cal.getTime()))) {
                                int day = cal.get(Calendar.DAY_OF_WEEK);
                                datetimevalue = getDayOfWeek(day);

                                break;
                            } else {
                                if (i == 4) {
                                    datetimevalue = df1.format(outDateinput);

                                } else
                                    cal.add(Calendar.DATE, -1);
                            }
                        }
                    }
                }
            }

            if (FLAG.equalsIgnoreCase("DT"))
                datetimevalue = datetimevalue + " " + outtime;
            else if (FLAG.equalsIgnoreCase("T"))
                datetimevalue = outtime;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return datetimevalue;
    }


    public static String getDayOfWeek(int day) {
        String dayOfWeek = "";

        switch (day) {
            case 1:
                dayOfWeek = "Sunday";
                break;
            case 2:
                dayOfWeek = "Monday";
                break;
            case 3:
                dayOfWeek = "Tuesday";
                break;
            case 4:
                dayOfWeek = "Wednesday";
                break;
            case 5:
                dayOfWeek = "Thursday";
                break;
            case 6:
                dayOfWeek = "Friday";
                break;
            case 7:
                dayOfWeek = "Saturday";
                break;
        }

        return dayOfWeek;
    }

    public static boolean isConnectingToInternet(Context context){

        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
           // Config.alertDialog(context,"List",info.toString());
            Log.d("Test",info.toString());
            if (info != null)
               // for (int i = 0; i < info.length; i++)
              //  {
                    if ((info[0].getTypeName().equals("MOBILE")  || info[1].getTypeName().equals("WIFI")))
                    {
                        if (info[0].getState() == NetworkInfo.State.CONNECTED)
                        {
                            return true;
                        }
                        else if (info[1].getState() == NetworkInfo.State.CONNECTED)
                        {
                            return true;
                        }
                    }
                //}
        }
        return false;
    }

    /**
     * @param context
     * @param title
     * @param message
     */
    public static void alertDialog(final Context context, String title, String message) {
        AlertDialog.Builder adbdialog;
        adbdialog = new AlertDialog.Builder(context);
        adbdialog.setTitle(title);
        adbdialog.setMessage(message);
        //adbdialog.setIcon(android.R.drawable.ic_dialog_info);
        adbdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adbdialog.show();

    }


    public static String getMonth(int mon) {
        String dayOfWeek = "";

        switch (mon) {
            case 1:
                dayOfWeek = "Jan";
                break;
            case 2:
                dayOfWeek = "Feb";
                break;
            case 3:
                dayOfWeek = "Mar";
                break;
            case 4:
                dayOfWeek = "Apr";
                break;
            case 5:
                dayOfWeek = "May";
                break;
            case 6:
                dayOfWeek = "Jun";
                break;
            case 7:
                dayOfWeek = "Jul";
                break;
            case 8:
                dayOfWeek = "Aug";
                break;
            case 9:
                dayOfWeek = "Sept";
                break;
            case 10:
                dayOfWeek = "Oct";
                break;
            case 11:
                dayOfWeek = "Nov";
                break;
            case 12:
                dayOfWeek = "Dec";
                break;
        }

        return dayOfWeek;
    }
    public static void showEmergencyAcceptReject(String msg,String troubler,final String id, final Context context) {
        configContext=context;
       // new MyFirebaseMessagingService().setCountZero("Emergency");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialoglayout = inflater.inflate(R.layout.alert_acknowledge_diaolg_layout, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialoglayout);

        Button ack= (Button) dialoglayout.findViewById(R.id.btn_ack);
        Button ackmap= (Button) dialoglayout.findViewById(R.id.btn_ackmap);
        Button reject= (Button) dialoglayout.findViewById(R.id.btn_reject);
        TextView titleName=(TextView) dialoglayout.findViewById(R.id.titleName);
        TextView alertMsg=(TextView) dialoglayout.findViewById(R.id.alertMsg);

        final AlertDialog alertDialog = builder.create();
        titleName.setText("Emergency Alert From " + troubler);
        alertMsg.setText(msg);

        ack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(context))
                {
                    OverAllEmergencyAcknowledgementAsynctask aer=new OverAllEmergencyAcknowledgementAsynctask("Acknowledge",id,true,"",context);
                    aer.execute();
                    alertDialog.dismiss();
                }
                else
                {
                    Config.alertDialog(context, "Network Error", "No Internet connection");
                }
            }
        });

        ackmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(context))
                {
                    OverAllEmergencyAcknowledgementAsynctask aer = new OverAllEmergencyAcknowledgementAsynctask("ACK & Map", id, true, "", context);
                    aer.execute();
                    alertDialog.dismiss();
                }
                else
                {
                    Config.alertDialog(context, "Network Error", "No Internet connection");
                }

            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(context))
                {
                    OverAllEmergencyAcknowledgementAsynctask aer = new OverAllEmergencyAcknowledgementAsynctask("Reject", id, false, "", context);
                    aer.execute();
                    alertDialog.dismiss();
                }
                else
                {
                    Config.alertDialog(context, "Network Error", "No Internet connection");
                }

            }
        });

        alertDialog.show();
    }
    public static void showacceptrejectFriendRequest(String name,String thumbnail,final Context context)
    {
        configContext=context;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String age=sharedpreferences.getString("FriendAge","");
        String gender=sharedpreferences.getString("FriendGender","");
        String address=sharedpreferences.getString("FriendAddress","");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View dialoglayout = inflater.inflate(R.layout.accept_reject_alert_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialoglayout);
        Button btn_accpet= (Button) dialoglayout.findViewById(R.id.btn_accpet);

        Button btn_reject= (Button) dialoglayout.findViewById(R.id.btn_reject);
        TextView title= (TextView) dialoglayout.findViewById(R.id.titleName);
        //title.setText("Friend Request By "+name);
        TextView friendName= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_frndname);
        friendName.setText(name);

        TextView txt_addfrnd_gender= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_gender);
        txt_addfrnd_gender.setText(gender+","+age+" Yrs");

        TextView txt_addfrnd_frndaddress= (TextView) dialoglayout.findViewById(R.id.txt_addfrnd_frndaddress);
        txt_addfrnd_frndaddress.setText(address);

        RoundedImageView dpImage=(RoundedImageView)dialoglayout.findViewById(R.id.add_friend_img);

        if (thumbnail.equalsIgnoreCase("")|| thumbnail==null)
        {

        }
        else
        {
            String newURL= Utility.getURLImage(thumbnail);
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,dpImage,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length-1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                dpImage.setImageBitmap(bitmap);
            }
        }

        final AlertDialog alertDialog = builder.create();
        //alertDialog.setCancelable(false);
        btn_accpet.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
       // SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String frndID=sharedpreferences.getString("RequsetByUserId", "");
        btn_accpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(context))
                {
                    //loadingemergeny.setVisibility(View.VISIBLE);
                    OverAllAcceptRejectFriendRequestAsyntask sendalert=new OverAllAcceptRejectFriendRequestAsyntask("","AcceptRequest",frndID,false,true,context);
                    sendalert.execute();
                    alertDialog.dismiss();
                }
                else
                {
                    Config.alertDialog(context, "Network Error", "No Internet connection");
                }
            }
        });

        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isConnectingToInternet(context))
                {
                    //loadingemergeny.setVisibility(View.VISIBLE);
                    OverAllAcceptRejectFriendRequestAsyntask sendalert=new OverAllAcceptRejectFriendRequestAsyntask("","RejectRequest",frndID,false,false,context);
                    sendalert.execute();
                    alertDialog.dismiss();
                }
                else
                {
                    Config.alertDialog(context, "Network Error", "No Internet connection");
                }
            }
        });


        alertDialog.show();
    }

    public static void showEmergencyAckAlert(final String msg,String troubler,final Context context) {
        configContext=context;
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);

        // Setting Dialog Title
        if (msg.contains("not"))
            alertDialog.setTitle("Emergency Ignored From "+troubler);
        else
            alertDialog.setTitle("Emergency Acknowledge From "+troubler);
        // Setting Dialog Message
        alertDialog.setMessage(msg);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);
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


    public static void showAccptedRequestAlert(final String msg,String troubler,final Context context) {
        configContext=context;
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("Friend Request");

        // Setting Dialog Message
        alertDialog.setMessage(msg);

        // On pressing Settings button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor edit = sharedpreferences.edit();
                        edit.putString("Type", "");
                        edit.putString("AcceptByName", "");
                        edit.putString("AcceptByUserId","");
                        edit.putString("ThumbnailUrl","");
                        edit.commit();

                        dialog.dismiss();
                    }
                });


        alertDialog.show();
    }



   /* @Override
    public void onTaskCompleted(String s) {
        Context context;
        String arr[]=s.split("@@@");
        if (arr[1].equalsIgnoreCase("AcceptReject"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest"))
                    Toast.makeText(configContext, "Friend Request Accepted Successfully", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(configContext, "Friend Request Rejected Successfully", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (arr[2].equalsIgnoreCase("AcceptRequest"))
                    Toast.makeText(configContext, "Friend Request Not Accepted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(configContext, "Friend Request Not Rejected", Toast.LENGTH_SHORT).show();
                //EmergencyDialog();
            }
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(configContext);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", "");
            edit.putString("RequsetByName", "");
            edit.putString("ThumbnailUrl","");
            edit.putString("RequsetByUserId","");
            edit.commit();
        }
        else if (arr[1].equalsIgnoreCase("AcceptReject"))
        {
            if (arr[0].equalsIgnoreCase("true"))
            {
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(configContext);
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
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(configContext);
                if (arr[2].equalsIgnoreCase("ACK & Map")) {
                    Intent intent = new Intent(configContext, TrackShowMap.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("FriendName", sharedpreferences.getString("TroublerName", ""));
                    bundle.putDouble("LATITUDE", Double.valueOf(sharedpreferences.getString("Latitude", "")));
                    bundle.putDouble("LONGITUDE", Double.valueOf(sharedpreferences.getString("Longitude", "")));
                    intent.putExtras(bundle);
                    configContext.startActivity(intent);
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
    }*/

}
