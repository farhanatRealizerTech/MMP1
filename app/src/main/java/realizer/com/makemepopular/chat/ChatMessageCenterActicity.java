package realizer.com.makemepopular.chat;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import realizer.com.makemepopular.MyFirebaseMessagingService;
import realizer.com.makemepopular.R;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.adapter.ChatMessageCenterListAdapter;
import realizer.com.makemepopular.chat.asynctask.GetThreadMessageAsyncTaskPost;
import realizer.com.makemepopular.chat.asynctask.ReceiveMessageAsyncTaskPut;
import realizer.com.makemepopular.chat.asynctask.SendMessgeAsyncTaskPost;
import realizer.com.makemepopular.chat.model.ChatMessageViewListModel;
import realizer.com.makemepopular.emoji.EmojiconEditText;
import realizer.com.makemepopular.emoji.EmojiconGridView;
import realizer.com.makemepopular.emoji.EmojiconsPopup;
import realizer.com.makemepopular.emoji.emoji.Emojicon;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Singleton;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 30/11/2016.
 */
public class ChatMessageCenterActicity extends AppCompatActivity implements AbsListView.OnScrollListener,OnTaskCompleted
{
    DatabaseQueries qr;
    boolean chkUser;
    TextView send;
    ProgressWheel loading;
    EmojiconEditText emojiconEditText;
    int mCurrentX ;
    int  mCurrentY;
    int currentPosition,unreadCount;
    int lstsize,num;
    String dateForMsgId="",timeForMsgId="";
    MessageResultReceiver resultReceiver;
    String ThreadID,ReceiverID,UserID,InitiateID,SendtoMSGID,UserFullName,SenderThumbnail;
    ListView lsttname;
    String IsFirstTimeLogin="";
    SharedPreferences sharedpreferences;

    ArrayList<ChatMessageViewListModel> chatMessages=new ArrayList<>();
    ChatMessageCenterListAdapter adapter;

    LinearLayout messagesendOuter;
    TextView noLongerFriend;

