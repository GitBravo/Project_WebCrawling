package kr.ac.kumoh.s20130053.okky;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class Board extends AppCompatActivity implements View.OnClickListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerViewAdapterForBoard mAdapter;
    private RecyclerViewEndlessScrollListener mScrollListener;
    private RecyclerView mRecyclerView;
    private int currentPage;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    // Back 버튼 연속눌림 시간측정 변수
    private long time = 0;
    private String boardURL;
    private String boardTitle;
    private String sort;

    // Q&A 게시판인지 여부 판단 변수
    public static boolean isQNA;

    // 게시글 제목, 게시글 주소, (덧글수, 추천수, 조회수), 아이디, 게시시간, 아이디 주소
    private ArrayList<String> mTitle, mTitle_Href, mCount, mId, mDate;

    // 검색 액티비티 결과값 저장 변수
    private boolean isSearchComplete;
    private String searchKeyword;
    private String query;

    private Button bottomBtn1;
    private Button bottomBtn2;
    private Button bottomBtn3;
    private Button bottomBtn4;
    private Button bottomBtn5;

    // SharedPreferences 객체 선언
    private Personal personal;

    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawerlayout);

        // 애드몹 광고 객체 초기화
        MobileAds.initialize(this, initializationStatus -> {});

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        isSearchComplete = true;
                        if(intent != null) {
                            searchKeyword = intent.getStringExtra("searchKeyword");
                            query = intent.getStringExtra("query");
                        }

                        setRefresh();
                    }
                });

        // Board 의 하단 버튼 5개 리스너 부착
        bottomBtn1 = findViewById(R.id.bottomBtn1);
        bottomBtn2 = findViewById(R.id.bottomBtn2);
        bottomBtn3 = findViewById(R.id.bottomBtn3);
        bottomBtn4 = findViewById(R.id.bottomBtn4);
        bottomBtn5 = findViewById(R.id.bottomBtn5);
        bottomBtn1.setOnClickListener(this);
        bottomBtn2.setOnClickListener(this);
        bottomBtn3.setOnClickListener(this);
        bottomBtn4.setOnClickListener(this);
        bottomBtn5.setOnClickListener(this);

        // 초기 설정
        boardTitle = "커뮤니티"; // 기본 게시판 제목
        boardURL = "https://okky.kr/articles/community"; // 기본 게시판 URL
        currentPage = 0; // 게시판 시작 페이지 번호
        sort = "id"; // 정렬 쿼리
        bottomBtn1.setBackgroundResource(R.color.colorPrimaryDark); // 정렬 버튼 색상변경
        isQNA = false; // QNA 게시판 여부
        isSearchComplete = false; // 검색 여부

        // ActionBar 대신 ToolBar 적용
        setSupportActionBar(findViewById(R.id.toolBar_board));
        if (getSupportActionBar() != null)
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
        mDate = new ArrayList<>();

        // 리니어레이아웃 매니저, 리사이클러뷰 아답터 객체 생성
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mAdapter = new RecyclerViewAdapterForBoard(this, mTitle, mCount, mDate, mId, mTitle_Href);

        // 스크롤 리스너 객체 생성 후 스크롤 리스너 등록
        mScrollListener = new RecyclerViewEndlessScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // 스크롤 최하단 도착 시 게시글 추가로드
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
                // 이하 아이템 클릭 시 액션
                Intent intent = new Intent(Board.this, Detail.class);
                intent.putExtra("mTitle_Href", mTitle_Href.get(position)); // 글 주소

                // SharedPreferences 객체를 사용하여 읽음 표시
                personal = new Personal(getApplicationContext());
                personal.setAlreadyRead(mTitle_Href.get(position));

                // Detail 액티비티 시작
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mTitle_Href.get(position))));
            }
        }));

        // 네비게이션 뷰 초기화
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            // 현재 리프레쉬중이면 아이템 선택 금지
            if (mSwipeRefreshLayout.isRefreshing())
                return false;

            int id = item.getItemId();
            if (id == R.id.qna_all) {
                boardTitle = "Q&A";
                boardURL = "https://okky.kr/articles/questions";
                isQNA = true;
            } else if (id == R.id.qna_tech) {
                boardTitle = "Tech Q&A";
                boardURL = "https://okky.kr/articles/tech-qna";
                isQNA = true;
            } else if (id == R.id.qna_blockchain) {
                boardTitle = "Blockchain Q&A";
                boardURL = "https://okky.kr/articles/blockchain-qna";
                isQNA = true;
            } else if (id == R.id.tech_ALL) {
                boardTitle = "Tech";
                boardURL = "https://okky.kr/articles/tech";
                isQNA = false;
            } else if (id == R.id.tech_news) {
                boardTitle = "IT News & 정보";
                boardURL = "https://okky.kr/articles/news";
                isQNA = false;
            } else if (id == R.id.tech_tips) {
                boardTitle = "Tips & 강좌";
                boardURL = "https://okky.kr/articles/tips";
                isQNA = false;
            } else if (id == R.id.community_all) {
                boardTitle = "커뮤니티";
                boardURL = "https://okky.kr/articles/community";
                isQNA = false;
            } else if (id == R.id.community_notice) {
                boardTitle = "공지사항";
                boardURL = "https://okky.kr/articles/notice";
                isQNA = false;
            } else if (id == R.id.community_life) {
                boardTitle = "사는얘기";
                boardURL = "https://okky.kr/articles/life";
                isQNA = false;
            } else if (id == R.id.community_forum) {
                boardTitle = "포럼";
                boardURL = "https://okky.kr/articles/forum";
                isQNA = false;
            } else if (id == R.id.community_it_event) {
                boardTitle = "IT 행사";
                boardURL = "https://okky.kr/articles/event";
                isQNA = false;
            } else if (id == R.id.techbook_review) {
                boardTitle = "기술 서적 리뷰";
                boardURL = "https://okky.kr/articles/techbook-review";
                isQNA = false;
            } else if (id == R.id.device_review) {
                boardTitle = "IT 제품 리뷰";
                boardURL = "https://okky.kr/articles/device-review";
                isQNA = false;
            } else if (id == R.id.community_study) {
                boardTitle = "정기모임/스터디";
                boardURL = "https://okky.kr/articles/gathering";
                isQNA = false;
            } else if (id == R.id.community_edu) {
                boardTitle = "학원/홍보";
                boardURL = "https://okky.kr/articles/promote";
                isQNA = false;
            } else if (id == R.id.column) {
                boardTitle = "칼럼";
                boardURL = "https://okky.kr/articles/columns";
                isQNA = false;
            /*} else if (id == R.id.jobs_ALL) {
                boardTitle = "Jobs";
                boardURL = "https://okky.kr/articles/jobs";
                isQNA = false;
                */
            } else if (id == R.id.jobs_goodCompany) {
                boardTitle = "좋은회사/나쁜회사";
                boardURL = "https://okky.kr/articles/evalcom";
                isQNA = false;
            /*} else if (id == R.id.jobs_opening) {
                boardTitle = "구인";
                boardURL = "https://okky.kr/articles/recruit";
                isQNA = false;
                */
            } else if (id == R.id.jobs_jobHunt) {
                boardTitle = "구직";
                boardURL = "https://okky.kr/articles/resumes";
                isQNA = false;
            }
            if (isSearchComplete)
                isSearchComplete = false;
            sort = "id";
            bottomBtn1.setBackgroundResource(R.color.colorPrimaryDark);
            bottomBtn2.setBackgroundResource(R.color.colorPrimary);
            bottomBtn3.setBackgroundResource(R.color.colorPrimary);
            bottomBtn4.setBackgroundResource(R.color.colorPrimary);
            bottomBtn5.setBackgroundResource(R.color.colorPrimary);
            setRefresh();
            mDrawerLayout.closeDrawer(GravityCompat.START); // 네비게이션 드로어 닫기
            return false;
        });

        // 당겨서 새로고침
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.refreshIcon);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorBackground);
        mSwipeRefreshLayout.setOnRefreshListener(this::setRefresh);

        // 최초 실행시 게시글 불러오기
        setRefresh();
    }

    @Override
    public void onClick(View v) {
        if (mSwipeRefreshLayout.isRefreshing())
            return;

        if (v.getId() == R.id.bottomBtn1) {
            sort = "id";
            bottomBtn1.setBackgroundResource(R.color.colorPrimaryDark);
            bottomBtn2.setBackgroundResource(R.color.colorPrimary);
            bottomBtn3.setBackgroundResource(R.color.colorPrimary);
            bottomBtn4.setBackgroundResource(R.color.colorPrimary);
            bottomBtn5.setBackgroundResource(R.color.colorPrimary);
        } else if (v.getId() == R.id.bottomBtn2) {
            sort = "voteCount";
            bottomBtn1.setBackgroundResource(R.color.colorPrimary);
            bottomBtn2.setBackgroundResource(R.color.colorPrimaryDark);
            bottomBtn3.setBackgroundResource(R.color.colorPrimary);
            bottomBtn4.setBackgroundResource(R.color.colorPrimary);
            bottomBtn5.setBackgroundResource(R.color.colorPrimary);
        } else if (v.getId() == R.id.bottomBtn3) {
            sort = "noteCount";
            bottomBtn1.setBackgroundResource(R.color.colorPrimary);
            bottomBtn2.setBackgroundResource(R.color.colorPrimary);
            bottomBtn3.setBackgroundResource(R.color.colorPrimaryDark);
            bottomBtn4.setBackgroundResource(R.color.colorPrimary);
            bottomBtn5.setBackgroundResource(R.color.colorPrimary);
        } else if (v.getId() == R.id.bottomBtn4) {
            sort = "scrapCount";
            bottomBtn1.setBackgroundResource(R.color.colorPrimary);
            bottomBtn2.setBackgroundResource(R.color.colorPrimary);
            bottomBtn3.setBackgroundResource(R.color.colorPrimary);
            bottomBtn4.setBackgroundResource(R.color.colorPrimaryDark);
            bottomBtn5.setBackgroundResource(R.color.colorPrimary);
        } else if (v.getId() == R.id.bottomBtn5) {
            sort = "viewCount";
            bottomBtn1.setBackgroundResource(R.color.colorPrimary);
            bottomBtn2.setBackgroundResource(R.color.colorPrimary);
            bottomBtn3.setBackgroundResource(R.color.colorPrimary);
            bottomBtn4.setBackgroundResource(R.color.colorPrimary);
            bottomBtn5.setBackgroundResource(R.color.colorPrimaryDark);
        }
        setRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 검색 액션 버튼 툴바에 부착
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_search_btn, menu);
        inflater.inflate(R.menu.toolbar_overflow_btn, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 툴바에 부착된 버튼의 액션을 결정
        int itemId = item.getItemId();
        if(itemId == R.id.actionBtn_search) {
            Intent intent = new Intent(getApplicationContext(), SearchActivityOnKeyboard.class);
            resultLauncher.launch(intent);
        } else if(itemId == R.id.OptionMenu_setting){
            startActivity(new Intent(this, OptionMenuSetting.class));
        }
        return super.onOptionsItemSelected(item) || mToggle.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            // 검색 성공
            if (resultCode == Activity.RESULT_OK) {
                isSearchComplete = true;
                searchKeyword = data.getStringExtra("searchKeyword");
                query = data.getStringExtra("query");

                setRefresh();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // 드로워레이아웃이 열려있는 상태에서 Back 키 누르면 자동 닫힘
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else if (System.currentTimeMillis() - time >= 2000) {
            // Back 버튼 연속 2회 눌러야 종료되도록 설정
            time = System.currentTimeMillis();
            Toasty.Config.getInstance().setWarningColor(getResources().getColor(R.color.colorGray)).apply();
            Toasty.warning(getApplicationContext(), "한번 더 누르면 종료합니다", Toast.LENGTH_SHORT, true).show();
        } else if (System.currentTimeMillis() - time < 2000)
            super.onBackPressed();
    }

    public void setRefresh() {
        currentPage = 0;
        setLocalDataRemove(); // 기존 데이터 제거
        new JsoupAsyncTask(Board.this, currentPage++).execute(); // 새 게시판 글 갱신
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setLocalDataRemove() {
        mTitle.clear();
        mTitle_Href.clear();
        mCount.clear();
        mId.clear();
        mDate.clear();
        mAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        // onResume 상태로 복귀할 때마다 게시글 읽었음을 표시하기 위해 notifyDataSetChanged() 호출
        mAdapter.notifyDataSetChanged();
    }

    private static class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        /* Activity 클래스 하위에 존재하는 Non-static 내부 클래스는 Activity 클래스 보다
        오래 가지고 살아있기 때문에 GC 이 되지 않는다. 따라서 이러한 문제 때문에 메모리 누수가
        발생할 수 있다.

        위 문제를 해결하기 위해서는 익명 클래스, 로컬 및 내부 클래스 대신 static 중첩 클래스를 사용하거나
        최상위 클래스를 사용해야 한다. 하지만 이 경우 UI View 또는 멤버 변수에 접근하지 못한다는 문제점
        을 갖고 있는데 그에 대한 해결책으로 WeakReference 를 만들어 준다.*/
        private final WeakReference<Board> mActivityReference;
        private final int mPage;

        JsoupAsyncTask(Board context, int page) {
            // 생성자
            mActivityReference = new WeakReference<>(context);
            mPage = page;
        }

        @Override
        protected void onPreExecute() {
            // 백그라운드 작업 진행 전 실행될 작업
            Board activity = mActivityReference.get();
            activity.mSwipeRefreshLayout.setRefreshing(true);
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

                Document doc; // 타겟 페이지 URL
                if (activity.isSearchComplete) {
                    // 검색 상황
                    doc = Jsoup.connect(activity.boardURL + "?offset=" + (mPage * 24)
                            + "&max=24&sort=" + activity.sort + "&order=desc" + activity.query).get();
                } else {
                    // 일반 상황
                    doc = Jsoup.connect(activity.boardURL + "?offset=" + (mPage * 24)
                            + "&max=24&sort=" + activity.sort + "&order=desc").get();
                }

                // 1. 게시글 제목 2. 게시글 주소
                Elements title = doc.select("#list-article > " +
                        ".panel.panel-default " +
                        ".list-group " +
                        ".list-title-wrapper.clearfix " +
                        ".list-group-item-heading.list-group-item-evaluate a");
                boardCount = 1;
                for (Element link : title) {
                    if (boardCount > 24)
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
                        if (boardCount > 48)
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
                        if (boardCount > 72)
                            break;
                        activity.mCount.add(link.text().trim());
                        boardCount++;
                    }
                }

                // 게시글 아이디
                Elements account = doc.select("#list-article > " +
                        ".panel.panel-default " +
                        ".list-group " +
                        ".avatar-info " +
                        ".nickname");
                boardCount = 1;
                for (Element link : account) {
                    if (boardCount > 24)
                        break;
                    activity.mId.add(link.text().trim());
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
                    if (boardCount > 24)
                        break;
                    activity.mDate.add(link.text().trim());
                    boardCount++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Void result) {
            // 백그라운드 작업 진행 후 실행될 작업
            Board activity = mActivityReference.get(); // Activity 객체 획득
            if (activity.isSearchComplete && activity.getSupportActionBar() != null)
                activity.getSupportActionBar().setTitle(activity.boardTitle + activity.searchKeyword);
            else if (!activity.isSearchComplete && activity.getSupportActionBar() != null)
                activity.getSupportActionBar().setTitle(activity.boardTitle);
            activity.mSwipeRefreshLayout.setRefreshing(false); // 리프레쉬 아이콘 제거
            activity.mScrollListener.resetState(); // 스크롤바 위치 재조정

            // 데이터가 없으면 에러표시
            if (activity.mAdapter.getItemCount() == 0) {
                activity.mRecyclerView.setVisibility(View.GONE);
                TextView tv = activity.findViewById(R.id.err_text);
                tv.setVisibility(View.VISIBLE);
            }else{
                activity.mRecyclerView.setVisibility(View.VISIBLE);
                TextView tv = activity.findViewById(R.id.err_text);
                tv.setVisibility(View.GONE);
            }
            activity.mAdapter.notifyDataSetChanged(); // 각 게시글 데이터 출력
        }
    }
}
