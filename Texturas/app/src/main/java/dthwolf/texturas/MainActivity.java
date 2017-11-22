package dthwolf.texturas;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;

import static org.opencv.core.Core.FONT_HERSHEY_PLAIN;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat a_imgRGBA,a_imgHSV;
    private Mat a_imgGreen,a_imgBlue,a_imgYellow;
    private Scalar a_scaHighGreen,a_scaLowGreen;
    private Scalar a_scaHighBlue,a_scaLowBlue;
    private Scalar a_scaHighYellow,a_scaLowYellow;

    private boolean a_bndGreen,a_bndBlue,a_bndYellow;
    double a_angulo = 0;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //a_scaLowGreen = new Scalar(66,122,129);
        //a_scaHighGreen = new Scalar(86,255,255);
        a_scaLowGreen = new Scalar(34,50,50);
        a_scaHighGreen = new Scalar(80,220,200);

        a_scaLowBlue = new Scalar(97,100,117);
        a_scaHighBlue = new Scalar(117,255,255);

        //a_scaLowBlue = new Scalar(92,0,0);
        //a_scaHighBlue = new Scalar(124,256,256);

        //a_scaLowYellow = new Scalar(23,59,119);
        //a_scaHighYellow = new Scalar(54,255,255);

        a_scaLowYellow = new Scalar(20,124,123);
        a_scaHighYellow = new Scalar(30,256,256);


        a_bndGreen = false;
        a_bndYellow = false;
        a_bndBlue = false;

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(480, 320);
        //mOpenCvCameraView.enableFpsMeter();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        a_imgHSV = new Mat(width,height,CvType.CV_16UC4);
        a_imgBlue = new Mat(width,height,CvType.CV_16UC4);
        a_imgGreen = new Mat(width,height,CvType.CV_16UC4);
        a_imgYellow = new Mat(width,height,CvType.CV_16UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        double v_posX = 0.0;
        double v_posY = 0.0;
        int v_radio = 0;

        //Reiniciando
        a_bndGreen = false;
        a_bndYellow = false;
        a_bndBlue = false;

        //Puntos de cada circulo
        Point v_centerBlue = new Point();
        Point v_centerGreen = new Point();
        Point v_centerYellow = new Point();

        //Transformar RGB a HSV
        a_imgRGBA =inputFrame.rgba();
        Imgproc.cvtColor(a_imgRGBA,a_imgHSV,Imgproc.COLOR_RGB2HSV);

        //Rango de Colores
        Core.inRange(a_imgHSV, a_scaLowGreen, a_scaHighGreen,a_imgGreen); // Listo
        Core.inRange(a_imgHSV,a_scaLowBlue,a_scaHighBlue,a_imgBlue); //Listo
        Core.inRange(a_imgHSV,a_scaLowYellow,a_scaHighYellow,a_imgYellow); //Listo


        //Aplicación GaussianBlur y Dilatacion
        Imgproc.GaussianBlur (a_imgGreen,a_imgGreen,new Size(9,9),2,2);
        Imgproc.dilate(a_imgGreen,a_imgGreen,new Mat());

        Imgproc.GaussianBlur (a_imgBlue,a_imgBlue,new Size(9,9),2,2);
        Imgproc.dilate(a_imgBlue,a_imgBlue,new Mat());

        Imgproc.GaussianBlur (a_imgYellow,a_imgYellow,new Size(9,9),2,2);
        Imgproc.dilate(a_imgYellow,a_imgYellow,new Mat());

        //Obtener los circulo Azul
        Mat v_circlesBlue = new Mat(a_imgBlue.rows(),a_imgBlue.cols(),CvType.CV_16UC4);
        Imgproc.HoughCircles(a_imgBlue,v_circlesBlue,Imgproc.CV_HOUGH_GRADIENT, 1,a_imgBlue.rows()/8,100,20,0,0);

        if (v_circlesBlue.rows()>0 && v_circlesBlue.cols()>0){
            double[] v_data = v_circlesBlue.get(0,0);
            v_posX = v_data[0];
            v_posY = v_data[1];
            v_radio = (int) v_data[2];
            v_centerBlue = new Point(v_posX,v_posY);
            // circle center
            Imgproc.circle(a_imgRGBA,v_centerBlue,3, new Scalar(255,0,0),-1);
            // circle outline
            Imgproc.circle(a_imgRGBA,v_centerBlue,v_radio,new Scalar(255,0,0),2);
            a_bndBlue = true;
        }

        //Obtener los circulo Verde
        Mat v_circlesGreen = new Mat(a_imgGreen.rows(),a_imgGreen.cols(),CvType.CV_16UC4);
        Imgproc.HoughCircles(a_imgGreen,v_circlesGreen,Imgproc.CV_HOUGH_GRADIENT, 1,a_imgGreen.rows()/8,100,20,0,0);

        if (v_circlesGreen.rows()>0 && v_circlesGreen.cols()>0){
            double[] v_data = v_circlesGreen.get(0,0);
            v_posX = v_data[0];
            v_posY = v_data[1];
            v_radio = (int) v_data[2];
            v_centerGreen = new Point(v_posX,v_posY);
            // circle center
            Imgproc.circle(a_imgRGBA,v_centerGreen,3, new Scalar(255,0,0),-1);
            // circle outline
            Imgproc.circle(a_imgRGBA,v_centerGreen,v_radio,new Scalar(255,0,0),2);
            a_bndGreen = true;
        }

        //Obtener los circulo Amarisho
        Mat v_circlesYellow = new Mat(a_imgYellow.rows(),a_imgYellow.cols(),CvType.CV_16UC4);
        Imgproc.HoughCircles(a_imgYellow,v_circlesYellow,Imgproc.CV_HOUGH_GRADIENT, 1,a_imgYellow.rows()/8,100,20,0,0);

        if (v_circlesYellow.rows()>0 && v_circlesYellow.cols()>0){
            double[] v_data = v_circlesYellow.get(0,0);
            v_posX = v_data[0];
            v_posY = v_data[1];
            v_radio = (int) v_data[2];
            v_centerYellow = new Point(v_posX,v_posY);
            // circle center
            Imgproc.circle(a_imgRGBA,v_centerYellow,3, new Scalar(255,0,0),-1);
            // circle outline
            Imgproc.circle(a_imgRGBA,v_centerYellow,v_radio,new Scalar(255,0,0),2);
            a_bndYellow = true;
        }

        if (a_bndBlue&&a_bndGreen&&a_bndYellow){
            Imgproc.line(a_imgRGBA,v_centerBlue,v_centerGreen, new Scalar(255,255,255), 2);
            Imgproc.line(a_imgRGBA,v_centerGreen,v_centerYellow, new Scalar(255,255,255), 2);
            double v_m1= (v_centerGreen.y-v_centerBlue.y)/(v_centerGreen.x-v_centerBlue.x);
            double v_m2= (v_centerYellow.y-v_centerGreen.y)/(v_centerYellow.x-v_centerGreen.x);

            a_angulo = Math.toDegrees(Math.atan((v_m2-v_m1)/(1+v_m1*v_m2)));
            if(v_centerYellow.x>v_centerBlue.x&&v_centerYellow.y<v_centerGreen.y){
                a_angulo=a_angulo*-1;
                a_angulo=180+a_angulo;
            }
            if(v_centerYellow.x<v_centerBlue.x&&v_centerYellow.y<v_centerGreen.y){
                a_angulo=180+a_angulo;
            }
            if(v_centerYellow.x>v_centerBlue.x&&v_centerYellow.y>v_centerGreen.y){
                a_angulo=a_angulo*-1;
            }
        }
        String v_angulo ="Grados: "+m_Decimales(a_angulo);
        Imgproc.putText(a_imgRGBA,v_angulo,new Point(0,15),FONT_HERSHEY_PLAIN,1,new Scalar(0,0,0),2);
        return a_imgRGBA;
    }

    String m_Decimales(double p_Numero){
        DecimalFormat DF=new DecimalFormat("0.00");
        return DF.format(p_Numero);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}