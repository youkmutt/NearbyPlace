package com.example.nearbyplaceapplication;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListMapActivity extends Activity implements LocationListener {

    mDBList mDB;
    ListView listview;
    int count = 0, sizeData = 0,tempsizeData = 0;
    LocationManager locationManager;
    double lat=0.00,lon=0.00;
    String typeSearch = "restaurant",pagetoken = "";
    int radius = 1500;

    EditText edtRadius,edtType;
    Button btn_search;
    boolean flag_loading = false;

    ProgressDialog waiting_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_map);

        listview = findViewById(R.id.listview);
        edtRadius = findViewById(R.id.edtRadius);
        edtType = findViewById(R.id.edtType);
        btn_search = findViewById(R.id.btn_search);

        edtRadius.setText(Integer.toString(radius));
        edtType.setText(typeSearch);

        mDB = new mDBList(this);
        mDB.Drop();

        search();

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            enableLocationSettings();
        }
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void search(){
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_loading = false;
                mDB.Drop();
                sizeData = 0;
                tempsizeData = 0;
                radius = Integer.parseInt(edtRadius.getText().toString());
                typeSearch = edtType.getText().toString();
                pagetoken = "";
                try {
                    start_dialog();
                    GET_List("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+","+lon+"&radius=" + radius + "&type="+typeSearch+"&keyword=&key=AIzaSyDLk0QrEMZxgUBO_PTZezOaH4er8xb9Mh0&pagetoken="+pagetoken);
                } catch (IOException e) {
                    waiting_dialog.dismiss();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if(Math.abs(lat - location.getLatitude()) > 5 || Math.abs(lon - location.getLongitude()) > 5){
            lat = location.getLatitude();
            lon = location.getLongitude();
            try {
                start_dialog();
                GET_List("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+","+lon+"&radius=" + radius + "&type="+typeSearch+"&keyword=&key=AIzaSyDLk0QrEMZxgUBO_PTZezOaH4er8xb9Mh0&pagetoken="+pagetoken);
            } catch (IOException e) {
                waiting_dialog.dismiss();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    private void GET_List(String url) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String myResponse = response.body().string();
                ListMapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Spanned result = Html.fromHtml(myResponse);
                        String json = result.toString();

                        try {
                            JSONObject obj = new JSONObject(json);
                            try{
                                pagetoken = obj.getString("next_page_token");
                            }catch(Exception pe){
                                pagetoken = "";
                            }

                            JSONArray data = obj.getJSONArray("results");
                            sizeData = data.length();
                            count =0;
                            for(int i=0; i < data.length(); i++) {
                                JSONObject jObject = data.getJSONObject(i);
                                JSONObject geometry = jObject.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                double locatelat = location.getDouble("lat");
                                double locatelng = location.getDouble("lng");
                                double distancebetween = Callatlongx(lat,lon,locatelat,locatelng);
                                new DownLoadImageTask().execute(jObject.getString("name"),jObject.getString("vicinity"),Double.toString(distancebetween) + " KM",jObject.getString("icon"));
                            }
                        }catch (Exception ex){
                            waiting_dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    public Double Callatlongx(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
//        if (unit == "K") {
        dist = dist * 1.609344;
//        } else if (unit == "N") {
//            dist = dist * 0.8684;
//        }
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public class DownLoadImageTask extends AsyncTask<String,Void, Bitmap> {

        public DownLoadImageTask(){ }

        String name,address,distance,imageUrl;

        protected Bitmap doInBackground(String...urls){
            name = urls[0];
            address = urls[1];
            distance = urls[2];
            imageUrl = urls[3];
            Bitmap logo = null;
            try{
                InputStream is = new URL(imageUrl).openStream();
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){
                e.printStackTrace();
            }
            return logo;
        }
        protected void onPostExecute(Bitmap result){

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            mDB.addData(name,address,distance,encoded);
            count ++;
            if(count == sizeData){


                waiting_dialog.dismiss();

                sizeData = tempsizeData + sizeData;
                Cursor data1 = mDB.getData();
                String nam[] = new String[sizeData];
                String add[] = new String[sizeData];
                String dis[] = new String[sizeData];
                String img[] = new String[sizeData];
                int i =0;
                while(data1.moveToNext()){
                    nam[i] = data1.getString(1);
                    add[i] = data1.getString(2);
                    dis[i] = data1.getString(3);
                    img[i] = data1.getString(4);
                    i++;
                }
                tempsizeData = sizeData;

                if(flag_loading == true && pagetoken == ""){flag_loading = true; }
                else{flag_loading = false;}
                CustomList customList = new CustomList(ListMapActivity.this, nam, add, dis,img);
                listview.setAdapter(customList);
                listview.setOnScrollListener(new AbsListView.OnScrollListener() {

                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    public void onScroll(AbsListView view, int firstVisibleItem,
                                         int visibleItemCount, int totalItemCount) {

                        if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                        {
                            if(flag_loading == false)
                            {
                                flag_loading = true;
                                try {
                                    start_dialog();
                                    GET_List("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+","+lon+"&radius=" + radius + "&type="+typeSearch+"&keyword=&key=AIzaSyDLk0QrEMZxgUBO_PTZezOaH4er8xb9Mh0&pagetoken="+pagetoken);
                                } catch (IOException e) {
                                    waiting_dialog.dismiss();
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });



            }
        }
    }

    private void start_dialog(){
        waiting_dialog = new ProgressDialog(ListMapActivity.this);
        waiting_dialog.setMessage("loading");
        waiting_dialog.setIndeterminate(true);
        waiting_dialog.setCancelable(false);
        waiting_dialog.show();
    }
}
