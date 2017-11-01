package com.example.senurad.testprintsdk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class TestPrintContentActivity extends AppCompatActivity {


    private UsbPrinterTestActivity printerActv=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_print_content);
        printerActv=new UsbPrinterTestActivity();
        printerActv.UsbPrinterTestActivityStart(TestPrintContentActivity.this);


        //button click event
        final Button btnPrintContent = (Button)findViewById(R.id.buttonPrintContent);
        //btnCheck.setVisibility(View.INVISIBLE);//button will hidden untill user clicks on scan button
        btnPrintContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printerActv.printContentNow();
            }
        });//btn event end




        //button click event
        final Button btnPrintImage = (Button)findViewById(R.id.buttonPrintImage);
        //btnCheck.setVisibility(View.INVISIBLE);//button will hidden untill user clicks on scan button
        btnPrintImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printerActv.printPictureNow();
            }
        });//btn event end

    }







    @Override
    protected void onDestroy() {
        if (printerActv.progressDialog != null && !TestPrintContentActivity.this.isFinishing()) {
            printerActv.progressDialog.dismiss();
            printerActv.progressDialog = null;
        }
        unregisterReceiver(printerActv.printReceive);
        printerActv.mUsbThermalPrinter.stop();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }




}
