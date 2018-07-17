package kr.ac.kumoh.s20130053.okky;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Board extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerViewAdapterForBoard mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerViewEndlessScrollListener mScrollListener;
    private RecyclerView mRecyclerView;
    private int currentPage;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigationView;
    private View navigationHeaderView;

    // Back 버튼 연속눌림 시간측정 변수
    private long time = 0;
    private String boardURL;
    private String boardTitle;
    private String searchKeyword;
    private int boardPageCount;

    // Q&A 게시판인지 여부 판단 변수
    public static boolean isQNA;

    // 검색시 쿼리문 저장 변수
    private String query;

    // 게시글 제목, 게시글 주소, (덧글수, 추천수, 조회수), 아이디, 활동점수, 게시시간
    private ArrayList<String> mTitle, mTitle_Href, mCount, mId, mActPoint, mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawerlayout);

        // 광고 객체 초기화
        MobileAds.initialize(this, "ca-app-pub-4355755954533542~2572341570");

        // 초기 설정
        boardTitle = "커뮤니티"; // 기본 게시판 제목
        searchKeyword = "";
        boardURL = "https://okky.kr/articles/community"; // 기본 게시판 URL
        boardPageCount = 20; // 기본 게시판 글 개수
        currentPage = 0; // 기본 게시판 페이지 번호
        isQNA = false; // QNA 게시판 여부
        query = ""; // 검색 쿼리문

        // ActionBar 대신 ToolBar 적용
        setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolBar_board));
        getSupportActionBar().setTitle(boardTitle);

        // 드로워레이아웃 및 토글버튼 초기화 코드
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.DrawerOpen, R.string.DrawerClose);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 각 게시글에 대한 정보를 저장하는 변수 6개 객체 할당
        mTitle = new ArrayList<>();
        mTitle_Href = new ArrayList<>();
        mCount = new ArrayList<>();
        mId = new ArrayList<>();
        mActPoint = new ArrayList<>();
        mDate = new ArrayList<>();

        // 리니어레이아웃 매니저, 리사이클러뷰 아답터 객체 생성
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new RecyclerViewAdapterForBoard(this, mTitle, mCount, mDate, mId);

        // 스크롤 리스너 객체 생성 후 스크롤 리스너 등록
        mScrollListener = new RecyclerViewEndlessScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // 스크롤 최하단 도착 시 액션
                new JsoupAsyncTask(Board.this, currentPage++).execute();
            }
        };

        // 리사이클러뷰 객체 선언 후 위에서 선언한 매니저, 아답터 부착
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mScrollListener);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 아이템 클릭 시 액션
                Intent intent = new Intent(Board.this, Detail.class);
                intent.putExtra("mTitle", mTitle.get(position)); // 글제목
                intent.putExtra("mTitle_Href", mTitle_Href.get(position)); // 글주소
                intent.putExtra("mId", mId.get(position)); // 아이디
                intent.putExtra("mDate", mDate.get(position)); // 게시날짜
                if (isQNA) {
                    intent.putExtra("mRecCount", mCount.get(2 * position)); // 추천수
                    intent.putExtra("mHits", mCount.get(2 * position + 1)); // 조회수
                } else {
                    intent.putExtra("mRecCount", mCount.get(3 * position + 1)); // 추천수
                    intent.putExtra("mHits", mCount.get(3 * position + 2)); // 조회수
                }

                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        // 네비게이션 뷰 초기화
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.qna_all) {
                    boardTitle = "Q&A";
                    boardURL = "https://okky.kr/articles/questions";
                    searchKeyword = "";
                    isQNA = true;
                    query = "";
                } else if (id == R.id.qna_tech) {
                    boardTitle = "Tech Q&A";
                    boardURL = "https://okky.kr/articles/tech-qna";
                    searchKeyword = "";
                    isQNA = true;
                    query = "";
                } else if (id == R.id.qna_blockchain) {
                    boardTitle = "Blockchain Q&A";
                    boardURL = "https://okky.kr/articles/blockchain-qna";
                    searchKeyword = "";
                    isQNA = true;
                    query = "";
                } else if (id == R.id.tech_ALL) {
                    boardTitle = "Tech";
                    boardURL = "https://okky.kr/articles/tech";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.tech_news) {
                    boardTitle = "IT News & 정보";
                    boardURL = "https://okky.kr/articles/news";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.tech_tips) {
                    boardTitle = "Tips & 강좌";
                    boardURL = "https://okky.kr/articles/tips";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.community_all) {
                    boardTitle = "커뮤니티";
                    boardURL = "https://okky.kr/articles/community";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.community_notice) {
                    boardTitle = "공지사항";
                    boardURL = "https://okky.kr/articles/notice";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.community_life) {
                    boardTitle = "사는얘기";
                    boardURL = "https://okky.kr/articles/life";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.community_forum) {
                    boardTitle = "포럼";
                    boardURL = "https://okky.kr/articles/forum";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.community_it_event) {
                    boardTitle = "IT 행사";
                    boardURL = "https://okky.kr/articles/event";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.community_study) {
                    boardTitle = "정기모임/스터디";
                    boardURL = "https://okky.kr/articles/gathering";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.community_edu) {
                    boardTitle = "학원/홍보";
                    boardURL = "https://okky.kr/articles/promote";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.column) {
                    boardTitle = "칼럼";
                    boardURL = "https://okky.kr/articles/columns";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.jobs_ALL) {
                    boardTitle = "Jobs";
                    boardURL = "https://okky.kr/articles/jobs";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.jobs_goodCompany) {
                    boardTitle = "좋은회사/나쁜회사";
                    boardURL = "https://okky.kr/articles/evalcom";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.jobs_opening) {
                    boardTitle = "구인";
                    boardURL = "https://okky.kr/articles/recruit";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                } else if (id == R.id.jobs_jobHunt) {
                    boardTitle = "구직";
                    boardURL = "https://okky.kr/articles/resumes";
                    searchKeyword = "";
                    isQNA = false;
                    query = "";
                }
                setLocalDataRemove();
                new JsoupAsyncTask(Board.this, 0).execute(); // 새 게시판 글 갱신
                mDrawerLayout.closeDrawer(GravityCompat.START); // 네비게이션 드로어 닫기
                mSwipeRefreshLayout.setRefreshing(true); // 리프레쉬 아이콘 생성
                return false;
            }
        });

        // 네비게이션 헤더뷰 초기화
        navigationHeaderView = navigationView.getHeaderView(0);
        Button loginBtn = navigationHeaderView.findViewById(R.id.header_login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogForLogin dialog = new CustomDialogForLogin(Board.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();
            }
        });

        // 당겨서 새로고침
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setLocalDataRemove();
                new JsoupAsyncTask(Board.this, 0).execute();
                currentPage = 1;
            }
        });

        // 최초 실행시 게시글 불러오기
        mSwipeRefreshLayout.setRefreshing(true);
        new JsoupAsyncTask(Board.this, currentPage++).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 검색 액션 버튼 툴바에 부착
        getMenuInflater().inflate(R.menu.toolbar_search_btn, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 툴바에 부착된 버튼의 액션을 결정
        switch (item.getItemId()) {
            case R.id.action_search:
                final CustomDialogForSearch dialog = new CustomDialogForSearch(Board.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    // 검색 액션버튼 클릭 액션
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        /* Dialog 의 EditText 가 바로 포커스를 가져가서 소프트키보드가 자동으로 나오게끔 설정.
                        * 일반 액티비티에서는 자동으로 포커스를 갖지만 Dialog 내부에서는 포커스를 수동으로 잡아줘야함*/

                        // 참고로 Dismiss 되는 경우는 뒤로가기 or 아무것도 입력안함 2가지 상태
                        if (dialog.getKeyWord() != null) {
                            setLocalDataRemove();
                            query = dialog.getQuery();
                            new JsoupAsyncTask(Board.this, 0).execute();
                            mSwipeRefreshLayout.setRefreshing(true); // 리프레쉬 아이콘 생성
                            searchKeyword = " : " + dialog.getKeyWord();
                        }
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item) || mToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // 드로워레이아웃이 열려있는 상태에서 Back 키 누르면 자동 닫힘
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else if (System.currentTimeMillis() - time >= 2000) {
            // Back 버튼 연속 2회 눌러야 종료되도록 설정
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "한번 더 누르면 종료합니다", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000)
            super.onBackPressed();
    }

    public void setLocalDataRemove() {
        mTitle.clear();
        mTitle_Href.clear();
        mCount.clear();
        mId.clear();
        mActPoint.clear();
        mDate.clear();
        mAdapter.notifyDataSetChanged();
    }

    private static class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        /* Activity 클래스 하위에 존재하는 Non-static 내부 클래스는 Activity 클래스 보다
        오래 가지고 살아있기 때문에 GC 이 되지 않는다. 따라서 이러한 문제 때문에 메모리 누수가
        발생할 수 있다.

        위 문제를 해결하기 위해서는 익명 클래스, 로컬 및 내부 클래스 대신 static 중첩 클래스를 사용하거나
        최상위 클래스를 사용해야 한다. 하지만 이 경우 UI View 또는 멤버 변수에 접근하지 못한다는 문제점
        을 갖고 있는데 그에 대한 해결책으로 WeakReference 를 만들어 준다.*/
        private WeakReference<Board> mActivityReference;
        private int mPage;

        JsoupAsyncTask(Board context, int page) {
            // 생성자
            mActivityReference = new WeakReference<>(context);
            mPage = page;
        }

        @Override
        protected void onPreExecute() {
            // 백그라운드 작업 진행 전 실행될 작업
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            int boardCount;
            Board activity = mActivityReference.get();
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
                 *
                 * 띄어쓰기로 각 태그를 구분하면 하위에 있는 해당 이름의 태그를 모두 가져온다.
                 * 하지만 > 로 구분하면 해당 계층구조에 있는 태그만 가져온다.
                 * */
                Document doc = Jsoup.connect(activity.boardURL + "?offset=" + (mPage * activity.boardPageCount) + "&max=20&sort=id&order=desc" + activity.query).get(); // 타겟 페이지 URL

                // 1. 게시글 제목 2. 게시글 주소
                Elements title = doc.select("#list-article > " +
                        ".panel.panel-default " +
                        ".list-group " +
                        ".list-title-wrapper.clearfix " +
                        ".list-group-item-heading.list-group-item-evaluate a");
                boardCount = 1;
                for (Element link : title) {
                    if (boardCount > activity.boardPageCount)
                        break;
                    activity.mTitle.add(link.text().trim());
                    activity.mTitle_Href.add(link.attr("abs:href"));
                    boardCount++;
                }

                // QNA 일 경우 추천수, 덧글수 게시글당 2개씩 나옴
                if (isQNA) {
                    Elements recCount = doc.select("#list-article > " +
                            ".panel.panel-default " +
                            ".list-group " +
                            ".list-summary-wrapper.clearfix " +
                            ".item-evaluate-count");
                    boardCount = 1;
                    for (Element link : recCount) {
                        if (boardCount > activity.boardPageCount * 2)
                            break;
                        activity.mCount.add(link.text().trim());
                        boardCount++;
                    }
                } else {
                    // 게시글 덧글수, 추천수, 조회수. 게시글당 3개씩 나옴
                    Elements recCount = doc.select("#list-article > " +
                            ".panel.panel-default " +
                            ".list-group " +
                            ".list-group-item-summary.clearfix " +
                            "ul " +
                            "li");
                    boardCount = 1;
                    for (Element link : recCount) {
                        if (boardCount > activity.boardPageCount * 3)
                            break;
                        activity.mCount.add(link.text().trim());
                        boardCount++;
                    }
                }


                // 게시글 아이디, 유저정보주소
                Elements account = doc.select("#list-article > " +
                        ".panel.panel-default " +
                        ".list-group " +
                        ".avatar-info " +
                        ".nickname");
                boardCount = 1;
                for (Element link : account) {
                    if (boardCount > activity.boardPageCount)
                        break;
                    activity.mId.add(link.text().trim());
                    boardCount++;
                }

                // 활동점수
                Elements actPoint = doc.select("#list-article > " +
                        ".panel.panel-default " +
                        ".list-group " +
                        ".avatar-info " +
                        ".activity");
                boardCount = 1;
                for (Element link : actPoint) {
                    if (boardCount > activity.boardPageCount)
                        break;
                    activity.mActPoint.add(link.text().trim());
                    boardCount++;
                }

                // 게시시간
                Elements date = doc.select("#list-article > " +
                        ".panel.panel-default " +
                        ".list-group " +
                        ".avatar-info " +
                        ".date-created " +
                        "span.timeago");
                boardCount = 1;
                for (Element link : date) {
                    if (boardCount > activity.boardPageCount)
                        break;
                    activity.mDate.add(link.text().trim());
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
            Board activity = mActivityReference.get(); // Activity 객체 획득
            activity.mAdapter.notifyDataSetChanged(); // 각 게시글 데이터 출력
            activity.getSupportActionBar().setTitle(activity.boardTitle + activity.searchKeyword); // 게시판 제목 재설정
            activity.mSwipeRefreshLayout.setRefreshing(false); // 리프레쉬 아이콘 제거
            activity.mScrollListener.resetState(); // 스크롤바 위치 재조정
        }
    }
}
