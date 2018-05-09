package com.cheater.flashlight;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private Camera.Parameters mParameters;
    private ToggleButton flashlightTB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashlightTB = findViewById(R.id.flashlightTB);

        flashlightTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    flashlightTB.setBackgroundResource(R.drawable.control_on);
                    try {
                        openFlashLight();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    flashlightTB.setBackgroundResource(R.drawable.control_off);
                    closeFlashLight();
                }
            }
        });
    }

    private void openFlashLight() throws IOException {
        if (isFlashSupported()){
            mCamera=Camera.open();
            int textureId = 0;
            mCamera.setPreviewTexture(new SurfaceTexture(textureId));
            mCamera.startPreview();
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParameters);
        }else {
            Toast.makeText(MainActivity.this,"您的设备不支持手电筒!",Toast.LENGTH_SHORT).show();
        }
    }

    private void closeFlashLight() {
        if (mCamera != null) {
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private int MY_PERMISSION_REQUEST_CODE = 3000;
    public void checkAndRequestPermissions() throws IOException {
        boolean isAllGranted = checkPermissionsAllGranted(
                new String[]{
                        Manifest.permission.CAMERA
                });

        if (!isAllGranted){
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[] {
                            Manifest.permission.CAMERA
                    },
                    MY_PERMISSION_REQUEST_CODE
            );
        }else {
            openFlashLight();
        }
    }

    private boolean checkPermissionsAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    private boolean isFlashSupported() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode==MY_PERMISSION_REQUEST_CODE){
            boolean isAllGranted=true;

            for (int grant : grantResults){
                if (grant!=PackageManager.PERMISSION_GRANTED){
                    isAllGranted=false;
                    break;
                }
            }

            if (!isAllGranted){
                //权限获取失败，提示并退出应用
                Toast.makeText(MainActivity.this,"无法获取相机权限，无法使用，请手动设置权限！",Toast.LENGTH_SHORT).show();
                finish();
            }else {
                //拿到权限，打开手电筒
                try {
                    openFlashLight();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //权限检查及申请
        try {
            checkAndRequestPermissions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            openFlashLight();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeFlashLight();
    }
}
