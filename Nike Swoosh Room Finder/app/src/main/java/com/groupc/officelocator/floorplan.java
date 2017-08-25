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
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;

public class floorplan extends AppCompatActivity {

    RelativeLayout relativeLayout;
    public static int buildingselected = 0; //Tracks which building
    public static int setRoomfromSearch = 0; //Determines if a room was chosen in Search
    public static int floorselected = 0; //Determines if a floor number was chosen in Search or through Spinner
    public static int fromSearch = 0; //Determines if the previous page was Search before coming to the floorplan page
    public static int fromCampus = 0; //Determines if the previous page was the campusorange page
    public static int fromFavsFloor = 0; //Determines if previous page was Favorites floor selection

    ImageView buildingLocation, spinner2drop, selectedRoom;
    private Spinner chooseFloor, chooseRoom;
    ImageButton favorite, maplocationbut, share;
    File imagePath, imagePath2;

    Dialog dialog, favoriteDialog, favoriteSecondDialog;
    TextView cancel, floorplanname, roomName, favoriteyes, favoriteno, favoritecancel, favoritesecondcancel, favoritesubmit, roomspinnerprompt;
    public static String addtofavorite, display, savetofavorites;
    public static String fpname, imageName, floorNumber, chosenRoomFromSearch, rmName = "";
    public static int roomID, spinnerNumber, numberOfFloors, modifiedfavorite;
    EditText favoriteinput;
    public int firstRun = 0; //Changed if app recreated on color change

    public mapdata data;
    Bundle dataContainer;

    SharedPreferences favoritesList, favoritesValues;
    Set<String> favRooms, favUserKeys;

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
        colorPreferences = getSharedPreferences("ColorPreferences", Context.MODE_PRIVATE);
        colorValue = colorPreferences.getString("color", "default");

        //Sets appropriate floorplan layout and spinners.
        setup();

