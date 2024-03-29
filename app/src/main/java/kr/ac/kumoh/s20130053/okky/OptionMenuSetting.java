package kr.ac.kumoh.s20130053.okky;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class OptionMenuSetting extends AppCompatActivity {
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option_menu_setting);

        // ActionBar 대신 ToolBar 적용 후 타이틀 설정
        setSupportActionBar((androidx.appcompat.widget.Toolbar) findViewById(R.id.OptionMenu_toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.Setting);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // SupportActionBar 에 Back 버튼 추가
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 옵션 메뉴에 들어갈 ArrayList 초기화 및 값 입력
        ArrayList<String> mTitle = new ArrayList<>();
        ArrayList<String> mContent = new ArrayList<>();

        mTitle.add(getResources().getString(R.string.AppVersion));
        mContent.add(getResources().getString(R.string.Version));
        mTitle.add(getResources().getString(R.string.Privacy));
        mContent.add(getResources().getString(R.string.Privacy_content));

        // 리사이클러뷰 객체 할당 및 어댑터 부착
        RecyclerViewAdapterForOption mAdapter = new RecyclerViewAdapterForOption(this, mTitle, mContent);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        RecyclerView mRecyclerView = findViewById(R.id.OptionMenu_recyclerView);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 이하 아이템 클릭 시 액션
                if (position == 1) {
                    // 두 번째 아이템 선택
                    new Personal(getApplicationContext()).removeAllAlreadyRead();
                    Toasty.Config.getInstance().setSuccessColor(getResources().getColor(R.color.colorPrimary)).apply();
                    Toasty.success(OptionMenuSetting.this, "초기화 완료", Toast.LENGTH_SHORT, true).show();
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // back 버튼 클릭시 뒤로 가기
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
