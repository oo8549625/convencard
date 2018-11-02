package net.macdidi.convencard;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class PrefActivity extends PreferenceActivity {
    // 加入欄位變數宣告
    private SharedPreferences sharedPreferences;
    private Preference defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定使用的設定畫面配置資源
        // 這行敘述從API Level 11開始會產生警告訊息
        // 不過不會影響應用程式的運作
        addPreferencesFromResource(R.xml.mypreference);
        // 讀取顏色設定元件
        defaultColor = (Preference)findPreference("DEFAULT_COLOR");
        // 建立SharedPreferences物件
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 讀取設定的預設顏色
        int color = sharedPreferences.getInt("DEFAULT_COLOR", -1);

        if (color != -1) {
             //設定顏色說明
            defaultColor.setSummary(getString(R.string.default_color_summary) +
                    ": " + ItemActivity.getColors(color));
        }
    }

}