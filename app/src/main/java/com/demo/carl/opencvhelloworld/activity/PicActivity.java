package com.demo.carl.opencvhelloworld.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.demo.carl.opencvhelloworld.OpencvUtils;
import com.demo.carl.opencvhelloworld.Overlap;
import com.demo.carl.opencvhelloworld.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import androidx.appcompat.app.AppCompatActivity;

public class PicActivity extends AppCompatActivity {

    private String TAG="Opencv--";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOpenCV();
        ImageView imageView=findViewById(R.id.imageView);
        ImageView ivTpl = findViewById(R.id.ivTpl);
        ImageView ivCut = findViewById(R.id.ivCut);
        Overlap overlap=findViewById(R.id.overlap);
        Bitmap org_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.desk);
        Bitmap tpl = BitmapFactory.decodeResource(getResources(), R.drawable.tpl4);

        ivTpl.setImageBitmap(tpl);
        log("tpl.getWidth():"+tpl.getWidth());
        log("tpl.getHeight():"+tpl.getHeight());
        log("bitmap.getWidth():"+org_bitmap.getWidth());
        log("bitmap.getHeight():"+org_bitmap.getHeight());
//        Bitmap tpl2 = ((BitmapDrawable)getResources().getDrawable( R.drawable.tpl00)).getBitmap();
////        log("tpl2.getWidth():"+tpl2.getWidth());
////        log("tpl2.getHeight():"+tpl2.getHeight());
        RectF box = OpencvUtils.templateMatch(tpl,org_bitmap);
        Bitmap cut = Bitmap.createBitmap(org_bitmap, (int) box.left, (int) box.top, tpl.getWidth(), tpl.getHeight());
        ivCut.setImageBitmap(cut);
        //overlap.drawBox(box);
        imageView.setImageBitmap(org_bitmap);
        OpencvUtils.comPareHist(cut,tpl);
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
    private void log(String str){
        Log.i("carl",str);
    }


}











