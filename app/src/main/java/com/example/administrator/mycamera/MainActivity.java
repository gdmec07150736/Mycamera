package com.example.administrator.mycamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SurfaceHolder.Callback{
    private SurfaceView surv;
    private ImageView imgv;
    private SurfaceHolder surhold;
    private ImageView shutter;
    private Camera mcarma=null;
    private boolean mprerun;
    private static final int menustart=1;
    private static final int menusensor=2;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        surv=(SurfaceView) findViewById(R.id.camera);
        imgv=(ImageView) findViewById(R.id.img);
        shutter=(ImageView) findViewById(R.id.shut);
        shutter.setOnClickListener(this);
        imgv.setVisibility(View.GONE);
        surhold.addCallback(this);
        surhold.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setCameraParams();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try{
            if(mprerun){
                mcarma.stopPreview();
            }
            mcarma.setPreviewDisplay(holder);
            mcarma.startPreview();
            mprerun=true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mcarma!=null){
            mcarma.stopPreview();
            mprerun=false;
            mcarma.release();
            mcarma=null;
        }
    }

    @Override
    public void onClick(View v) {
        if(mprerun){
            shutter.setEnabled(false);
            mcarma.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mcarma.takePicture(shutcallback,null,piccallback);
                }
            });
        }
    }
    Camera.PictureCallback piccallback=new Camera.PictureCallback(){
        public void onPictureTaken(byte[] data,Camera camera){
            if(data!=null){
                saveandshow(data);
            }
        }
    };
    Camera.ShutterCallback shutcallback= new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            System.out.println("快照回调...");
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,menustart,0,"重拍");
        menu.add(0,menusensor,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==menustart){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if(item.getItemId()==menusensor){
            Intent in=new Intent(this,AlbumActivity.class);
            startActivity(in);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setCameraParams(){
        if(mcarma!=null){
            return;
        }
        mcarma=Camera.open();
        Camera.Parameters para=mcarma.getParameters();
        para.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        para.setPreviewFrameRate(3);
        para.setPreviewFormat(PixelFormat.YCbCr_422_SP);
        para.set("jpeg-quality",85);
        List<Camera.Size> l=para.getSupportedPictureSizes();
        Camera.Size s=l.get(0);
        int w=s.width;
        int h=s.height;
        para.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }
    public void saveandshow(byte[] data){
        try{
            String imgid=System.currentTimeMillis()+"";
            String path=android.os.Environment.getExternalStorageDirectory().getPath()+"/mycamera";
            File f=new File(path);
            if(!f.exists()){
                f.mkdirs();
            }
            path+="/"+imgid+".jpeg";
            f=new File(path);
            if(!f.exists()){
                f.createNewFile();
            }
            FileOutputStream fos=new FileOutputStream(f);
            fos.write(data);
            fos.close();
            AlbumActivity album=new AlbumActivity();
            bitmap=album.loadImage(path);
            imgv.setImageBitmap(bitmap);
            imgv.setVisibility(View.VISIBLE);
            surv.setVisibility(View.GONE);
            if(mprerun){
                mcarma.stopPreview();
                mprerun=false;
            }
            shutter.setEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
