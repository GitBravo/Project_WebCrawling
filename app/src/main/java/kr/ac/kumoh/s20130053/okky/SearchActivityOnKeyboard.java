package kr.ac.kumoh.s20130053.okky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import es.dmoral.toasty.Toasty;

public class SearchActivityOnKeyboard extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {
    private InputMethodManager softKeyboard;
    private String mKeyword;
    private EditText mEditText;

    // 파이어베이스 애널리틱스
    public static FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_on_keyboard);

        // 파이어베이스 애널리틱스 객체 초기화
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // 기본값 초기화
        this.mKeyword = "";

        mEditText = findViewById(R.id.search_editText);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEditText.setOnEditorActionListener(this);

        // 키보드 자동 활성화
        softKeyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        softKeyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        // 빈공간 클릭 시 액티비티 종료
        ConstraintLayout constraintLayout = findViewById(R.id.background);
        constraintLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.background:
                softKeyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
                finish();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            mKeyword = mEditText.getText().toString();
            if (mKeyword.equals("")) {
                Toasty.Config.getInstance().setWarningColor(getResources().getColor(R.color.colorGray)).apply();
                Toasty.warning(getApplicationContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT, true).show();
            }  else {
                // Board 액티비티로 결과값 전송
                Intent intent = new Intent();
                intent.putExtra("searchKeyword", getSearchKeyword());
                intent.putExtra("query", getQuery());
                setResult(Activity.RESULT_OK, intent);
                softKeyboard.hideSoftInputFromWindow(v.getWindowToken(), 0); // 키보드 내리기
                // 파이어베이스 애널리틱스로 검색어 통계 전송
                Bundle bundle = new Bundle();
                bundle.putString("Sentence", mKeyword);
                mFirebaseAnalytics.logEvent("Keyword", bundle);

                //액티비티 종료
                finish();
            }
            return true;
        } else
            return false;
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
        if (getCurrentFocus() != null)
            softKeyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        finish();
        super.onStop();
    }
}
