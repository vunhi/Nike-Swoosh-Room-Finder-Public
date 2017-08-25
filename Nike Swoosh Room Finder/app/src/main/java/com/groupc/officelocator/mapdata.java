/******************************************************************
 *      Copyright (c) 2017 Nhi Vu, Victor Diego, Tyler Wood       *
 *      Zachary Pfister-Shanders, Derek Keeton, Chris Norton      *
 *      Please see the file COPYRIGHT in the source               *
 *      distribution of this software for further copyright       *
 *      information and license terms.                            *
 +/****************************************************************/

package com.groupc.officelocator;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class mapdata implements Parcelable{

    static public class building implements Parcelable {
        public String buildingName;
        public int numberofFloors;
        public ArrayList<floor> floors;

        building() {
            buildingName = null;
            numberofFloors = 0;
            floors = null;
        }

        @Override
        public int describeContents() {
            return 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(buildingName);
            dest.writeInt(numberofFloors);
            dest.writeList(floors);
        }

        public building(Parcel source) {
            buildingName = source.readString();
            numberofFloors = source.readInt();
            floors = new ArrayList<floor>();
            source.readList(floors, getClass().getClassLoader());
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            @Override
            public building createFromParcel(Parcel source) {
                return new building(source);
            }
            @Override
            public building[] newArray(int size) {
                return new building[size];
            }
        };
    }

    static public class floor implements Parcelable {
        public ArrayList<room> rooms;
        public int level;
        public String image;

        floor() {
            level = 0;
            rooms = null;
            image = null;
        }

        @Override
        public int describeContents() {
            return 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(level);
            dest.writeList(rooms);
            dest.writeString(image);
        }

        public floor(Parcel source) {
            level = source.readInt();
            rooms = new ArrayList<room>();
            source.readList(rooms, getClass().getClassLoader());
            image = source.readString();
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            @Override
            public floor createFromParcel(Parcel source) {
                return new floor(source);
            }
            @Override
            public floor[] newArray(int size) {
                return new floor[size];
            }
        };
    }

    static public class room implements Parcelable {
        public String roomName;

        room() {roomName = null;}
        room(String newName) {this.roomName = newName;}

        @Override
        public int describeContents() {
            return 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(roomName);
        }

        public room(Parcel source) {
            roomName = source.readString();
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            @Override
            public room createFromParcel(Parcel source) {return new room(source);}
            @Override
            public room[] newArray(int size) {return new room[size];}
        };
    }

    public String campusName;
    public int numberofBuildings;
    public ArrayList<building> buildings;

    public mapdata() {
        campusName = null;
        numberofBuildings = 0;
        buildings = null;
    }

    @Override
    public int describeContents() {
        return 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(campusName);
        dest.writeInt(numberofBuildings);
        dest.writeList(buildings);
    }

    public mapdata(Parcel source) {
        campusName = source.readString();
        numberofBuildings = source.readInt();
        buildings = new ArrayList<>();
        source.readList(buildings, mapdata.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public mapdata createFromParcel(Parcel source) {
            return new mapdata(source);
        }
        @Override
        public mapdata[] newArray(int size) {
            return new mapdata[size];
        }
    };
}
