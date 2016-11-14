package com.cadaloco.sunshine.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cadaloco.sunshine.LogUtil;
import com.cadaloco.sunshine.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogUtil.logMethodCalled();
        LogUtil.v("test v");
        LogUtil.d("test d");
        LogUtil.i("test i");
        LogUtil.w("test w");
        LogUtil.e("test e");
    }
}
