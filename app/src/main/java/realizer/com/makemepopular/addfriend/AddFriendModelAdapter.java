package realizer.com.makemepopular.addfriend;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;

/**
 * Created by Win on 13/01/2017.
 */
public class AddFriendModelAdapter extends BaseAdapter {
    private static ArrayList<AddFriendModel> friendList;
    private LayoutInflater friendListInflater;
    private Context context1;
    View convrtview;
    ViewHolder holder;

    public AddFriendModelAdapter(Context context1,ArrayList<AddFriendModel> frndlst) {
        friendList=frndlst;
        this.context1 = context1;
        friendListInflater=LayoutInflater.from(context1);
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*@Override
    public int getViewTypeCount() {
        return friendList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }*/


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convrtview = convertView;
        if (convertView == null) {
            convertView = friendListInflater.inflate(R.layout.addfriend_adapter, null);
            holder = new ViewHolder();
            holder.txtfriendname= (TextView) convertView.findViewById(R.id.txt_addfrnd_frndname);
            holder.txtAddFriend= (TextView) convertView.findViewById(R.id.txt_addfrnd_addfriendbtn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtfriendname.setText(friendList.get(position).getFriendName());
        holder.txtAddFriend.setTypeface(FontManager.getTypeface(context1, FontManager.FONTAWESOME));
        if (friendList.get(position).getStatus().equalsIgnoreCase("pending"))
        {
            holder.txtAddFriend.setText(R.string.fa_check_ico);
            holder.txtAddFriend.setTextColor(Color.GREEN);
        }
        else
        {
            holder.txtAddFriend.setText(R.string.fa_addfriend_ico);
            holder.txtAddFriend.setTextColor(Color.WHITE);
        }

        holder.txtAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context1, ""+holder.txtfriendname.getText().toString(), Toast.LENGTH_SHORT).show();

                if (! holder.txtAddFriend.getText().toString().equalsIgnoreCase(context1.getString(R.string.fa_check_ico)) && friendList.get(position).getStatus().equalsIgnoreCase("null"))
                {
                    if (Config.isConnectingToInternet(context1))
                    {
                        SendFriendRequest1Asyntask sendreq=new SendFriendRequest1Asyntask(friendList.get(position).getFriendid(),position,context1);
                        sendreq.execute();
                    }
                    else
                    {
                        Config.alertDialog(context1,"Network Error","No Internet connection");
                    }
                }
                else
                {
                    //T
                }
            }
        });

        return convertView;
    }
    static class ViewHolder {

        TextView txtfriendname,txtAddFriend;
    }

    class SendFriendRequest1Asyntask extends AsyncTask<Void,Void,StringBuilder> {

        StringBuilder resultbuilder;
        Context mycontext;
        // private OnTaskCompleted callback;
        SharedPreferences sharedpreferences;
        String friendID;
        int posi;

        public SendFriendRequest1Asyntask(String frndid,int pos,Context mycontext) {
            this.mycontext=mycontext;
            //this.callback=cb;
            this.posi=pos;
            this.friendID=frndid;
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
            String url= Config.URL_Mmp+"sendFriendRequest";
            HttpPost httpPost=new HttpPost(url);
            String json="";
            StringEntity se=null;
            JSONObject jsonObject=new JSONObject();
            try
            {
                sharedpreferences = PreferenceManager.getDefaultSharedPreferences(mycontext);

                jsonObject.put("UserId",sharedpreferences.getString("UserId",""));
                jsonObject.put("friendId",friendID);
                jsonObject.put("isEmergencyContact",false);

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
            //  dialog.dismiss();
            AddFriendModel afm=new AddFriendModel();
            afm.setFriendName(friendList.get(posi).getFriendName());
            afm.setFriendid(friendList.get(posi).getFriendid());
            afm.setStatus("pending");
            friendList.set(posi,afm);

            holder.txtAddFriend.setText(R.string.fa_check_ico);
            holder.txtAddFriend.setTextColor(Color.GREEN);
            notifyDataSetChanged();
            //callback.onTaskCompleted(stringBuilder.toString());
        }
    }
}



