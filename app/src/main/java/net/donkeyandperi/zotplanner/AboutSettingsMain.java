package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutSettingsMain extends AppCompatActivity {

    private MyApp app;
    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_settings_information);
        app = (MyApp) getApplication();
        setTitle(R.string.main_settings_about_textview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.about_settings_toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final TextView version = (TextView) findViewById(R.id.about_settings_information_version);
        ImageView icon = (ImageView) findViewById(R.id.about_settings_information_icon);
        TextView emailUsForQuestion = (TextView) findViewById(R.id.about_settings_email_us_for_question);

        version.setText(BasicFunctions.getLocalVersionName(context));

        final Integer[] countOfChange = {0};
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(countOfChange[0] == 5){
                    //version.setText(BasicFunctions.getLocalVersion(context));  Removed for getting bug
                    version.setText("╭(●｀∀´●)╯╰(●’◡’●)╮");
                    countOfChange[0] = 0;
                } else {
                    ++countOfChange[0];
                }
            }
        });

        emailUsForQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("mailto:lyx981mike@gmail.com");
                String[] email = {"lyx981mike@gmail.com"};
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_about_email_title)); // 主题
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_about_email_detail)); // 正文
                startActivity(Intent.createChooser(intent, getString(R.string.settings_about_send_email_chooser)));
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
