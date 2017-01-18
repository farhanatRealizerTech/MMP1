package realizer.com.makemepopular;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import realizer.com.makemepopular.asynctask.SetInterestAsyntask;
import realizer.com.makemepopular.interest.InterestAdapter;
import realizer.com.makemepopular.interest.InterestModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by Win on 11/01/2017.
 */
public class InterestActivity extends AppCompatActivity implements OnTaskCompleted
{
    ArrayList<InterestModel> interestmodel=new ArrayList<>();
    String selectlist;
    TextView ico_trecking,ico_dating,ico_sports,ico_music,ico_dancing,ico_bikers,ico_gossip,ico_socialwork,ico_technical;
    ProgressWheel loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interest_dialog);


        getSupportActionBar().hide();

        String[] Interests = getResources().getStringArray(R.array.interest_text);
        String[] Interestico=getResources().getStringArray(R.array.icofont);

        for(int i=0;i<Interests.length;i++){

            InterestModel im=new InterestModel();
            im.setInteresticoText(Interestico[i]);
            im.setInterestText(Interests[i]);
            interestmodel.add(im);
        }
        loading =(ProgressWheel) findViewById(R.id.loading);
        final GridView interestgridview= (GridView) findViewById(R.id.interest_gridview);

        interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
        interestgridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (interestmodel.get(position).is_selected()) {
                    interestmodel.get(position).setIs_selected(false);
                    interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
                } else {
                    interestmodel.get(position).setIs_selected(true);
                    interestgridview.setAdapter(new InterestAdapter(InterestActivity.this, interestmodel));
                }
            }
        });



        Button btn_addinterest= (Button) findViewById(R.id.btn_addinterest);
        btn_addinterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  StringBuilder sb=new StringBuilder();
                for (int i=0;i<interestmodel.size();i++)
                {
                    if (interestmodel.get(i).is_selected())
                    {
                        sb.append(interestmodel.get(i).getInterestText().toString()+"\n");
                    }

                }
                selectlist=sb.toString();
                Toast.makeText(InterestActivity.this, ""+selectlist, Toast.LENGTH_SHORT).show();*/

                ArrayList<String> list = new ArrayList<String>();
                for (int i=0;i<interestmodel.size();i++)
                {
                    if (interestmodel.get(i).is_selected())
                    {
                        list.add(interestmodel.get(i).getInterestText().toString());
                    }
                }

                if (Config.isConnectingToInternet(InterestActivity.this))
                {
                    loading.setVisibility(View.VISIBLE);
                    SetInterestAsyntask interest=new SetInterestAsyntask(list,InterestActivity.this,InterestActivity.this);
                    interest.execute();
                }
                else
                {
                    Config.alertDialog(InterestActivity.this,"Network Error","No Internet connection");
                }
            }
        });


/*
        ico_trecking= (TextView) findViewById(R.id.interest_ico_trekking);
        ico_dating= (TextView) findViewById(R.id.interest_ico_dating);
        ico_sports= (TextView) findViewById(R.id.interest_ico_sports);
        ico_music= (TextView) findViewById(R.id.interest_ico_music);
        ico_dancing= (TextView) findViewById(R.id.interest_ico_dancing);
        ico_bikers= (TextView) findViewById(R.id.interest_ico_bikers);
        ico_gossip= (TextView) findViewById(R.id.interest_ico_gossip);
        ico_socialwork= (TextView) findViewById(R.id.interest_ico_socialwork);
        ico_technical= (TextView) findViewById(R.id.interest_ico_technical);

        ico_trecking.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        ico_dancing.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_sports.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_dating.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_music.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_bikers.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_gossip.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_socialwork.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
        ico_technical.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME));
*/

    }

    @Override
    public void onTaskCompleted(String s) {
        if (s.equalsIgnoreCase("true"))
        {
            Toast.makeText(InterestActivity.this, "Successfully added interest", Toast.LENGTH_SHORT).show();
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(InterestActivity.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("Login", "true");
            edit.commit();

            Intent interest=new Intent(InterestActivity.this,DashboardActivity.class);
            startActivity(interest);
        }
        else
        {
            Toast.makeText(InterestActivity.this, "Error for adding interest", Toast.LENGTH_SHORT).show();
        }
        loading.setVisibility(View.GONE);
    }
}
