//package com.tajj.mapdemo;
//
///**
// * Created by amade002 on 7/28/17.
// */
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ImageButton;
//
//import com.facebook.FacebookSdk;
//import com.parse.FindCallback;
//import com.parse.ParseClassName;
//import com.parse.ParseObject;
//import com.parse.ParseQuery;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//
//@ParseClassName("MessageActivity")
//public class MessageActivity extends AppCompatActivity {
//
//    public final static int CHAT_CODE = 1058;
//
//    public boolean parseFlag = false;
//
//    // loading the right messages
//    public String groupID;
//    String markerID;
//    String fullName;
//
//    //If we decide to do get to it
//    RecyclerView rvMessages;
//    MessageAdapter messageAdapter;
//    ArrayList<Message> messages;
//
//    EditText etPostMessage;
//    ImageButton ibPostMessage;
//
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.message_activity);
//
//        // initialize FB SDK for share
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        // retrieve intent & setup
//        String ID = getIntent().getStringExtra("title");
//        String snippet = getIntent().getStringExtra("snippet");
//        fullName = getIntent().getStringExtra("fullName");
//
//
//        //Chat Implementation
//        messages = new ArrayList<>();
//        messageAdapter = new MessageAdapter(messages);
//        rvMessages = (RecyclerView) findViewById(R.id.rvMessages);
//        // setup RV -- layout manager & setup w adapter
//        rvMessages.setLayoutManager(new LinearLayoutManager(this));
//        rvMessages.setAdapter(messageAdapter);
//
//        groupID = getIntent().getStringExtra("groupID");
//
//        // if there's already a path to the corresponding message for this rv
//        if (!parseFlag) { // TODO figure out how to fix double loading -- local & parse loading happen asynchronously so flag isn't useful
//
////            // Post a message
////            ibMessage = (ImageButton) findViewById(R.id.ibMessage);
////            ibMessage.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    Intent postMessageIntent = new Intent(getApplicationContext(), PostMessageActivity.class);
////                    startActivityForResult(postMessageIntent, CHAT_CODE);}
////            });
//
////            Intent postMessageIntent = new Intent(getApplicationContext(), MessageActivity.class);
////            startActivityForResult(postMessageIntent, CHAT_CODE);
//
//            ibPostMessage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String messageBody = String.valueOf(etPostMessage.getText());
//                    // pass back to MarkerDetailsActivity
//                    Intent passBack = new Intent();
//                    // Pass data back
//                    passBack.putExtra("action", "post");
//                    passBack.putExtra("messageBody", messageBody);
//                    // passBack.putExtra("body", commentBody);
//                    setResult(CHAT_CODE, passBack);
//                    finish();
//                }
//            });
//
//            markerID = ID + snippet;
//
//            ParseQuery<ParseObject> query3 = ParseQuery.getQuery("Message");
//            query3.whereEqualTo("markerID", markerID);
//            query3.findInBackground(new FindCallback<ParseObject>() {
//                @Override
//                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
//                    if (e == null) {
//                        int size = parseObjects.size();
//                        for (int i = 0; i < size; i++) {
//                            ParseObject current = parseObjects.get(i);
//                            // double query
//                            String checkGroupID = String.valueOf(current.get("groupID"));
//                            if (checkGroupID.equals(groupID)) {
//                                // String username = current.getString("userID");
//                                String body = current.getString("body");
//                                String timestamp = current.getString("timestamp");
//                                Message curr = new Message(body, fullName.toUpperCase() + " AT " + timestamp, timestamp);
//                                messages.add(curr);
//                                messageAdapter.notifyItemInserted(messages.size() - 1);
//                            }
//                        }
//                    }
//                }
//            });
//            // Check for error
//            messageAdapter.notifyDataSetChanged();
//        }
//
//    }
//
//        @Override
//        public void onActivityResult ( int requestCode, int resultCode, Intent data){
//            if (requestCode == CHAT_CODE) {
//                if (data != null) { // if the user did not hit the cancel button
//                    String body = data.getStringExtra("messageBody");
//                    String tempFullName = fullName;
//                    String timeStamp = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(new Date());
//                    Message message = new Message(body, tempFullName.toUpperCase() + " AT " + timeStamp, timeStamp);
//                    messages.add(message);
//                    messageAdapter.notifyDataSetChanged();
//                    rvMessages.scrollToPosition(0);
//                    // Put the message into Parse under the Chat class
//                    ParseObject testObject = new ParseObject("Message");
//                    testObject.put("body", body);
//                    testObject.put("timestamp", timeStamp);
//                    testObject.put("markerID", markerID);
//                    // safety
//                    testObject.put("groupID", groupID);
//                    // testObject.put("userID", userID);
//                    testObject.saveInBackground();
//                }
//            }
//        }
//    @Override
//    public void onBackPressed() {
//        Intent goBackIntent = new Intent();
//        goBackIntent.putExtra("action", "back");
//        setResult(CHAT_CODE, goBackIntent);
//        finish();
//    }
//
//}
