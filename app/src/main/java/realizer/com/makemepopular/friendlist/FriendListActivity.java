package realizer.com.makemepopular.friendlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.asynctask.GetFriendListAsynTask;
import realizer.com.makemepopular.friendlist.adapter.FriendListModelAdapter;
import realizer.com.makemepopular.friendlist.model.FriendListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 12/01/2017.
 */
public class FriendListActivity extends AppCompatActivity implements OnTaskCompleted
{
    ListView listviewFrnd;
    ArrayList<FriendListModel> friendlist=new ArrayList<>();
    FriendListModelAdapter friendadapter;
    ProgressWheel loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        listviewFrnd= (ListView) findViewById(R.id.list_friendlist);
        loading =(ProgressWheel) findViewById(R.id.loading);

        if (Config.isConnectingToInternet(FriendListActivity.this))
        {
            loading.setVisibility(View.VISIBLE);
            GetFriendListAsynTask getfrnd=new GetFriendListAsynTask(FriendListActivity.this,FriendListActivity.this);
            getfrnd.execute();
        }
        else
        {
            Config.alertDialog(FriendListActivity.this,"Network Error","No Internet connection");
        }

    }

    @Override
    public void onTaskCompleted(String s) {

        if (!s.equalsIgnoreCase("") || ! s.equalsIgnoreCase("[]"))
        {
            JSONArray jsonarray = null;
            try {
                jsonarray = new JSONArray(s);

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

            friendadapter= new FriendListModelAdapter(this,friendlist);
            listviewFrnd.setAdapter(friendadapter);
            loading.setVisibility(View.GONE);
        }
        else
        {
            loading.setVisibility(View.GONE);
        }
    }
}
