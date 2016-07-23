package in.eswarm.debugpurge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.Signature;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

/* Created by Eswar Malla */
public class MainActivity extends AppCompatActivity {

    public static final String TAG  = MainActivity.class.getSimpleName();
    private RecyclerView mAppList;
    private static final X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppList = (RecyclerView) findViewById(R.id.appList);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mAppList.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAppList.setLayoutManager(mLayoutManager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new DebugAppListAsync().execute();
    }

    private class DebugAppListAsync extends AsyncTask<Void, Void, List<PackageInfo>> {

        @Override
        protected void onPreExecute() {

        }

        protected void onPostExecute(List<PackageInfo> packageInfoList) {
            AppListAdapter mAppListAdapter = new AppListAdapter(packageInfoList, MainActivity.this);
            mAppList.setAdapter(mAppListAdapter);
        }

        @Override
        protected List<PackageInfo> doInBackground(Void... voids) {
            return getDebugPackages(MainActivity.this);
        }

    }

    public static List<PackageInfo> getDebugPackages(Context context) {
        ArrayList<PackageInfo> packageInfoList = new ArrayList<>();

        List<ApplicationInfo> appInfoList = context.getPackageManager().getInstalledApplications(0);
        for(ApplicationInfo appInfo : appInfoList) {
            try {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(appInfo.packageName, PackageManager.GET_SIGNATURES);
                if( isDebuggable(pkgInfo)) {
                    packageInfoList.add(pkgInfo);
                    Log.i(TAG, pkgInfo.packageName);
                }
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return packageInfoList;
    }

    // From stackoverflow, http://stackoverflow.com/questions/7085644/how-to-check-if-apk-is-signed-or-debug-build
    public static boolean isDebuggable(PackageInfo pkgInfo) {
        boolean debuggable = false;

        //PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
        Signature signatures[] = pkgInfo.signatures;
        if(signatures == null) {
            Log.e(TAG, "Signature is null for " + pkgInfo.packageName);
            return false;
        }

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for (int i = 0; i < signatures.length; i++) {
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
                if (debuggable)
                    break;
            }
        } catch (CertificateException ce) {
            Log.e(TAG, ce.getMessage());
        }

        return debuggable;
    }

    public static Intent getUninstallIntent(String packageName) {
        final Uri packageURI = Uri.parse("package:" + packageName);
        final Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageURI);
        intent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> implements AppItemClickListener {
        private List<PackageInfo> mPackageInfoList;
        private Context mContext;

        @Override
        public void appItemClicked(String packageName) {
            // From stackoverflow, http://stackoverflow.com/questions/28739533/is-there-an-intent-for-uninstallation-of-an-app-for-all-users
            mContext.startActivity(getUninstallIntent(packageName));
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView mAppName;
            public TextView mPackageName;
            public Button mAppButton;
            private AppItemClickListener mListener;

            public ViewHolder(View v, AppItemClickListener listener) {
                super(v);
                mAppName = (TextView)v.findViewById(R.id.appItemNameText);
                mPackageName = (TextView)v.findViewById(R.id.appItemPackageText);
                mAppButton = (Button)v.findViewById(R.id.appItemButton);
                mAppButton.setOnClickListener(this);
                mListener = listener;
            }

            @Override
            public void onClick(View view) {
                mListener.appItemClicked(mPackageName.getText().toString());
            }

        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public AppListAdapter(List<PackageInfo> packageInfoList, Context context) {
            mContext = context;
            mPackageInfoList = packageInfoList;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public AppListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_item, parent, false);
            ViewHolder vh = new ViewHolder(v, this);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PackageInfo pkgInfo = mPackageInfoList.get(position);
            holder.mAppName.setText(mContext.getPackageManager().getApplicationLabel(pkgInfo.applicationInfo));
            holder.mPackageName.setText(pkgInfo.packageName);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mPackageInfoList.size();
        }
    }

    public interface AppItemClickListener {
        void appItemClicked(String packageName);
    }
}
