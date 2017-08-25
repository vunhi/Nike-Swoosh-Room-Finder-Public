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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class favoritesList extends AppCompatActivity {
    private ListView allFavorites; //Listview containing the list of all of the favorites
    private String choice, fpname, floorNumber; //Choice = user selected choice in the list, fpname = floorplan name, floorNumber = floor number
    private int floorCode; //floorCode - helps with parsing of data between this page and mapdata
    SharedPreferences favoritesList, favoritesValues; //SharedPreference files that store the favorited locations
    Set<String> defaultrooms, favRooms, userkeys, favUserKeys;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> listofFavorites;
    public mapdata data;
    public int choiceFloors; //Number of floors in chosen building
    Bundle dataContainer;
    ImageButton clearAll;
    TextView cancel, yes, no, prompt;
    Dialog clearDialog;

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
        setContentView(R.layout.activity_favorites);

        colorPreferences = getSharedPreferences("ColorPreferences", Context.MODE_PRIVATE);
        colorValue = colorPreferences.getString("color", "default");

        //Grab data from mapdata class
        Intent priorInt = getIntent();
        dataContainer = priorInt.getExtras();
        data = new mapdata();
        data = dataContainer.getParcelable("parse");

        //Sets font for page header
        TextView text = (TextView) findViewById(R.id.favoritestitle);
        Typeface myCustomfont = Typeface.createFromAsset(getAssets(), "fonts/newsgothiccondensedbold.ttf");
        text.setTypeface(myCustomfont);

        allFavorites = (ListView) findViewById(R.id.favoritesList);
        allFavorites.setEmptyView(findViewById(R.id.empty)); //Sets listview empty view

        listofFavorites = new ArrayList<String>();
        //Grab list of favorites stored in SharedPreference file
        favoritesList = getSharedPreferences("MyFavorites", Context.MODE_PRIVATE);
        defaultrooms = new HashSet();
        //List of actual location values saved (user did not enter in a value to save a location under)
        favRooms = favoritesList.getStringSet("favRooms", defaultrooms);
        //Adds "Floor" to saved locations to make the favorites list displayed more understandable
        //Mia Hamm 1 Flyknit -> Mia Hamm Floor 1 Flyknit
        for(String room : favRooms) {
            if (room.matches(".*\\d+.*")){
                String[] parts = room.split("\\d+",2);
                String part1 = parts[0].trim();
                String part2 = room.substring(part1.length() + 1).trim();
                String toAdd = part1 + " Floor " + part2;
                listofFavorites.add(toAdd);
                continue;
            }
            listofFavorites.add(room);
        }

        //List of user saved favorite values
        userkeys = new HashSet();
        favUserKeys = favoritesList.getStringSet("favUserKeys", userkeys);
        for(String keys: favUserKeys){
            listofFavorites.add(keys);
        }

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.listview_item_format, listofFavorites);
        allFavorites.setAdapter(arrayAdapter);

        //The first time the user utilizes the app a popup dialog will appear giving them instructions on
        //how to use the favorites activity page. After the first time, it will not appear again. To get the
        //instruction to appear again you must uninstall and reinstall the app
        if(firstTime()){
            Toast favoritesInstruction = Toast.makeText(getApplicationContext(), "Click to display the location. " +
                    "\nHold to remove from favorites", Toast.LENGTH_LONG);
            favoritesInstruction.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            favoritesInstruction.show();
        }

        //A separate SharedPreference value that stores the location values associated with the
        //user-entered favorite values
        favoritesValues = getSharedPreferences("UserEnteredValues", Context.MODE_PRIVATE);

        //What to do when the user clicks on a result from the favorites list
        allFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                floorplan.fromSearch = 1;
                //Gets object at the position and parses
                choice = (String) allFavorites.getAdapter().getItem(position);

                //If the choice in the list matches a user entered favorite key, we must grab the
                //corresponding location value that this alias is associated with from the SharedPreference file
                for(String keys: favUserKeys){
                    if(choice.matches(keys))
                        choice = favoritesValues.getString(keys, "default value");
                }

                //If the displayed entry has "Floor" in it, we must take it out to obtain the proper location
                //Choice: "Mia Hamm Floor 1 Flyknit" --> Location: "Mia Hamm 1 Flyknit"
                String[] parts = choice.split("\\d+",2);
                String part1 = parts[0].trim();
                String part2 = choice.substring(part1.length() + 1).trim();
                choice = part1.replace(" Floor","");

                for(int i = 0; i < data.numberofBuildings; ++i) {
                    //If the user clicks a floor
                    //If string reads ...Floor (number) [this will not work for buildings
                    //with over 100 floors, but that shouldn't be an issue for now. If we
                    //sell the app to a New York City developer, change this line!]
                    if (choice.contains(data.buildings.get(i).buildingName) &&
                            (part2.length() == 1 || part2.length() == 2)) {
                        fpname = data.buildings.get(i).buildingName;
                        floorCode = i;
                        choiceFloors = data.buildings.get(i).numberofFloors;
                        floorplan.floorNumber = part2;
                        floorplan.rmName = "";
                        //Sending to appropriate floor
                        floorplan.imageName = fpname.toLowerCase().replaceAll("\\s","") + part2;
                        floorplan.chosenRoomFromSearch = "";
                        floorplan.fromFavsFloor = 1;
                        break;
                    }
                    //If a room is selected
                    for(int j = 0; j < data.buildings.get(i).numberofFloors; ++j) {
                        for(int k = 0; k < data.buildings.get(i).floors.get(j).rooms.size(); ++k) {
                            if(part2.contains(data.buildings.get(i).floors.get(j).rooms.get(k).roomName)) {
                                fpname = data.buildings.get(i).buildingName;
                                floorCode = i;
                                floorNumber = Integer.toString(data.buildings.get(i).floors.get(j).level);
                                choiceFloors = data.buildings.get(i).numberofFloors;
                                floorplan.buildingselected = floorCode + 1; //Used in floorplan class
                                floorplan.setRoomfromSearch = 1;
                                floorplan.floorNumber = floorNumber;
                                floorplan.imageName =
                                        fpname.toLowerCase().replaceAll("\\s","") + floorNumber;
                                floorplan.rmName = floorplan.chosenRoomFromSearch =
                                        data.buildings.get(i).floors.get(j).rooms.get(k).roomName;
                                break;
                            }
                        }
                    }
                }

                //Grab necessary data from the selected value in the favorites list and go to the
                //Floorplan activity page
                Intent goToFloorPlan = new Intent(favoritesList.this, floorplan.class);
                goToFloorPlan.putExtras(dataContainer);
                floorplan.fpname = fpname;
                floorplan.spinnerNumber = floorCode;
                floorplan.numberOfFloors = choiceFloors;
                startActivity(goToFloorPlan);
            }

        });

        //When the user presses down on a value in the favorites list, it will be cleared from the list
        allFavorites.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
                int foundmatch = 0;
                choice = (String) allFavorites.getAdapter().getItem(position);
                //If the selection is a user-entered favorite key, we must remove both the key and
                //its corresponding location value from their respective SharedPreference files
                for(String keys: favUserKeys) {
                    if (choice.matches(keys)) {
                        foundmatch = 1;
                        favUserKeys.remove(keys);
                        SharedPreferences.Editor editor = favoritesList.edit();
                        editor.clear();
                        editor.putStringSet("favRooms", favRooms);
                        editor.putStringSet("favUserKeys", favUserKeys);
                        SharedPreferences.Editor editor2 = favoritesValues.edit();
                        editor2.remove(keys);
                        editor.commit();
                        editor2.commit();
                        break;
                    }
                }

                //If the selection is an actual location value, take out "Floor" from the value and
                //remove it from the SharedPreference file
                for (String room : favRooms) {
                    if (choice.contains("Floor")) {
                        choice = choice.replace("Floor ", "");
                    }
                    if (room.trim().matches(choice)) {
                        foundmatch = 1;
                        favRooms.remove(room);
                        SharedPreferences.Editor editor = favoritesList.edit();
                        editor.clear();
                        editor.putStringSet("favRooms", favRooms);
                        editor.putStringSet("favUserKeys",favUserKeys);
                        editor.commit();
                        break;
                    }
                }

                    if(foundmatch == 1){
                        Toast.makeText(favoritesList.this, choice + " was removed from favorites", Toast.LENGTH_SHORT).show();

                        //Now we must update the adapter populating the favorites listview since a value has been removed
                        arrayAdapter.clear();
                        ArrayList<String> listofFavorites = new ArrayList<String>();
                        favRooms = favoritesList.getStringSet("favRooms", defaultrooms);
                        for(String rooms : favRooms) {
                            if (rooms.matches(".*\\d+.*")){
                                String[] parts = rooms.split("\\d+",2);
                                String part1 = parts[0].trim();
                                String part2 = rooms.substring(part1.length() + 1).trim();
                                String toAdd = part1 + " Floor " + part2;
                                listofFavorites.add(toAdd);
                                continue;
                            }
                            listofFavorites.add(rooms);
                        }

                        favUserKeys = favoritesList.getStringSet("favUserKeys", userkeys);
                        for(String key: favUserKeys){
                            listofFavorites.add(key);
                        }
                        arrayAdapter = new ArrayAdapter<String>(favoritesList.this, R.layout.listview_item_format, listofFavorites);
                        allFavorites.invalidateViews();
                        allFavorites.setAdapter(arrayAdapter);
                    }
                return true;
            }
        });


        //User clicks on button to clear Favorites List
        clearAll = (ImageButton)findViewById(R.id.clearAll);
        createClearDialog();
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    clearDialog.show();
            }
        });

        //If cancel button in dialog popup is clicked then exit the dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDialog.dismiss();
            }
        });

        //If the no button in the dialog is clicked then exit the dialog
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDialog.dismiss();
            }
        });

        //If the yes button in the dialog is clicked then exit the dialog
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Empty list, nothing to clear
                if(arrayAdapter.isEmpty()){
                    Toast.makeText(favoritesList.this, "There is nothing to remove", Toast.LENGTH_SHORT).show();
                }
                //Clear entire list by clearing all SharedPreference files
                else{
                    SharedPreferences.Editor editor = favoritesList.edit();
                    editor.clear();
                    editor.commit();
                    SharedPreferences.Editor editor2 = favoritesValues.edit();
                    editor2.clear();
                    editor.commit();
                    arrayAdapter.clear();
                    allFavorites.invalidateViews();
                    allFavorites.setAdapter(arrayAdapter);
                }
                clearDialog.dismiss();
            }
        });
    }

    //Sets up color dialog
    private void createColorDialog(){
        colorDialog = new Dialog(favoritesList.this);
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

    //Sets up clear all pop up dialog
    private void createClearDialog(){
        clearDialog = new Dialog(favoritesList.this);
        clearDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (colorValue.equals("1") || colorValue.equals("default"))
            clearDialog.setContentView(R.layout.yesnodialog_green);
        else
            clearDialog.setContentView(R.layout.yesnodialog_orange);

        prompt = (TextView) clearDialog.findViewById(R.id.prompt);
        prompt.setText("Would you like to clear your Favorites?");
        cancel = (TextView) clearDialog.findViewById(R.id.cancel);
        yes = (TextView) clearDialog.findViewById(R.id.yes);
        no = (TextView) clearDialog.findViewById(R.id.no);
    }

    //Determines if this is the user's first time running the app
    private boolean firstTime(){
        SharedPreferences firstTime = getSharedPreferences("FirstTime", Context.MODE_PRIVATE);
        boolean isFirstTime = firstTime.getBoolean("isFirstTime", false);
        if(!isFirstTime){
            SharedPreferences.Editor editor = firstTime.edit();
            editor.putBoolean("isFirstTime",true);
            editor.commit();
        }
        return !isFirstTime;
    }

    //Setting up the bottom navigation toolbar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent theintent = new Intent(favoritesList.this, campus.class);
                    theintent.putExtras(dataContainer);
                    startActivity(theintent);
                    return true;

                case R.id.navigation_search:
                    Intent theintent2 = new Intent(favoritesList.this, masterSearchWithHeaders.class);
                    theintent2.putExtras(dataContainer);
                    startActivity(theintent2);
                    return true;

                case R.id.navigation_favorites:
                    return true;
            }
            return false;
        }
    };

    //When the user presses the back button to get back to the Favorites page then the Favorites icon in the
    //bottom navigation toolbar should be set
    @Override
    public void onResume(){
        //Bottom navigation toolbar. Set "FAVORITE" to checked
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(2).setChecked(true);

        //Code to allow user to change color preferences
        RelativeLayout universalLayout = (RelativeLayout)findViewById(R.id.universal_layout);
        View gradientBlock = (View) universalLayout.findViewById(R.id.gradientBlock); //Color block in theme
        colorPreferences = getSharedPreferences("ColorPreferences", Context.MODE_PRIVATE);
        colorValue = colorPreferences.getString("color", "default");
        editor = colorPreferences.edit();
        changeColors = (Button)findViewById(R.id.colors); //Button user clicks on to change color preference
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

        //Different icon colors - Green dark theme
        int[] greenColors = new int[]{
                ResourcesCompat.getColor(getResources(),R.color.colorPrimary, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
        };

        //Different icon colors - Orange dark theme
        int[] orangeColors = new int[]{
                ResourcesCompat.getColor(getResources(),R.color.NikeOrange, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
                ResourcesCompat.getColor(getResources(),R.color.iconColor, null),
        };


        switch (colorValue) {
            //Green Dark
            case "default":
            case "1":
                gradientBlock.setBackgroundResource(R.drawable.greengradient); //set color block to green
                ((TextView)colorDialog.findViewById(R.id.submit)).setBackgroundResource(R.drawable.greengradient); //set submit box color in dialog to green
                ColorStateList navigationColorStateList = new ColorStateList(iconStates, greenColors);//set icons in navigation toolbar to green when checked
                navigation.setItemTextColor(navigationColorStateList);
                navigation.setItemIconTintList(navigationColorStateList);
                break;

            //Orange Dark
            case "2":
                gradientBlock.setBackgroundResource(R.drawable.orangegradient); //set color block to orange
                ((TextView)colorDialog.findViewById(R.id.submit)).setBackgroundResource(R.drawable.orangegradient); //set submit box color in dialog to orange
                ColorStateList navigationColorStateList2 = new ColorStateList(iconStates, orangeColors);//set icons in navigation toolbar to orange when checked
                navigation.setItemTextColor(navigationColorStateList2);
                navigation.setItemIconTintList(navigationColorStateList2);
                break;
        }

        //User clicks on button to clear Favorites List
        clearAll = (ImageButton)findViewById(R.id.clearAll);
        createClearDialog();
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearDialog.show();
            }
        });

        //If cancel button in dialog popup is clicked then exit the dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDialog.dismiss();
            }
        });

        //If the no button in the dialog is clicked then exit the dialog
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDialog.dismiss();
            }
        });

        //If the yes button in the dialog is clicked then exit the dialog
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Empty list, nothing to clear
                if(arrayAdapter.isEmpty()){
                    Toast.makeText(favoritesList.this, "There is nothing to remove", Toast.LENGTH_SHORT).show();
                }
                //Clear entire list by clearing all SharedPreference files
                else{
                    SharedPreferences.Editor editor = favoritesList.edit();
                    editor.clear();
                    editor.commit();
                    SharedPreferences.Editor editor2 = favoritesValues.edit();
                    editor2.clear();
                    editor.commit();
                    arrayAdapter.clear();
                    allFavorites.invalidateViews();
                    allFavorites.setAdapter(arrayAdapter);
                }
                clearDialog.dismiss();
            }
        });

        super.onResume();
    }
}
