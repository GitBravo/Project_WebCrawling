package kr.ac.kumoh.s20130053.okky;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class community extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask(community.this);
                jsoupAsyncTask.execute();
            }
        });
    }

    private static class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        /* Activity 클래스 하위에 존재하는 Non-static 내부 클래스는 Activity 클래스 보다
        오래 가지고 살아있기 때문에 GC 이 되지 않는다. 따라서 이러한 문제 때문에 메모리 누수가
        발생할 수 있다.

        위 문제를 해결하기 위해서는 익명 클래스, 로컬 및 내부 클래스 대신 static 중첩 클래스를 사용하거나
        최상위 클래스를 사용해야 한다. 하지만 이 경우 UI View 또는 멤버 변수에 접근하지 못한다는 문제점
        을 갖고 있는데 그에 대한 해결책으로 WeakReference 를 만들어 준다.*/
        private WeakReference<community> activityReference;
        private String Content;

        JsoupAsyncTask(community context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            // 진행 전 실행될 작업
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Content = "";
            int count;
            try {
                Document doc = Jsoup.connect("https://www.naver.com/").get();
                Elements links = doc.select("span.ah_k"); // 셀렉터로 <span> 태그 중 class 값이 ah_k(검색어) 인 내용을 획득

                count = 1;
                for (Element link : links) {
                    if (count > 10)
                        break;
                    Content += (link.attr("abs:href") + count + ". " + link.text().trim() + "\n");
                    count++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // 진행 후 실행될 작업

            // 액티비티 객체 참조 획득. 만약 획득할 수 없다면 리턴.
            community activity = activityReference.get();
            if (activity == null || activity.isFinishing())
                return;

            // 내부 View 객체에 접근
            TextView textView = activity.findViewById(R.id.textView);
            textView.setText(Content);
        }
    }
}
