package realizer.com.makemepopular.chat.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.chat.model.ChatMessageViewListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.Utility;

/**
 * Created by Win on 11/26/2015.
 */
public class ChatMessageCenterListAdapter extends BaseAdapter {


    private static ArrayList<ChatMessageViewListModel> messageList;
    private LayoutInflater mhomeworkdetails;
    private String Currentdate;
    String date;
    int counter,unreadCount;
    String userFullName="";
    int datepos;
    ViewHolder holder;
    String username="";
    String thumbnailurl="";
    public ChatMessageCenterListAdapter(Context context, ArrayList<ChatMessageViewListModel> messageList1,int unreadCount) {
        messageList = messageList1;
        mhomeworkdetails = LayoutInflater.from(context);
        Calendar c = Calendar.getInstance();
        this.unreadCount=unreadCount;
        counter = 0;
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Currentdate = df.format(c.getTime());
        date = Currentdate;
        datepos = -1;
        Log.d("Date", Currentdate);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        username = preferences.getString("UserName", "");
        userFullName=preferences.getString("userLoginId", "");
        thumbnailurl = preferences.getString("ThumbnailID","");

    }
    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {

        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public int getViewTypeCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = mhomeworkdetails.inflate(R.layout.chat_messgagecenter_list_layout, null);
            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.txtdate);
            holder.date.setTag(position);
            holder.sendername = (TextView) convertView.findViewById(R.id.txtsenderName);
            holder.time = (TextView) convertView.findViewById(R.id.txttime);
            holder.message = (TextView) convertView.findViewById(R.id.txtMessage);
            holder.newmessage = (LinearLayout) convertView.findViewById(R.id.linlayoutnewmsgbar);
            holder.datelayout = (LinearLayout) convertView.findViewById(R.id.linlayoutdate);
            holder.initial = (TextView) convertView.findViewById(R.id.txtinitial);
            holder.newMessageCountTxt= (TextView) convertView.findViewById(R.id.txt_newMessage_count);
            holder.newMsgCountouter= (LinearLayout) convertView.findViewById(R.id.layout_new_message);

            holder.messageouter= (LinearLayout) convertView.findViewById(R.id.linear_message_outer);
            holder.profilepic = (ImageView) convertView.findViewById(R.id.profile_image_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Log.d("Date", Currentdate);
        Log.d("Date1", messageList.get(position).getSendDate());

        holder.newmessage.setVisibility(View.GONE);

        if(position==0)
        {
            if(messageList.get(position).getSendDate().equals(Currentdate))
            {
                int datetag = (Integer)holder.date.getTag();
                if (counter == 0 || datetag == datepos)
                {
                    hideShowDate(true,messageList.get(position).getSendDate());
                    counter = counter + 1;
                    datepos= (Integer)holder.date.getTag();
                }
                else
                {
                    hideShowDate(false, "");
                }
            }
            else
            {
                hideShowDate(true,messageList.get(position).getSendDate());
            }
            if (messageList.get(position).getIsNewMessage().equals("true"))
            {
                if (unreadCount==1)
                {
                    holder.newMessageCountTxt.setText("You have " + unreadCount + " new message.");
                }
                else
                {
                    holder.newMessageCountTxt.setText("You have " + unreadCount + " new messages.");
                }
                holder.newMsgCountouter.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.newMsgCountouter.setVisibility(View.GONE);
            }
        }
        else if(position>0) {
            if (messageList.get(position - 1).getSendDate().equals(messageList.get(position).getSendDate()) )
            {
                hideShowDate(false, "");
            }
            else
            {
                int datetag = (Integer)holder.date.getTag();
                if (counter == 0 || datetag == datepos)
                {
                    hideShowDate(true,messageList.get(position).getSendDate());
                    counter = counter + 1;
                    datepos= (Integer)holder.date.getTag();
                }
                else
                {
                    hideShowDate(true,messageList.get(position).getSendDate());
                }

            }
            if (messageList.get(position - 1).getIsNewMessage().equals(messageList.get(position).getIsNewMessage()))
            {
                holder.newMsgCountouter.setVisibility(View.GONE);
            }
            else
            {
                if (unreadCount==1)
                {
                    holder.newMessageCountTxt.setText("You have " + unreadCount + " new message.");
                }
                else {
                    holder.newMessageCountTxt.setText("You have " + unreadCount + " new messages.");
                }
                holder.newMsgCountouter.setVisibility(View.VISIBLE);
            }
        }

        String name[] = messageList.get(position).getSenderName().trim().split(" ");

        if(messageList.get(position).getSenderThumbnail() != null && !messageList.get(position).getSenderThumbnail().equals("") && !messageList.get(position).getSenderThumbnail().equalsIgnoreCase("null"))
        {
            /*String urlString="";
            if( username.contains(messageList.get(position).getTname()))
            {
                urlString = thumbnailurl;
            }
            else
            {
                urlString = messageList.get(position).getProfileImage();
            }*/

            String newURL= Utility.getURLImage(messageList.get(position).getSenderThumbnail());
            holder.initial.setVisibility(View.GONE);
            holder.profilepic.setVisibility(View.VISIBLE);
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,holder.profilepic,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length - 1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                holder.initial.setVisibility(View.GONE);
                holder.profilepic.setVisibility(View.VISIBLE);
                holder.profilepic.setImageBitmap(bitmap);
            }
        }
        else {
            holder.initial.setVisibility(View.VISIBLE);
            holder.profilepic.setVisibility(View.GONE);

            char fchar = name[0].toUpperCase().charAt(0);
            char lchar = name[0].toUpperCase().charAt(0);
            for (int i = 0; i < name.length; i++) {
                if (!name[i].equals("") && i == 0)
                    fchar = name[i].toUpperCase().charAt(0);
                else if (!name.equals("") && i == (name.length - 1))
                    lchar = name[i].toUpperCase().charAt(0);

            }

            holder.initial.setText(fchar + "" + lchar);

        }

