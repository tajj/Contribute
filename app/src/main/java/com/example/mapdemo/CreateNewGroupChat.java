package com.tajj.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNewGroupChat extends AppCompatActivity {


    Intent i;

    @BindView(R.id.btnCreateGroupChat) Button btnCreateGroupChat;
    @BindView(R.id.edGroupChatName) EditText edGroupChatName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group_chat);

        ButterKnife.bind(this);
        i=new Intent(CreateNewGroupChat.this, SelectGroupChatMembers.class);

        //button for creating group
        btnCreateGroupChat.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                String grpname =String.valueOf(edGroupChatName.getText());

                //putting group name, group id and adding these values to the current app user
                i.putExtra("grpName", grpname);
                try{
                    //registers group in parse
                    ParseObject newGrp=new ParseObject("GroupChats");
                    newGrp.put("Name", grpname);
                    newGrp.save();
                    //passing it through intent to next acitivity
                    i.putExtra("grpId", newGrp.getObjectId());
                    i.putExtra("grpName", grpname);
                    ParseObject addUsr=new ParseObject("UserConnectionsChat");
                    addUsr.put("username",ParseUser.getCurrentUser().getUsername());
                    addUsr.put("userGroup", newGrp.getObjectId());
                    addUsr.put("groupName", grpname);
                    addUsr.save();
                    Toast.makeText(getApplicationContext(), "GroupChat Created Successfully", Toast.LENGTH_LONG).show();

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });


    }


}

