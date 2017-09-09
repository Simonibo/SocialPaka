package me.simonbohnen.socialpaka;

import android.content.Intent;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.samples.vision.ocrreader.R;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountDetailActivity extends AppCompatActivity {
    private static final String USER_NAME_ID = "USER_NAME_ID";
    private static final int REQUEST_CODE_DOWNLOAD = 5;

    private Toolbar toolbar;
    private String name;
    private TextView textView_name;
    private TextView textView_mail;
    private TextView textView_tel;
    private TextView textView_bday;
    private Button slackbutton;

    private ProgressBar progressBar;
    private TextView textView_download;


    public static void putName(String name, Intent intent) {
        intent.putExtra(USER_NAME_ID, name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail_activity);

        this.name = getIntent().getStringExtra(USER_NAME_ID);

        toolbar = (Toolbar) findViewById(R.id.account_detail_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar t = getSupportActionBar();
        if(t != null) {
            t.setDisplayHomeAsUpEnabled(true);
            t.setTitle(name);
        }

        textView_name = (TextView) findViewById(R.id.textView_name);
        textView_mail = (TextView) findViewById(R.id.textView_e_mail);
        textView_tel = (TextView) findViewById(R.id.textView_phone);
        textView_bday = (TextView) findViewById(R.id.textView_birthday);

        textView_download = (TextView) findViewById(R.id.account_detail_download);
        progressBar = (ProgressBar) findViewById(R.id.account_detail_progressBar);

         slackbutton = (Button) findViewById(R.id.slackbutton);
        slackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "slack://user?team=T02E176Q1&id=";
                if(name.equals("Simon")) {
                    uri += "U6ZLUMU5N";
                }
                final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(browserIntent);
            }
        });
    }

    private void showProgressbar() {
        progressBar.setVisibility(View.VISIBLE);
        textView_download.setVisibility(View.VISIBLE);
        textView_name.setVisibility(View.INVISIBLE);
        textView_mail.setVisibility(View.INVISIBLE);
        textView_tel.setVisibility(View.INVISIBLE);
        textView_bday.setVisibility(View.INVISIBLE);
        slackbutton.setVisibility(View.INVISIBLE);
    }

    private void hideProgressbar() {
        progressBar.setVisibility(View.INVISIBLE);
        textView_download.setVisibility(View.INVISIBLE);
        textView_name.setVisibility(View.VISIBLE);
        textView_mail.setVisibility(View.VISIBLE);
        textView_tel.setVisibility(View.VISIBLE);
        textView_bday.setVisibility(View.VISIBLE);
        slackbutton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        DownloadIntentService.startService(this, REQUEST_CODE_DOWNLOAD);

        showProgressbar();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DOWNLOAD) {
            if (resultCode == DownloadIntentService.SUCCESS_CODE) {
                String text = data.getStringExtra(DownloadIntentService.ID_EXTRA_DATA);

                try {
                    JSONObject jsonObject = new JSONObject(text);

                    String fullName = jsonObject.getString("fullName");
                    String bday = jsonObject.getString("bday");
                    String tel = jsonObject.getString("phone");
                    String mail = jsonObject.getString("mail");

                    textView_name.setText(fullName);
                    textView_tel.setText(tel);
                    textView_mail.setText(mail);
                    textView_bday.setText(bday);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hideProgressbar();
            } else if (resultCode == DownloadIntentService.ERROR_CODE) {
                Intent errorIntent = new Intent(this, ErrorActivity.class);
                startActivity(errorIntent);
            }
        }
    }
}
