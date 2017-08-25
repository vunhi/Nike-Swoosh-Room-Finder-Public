Portland State University
CS Capstone 2017
 
# How to add new floor plan data

## I. Adding a Building
1. Navigate to ...\OfficeLocator\app\src\main\res\drawable.

2. Add the floor plan images for the building you would like to add in png format. The floor plan images should be named "buildingname + floornumber.png" in lowercase, so for example if you wanted to add the 3 floors of the Tiger Wood Center you would add the 3 floor plan png images to the drawable director as tigerwoodscenter1.png, tigerwoodscenter2.png, and tigerwoodscenter3.png. 

3. Navigate to ...\OfficeLocator\app\src\main\assets\data.xml

4. Increment the numberOfBuildings variable by 1. 

5. Add the desired building by copying an entire <building> element and placing it in its appropriate order alphabetically in the list. 

6. In this <building> element, add the proper number of floors you would like to add to the numberOfFloors variable. 

7. Add each floor accordingly for each <floor> element. For each floor, the floor level gives the floor number, src gives the name of the floor plan png image for that floor, and the individual <room> elements are the room names that you wish to add. A more detailed description of how to add a floor to a building and rooms to a building are given in the sections below. 
  
Example:
   <building buildingName="Mia Hamm" numberOfFloors="2">
    <floor level="1" src="miahamm1">
        <room roomName="Air Max"/>
        <room roomName="Cachana"/>
        <room roomName="Flyknit"/>
        <room roomName="Grind"/>
        <room roomName="Mercurial Vapor"/>
    </floor>
    <floor level="2" src="miahamm2">
        <room roomName="Kobe Mamba"/>
        <room roomName="LunarCharge"/>
    </floor>
</building>

8. Now navigate your way to the activity_campus layout in ...\OfficeLocator\app\src\main\res\layout. 

9. To add a new campus building you must add a new button widget to the activity_campus xml layout. This can be done 1) through the Design tab by dragging a new Button widget from the Android Studio palette to the campus map and placing the button over the building image accordingly or 2) through the Text tab by copying the code of preexisting campus building button and changing its size and margin elements (layout_width, layout_height, layout_marginTop, layout_marginLeft) to match your new desired building button. Both methods will result in the same new code being added to the xml layout. So for example if we wanted to add a button for the Mia Hamm building we would add this to the activity_campus xml layout: 

<!-- Mia Hamm Button -->
<Button
    android:id="@+id/miahamm"
    android:background="@android:color/transparent"
    android:layout_width="10dip"
    android:layout_height="61dip"
    android:layout_marginTop="105dp"
    android:layout_marginLeft="24dp"/>

The button id should match the name of the building exactly but with no spaces and all lowercase. The height and width are the size of the button in the building and the margins tell the layout where to place buttons on the screen to match the campus map. The background is set to the android transprent color so that it does not cover the campus map image behind it. To preview the placement of your newly added button, you can use the Design tab to see the preview or click on the Preview tab when the Text tab is open. Make sure that the preview device settings in Android Studio is set to match the dpi of the phone you are trying to fit this program to. If it is not you can change the settings or add your own Android Virtual Device that matches your desired phone settings. 

## II. Adding a Floor to an Existing Building 

1. Navigate to ...\OfficeLocator\app\src\main\assets\data.xml 

2. Locate desired building; floors are a child element of a building, and should be placed immediately under the opening tag for the appropriate building, or under the closing tag of the prior floor of that building.

3. The open tag for a floor is i. Attribute floor level requires the floor number, in double quotes. ii. Atribute src requires the filename for the floor layout, in double quotes. a. Expected style for floor names is the name of the building in all lower case, followed by the floor number. There should be no spaces. iii. Ex. 

4. The closing tag for a floor is <\floor> 

5. Navigate to ...\OfficeLocator\app\src\main\res\layout 

6. Create a copy of the file activity_floorplan.xml in the directory; name the new file so as to match the filename from step 3ii, adding an .xml extension. i. Ex. floorname1.xml 

7. Open the created file, and navigate to design view. 

8. Select the floorPlanimage (ID of the image will be floorPlanImage, viewable in Properties). 

9. In Properties, edit srcCompat view to read: drawable/imagename i. imagename should match that entered in 3iii. Ex. floorname1 

10. Navigate to ...\OfficeLocator\app\src\main\res\drawable 11. Add the image file for the floor, matching the naming convention of 3iii.

## III. Adding a Room to an Existing Floor

1. Navigate to ...\OfficeLocator\app\src\main\assets\data.xml 

2. Locate desired building and floor; rooms are a child element of a floor. 

3. The tag for a room is i. Attribute room name should be written as one wishes to display, including following normal capitalization and spacing conventions, in double quotes. ii. Ex. <room roomName="Awesome Room/>. Rooms are added in alphabetical order. 

4. Navigate to ...\OfficeLocator\app\src\main\res\layout 

5. Locate the desired floor layout .xml file in which your room is to be placed. 

6. Open the file and navigate to design view. 

7. Use the Palette to drag and drop an ImageView onto the location of the room; this image will appear as a marker in the application when the room is selected.

8. When given the option, select the desired marker image from ...\OfficeLocator\app\src\main\res\drawable 

9. In Properties, edit the ID field of the ImageView and add the name of the room, all in lower case, with no spaces. i. Ex. awesomeroom 

10. In Properties, set visiblity of the ImageView to invisible.

By: Victor Diego, Nhi Vu