package com.tajj.mapdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mayajey on 7/21/17.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    // list of comments
    ArrayList<Comment> comments;
    // context
    Context context;

    // initialize with list
    public CommentAdapter(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    // creates and inflates a new view
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create inflater via getting context from parent
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // create view object using inflater; return that wrapped w/ new ViewHolder
        View commentView = inflater.inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(commentView);
    }

    // associates a created or inflated view with a new item at a specific position of a list
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // extract element from specified position
        Comment current = comments.get(position);
        // populate view with data from this movie
        holder.tvCommentBody.setText(current.body);
        holder.tvUserName.setText(current.userName);
    }

    // size of entire data set
    @Override
    public int getItemCount() {
        // do not return zero!
        return comments.size();
    }

    // create viewholder as a nested class -- cannot be static for parceling
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // instance variables -- track view objects
        TextView tvCommentBody;
        TextView tvUserName;

        public ViewHolder(View itemView) {
            super(itemView);
            // init instance variables via ID
            tvCommentBody = (TextView) itemView.findViewById(R.id.tvCommentBody);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUsername);
            // tvTimestamp = (TextView) itemView.findViewById(R.id.tvTimestamp);
            // useless, need to implement b/c Java
            itemView.setOnClickListener(this);
        }

        // does nothing (we don't have commentDetails or anything; has to be here)
        @Override
        public void onClick(View v) {
            // Literally do nothing
            int position = getAdapterPosition();
        }
    }
}