        //Floor spinner listener setup
        floorSet();
    }

    private void select() {
        if (floorNumber.equals("Choose a floor")) {
            //Disable Room spinner
            chooseRoom.setSelection(0, true);
            chooseRoom.setEnabled(false);
            chooseRoom.setClickable(false);
            floorplanname.setText(fpname);
            //If user tries to click on the room spinner, a message pops up telling them to select a floor first
            Button instructionbutton = (Button) findViewById(R.id.instructionButton);
            instructionbutton.setVisibility(View.VISIBLE);
            instructionbutton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Toast.makeText(floorplan.this, "Please select a floor first", Toast.LENGTH_SHORT).show();
                }
            });
        }
        chooseRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = ((TextView) view).getText().toString();
                if (selection.equals("Choose a room")) {
                    //Make room header + markers invisible
                    rmName = "";
                    roomName.setVisibility(View.INVISIBLE);
                    selectedRoom = (ImageView) findViewById(roomID);
                    selectedRoom.setVisibility(View.INVISIBLE);
                    return;
                }
                rmName = selection;

                roomName.setVisibility(View.VISIBLE);
                roomName.setText(selection);

                //Clear existing room markers
                clearMarkers(floorNumber);

                //Sets new marker
                selection = selection.toLowerCase().replaceAll("\\s", "");
                roomID = getResources().getIdentifier(selection, "id", getPackageName());
                selectedRoom = (ImageView) findViewById(roomID);
                if (colorValue.equals("1") || colorValue.equals("default"))
                    selectedRoom.setColorFilter(getResources().getColor(R.color.colorPrimary));
                else
                    selectedRoom.setColorFilter(getResources().getColor(R.color.NikeOrange));
                selectedRoom.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void floorSet() {
        final String previousFloor = floorNumber;

        //What to do when the user clicks on a choice for the first spinner for choosing floors
        chooseFloor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                roomName.setVisibility(View.INVISIBLE);

                //Set floor choice
                floorNumber = ((TextView) view).getText().toString();

                //Clearing room variable
                rmName = "";

                if (floorNumber.equals("Choose a floor")) {
                    //Disable Room spinner
                    chooseRoom.setSelection(0, true);
                    chooseRoom.setEnabled(false);
                    chooseRoom.setClickable(false);
                    floorplanname.setText(fpname);
                    //If user tries to click on the room spinner, a message pops up telling them to select a floor first
                    Button instructionbutton = (Button) findViewById(R.id.instructionButton);
                    instructionbutton.setVisibility(View.VISIBLE);
                    instructionbutton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Toast.makeText(floorplan.this, "Please select a floor first", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //Clear existing room markers
                    clearMarkers(previousFloor);
                    return;
                }

                chooseRoom.setEnabled(true);
                chooseRoom.setClickable(true);

                floorselected = Integer.parseInt(floorNumber);

                //Flag reset for setup function
                fromSearch = 0;
                setRoomfromSearch = 0;
                //Sets new floorplan name
                imageName = fpname.toLowerCase().replaceAll("\\s", "") + floorNumber;
                //Creates new layout based on new floor #
                setup();

                //Sets floor spinner to appropriate floor
                chooseFloor.setSelection(floorselected, true);
                chooseFloor.setSelected(true);

                //Make room spinners appear
                spinner2drop.setVisibility(View.VISIBLE);
                chooseRoom.setVisibility(View.VISIBLE);
                roomspinnerprompt.setVisibility(View.VISIBLE);

                //Set room spinner list based on floor selected
                List<String> spinnerArray = new ArrayList<String>();
                spinnerArray.add("Choose a room");
                for (int j = 0; j < data.numberofBuildings; ++j) {
                    for (int k = 0; k < data.buildings.get(j).floors.size(); ++k) {
                        if (buildingselected == (j + 1) && data.buildings.get(j).floors.get(k).level == floorselected) {
                            for (int m = 0; m < data.buildings.get(j).floors.get(k).rooms.size(); ++m) {
                                spinnerArray.add(data.buildings.get(j).floors.get(k).rooms.get(m).roomName);
                            }
                            //If color theme is 1/default, use green spinner layouts
                            if (colorValue.equals("1") || colorValue.equals("default")) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(floorplan.this, R.layout.spinner_layout_green, spinnerArray);
                                adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout_green);
                                chooseRoom.setAdapter(adapter);
                            }
                            //If color theme is 2, use orange spinner layouts
                            else {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(floorplan.this, R.layout.spinner_layout_orange, spinnerArray);
                                adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout_orange);
                                chooseRoom.setAdapter(adapter);
                            }
                            break;
                        }
                    }
                }
                chooseRoom.setSelected(false);
                chooseRoom.setSelection(0, true);

                select();

                //Resets floor spinner listener
                floorSet();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setup() {
        setContentView(getResources().getIdentifier(imageName, "layout", this.getPackageName()));
        if (fromCampus == 1)
            rmName = "";
        fromCampus = 0;

        //Zoom layout - allows user to pinch-zoom on campus map
        ZoomLayout myZoomView = new ZoomLayout(floorplan.this);
        relativeLayout = (RelativeLayout) findViewById(R.id.zoom);
        relativeLayout.addView(myZoomView);

        //Pulling map data on entry into the activity
        data = new mapdata();
        Intent goToFloorPlan = getIntent();
        dataContainer = goToFloorPlan.getExtras();
        data = dataContainer.getParcelable("parse");

        spinner2drop = (ImageView) findViewById(R.id.imageView10); //Dropdown arrow for 2nd spinner
        roomspinnerprompt = (TextView) findViewById(R.id.roomSpinnerTitle);

        //Room title on the floorplan page
        roomName = (TextView) findViewById(R.id.roomName);
        Typeface myCustomfont = Typeface.createFromAsset(getAssets(), "fonts/newsgothiccondensedbold.ttf");
        roomName.setTypeface(myCustomfont);

        //If a room was chosen through search, must cause the room title to appear since by default it doesn't until you choose the
        //room through the second spinner
        if (setRoomfromSearch == 1) {
            roomName.setText(rmName);
            roomName.setVisibility(View.VISIBLE);
        }

        //Setting floor plan title name + font style
        floorplanname = (TextView) findViewById(R.id.floorPlanName);
        floorplanname.setTypeface(myCustomfont);

        //Set appropriate color for room markers based on user's color preference
        if (!rmName.equals("") && fromSearch == 1 && fromFavsFloor == 0) {
            String tempName = rmName.toLowerCase().replaceAll("\\s", "");
            roomID = getResources().getIdentifier(tempName, "id", getPackageName());
            selectedRoom = (ImageView) findViewById(roomID);
            if (colorValue.equals("1") || colorValue.equals("default"))
                selectedRoom.setColorFilter(getResources().getColor(R.color.colorPrimary));
            else
                selectedRoom.setColorFilter(getResources().getColor(R.color.NikeOrange));
            selectedRoom.setVisibility(View.VISIBLE);
        }

        if(floorNumber.equals("Choose a floor"))
            floorplanname.setText(fpname);
        else {
            if (Integer.parseInt(floorNumber) == 0) { //For those without a floor number ("Mia Hamm"/"Tiger Woods"
                //the default is the first floor
                floorNumber = "1";
            }
            floorplanname.setText(fpname + " Floor " + floorNumber);
        }

        buildingselected = spinnerNumber + 1;

        //Creating the two spinner drop down menus that choose the floor and rooms
        //Choosing a floor in the first spinner causes the second spinner to be visible
        //The choice of the floor also determines the choices of rooms for the second spinner
        chooseFloor = (Spinner) findViewById(R.id.floorSelector);
        chooseRoom = (Spinner) findViewById(R.id.roomSelector);

        //First spinner - Choosing which floor
        List<String> list = new ArrayList<String>();
        list.add("Choose a floor");
        for (int i = 1; i <= numberOfFloors; i++) {
            list.add(String.valueOf(i));
        }
        //If color theme is 1/default, use green spinner layouts
        if (colorValue.equals("1") || colorValue.equals("default")) {
            ArrayAdapter<String> numberAdapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown_layout_green, list);
            chooseFloor.setAdapter(numberAdapter);
        } else { //Else if color theme is 2, use orange spinner layouts
            ArrayAdapter<String> numberAdapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown_layout_orange, list);
            chooseFloor.setAdapter(numberAdapter);
        }
        chooseFloor.setSelected(false);
        chooseFloor.setSelection(0, true);

        //When coming from the search menu, the floor number is already chosen and the room can be chosen through the search
        //results. If they are we must set the spinners to reflect these predetermined choices
        if (fromSearch == 1) {
            //All search results have floor values set into them (if there is not one explicitly set, it gets sent
            //to the first floor
            if(floorNumber.equals("Choose a floor"))
                chooseFloor.setSelection(0,true);
            else{
                floorselected = Integer.parseInt(floorNumber);
                chooseFloor.setSelection(floorselected, true);
                chooseFloor.setSelected(true);
            }

            //Make the room spinner appear
            spinner2drop.setVisibility(View.VISIBLE);
            chooseRoom.setVisibility(View.VISIBLE);
            roomspinnerprompt.setVisibility(View.VISIBLE);

            //Set the list of choices for the room spinner based on the floor the user selected
            List<String> spinnerArray = new ArrayList<String>();
            spinnerArray.add("Choose a room");
            for (int i = 0; i < data.numberofBuildings; ++i) {
                for (int j = 0; j < data.buildings.get(i).floors.size(); ++j) {
                    if (buildingselected == (i + 1) && data.buildings.get(i).floors.get(j).level == floorselected) {
                        for (int k = 0; k < data.buildings.get(i).floors.get(j).rooms.size(); ++k) {
                            spinnerArray.add(data.buildings.get(i).floors.get(j).rooms.get(k).roomName);
                        }
                        if (colorValue.equals("1") || colorValue.equals("default")) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(floorplan.this, R.layout.spinner_layout_green, spinnerArray);
                            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout_green);
                            chooseRoom.setAdapter(adapter);
                        } else {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(floorplan.this, R.layout.spinner_layout_orange, spinnerArray);
                            adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout_orange);
                            chooseRoom.setAdapter(adapter);
                        }
                        break;
                    }
                }
            }
            chooseRoom.setSelected(false);
            chooseRoom.setSelection(0, true);

            //If the room was also chosen through the search result chosen, we choose that value to
            //appear in the second spinner for the rooms
            if (setRoomfromSearch == 1) {
                chooseRoom.setSelected(true);
                int selection = 0;
                if (chosenRoomFromSearch != null) {
                    for (int i = 0; i < data.numberofBuildings; ++i) {
                        for (int j = 0; j < data.buildings.get(i).floors.size(); ++j) {
                            for (int k = 0; k < data.buildings.get(i).floors.get(j).rooms.size(); ++k) {
                                if (chosenRoomFromSearch.matches(data.buildings.get(i).floors.get(j).rooms.get(k).roomName))
                                    selection = k;
                            }
                        }
                    }
                }
                //+1 to skip past 'choose a room'
                chooseRoom.setSelection(selection + 1, true);
                //Flag resets
                setRoomfromSearch = 0;
            }
            fromSearch = 0;
            fromFavsFloor = 0;
            select();
        }
        //Pop up dialog for building location
        createDialog();
        maplocationbut = (ImageButton) findViewById(R.id.buildinglocator);
        maplocationbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        //If cancel button in dialog popup is clicked
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //Bottom navigation toolbar. Set selected to false since the floorplan page is not an option in the toolbar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelected(false);

        createFavoriteDialog(); //Instantiates the popup dialog that asks the user if they would like to add a location to favorites
        favorite = (ImageButton) findViewById(R.id.favorite);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If no floor is chosen then the user is told to select a floor first; cannot add to favorites
                if (floorNumber.equals("Choose a floor")) {
                    Toast.makeText(floorplan.this, "Select a floor", Toast.LENGTH_SHORT).show();
                    favoriteDialog.dismiss();
                } else
                    favoriteDialog.show();
            }
        });

        //If cancel button in dialog popup is clicked then exit the dialog
        favoritecancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteDialog.dismiss();
            }
        });

        //If the no button in the dialog is clicked then exit the dialog
        favoriteno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteDialog.dismiss();
            }
        });

        //If the yes button in the favorite dialog is clicked then save the string to the favorites
        createSecondFavoriteDialog();

        favoritesList = getSharedPreferences("MyFavorites", Context.MODE_PRIVATE);
        favoritesValues = getSharedPreferences("UserEnteredValues", Context.MODE_PRIVATE);
        favRooms = new HashSet<>(favoritesList.getStringSet("favRooms", new HashSet<String>()));
        favUserKeys = new HashSet<>(favoritesList.getStringSet("favUserKeys", new HashSet<String>()));

        favoriteyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display = null;
                if (floorNumber.equals("0")) {
                    addtofavorite = fpname;
                    display = fpname;
                } else if (rmName.equals("")) {
                    addtofavorite = fpname + " " + Integer.toString(floorselected);
                    display = fpname + " Floor " + Integer.toString(floorselected);
                } else {
                    addtofavorite = fpname + " " + Integer.toString(floorselected) + " " + rmName;
                    display = fpname + " Floor " + Integer.toString(floorselected) + " " + rmName;
                }

                //Must check if the user has already added this location to their favorites as a...
                //...pure location value
                for (String room : favRooms) {
                    if (room.matches(addtofavorite)) {
                        Toast.makeText(floorplan.this, display + " was already added to favorites", Toast.LENGTH_SHORT).show();
                        favoriteDialog.dismiss();
                        return;
                    }
                }

                //... or as a user-entered key
                for (String key : favUserKeys) {
                    String value = favoritesValues.getString(key, "default value");
                    if (value.matches(addtofavorite)) {
                        Toast.makeText(floorplan.this, addtofavorite + " was already added to favorites as \"" + key + "\"", Toast.LENGTH_LONG).show();
                        favoriteDialog.dismiss();
                        return;
                    }
                }

                favoriteDialog.dismiss();
                favoriteSecondDialog.show();

                //If the location hasn't been added yet, ask the user what they would like to add it as (EditText)
                favoriteinput.setText(display);
                favoriteinput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //If the user enters in text
                        modifiedfavorite = 1;
                        favoriteinput.setOnKeyListener(new View.OnKeyListener() {
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                // If user hits "Enter" button"
                                if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    if (favoriteinput.getText().toString().trim().length() == 0 || favoriteinput.getText().toString().isEmpty()) {
                                        Toast instruction = Toast.makeText(getApplicationContext(), "Please enter in a value", Toast.LENGTH_SHORT);
                                        instruction.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                                        instruction.show();
                                        return false;
                                    }
                                    else if(isAlphaNumeric(favoriteinput.getText().toString().replaceAll("\\s", "")) == false) {
                                            Toast instruction = Toast.makeText(getApplicationContext(), "Please enter only alphanumeric characters", Toast.LENGTH_SHORT);
                                            instruction.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                                            instruction.show();
                                            return false;
                                        }
                                    else {
                                        savetofavorites = favoriteinput.getText().toString().trim();
                                        addUserFavorite(modifiedfavorite);
                                        return true;
                                    }
                                }
                                return false;
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                //Submits the value in the EditText to save it to the favorites list
                favoritesubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (favoriteinput.getText().toString().trim().length() == 0 || favoriteinput.getText().toString().isEmpty()) {
                            Toast instruction = Toast.makeText(getApplicationContext(), "Please enter in a value", Toast.LENGTH_SHORT);
                            instruction.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                            instruction.show();
                            return;
                        }
                        else if(isAlphaNumeric(favoriteinput.getText().toString().replaceAll("\\s", "")) == false) {
                            Toast instruction = Toast.makeText(getApplicationContext(), "Please enter only alphanumeric characters", Toast.LENGTH_SHORT);
                            instruction.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                            instruction.show();
                            return;
                        }
                        else {
                            savetofavorites = favoriteinput.getText().toString().trim();
                            addUserFavorite(modifiedfavorite);
                        }
                    }
                });
            }
        });

        //If cancel button in dialog popup is clicked then exit the dialog
        favoritesecondcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteSecondDialog.dismiss();
            }
        });

        //Sharing the map floorplan
        share = (ImageButton) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*if(Build.VERSION.SDK_INT>22){
                                  ActivityCompat.requestPermissions(floorplan.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
              }*/
              Bitmap floorplan = takeScreenshot(); //Takes a screenshot of the floorplan
                // Grab the "highlighted building" in the campus image
                ImageView highlighteddrawable = ((ImageView) dialog.findViewById(R.id.buildingLocation));
                Bitmap highlightedimage = ((BitmapDrawable) highlighteddrawable.getDrawable()).getBitmap();
                saveBitmaps(floorplan, highlightedimage); //Save both bitmap images to device external drive
                shareImages(); //Allow user to share these two images via Sharing Intent
            }
        });

        //Code to allow user to change color preferences
        colorSet();
    }

    //Takes a screenshot of the floorplan activity page
    public Bitmap takeScreenshot() {
        View screenshot = findViewById(android.R.id.content).getRootView();
        screenshot.setDrawingCacheEnabled(true);
        return screenshot.getDrawingCache();
    }

    //Saves the floorplan screenshot and highlighted building imageview to device's external storage
    public void saveBitmaps(Bitmap bitmap1, Bitmap bitmap2) {
        String screenshot = null;
        if (floorNumber.equals("0") || floorNumber.equals("Choose a floor")) {
            screenshot = fpname;
        } else if (rmName.equals("")) {
            screenshot = fpname + " Floor " + Integer.toString(floorselected);
        } else {
            screenshot = fpname + " Floor " + Integer.toString(floorselected) + " " + rmName;
        }
        imagePath = new File(getApplicationContext().getFilesDir().getPath() + "/" + screenshot + " Floor Plan.png");
        imagePath2 = new File(getApplicationContext().getFilesDir().getPath() + "/" + fpname + " Campus Map.png");
        FileOutputStream fos1, fos2;
        //Save both bitmap images to external storage
        try {
            fos1 = new FileOutputStream(imagePath);
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, fos1);
            fos1.flush();
            fos1.close();
            fos2 = new FileOutputStream(imagePath2);
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, fos2);
            fos2.flush();
            fos2.close();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }

    //Opens sharing intent in android device so user can share the floorplan + highlighted building image
    private void shareImages() {
        Uri uri = FileProvider.getUriForFile(floorplan.this, BuildConfig.APPLICATION_ID + ".provider",imagePath);
        Uri uri2 = FileProvider.getUriForFile(floorplan.this, BuildConfig.APPLICATION_ID + ".provider",imagePath2);
        //Add URI of both floorplan + highlighted building images
        ArrayList<Uri> Imageuris = new ArrayList<Uri>();
        Imageuris.add(uri);
        Imageuris.add(uri2);
        Intent sharingImages = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        sharingImages.setType("image/*");
        sharingImages.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sharing Nike Campus Location");
        sharingImages.putExtra(android.content.Intent.EXTRA_TEXT, "I would like to share this location with you.");
        sharingImages.putParcelableArrayListExtra(Intent.EXTRA_STREAM, Imageuris);
        sharingImages.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sharingImages.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //sharingImages.putExtra(Intent.EXTRA_STREAM, Imageuris);
        startActivity(Intent.createChooser(sharingImages, "Share via"));
    }

    //Clears existing room markers
    private void clearMarkers(String floorNumber) {
        for (int j = 0; j < data.numberofBuildings; ++j) {
            if (data.buildings.get(j).buildingName.equals(fpname)) {
                for (int k = 0; k < data.buildings.get(j).floors.size(); ++k) {
                    if(floorNumber.equals("Choose a floor"))
                        break;
                    else if (data.buildings.get(j).floors.get(k).level == Integer.parseInt(floorNumber)) {
                        for (int m = 0; m < data.buildings.get(j).floors.get(k).rooms.size(); ++m) {
                            String tempName = data.buildings.get(j).floors.get(k).rooms.get(m).roomName;
                            tempName = tempName.toLowerCase().replaceAll("\\s", "");
                            roomID = getResources().getIdentifier(tempName, "id", getPackageName());
                            selectedRoom = (ImageView) findViewById(roomID);
                            selectedRoom.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        }
    }

    //What to do with user-entered favorites input
    private void addUserFavorite(int modifiedfavorite) {
        //If user has entered in their own value
        if (modifiedfavorite == 1) {
            //Check if that alias has already been used, if it has exit out
            for (String userKey : favUserKeys) {
                if (userKey.matches(savetofavorites)) {
                    Toast.makeText(floorplan.this, "\"" + savetofavorites + "\" is already in your favorites", Toast.LENGTH_SHORT).show();
                    favoriteSecondDialog.dismiss();
                    return;
                }
            }

            //If user hasn't used this alias before, then grab the location value and store both the user entered
            //key and the location value in their respective SharedPreference values
            favUserKeys.add(savetofavorites);
            SharedPreferences.Editor editor = favoritesList.edit();
            editor.putStringSet("favUserKeys", favUserKeys);

            SharedPreferences.Editor editor2 = favoritesValues.edit();
            editor2.putString(savetofavorites, addtofavorite);
            editor.commit();
            editor2.commit();
            Toast.makeText(floorplan.this, savetofavorites + " was added to favorites", Toast.LENGTH_SHORT).show();
        } else { //If user did not enter in a value and wants to just save it as its location name
            //save the location to the appropriate SharedPreference File
            favRooms.add(addtofavorite);
            SharedPreferences.Editor editor = favoritesList.edit();
            editor.putStringSet("favRooms", favRooms);
            editor.commit();
            Toast.makeText(floorplan.this, addtofavorite + " was added to favorites", Toast.LENGTH_SHORT).show();
        }
        favoriteSecondDialog.dismiss();
        modifiedfavorite = 0;
        return;
    }

    //Sets up color dialog
    private void createColorDialog() {
        colorDialog = new Dialog(floorplan.this);
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

    //Sets up favorite pop up dialog
    private void createFavoriteDialog() {
        favoriteDialog = new Dialog(floorplan.this);
        favoriteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (colorValue.equals("1") || colorValue.equals("default"))
            favoriteDialog.setContentView(R.layout.yesnodialog_green);
        else
            favoriteDialog.setContentView(R.layout.yesnodialog_orange);

        favoritecancel = (TextView) favoriteDialog.findViewById(R.id.cancel);
        favoriteyes = (TextView) favoriteDialog.findViewById(R.id.yes);
        favoriteno = (TextView) favoriteDialog.findViewById(R.id.no);
    }

    //Sets up second favorite pop up dialog
    private void createSecondFavoriteDialog() {
        favoriteSecondDialog = new Dialog(floorplan.this);
        favoriteSecondDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (colorValue.equals("1") || colorValue.equals("default"))
            favoriteSecondDialog.setContentView(R.layout.favoriteseconddialog_green);
        else
            favoriteSecondDialog.setContentView(R.layout.favoriteseconddialog_orange);

        favoritesecondcancel = (TextView) favoriteSecondDialog.findViewById(R.id.cancel);
        favoritesubmit = (TextView) favoriteSecondDialog.findViewById(R.id.submit);
        favoriteinput = (EditText) favoriteSecondDialog.findViewById(R.id.savetofavorites);
    }

    //Come back to fix this perhaps after room class stores images
    //Sets up pop up dialog of highlighted building in campus image
    private void createDialog() {
        dialog = new Dialog(floorplan.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.imagedialog);
        buildingLocation = (ImageView) dialog.findViewById(R.id.buildingLocation);

        //If the color preference is 1, use the appropriate green images
        if (colorValue.equals("1") || colorValue.equals("default")) {
            String dialogImage = fpname.toLowerCase().replaceAll("\\s", "") + "highlighted";
            int imgid = getResources().getIdentifier(dialogImage, "drawable", getPackageName());
            buildingLocation.setImageResource(imgid);
        }
        //Else use the appropriate orange images
        else {
            String dialogImage = fpname.toLowerCase().replaceAll("\\s", "") + "orangehighlighted";
            int imgid = getResources().getIdentifier(dialogImage, "drawable", getPackageName());
            buildingLocation.setImageResource(imgid);
        }

        cancel = (TextView) dialog.findViewById(R.id.cancel);
    }

    //Setting up the bottom navigation toolbar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Intent theintent = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    theintent = new Intent(floorplan.this, campus.class);
                    theintent.putExtras(dataContainer);
                    break;

                case R.id.navigation_search:
                    theintent = new Intent(floorplan.this, masterSearchWithHeaders.class);
                    theintent.putExtras(dataContainer);
                    break;

                case R.id.navigation_favorites:
                    theintent = new Intent(floorplan.this, favoritesList.class);
                    theintent.putExtras(dataContainer);
                    break;
            }
            startActivity(theintent);
            return true;
        }

    };

    //Sets all color preferences in layout
    private void colorSet() {
        //Code to allow user to change color preferences
        RelativeLayout universalLayout = (RelativeLayout)findViewById(R.id.universal_layout);
        View gradientBlock = (View) universalLayout.findViewById(R.id.gradientBlock); //Color block in theme
        editor = colorPreferences.edit();
        changeColors = (Button)findViewById(R.id.colors); //Button user clicks on to change color preference
        createColorDialog();//Sets up color pop up dialog where user makes their color preference
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
                //Saves "spot" in room dropdown
                fromSearch = 1;
                recreate();
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

        switch (colorValue) {
            //Green Dark
            case "default":
            case "1":
                gradientBlock.setBackgroundResource(R.drawable.greengradient);//set color block to green
                ((TextView)colorDialog.findViewById(R.id.submit)).setBackgroundResource(R.drawable.greengradient); //set submit box color in dialog to green
                ((Spinner)findViewById(R.id.floorSelector)).setBackgroundResource(R.drawable.greenbgroundedcorners); //set floor spinner to green
                ((Spinner)findViewById(R.id.roomSelector)).setBackgroundResource(R.drawable.greenbgroundedcorners); //set room spinner to green
                break;

            //Orange Dark
            case "2":
                gradientBlock.setBackgroundResource(R.drawable.orangegradient);//set color block to orange
                ((TextView)colorDialog.findViewById(R.id.submit)).setBackgroundResource(R.drawable.orangegradient);//set submit box color in dialog to orange
                ((Spinner)findViewById(R.id.floorSelector)).setBackgroundResource(R.drawable.orangebgroundedcorners);//set floor spinner to orange
                ((Spinner)findViewById(R.id.roomSelector)).setBackgroundResource(R.drawable.orangebgroundedcorners); //set room spinner to orange
                break;
        }
    }

    public boolean isAlphaNumeric(String string){
        String pattern= "^[a-zA-Z0-9]*$";
        return string.matches(pattern);
    }

    //Ensures proper reset on back button navigation
    @Override
    public void onBackPressed() {
        fromSearch = 1;
        super.onBackPressed();
    }

    @Override
    public void onResume() {

        if(firstRun==1) {
            fromSearch = 1;
            recreate();
        }
        else
            firstRun = 1;

        super.onResume();
    }
}