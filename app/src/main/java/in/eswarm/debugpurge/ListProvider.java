package in.eswarm.debugpurge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.List;

/* Created by Eswar Malla */
public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<PackageInfo> mPackageInfoList;
    private Context mContext;

    public ListProvider(Context context, Intent intent) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {
        mPackageInfoList = MainActivity.getDebugPackages(mContext);
    }

    @Override
    public void onDataSetChanged() {
        mPackageInfoList = MainActivity.getDebugPackages(mContext);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mPackageInfoList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                mContext.getPackageName(), R.layout.widget_app_item);
        PackageInfo pkgInfo = mPackageInfoList.get(position);
        remoteView.setTextViewText(R.id.appItemNameText, mContext.getPackageManager().getApplicationLabel(pkgInfo.applicationInfo));
        remoteView.setTextViewText(R.id.appItemPackageText, pkgInfo.packageName);

        Bundle extras = new Bundle();
        extras.putInt(WidgetProvider.EXTRA_ITEM, position);
        extras.putString(WidgetProvider.PACKAGE_ITEM, pkgInfo.packageName);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteView.setOnClickFillInIntent(R.id.appItemButton, fillInIntent);
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}