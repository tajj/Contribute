package com.example.mapdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeGroupActivity extends AppCompatActivity {


    //these are going to hold all the group ids, names and dates in one place: an array
    String[] groupID;
    String[] groupNAME;
    String fullName;
    Date[] grpDate;

    @BindView(R.id.btnAdd)ImageButton btnAdd;
    @BindView(R.id.tvSecretSeenAds) TextView btnSecretSeenAds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homegroup_activity);
        ButterKnife.bind(this);
        fullName = getIntent().getStringExtra("fullName");

        //ParseUser currentUser = ParseUser.getCurrentUser();
        //btnAdd = (Button) findViewById(R.id.btnAdd);
        //btnSecretSeenAds = (Button) findViewById(R.id.tvSecretSeenAds);
        //ii=new Intent(HomeGroupActivity.this, SelectGroupMembers.class); //
        Intent i=getIntent();
        final String username=i.getStringExtra("username");


        //N.B. this intent needs to be final, used in inner class later
        final Intent ii = new Intent(HomeGroupActivity.this, MapDemoActivity.class);

        final ListView listview =(ListView)findViewById(R.id.lvGroupsList);

        //creating query in parse for the users
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserConnections");
       // query.whereEqualTo("email", ParseUser.getCurrentUser().getEmail());
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

        final ArrayList<String> list = new ArrayList<String>();

        //a progress dialog for fun
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();

        //using arrays for navigating groups
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.group_row, list);
        listview.setAdapter(listAdapter);
        try{
            query.findInBackground(new FindCallback<ParseObject>() {
                //using scorelist object to get # of groups to create correct size array
                public void done(List<ParseObject> scoreList, com.parse.ParseException e) {
                    if (e == null) {
                        groupID=new String[scoreList.size()];
                        groupNAME=new String[scoreList.size()];
                        grpDate=new Date[scoreList.size()];

                        //using count to locate a specific group in the list,
                        int count=0;
                        for (ParseObject groups : scoreList) {
                            list.add((String) groups.get("groupName")+ "\nOther Members: "+username+"\nCreated: "+groups.getCreatedAt());
                            groupID[count]=(String) groups.get("userGroup");
                            groupNAME[count]=(String) groups.get("groupName");
                            grpDate[count]=groups.getCreatedAt();
                            count++;
                        }

                        listview.setTextFilterEnabled(true);
                        listAdapter.notifyDataSetChanged();
                        pd.cancel();
                    } else {
                        Log.d("score", "Error: " + e.getMessage());                    }
                    if (scoreList.size()==0) {
                        Toast.makeText(getApplicationContext(), "No Groups Found", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        catch(Exception e){
            Log.e("exception:", e.toString());
        }



//passes group id and name to the maps activity


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ii.putExtra("groupId", groupID[position]);
                ii.putExtra("groupName", groupNAME[position]);
                ii.putExtra("fullName", fullName);
                ii.putExtra("grpCreatedAt", grpDate[position]);
                startActivity(ii);
                finish();
                //Toast.makeText(getApplicationContext(), values[position], Toast.LENGTH_LONG).show();
            }
        });




//this is the button to add groups
        btnAdd.setOnClickListener(new View.OnClickListener() {

public void onClick(View v) {

        Intent i = new Intent (HomeGroupActivity.this, CreateNewGroup.class);
        startActivity(i);
        }


        });


        btnSecretSeenAds.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Intent i = new Intent (HomeGroupActivity.this, SecretSeenAds.class);
                startActivity(i);
            }


        });

        }
    @Override
    public void onBackPressed() {
        Intent homeGroupIntent = new Intent(HomeGroupActivity.this, ChooseActivity.class);
        startActivity(homeGroupIntent);
    }












}

