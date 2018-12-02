package kr.ac.kumoh.s20130053.okky;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class UserInfo extends AppCompatActivity {
    private String mUrl;
    private String mUserName;
    private ArrayList<String> mActivity;
    private ArrayList<String> mTitle;

    private RecyclerViewAdapterForUserInfo mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Detail 액티비티로부터 url 주소, 유저이름 받아와서 툴바 타이틀 변경
        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        mUserName = intent.getStringExtra("userName");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mUserName);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // SupportActionBar 에 Back 버튼 추가
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mActivity = new ArrayList<>();
        mTitle = new ArrayList<>();

        // 리사이클러뷰 객체 할당 및 어댑터 부착
        mAdapter = new RecyclerViewAdapterForUserInfo(this, mActivity, mTitle);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        RecyclerView mRecyclerView = findViewById(R.id.UserInfo_recyclerView);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        mAdapter.notifyDataSetChanged();

        new UserInfoAsyncTask(this, mActivity, mTitle).execute(mUrl);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // back 버튼 클릭시 뒤로 가기
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private static class UserInfoAsyncTask extends AsyncTask<Object, Void, Void> {
        private WeakReference<UserInfo> mActivityReference;
        private String mUserPoint; // 활동점수
        private String mUserFollowing; // 팔로잉
        private String mUserFollower; // 팔로워
        private ArrayList<String> mActivity;
        private ArrayList<String> mTitle;

        UserInfoAsyncTask(UserInfo context, ArrayList<String> activity, ArrayList<String> title) {
            // 생성자
            mActivityReference = new WeakReference<>(context);
            mUserPoint = null;
            mUserFollowing = null;
            mUserFollower = null;
            mActivity = activity;
            mTitle = title;
        }

        @Override
        protected Void doInBackground(Object... params) {
            String url = (String) params[0];
            int count;
            try {
                /* div.className : 클래스명 className 만 가져오기
                 * div#id : 아이디명 id 만 가져오기
                 * div.className a : 클래스명 항목 중 a 태그만 가져오기
                 * input[name=btnK] : input 태그의 name 속성값이 btnK 인것을 가져오기
                 *
                 * 구성요소.text(); : 구성요소 값을 반환(태그는 포함하지 않음)
                 * 구성요소.attr("속성이름"); : 구성요소 "속성이름"에 대한 값을 반환
                 * 구성요소.html(); : 구성요소 값을 반환(태그도 포함)
                 * 구성요소.outerHtml(); : 구성요소를 반환(태그와 값 모두)
                 * */

                Document doc = Jsoup.connect(url).get(); // 타겟 페이지 URL

                // 활동점수 가져오기
                Elements elements = doc.select("#user > div.panel.panel-default > div > div.user-info.col-sm-9 > div.user-points > div:nth-child(1) > div.user-point-num > a");
                mUserPoint = elements.text();

                // 팔로잉 가져오기
                elements = doc.select("#user > div.panel.panel-default > div > div.user-info.col-sm-9 > div.user-points > div:nth-child(2) > div.user-point-num > a");
                mUserFollowing = elements.text();

                // 팔로워 가져오기
                elements = doc.select("#user > div.panel.panel-default > div > div.user-info.col-sm-9 > div.user-points > div:nth-child(3) > div.user-point-num > a");
                mUserFollower = elements.text();

                // 최근 활동 가져오기
                doc = Jsoup.connect(url + "/articles").get();
                elements = doc.select(".list-activity-desc-text");
                count = 1;
                for (Element link : elements) {
                    if (count > 20)
                        break;
                    mActivity.add(link.text());
                    count++;
                }

                // 최근 게시글 제목 가져오기
                doc = Jsoup.connect(url + "/articles").get();
                elements = doc.select(".list-group-item-heading > a");
                count = 1;
                for (Element link : elements) {
                    if (count > 20)
                        break;
                    mTitle.add(link.text());
                    count++;
                }

            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 백그라운드 작업 진행 후 실행될 작업
            UserInfo activity = mActivityReference.get(); // Activity 객체 획득
            TextView tv = activity.findViewById(R.id.UserInfo_point);
            tv.setText(mUserPoint);
            tv = activity.findViewById(R.id.UserInfo_following);
            tv.setText(mUserFollowing);
            tv = activity.findViewById(R.id.UserInfo_follower);
            tv.setText(mUserFollower);

            activity.mAdapter.notifyDataSetChanged();
        }
    }
}
