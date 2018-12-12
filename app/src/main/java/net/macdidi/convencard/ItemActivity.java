package net.macdidi.convencard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.*;
import java.util.*;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;

import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ItemActivity extends AppCompatActivity {

    // 啟動功能用的請求代碼
    private static final int START_CAMERA = 0;
    private static final int START_COLOR = 1;
    private static final int START_CROP = 2;

    // 照片檔案名稱
    private String fileName=null;
    // bitmap檔案名稱
    private String recFileName;
    // 照片
    private ImageView picture;
    private ImageView temp;
    // 寫入外部儲存設備授權請求代碼
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 100;

    // 記事物件
    private Item item;
    private EditText title_text, content_text;
    private String test;

    //照片名稱
    private Uri imageUri;

    //商店名稱和卡號
    private EditText titleEdit;
    private EditText contentEdit;
    private TextInputLayout titleTextInput;
    private TextInputLayout contentTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        processViews();

        // 取得Intent物件
        Intent intent = getIntent();
        // 讀取Action名稱
        String action = intent.getAction();

        // 如果是修改記事
        if (action.equals("net.macdidi.convencard.EDIT_ITEM")) {
            // 接收記事物件與設定標題、內容
            item = (Item) intent.getExtras().getSerializable(
                    "Item");
            title_text.setText(item.getTitle());
            content_text.setText(item.getContent());
        }
        // 新增記事
        else {
            item = new Item();
        }


    }

    private void processViews() {
        title_text = (EditText) findViewById(R.id.title_text);
        content_text = (EditText) findViewById(R.id.content_text);

        // 取得顯示照片的ImageView元件
        picture = (ImageView) findViewById(R.id.picture);
        temp = (ImageView) findViewById(R.id.temp);

        // 取得比對有無輸入的editText元件
        titleEdit = (EditText) findViewById(R.id.title_text);
        titleTextInput = (TextInputLayout) findViewById(R.id.title_layout);
        contentEdit = (EditText) findViewById(R.id.content_text);
        contentTextInput = (TextInputLayout) findViewById(R.id.content_layout);
    }

    // 點擊確定與取消按鈕都會呼叫這個方法
    public void onSubmit(View view) {
        // 沒有輸入商店名稱或卡號
        if(view.getId() == R.id.cancel_item){
            finish();
        }
        else if(contentEdit.getText().length() <= 0) {
            contentTextInput.setErrorEnabled(true);
            contentTextInput.setError("請輸入卡號");
        }
        else if(titleEdit.getText().length() <= 0){
            titleTextInput.setErrorEnabled(true);
            titleTextInput.setError("請輸入商店名稱");

        }
        else {
            // 確定按鈕
            if (view.getId() == R.id.ok_item && titleEdit.getText().length() > 0) {

                // 讀取使用者輸入的標題與內容
                String titleText = title_text.getText().toString();
                String contentText = content_text.getText().toString();
                titleTextInput.setErrorEnabled(false);

                // 設定記事物件的標題與內容
                item.setTitle(titleText);
                item.setContent(contentText);

                // 如果是修改記事
                if (getIntent().getAction().equals(
                        "net.macdidi.convencard.EDIT_ITEM")) {
                    item.setLastModify(new Date().getTime());
                }
                // 新增記事
                else {
                    item.setDatetime(new Date().getTime());
                    // 建立SharedPreferences物件
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(this);
                    // 讀取設定的預設顏色
                    int color = sharedPreferences.getInt("DEFAULT_COLOR", -1);
                    item.setColor(getColors(color));
                }
                // 取得回傳資料用的Intent物件
                Intent result = getIntent();
                // 設定回傳的記事物件
                result.putExtra("Item", item);
                // 設定回傳的ResultCode的值，和Intent的資料
                setResult(Activity.RESULT_OK, result);

            }
            if (view.getId() == R.id.ok_item && contentEdit.getText().length() > 0){
                contentTextInput.setErrorEnabled(false);
                String contentText = content_text.getText().toString();
                item.setContent(contentText);
                item.setRecFileName(contentText);
                Bitmap a = encodeAsBitmap(item.getContent());
                picture.setVisibility(View.VISIBLE);
                picture.setImageBitmap(a);
            }
            // 結束
            finish();
        }
    }

    public void clickFunction(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.code_generation:
                contentTextInput.setErrorEnabled(false);
                String contentText = content_text.getText().toString();
                item.setContent(contentText);
                item.setRecFileName(contentText);
                Bitmap a = encodeAsBitmap(item.getContent());
                picture.setVisibility(View.VISIBLE);
                picture.setImageBitmap(a);
                break;
        }

    }


    public void processScan()
    {
        IntentIntegrator integrator = new IntentIntegrator(this);
        // ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码，QRcode
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.setPrompt("請對準卡片上的條碼"); //底部的提示文字
        integrator.setCameraId(0); //前置或後置鏡頭
        integrator.setBeepEnabled(true); //掃描成功的BIBI聲
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                content_text.setText(result.getContents());
                test=content_text.getText().toString();
                recFileName=test;
                item.setRecFileName(recFileName);
                Bitmap a=encodeAsBitmap(test);
                picture.setImageBitmap(a);
                picture.setVisibility(View.VISIBLE);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // 照像
                case START_CAMERA:
                    // 設定照片檔案名稱
                    item.setFileName(fileName);
                    if (resultCode == RESULT_OK) {
                        cropImageUri(imageUri, START_CROP);
                    }
                    fileName=null;
                    break;
                // 設定顏色
                case START_COLOR:
                    int colorId = data.getIntExtra(
                            "colorId", Colors.LIGHTGREY.parseColor());
                    item.setColor(getColors(colorId));
                    break;
                case START_CROP:
                    if (resultCode == RESULT_OK) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            temp.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }
    private void cropImageUri(Uri imageUri, int requestCode){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*"); //這裡的imageUri指向拍照後得到的原圖
        //intent.putExtra("crop", "true");
        //intent.putExtra("aspectX", 1);
        //intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 900);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        intent = Intent.createChooser(intent, "裁剪圖片");
        startActivityForResult(intent, requestCode);
    }

    public static Bitmap encodeAsBitmap(String str) {
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            //個參數解析
            result = multiFormatWriter.encode(str, BarcodeFormat.CODE_128, 800, 300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) {
            return null;
        }
        return bitmap;
    }

    public static Colors getColors(int color) {
        Colors result = Colors.LIGHTGREY;

        if (color == Colors.BLUE.parseColor()) {
            result = Colors.BLUE;
        } else if (color == Colors.PURPLE.parseColor()) {
            result = Colors.PURPLE;
        } else if (color == Colors.GREEN.parseColor()) {
            result = Colors.GREEN;
        } else if (color == Colors.ORANGE.parseColor()) {
            result = Colors.ORANGE;
        } else if (color == Colors.RED.parseColor()) {
            result = Colors.RED;
        }

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /// 如果有檔案名稱
        if (item.getFileName() != null && item.getFileName().length() > 0) {
            // 照片檔案物件
            File file = configFileName("P", ".jpg");

            // 如果照片檔案存在
            if (file.exists()) {
                // 顯示照片元件
                temp.setVisibility(View.VISIBLE);
                // 設定照片
                FileUtil.fileToImageView(file.getAbsolutePath(), temp);
            }
        }
        if (item.getRecFileName() != null && item.getRecFileName().length() > 0) {
            Bitmap a = encodeAsBitmap(item.getRecFileName());
            picture.setVisibility(View.VISIBLE);
            picture.setImageBitmap(a);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 設置要用哪個menu檔做為選單
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    // 使用者選擇所有的選單項目都會呼叫這個方法
    public void clickMenuItem(MenuItem item) {
        // 使用參數取得使用者選擇的選單項目元件編號
        int itemId = item.getItemId();

        // 判斷該執行什麼工作，目前還沒有加入需要執行的工作
        switch (itemId) {
            case R.id.take_picture:
                // 讀取與處理寫入外部儲存設備授權請求
                requestStoragePermission();
                break;
            case R.id.take_bitmap:
                processScan();
                break;
            case R.id.color_edit_item:
                // 啟動設定顏色的Activity元件
                startActivityForResult(
                        new Intent(this, ColorActivity.class), START_COLOR);
                break;
        }
    }

    private File configFileName(String prefix, String extension) {
        // 如果記事資料已經有檔案名稱
        if (item.getFileName() != null && item.getFileName().length() > 0) {
            fileName = item.getFileName();
        }
        // 產生檔案名稱
        else {
            fileName = FileUtil.getUniqueFileName();
        }

        return new File(FileUtil.getExternalStorageDir(FileUtil.APP_DIR),
                prefix + fileName + extension);
    }

    // 拍攝照片
    private void takePicture() {

        // 啟動相機元件用的Intent物件
        Intent intentCamera =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 照片檔案名稱
        File pictureFile = configFileName("P", ".jpg");
        imageUri = Uri.fromFile(pictureFile);
        // 設定檔案名稱
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // 啟動相機元件
        startActivityForResult(intentCamera, START_CAMERA);
    }

    // 讀取與處理寫入外部儲存設備授權請求
    private void requestStoragePermission() {
        // 如果裝置版本是6.0（包含）以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 取得授權狀態，參數是請求授權的名稱
            int hasPermission = checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            // 如果未授權
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                // 請求授權
                //     第一個參數是請求授權的名稱
                //     第二個參數是請求代碼
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
                return;
            }
        }

        // 如果裝置版本是6.0以下，
        // 或是裝置版本是6.0（包含）以上，使用者已經授權，
        // 拍攝照片
        takePicture();
    }

    // 覆寫請求授權後執行的方法
    // 第一個參數是請求代碼
    // 第二個參數是請求授權的名稱
    // 第三個參數是請求授權的結果，PERMISSION_GRANTED或PERMISSION_DENIED
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        // 如果是寫入外部儲存設備授權請求
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            // 如果在授權請求選擇「允許」
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 拍攝照片
                takePicture();
            }
            // 如果在授權請求選擇「拒絕」
            else {
                // 顯示沒有授權的訊息
                Toast.makeText(this, R.string.write_external_storage_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}

