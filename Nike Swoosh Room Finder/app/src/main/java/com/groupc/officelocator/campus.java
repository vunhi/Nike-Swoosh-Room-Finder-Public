/******************************************************************
 *      Copyright (c) 2017 Nhi Vu, Victor Diego, Tyler Wood       *
 *      Zachary Pfister-Shanders, Derek Keeton, Chris Norton      *
 *      Please see the file COPYRIGHT in the source               *
 *      distribution of this software for further copyright       *
 *      information and license terms.                            *
 +/****************************************************************/

package com.groupc.officelocator;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;

public class campus extends AppCompatActivity {
        RelativeLayout relativeLayout;

    public mapdata data; //Parses building data
    public ImageButton satelliteview; //Button to switch between campus maps and satellite views
    private static int globesetting = 0; //Flag used to switch between campus maps and satellite views
    ImageView mapimage; //Campus map variable
    Bundle dataContainer; //Used to pass data between activity pages

    //Change color feature variables
    Button changeColors; //Button user clicks on to change color themes
    private static String colorValue; //Variable used to determine which theme the user selects
    Dialog colorDialog; //Dialog that pops up allowing user to choose a color theme
    TextView colorCancel, colorSubmit, colorPrompt; //Different aspects of the Change color popup dialog
    CheckBox colorOrange, colorGreen; //Checkboxes user uses to select which color theme they want
    SharedPreferences colorPreferences; //Stores user color preference. 1 = Green, 2 = Orange
    SharedPreferences.Editor editor; //Editor to edit user color preferences in SharedPreference file

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_campus);

            //Zoom layout - allows user to pinch-zoom on campus map
            ZoomLayout myZoomView = new ZoomLayout(campus.this);
            relativeLayout = (RelativeLayout) findViewById(R.id.zoom);
            relativeLayout.addView(myZoomView);

            //Sets font for page header
            TextView text = (TextView) findViewById(R.id.campus);
            Typeface myCustomfont = Typeface.createFromAsset(getAssets(), "fonts/newsgothiccondensedbold.ttf");
            text.setTypeface(myCustomfont);

            //Sets up data with campus buttons on campus map
            Intent priorInt = getIntent();
            dataContainer = priorInt.getExtras();
            data = new mapdata();
            data = dataContainer.getParcelable("parse");

            if(data == null)
                android.os.Process.killProcess(android.os.Process.myPid());
            Button[] buttons = new Button[data.buildings.size()];

            for (int i = 0; i < data.buildings.size(); i++) {
                final int j = i;

                String temp = data.buildings.get(i).buildingName.toLowerCase().replaceAll("\\s", "");
                int id = getResources().getIdentifier(temp, "id", getPackageName());
                buttons[i] = (Button) findViewById(id);

                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent goToFloorPlan = new Intent(campus.this, floorplan.class);

                        goToFloorPlan.putExtras(dataContainer);
                        floorplan.fromSearch = 1;
                        floorplan.fromCampus = 1;
                        floorplan.floorNumber = "0";
                        floorplan.fpname = data.buildings.get(j).buildingName;
                        floorplan.imageName = data.buildings.get(j).buildingName.replaceAll("\\s","").toLowerCase() + 1;
                        floorplan.spinnerNumber = j;
                        floorplan.numberOfFloors = data.buildings.get(j).numberofFloors;
                        floorplan.buildingselected = j + 1;

                        startActivity(goToFloorPlan);
                    }
                });
            }

            //Grab user color preferences from SharedPreference file
            colorPreferences = getSharedPreferences("ColorPreferences", Context.MODE_PRIVATE);
            colorValue = colorPreferences.getString("color", "default");

            //CHANGING TO AND FROM SATELLITE VIEW
            mapimage = (ImageView)findViewById(R.id.campusmap);
            satelliteview = (ImageButton)findViewById(R.id.satelliteViewButton);

            satelliteview.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    colorValue = colorPreferences.getString("color", "default");
                    //Normal View -> satellite view
                    if(globesetting == 0){
                        mapimage.setImageResource(R.drawable.satellite);
                        satelliteview.setImageResource(R.drawable.globe2);
                        if(colorValue.equals("1") || colorValue.equals("default"))
                            satelliteview.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        else
                            satelliteview.setColorFilter(getResources().getColor(R.color.NikeOrange));
                        globesetting = 1;
                    }
                    //Satellite View -> Normal
                    else{
                        if(colorValue.equals("1") || colorValue.equals("default"))
                            mapimage.setImageResource(R.drawable.campus);
                        else
                            mapimage.setImageResource(R.drawable.campusorange);
                        satelliteview.setImageResource(R.drawable.globe);
                        satelliteview.setColorFilter(getResources().getColor(R.color.white));
                        globesetting = 0;
                    }
                }
            });
        }

    //Sets up color dialog
    private void createColorDialog(){
        colorDialog = new Dialog(campus.this);
        colorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        colorDialog.setContentView(R.layout.colordialog);
        colorCancel = (TextView) colorDialog.findViewById(R.id.cancel);
        colorPrompt = (TextView) colorDialog.findViewById(R.id.prompt);
        Typeface myCustomfont = Typeface.createFromAsset(getAssets(), "fonts/newsgothiccondensedbold.ttf");
        colorPrompt.setTypeface(myCustomfont);
        colorSubmit = (TextView) colorDialog.findViewById(R.id.submit);
        colorOrange = (CheckBox) colorDialog.findViewById(R.id.checkorange);
        colorGreen = (CheckBox) colorDialog.findViewById(R.id.checkneongreen);
    }

    //Setting up the bottom navigation toolbar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Intent theintent = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;

                case R.id.navigation_search:
                    theintent = new Intent(campus.this, masterSearchWithHeaders.class);
                    theintent.putExtras(dataContainer);
                    break;

                case R.id.navigation_favorites:
                    theintent = new Intent(campus.this, favoritesList.class);
                    theintent.putExtras(dataContainer);
                    break;
            }
            startActivity(theintent);
            return true;
        }

    };

    //When the user presses the back button to get back to the Home page then the Home icon in the
    //bottom navigation toolbar should be set
    @Override
    public void onResume(){

        //Bottom navigation toolbar. Set "HOME" to checked
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(0).setChecked(true);

        //Grab user color preferences from SharedPreference file
        colorPreferences = getSharedPreferences("ColorPreferences", Context.MODE_PRIVATE);
        colorValue = colorPreferences.getString("color", "default");

        //Code to allow user to change color preferences
        RelativeLayout universalLayout = (RelativeLayout)findViewById(R.id.universal_layout);
        View gradientBlock = (View) universalLayout.findViewById(R.id.gradientBlock); //Color block in theme
        editor = colorPreferences.edit();
        changeColors = (Button) findViewById(R.id.colors); //Button user clicks on to change color preference
        createColorDialog(); //Sets up color pop up dialog where user makes their color preference
        changeColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorDialog.show();
                //Default theme is "Nike Green" (SharedPreference value = 1, default)
                //Set the appropriate checkedboxes
                if (colorValue.equals("1") || colorValue.equals("default")) {
                    colorGreen.setChecked(true);
                    colorOrange.setChecked(false);
                }
                else {
                    colorOrange.setChecked(true);
                    colorGreen.setChecked(false);
                }
                editor.clear();
            }
        });

        //If user selects the Nike Orange checkbox
        colorOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                colorOrange.setChecked(true);
                colorGreen.setChecked(false);
                editor.putString("color", "2");
            }
        });

        //If user selects the Nike Green checkbox
        colorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                colorGreen.setChecked(true);
                colorOrange.setChecked(false);
                editor.putString("color", "1");
            }
        });

        //If user submits their preference change, we must change the screen
        colorSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                editor.commit();
                colorDialog.dismiss();
                finish();
                overridePendingTransition( 0, 0);
                startActivity(getIntent());
                overridePendingTransition( 0, 0);
            }
        });

        //If user cancels their color preference change, reset the checkboxes and exit the pop up dialog
        colorCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if (colorValue.equals("1") || colorValue.equals("default")){
                    colorGreen.setChecked(true);
                    colorOrange.setChecked(false);
                }
                else {
                    colorOrange.setChecked(true);
                    colorGreen.setChecked(false);
                }
                colorDialog.dismiss();
            }
        });

        //Setting colors for bottom navigation bar
        //Different icon states
        int[][] iconStates = new int[][]{
                new int[]{android.R.attr.state_checked},//checked state
                new int[]{-android.R.attr.state_checked},//unchecked state
                new int[]{} //default color
        };

        //Different icon colors - Green theme
        int[] greenColors = new int[]{
                ResourcesCompat.getColor(getResources(),R.color.colorPrimary, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
        };

        //Different icon colors - Orange theme
        int[] orangeColors = new int[]{
                ResourcesCompat.getColor(getResources(),R.color.NikeOrange, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
        };

        switch (colorValue) {
            //Green Theme
            case "default":
            case "1":
                mapimage.setImageResource(R.drawable.campus); //set campus map to white-gray version
                gradientBlock.setBackgroundResource(R.drawable.greengradient); //set color block to green
                ((TextView)colorDialog.findViewById(R.id.submit)).setBackgroundResource(R.drawable.greengradient); //set submit box color in dialog to green
                ColorStateList navigationColorStateList = new ColorStateList(iconStates, greenColors); //set icons in navigation toolbar to green when checked
                navigation.setItemTextColor(navigationColorStateList);
                navigation.setItemIconTintList(navigationColorStateList);
                break;

            //Orange Theme
            case "2":
                mapimage.setImageResource(R.drawable.campusorange); //set campus map to orange version
                gradientBlock.setBackgroundResource(R.drawable.orangegradient); //set color block to orange
                ((TextView)colorDialog.findViewById(R.id.submit)).setBackgroundResource(R.drawable.orangegradient); //set submit box color in dialog to orange
                ColorStateList navigationColorStateList2 = new ColorStateList(iconStates, orangeColors); //set icons in navigation toolbar to orange when checked
                navigation.setItemTextColor(navigationColorStateList2);
                navigation.setItemIconTintList(navigationColorStateList2);
                break;
        }

        //Set to satellite view when going back to the campusorange map from another page
        if(globesetting == 1){
            //Set to satellite view
            mapimage.setImageResource(R.drawable.satellite);
            satelliteview.setImageResource(R.drawable.globe2);
            if(colorValue.equals("1") || colorValue.equals("default"))
                satelliteview.setColorFilter(getResources().getColor(R.color.colorPrimary));
            else
                satelliteview.setColorFilter(getResources().getColor(R.color.NikeOrange));
            globesetting = 1;

            //Satellite view -> Normal
        }else{
            if(colorValue.equals("1") || colorValue.equals("default"))
                mapimage.setImageResource(R.drawable.campus);
            else
                mapimage.setImageResource(R.drawable.campusorange);
            satelliteview.setImageResource(R.drawable.globe);
            satelliteview.setColorFilter(getResources().getColor(R.color.white));
            globesetting = 0;
        }

        super.onResume();
    }

}

