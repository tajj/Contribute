package com.example.mapdemo;

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

public class CreateNewGroup extends AppCompatActivity {

    EditText edGroupName;
    Button btnCreateGroup;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        btnCreateGroup=(Button)findViewById(R.id.btnCreateGroup);
        edGroupName=(EditText)findViewById(R.id.edGroupName);
        i=new Intent(CreateNewGroup.this, SelectGroupMembers.class);

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                String grpname =String.valueOf(edGroupName.getText());

                //putting group name, group id and adding these values to the current app user
                i.putExtra("grpName", grpname);
                try{
                    ParseObject newGrp=new ParseObject("Groups");
                    newGrp.put("Name", grpname);
                    newGrp.save();
                    i.putExtra("grpId", newGrp.getObjectId());
                    i.putExtra("grpName", grpname);
                   // Log.v("Grp id", newGrp.getObjectId());
                    ParseObject addUsr=new ParseObject("UserConnections");
                    addUsr.put("userId",ParseUser.getCurrentUser().getEmail());
                    addUsr.put("userGroup", newGrp.getObjectId());
                    addUsr.put("groupName", grpname);
                    addUsr.save();
                    Toast.makeText(getApplicationContext(), "Group Created Successfully", Toast.LENGTH_LONG).show();

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });


        }


    }

