package com.example.mapdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@ParseClassName("MarkerDetailsActivity")
@RuntimePermissions
public class MarkerDetailsActivity extends AppCompatActivity {

    public final String APP_TAG = "SeenAds";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int PICK_PHOTO_CODE = 1046;
    public final static int COMMENT_CODE = 1058;

    public boolean parseFlag = false;

    // loading the right images
    public String groupID;

    // For local storage
    public final String ABSOLUTE_FILE_PATH = "/storage/emulated/0/Android/data/com.example.mapdemo/files/Pictures/SeenAds/";
    public String photoFileName = "photo.jpg";
    // private String finalFileName = "";

    // For parse & compression
    private ByteArrayOutputStream stream;
    private String snip;
    private String location;

    TextView tvTitle;
    TextView tvSnippet;
    ImageButton ibUploadPic;
    ImageButton ibGalleryPic;
    ImageButton ibComment;
    ImageButton ibPost; // TODO right now add to appropriate .xml
    ImageView ivMarkerPhoto;

    // Share
    private ShareButton shareButton;
    private Bitmap image;
    private int counter = 0;

    // Hmm...
    String markerID;
    String fullName;

    // Later if we get to it
    RecyclerView rvComments;
    CommentAdapter commentAdapter;
    ArrayList<Comment> comments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.markerdetails_activity);
        // initialize FB SDK for share
        FacebookSdk.sdkInitialize(getApplicationContext());
        // retrieve intent & setup
        String ID = getIntent().getStringExtra("title");
        String snippet = getIntent().getStringExtra("snippet");
        snip = snippet;
        location = getIntent().getStringExtra("location");
        fullName = getIntent().getStringExtra("fullName");
        photoFileName = photoFileName + ID;
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvSnippet = (TextView) findViewById(R.id.tvSnippet);
        ivMarkerPhoto = (ImageView) findViewById(R.id.ivMarkerPhoto);
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(comments);
        rvComments = (RecyclerView) findViewById(R.id.rvComments);
        // setup RV -- layout manager & setup w adapter
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
        groupID = getIntent().getStringExtra("groupID");


        ParseQuery<ParseObject> query  = ParseQuery.getQuery("ParseImageArrays");
        query.whereEqualTo("Location", location);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e==null){
                    int size = parseObjects.size();
                    parseFlag = true;
                    // TODO figure out a way to handle duplicate images LATER
                    // if there's something at this location already, load the one that matches the current group
                    if (size > 0) {
                        // pretty much the safest way to avoid collisions ever
                        for (int i = 0; i < size; i++) {
                            ParseObject match = parseObjects.get(i);
                            String checkGroupID = String.valueOf(match.get("groupID"));
                            if (checkGroupID.equals(groupID)) {
                                ParseFile imgFile = match.getParseFile("MarkerImage");
                                // get the URL
                                String imgFileUrl = imgFile.getUrl();
                                // load using Glide
                                Glide.with(getApplicationContext())
                                        .load(imgFileUrl)
                                        .bitmapTransform(new RoundedCornersTransformation(MarkerDetailsActivity.this, 10, 5))
                                        .into(ivMarkerPhoto);
                                // load it into the image view
                                String itemConfirmID = parseObjects.get(i).getObjectId();
                                Toast.makeText(MarkerDetailsActivity.this, "Loading from PARSE: object " + itemConfirmID, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    // else don't load any image & wait for the user to upload one
                } else {
                    Log.e("ERROR:", "" + e.getMessage());
                }
            }
        });

        // if there's already a path to the corresponding picture for this marker, load it instead of the placeholder image
        if (!parseFlag) { // TODO figure out how to fix double loading -- local & parse loading happen asynchronously so flag isn't useful
            // possible solution ^: multiple threads?
            File imgFile = new  File(ABSOLUTE_FILE_PATH + photoFileName);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivMarkerPhoto.setImageBitmap(myBitmap);
            }
        }

        // Camera taking picture
        ibUploadPic = (ImageButton) findViewById(R.id.ibUploadPic);
        ibUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerDetailsActivityPermissionsDispatcher.onLaunchCameraWithCheck(MarkerDetailsActivity.this, v);
            }
        });

        // Upload picture from gallery (onClickListener is set in .xml b/c permissions not required)
        ibGalleryPic = (ImageButton) findViewById(R.id.ibGalleryPic);

        // Post a comment
        ibComment = (ImageButton) findViewById(R.id.ibComment);
        ibComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO pass in username from groupID, etc. with the intent --> actually may not need to do that
                Intent postCommentIntent = new Intent(getApplicationContext(), PostCommentActivity.class);
                startActivityForResult(postCommentIntent, COMMENT_CODE);
            }
        });

        markerID = ID + snippet;
        // loading COMMENTS from Parse (can double-query for safety later)
        ParseQuery<ParseObject> query2  = ParseQuery.getQuery("Comment");
        query2.whereEqualTo("markerID", markerID);
        query2.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {
                    int size = parseObjects.size();
                    for (int i = 0; i < size; i++) {
                        ParseObject current = parseObjects.get(i);
                        // double query
                        String checkGroupID = String.valueOf(current.get("groupID"));
                        if (checkGroupID.equals(groupID)) {
                            // String username = current.getString("userID");
                            String body = current.getString("body");
                            String timestamp = current.getString("timestamp");
                            Comment curr = new Comment(body, fullName.toUpperCase() + " AT " + timestamp, timestamp);
                            comments.add(curr);
                            commentAdapter.notifyItemInserted(comments.size() - 1);
                        }
                    }
                }
            }
        });
        // safety/sanity
        commentAdapter.notifyDataSetChanged();
        // POST AFTER SCREENSHOT
        ibPost = (ImageButton) findViewById(R.id.ibPost);
        ibPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rootView = findViewById(android.R.id.content).getRootView();
                rootView.setDrawingCacheEnabled(true);
                // creates immutable clone of image
                image = Bitmap.createBitmap(rootView.getDrawingCache());
                // destroy
                rootView.destroyDrawingCache();
                SharePhoto photo = new SharePhoto.Builder().setBitmap(image).build();
                SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
                shareButton.setShareContent(content);
                counter = 0;
                shareButton.performClick();

            }
        });


        // set information
        tvTitle.setText(ID);
        tvSnippet.setText(snippet);

        shareButton = (ShareButton) findViewById(R.id.share_btn);
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                postPicture();
            }
        });

        ImageButton btn = (ImageButton)findViewById(R.id.btnChat);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MarkerDetailsActivity.this, ChatActivity.class));
            }
        });
    }

    public void postPicture() {
        //check counter
        if(counter == 0) {
            //save the screenshot
            View rootView = findViewById(android.R.id.content).getRootView();
            rootView.setDrawingCacheEnabled(true);
            // creates immutable clone of image
            image = Bitmap.createBitmap(rootView.getDrawingCache());
            // destroy
            rootView.destroyDrawingCache();
        }
        else {
            counter = 0;
            shareButton.setShareContent(null);
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the image file name
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MarkerDetailsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == COMMENT_CODE) {
            if (data != null) { // if the user did not hit the cancel button
                String body = data.getStringExtra("commentBody");
                String tempFullName = fullName;
                String timeStamp = new SimpleDateFormat("HH:mm MM/dd/yyyy").format(new Date());
                Comment comment = new Comment(body, tempFullName.toUpperCase() + " AT " + timeStamp, timeStamp);
                comments.add(comment);
                commentAdapter.notifyDataSetChanged();
                rvComments.scrollToPosition(0);
                // Put the comment into Parse under the Comment class
                ParseObject testObject = new ParseObject("Comment");
                testObject.put("body", body);
                testObject.put("timestamp", timeStamp);
                testObject.put("markerID", markerID);
                // safety
                testObject.put("groupID", groupID);
                // testObject.put("userID", userID);
                testObject.saveInBackground();
            }
        }
        else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(ABSOLUTE_FILE_PATH + photoFileName);
                Bitmap resizedImage = BitmapScaler.scaleToFitWidth(takenImage, 400);
                // Configure byte output stream
                stream = new ByteArrayOutputStream();
                // Compress the image further
                resizedImage.compress(Bitmap.CompressFormat.JPEG, 78, stream);

                // Save image to Parse
                byte[] image = stream.toByteArray();
                final ParseFile parseImage = new ParseFile(image);
                try {
                    parseImage.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                ParseObject testObject = new ParseObject("ParseImageArrays");
                // Put the image file into parse DB under ParseImageArrays class
                testObject.put("MarkerImage", parseImage);
                // For retrieval of images; check if the Parse location matches the current marker's location for loading
                testObject.put("Snippet", snip);
                testObject.put("Location", location);
                testObject.put("groupID", groupID);
                testObject.saveInBackground();

                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                Uri resizedUri = getPhotoFileUri(photoFileName + "_resized");
                File resizedFile = new File(resizedUri.getPath());
                FileOutputStream fos = null;
                try {
                    resizedFile.createNewFile();
                    fos = new FileOutputStream(resizedFile);
                    fos.write(stream.toByteArray());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Load the resized image into a preview
                ivMarkerPhoto.setImageBitmap(resizedImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }

        else if (data != null) {
            Uri photoUri = data.getData();
            // Do something with the photo based on Uri
            Bitmap selectedImage = null;
            try {
                if (photoUri != null) {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // if user cancels share to FB, get out of method
            if (selectedImage == null) {
                return;
            }
        //        if (requestCode == PICK_PHOTO_CODE && data.getClipData() != null) {
//            ClipData mClipData = data.getClipData();
//
//            ArrayList<Uri> mArrayUri = new ArrayList<>();
//            ArrayList<Bitmap> mBitmapsSelected = new ArrayList<>();
//
//
//            for (int i = 0; i < mClipData.getItemCount(); i++) {
//                ClipData.Item item = mClipData.getItemAt(i);
//                Uri uri = item.getUri();
//                mArrayUri.add(uri);
//                // !! You may need to resize the image if it's too large
//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//                    mBitmapsSelected.add(bitmap);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            Bitmap resizedImage = BitmapScaler.scaleToFitWidth(selectedImage, 430);
            // Configure byte output stream
            stream = new ByteArrayOutputStream();
            // Compress the image further
            resizedImage.compress(Bitmap.CompressFormat.JPEG, 88, stream);

            // Save image to Parse
            byte[] image = stream.toByteArray();
            final ParseFile parseImage = new ParseFile(image);
            try {
                parseImage.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            ParseObject testObject = new ParseObject("ParseImageArrays");
            // Put the image file into parse DB under ParseImageArrays class
            testObject.put("MarkerImage", parseImage);
            // For retrieval of images; check if the Parse location matches the current marker's location for loading
            testObject.put("Snippet", snip);
            testObject.put("Location", location);
            testObject.put("groupID", groupID);
            testObject.saveInBackground();

            // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
            Uri resizedUri = getPhotoFileUri(photoFileName + "_resized");
            File resizedFile = new File(resizedUri.getPath());
            FileOutputStream fos = null;
            try {
                resizedFile.createNewFile();
                fos = new FileOutputStream(resizedFile);
                fos.write(stream.toByteArray());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Load the resized image into a preview
            ivMarkerPhoto.setImageBitmap(selectedImage);
        } else { // Result was a failure OR you loaded from camera directly instead
            // Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
        }
    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }
            // Return the file target for the photo based on filename
            File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
            // wrap File object into a content provider
            return FileProvider.getUriForFile(MarkerDetailsActivity.this, "com.example.mapdemo.fileprovider", file);
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }
}
