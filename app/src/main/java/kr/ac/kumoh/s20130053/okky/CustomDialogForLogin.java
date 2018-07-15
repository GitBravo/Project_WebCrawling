package kr.ac.kumoh.s20130053.okky;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CustomDialogForLogin extends Dialog implements View.OnClickListener {
    private static final int LAYOUT = R.layout.custom_dialog_for_login;
    private Context mContext;

    private TextInputEditText mEditText_ID;
    private TextInputEditText mEditText_PW;
    private Button mDialogLoginBtn;

    CustomDialogForLogin(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        mEditText_ID = findViewById(R.id.dialog_editText_id);
        mEditText_PW = findViewById(R.id.dialog_editText_pw);
        mDialogLoginBtn = findViewById(R.id.dialog_login_btn);
        mDialogLoginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
