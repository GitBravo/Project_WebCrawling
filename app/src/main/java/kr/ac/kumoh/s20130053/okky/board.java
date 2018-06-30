package kr.ac.kumoh.s20130053.okky;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class board extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);

        new JsoupAsyncTask(board.this).execute();

        // 글 새로고침 버튼
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new JsoupAsyncTask(board.this).execute();
            }
        });
    }


    private static class JsoupAsyncTask extends AsyncTask<Void, Void, Void> implements AdapterView.OnItemClickListener {
        /* Activity 클래스 하위에 존재하는 Non-static 내부 클래스는 Activity 클래스 보다
        오래 가지고 살아있기 때문에 GC 이 되지 않는다. 따라서 이러한 문제 때문에 메모리 누수가
        발생할 수 있다.

        위 문제를 해결하기 위해서는 익명 클래스, 로컬 및 내부 클래스 대신 static 중첩 클래스를 사용하거나
        최상위 클래스를 사용해야 한다. 하지만 이 경우 UI View 또는 멤버 변수에 접근하지 못한다는 문제점
        을 갖고 있는데 그에 대한 해결책으로 WeakReference 를 만들어 준다.*/
        private WeakReference<board> mActivityReference;
        private ArrayList<String[]> mBoardContent;

        JsoupAsyncTask(board context) {
            // 생성자
            mActivityReference = new WeakReference<>(context);
            mBoardContent = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            // 백그라운드 작업 진행 전 실행될 작업
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("https://okky.kr/articles/community").get(); // 타겟 페이지 URL
                /* div.className : 클래스명 className 만 가져오기
                 * div#id : 아이디명 id 만 가져오기
                 * div.className a : 클래스명 항목 중 a 태그만 가져오기
                 * input[name=btnK] : input 태그의 name 속성값이 btnK 인것을 가져오기
                 * */

                // 게시글 제목, 게시글내용주소
                Elements title = doc.select("div.list-title-wrapper.clearfix " +
                        "h5.list-group-item-heading.list-group-item-evaluate a");

                // 게시글 덧글수, 추천수, 조회수. 게시글당 3개씩 나옴
                Elements count = doc.select("div.list-group-item-summary.clearfix " +
                        "ul " +
                        "li");

                // 게시글 아이디, 유저정보주소
                Elements account = doc.select("div.avatar-info a");

                // 활동점수
                Elements activityPoint = doc.select("div.avatar-info " +
                        "div.activity");

                // 게시시간
                Elements date = doc.select("div.avatar-info " +
                        "div.date-created " +
                        "span.timeago");

                int boardCount = 1;
                for (Element link : title) {
                    if (boardCount > 20)
                        break;
                    // 게시글 제목, 게시글 주소, 덧글수, 추천수, 조회수, 아이디, 유저 주소, 활동점수, 게시시간

                    // ★★★★★★★★★★★★★★★★여기부터 코딩시작★★★★★★★★★★★★★★★★
                    mBoardContent.add(new String[]{link.text().trim(), link.attr("abs:href")});

                    boardCount++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 백그라운드 작업 진행 후 실행될 작업

            // 액티비티 객체 참조 획득. 만약 획득할 수 없다면 리턴.
            final board activity = mActivityReference.get();
            if (activity == null || activity.isFinishing())
                return;

            // 리니어레이아웃 매니저, 리사이클러뷰 아답터 객체 선언
            LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(activity, mBoardContent);

            // 리사이클러뷰 객체 선언 후 위에서 선언한 매니저, 아답터 부착
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(activity, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    activity.startActivity(new Intent(activity, detail.class));
                }

                @Override
                public void onLongItemClick(View view, int position) {

                }
            }));

            // 리프레쉬 아이콘 제거
            activity.mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mActivityReference.get().startActivity(new Intent(mActivityReference.get(), detail.class));
        }
    }


}
