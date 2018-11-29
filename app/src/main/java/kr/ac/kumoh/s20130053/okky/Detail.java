package kr.ac.kumoh.s20130053.okky;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Detail extends AppCompatActivity implements View.OnClickListener {
    private String mTitle, mTitle_Href, mId, mId_Href, mDate, mRecCount, mHits; // Board 에서 받아오는 데이터

    private ArrayList<String> commentNickname; // 덧글 게시자
    private ArrayList<String> commentDate; // 덧글 게시날짜
    private ArrayList<String> commentContent; // 덧글 내용

    private RecyclerViewAdapterForDetail mAdapter;
    private NestedScrollView nestedScrollView;

    private TextView tvTitle;
    private TextView tvId;
    private TextView tvDate;
    private HTMLTextView tvContent; // 글 내용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // 광고 객체 초기화
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Board 액티비티로부터 현재 게시글 데이터 수신
        Intent intent = getIntent();
        mTitle = intent.getStringExtra("mTitle");
        mTitle_Href = intent.getStringExtra("mTitle_Href");
        mId = intent.getStringExtra("mId");
        mId_Href = intent.getStringExtra("mId_Href");
        mDate = intent.getStringExtra("mDate");
        mRecCount = intent.getStringExtra("mRecCount");
        mHits = intent.getStringExtra("mHits");

        // 텍스트뷰 선언 및 할당
        tvTitle = findViewById(R.id.detail_title);
        tvId = findViewById(R.id.detail_id);
        tvDate = findViewById(R.id.detail_date);
        tvContent = findViewById(R.id.detail_content);

        // 리스너 부착
        tvId.setOnClickListener(this);

        // 게시물 제목, 메타정보(게시자, 게시날짜)를 담기위한 TextView 출력
        tvTitle.setText(mTitle);
        tvId.setText(mId); // 게시자 닉네임
        tvDate.setText(getString(R.string.ThreeString, mDate, mRecCount, mHits)); // 게시 날짜

        // 덧글 내용을 담기위한 객체 선언
        commentContent = new ArrayList<>();
        commentNickname = new ArrayList<>();
        commentDate = new ArrayList<>();

        // 리니어레이아웃 매니저, 리사이클러뷰 아답터 객체 생성
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new RecyclerViewAdapterForDetail(this, commentNickname, commentDate, commentContent);

        // 리사이클러뷰 객체 선언 후 위에서 선언한 매니저, 아답터 부착
        RecyclerView mRecyclerView = findViewById(R.id.detail_recyclerView);
        mRecyclerView.setHasFixedSize(true); // 고정 크기 설정시 RecyclerView 의 성능을 개선할 수 있음
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        // 게시물 제목 선택시 최상단 이동
        nestedScrollView = findViewById(R.id.scrollview);
        findViewById(R.id.detail_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nestedScrollView = findViewById(R.id.scrollview);
                nestedScrollView.scrollTo(0, 0);
                nestedScrollView.smoothScrollTo(0, 0);
            }
        });

        // 게시글 내용 및 덧글 로드
        new JsoupAsyncTask(this).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_id :
                if (!mId_Href.equals("")) {
                    Intent intent = new Intent(this, UserInfo.class);
                    intent.putExtra("url", mId_Href);
                    intent.putExtra("userName", mId);
                    startActivity(intent);
                }
                break;
        }
    }

    private static class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        /* Activity 클래스 하위에 존재하는 Non-static 내부 클래스는 Activity 클래스 보다
        오래 가지고 살아있기 때문에 GC 이 되지 않는다. 따라서 이러한 문제 때문에 메모리 누수가
        발생할 수 있다.

        위 문제를 해결하기 위해서는 익명 클래스, 로컬 및 내부 클래스 대신 static 중첩 클래스를 사용하거나
        최상위 클래스를 사용해야 한다. 하지만 이 경우 UI View 또는 멤버 변수에 접근하지 못한다는 문제점
        을 갖고 있는데 그에 대한 해결책으로 WeakReference 를 만들어 준다.*/
        private WeakReference<Detail> mActivityReference;
        private StringBuffer tag; // 게시글 내용

        JsoupAsyncTask(Detail context) {
            // 생성자
            mActivityReference = new WeakReference<>(context);
            tag = new StringBuffer();
        }

        @Override
        protected void onPreExecute() {
            // 백그라운드 작업 진행 전 실행될 작업
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Detail activity = mActivityReference.get();
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

                Document doc = Jsoup.connect(activity.mTitle_Href).get(); // 타겟 페이지 URL

                // 게시글 내용 파싱
                Elements title = doc.select("article.content-text");
                for (Element link : title) {
                    tag.append(link.html());
                }

                // 덧글 게시자 파싱
                Elements comment_nick = doc.select("div.content-body.panel-body.pull-left " +
                        "div.avatar.avatar-medium.clearfix " +
                        "div.avatar-info " +
                        ".nickname");
                for (Element link : comment_nick) {
                    activity.commentNickname.add(link.text());
                }

                // 덧글 게시날짜 파싱
                Elements comment_date = doc.select("div.content-body.panel-body.pull-left " +
                        "div.avatar.avatar-medium.clearfix " +
                        "div.avatar-info " +
                        "div.date-created");
                for (Element link : comment_date) {
                    activity.commentDate.add(link.text());
                }

                // 덧글 내용 파싱
                Elements comment = doc.select("article.list-group-item-text.note-text");
                for (Element link : comment) {
                    activity.commentContent.add(link.html()); // HTML 그대로 가져옴
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 백그라운드 작업 진행 후 실행될 작업
            final Detail activity = mActivityReference.get(); // Activity 객체 획득
            if (activity.tvContent != null)
                activity.tvContent.setHtmlText(tag.toString());
            activity.mAdapter.notifyDataSetChanged(); // 각 덧글 데이터 출력
        }
    }
}
