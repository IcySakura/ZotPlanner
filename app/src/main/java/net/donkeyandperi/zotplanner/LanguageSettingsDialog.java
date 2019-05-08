package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Locale;

public class LanguageSettingsDialog extends AppCompatActivity {

    private MyApp app;
    private Context context = this;
    private boolean checkedByDefault = false;
    private RadioButton englishUSbtn;
    private RadioButton chineseSimplifiedbtn;
    private RadioButton defaultBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_settings);
        setTitle(R.string.language_settings_dialog_label);
        app = (MyApp) getApplication();
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.language_settings_radiogroup);
        englishUSbtn = (RadioButton) findViewById(R.id.language_settings_english_us);
        chineseSimplifiedbtn = (RadioButton) findViewById(R.id.language_settings_chinese_simplified);
        defaultBtn = (RadioButton) findViewById(R.id.language_settings_default);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String notification = "";
                switch (group.getCheckedRadioButtonId()){
                    case R.id.language_settings_default:
                        app.saveLanguage(CourseStaticData.defaultLanguage);
                        notification = getString(R.string.change_language_to_default);
                        break;
                    case R.id.language_settings_english_us:
                        app.saveLanguage(CourseStaticData.unitedStatesEnglish);
                        notification = "App Language will be changed to English (US) after relaunch";
                        break;
                    case R.id.language_settings_chinese_simplified:
                        app.saveLanguage(CourseStaticData.mainlandChinese);
                        notification = "应用语言将会在下次打开时变为中文（简体）";
                        break;
                }
                if(!checkedByDefault){
                    if(!notification.isEmpty()){
                        Toast.makeText(context, notification, Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
            }
        });

        setDefaultCheckedBtn();

    }

    private void setDefaultCheckedBtn(){
        checkedByDefault = true;
        switch (app.getCurrentLanguage()){
            case "default":
                defaultBtn.setChecked(true);
                break;
            case "zh-cn":
                chineseSimplifiedbtn.setChecked(true);
                break;
            case "en-us":
                englishUSbtn.setChecked(true);
                break;
        }
        checkedByDefault = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