    EditText msg;
    int qid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.activity_chat_message_center);

        qr=new DatabaseQueries(ChatMessageCenterActicity.this);
        //send= (TextView) findViewById(R.id.btnSendText);
        lsttname = (ListView) findViewById(R.id.lstviewquery);
        //msg = (EditText) findViewById(R.id.edtmsgtxt);
        loading = (ProgressWheel) findViewById(R.id.loading);

        messagesendOuter= (LinearLayout) findViewById(R.id.ll_compose_layout);
        noLongerFriend= (TextView) findViewById(R.id.noLongerFriendTxt);

        /****** Emojies Start*******/

        // emojies
        emojiconEditText= (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        final View rootView = findViewById(R.id.root_view);
        final ImageView emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        final ImageView submitButton = (ImageView) findViewById(R.id.submit_btn);


        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        new MyFirebaseMessagingService().setCountZero("SendMessage");
        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiconEditText == null || emojicon == null) {
                    return;
                }

                int start = emojiconEditText.getSelectionStart();
                int end = emojiconEditText.getSelectionEnd();
                if (start < 0) {
                    emojiconEditText.append(emojicon.getEmoji());
                } else {
                    emojiconEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiconEditText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if(!popup.isShowing()){

                    //If keyboard is visible, simply show the emoji popup
                    if(popup.isKeyBoardOpen()){
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else{
                        emojiconEditText.setFocusableInTouchMode(true);
                        emojiconEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else{
                    popup.dismiss();
                }
            }
        });
        /****** Emojies Code End*******/

        qid=0;
        lstsize = chatMessages.size();
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatMessageCenterActicity.this);
        UserID = sharedpreferences.getString("UserId", "");
        UserFullName = sharedpreferences.getString("userLoginId", "");
        SenderThumbnail = sharedpreferences.getString("ThumbnailURL", "");
        IsFirstTimeLogin=sharedpreferences.getString("FirstTimeLogin1","");


        lsttname.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String CLIPBOARD_TEXT="";
                CLIPBOARD_TEXT=chatMessages.get(position).getMessage();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(CLIPBOARD_TEXT, CLIPBOARD_TEXT);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ChatMessageCenterActicity.this, "Message Copied", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Singleton obj = Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        ThreadID = b.getString("THREADID");
        ReceiverID = b.getString("RECEIVERID");
        InitiateID=b.getString("InitiatedID");
        unreadCount=b.getInt("UnreadCountThread");

        //SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(ChatThreadListActivity.this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("ActiveThread", ThreadID);
        edit.commit();

        getSupportActionBar().setTitle(b.getString("ActionBarTitle"));



        if (UserID.equals(ReceiverID))
        {
            SendtoMSGID=InitiateID;
        }
        else if (UserID.equals(InitiateID))
        {
            SendtoMSGID=ReceiverID;
        }
        // for Cheking user is friend or not

        chkUser = qr.ChekFriendinFrndList(SendtoMSGID);
        if (!chkUser)
        {
            /*emojiconEditText.setText("No Longer Friend");
            emojiconEditText.setEnabled(false);
            submitButton.setEnabled(false);*/
            noLongerFriend.setVisibility(View.VISIBLE);
            messagesendOuter.setVisibility(View.GONE);
        }
        else
        {
            noLongerFriend.setVisibility(View.GONE);
            messagesendOuter.setVisibility(View.VISIBLE);
/*            emojiconEditText.setEnabled(true);
            submitButton.setEnabled(true);*/
        }


        GetThreadMessages();

        NotificationManager notifManager= (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();

//On submit, add the edittext text to listview and clear the edittext
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (emojiconEditText.getText().toString().trim().equals(""))
                {
                    Toast.makeText(ChatMessageCenterActicity.this, "Enter Message", Toast.LENGTH_SHORT).show();
                } else {
                    /*if (!chkUser) {
                        Toast.makeText(ChatMessageCenterActicity.this, "This user is not in your friend list..", Toast.LENGTH_SHORT).show();
                    } else
                    {*/
                        String toServer = emojiconEditText.getText().toString().trim();
                        String toServerUnicodeEncoded = StringEscapeUtils.escapeJava(toServer);

                        if (toServer.length() != 0) {
                            loading.setVisibility(View.VISIBLE);
                            emojiconEditText.setEnabled(false);
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
                            String date = df.format(calendar.getTime());
                            if (isConnectingToInternet()) {
                                SendMessgeAsyncTaskPost sendMsg = new SendMessgeAsyncTaskPost(ChatMessageCenterActicity.this, ThreadID, UserID, date.toString(), toServerUnicodeEncoded, SendtoMSGID, ChatMessageCenterActicity.this);
                                sendMsg.execute();

                                String[] datenew = date.split(" ");
                                String newdate = datenew[0];
                                String[] senddate = newdate.split("/");

                        /*ChatMessageViewListModel o=new ChatMessageViewListModel();
                        o.setSendDate(senddate[2] + "-" + senddate[1] + "-" + senddate[0]);
                        o.setSendTime(GetMSGTime(date));
                        o.setMessage(toServer.toString());
                        o.setThreadId(ThreadID);
                        o.setSenderName(UserFullName);
                        o.setSenderThumbnail(SenderThumbnail);
                        chatMessages.add(o);*/

                                dateForMsgId = senddate[1] + "-" + senddate[0] + "-" + senddate[2];
                                timeForMsgId = GetMSGTime(date);
                                qr.insertMessageDtls("", UserID, localToGMT().toString(), toServerUnicodeEncoded, ThreadID, SendtoMSGID, UserFullName, SenderThumbnail, senddate[1] + "-" + senddate[0] + "-" + senddate[2], GetMSGTime(date),"false");
                                emojiconEditText.getText().clear();

                            } else {
                            }
                        }
                    /*}*/
                }

            }
        });
    }
    public static Date localToGMT() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gmt = new Date(sdf.format(date));
        return gmt;
    }
    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }
    public boolean isConnectingToInternet(){

        ConnectivityManager connectivity =
                (ConnectivityManager) this.getSystemService(
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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mCurrentX = view.getScrollX();
        mCurrentY = view.getScrollY();
        currentPosition = lsttname.getSelectedItemPosition();
        Log.d("Position", "" + currentPosition);
    }

    public void GetThreadMessages()
    {
        if (Config.isConnectingToInternet(ChatMessageCenterActicity.this))
        {
            if (IsFirstTimeLogin.equals("true")) {
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("FirstTimeLogin1", "False");
                edit.commit();
                loading.setVisibility(View.VISIBLE);
                GetThreadMessageAsyncTaskPost getMessage = new GetThreadMessageAsyncTaskPost(ChatMessageCenterActicity.this, ThreadID,"", ChatMessageCenterActicity.this);
                getMessage.execute();
            }
            else
            {
                String lastMsgTime=qr.getLastMsgTimeStamp(ThreadID,SendtoMSGID);
                loading.setVisibility(View.VISIBLE);
                GetThreadMessageAsyncTaskPost getMessage = new GetThreadMessageAsyncTaskPost(ChatMessageCenterActicity.this, ThreadID,lastMsgTime, ChatMessageCenterActicity.this);
                getMessage.execute();
            }
        }
        else
        {
            DisplayMessagesinList();
            Config.alertDialog(ChatMessageCenterActicity.this, "Network Error", "No Internet Connection..!");
        }
    }
    @Override
    public void onTaskCompleted(String s) {
        String tp;
        if (s.equals("@@@MessageList")) {
        }
        else
        {

            String[] onTask = s.split("@@@");
            if (onTask[1].equals("MessageList"))
            {
                loading.setVisibility(View.GONE);

                JSONArray rootObj = null;
                Log.d("String", onTask[0]);
                try {

                    rootObj = new JSONArray(onTask[0]);
                    int i = rootObj.length();

                    for (int j = 0; j < i; j++) {
                        JSONObject obj = rootObj.getJSONObject(j);
                        String date = Utility.convertUTCdate(obj.getString("timeStamp").replace("T", " "));

                        String datet[] = date.split(" ");
                        String time[] = datet[1].split(":");
                        int t1 = Integer.valueOf(time[0]);
                        String sentTm = "";
                        if (t1 == 12) {
                            tp = "PM";
                            sentTm = "" + t1 + ":" + time[1] + " " + tp;
                        } else if (t1 > 12) {
                            int t2 = t1 - 12;
                            tp = "PM";
                            sentTm = "" + t2 + ":" + time[1] + " " + tp;
                        } else {
                            tp = "AM";
                            sentTm = time[0] + ":" + time[1] + " " + tp;
                        }

                        String serverResponse = obj.getString("message");
                        String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
                        if (SendtoMSGID.equals(obj.getString("senderId"))) {
                            boolean chk = qr.ChekMessageInMessageList(obj.getString("messageId"));
                            if (!chk) {
                                qr.insertMessageDtls(obj.getString("messageId"), obj.getString("senderId"), obj.getString("timeStamp"), fromServerUnicodeDecoded, obj.getString("threadId"), obj.getString("receiverId"), obj.getString("senderName"), obj.getString("senderThumbnail"), datet[0], sentTm, obj.getString("isNewMessage"));
                            } else {
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    num = 1;
                    Log.e("JSON", e.toString());
                    Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                    Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                    Log.e("Login(JCause)", e.getCause().toString());
                    Log.wtf("Login(JMsg)", e.getMessage());
                }
            } else if (onTask[1].equals("SendMsg"))
            {
                emojiconEditText.setEnabled(true);
                loading.setVisibility(View.GONE);
                if (onTask[0].equals("")) {

                } else {
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(onTask[0]);
                        String Success = obj.getString("success");
                        if (Success.equals("true")) {
                            long update = qr.updateMessageId(obj.getString("messageId"), dateForMsgId, timeForMsgId);
                            if (update > 0) {
                                //Toast.makeText(ChatMessageCenterActicity.this, "Message Id Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                // Toast.makeText(ChatMessageCenterActicity.this, "Message not Id Updated", Toast.LENGTH_SHORT).show();
                            }
                        /*String serverResponse = obj.getString("message");
                        String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
                        qr.insertMessageDtls(obj.getString("messageId"),obj.getString("senderId"), obj.getString("timeStamp"),fromServerUnicodeDecoded,obj.getString("threadId"),obj.getString("receiverId"),obj.getString("senderName"),obj.getString("senderThumbnail"),datet[0],sentTm);*/

                        } else {
                            Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Message Not Sent");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        num = 1;
                        Log.e("JSON", e.toString());
                        Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                        Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                        Log.e("Login(JCause)", e.getCause().toString());
                        Log.wtf("Login(JMsg)", e.getMessage());
                    }
                }
            } else if (onTask[1].equals("NoFriend"))
            {
                loading.setVisibility(View.GONE);
                if (onTask[0].equals("")) {

                } else {

                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(onTask[0]);
                        String Success = obj.getString("message");
                        if (Success.equals("No Longer Friend")) {
                            qr.deleteUserFromTable(SendtoMSGID);
                            chkUser = false;
                            emojiconEditText.setText("No Longer Friends");
                            emojiconEditText.setEnabled(false);
                        } else {
                            //Config.alertDialog(ChatMessageCenterActicity.this, "Error", "Message Not Sent");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        num = 1;
                        Log.e("JSON", e.toString());
                        Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                        Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                        Log.e("Login(JCause)", e.getCause().toString());
                        Log.wtf("Login(JMsg)", e.getMessage());
                    }
                }
            } else if (onTask[1].equals("Blocked")) {
                Toast.makeText(ChatMessageCenterActicity.this, "You can't send message.\n This user blocked you", Toast.LENGTH_SHORT).show();
            } else {
            }
        }

        DisplayMessagesinList();

    }
    public void DisplayMessagesinList()
    {
        chatMessages=new ArrayList<>();
        chatMessages=qr.getThreadMessage(ThreadID);
        if(chatMessages.size()!=0)
        {
            adapter = new ChatMessageCenterListAdapter(this,chatMessages,unreadCount);
            lsttname.setAdapter(adapter);

            lsttname.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            //lsttname.setFastScrollEnabled(true);
            //lsttname.setScrollY(lsttname.getCount());
            lsttname.setSelection(lsttname.getCount() - 1);
            //lsttname.smoothScrollToPosition(lsttname.getCount());
            lsttname.setOnScrollListener(this);
            lstsize =  chatMessages.size();
        }
        else
        {
            //noData.setVisibility(View.VISIBLE);
            //lsttname.setVisibility(View.GONE);
        }
        qr.updateisNewMessage(ThreadID,"true");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("ActiveThread", "");
        edit.commit();
        finish();
    }

    class UpdateUI implements Runnable
    {
        String update;
        Bundle b;

        public UpdateUI(String update,Bundle data) {

            this.update = update;
            b=data;
        }

        public void run() {

            if(update.equals("RecieveMessage")) {

                String tp;
                String url=b.getString("ReceiverURL");
                String time1=b.getString("ReceiveTime");


                String date = Utility.convertUTCdate(time1.replace("T", " "));
                String datet[] = date.split(" ");
                String time[] = datet[1].split(":");
                int t1 = Integer.valueOf(time[0]);
                String sentTm = "";
                if (t1 == 12) {
                    tp = "PM";
                    sentTm = "" + t1 + ":" + time[1] + " " + tp;
                } else if (t1 > 12) {
                    int t2 = t1 - 12;
                    tp = "PM";
                    sentTm = "" + t2 + ":" + time[1] + " " + tp;
                } else {
                    tp = "AM";
                    sentTm = time[0] + ":" + time[1] + " " + tp;
                }
                String serverResponse = b.getString("ReceiveMSG");
                String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
                String messageId=b.getString("MessageId");
                String ReceiverId=b.getString("ReceiverId");
                String participentId=b.getString("ParticipentId");
                String SenderId="";
                String ReceivId="";
                if (UserID.equals(ReceiverId))
                {
                    SenderId=participentId;
                    ReceivId=ReceiverId;
                }
                else if (UserID.equals(participentId))
                {
                    SenderId=ReceiverId;
                    ReceivId=participentId;
                }
                boolean chk = qr.ChekMessageInMessageList(messageId);
                if (!chk) {
                    qr.insertMessageDtls(messageId, SenderId, time1, fromServerUnicodeDecoded, ThreadID, ReceivId, b.getString("ReceiverNAME"), url, datet[0], sentTm,"false");
                } else {
                }
                ReceiveMessageAsyncTaskPut receiveMsg=new ReceiveMessageAsyncTaskPut(getApplicationContext(),messageId,true);
                receiveMsg.execute();
                DisplayMessagesinList();
                //GetThreadMessages();
            }

            else if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,ChatMessageCenterActicity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,FriendListActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,ChatMessageCenterActicity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,ChatMessageCenterActicity.this);
                }
                else if (notType.equalsIgnoreCase("FriendRequestAccepted"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("AcceptByName", "");

                    Config.showAccptedRequestAlert(msg,helpername,ChatMessageCenterActicity.this);
                }
            }
            else if(update.equals("SendMessageMessage")) {

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("ActiveThread", "");
                edit.commit();
                finish();
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //Recive the result when new Message Arrives
    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 100){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("RecieveMessage",resultData));
            }
            if(resultCode == 200){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("SendMessageMessage",resultData));
            }
            if(resultCode == 300){
                ChatMessageCenterActicity.this.runOnUiThread(new UpdateUI("Emergency",resultData));
            }

        }
    }
    public String GetMSGTime(String time1)
    {
        String returntime;
        String tp;
        String datet[] = time1.split(" ");

        String time[] = datet[1].split(":");
        int t1 = Integer.valueOf(time[0]);
        if (t1==12)
        {
            tp = "PM";
            returntime=("" + t1 + ":" + time[1] + " " + tp);
        } else if (t1 > 12) {
            int t2 = t1-12;
            tp = "PM";
            returntime=("" + t2 + ":" + time[1] + " " + tp);
        }
        else
        {
            tp = "AM";
            returntime=(time[0] + ":" + time[1] + " " + tp);
        }
        return returntime;
    }
}
