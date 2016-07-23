package in.eswarm.debugpurge;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/* Created by Eswar Malla */
public class PackageReceiver extends BroadcastReceiver {
    public PackageReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_INSTALL_PACKAGE) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_FULLY_REMOVED)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.listView);
        }
    }
}
