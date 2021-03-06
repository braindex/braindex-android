package com.cybexmobile.activity;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cybexmobile.helper.ActionBarTitleHelper;
import com.cybexmobile.helper.StoreThemeHelper;
import com.cybexmobile.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ChooseThemeActivity extends AppCompatActivity {

    private ListView mListView;
    private List<String> mThemeList;
    private int selectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_theme);
        if (getSupportActionBar() != null) {
            ActionBarTitleHelper.centeredActionBarTitle(this);
        }
        initView();
        initData();
        setBackButton();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.select_theme_list);
    }

    private void initData() {
        mThemeList = new ArrayList<String>();
        mThemeList.add(getResources().getString(R.string.setting_theme_dark));
        mThemeList.add(getResources().getString(R.string.setting_theme_light));
        selectedPosition = StoreThemeHelper.getLocalThemePosition(this);

        final ThemeAdapter themeAdapter = new ThemeAdapter(this, mThemeList, selectedPosition);
        mListView.setAdapter(themeAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                StoreThemeHelper.setLocalThemePosition(ChooseThemeActivity.this, selectedPosition);
                themeAdapter.notifyDataSetChanged();
                if (mThemeList.get(position).equals(getResources().getString(R.string.setting_theme_light))) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    PreferenceManager.getDefaultSharedPreferences(ChooseThemeActivity.this).edit().putBoolean("night_mode", true).apply();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    PreferenceManager.getDefaultSharedPreferences(ChooseThemeActivity.this).edit().putBoolean("night_mode", false).apply();

                }
                EventBus.getDefault().post("THEME_CHANGED");
                recreate();
            }
        });
    }

    private void setBackButton() {
        ImageView backButton;
        TextView mTitile;
        if (getSupportActionBar() != null) {
            backButton = (ImageView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_arrow_back_button);
            mTitile = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.actionbar_title);
            backButton.setVisibility(View.VISIBLE);
            mTitile.setText(getResources().getString(R.string.title_setting));
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    public class ThemeAdapter extends BaseAdapter {
        Context mContext;
        List<String> mList;
        int mPosiion;
        LayoutInflater inflater;

        public ThemeAdapter(Context context, List<String> list, int position) {
            mContext = context;
            mList = list;
            mPosiion = position;
            inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }



        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.theme_item_adapter, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.theme_name);
                viewHolder.select = (RadioButton) convertView.findViewById(R.id.theme_radio_button);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            viewHolder.name.setText(mList.get(position));
            if(selectedPosition == position) {
                viewHolder.select.setBackgroundResource(R.drawable.ic_check);
                viewHolder.name.setTextColor(getResources().getColor(R.color.primary_orange));
            } else {
                viewHolder.select.setBackgroundResource(0);
                viewHolder.name.setTextColor(getResources().getColor(R.color.primary_color_white));
            }

            return convertView;
        }

        public class ViewHolder {
            TextView name;
            RadioButton select;
        }
    }

}
