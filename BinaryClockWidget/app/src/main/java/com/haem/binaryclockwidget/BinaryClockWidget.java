package com.haem.binaryclockwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.security.acl.LastOwnerException;
import java.util.Calendar;


/**
 * Implementation of App Widget functionality.
 */
public class BinaryClockWidget extends AppWidgetProvider {

    //Tag for any log messages that may be needed
    private static final String BC_WIDGET = "BCWidget";
    public static final String ACTION_MANUAL_UPDATE = "com.haem.binaryclockwidget.MANUAL_UPDATE";
    private static byte seconds=0;
    private static byte hours=0;
    private static byte minutes=0;

    private static BinaryClockView clockView;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction().equals(ACTION_MANUAL_UPDATE)){
            doManualUpdate(context);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;

        if(clockView==null){
            onEnabled(context);
        }

        Calendar calendar= Calendar.getInstance();

        seconds= (byte) (calendar.get(Calendar.SECOND)+Math.round(0.001f * calendar.get(Calendar.MILLISECOND)));
        hours= (byte) calendar.get(Calendar.HOUR_OF_DAY);
        minutes= (byte) calendar.get(Calendar.MINUTE);

        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        clockView=new BinaryClockView(context);
        float d=context.getResources().getDisplayMetrics().density;
        clockView.measure((int) (200 * d), (int) (200 * d));
        clockView.layout(0, 0, (int) (200 * d), (int) (200 * d));
        int padding= (int) (20*d);
        clockView.setPadding(padding, padding, padding, padding);
        clockView.setDrawingCacheEnabled(true);

        //Set the repeating alarm to update the seconds and minutes
        Intent intent=new Intent(context,BinaryClockWidget.class);
        intent.setAction(ACTION_MANUAL_UPDATE);

        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 1000, 937, pendingIntent);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //Get the width and height of the widget to render the clockview in a proper size
        int width,height;
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        width= options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        if(clockView==null) clockView=new BinaryClockView(context);
        clockView.setDrawingCacheEnabled(true);
        clockView.measure(width, height);
        clockView.layout(0, 0, width, height);
        int padding = (Math.min(width, height) / 20);
        clockView.setPadding(padding, padding, padding, padding);
        clockView.setTime(hours,minutes,seconds);
        clockView.invalidate();

        //Draw the clockview's drawing canvas on each widget's renderingimage
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.binary_clock_widget);
        Bitmap bitmap=clockView.getDrawingCache();
        views.setImageViewBitmap(R.id.renderingImage,bitmap);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Update all the widgets without the benefit of an ACTION_APPWIDGET_UPDATE
     * @param context
     */
    private void doManualUpdate(Context context){
        ComponentName name=new ComponentName(context,BinaryClockWidget.class);
        AppWidgetManager manager=AppWidgetManager.getInstance(context);
        int[] ids=manager.getAppWidgetIds(name);
        onUpdate(context, manager, ids);
    }
}

