package kr.ac.kumoh.s20130053.okky;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class detail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        TextView textView = findViewById(R.id.textDetail);
        textView.setText("핡");
    }
}
