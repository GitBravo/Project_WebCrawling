package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapterForUserInfo extends RecyclerView.Adapter<RecyclerViewAdapterForUserInfo.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mActivity;
    private ArrayList<String> mTitle;

    RecyclerViewAdapterForUserInfo(Context context, ArrayList<String> activity, ArrayList<String> title) {
        this.mContext = context;
        this.mActivity = activity;
        this.mTitle = title;
    }

    @NonNull
    @Override
    public RecyclerViewAdapterForUserInfo.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 새로운 뷰 생성
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item_for_userinfo, parent, false);
        return new RecyclerViewAdapterForUserInfo.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterForUserInfo.ViewHolder holder, int position) {
        holder.tvActivity.setText(mActivity.get(position)); // 액티비티 내용
        holder.tvTitle.setText(mTitle.get(position)); // 글 제목
    }

    @Override
    public int getItemCount() {
        return mTitle.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvActivity, tvTitle;

        ViewHolder(View view) {
            super(view);
            tvActivity = view.findViewById(R.id.UserInfo_recyclerView_activity);
            tvTitle = view.findViewById(R.id.UserInfo_recyclerView_title);
        }
    }
}
