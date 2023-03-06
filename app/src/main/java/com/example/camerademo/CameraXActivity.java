package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.camerademo.databinding.ActivityCameraXactivityBinding;
import com.example.camerademo.tools.Permission;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraXActivity extends AppCompatActivity {

    ActivityCameraXactivityBinding binding ;
    static final int REQUEST_CODE_PERMISSION = 100 ;
    static final int CAMERA_PERM_CODE = 101 ;
    private final String[] REQUEST_PERMISSION = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private Executor executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityCameraXactivityBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        // Request camera permissions
        if(Permission.askCameraPermissions(CameraXActivity.this,REQUEST_CODE_PERMISSION) == false)
            // 表示未有權限
            // 要求使用者給予權限 (第三個參數 自定義的請求代碼)
            ActivityCompat.requestPermissions(CameraXActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM_CODE);
        else
            openCameraX();



        binding.btncapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });





    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //executor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case CAMERA_PERM_CODE:
                if(Permission.askCameraPermissions(CameraXActivity.this,REQUEST_CODE_PERMISSION))
                    openCameraX();
                else
                    Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show();
            break;
        }
    }

    private void openCameraX(){
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
                //ListenableFuture 表示異步計算的結果：可能已經或可能尚未完成產生結果的計算。
                //   它是一種類型Future ，允許您註冊回調，以便在計算完成後立即執行，或者如果計算已經完成，則立即執行
                //ProcessCameraProvider.getInstance(this):獲取相機提供者。
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(ProcessCameraProvider cameraProvider){
        //創建預覽
        Preview preview = new Preview.Builder().build();
        //鏡頭方向(Valid values for lens facing are LENS_FACING_FRONT前鏡頭, LENS_FACING_BACK後鏡頭)
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        //圖像分析
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
        //創建拍照
        ImageCapture.Builder builder = new ImageCapture.Builder();
        @SuppressLint("WrongConstant")
        final ImageCapture imageCapture = builder.setTargetAspectRatio(this.getWindowManager().getDefaultDisplay().getRotation()).build();
        //設定預覽
        preview.setSurfaceProvider(binding.cameraPv.getSurfaceProvider());
        //指定一個生命週期來關聯相機，通知 CameraX 何時配置相機捕獲會話並確保相機狀態適當更改以匹配生命週期轉換。
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this,cameraSelector,preview,imageAnalysis, imageCapture);



    }




}