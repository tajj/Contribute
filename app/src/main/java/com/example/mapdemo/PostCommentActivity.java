package com.example.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class PostCommentActivity extends AppCompatActivity {

    TextView tvCommentPrompt;
    EditText etPostComment;
    ImageButton ibPostComment;
    ImageButton ibCancel;
    // Comment comment;
    public final static int COMMENT_CODE = 1058;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postcomment);
        // setup
        tvCommentPrompt = (TextView) findViewById(R.id.tvCommentPrompt);
        etPostComment = (EditText) findViewById(R.id.etPostComment);
        ibPostComment = (ImageButton) findViewById(R.id.ibPostComment);
        ibCancel = (ImageButton) findViewById(R.id.ibCancel);
        ibPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentBody = String.valueOf(etPostComment.getText());
                // pass back to MarkerDetailsActivity
                Intent passBack = new Intent();
                // Pass data back
                passBack.putExtra("commentBody", commentBody);
                // passBack.putExtra("body", commentBody);
                setResult(COMMENT_CODE, passBack);
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
}
