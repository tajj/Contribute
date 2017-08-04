package com.example.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Profile;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SubscriptionHandling;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {
    static final String TAG = ChatActivity.class.getSimpleName();
    static final String USER_ID_KEY = "userId";
    static final String BODY_KEY = "body";
    static final String userName_KEY = "username";
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    // Create a handler which can run code periodically
    static final int POLL_INTERVAL = 1000; // milliseconds
    Handler myHandler = new Handler();  // android.os.Handler
    Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            myHandler.postDelayed(this, POLL_INTERVAL);
        }
    };

    Profile mFbProfile;


    EditText etMessage;
    Button btSend;

    RecyclerView rvChat;
    ArrayList<Message> mMessages;
//    ArrayList<UserConnectionsChat> mUsernames;
    ChatAdapter mAdapter;
    // Keep track of initial load to scroll to the bottom of the ListView
    boolean mFirstLoad;
    Bundle extras;
    public String groupID;
//
//    @BindView(R.id.ivProfileOther) ImageView mProfileImage1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        groupID = getIntent().getStringExtra("GroupPointer");

        // User login
        if (ParseUser.getCurrentUser() != null) { // start with existing user
            startWithCurrentUser();
        } else { // If not logged in, login as a new anonymous user
            login();
        }
        extras = getIntent().getExtras();
        refreshMessages();

        final long period = 5000;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                refreshMessages();
                // do your task here
            }
        }, 0, period);

        // myHandler.postDelayed(mRefreshMessagesRunnable, POLL_INTERVAL);

        // Make sure the Parse server is setup to configured for live queries
        // URL for server is determined by Parse.initialize() call.
        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();

        ParseQuery<Message> parseQuery = ParseQuery.getQuery(Message.class);
        // This query can even be more granular (i.e. only refresh if the entry was added by some other user)
        // parseQuery.whereNotEqualTo(USER_ID_KEY, ParseUser.getCurrentUser().getObjectId());

        // Connect to Parse server
        SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

        // Listen for CREATE events
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new
                SubscriptionHandling.HandleEventCallback<Message>() {
                    @Override
                    public void onEvent(ParseQuery<Message> query, Message object) {
                        mMessages.add(0, object);

                        // RecyclerView updates need to be run on the UI thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                rvChat.scrollToPosition(0);
                            }
                        });
                    }
                });
//
//            // Suggested by https://disqus.com/by/dominiquecanlas/
//            Bundle parameters = new Bundle();
//            parameters.putString("fields", "email,name,picture");
//
//
//            new GraphRequest(
//                    AccessToken.getCurrentAccessToken(),
//                    "/me",
//                    parameters,
//                    HttpMethod.GET,
//                    new GraphRequest.Callback() {
//                        public void onCompleted(GraphResponse response) {
//            /* handle the result */
//                            try {
//
//                                Log.d("Response", response.getRawResponse());
//
//                                JSONObject picture = response.getJSONObject().getJSONObject("picture");
//                                JSONObject data = picture.getJSONObject("data");
//
//                                //  Returns a 50x50 profile picture
//                                String pictureUrl = data.getString("url");
//
//                                Log.d("Profile pic", "url: " + pictureUrl);
//
//                                new ProfilePhotoAsync(pictureUrl).execute();
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//            ).executeAsync();




    }

    // Get the userId from the cached currentUser object
    void startWithCurrentUser() {
        setupMessagePosting();
    }

    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Anonymous login failed: ", e);
                } else {
                    startWithCurrentUser();
                }
            }
        });
    }


    // Setup message field and posting
    void setupMessagePosting() {
        // Find the text field and button
        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (Button) findViewById(R.id.btSend);
        rvChat = (RecyclerView) findViewById(R.id.rvChat);
        mMessages = new ArrayList<>();
        mFirstLoad = true;
        final String userId = ParseUser.getCurrentUser().getObjectId();
        mAdapter = new ChatAdapter(ChatActivity.this, userId, mMessages);
        rvChat.setAdapter(mAdapter);

        // associate the LayoutManager with the RecylcerView
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(linearLayoutManager);

        // When send button is clicked, create message object on Parse
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Message message = new Message();

                String data = etMessage.getText().toString();
                //ParseObject message = ParseObject.create("Message");
                //message.put(Message.USER_ID_KEY, userId);
                //message.put(Message.BODY_KEY, data);
                // Using new `Message` Parse-backed model now

//                final Message message = new Message();
                message.setBody(data);
                message.setUserId(ParseUser.getCurrentUser().getObjectId());
                message.setUserName(ParseUser.getCurrentUser().getUsername());
                message.setGroupPointer(extras.getString("grpPointer"));


////        Saving profile photo as a ParseFile
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    Bitmap bitmap = ((BitmapDrawable) mProfileImage1.getDrawable()).getBitmap();
//                    if (bitmap != null) {
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
//                        byte[] data1 = stream.toByteArray();
//                        String thumbName = message.getUserName().replaceAll("\\s+", "");
//                        final ParseFile parseFile = new ParseFile(thumbName + "_thumb.jpg", data1);
//
//                        message.saveInBackground(new SaveCallback() {
//                            @Override
//                            public void done(ParseException e) {
//                                message.put("profileThumb", parseFile);
//                                refreshMessages();
//
//                            }
//                        });
//                    }

                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e){
                        Toast.makeText(ChatActivity.this, "Sent!",
                                Toast.LENGTH_SHORT).show();
                        refreshMessages();
                    }
                });
                etMessage.setText(null);
            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.menu_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    class ProfilePhotoAsync extends AsyncTask<String, String, String> {
//        public Bitmap bitmap;
//        String url;
//
//        public ProfilePhotoAsync(String url) {
//            this.url = url;
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            // Fetching data from URI and storing in bitmap
//            bitmap = DownloadImageBitmap(url);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//
//
//            //mProfileImage1.setImageBitmap(bitmap);
//
//        }
//    }
//
//    public static Bitmap DownloadImageBitmap(String url) {
//        Bitmap bm = null;
//        try {
//            URL aURL = new URL(url);
//            URLConnection conn = aURL.openConnection();
//            conn.connect();
//            InputStream is = conn.getInputStream();
//            BufferedInputStream bis = new BufferedInputStream(is);
//            bm = BitmapFactory.decodeStream(bis);
//            bis.close();
//            is.close();
//        } catch (IOException e) {
//            Log.e("IMAGE", "Error getting bitmap", e);
//        }
//        return bm;
//
//
//    }

    // Query messages from Parse so we can load them into the chat adapter
    void refreshMessages() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        // get the latest 50 messages, order will show up newest to oldest of this group
        query.orderByDescending("createdAt");
        query.whereEqualTo("GRPOINTER",extras.getString("grpPointer"));
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) {

                if (messages.size() != 0 && mMessages.size() != 0 && messages.get(messages.size() -1).equals(mMessages.get(mMessages          .size() -1))) {
                    return;
                }
                if (e == null) {
                    mMessages.clear();
                    mMessages.addAll(messages);
                    mAdapter.notifyDataSetChanged(); // update adapter
                    // Scroll to the bottom of the list on initial load
                    if (mFirstLoad) {
                        rvChat.scrollToPosition(0);
                        mFirstLoad = false;
                    }
                } else {
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent homeGroupIntent = new Intent(ChatActivity.this, HomeChatActivity.class);
        startActivity(homeGroupIntent);
    }

}
