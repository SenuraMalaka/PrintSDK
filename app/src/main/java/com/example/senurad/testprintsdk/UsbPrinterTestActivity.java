package com.example.senurad.testprintsdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.printer.UsbThermalPrinter;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;


public class UsbPrinterTestActivity {


    private String printVersion;
    private final int NOPAPER = 3;
    private final int LOWBATTERY = 4;
    private final int PRINTVERSION = 5;
    private final int PRINTBARCODE = 6;
    private final int PRINTQRCODE = 7;
    private final int PRINTPAPERWALK = 8;
    private final int PRINTCONTENT = 9;
    private final int CANCELPROMPT = 10;
    private final int PRINTERR = 11;
    private final int OVERHEAT = 12;
    private final int MAKER = 13;
    private final int PRINTPICTURE = 14;
    private final int NOBLACKBLOCK = 15;

    MyHandler handler;

    private String Result;
    private Boolean nopaper = false;
    private boolean LowBattery = false;

    public static String barcodeStr;
    public static String qrcodeStr;
    public static int paperWalk;
    public static String printContent;
    private int leftDistance = 0;
    private int lineDistance;
    private int wordFont;
    private int printGray;
    public ProgressDialog progressDialog;
    private final static int MAX_LEFT_DISTANCE = 255;
    ProgressDialog dialog;
    UsbThermalPrinter mUsbThermalPrinter = null;
    private String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/111.bmp";

    private TestPrintContentActivity thisActivity =null;


    private static final String TAG = "USBPrintACT";


