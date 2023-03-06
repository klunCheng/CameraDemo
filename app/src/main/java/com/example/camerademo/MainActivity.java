package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.camerademo.databinding.ActivityMainBinding;
import com.example.camerademo.tools.Permission;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 103;
    private ContentResolver resolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        resolver = this.getContentResolver();

        /* 1.取得相機允許權限 askCameraPermissions()
        *  2.判斷是否成功開啟相機權限 onRequestPermissionsResult()
        *  3.開啟相機 openCamera()
        *  4.改寫onActivityResult 來接收回傳的影像並將收到的圖片轉成 Bitmap 格式顯示在 ImageView
        * */
    // Open Camera
        binding.btnCarema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Permission.askCameraPermissions(MainActivity.this,CAMERA_REQUEST_CODE)==false){
                    // 表示未有權限
                    // 要求使用者給予權限 (第三個參數 自定義的請求代碼)
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM_CODE);
                    //Log.e("context的permission", String.valueOf(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)));
                    //Log.e("PackageManager的permission", String.valueOf(PackageManager.PERMISSION_GRANTED));
                }
                else
                    openCamera();
            }
        });
    //open Photos
        binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToG = new Intent();
                intentToG.setType("image/*");
                intentToG.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intentToG,GALLERY_REQUEST_CODE);
            }
        });
        binding.btnToX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intenttoX = new Intent(MainActivity.this,CameraXActivity.class);
                startActivity(intenttoX);
            }
        });


    }

    /*private void askCameraPermissions() {
        //檢查有沒有跟使用者要權限
        //如果使用者「同意權限」PERMISSION_GRANTED、「拒絕權限」PERMISSION_DENIED

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //要求使用者給予權限
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM_CODE);
            Log.e("context的permission", String.valueOf(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)));
            Log.e("PackageManager的permission", String.valueOf(PackageManager.PERMISSION_GRANTED));
            //第三個參數 自定義的請求代碼
        }
        else {
            openCamera();
        }

    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case CAMERA_PERM_CODE:
                //如果允許了(用int值檢查)
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                    //Log.e("requestCode", String.valueOf(requestCode));
                    Log.e("grantResults", String.valueOf(grantResults[0]));
                    Log.e("PackageManager的permission", String.valueOf(PackageManager.PERMISSION_GRANTED));
                }
                //如果拒絕了
                else
                    Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show();
            break;

        }

    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);

    }

    @Override
    //requestCode的意思是接收從哪個跳轉頁面的指令發出的訊息
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("requestCode", String.valueOf(requestCode));
        Log.w("resultCode", String.valueOf(resultCode));
        Log.w("resultOK", String.valueOf(RESULT_OK));
        switch(requestCode){

            case CAMERA_REQUEST_CODE : //102

                if(resultCode == RESULT_OK){
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    binding.imagePhoto.setImageBitmap(image);
                }
                else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(MainActivity.this, "您取消了照相機", Toast.LENGTH_SHORT).show();
                }
            break;

            case GALLERY_REQUEST_CODE: //103
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();  //取得相片路徑
                    try {
                        //將該路徑的圖片轉成bitmap
                        Bitmap imageFromG = BitmapFactory.decodeStream(resolver.openInputStream(uri));
                        binding.imagePhoto.setImageBitmap(imageFromG);

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                }

            break;
        }

    }
}