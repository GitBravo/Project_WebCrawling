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

    private TextInputEditText editText_id;
    private TextInputEditText editText_pw;
    private Button login_btn;

    public CustomDialogForLogin(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        editText_id = findViewById(R.id.dialog_editText_id);
        editText_pw = findViewById(R.id.dialog_editText_pw);
        login_btn = findViewById(R.id.dialog_login_btn);
        login_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(mContext, "아이디 : " + editText_id.getText(), Toast.LENGTH_SHORT).show();
    }
}