    public void setActivity(TestPrintContentActivity act){
        thisActivity =act;
        new UsbThermalPrinter(thisActivity);
    }



    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {


            switch (msg.what) {
                case NOPAPER:
                    noPaperDlg();
                    break;
                case LOWBATTERY:
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(thisActivity);
                    alertDialog.setTitle(R.string.operation_result);
                    alertDialog.setMessage(thisActivity.getString(R.string.LowBattery));
                    alertDialog.setPositiveButton(thisActivity.getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    alertDialog.show();
                    break;
                case NOBLACKBLOCK:
                    Toast.makeText(thisActivity, R.string.maker_not_find, Toast.LENGTH_SHORT).show();
                    break;
                case PRINTVERSION:
                    dialog.dismiss();
                    if (msg.obj.equals("1")) {
                        Toast.makeText(thisActivity, "Print ver : "+printVersion, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(thisActivity, R.string.operation_fail, Toast.LENGTH_LONG).show();
                    }
                    break;
                case PRINTBARCODE:
                    new barcodePrintThread().start();
                    break;
                case PRINTQRCODE:
                    new qrcodePrintThread().start();
                    break;
                case PRINTPAPERWALK:
                    new paperWalkPrintThread().start();
                    break;
                case PRINTCONTENT:
                    new contentPrintThread().start();
                    break;
                case MAKER:
                    new MakerThread().start();
                    break;
                case PRINTPICTURE:
                    new printPicture().start();
                    break;
                case CANCELPROMPT:
                    if (progressDialog != null && !thisActivity.isFinishing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    break;
                case OVERHEAT:
                    AlertDialog.Builder overHeatDialog = new AlertDialog.Builder(thisActivity);
                    overHeatDialog.setTitle(R.string.operation_result);
                    overHeatDialog.setMessage(thisActivity.getString(R.string.overTemp));
                    overHeatDialog.setPositiveButton(thisActivity.getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    overHeatDialog.show();
                    break;
                default:
                    Toast.makeText(thisActivity, "Print Error!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }






    private void printQRCodeNow(){

        String exditText = "1";
        if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal()){
            exditText="5";
        }

        if (exditText == null || exditText.length() < 1) {
            return;
        }
        printGray = Integer.parseInt(exditText);
        if (printGray < 0 || printGray > 7) {
            return;
        }
        qrcodeStr = "sample qr";
        if (qrcodeStr == null || qrcodeStr.length() == 0) {
            return;
        }
        if (LowBattery == true) {
            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
        } else {
            if (!nopaper) {
                progressDialog = ProgressDialog.show(thisActivity, thisActivity.getString(R.string.D_barcode_loading), thisActivity.getString(R.string.generate_barcode_wait));
                handler.sendMessage(handler.obtainMessage(PRINTQRCODE, 1, 0, null));
            } else {
                Toast.makeText(thisActivity, thisActivity.getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
            }
        }
    }




    public void printBarcodeNow(){
        String exditText = "1";
        if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal()){
            exditText="5";
        }
        if (exditText == null || exditText.length() < 1) {
            return;
        }
        printGray = Integer.parseInt(exditText);
        if (printGray < 0 || printGray > 7) {
            return;
        }
        barcodeStr = "12345";
        if (barcodeStr == null || barcodeStr.length() == 0) {
             return;
        }
        if (LowBattery == true) {
            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
        } else {
            if (!nopaper) {
                progressDialog = ProgressDialog.show(thisActivity, thisActivity.getString(R.string.bl_dy), thisActivity.getString(R.string.printing_wait));
                handler.sendMessage(handler.obtainMessage(PRINTBARCODE, 1, 0, null));
            } else {
                Toast.makeText(thisActivity, thisActivity.getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
            }
        }
    }




    public void printContentNow(){

        String exditText;
        //left margin
        exditText = "0";
        if (exditText == null || exditText.length() < 1) {
            return;
        }
        leftDistance = 0;//Integer.parseInt(exditText);

        //row space
        exditText = "0";
        if (exditText == null || exditText.length() < 1) {
            return;
        }
        lineDistance = 0;//Integer.parseInt(exditText);

        printContent = "Telpo Print Worked 2";

        exditText = "2";
        if (exditText == null || exditText.length() < 1) {
             return;
        }
        wordFont = 2;//Integer.parseInt(exditText);

        exditText = "1";
        if (exditText == null || exditText.length() < 1) {
             return;
        }
        printGray = 1;//Integer.parseInt(exditText);

        if (leftDistance > MAX_LEFT_DISTANCE) {
            return;
        }
        if (lineDistance > 255) {
            return;
        }
        if (wordFont > 4 || wordFont < 1) {
            return;
        }
        if (printGray < 0 || printGray > 7) {
            return;
        }
        if (printContent == null || printContent.length() == 0) {
            return;
        }
        if (LowBattery == true) {
            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
        } else {
            if (!nopaper) {
                progressDialog = ProgressDialog.show(thisActivity, thisActivity.getString(R.string.bl_dy), thisActivity.getString(R.string.printing_wait));
                handler.sendMessage(handler.obtainMessage(PRINTCONTENT, 1, 0, null));
            } else {
                Toast.makeText(thisActivity, thisActivity.getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
            }
        }

    }




    public void printPictureNow(){

        String exditText = "1";
        if (exditText == null || exditText.length() < 1) {
             return;
        }
        printGray = 1;
        if (printGray < 0 || printGray > 7) {
            return;
        }
        if (LowBattery == true) {
            handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
        } else {
            if (!nopaper) {
                progressDialog = ProgressDialog.show(thisActivity, thisActivity.getString(R.string.bl_dy), thisActivity.getString(R.string.printing_wait));
                handler.sendMessage(handler.obtainMessage(PRINTPICTURE, 1, 0, null));
            } else {
                Toast.makeText(thisActivity, thisActivity.getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
            }
        }

    }





    public void UsbPrinterTestActivityStart(TestPrintContentActivity passedActivity) {

        setActivity(passedActivity);
        savepic();
        handler = new MyHandler();

        IntentFilter pIntentFilter = new IntentFilter();
        pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        pIntentFilter.addAction("android.intent.action.BATTERY_CAPACITY_EVENT");
        thisActivity.registerReceiver(printReceive, pIntentFilter);



        dialog = new ProgressDialog(thisActivity);
        dialog.setTitle(R.string.idcard_czz);
        dialog.setMessage(thisActivity.getText(R.string.watting));
        dialog.setCancelable(false);
        dialog.show();

        mUsbThermalPrinter= new UsbThermalPrinter(thisActivity);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mUsbThermalPrinter.start(0);
                    mUsbThermalPrinter.reset();
                    printVersion = mUsbThermalPrinter.getVersion();
                } catch (TelpoException e) {
                    e.printStackTrace();
                } finally {
                    if (printVersion != null) {
                        Message message = new Message();
                        message.what = PRINTVERSION;
                        message.obj = "1";
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = PRINTVERSION;
                        message.obj = "0";
                        handler.sendMessage(message);
                    }
                }
            }
        }).start();

    }




    public final BroadcastReceiver printReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                //TPS390 can not print,while in low battery,whether is charging or not charging
                if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390.ordinal()){
                    if (level * 5 <= scale) {
                        LowBattery = true;
                    } else {
                        LowBattery = false;
                    }
                }else {
                    if (status != BatteryManager.BATTERY_STATUS_CHARGING) {
                        if (level * 5 <= scale) {
                            LowBattery = true;
                        } else {
                            LowBattery = false;
                        }
                    } else {
                        LowBattery = false;
                    }
                }
            }
            //Only use for TPS550MTK devices
            else if (action.equals("android.intent.action.BATTERY_CAPACITY_EVENT")) {
                int status = intent.getIntExtra("action", 0);
                int level = intent.getIntExtra("level", 0);
                if(status == 0){
                    if(level < 1){
                        LowBattery = true;
                    }else {
                        LowBattery = false;
                    }
                }else {
                    LowBattery = false;
                }
            }
        }
    };

    private void noPaperDlg() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(thisActivity);
        dlg.setTitle(thisActivity.getString(R.string.noPaper));
        dlg.setMessage(thisActivity.getString(R.string.noPaperNotice));
        dlg.setCancelable(false);
        dlg.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dlg.show();
    }

