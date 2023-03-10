package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
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

public class CameraXActivity extends AppCompatActivity implements View.OnClickListener, ImageAnalysis.Analyzer{

    ActivityCameraXactivityBinding binding ;
    static final int REQUEST_CODE_PERMISSION = 100 ;
    static final int CAMERA_PERM_CODE = 101 ;
    private final String[] REQUEST_PERMISSION = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private Executor executor = Executors.newSingleThreadExecutor();

    ListenableFuture<ProcessCameraProvider> cameraProviderFuture ;
    Preview preview;
    ImageCapture imageCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityCameraXactivityBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        // Request camera permissions
        if(Permission.askCameraPermissions(CameraXActivity.this,REQUEST_CODE_PERMISSION) == false)
            // ??????????????????
            // ??????????????????????????? (??????????????? ????????????????????????)
            ActivityCompat.requestPermissions(CameraXActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM_CODE);
        else
            openCameraX();

        binding.btnCapture.setOnClickListener(this);
        binding.btnVedio.setOnClickListener(this);




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    //executor.shutdown();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode) {
         case CAMERA_PERM_CODE:
             if (Permission.askCameraPermissions(CameraXActivity.this, REQUEST_CODE_PERMISSION))
                 openCameraX();
             else
                Toast.makeText(this, "Camera permission is required to use camera", Toast.LENGTH_SHORT).show();
             break;
          }
      }

    private void openCameraX() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
                    //ListenableFuture ???????????????????????????????????????????????????????????????????????????????????????
                    //   ??????????????????Future ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    //ProcessCameraProvider.getInstance(this):????????????????????????
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
         }, getExecutor());
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("WrongConstant")
    void bindPreview(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        //????????????(Valid values for lens facing are LENS_FACING_FRONT?????????, LENS_FACING_BACK?????????)
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        //????????????
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
        //????????????
        ImageCapture.Builder builder = new ImageCapture.Builder();
        imageCapture = builder.setTargetAspectRatio(this.getWindowManager().getDefaultDisplay().getRotation()).build();

        //????????????
        preview = new Preview.Builder().build();
        //??????UI??????
        preview.setSurfaceProvider(binding.cameraPv.getSurfaceProvider());
        //???????????????????????????????????????????????? CameraX ?????????????????????????????????????????????????????????????????????????????????????????????
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_capture:
                Log.w("msg:","????????????");
                /*???????????????????????????????????????
                File photoDir = new File("/mnt/sdcard/Pictures/CameraXPhotos");
                //?????????????????????????????????????????????
                if(!photoDir.exists())
                    photoDir.mkdir();*/
                // ???media store API????????????
                //???????????????????????????
                long timeStamp = System.currentTimeMillis();

                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,timeStamp);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

                //ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(PhotoFilePath).build();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    imageCapture.takePicture(
                            new ImageCapture.OutputFileOptions.Builder(
                                getContentResolver(),
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                contentValues
                            ).build(),
                            getMainExecutor(),
                            new ImageCapture.OnImageSavedCallback ()
                            {
                                @Override
                                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                    new Handler(Looper.getMainLooper()) {
                                        @Override
                                        public void handleMessage(@NonNull Message msg) {
                                            super.handleMessage(msg);
                                            Toast.makeText(CameraXActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    };
                                    Log.w("save image:","Image Saved successfully");
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException exception) {
                                    new Handler(Looper.getMainLooper()){
                                        @Override
                                        public void handleMessage(@NonNull Message msg) {
                                            super.handleMessage(msg);
                                            Toast.makeText(CameraXActivity.this, "Error to Save Image", Toast.LENGTH_SHORT).show();
                                            ;}
                                    };
                                    Log.w("Error:",exception.toString());
                                }
                            });
                }
                break;
            case R.id.btn_vedio:
                Log.w("mesg:","????????????");
                break;


        }
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        // Image processing here for the current frame



    }
}