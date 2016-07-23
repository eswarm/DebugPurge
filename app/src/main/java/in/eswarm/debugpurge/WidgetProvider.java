package in.eswarm.debugpurge;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/* Created by Eswar Malla */
public class WidgetProvider extends AppWidgetProvider {

    public static final String UNINSTALL_ACTION = "in.eswarm.debugpurge.TOAST_ACTION";
    public static final String EXTRA_ITEM = "in.eswarm.debugpurge.EXTRA_ITEM";
    public static final String PACKAGE_ITEM = "in.eswarm.debugpurge.PACKAGE_ITEM";
    public static final String ACTION_UPDATE = "in.eswarm.debugpurge.ACTION_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(UNINSTALL_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            String packageName = intent.getStringExtra(PACKAGE_ITEM);
            if(packageName != null) {
                context.startActivity(MainActivity.getUninstallIntent(packageName));
            }
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = updateWidgetListView(context, appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews updateWidgetListView(Context context,
                                             int appWidgetId) {
        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),R.layout.widget_layout);
        //RemoteViews Service needed to provide adapter for ListView
        Intent intent = new Intent(context, WidgetService.class);
        //passing app widget id to that RemoteViews Service
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(
                intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(appWidgetId, R.id.listView,
                intent);
        remoteViews.setEmptyView(R.id.listView, R.id.emptyView);

        Intent toastIntent = new Intent(context, WidgetProvider.class);
        toastIntent.setAction(WidgetProvider.UNINSTALL_ACTION);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.listView, toastPendingIntent);

        return remoteViews;
    }

}
