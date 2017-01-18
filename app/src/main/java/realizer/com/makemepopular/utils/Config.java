package realizer.com.makemepopular.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Win on 11/3/2015.
 */
public class Config {
    // File upload url (replace the ip with your server address)
    public static final String URL = "http://45.35.4.250/Pathology/api/Path/";
//    public static final String URL = "http://104.217.254.180/SJRestWCF/svcEmp.svc/";
    // public static final String URL = "http://192.168.1.14/SJRestWCF/svcEmp.svc/";

    // File upload url (replace the ip with your server address)
    public static final String FILE_UPLOAD_URL = "http://192.168.0.104/AndroidFileUpload/fileUpload.php";

    public static final String URL_Account ="http://45.35.4.250/MMP/api/Account/";

    //mmp uri
    public static final String URL_Mmp ="http://45.35.4.250/MMP/api/Mmp/";

    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "School Diary";

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
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

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
}
