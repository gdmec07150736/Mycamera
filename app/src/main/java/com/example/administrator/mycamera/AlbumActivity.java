package com.example.administrator.mycamera;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.Vector;

public class AlbumActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private Bitmap[] bitmap;
    private long starttime=0;
    private SensorManager sm;
    private SensorEventListener seventl;
    private View addImage(Bitmap bitmap){
        ImageView img=new ImageView(this);
        img.setImageBitmap(bitmap);
        return img;
    }
    public Bitmap loadImage(String path){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        Bitmap bitmap=BitmapFactory.decodeFile(path,options);
        WindowManager manager=getWindowManager();
        Display display=manager.getDefaultDisplay();
        int screenwidth=display.getWidth();
        options.inSampleSize=options.outWidth/screenwidth;
        options.inJustDecodeBounds=false;
        bitmap=BitmapFactory.decodeFile(path,options);
        return bitmap;
    }

    public String[] loadAlbum(){
        String path=android.os.Environment.getExternalStorageDirectory().getPath()+"/mycamera";
        File f=new File(path);
        Vector<Bitmap> filename=new Vector<Bitmap>();
        if(f.exists()&&f.isDirectory()){
            String[] str=f.list();
            for(String s:str){
                if(new File(path+"/"+s).isFile()){
                    filename.addElement(loadImage(path+"/"+s));
                }
            }
            bitmap=filename.toArray(new Bitmap[]{});
        }
        return null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        viewFlipper=(ViewFlipper)this.findViewById(R.id.viewf1);
        loadAlbum();
        if(bitmap==null){
            Toast.makeText(this,"没有图片",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }else{
            for(int i=0;i<=bitmap.length-1;i++){
                viewFlipper.addView(addImage(bitmap[i]),i,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
        sm=(SensorManager) this.getSystemService(SENSOR_SERVICE);
        final Sensor s=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        seventl=new SensorEventListener() {
            public void onSensorChanged(SensorEvent event){
                float x=event.values[SensorManager.DATA_X];
                if(x>10&&System.currentTimeMillis()>starttime+1000){
                    starttime=System.currentTimeMillis();
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this,R.anim.push_right_in));
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this,R.anim.push_right_out));
                    viewFlipper.showPrevious();
                }else if(x<-10&&System.currentTimeMillis()>starttime+1000){
                    starttime=System.currentTimeMillis();
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this,R.anim.push_left_in));
                    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(AlbumActivity.this,R.anim.push_left_out));
                    viewFlipper.showNext();
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sm.registerListener(seventl,s,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sm.unregisterListener(seventl);
    }
}
