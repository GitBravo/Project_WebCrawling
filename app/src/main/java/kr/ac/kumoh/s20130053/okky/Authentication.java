package kr.ac.kumoh.s20130053.okky;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.Button;

public class Authentication {
    private Context mContext;

    Authentication(Context mContext) {
        this.mContext = mContext;
    }

    public void setLoginButtonOnClickListener() {
        NavigationView navi = ((Activity) mContext).findViewById(R.id.navigationView); // NavigationView 뷰 객체를 획득
        View header = navi.getHeaderView(0); // NavigationView 하위의 Header 뷰 객체를 획득
        Button login_btn = header.findViewById(R.id.login); // Header 하위의 Button 뷰 객체를 획득
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogForLogin customDialogForLogin = new CustomDialogForLogin(mContext);
                customDialogForLogin.show();
            }
        });
    }
}
