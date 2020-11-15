package com.demo.carl.opencvhelloworld;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class OpencvUtils {

    public static RectF templateMatch(Bitmap tpl, Bitmap bitmap){
        Mat src = new Mat();
        Mat tplMat = new Mat();
        Utils.bitmapToMat(bitmap,src);
        Utils.bitmapToMat(tpl,tplMat);
        int width = bitmap.getWidth() - tpl.getWidth()+1;
        int height = bitmap.getHeight() - tpl.getHeight() + 1;
        log("tpl.getWidth():"+tpl.getWidth()+",tpl.getHeight:"+tpl.getHeight());
        log("width:"+width+",height:"+height);
        Mat result = new Mat(width,height, CvType.CV_32FC1);
        Imgproc.matchTemplate(src,tplMat,result, Imgproc.TM_CCORR_NORMED);
        Core.normalize(result,result,0,1.0, Core.NORM_MINMAX,-1);
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);
        double max_score = minMaxLocResult.maxVal;
        double min_score = minMaxLocResult.minVal;
        Point pt = minMaxLocResult.maxLoc;
        log("min_score:"+min_score+",max_score:"+max_score+",pt:"+pt);
        Core.rectangle(src,pt,new Point(pt.x+ tpl.getWidth(),pt.y+tpl.getHeight()),
                new Scalar(255,0,0,0),5,8,0);
        Utils.matToBitmap(src,bitmap);
        src.release();
        result.release();
        tplMat.release();
        return new RectF((float)pt.x,(float) pt.y,(float)(pt.x+ tpl.getWidth()),(float)(pt.y+tpl.getHeight()));
    }

    public static Result templateMatch2(Bitmap tpl, Bitmap bitmap){
        RectF box = templateMatch(tpl, bitmap);
        Bitmap cut = Bitmap.createBitmap(bitmap, (int) box.left, (int) box.top, tpl.getWidth(), tpl.getHeight());
        double score = OpencvUtils.comPareHist(cut,tpl);
        if (score>0.25){
            return new Result(box,score);
        }else {
            return null;
        }
    }

    public static double comPareHist(Bitmap bitmap1,Bitmap bitmap2){
        Mat bitmap1Mat = new Mat();
        Mat bitmap2Mat = new Mat();
        Utils.bitmapToMat(bitmap1,bitmap1Mat);
        Utils.bitmapToMat(bitmap2,bitmap2Mat);
        bitmap1Mat.convertTo(bitmap1Mat, CvType.CV_32F);
        bitmap2Mat.convertTo(bitmap2Mat, CvType.CV_32F);
        Mat hist_1 = new Mat();
        Mat hist_2 = new Mat();
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(25);
        Imgproc.calcHist(Arrays.asList(bitmap1Mat), new MatOfInt(0),
                new Mat(), hist_1, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(bitmap2Mat), new MatOfInt(0),
                new Mat(), hist_2, histSize, ranges);
        double target = Imgproc.compareHist(hist_1, hist_2, Imgproc.CV_COMP_CORREL);
        log("相似度："+target);
        return target;
    }
    private static void log(String str){
        Log.i("carl",str);
    }
}
















