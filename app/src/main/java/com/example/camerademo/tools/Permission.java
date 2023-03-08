package com.example.camerademo.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class Permission {

    public static boolean askCameraPermissions(Context context, int CAMERA_PERM_CODE) {
        //檢查有沒有跟使用者要權限
        //如果使用者「同意權限」PERMISSION_GRANTED、「拒絕權限」PERMISSION_DENIED

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true ;

    }



}
