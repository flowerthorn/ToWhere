package test.lihongxin.towhere.CustomSpanView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import test.lihongxin.towhere.R;

/**
 * Created by lihongxin on 2017/8/8.
 */

public class CustomSpanView  extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Thread t;
    private boolean isRunning;
    private String[] mStrs=new String[]{"凯德mail","喷泉广场","北小河","利星行广场","保利国际广场","自定义"};
    private int[] mColors=new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300,
            0xFFF17E01, 0xFFFFC300, 0xFFF17E01};
    private int[] mPngs=new int[]{R.drawable.png01,R.drawable.png02,R.drawable.png03,R.drawable.png04,R.drawable.png05,R.drawable.png06};
    private Bitmap[] mBitmaps;
    private int mItemCount=6;//盘块的个数
    private RectF mRange=new RectF();//绘制盘块的范围
    private int d;//直径
    private Paint mPaint;//绘制盘块的画笔
    private Paint mTextPaint;//绘制文字的画笔
    //滚动speed
    private double mSpeed;
    private volatile float mStartAngle=0;
    //是否点击了停止
    private boolean isShouldEnd;
    private int mCenter;//控件的中心位置
    private int mPadding;//控件的padding
    //背景图
    private Bitmap mBgBitmap= BitmapFactory.decodeResource(getResources(),R.drawable.bg2);
    //文字的大小
    private float mTextSize= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,20,getResources().getDisplayMetrics());

    public CustomSpanView(Context context) {
        super(context);
    }

    public CustomSpanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder=getHolder();//拿到surfaceholder对象
        mHolder.addCallback(this);//设置回调，去监听SurfaceView的生命周期
        //设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置常亮
        this.setKeepScreenOn(true);
    }
    //重写onMeasure,使我们控件为正方形,并为我们的直径和中心点赋值
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=Math.min(getMeasuredWidth(),getMeasuredHeight());
        //圆形的直径
        d=width-getPaddingLeft()-getPaddingRight();
        mPadding=getPaddingLeft();
        mCenter=width/2;
        setMeasuredDimension(width,width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //初始化绘制圆弧的画笔
        mPaint=new Paint();
        mPaint.setAntiAlias(true);// 抗锯齿效果
        mPaint.setDither(true);
        mTextPaint=new Paint();
        mTextPaint.setColor(0xFFffffff);
        mTextPaint.setTextSize(mTextSize);
        //圆弧的绘制范围
        mRange=new RectF(getPaddingLeft(),getPaddingLeft(),d+getPaddingLeft(),d+getPaddingLeft());
        mBitmaps=new Bitmap[mItemCount];
        for (int i=0;i<mItemCount;i++){
            mBitmaps[i]=BitmapFactory.decodeResource(getResources(),mPngs[i]);
        }
        //开启线程
        isRunning=true;
        t=new Thread(this);
        t.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            //通知关闭线程
        isRunning=false;
    }

    @Override
    public void run() {
        //不断进行draw
        while (isRunning){
            long start=System.currentTimeMillis();
            draw();
            long end=System.currentTimeMillis();
            try {
                if (end-start<50){
                    Thread.sleep(50-(end-start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void draw() {
        try {
            mCanvas=mHolder.lockCanvas();//获得canvas，之后就可以绘制了
            if (mCanvas!=null){
                drawBg();//绘制背景图
                //绘制每个块块，每个块块上的文本，每个块块上的图片
                float tmpAngle=mStartAngle;
                float sweepAngle=(float) (360/mItemCount);
                for (int i=0;i<mItemCount;i++){
                    //1mpaint--marcpaint 圆弧
                    mPaint.setColor(mColors[i]);
                    mCanvas.drawArc(mRange,tmpAngle,sweepAngle,true,mPaint);
                    //2
                    drawText(tmpAngle,sweepAngle,mStrs[i]);
                    drawPng(tmpAngle,i);
                    //3
                    tmpAngle+=sweepAngle;
                }
                // 如果mspeend不等于0，则相当于在滚动
                mStartAngle+=mSpeed;
                //点击停止时，设置mspeed为递减，为0值转盘停止
                if (isShouldEnd){
                    mSpeed-=3;
                }
                if (mSpeed<=0){
                    mSpeed=0;
                    isShouldEnd=false;
                }
                //  根据当前旋转的mStartAngle计算当前滚动到的区域
               // callInExactArea(mStartAngle);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (mCanvas!=null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void callInExactArea(float startAngle) {
        // 让指针从水平向右开始计算
        float rotate = startAngle + 90;
        rotate %= 360.0;
        for (int i = 0; i < mItemCount; i++)
        {
            // 每个的中奖范围
            float from = 360 - (i + 1) * (360 / mItemCount);
            float to = from + 360 - (i) * (360 / mItemCount);

            if ((rotate > from) && (rotate < to))
            {
                //Log.d("TAG", mStrs[i]);
                return;
            }
        }

    }

    private void drawPng(float startAngle, int i) {
        //// 设置图片的宽度为直径的1/8
        int pngWidth=d/8;
        float angle= (float) ((30+startAngle)*(Math.PI/180));
        int x= (int) (mCenter+d/2/2*Math.cos(angle));
        int y= (int) (mCenter+d/2/2*Math.sin(angle));
        // 确定绘制图片的位置
        Rect rect=new Rect(x-pngWidth/2,y-pngWidth/2,x+pngWidth/2,y+pngWidth/2);
        mCanvas.drawBitmap(mBitmaps[i],null,rect,null);

    }

    private void drawText(float startAngle, float sweepAngle, String string) {
        Path path=new Path();
        path.addArc(mRange,startAngle,sweepAngle);
        float textWidth=mTextPaint.measureText(string);
        //利用水平偏移让文字居中
        float hOffset= (float) (d*Math.PI/mItemCount/2-textWidth/2);//水平偏移
        float vOffset = d / 2 / 6;// 垂直偏移
        mCanvas.drawTextOnPath(string,path,hOffset,vOffset,mTextPaint);

    }

    private void drawBg() {
        mCanvas.drawColor(0xFF00DB00);
        mCanvas.drawBitmap(mBgBitmap,null,new Rect(mPadding/2,
                mPadding/2,getMeasuredWidth()-mPadding/2,
                getMeasuredWidth()-mPadding/2),null);
    }
    //调用这个函数，就会开始旋转
    public void spinStart(){
       // float angle=(360/mItemCount);//每项角度大小
        //停下来时旋转的距离
        //float targetFrom = 4 * 360;
        mSpeed=(float)(Math.random()*187+39);
        isShouldEnd = false;
    }
    //转盘停止
    public void spinEnd(){
        mStartAngle=0;
        isShouldEnd=true;
    }
    public boolean isStart()
    {
        return mSpeed != 0;
    }

    public boolean isShouldEnd()
    {
        return isShouldEnd;
    }
}
