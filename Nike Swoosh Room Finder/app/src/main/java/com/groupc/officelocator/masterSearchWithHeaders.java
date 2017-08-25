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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.design.widget.BottomNavigationView;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Locale;

public class masterSearchWithHeaders extends AppCompatActivity {
    private ListView allSearchResults; //listview containing all search results
    private EditText searchBar; //Edittext where user can enter in a value to search
    private TextView title; //Page title
    private String choice, fpname, floorNumber;
    private int floorCode;
    public mapdata data;
    public int choiceFloors; //Number of floors in chosen building
    Bundle dataContainer;
    public int firstRun = 0; //Changed if app recreated on color change
    BottomNavigationView navigation;

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
        setContentView(R.layout.activity_mastersearch);
        floorplan.fromSearch = 1;
        colorPreferences = getSharedPreferences("ColorPreferences", Context.MODE_PRIVATE);
        colorValue = colorPreferences.getString("color", "default");
        navigation = (BottomNavigationView) findViewById(R.id.navigation);

        //Set font of page header
        title = (TextView) findViewById(R.id.searchTitle);
        Typeface myCustomfont = Typeface.createFromAsset(getAssets(), "fonts/newsgothiccondensedbold.ttf");
        title.setTypeface(myCustomfont);

        searchBar = (EditText) findViewById(R.id.searchBar);
        allSearchResults = (ListView) findViewById(R.id.searchList);
        allSearchResults.setEmptyView(findViewById(R.id.empty));

        Intent priorInt = getIntent();
        dataContainer = priorInt.getExtras();
        data = new mapdata();
        data = dataContainer.getParcelable("parse");

        //Populates the list of all buildings to display through mapdata, this is the list of all buildings,
        //floors and rooms displayed in the search page if the user hasn't entered in a value to search yet
        ArrayList<SearchItem> campusList = new ArrayList<masterSearchWithHeaders.SearchItem>();
        for (int i = 0; i < data.numberofBuildings; ++i){
            campusList.add(new BuildingName(data.buildings.get(i).buildingName));
            for(int j = 0; j < data.buildings.get(i).floors.size(); ++j) {
                for(int k = 0; k < data.buildings.get(i).floors.get(j).rooms.size(); ++k)
                    campusList.add(new RoomName(data.buildings.get(i).floors.get(j).rooms.get(k).roomName));
            }
        }

        //Set adapter
        final CampusAdapter adapter = new CampusAdapter(this, campusList);
        allSearchResults.setAdapter(adapter);
        allSearchResults.setTextFilterEnabled(true);

        //What to do when the user clicks on a search result
        allSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //Gets object at the position
                SearchItem object = (SearchItem) allSearchResults.getItemAtPosition(position);
                choice = object.getName(); //Gets the name of the object at the position
                for(int i = 0; i < data.numberofBuildings; ++i){
                    //Section header choice
                    if(choice.contains(data.buildings.get(i).buildingName)){
                        fpname = data.buildings.get(i).buildingName;
                        floorCode = i;
                        choiceFloors = data.buildings.get(i).numberofFloors;
                        floorplan.floorNumber = "0";
                        floorplan.rmName = "";
                        //1st floor default
                        floorplan.imageName = fpname.toLowerCase().replaceAll("\\s","") + "1";
                        floorplan.chosenRoomFromSearch = "";
                        break;
                    }
                    for(int j = 0; j < data.buildings.get(i).numberofFloors; ++j) {
                        //Room choice
                        for(int k = 0; k < data.buildings.get(i).floors.get(j).rooms.size(); ++k) {
                            if(choice.contains(data.buildings.get(i).floors.get(j).rooms.get(k).roomName)) {
                                fpname = data.buildings.get(i).buildingName;
                                floorCode = i;
                                floorNumber = Integer.toString(data.buildings.get(i).floors.get(j).level);
                                choiceFloors = data.buildings.get(i).numberofFloors;
                                floorplan.buildingselected = floorCode + 1; //Used in floorplan class
                                floorplan.setRoomfromSearch = 1;
                                floorplan.floorNumber = floorNumber;
                                floorplan.imageName =
                                        fpname.toLowerCase().replaceAll("\\s","") + floorNumber;
                                floorplan.rmName = choice;
                                floorplan.chosenRoomFromSearch = choice;
                                break;
                            }
                        }
                    }
                }

                Intent goToFloorPlan = new Intent(masterSearchWithHeaders.this, floorplan.class);

                goToFloorPlan.putExtras(dataContainer);
                floorplan.fpname = fpname;
                floorplan.spinnerNumber = floorCode;
                floorplan.numberOfFloors = choiceFloors;

