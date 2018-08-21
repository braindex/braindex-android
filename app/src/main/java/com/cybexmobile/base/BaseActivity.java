package com.cybexmobile.base;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.cybexmobile.R;
import com.cybexmobile.dialog.LoadDialog;
import com.cybexmobile.event.Event;
import com.cybexmobile.helper.StoreLanguageHelper;
import com.cybexmobile.receiver.NetWorkBroadcastReceiver;
import com.cybexmobile.receiver.NetworkChangedCallback;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import static com.cybexmobile.utils.NetworkUtils.TYPE_NOT_CONNECTED;

public abstract class BaseActivity extends AppCompatActivity {

    private final String TAG = BaseActivity.class.getSimpleName();

    private static final String PARAM_NETWORK_AVAILABLE = "param_network_available";

    public static boolean isActive;

    private LoadDialog mLoadDialog;
    private AlertDialog mHintDialog;

    private NetWorkBroadcastReceiver mNetWorkBroadcastReceiver;
    private NetworkChangedCallback mNetworkChangedCallback;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateResources(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            registerNetWorkCallback();
        } else {
            registerNetWorkReceiver();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        if (!isActive) {
            //app 从后台唤醒，进入前台
            isActive = true;
            Log.i("ACTIVITY", "程序从后台唤醒");
            EventBus.getDefault().post(new Event.IsOnBackground(false));
        }
        super.onResume();
        MobclickAgent.onResume(this);
        Log.d(TAG, "onResume");
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        if (!isAppOnForeground()) {
            //app 进入后台
            isActive = false;//记录当前已经进入后台
            Log.i("ACTIVITY", "程序进入后台");
            EventBus.getDefault().post(new Event.IsOnBackground(true));
        }
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            unregisterNetWorkCallback();
        } else {
            unregisterNetWorkReceiver();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Resources getResources() {
        /**
         * fix bug:CYM-455
         * app字体大小不跟随系统改变
         */
        Resources resources = super.getResources();
        if(resources.getConfiguration().fontScale != 1){
            Configuration newConf = new Configuration();
            newConf.setToDefaults();
            resources.updateConfiguration(newConf, resources.getDisplayMetrics());
        }
        return resources;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerNetWorkCallback(){
        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkChangedCallback = new NetworkChangedCallback(this);
        conn.requestNetwork(new NetworkRequest.Builder().build(), mNetworkChangedCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void unregisterNetWorkCallback(){
        if(mNetworkChangedCallback != null){
            ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            conn.unregisterNetworkCallback(mNetworkChangedCallback);
        }
    }

    /**
     * 注册广播
     */
    private void registerNetWorkReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetWorkBroadcastReceiver = new NetWorkBroadcastReceiver();
        registerReceiver(mNetWorkBroadcastReceiver, intentFilter);
    }

    /**
     * 注销广播
     */
    private void unregisterNetWorkReceiver(){
        if(mNetWorkBroadcastReceiver != null){
            unregisterReceiver(mNetWorkBroadcastReceiver);
            mNetWorkBroadcastReceiver = null;
        }
    }

    protected final void showHintDialog(@StringRes int messageId){
        if(mHintDialog == null){
            mHintDialog = new AlertDialog.Builder(this, R.style.LoadDialog)
                .setMessage(messageId)
                .create();
        }
        mHintDialog.setMessage(getString(messageId));
        mHintDialog.show();
    }

    protected final void hideHintDialog(){
        if(mHintDialog != null && mHintDialog.isShowing()){
            mHintDialog.dismiss();
        }
    }

    //show load dialog
    protected final void showLoadDialog(){
        this.showLoadDialog(false);

    }

    protected final void showLoadDialog(boolean isCancelable){
        if(mLoadDialog == null){
            mLoadDialog = new LoadDialog(this, R.style.LoadDialog);
        }
        mLoadDialog.setCancelable(isCancelable);
        mLoadDialog.show();
    }

    //hide load dialog
    protected final void hideLoadDialog(){
        if(mLoadDialog != null && mLoadDialog.isShowing()){
            mLoadDialog.dismiss();
        }
    }

    private Context updateResources(Context context) {
        String language = StoreLanguageHelper.getLanguageLocal(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetWorkStateChanged(Event.NetWorkStateChanged event){
        onNetWorkStateChanged(event.getState() != TYPE_NOT_CONNECTED);
    }

    public abstract void onNetWorkStateChanged(boolean isAvailable);

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }
}
