package com.example.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class HomeGroupActivity extends AppCompatActivity {
    Intent i;
    Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homegroup_activity);

        //ParseUser currentUser = ParseUser.getCurrentUser();
        btnAdd = (Button) findViewById(R.id.btnAdd);
        i=new Intent(HomeGroupActivity.this, SelectGroupMembers.class);


        btnAdd.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                try{
                    ParseObject newGrp=new ParseObject("Groups");
                    newGrp.save();


                    i.putExtra("grpId", newGrp.getObjectId());

                    //Log.d("Grp id", newGrp.getObjectId());
                    ParseObject addUsr=new ParseObject("UserConnections");
                    addUsr.put("userId", ParseUser.getCurrentUser().getEmail());
                    addUsr.put("userGroup", newGrp.getObjectId());
                    addUsr.save();
                   // Log.d("Credentials: ", ParseUser.getCurrentUser().getEmail()+" "+newGrp.getObjectId());
                    Toast.makeText(getApplicationContext(), "Success! Group Created.", Toast.LENGTH_LONG).show();
                }catch (ParseException e){
                    Log.e("Error Creating Group: ", e.getMessage());
                }




                startActivity(i);
                finish();

            }


        });

    }

}

