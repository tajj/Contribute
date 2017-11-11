package com.tajj.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by amade002 on 7/28/17.
 */

public class PostMessageActivity extends AppCompatActivity {

    TextView tvMessagePrompt;
    EditText etPostMessage;
    ImageButton ibPostMessage;
    ImageButton ibCancel;
    // Comment comment;
    public final static int CHAT_CODE = 1058;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postmessage);

        // setup
        tvMessagePrompt = (TextView) findViewById(R.id.tvMessagePrompt);
        etPostMessage = (EditText) findViewById(R.id.etPostMessage);
        ibPostMessage = (ImageButton) findViewById(R.id.ibPostMessage);
        ibCancel = (ImageButton) findViewById(R.id.ibCancel);

        ibPostMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageBody = String.valueOf(etPostMessage.getText());
                // pass back to MarkerDetailsActivity
                Intent passBack = new Intent();
                // Pass data back
                passBack.putExtra("action", "post");
                passBack.putExtra("messageBody", messageBody);
                // passBack.putExtra("body", commentBody);
                setResult(CHAT_CODE, passBack);
                finish();
            }
        });

        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // idiot proof
    @Override
    public void onBackPressed() {
        Intent goBackIntent = new Intent();
        goBackIntent.putExtra("action", "back");
        setResult(CHAT_CODE, goBackIntent);
        finish();
    }
}
