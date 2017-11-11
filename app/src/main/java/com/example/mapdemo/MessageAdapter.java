//package com.tajj.mapdemo;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
///**
// * Created by amade002 on 7/28/17.
// */
//
//public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
//
//    // list of comments
//    ArrayList<Message> messages;
//    // context
//    Context context;
//
//    // initialize with list
//    public MessageAdapter(ArrayList<Message> messages) {
//        this.messages = messages;
//    }
//
//    // creates and inflates a new view
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        // create inflater via getting context from parent
//        context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//        // create view object using inflater; return that wrapped w/ new ViewHolder
//        View messageView = inflater.inflate(R.layout.message2_item, parent, false);
//        return new ViewHolder(messageView);
//    }
//
//    // associates a created or inflated view with a new item at a specific position of a list
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        // extract element from specified position
//        Message current = messages.get(position);
//        // populate view with data from this movie
//        holder.tvMessageBody.setText(current.body);
//        holder.tvUserName.setText(current.userName);
//    }
//
//    // size of entire data set
//    @Override
//    public int getItemCount() {
//        // do not return zero!
//        return messages.size();
//    }
//
//    // create viewholder as a nested class -- cannot be static for parceling
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//
//        // instance variables -- track view objects
//        TextView tvMessageBody;
//        TextView tvUserName;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            // init instance variables via ID
//            tvMessageBody = (TextView) itemView.findViewById(R.id.tvMessageBody);
//            tvUserName = (TextView) itemView.findViewById(R.id.tvUsername);
//            // tvTimestamp = (TextView) itemView.findViewById(R.id.tvTimestamp);
//            // useless, need to implement b/c Java
//            itemView.setOnClickListener(this);
//        }
//
//        // does nothing (we don't have commentDetails or anything; has to be here)
//        @Override
//        public void onClick(View v) {
//            // Literally do nothing
//            int position = getAdapterPosition();
//        }
//    }
//}
