package kr.ac.kumoh.s20130053.okky;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class detail extends AppCompatActivity {
    private String mTitle, mTitle_Href, mId, mDate, mRecCount, mHits;
    private TextView mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // board 액티비티로부터 현재 게시글 데이터 수신
        Intent intent = getIntent();
        mTitle = intent.getStringExtra("mTitle");
        mTitle_Href = intent.getStringExtra("mTitle_Href");
        mId = intent.getStringExtra("mId");
        mDate = intent.getStringExtra("mDate");
        mRecCount = intent.getStringExtra("mRecCount");
        mHits = intent.getStringExtra("mHits");

        TextView tv = findViewById(R.id.detail_id);
        tv.setText(mId);
        tv = findViewById(R.id.detail_date);
        tv.setText(mDate + "ㆍ" + mRecCount + "ㆍ" + mHits);

        mContent = findViewById(R.id.detail_content);

        // SupportActionBar 의 제목을 게시글 제목으로 변경
        getSupportActionBar().setTitle(mTitle);

        new JsoupAsyncTask(this).execute();

    }

    private static class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        /* Activity 클래스 하위에 존재하는 Non-static 내부 클래스는 Activity 클래스 보다
        오래 가지고 살아있기 때문에 GC 이 되지 않는다. 따라서 이러한 문제 때문에 메모리 누수가
        발생할 수 있다.

        위 문제를 해결하기 위해서는 익명 클래스, 로컬 및 내부 클래스 대신 static 중첩 클래스를 사용하거나
        최상위 클래스를 사용해야 한다. 하지만 이 경우 UI View 또는 멤버 변수에 접근하지 못한다는 문제점
        을 갖고 있는데 그에 대한 해결책으로 WeakReference 를 만들어 준다.*/
        private WeakReference<detail> mActivityReference;
        private String content;

        JsoupAsyncTask(detail context) {
            // 생성자
            mActivityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            // 백그라운드 작업 진행 전 실행될 작업
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            content = "";
            try {
                /* div.className : 클래스명 className 만 가져오기
                 * div#id : 아이디명 id 만 가져오기
                 * div.className a : 클래스명 항목 중 a 태그만 가져오기
                 * input[name=btnK] : input 태그의 name 속성값이 btnK 인것을 가져오기
                 * */
                Document doc = Jsoup.connect(mActivityReference.get().mTitle_Href).get(); // 타겟 페이지 URL

                // 게시글 내용 파싱
                Elements title = doc.select("article.content-text p");
                for (Element link : title) {
                    content += link.text().trim();
                    content += "\n\n";
//                    mActivityReference.get().mTitle.add(link.text().trim());
//                    mActivityReference.get().mTitle_Href.add(link.attr("abs:href"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 백그라운드 작업 진행 후 실행될 작업

            mActivityReference.get().mContent.setText(content);

//            // 로컬 데이터 변경
//            mActivityReference.get().mAdapter.notifyDataSetChanged();
//
//            // 리프레쉬 아이콘 제거
//            mActivityReference.get().mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
