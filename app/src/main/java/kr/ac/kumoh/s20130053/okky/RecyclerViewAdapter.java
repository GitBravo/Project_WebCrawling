package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mArray;
    private ArrayList<String> mComCount;
    private ArrayList<String> mDate;
    private ArrayList<String> mId;

    RecyclerViewAdapter(Context context, ArrayList<String> title, ArrayList<String> comCount, ArrayList<String> date, ArrayList<String> id) {
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_itme, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTitle.setText(mArray.get(position)); // 제목
        holder.mComment.setText(mComCount.get(3*position)); // 덧글수
        holder.mDate.setText(mDate.get(position) + "ㆍ" + mComCount.get(3*position+1) + "ㆍ" + mComCount.get(3*position+2)); // 게시날짜ㆍ추천수ㆍ조회수
        holder.mAccount.setText(mId.get(position)); // 아이디
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
