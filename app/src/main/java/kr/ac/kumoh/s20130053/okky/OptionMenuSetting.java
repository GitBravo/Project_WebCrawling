package kr.ac.kumoh.s20130053.okky;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class OptionMenuSetting extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_menu_setting);

        // ActionBar 대신 ToolBar 적용 후 타이틀 설정
        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.OptionMenu_toolbar));
        getSupportActionBar().setTitle(R.string.Setting);

        // SupportActionBar 에 Back 버튼 추가
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // back 버튼 클릭시 뒤로 가기
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