        //ParentQueriesTeacherNameListModel result=dbQ.GetQueryTableData(messageList.get(position).getTname());
        /*if (result.getFname().equals(""))
        {*/
        holder.sendername.setText(messageList.get(position).getSenderName());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)holder.messageouter.getLayoutParams();

        if (holder.sendername.getText().equals(userFullName))
        {
            holder.messageouter.setBackgroundResource(R.drawable.in_message_bg);
            params.setMargins(150, 10, 10, 10);

            holder.messageouter.setLayoutParams(params);
            holder.messageouter.requestLayout();
        }
        else
        {
            holder.messageouter.setBackgroundResource(R.drawable.out_message_bg);
            holder.sendername.setTextColor(Color.WHITE);
            params.setMargins(10, 10, 150, 10);
            holder.messageouter.setLayoutParams(params);
            holder.messageouter.requestLayout();
        }
       /* }
        else
        {
            holder.sendername.setText(result.getFname());
        }*/

        holder.time.setText(messageList.get(position).getSendTime());

        //holder.message.setText(messageList.get(position).getMessage());

        String serverResponse =messageList.get(position).getMessage();
        String fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(serverResponse);
        holder.message.setText(fromServerUnicodeDecoded);

/*
        if(hList.get(position).getFlag().equals("T"))
       {

           holder.msgT.setText(hList.get(position).getMsg()+"\t");
           holder.msgTt.setText("\t"+hList.get(position).getTime());
           holder.lt.setBackgroundResource(R.drawable.in_message_bg);
           holder.msgP.setText("");
           holder.msgPt.setText("");
           holder.lp.setBackground(null);

       }
        else if(hList.get(position).getFlag().equals("P"))
       {
           holder.msgP.setText(hList.get(position).getMsg()+"\t");
           holder.msgPt.setText(hList.get(position).getTime());
           holder.lp.setBackgroundResource(R.drawable.out_message_bg);
           holder.msgT.setText("");
           holder.msgTt.setText("");
           holder.lt.setBackground(null);
       }*/

        return convertView;
    }

    //

    public void hideShowDate(boolean flag,String setdate)
    {
        if(flag)
        {
            String qdate[]=setdate.split("-");
            String newDate=qdate[0]+"/"+qdate[1]+"/"+qdate[2];
            ViewGroup.LayoutParams layoutParams = holder.datelayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.datelayout.setLayoutParams(layoutParams);
            holder.date.setText(Config.getDate(newDate, "D"));
            date = newDate;

           /* LinearLayout.LayoutParams layoutmargin = (LinearLayout.LayoutParams)holder.layoutallview.getLayoutParams();
            layoutmargin.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutmargin.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutmargin.setMargins(0,20,0,0);
            holder.layoutallview.setLayoutParams(layoutmargin);*/

        }

        else
        {

            ViewGroup.LayoutParams layoutParams = holder.datelayout.getLayoutParams();
            layoutParams.width = 0;
            layoutParams.height = 0;
            holder.datelayout.setLayoutParams(layoutParams);

            /*LinearLayout.LayoutParams layoutmargin = (LinearLayout.LayoutParams)holder.layoutallview.getLayoutParams();
            layoutmargin.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutmargin.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutmargin.setMargins(0,0,0,0);
            holder.layoutallview.setLayoutParams(layoutmargin);*/


        }
    }


    static class ViewHolder
    {
        TextView date,sendername,time,initial,message,newMessageCountTxt;
        LinearLayout newmessage,datelayout,messageouter,newMsgCountouter;
        ImageView profilepic;
    }
}

