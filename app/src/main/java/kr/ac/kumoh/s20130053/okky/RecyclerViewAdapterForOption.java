package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapterForOption extends RecyclerView.Adapter<RecyclerViewAdapterForOption.ViewHolder> {
    private final Context mContext;
    private final ArrayList<String> mTitle;
    private final ArrayList<String> mContent;

    RecyclerViewAdapterForOption(Context context, ArrayList<String> title, ArrayList<String> content) {
        this.mContext = context;
        this.mTitle = title;
        this.mContent = content;
    }

    @NonNull
    @Override
    public RecyclerViewAdapterForOption.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 새로운 뷰 생성
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item_for_optionmenu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterForOption.ViewHolder holder, int position) {
        holder.tvTitle.setText(mTitle.get(position)); // 옵션 제목
        holder.tvContent.setText(mContent.get(position)); // 옵션 내용
    }

    @Override
    public int getItemCount() {
        return mTitle.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.OptionMenu_recyclerView_title);
            tvContent = view.findViewById(R.id.OptionMenu_recyclerView_content);
        }
    }
}
