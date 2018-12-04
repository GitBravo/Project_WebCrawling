package kr.ac.kumoh.s20130053.okky;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

// 개인별 식별 정보를 담기위해 생선한 클래스
// SharedPreferences() 클래스를 이용해 개인 식별정보를 저장 및 획득

// 사용예시1. 게시물을 읽었는지 아닌지 파악 가능
// 사용예시2. 개인별 옵션 설정 저장가능(미구현)

class Personal {
    private Context mContext;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    Personal(Context context) {
        mContext = context;
    }

    // 게시글 이미 읽었는지에 대한 정보 저장하기
    void setAlreadyRead(String address){
        pref = mContext.getSharedPreferences("AlreadyRead", MODE_PRIVATE);
        editor = pref.edit();
        /* 키 = 게시글 주소
         * 값 = 읽었는지 여부(true, false) */
        editor.putBoolean(address, true);
        editor.apply();
    }

    // 게시글 이미 읽었는지에 대한 정보 불러오기
    boolean isAlreadyRead(String address){
        pref = mContext.getSharedPreferences("AlreadyRead", MODE_PRIVATE);
        return pref.getBoolean(address, false);
    }

    // 값(ALL Data) 삭제하기
    void removeAllAlreadyRead(){
        pref = mContext.getSharedPreferences("AlreadyRead", MODE_PRIVATE);
        editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    // 특정 값(Key Data) 삭제하기
    /*
    private void removePreferences(){
        SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("hi");
        editor.apply();
    }
    */
}
