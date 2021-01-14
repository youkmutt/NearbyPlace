package com.example.nearbyplaceapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                content.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                int height = content.getHeight();
                int width = content.getWidth();
                int selectionpixel = height / 3 > width / 2 ? width / 2 : height / 3;

                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                for (int i = 0; i < 3; i++) {
                    LinearLayout row = new LinearLayout(MainActivity.this);
                    row.setLayoutParams(new LinearLayout.LayoutParams(width, height / 3));
                    for (int j = 0; j < 2; j++) {
                        Button btnTag = new Button(MainActivity.this);
                        LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(selectionpixel, selectionpixel);
                        btnTag.setLayoutParams(layoutparams);
                        btnTag.setText("Button " + (j + 1 + (i * 2)));
                        btnTag.setId(j + 1 + (i * 2));
                        btnTag.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                                if (!gpsEnabled) {
                                    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(settingsIntent);
                                }else{
                                    Intent mainIntent = new Intent(MainActivity.this, ListMapActivity.class);
                                    startActivity(mainIntent);
                                }
                            }
                        });
                        row.setGravity(Gravity.CENTER_HORIZONTAL);
                        row.setGravity(Gravity.CENTER_VERTICAL);
                        row.setGravity(Gravity.CENTER);
                        row.addView(btnTag);
                    }
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    layout.setGravity(Gravity.CENTER_VERTICAL);
                    layout.setGravity(Gravity.CENTER);
                    layout.addView(row);
                }
                setContentView(layout);
            }
        });
    }
}
