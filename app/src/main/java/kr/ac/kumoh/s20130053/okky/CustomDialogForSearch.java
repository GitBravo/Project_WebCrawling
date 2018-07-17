package kr.ac.kumoh.s20130053.okky;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CustomDialogForSearch extends Dialog implements View.OnClickListener {
    private static final int LAYOUT = R.layout.custom_dialog_for_search;
    private Context mContext;
    private EditText mEditText;
    private Button mSearchBtn;
    private String mKeyWord;
    private String mQuery;

    public CustomDialogForSearch(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        mEditText = findViewById(R.id.dialog_editText_keyword);
        mSearchBtn = findViewById(R.id.dialog_search_btn);
        mSearchBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mEditText.getText().toString().equals("")) {
            Toast.makeText(mContext, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        mKeyWord = mEditText.getText().toString();
        mQuery = "&query=" + mEditText.getText().toString().replaceAll(" ", "+");
        dismiss();
    }

    public String getKeyWord(){
        return mKeyWord;
    }

    public String getQuery(){
        return mQuery;
    }
}
