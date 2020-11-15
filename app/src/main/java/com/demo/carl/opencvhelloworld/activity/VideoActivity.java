package com.demo.carl.opencvhelloworld.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.carl.opencvhelloworld.Camera_X;
import com.demo.carl.opencvhelloworld.OpencvUtils;
import com.demo.carl.opencvhelloworld.Overlap;
import com.demo.carl.opencvhelloworld.R;
import com.demo.carl.opencvhelloworld.Result;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class VideoActivity extends AppCompatActivity implements Camera_X.UpDataUiListen {
    private PreviewView pvCamera;
    private String rootPath;
    private String imaPath;
    private Overlap overlap;
    private Bitmap tpl;
    private Bitmap tpl_scaled;
    private String TAG="Opencv--";
    private ImageView imageView;
    private ImageView ivCut;
    private TextView tvScore;
    private Bitmap mOrgBitmap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen();
        setContentView(R.layout.activity_camera);
        requestPermission();
        initOpenCV();
        pvCamera = findViewById(R.id.pvCamera);
        imageView = findViewById(R.id.ivImage);
        ivCut = findViewById(R.id.ivCut);
        overlap = findViewById(R.id.overlap);
        tvScore = findViewById(R.id.tvScore);
        final Camera_X camera_x = new Camera_X(this,pvCamera,
                androidx.core.content.ContextCompat.getMainExecutor(VideoActivity.this));
        camera_x.setUpDataUiListen(this);

        tpl = BitmapFactory.decodeResource(getResources(), R.drawable.tpl01);
        log("tpl.w:"+tpl.getWidth()+",tpl.h:"+tpl.getHeight());
        float scale = 0.5f;
        tpl_scaled = scaleBitmap(tpl,(int)(tpl.getWidth()*scale),(int)(tpl.getHeight()*scale));
        Point start = new Point(200,500);
        overlap.setMaskBox(new RectF((float) start.x,(float) start.y,(float) (start.x+1900),
                (float)(start.y+tpl_scaled.getHeight()*2)));
        log("tpl.w:"+tpl_scaled.getWidth()+",tpl.h:"+tpl_scaled.getHeight());
        pvCamera.post(new Runnable() {
            @Override
            public void run() {

                camera_x.startCamera();
            }
        });

//        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.temp);
//        RectF boundingBox = OpencvUtils.templateMatch(tpl,bitmap2);
//        log("boundingBox:"+boundingBox);
//        overlap.drawBox(boundingBox);
    }
    private void fullScreen(){
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }
    public void requestPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // 申请 相机 麦克风权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 100);
        }
    }

    @Override
    public void MatchFinis(RectF boundingBox) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //overlap.drawBox(boundingBox);
            }
        });
    }
    private boolean isMatching = false;
    @Override
    public void readBitmap(Bitmap bitmap) {
        mOrgBitmap = bitmap;
        new Thread(new MatchRunnable(bitmap)).start();
//        runOnUiThread(new Runnable() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void run() {
//
//
//            }
//        });

    }

    private void initOpenCV() {
        Log.d(TAG, "into resume");
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    //openCV4Android 需要加载用到
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    private RectF cameraBox2WinBox(int win_w,int win_h,RectF box){
        int camera_w=1280;
        int camera_h=720;
        float win_top=box.top/camera_h*win_h;
        float win_bottom = box.bottom/camera_h*win_h;
        float win_left = box.left/camera_w*win_w;
        float win_right = box.right/camera_w*win_w;
        return new RectF(win_left,win_top,win_right,win_bottom);
    }

    private class MatchRunnable implements Runnable{
        Bitmap mBitmap;

        public MatchRunnable(Bitmap bitmap) {
            this.mBitmap = bitmap;
        }

        @Override
        public void run() {
            if (!isMatching){
                isMatching = true;
                //RectF box= OpencvUtils.templateMatch(tpl_scaled,mBitmap);

                //overlap.drawBox(box);
                //OpencvUtils.comPareHist(cut,tpl_scaled);
                //imageView.setImageBitmap(mBitmap);
                long startTime = System.nanoTime();
                Result r= OpencvUtils.templateMatch2(tpl_scaled, mBitmap);
                long consumingTime = System.nanoTime() - startTime;
                log("consumingTime:"+consumingTime/1000000+"ms");
                Message msg = new Message();
                msg.obj = r;
                myHandle.sendMessage(msg);


                isMatching = false;
            }
        }
    }
    private Handler myHandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Result r = (Result) msg.obj;
            RectF winBox=null;
            if (r!=null){
                Bitmap cut = Bitmap.createBitmap(mOrgBitmap, (int) r.box.left, (int) r.box.top, tpl_scaled.getWidth(), tpl_scaled.getHeight());
                ivCut.setImageBitmap(cut);
                tvScore.setText("score:"+r.score);
                winBox=cameraBox2WinBox(pvCamera.getWidth(),pvCamera.getHeight(),r.box);

            }else {
                Bitmap cut = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
                ivCut.setImageBitmap(cut);
                tvScore.setText("null");
            }

            overlap.drawBox(winBox);
            return false;
        }
    });
    private void log(String str){
        Log.i("carl",str);
    }
}
