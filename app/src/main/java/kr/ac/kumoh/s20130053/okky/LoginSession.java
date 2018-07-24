package kr.ac.kumoh.s20130053.okky;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;

public class LoginSession {
    private Map<String,String> cookies;
    public void Login(final String id, final String pw){
        new Thread(){
            @Override
            public void run() {
                // 로그인창 주소 https://okky.kr/login/auth?redirectUrl=%2F
                try{
                    // 로그인 정보를 서버로 전송
                    Connection.Response res = Jsoup.connect("https://okky.kr/j_spring_security_check")
                            .data("redirectUrl", "%2F",
                                    "j_username", id,
                                    "j_password", pw,
                                    "user_otp", "")
                            .method(Connection.Method.POST)
                            .timeout(5000)
                            .execute();

                    // 로그인 쿠키 획득
                    cookies = res.cookies();
                }catch (java.io.IOException e){
                    e.printStackTrace();
                    Log.d("실패", "로그인 정보 오류");
                }
            }
        }.start();
    }

    public Map<String, String> getCookies() {
        return cookies;
    }
}
