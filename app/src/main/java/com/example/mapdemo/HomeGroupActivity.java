package com.example.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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

                Intent i = new Intent (HomeGroupActivity.this, CreateNewGroup.class);
                startActivity(i);
            }


        });

    }

}