    private class paperWalkPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.walkPaper(paperWalk);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper){
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class barcodePrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setGray(printGray);
                Bitmap bitmap = CreateCode(barcodeStr, BarcodeFormat.CODE_128, 320, 176);
                if(bitmap != null){
                    mUsbThermalPrinter.printLogo(bitmap,true);
                }
                mUsbThermalPrinter.addString(barcodeStr);
                mUsbThermalPrinter.printString();
                mUsbThermalPrinter.walkPaper(20);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper){
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class qrcodePrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setGray(printGray);
                Bitmap bitmap = CreateCode(qrcodeStr, BarcodeFormat.QR_CODE, 256, 256);
                if(bitmap != null){
                    mUsbThermalPrinter.printLogo(bitmap, true);
                }
                mUsbThermalPrinter.addString(qrcodeStr);
                mUsbThermalPrinter.printString();
                mUsbThermalPrinter.walkPaper(20);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper){
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class contentPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                mUsbThermalPrinter.setLeftIndent(leftDistance);
                mUsbThermalPrinter.setLineSpace(lineDistance);
                if (wordFont == 4) {
                    mUsbThermalPrinter.setFontSize(2);
                    mUsbThermalPrinter.enlargeFontSize(2, 2);
                } else if (wordFont == 3) {
                    mUsbThermalPrinter.setFontSize(1);
                    mUsbThermalPrinter.enlargeFontSize(2, 2);
                } else if (wordFont == 2) {
                    mUsbThermalPrinter.setFontSize(2);
                } else if (wordFont == 1) {
                    mUsbThermalPrinter.setFontSize(1);
                }
                mUsbThermalPrinter.setGray(printGray);
                mUsbThermalPrinter.addString(printContent);
                mUsbThermalPrinter.printString();
                mUsbThermalPrinter.walkPaper(20);
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper){
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class MakerThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.searchMark(Integer.parseInt("200"),//marker search
                        Integer.parseInt("50"));//marker walk
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else if (Result.equals("com.telpo.tps550.api.printer.BlackBlockNotFoundException")) {
                    handler.sendMessage(handler.obtainMessage(NOBLACKBLOCK, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper){
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }

    private class printPicture extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                mUsbThermalPrinter.reset();
                mUsbThermalPrinter.setGray(printGray);
                mUsbThermalPrinter.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                File file = new File(picturePath);
                if (file.exists()) {
                    mUsbThermalPrinter.printLogo(BitmapFactory.decodeFile(picturePath),false);
                    mUsbThermalPrinter.walkPaper(20);
                } else {
                    thisActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(thisActivity, thisActivity.getString(R.string.not_find_picture), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Result = e.toString();
                if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
                    nopaper = true;
                } else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
                    handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
                } else {
                    handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
                }
            } finally {
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
                if (nopaper){
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
                    nopaper = false;
                    return;
                }
            }
        }
    }


    public Bitmap CreateCode(String str, com.google.zxing.BarcodeFormat type, int bmpWidth, int bmpHeight) throws WriterException {
        Hashtable<EncodeHintType,String> mHashtable = new Hashtable<EncodeHintType,String>();
        mHashtable.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix matrix = new MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight, mHashtable);
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    private void savepic() {
        File file = new File(picturePath);
        if (!file.exists()) {
            InputStream inputStream = null;
            FileOutputStream fos = null;
            byte[] tmp = new byte[1024];
            try {
                inputStream = thisActivity.getApplicationContext().getAssets().open("syhlogo.png");
                fos = new FileOutputStream(file);
                int length = 0;
                while((length = inputStream.read(tmp)) > 0){
                    fos.write(tmp, 0, length);
                }
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
