package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Abandoned_LanguageSettingsDialog extends AppCompatActivity {

    private MyApp app;
    private Context context = this;
    private boolean checkedByDefault = false;
    private RadioButton englishUSbtn;
    private RadioButton chineseSimplifiedbtn;
    private RadioButton japanesebtn;
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
        japanesebtn = (RadioButton) findViewById(R.id.language_settings_japanese);
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
                        app.saveLanguage(CourseStaticData.simplifiedChinese);
                        notification = "应用语言将会在下次打开时变为中文（简体）";
                        break;
                    case R.id.language_settings_japanese:
                        app.saveLanguage(CourseStaticData.japaneseJapan);
                        notification = "アプリの言語は再起動後に変更します";
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
        switch (app.getSavedLanguage()){
            case "zh-cn":
                chineseSimplifiedbtn.setChecked(true);
                break;
            case "en-us":
                englishUSbtn.setChecked(true);
                break;
            case "ja-rJP":
                japanesebtn.setChecked(true);
                break;
            default:
                 defaultBtn.setChecked(true);
                 break;
        }
        checkedByDefault = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
