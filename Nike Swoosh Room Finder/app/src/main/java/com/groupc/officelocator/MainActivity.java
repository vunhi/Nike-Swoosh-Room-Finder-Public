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
/*
Initial application setup. Inludes XML data file parser and splash screen setup.
 */
public class MainActivity extends AppCompatActivity {

    public mapdata data; //Populated by all data in assets file, data.xml; passed to other app activities
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

        //Sets up and calls parser function.
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

    //Data parser.
    private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException {

        int eventType = parser.getEventType();

        //Temporary variables with which to fill data structure.
        mapdata.building currentBuilding = null;
        mapdata.floor currentFloor = null;
        mapdata.room currentRoom = null;

        while(eventType != XmlPullParser.END_DOCUMENT) {

            String name; //Temporary variable to store parsed label.

            switch(eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;

                //Cases for distinct data-type possibilities.
                case XmlPullParser.START_TAG:
                    name = parser.getName();

                    if(name.equals("campus")) {
                        data.buildings = new ArrayList<>();
                        data.campusName = parser.getAttributeValue(null, "campusName");
                        data.numberofBuildings = Integer.parseInt(parser.getAttributeValue(null, "numberOfBuildings"));
                    }

                    else if(name.equals("building") && currentBuilding == null) {
                        currentBuilding = new mapdata.building();
                        currentBuilding.floors = new ArrayList<>();
                        currentBuilding.buildingName = parser.getAttributeValue(null, "buildingName");
                        currentBuilding.numberofFloors = Integer.parseInt(parser.getAttributeValue(null, "numberOfFloors"));
                    }

                    else if(currentBuilding != null) {
                        if(name.equals("floor") && currentFloor == null) {
                            currentFloor = new mapdata.floor();
                            currentFloor.level = Integer.parseInt(parser.getAttributeValue(null,"level"));
                            currentFloor.rooms = new ArrayList<>();
                            currentFloor.image = parser.getAttributeValue(null, "src");
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

                //At the end of the tag, populates the data structure with stored values from data within tags.
                case XmlPullParser.END_TAG:
                    name = parser.getName();

                    if(name.equalsIgnoreCase("building") && currentBuilding != null) {
                        data.buildings.add(currentBuilding);
                        currentBuilding = null;
                    }

                    else if(name.equalsIgnoreCase("floor") && currentFloor != null && currentBuilding != null) {
                        currentBuilding.floors.add(currentFloor);
                        currentFloor = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
    }
}
