package com.guardian.webview.angel;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Button btnEnabled = (Button) findViewById(R.id.btnEnabled);
        btnEnabled.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                finish();
            }
        });

        Button btnClosed = (Button) findViewById(R.id.btnClosed);
        btnClosed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
    }
}
