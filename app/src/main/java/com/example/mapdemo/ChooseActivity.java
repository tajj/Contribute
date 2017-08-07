package com.example.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by amade002 on 7/30/17.
 */

public class ChooseActivity extends AppCompatActivity {
    Button btnGroups;
    Button btnMessages;
    String fullName;

    public final static int CHAT_CODE = 1058;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_activity);

//button to move to group activity
        fullName = getIntent().getStringExtra("fullName");

        btnGroups = (Button) findViewById(R.id.btnGroups);
        btnGroups.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Intent i = new Intent(ChooseActivity.this, HomeGroupActivity.class);

                i.putExtra("fullName", fullName);
                startActivity(i);

                finish();
            }


        });

        //button to move to chat activity
        fullName = getIntent().getStringExtra("fullName");

        btnMessages = (Button) findViewById(R.id.btnMessages);
        btnMessages.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(ChooseActivity.this, HomeChatActivity.class);

                i.putExtra("fullName", fullName);
                startActivity(i);

                finish();

            }


        });

    }

    @Override
    public void onBackPressed() {
        Intent homeGroupChatIntent = new Intent(ChooseActivity.this, AboutActivity.class);
        startActivity(homeGroupChatIntent);
    }
}
