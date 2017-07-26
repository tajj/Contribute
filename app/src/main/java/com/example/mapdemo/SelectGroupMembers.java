package com.example.mapdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SelectGroupMembers extends AppCompatActivity {
    String[] userids = {""}; // ids of all users in parse
    //ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_members);
        Intent i=getIntent();
        final String grpName=i.getExtras().getString("grpName");
        final String grpId=i.getExtras().getString("grpId");
        final ListView listview=(ListView)findViewById(R.id.usersList);

        //ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
       //query.whereNotEqualTo("email", ParseUser.getCurrentUser().getEmail());

        final ArrayList<String> list = new ArrayList<String>();

        //throwing in progress dialog...not necessary tho
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();

        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.group_row, list);
        listview.setAdapter(listAdapter);

        try{

            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> scoreList, com.parse.ParseException e) {
                    if (e == null) {
                        //Log.d("score", "Retrieved " + scoreList.size() + " scores");
                        userids = new String[scoreList.size()];
                        int count = 0;
                        //adding users to list
                        for (ParseUser groups : scoreList) {
                            list.add((String) groups.get("username"));

                            userids[count]=(String) groups.get("objectId");
                            count++;
                        }
                        //notify adapter

                        listview.setTextFilterEnabled(true);
                        listAdapter.notifyDataSetChanged();
                        pd.cancel();

                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }
//                    listAdapter.notifyDataSetChanged();
                    if (scoreList.size() == 0)
                        Log.d("score", "no friends ");
                        Toast.makeText(getApplicationContext(), "You do not have any friends to join you :/", Toast.LENGTH_LONG).show();
                }
            });



        }
        catch(Exception e){
            Log.e("exception:", e.toString());
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                try {
                    ParseObject addUsr=new ParseObject("UserConnections");
                    addUsr.put("groupName", grpName);
                    addUsr.put("userGroup", grpId);
                    addUsr.put("objectId", userids[position]);
                    addUsr.save();
                    Toast.makeText(getApplicationContext(), "Member added to Group", Toast.LENGTH_LONG).show();
                } catch (ParseException e) {
                    Log.e("error:", e.getMessage());
                }
            }
        });



    }

    //goes back to the home group activity when u click the back button
    @Override
    public void onBackPressed(){
        Intent i =new Intent(this, HomeGroupActivity.class);
        startActivity(i);
    }

}
