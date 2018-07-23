package kr.ac.kumoh.s20130053.okky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivityOnKeyboard extends AppCompatActivity implements View.OnClickListener{
    private InputMethodManager softKeyboard;
    private String mKeyword;
    private EditText mEditText;
    private Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_on_keyboard);

        // 기본값 초기화
        this.mKeyword = "";

        mEditText = findViewById(R.id.search_editText);
        mButton = findViewById(R.id.search_button);
        mButton.setOnClickListener(this);

        // 키보드 자동 활성화
        softKeyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        softKeyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        // 빈공간 클릭 시 액티비티 종료
        ConstraintLayout constraintLayout = findViewById(R.id.background);
        constraintLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.background :
                softKeyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
                finish();
                break;
            case R.id.search_button :
                mKeyword = mEditText.getText().toString();
                if (mKeyword.equals("") || mKeyword == null)
                    Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent();
                    intent.putExtra("searchKeyword", getSearchKeyword());
                    intent.putExtra("query", getQuery());
                    setResult(Activity.RESULT_OK, intent);
                    softKeyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    finish();
                }
                break;
        }
    }

    public String getSearchKeyword() {
        return " : " + mKeyword;
    }

    public String getQuery() {
        return "&query=" + mKeyword
                .replaceAll(" ", "+")
                .replaceAll("[+]", "%2B")
                .replaceAll("!", "%21")
                .replaceAll("@", "%40")
                .replaceAll("#", "%23")
                .replaceAll("%", "%25")
                .replaceAll("&", "%26")
                .replaceAll("[(]", "%28")
                .replaceAll("[)]", "%29")
                .replaceAll("=", "%3D");
    }

    @Override
    protected void onStop() {
        // 홈버튼 누를 시 검색 액티비티 및 키보드 종료
        softKeyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        finish();
        super.onStop();
    }
}
