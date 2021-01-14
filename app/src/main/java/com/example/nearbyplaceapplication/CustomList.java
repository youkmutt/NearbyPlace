package com.example.nearbyplaceapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String> {
    private String[] name;
    private String[] address;
    private String[] distance;
    private String[] image;
    private Activity context;

    public CustomList(Activity context, String[] name, String[] address, String[] distance, String[] image) {
        super(context, R.layout.lineitem, name);
        this.context = context;
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.image = image;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row=convertView;
        LayoutInflater inflater = context.getLayoutInflater();
        if(convertView==null)
            row = inflater.inflate(R.layout.lineitem, null, true);
        TextView txt1 = (TextView) row.findViewById(R.id.txt1);
        TextView txt2 = (TextView) row.findViewById(R.id.txt2);
        TextView txt3 = (TextView) row.findViewById(R.id.txt3);
        ImageView imageFlag = (ImageView) row.findViewById(R.id.imgURL);

        txt1.setText(name[position]);
        txt2.setText(address[position]);
        txt3.setText(distance[position]);

        byte[] decodedString = Base64.decode(image[position], Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageFlag.setImageBitmap(decodedByte);

//        imageFlag.setImageResource(imageid[position]);
        return  row;
    }
}
