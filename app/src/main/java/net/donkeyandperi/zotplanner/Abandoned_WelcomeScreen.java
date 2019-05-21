package net.donkeyandperi.zotplanner;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.dx.dxloadingbutton.lib.LoadingButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class Abandoned_WelcomeScreen extends AppCompatActivity {
    private boolean success = false;
    private Intent intent;
    private MyApp app;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);
        app = (MyApp) getApplication();
        final LoadingButton lb = (LoadingButton)findViewById(R.id.loading_btn);
        intent = new Intent(Abandoned_WelcomeScreen.this, SearchCourseOption.class);
        lb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lb.startLoading(); //start loading
                refreshLists(lb);
            }
        });
        lb.setAnimationEndListener(new LoadingButton.AnimationEndListener() {
            @Override
            public void onAnimationEnd(LoadingButton.AnimationType animationType) {
                startActivity(intent);
                lb.reset();
            }
        });
    }
    private void refreshLists(final LoadingButton lb){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    org.jsoup.nodes.Document document = Jsoup.connect("https://www.reg.uci.edu/perl/WebSoc").timeout(999999999).get();
                    intent.putStringArrayListExtra("dept_list", new ArrayList<String>(parseHtmlForText(document, "Dept")));
                    intent.putStringArrayListExtra("quarter_list", new ArrayList<String>(parseHtmlForText(document, "YearTerm")));
                    intent.putStringArrayListExtra("ge_list", new ArrayList<String>(parseHtmlForText(document, "Breadth")));
                    intent.putStringArrayListExtra("level_list", new ArrayList<String>(parseHtmlForText(document, "Division")));
                    intent.putStringArrayListExtra("dept_value_list", new ArrayList<String>(parseHtmlForValue(document, "Dept")));
                    intent.putStringArrayListExtra("quarter_value_list", new ArrayList<String>(parseHtmlForValue(document, "YearTerm")));
                    intent.putStringArrayListExtra("ge_value_list", new ArrayList<String>(parseHtmlForValue(document, "Breadth")));
                    intent.putStringArrayListExtra("level_value_list", new ArrayList<String>(parseHtmlForValue(document, "Division")));
                    app.setRegNormalPage(document);
                    continueToNext(lb, true);
                }catch (Exception e){
                    e.printStackTrace();
                    continueToNext(lb, false);
                }
            }
        }).start();
    }
    private List<String> parseHtmlForValue(Document document, String name) {
        Elements elements = document.select("select[name=" + name + "]").select("option");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < elements.size(); ++i) {
            //Log.i("size: ", String.valueOf(elements.size()));
            //list.add(element.select("option").attr("value"));
            list.add(elements.get(i).attr("value"));
        }
        return list;
    }
    private List<String> parseHtmlForText(Document document, String name) {
        Elements elements = document.select("select[name=" + name + "]").select("option");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < elements.size(); ++i) {
            //Log.i("size: ", String.valueOf(elements.size()));
            //list.add(element.select("option").attr("value"));
            list.add(elements.get(i).text());
        }
        return list;
    }
    private void continueToNext(final LoadingButton lb, final boolean judge){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (judge)
                {
                    lb.loadingSuccessful();
                    success = true;
                }else {
                    lb.loadingFailed();
                    lb.reset();
                }
            }
        });
    }
}
