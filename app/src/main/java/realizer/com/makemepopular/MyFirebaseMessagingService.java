package realizer.com.makemepopular;

/**
 * Created by Win on 05/12/2016.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import realizer.com.makemepopular.chat.ChatMessageCenterActicity;
import realizer.com.makemepopular.exceptionhandler.NetworkException;
import realizer.com.makemepopular.friendlist.FriendListActivity;
import realizer.com.makemepopular.service.AutoSyncService;
import realizer.com.makemepopular.service.AutoSynckSetCordinatesTask;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    boolean isMessage=false;
    static int notificatinChatID=001;
    final static String GROUP_KEY_PARENT = "FFM";
    static int numChatMessages = 0;
    static int numEmergencyMessages = 0;
    static int numFriendRequestMessages = 0;
    static int numTrackMessages = 0;
    boolean isTracking=false;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String LogCheck=sharedpreferences.getString("Login", "");
        String AppStatus=sharedpreferences.getString("AppStatus","");

        String msgType=remoteMessage.getData().get("Type");
        Intent intent=null;
        if (msgType.equalsIgnoreCase("Emergency"))
        {
           // numEmergencyMessages++;
            // SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", msgType);
            edit.putString("ContactNo", remoteMessage.getData().get("ContactNo"));
            edit.putString("Message",remoteMessage.getData().get("Message"));
            edit.putString("Latitude",remoteMessage.getData().get("Latitude"));
            edit.putString("Longitude", remoteMessage.getData().get("Longitude"));
            edit.putString("ThumbnailUrl",remoteMessage.getData().get("ThumbnailUrl"));
            edit.putString("TroublerName",remoteMessage.getData().get("TroublerName"));
            edit.putString("TroublerUserId",remoteMessage.getData().get("TroublerUserId"));

            edit.commit();
            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }
        else if (msgType.equalsIgnoreCase("FriendRequest"))
        {
            String requsetByName=remoteMessage.getData().get("RequsetByName");
            String thumbnailUrl=remoteMessage.getData().get("ThumbnailUrl");
            String requsetByUserId=remoteMessage.getData().get("RequsetByUserId");

            //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", msgType);
            edit.putString("RequsetByName", requsetByName);
            edit.putString("ThumbnailUrl",thumbnailUrl);
            edit.putString("RequsetByUserId",requsetByUserId);
            edit.putString("FriendAge", remoteMessage.getData().get("Age"));
            edit.putString("FriendAddress",remoteMessage.getData().get("Address"));
            edit.putString("FriendGender",remoteMessage.getData().get("Gender"));
            edit.commit();
            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }
        else if (msgType.equalsIgnoreCase("FriendRequestAccepted"))
        {
            //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", msgType);
            edit.putString("AcceptByName", remoteMessage.getData().get("AcceptByName"));
            edit.putString("Message", remoteMessage.getNotification().getBody());
            edit.putString("AcceptByUserId",remoteMessage.getData().get("AcceptByUserId"));
            edit.putString("ThumbnailUrl",remoteMessage.getData().get("ThumbnailUrl"));
            edit.commit();

            intent=new Intent(MyFirebaseMessagingService.this,FriendListActivity.class);
        }
        else if (msgType.equalsIgnoreCase("EmergencyRecipt"))
        {
            //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", msgType);
            edit.putString("HelperUserId", remoteMessage.getData().get("HelperUserId"));
            edit.putString("HelperUserName",remoteMessage.getData().get("HelperUserName"));
            edit.putString("Thumbnailurl",remoteMessage.getData().get("Thumbnailurl"));
            edit.putString("Message",remoteMessage.getNotification().getBody());
            edit.putString("isReaching",remoteMessage.getData().get("isReaching"));
            edit.commit();

            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }
        else if (msgType.equalsIgnoreCase("TrackingRequest"))
        {
            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }
        else if (msgType.equalsIgnoreCase("ApproveTracking"))
        {
            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }
        else if (msgType.equalsIgnoreCase("TrackingStarted"))
        {
            isTracking=true;
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Type", msgType);
            edit.putString("NotificationId", remoteMessage.getData().get("NotificationId"));
            edit.putString("ThumbnailUrl",remoteMessage.getData().get("ThumbnailUrl"));
            edit.putString("TrackingByUserName",remoteMessage.getData().get("TrackingByUserName"));
            edit.putString("TrackingByUserId", remoteMessage.getData().get("TrackingByUserId"));
            edit.commit();

           /* GPSTracker gpsTracker = new GPSTracker(this);

            if (gpsTracker.getIsGPSTrackingEnabled()) {
                if(Double.valueOf(sharedpreferences.getString("CurrentLatitudeTrackNot","")) != null)
                {
                    AutoSynckSetCordinatesTask auto=new AutoSynckSetCordinatesTask(gpsTracker.latitude,gpsTracker.longitude,
                            MyFirebaseMessagingService.this);
                    auto.execute();
                }
            }*/

            Intent intent11 = new Intent();
            intent11.setAction("com.tutorialspoint.CUSTOM_INTENT");
            sendBroadcast(intent11);

            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }
        else if (msgType.equalsIgnoreCase("FriendRequestRejected"))
        {
            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }
        else if (msgType.equalsIgnoreCase("SendMessage"))
        {
            isMessage=true;
            Bundle b=new Bundle();
            b.putString("MSGReceive", "true");


            String threadId=remoteMessage.getData().get("threadId");
            String participentId=remoteMessage.getData().get("participantId");
            String initiateId=remoteMessage.getData().get("initiateId");
            String ReceiverURL=remoteMessage.getData().get("senderThumbnailUrl");
            String ReceiverTime=remoteMessage.getData().get("timeStamp");
            String messageId=remoteMessage.getData().get("messageId");
            String ActiveThred=sharedpreferences.getString("ActiveThread", "");
            String unreadCount= remoteMessage.getData().get("UnreadMessageCount");

            if (ActiveThred.equals(threadId))
            {
                Singleton obj = Singleton.getInstance();
                if(obj.getResultReceiver() != null)
                {
                    b.putString("ReceiverURL",ReceiverURL);
                    b.putString("ReceiveTime",ReceiverTime);
                    b.putString("ReceiverId",initiateId);
                    b.putString("ParticipentId",participentId);
                    b.putString("MessageId",messageId);
                    b.putString("ReceiveMSG",remoteMessage.getNotification().getBody());
                    b.putString("ReceiverNAME",remoteMessage.getNotification().getTitle());
                    obj.getResultReceiver().send(100, b);
                }
            }
            else if (LogCheck.equals("true"))
            {
                Singleton obj = Singleton.getInstance();
                if(obj.getResultReceiver() != null)
                {
                    obj.getResultReceiver().send(200, null);
                }
                sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle(), threadId, participentId, initiateId,unreadCount);
           /* SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("ActiveThread", threadId);
            edit.commit();*/

            }
            else
            {

            }
        }
        else
        {
            intent=new Intent(MyFirebaseMessagingService.this,DashboardActivity.class);
        }

        if (!isMessage) {
            if (LogCheck.equals("true")) {
                if (AppStatus.equals("Open")) {
                    if(!isTracking)
                    {
                        Bundle b=new Bundle();
                        Singleton obj = Singleton.getInstance();
                        if(obj.getResultReceiver() != null)
                        {
                            obj.getResultReceiver().send(300, b);//300 for emergency
                        }
                    }
                    else
                    {
                        sendNotification(msgType,remoteMessage.getNotification().getBody(), intent, remoteMessage.getNotification().getTitle());
                        Bundle b = new Bundle();
                        b.putString("MSGReceive", "true");
                    }
                }
                else
                {
                    //Calling method to generate notification
                    sendNotification(msgType,remoteMessage.getNotification().getBody(), intent, remoteMessage.getNotification().getTitle());
                    Bundle b = new Bundle();
                    b.putString("MSGReceive", "true");
                }
            }
        }
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String msgType,String messageBody,Intent intent,String title) {

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DashboardActivity.class);
        stackBuilder.addNextIntent(intent);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (msgType.equalsIgnoreCase("Emergency"))
        {

            int num=++numEmergencyMessages;
            builder.setAutoCancel(true);
            builder.setContentTitle(title);
            if (num==1) {
                builder.setContentText(messageBody);
            }
            else {
                builder.setContentText("You have received " + num + " emergency alerts.");
            }
            builder.setSmallIcon(R.drawable.ic_notification_icon);
            builder.setContentIntent(pendingIntent);
            builder.setOngoing(false);  //API level 16
            builder.setNumber(num);
            builder.setSound(defaultSoundUri);
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            builder.setGroup(GROUP_KEY_PARENT);
            builder.setGroupSummary(true);
            builder.build();

        }
        else  if (msgType.equalsIgnoreCase("TrackingStarted"))
        {
            int num=++numTrackMessages;
            builder.setAutoCancel(true);
            builder.setContentTitle(title);
            if (num==1) {
                builder.setContentText(messageBody);
            }
            else {
                builder.setContentText("You have received " + num + " tracking request.");
            }
            builder.setSmallIcon(R.drawable.ic_notification_icon);
            builder.setContentIntent(pendingIntent);
            builder.setOngoing(false);  //API level 16
            builder.setNumber(num);
            builder.setSound(defaultSoundUri);
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            builder.setGroup(GROUP_KEY_PARENT);
            builder.setGroupSummary(true);
            builder.build();
        }
        else
        {

            builder.setSmallIcon(R.drawable.ic_notification_icon);
            builder.setContentTitle(title);
            builder.setContentText(messageBody);
            builder.setAutoCancel(true);
            builder.setSound(defaultSoundUri);
            builder.setContentIntent(pendingIntent);
        }

      /*  NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_ffm_icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);*/

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());
    }

    private void sendNotification(String messageBody,String Title,String threadId,String participentId,String InitiateId,String unread) {
        Intent intent = new Intent(this, ChatMessageCenterActicity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putString("THREADID",threadId );
        bundle.putString("RECEIVERID",participentId);
        bundle.putString("InitiatedID", InitiateId);
        bundle.putString("ActionBarTitle",Title);
        bundle.putInt("UnreadCountThread",Integer.valueOf(unread));
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String serverResponse =messageBody;
        String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        int num=++numChatMessages;
        builder.setAutoCancel(true);
        builder.setContentTitle(Title);
        builder.setColor(Color.rgb(107, 178, 211));
        if (num==1) {
            builder.setContentText(messageBody);
        }
        else {
            builder.setContentText("You have received " + num + " messages.");
        }

        builder.setContentText(fromServerUnicodeDecoded);
        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(false);  //API level 16
        builder.setNumber(num);
        builder.setSound(defaultSoundUri);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setGroup(GROUP_KEY_PARENT);
        builder.setGroupSummary(true);
        builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificatinChatID, builder.build());
    }


    public void setCountZero(String notifyFragment)
    {
        if (notifyFragment.equals("Emergency"))
        {
            numEmergencyMessages=0;
        }
        else  if (notifyFragment.equals("SendMessage"))
        {
            numChatMessages=0;
        }
        else  if (notifyFragment.equals("TrackingStarted"))
        {
            numTrackMessages=0;
        }
    }

    public class AutoSynckSetCordinatesTask extends AsyncTask<Void,Void,StringBuilder> {

        StringBuilder resultbuilder;
        Context mycontext;
        Double latitude,longitude;
        SharedPreferences sharedpreferences;

        public AutoSynckSetCordinatesTask(Double lat,Double log,Context mycontext) {
            this.mycontext=mycontext;
            this.latitude=lat;
            this.longitude=log;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            // dialog=ProgressDialog.show(mycontext,"","Inserting Data...!");
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            resultbuilder =new StringBuilder();
            HttpClient httpClient=new DefaultHttpClient();
            String url= Config.URL_Tracking+"setUserCordinates";
            HttpPost httpPost=new HttpPost(url);
            String json="";
            StringEntity se=null;
            JSONObject jsonObject=new JSONObject();
            try
            {
                sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

                jsonObject.put("userId",sharedpreferences.getString("UserId",""));
                jsonObject.put("latitude",latitude);
                jsonObject.put("longitude",longitude);
                jsonObject.put("city","");

                json=jsonObject.toString();

                se=new StringEntity(json);
                httpPost.setEntity(se);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse=httpClient.execute(httpPost);
                StatusLine statusLine=httpResponse.getStatusLine();
                int statuscode=statusLine.getStatusCode();
                if (statuscode==200)
                {
                    HttpEntity entity=httpResponse.getEntity();
                    InputStream content=entity.getContent();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line=reader.readLine())!=null)
                    {
                        resultbuilder.append(line);
                    }
                }
                else
                {
                    StringBuilder exceptionString = new StringBuilder();
                    HttpEntity entity = httpResponse.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while((line=reader.readLine()) != null)
                    {
                        exceptionString.append(line);
                    }

                    NetworkException.insertNetworkException(mycontext, exceptionString.toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return resultbuilder;
        }

        @Override
        protected void onPostExecute(StringBuilder stringBuilder) {
            super.onPostExecute(stringBuilder);
            if (!stringBuilder.toString().equalsIgnoreCase(""))
            {
                try {
                    JSONObject json = new JSONObject(stringBuilder.toString());
                    String success=json.getString("success");
                    String todaysTrackingCount=json.getString("todaysTrackingCount");
                    String lastWeekTrackingCount=json.getString("lastWeekTrackingCount");
                    String lastMonthTrackingCount=json.getString("lastMonthTrackingCount");

                    if (success.equalsIgnoreCase("true"))
                    {
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this);
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

    }
}