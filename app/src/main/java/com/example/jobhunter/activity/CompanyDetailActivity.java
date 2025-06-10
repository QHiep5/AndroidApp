package com.example.jobhunter.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobhunter.R;

public class CompanyDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_detail);
        long companyId = getIntent().getLongExtra("company_id", -1);
        String companyName = getIntent().getStringExtra("company_name");
        TextView tv = new TextView(this);
        tv.setText("ID: " + companyId + "\nTên: " + companyName);
        tv.setTextSize(20);
        setContentView(tv);
    }
}
