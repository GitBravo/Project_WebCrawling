package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
    private ArrayList<String> mNicknameHref;

    RecyclerViewAdapterForDetail(Context context,
                                 ArrayList<String> nickname,
                                 ArrayList<String> date,
                                 ArrayList<String> comment,
                                 ArrayList<String> href) {
        this.mContext = context;
        this.mNickname = nickname;
        this.mDate = date;
        this.mComment = comment;
        this.mNicknameHref = href;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 새로운 뷰 생성
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item_for_detail, parent, false);
        return new RecyclerViewAdapterForDetail.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterForDetail.ViewHolder holder, final int position) {
        holder.tvNickname.setText(mNickname.get(position)); // 덧글 아이디
        holder.tvDate.setText(mDate.get(position)); // 덧글 게시날짜
        holder.tvComment.setHtmlText(mComment.get(position)); // 덧글 내용
        holder.tvNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 댓글 각각의 아이디를 누르면 유저정보로 이동하는 부분 구현
                switch (v.getId()) {
                    case R.id.detail_nickname:
                        if (!mNicknameHref.get(position).equals("")) {
                            Intent intent = new Intent(mContext, UserInfo.class);
                            intent.putExtra("nickname", mNickname.get(position));
                            intent.putExtra("url", mNicknameHref.get(position));
                            mContext.startActivity(intent);
                        }
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        HTMLTextView tvComment;
        TextView tvDate, tvNickname;

        ViewHolder(View view) {
            super(view);
            tvNickname = view.findViewById(R.id.detail_nickname);
            tvDate = view.findViewById(R.id.detail_date);
            tvComment = view.findViewById(R.id.detail_comment);
        }
    }
}
