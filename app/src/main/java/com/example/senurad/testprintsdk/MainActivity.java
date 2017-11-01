package com.example.senurad.testprintsdk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button click event
        final Button btnCheck = (Button)findViewById(R.id.buttonUSBPrint);
        //btnCheck.setVisibility(View.INVISIBLE);//button will hidden untill user clicks on scan button
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(MainActivity.this, UsbPrinterActivity.class ));
                finish();
           }
        });//btn event end






        //button click event
        final Button btnCheck2 = (Button)findViewById(R.id.buttonUSBPrint2);
        //btnCheck.setVisibility(View.INVISIBLE);//button will hidden untill user clicks on scan button
        btnCheck2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(MainActivity.this, TestPrintContentActivity.class ));
                finish();
            }
        });//btn event end




    }



}
