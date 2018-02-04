package io.gresse.hugo.chatFirabase;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.text.ParseException;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    static final int TYPE_SENT = 1;
    static final int TYPE_RECEIVE = 0;

    private Listener mListener;
    private List<Message> mData;
    private User mUser;


    public MessageAdapter(Listener listener, List<Message> data, User user) {
        mListener = listener;
        mData = data;
        mUser = user;
    }

    public void setData(List<Message> data) {
        mData = data;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType==TYPE_SENT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sent, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_receive, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView mUserImageView;
        TextView  mUserTextView;
        TextView  mContentTextView;
        RelativeTimeTextView mTimerTextView;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mUserImageView = itemView.findViewById(R.id.userImageView);
            mUserTextView = itemView.findViewById(R.id.userTextView);
            mContentTextView = itemView.findViewById(R.id.contentTextView);
            mTimerTextView=itemView.findViewById(R.id.timerTextView);
        }

        void setData(Message message) {
            mUserTextView.setText(message.userName + ": ");
            mContentTextView.setText(message.content);
            mTimerTextView.setReferenceTime(message.timestamp);

            if (!TextUtils.isEmpty(message.userEmail)) {
                Glide
                        .with(mUserImageView.getContext())
                        .load(Constant.GRAVATAR_PREFIX + Utils.md5(message.userEmail))
                        .apply(RequestOptions.circleCropTransform())
                        .into(mUserImageView);
            } else {
                mUserImageView.setImageResource(R.color.colorAccent);
            }
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(getAdapterPosition(), mData.get(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View view) {
            mListener.onItemLongClick(getAdapterPosition(),mData.get(getAdapterPosition()));
            return true;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message=mData.get(position);
        if(message.userEmail.equals(mUser.email)){
            return TYPE_SENT;
        }else{
            return TYPE_RECEIVE;
        }
    }

    public interface Listener {
        void onItemClick(int position, Message message);

        void onItemLongClick(int adapterPosition, Message message);
    }
}
