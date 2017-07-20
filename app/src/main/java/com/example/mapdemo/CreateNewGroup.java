package com.example.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class CreateNewGroup extends AppCompatActivity {

    EditText edGroupName; //TODO add these names in the xml
    Button btnCreateGroup;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        btnCreateGroup=(Button)findViewById(R.id.creategroupbutton);
        edGroupName=(EditText)findViewById(R.id.groupname);
        i=new Intent(CreateNewGroup.this, SelectGroupMembers.class);

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {

            //FIX STARTING HERE
            @Override
            public void onClick(View v) {
                String grpname =String.valueOf(ed.getText());

                i.putExtra("grpName", grpname);
                try{
                    ParseObject newGrp=new ParseObject("Groups");
                    newGrp.put("Name", grpname);
                    newGrp.save();
                    i.putExtra("grpId", newGrp.getObjectId());
                    i.putExtra("grpName", grpname);
                    Log.v("Grp id", newGrp.getObjectId());
                    ParseObject addUsr=new ParseObject("UserConnections");
                    addUsr.put("userId",ParseUser.getCurrentUser().getEmail());
                    addUsr.put("userGroup", newGrp.getObjectId());
                    addUsr.put("groupName", grpname);
                    addUsr.save();
                    Log.v("Credentials: ", ParseUser.getCurrentUser().getEmail()+" "+newGrp.getObjectId()+" "+grpname);
                    Toast.makeText(getApplicationContext(),
                            "Group Created Successfully",
                            Toast.LENGTH_LONG).show();
                }catch (ParseException e){
                    Log.e("Error Creating Group: ", e.getMessage());
                }
                startActivity(i);
            }
        });


        }


    }
}