                startActivity(goToFloorPlan);
            }

        });

        //What to do when the user enters input into the search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If the user enters in text, filter the result
                if(adapter != null)
                    adapter.getFilter().filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        colorSet();
    }

    //Sets up color dialog
    private void createColorDialog(){
        colorDialog = new Dialog(masterSearchWithHeaders.this);
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

    //WHERE WE DEFINE THE SEARCH ITEMS THAT WILL POPULATE THE SEARCH RESULT LISTVIEW
    //Search result items can be either a section header or a regular search result
    public interface SearchItem {
        public boolean isSection();
        public String getName();
    }

    //Listview Section Headers - Buildings
    public class BuildingName implements SearchItem {
        private final String name;
        public BuildingName(String name) {this.name = name;}
        public String getName() {return name;}
        @Override
        public boolean isSection() {return true;}
    }

    //Listview Nonsection-Header results - Rooms
    public class RoomName implements SearchItem {
        public final String name;
        public RoomName(String name) {this.name = name;}
        public String getName() {return name;}
        @Override
        public boolean isSection() {return false;}
    }

    //Custom "CampusAdapter"
    public class CampusAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<SearchItem> searchItem;
        private ArrayList<SearchItem> originalSearchItem;
        public CampusAdapter(Context context, ArrayList<SearchItem> searchItem) {
            this.context = context;
            this.searchItem = searchItem;
        }

        //Supporting methods
        @Override
        public int getCount() {return searchItem.size();}
        @Override
        public Object getItem(int position) {return searchItem.get(position);}
        @Override
        public long getItemId(int position) {return position;}

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (searchItem.get(position).isSection()) {
                //If the item in the listview search result is a section header...
                if(colorValue.equals("1") || colorValue.equals("default"))
                    convertView = inflater.inflate(R.layout.list_header_green, parent, false);
                else
                    convertView = inflater.inflate(R.layout.list_header_orange, parent,false);
                TextView headerName = (TextView) convertView.findViewById(R.id.searchHeader);
                headerName.setText(((BuildingName) searchItem.get(position)).getName());
            }
            else
            {
                //If the item in the listview search result is not a section header...
                convertView = inflater.inflate(R.layout.list_items, parent, false);
                final TextView searchResult = (TextView) convertView.findViewById(R.id.searchItems);
                searchResult.setText(((RoomName) searchItem.get(position)).getName());
            }
            return convertView;
        }


        //Filtering the search results
        public Filter getFilter()
        {
            Filter filter = new Filter() {
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence userEnteredString, FilterResults results) {
                    searchItem = (ArrayList<SearchItem>) results.values;
                    notifyDataSetChanged();
                }

                @SuppressWarnings("null")
                @Override
                protected FilterResults performFiltering(CharSequence userEnteredString) {
                    FilterResults results = new FilterResults();
                    ArrayList<SearchItem> filteredArrayList = new ArrayList<SearchItem>();

                    if(originalSearchItem == null || originalSearchItem.size() == 0)
                        originalSearchItem = new ArrayList<SearchItem>(searchItem);

                    //if userEnteredString is null then we return the original value, else return the filtered value
                    if(userEnteredString == null || userEnteredString.length() == 0){
                        results.count = originalSearchItem.size();
                        results.values = originalSearchItem;
                    }
                    else {
                        userEnteredString = userEnteredString.toString().toLowerCase(Locale.ENGLISH);
                        for (int i = 0; i < originalSearchItem.size(); i++)
                        {
                            String name = originalSearchItem.get(i).getName().toLowerCase(Locale.ENGLISH);
                            if(name.contains(userEnteredString.toString()))
                                filteredArrayList.add(originalSearchItem.get(i));
                        }
                        results.count = filteredArrayList.size();
                        results.values = filteredArrayList;
                    }
                    return results;
                }
            };
            return filter;
        }
    }

    //Setting up the bottom navigation toolbar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Intent theintent = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    theintent = new Intent(masterSearchWithHeaders.this, campus.class);
                    theintent.putExtras(dataContainer);
                    break;

                case R.id.navigation_search:
                    return true;

                case R.id.navigation_favorites:
                    theintent = new Intent(masterSearchWithHeaders.this, favoritesList.class);
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
                ColorStateList navigationColorStateList = new ColorStateList(iconStates, greenColors); //set icons in navigation toolbar to green when checked
                navigation.setItemTextColor(navigationColorStateList);
                navigation.setItemIconTintList(navigationColorStateList);
                break;

            //Orange Dark
            case "2":
                gradientBlock.setBackgroundResource(R.drawable.orangegradient); //set color block to orange
                ((TextView)colorDialog.findViewById(R.id.submit)).setBackgroundResource(R.drawable.orangegradient); //set submit box color in dialog to orange
                ColorStateList navigationColorStateList2 = new ColorStateList(iconStates, orangeColors); //set icons in navigation toolbar to orange when checked
                navigation.setItemTextColor(navigationColorStateList2);
                navigation.setItemIconTintList(navigationColorStateList2);
                break;
        }
    }

    //When the user presses the back button to get back to the Search page then the Search icon in the
    //bottom navigation toolbar should be set
    @Override
    public void onResume(){
        //Bottom navigation toolbar. Set "SEARCH" to checked
        //navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(1).setChecked(true);

        if(firstRun==1) {
            recreate();
        }
        else
            firstRun = 1;
        super.onResume();
    }
}
