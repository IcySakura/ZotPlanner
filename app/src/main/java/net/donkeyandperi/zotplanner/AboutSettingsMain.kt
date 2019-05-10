package net.donkeyandperi.zotplanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class AboutSettingsMain : AppCompatActivity() {

    private var app: MyApp? = null
    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_settings_information)
        app = application as MyApp
        setTitle(R.string.main_settings_about_textview)

        val toolbar = findViewById<View>(R.id.about_settings_toolbar) as Toolbar

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val version = findViewById<View>(R.id.about_settings_information_version) as TextView
        val icon = findViewById<View>(R.id.about_settings_information_icon) as ImageView
        val emailUsForQuestion = findViewById<View>(R.id.about_settings_email_us_for_question) as TextView

        version.text = BasicFunctions.getLocalVersionName(context)

        val countOfChange = arrayOf(0)
        icon.setOnClickListener {
            if (countOfChange[0] == 5) {
                //version.setText(BasicFunctions.getLocalVersion(context));  Removed for getting bug
                version.text = "╭(●｀∀´●)╯╰(●’◡’●)╮"
                countOfChange[0] = 0
            } else {
                ++countOfChange[0]
            }
        }

        emailUsForQuestion.setOnClickListener {
            val uri = Uri.parse("mailto:lyx981mike@gmail.com")
            val email = arrayOf("lyx981mike@gmail.com")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra(Intent.EXTRA_CC, email) // 抄送人
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_about_email_title)) // 主题
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_about_email_detail)) // 正文
            startActivity(Intent.createChooser(intent, getString(R.string.settings_about_send_email_chooser)))
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
