package com.haem.binaryclockwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;

/**
 * Displays a circular binary clock with 64-minute hours, 64-second minutes,
 */
public class BinaryClockView extends View {

    private  static final String LOGTAG="BCVIew";
    /**
     * The multiplier between base-60 and base-64 minutes and seconds
     */
    private static final float CONVERSION_MULTIPLIER=64f/60f;

    private byte hours=18;
    private byte minutes=44;
    private byte seconds=26;
    private double[] precalcSinHours={0,Math.sin(2*Math.PI/3),Math.sin(4*Math.PI/3)};
    private double[] precalcCosHours={1,Math.cos(2 * Math.PI / 3),Math.cos(4 * Math.PI / 3)};
    private double[] precalcSinMinutes={0,Math.sin(2*Math.PI/6),Math.sin(4*Math.PI/6),Math.sin(6 * Math.PI / 6),
            Math.sin(8 * Math.PI / 6),Math.sin(10 * Math.PI / 6)};
    private double[] precalcCosMinutes={1,Math.cos(2 * Math.PI / 6),Math.cos(4 * Math.PI / 6),Math.cos(6 * Math.PI / 6),
            Math.cos(8 * Math.PI / 6),Math.cos(10 * Math.PI / 6)};
    private int paddingLeft=0;
    private int paddingTop=0;
    private int paddingRight=0;
    private int paddingBottom=0;

    public boolean isShowSeconds() {
        return showSeconds;
    }

    public void setShowSeconds(boolean showSeconds) {
        this.showSeconds = showSeconds;
    }

    private boolean showSeconds=false;

    public BinaryClockView(Context context) {
        super(context);
        init(null, 0);
    }
    public BinaryClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }
    public BinaryClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
        Calendar calendar= Calendar.getInstance();
        hours= (byte) calendar.get(Calendar.HOUR);
        minutes= (byte) calendar.get(Calendar.MINUTE);
        seconds= (byte) calendar.get(Calendar.SECOND);
    }

    /**
     * Converts the given time to 64-second minutes and 64-minute hours
     * @param hours
     * @param minutes
     * @param seconds
     */
    public void setTime(int hours,int minutes,int seconds){
        double hourfraction=seconds/3600.0 + minutes/60.0;
        int time= (int) (hourfraction*64*64);
        this.seconds= (byte) (time%64);
        time=(time-seconds)/64;
        this.minutes= (byte) (time%64);
        this.hours= (byte) hours;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BinaryClockView, defStyle, 0);
        showSeconds=a.getBoolean(R.styleable.BinaryClockView_drawSeconds,false);
        a.recycle();


        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void invalidateTextPaintAndMeasurements() {
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        paddingLeft = left;
        paddingTop = top;
        paddingRight = right;
        paddingBottom = bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        float baseRadius=contentWidth>contentHeight?contentHeight:contentWidth;
        baseRadius=baseRadius/2;

        float centerX=paddingLeft+contentWidth/2;
        float centerY=paddingTop+contentHeight/2;

        baseRadius*=7f/8;

        Paint p=new Paint();

        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.STROKE);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);
        p.setStrokeWidth(1.25f*getResources().getDisplayMetrics().density);


        float r=baseRadius/8;

        int third= hours/8;

        for(int i=0;i<precalcCosHours.length;++i){
            float x= (float) (baseRadius*precalcSinHours[i]+centerX);
            float y= (float) (-baseRadius*precalcCosHours[i]+centerY);

            byte mask= (byte) (1<<i);

            if(i==third){
                p.setColor(Color.RED);
            }else{
                p.setColor(Color.WHITE);
            }

            if((hours & mask) !=0){
                p.setStyle(Paint.Style.FILL_AND_STROKE);
            }else{
                p.setStyle(Paint.Style.STROKE);
            }

            canvas.drawCircle(x, y, r, p);

        }
        p.setColor(Color.WHITE);

        baseRadius*=0.6;

        r=baseRadius/10;

        for(int i=0;i<precalcCosMinutes.length;i++){
            float x= (float) (baseRadius*precalcSinMinutes[i]+centerX);
            float y= (float) (-baseRadius*precalcCosMinutes[i]+centerY);

            byte mask= (byte) (1<<i);

            if((minutes & mask) !=0){
                p.setStyle(Paint.Style.FILL_AND_STROKE);
            }else{
                p.setStyle(Paint.Style.STROKE);
            }

            canvas.drawCircle(x, y, r, p);

        }

        baseRadius*=0.5;

        r=baseRadius/10;

        p.setStrokeWidth(1.0f*getResources().getDisplayMetrics().density);

        for(int i=0;showSeconds&&i<precalcCosMinutes.length;i++){
            float x= (float) (baseRadius*precalcSinMinutes[i]+centerX);
            float y= (float) (-baseRadius*precalcCosMinutes[i]+centerY);

            byte mask= (byte) (1<<i);

            if((seconds & mask) !=0){
                p.setStyle(Paint.Style.FILL_AND_STROKE);
            }else{
                p.setStyle(Paint.Style.STROKE);
            }

            canvas.drawCircle(x, y, r, p);

        }
    }

}
