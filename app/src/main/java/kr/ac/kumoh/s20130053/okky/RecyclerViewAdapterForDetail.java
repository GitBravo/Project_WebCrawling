package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapterForDetail extends RecyclerView.Adapter<RecyclerViewAdapterForDetail.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mNickname;
    private ArrayList<String> mComment;
    private ArrayList<String> mDate;

    RecyclerViewAdapterForDetail(Context context, ArrayList<String> nickname, ArrayList<String> date, ArrayList<String> comment) {
        this.mContext = context;
        this.mNickname = nickname;
        this.mDate = date;
        this.mComment = comment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 새로운 뷰 생성
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item_for_detail, parent, false);
        return new RecyclerViewAdapterForDetail.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterForDetail.ViewHolder holder, int position) {
        holder.tvNickname.setText(mNickname.get(position)); // 덧글 아이디
        holder.tvDate.setText(mDate.get(position)); // 덧글 게시날짜
        holder.tvComment.setText(mComment.get(position)); // 덧글 내용

        holder.tvComment.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Log.d("[ Height ] ", String.valueOf(holder.tvComment.getMeasuredHeight()
                + holder.tvDate.getMeasuredHeight()
                + holder.tvNickname.getMeasuredHeight()));
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvComment, tvDate, tvNickname;

        ViewHolder(View view) {
            super(view);
            tvNickname = view.findViewById(R.id.detail_nickname);
            tvDate = view.findViewById(R.id.detail_date);
            tvComment = view.findViewById(R.id.detail_comment);
        }
    }
}
