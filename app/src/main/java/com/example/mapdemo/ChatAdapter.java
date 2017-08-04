package com.example.mapdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

/**
 * Created by amade002 on 7/3/17.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<Message> mMessages;
    private Context mContext;
    private String mUserId;

    public ChatAdapter(Context context, String userId, List<Message> messages) {
        mMessages = messages;
        this.mUserId = userId;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_chat, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Message message = mMessages.get(position);

        final boolean isMe = message.getUserId() != null && message.getUserId().equals(mUserId);

        if (isMe) {
            holder.imageMe.setVisibility(View.VISIBLE);
            holder.imageOther.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            holder.userName.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            ParseUser parseUser = ParseUser.getCurrentUser();
            try {
                ParseFile parseFile = parseUser.getParseFile("profileThumb");
                byte[] data = parseFile.getData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                holder.imageMe.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.imageOther.setVisibility(View.VISIBLE);
            holder.imageMe.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            holder.userName.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            ParseQuery<ParseUser> query = ParseQuery.getQuery("_User");
            query.whereEqualTo("objectId",message.getUserId());

            query.findInBackground(new FindCallback<ParseUser>() {

                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        //List contain object with specific user id.
                        ParseUser parseUser = objects.get(0);
                        try {
                            ParseFile parseFile = parseUser.getParseFile("profileThumb");
                            byte[] data = parseFile.getData();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            holder.imageOther.setImageBitmap(bitmap);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    } else {
                        // error
                        Log.e("message", "Error Loading" + e);
                    }
                }
            });
//            ParseUser parseUser = ParseUser.getCurrentUser();
//            try {
//                ParseFile parseFile = parseUser.getParseFile("profileThumb");
//                byte[] data = parseFile.getData();
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                holder.imageMe.setImageBitmap(bitmap);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }

        //final ImageView profileView = holder.imageOther;
        //Glide.with(mContext).load(getProfileUrl(message.getUserId())).into(profileView);
        holder.body.setText(message.getBody());
        holder.userName.setText(message.getUserName());
    }

    // Create a gravatar image based on the hash value obtained from userId
    private static String getProfileUrl(final String userId) {
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "http://www.gravatar.com/avatar/" + hex + "?d=identicon";
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageOther;
        ImageView imageMe;
        TextView body;
        TextView userName;

        public ViewHolder(View itemView) {
            super(itemView);
            imageOther = (ImageView)itemView.findViewById(R.id.ivProfileOther);
            imageMe = (ImageView)itemView.findViewById(R.id.ivProfileMe);
            body = (TextView)itemView.findViewById(R.id.tvBody);
            userName = (TextView)itemView.findViewById(R.id.tvUsername);

        }
    }
}
