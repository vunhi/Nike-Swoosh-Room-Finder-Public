/******************************************************************
 *      Copyright (c) 2017 Nhi Vu, Victor Diego, Tyler Wood       *
 *      Zachary Pfister-Shanders, Derek Keeton, Chris Norton      *
 *      Please see the file COPYRIGHT in the source               *
 *      distribution of this software for further copyright       *
 *      information and license terms.                            *
 +/****************************************************************/

package com.groupc.officelocator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public Button search;
    public mapdata data;
    private static int SPLASH_TIME_OUT = 1500;
    private static String colorValue;
    SharedPreferences colorPreferences; //1 = Green, 2 = Orange
    ImageView splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the splash screen to the Nike Green or Nike Orange version depending on the user's preferences
        colorPreferences = getSharedPreferences("ColorPreferences", Context.MODE_PRIVATE);
        colorValue = colorPreferences.getString("color", "default");
        splash = (ImageView) findViewById(R.id.splash);
        if(colorValue.equals("1") || colorValue.equals("default"))
            splash.setImageResource(R.drawable.splashgreen);
        else
            splash.setImageResource(R.drawable.splashorange);

        //Initiates data parsing
        data = new mapdata();

        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            InputStream input = getApplicationContext().getAssets().open("data.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(input,null);
            parseXML(parser);
        }
        catch(XmlPullParserException|IOException e) {
            e.printStackTrace();
        }

        final Bundle dataContainer = new Bundle();
        dataContainer.putParcelable("parse", data);

        //Splash Screen delay
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent intent = new Intent(MainActivity.this, campus.class);
                intent.putExtras(dataContainer);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        }, SPLASH_TIME_OUT);
    }

    private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException {
        data.buildings = null; //Move this to a constructor later.
        int eventType = parser.getEventType();
        mapdata.building currentBuilding = null;
        mapdata.floor currentFloor = null;
        mapdata.room currentRoom;

        while(eventType != XmlPullParser.END_DOCUMENT) {
            String name;
            switch(eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;

                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if(name.equals("campus")) {
                        data.buildings = new ArrayList<>();
                        data.campusName = parser.getAttributeValue(null, "campusName");
                        data.numberofBuildings = Integer.parseInt(parser.getAttributeValue(null, "numberOfBuildings"));
                    } else if(name.equals("building") && currentBuilding == null) {
                        currentBuilding = new mapdata.building();
                        currentBuilding.floors = new ArrayList<>();
                        currentBuilding.buildingName = parser.getAttributeValue(null, "buildingName");
                        currentBuilding.numberofFloors = Integer.parseInt(parser.getAttributeValue(null, "numberOfFloors"));
                    } else if(currentBuilding != null) {
                        if(name.equals("floor") && currentFloor == null) {
                            currentFloor = new mapdata.floor();
                            currentFloor.level = Integer.parseInt(parser.getAttributeValue(null,"level"));
                            currentFloor.rooms = new ArrayList<>();
                            currentFloor.image = parser.getAttributeValue(null, "src");
                            //Example of how to load a drawable from string
                            /*Context context = getApplicationContext();
                            int id = context.getResources().getIdentifier(parser.getAttributeValue(null, "src"), "drawable", getPackageName());
                            Drawable d = getResources().getDrawable(id);
                            */
                        }
                        else if(currentFloor != null) {
                            if(name.equals("room")) {
                                currentRoom = new mapdata.room();
                                currentRoom.roomName = parser.getAttributeValue(null, "roomName");
                                currentFloor.rooms.add(currentRoom);
                            }
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if(name.equalsIgnoreCase("building") && currentBuilding != null) {
                        data.buildings.add(currentBuilding);
                        currentBuilding = null;
                    } else if(name.equalsIgnoreCase("floor") && currentFloor != null && currentBuilding != null) {
                        currentBuilding.floors.add(currentFloor);
                        currentFloor = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
    }
}
