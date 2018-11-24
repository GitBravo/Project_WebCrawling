package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static kr.ac.kumoh.s20130053.okky.Board.isQNA;

public class RecyclerViewAdapterForBoard extends RecyclerView.Adapter<RecyclerViewAdapterForBoard.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mArray;
    private ArrayList<String> mComCount;
    private ArrayList<String> mDate;
    private ArrayList<String> mId;

    RecyclerViewAdapterForBoard(Context context,
                                ArrayList<String> title,
                                ArrayList<String> comCount,
                                ArrayList<String> date,
                                ArrayList<String> id) {
        this.mContext = context;
        this.mArray = title;
        this.mComCount = comCount;
        this.mDate = date;
        this.mId = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 새로운 뷰 생성
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item_for_board, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTitle.setText(mArray.get(position)); // 제목
        if (isQNA) {
            holder.mComment.setText(mComCount.get(2 * position + 1)); // 덧글수
            holder.mDate.setText(mContext.getString(R.string.TwoString, mDate.get(position), mComCount.get(2 * position))); // 게시날짜ㆍ추천수
        } else {
            holder.mComment.setText(mComCount.get(3 * position)); // 덧글수
            holder.mDate.setText(mContext.getString(R.string.ThreeString, mDate.get(position), mComCount.get(3 * position + 1), mComCount.get(3 * position + 2))); // 게시날짜ㆍ추천수ㆍ조회수
        }
        holder.mAccount.setText(mId.get(position)); // 아이디

        commentController(holder);
    }

    private void commentController(@NonNull ViewHolder holder){
        // 댓글 개수에 따라 색상을 동적으로 조정하는 메소드
        if (Integer.valueOf(holder.mComment.getText().toString()) == 0)
            holder.mComment.setVisibility(View.INVISIBLE); // 댓글 없을 시 표시안함
        else if (Integer.valueOf(holder.mComment.getText().toString()) < 10){
            holder.mComment.setVisibility(View.VISIBLE);
            holder.mComment.setBackgroundResource(R.drawable.commnetbackground_1);
        }else if(Integer.valueOf(holder.mComment.getText().toString()) < 50) {
            holder.mComment.setVisibility(View.VISIBLE);
            holder.mComment.setBackgroundResource(R.drawable.commnetbackground_2);
        }else {
            holder.mComment.setVisibility(View.VISIBLE);
            holder.mComment.setBackgroundResource(R.drawable.commnetbackground_3);
        }
    }

    @Override
    public int getItemCount() {
        return mArray.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle, mComment, mDate, mAccount;

        ViewHolder(View view) {
            super(view);
            mTitle = view.findViewById(R.id.TextView_titleItem);
            mComment = view.findViewById(R.id.TextView_commentItem);
            mDate = view.findViewById(R.id.TextView_dateItem);
            mAccount = view.findViewById(R.id.TextView_accountItem);
        }
    }
}
