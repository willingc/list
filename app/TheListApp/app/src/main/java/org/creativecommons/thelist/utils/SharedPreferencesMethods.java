/* The List powered by Creative Commons

   Copyright (C) 2014 Creative Commons

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.creativecommons.thelist.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesMethods {
    public static final String TAG = SharedPreferencesMethods.class.getSimpleName();

    protected Context mContext;

    public SharedPreferencesMethods(Context context) {
        mContext = context;
    }

    //SharedPreferences Constants
    public static final String CATEGORY_PREFERENCE_KEY = "category";
    public static final String LIST_ITEM_PREFERENCE_KEY = "item";
    public static final String USER_ID_PREFERENCE_KEY = "id";
    public static final String USER_TOKEN_PREFERENCE_KEY = "skey";

    public static final String APP_PREFERENCES_KEY = "org.creativecommons.thelist.434932";

    //Add username, id, session token, category preferences, user item preferences

    //Add any sharedPreference
    public void SaveSharedPreference (String key, String value){
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    //RetrieveSharedPreference
    public JSONArray RetrieveSharedPreference (String key){
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        String value = sharedPref.getString(key, null);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(value);
        JsonArray array = element.getAsJsonArray();

        //Make usable as JSONArray
        List<String> catIds = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            catIds.add(array.get(i).getAsString());
        }

        return new JSONArray(catIds);
    }

    //Get User ID from SharedPreferences
    public String getUserId(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        if(sharedPref.contains(SharedPreferencesMethods.USER_ID_PREFERENCE_KEY)){
            String userID = sharedPref.getString(USER_ID_PREFERENCE_KEY, null);
            return userID;
        } else {
            return null;
        }
    }

    public int getUserItemCount(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);

        if(sharedPref.contains(SharedPreferencesMethods.LIST_ITEM_PREFERENCE_KEY)){
            String listOfValues = sharedPref.getString(LIST_ITEM_PREFERENCE_KEY, null);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(listOfValues);
            JsonArray array = element.getAsJsonArray();
            int size = array.size();
            return size;
        } else{
            return 0;
        }
    }

    //Remove single key in Preferences
    public void ClearSharedPreference(String key) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

    //Remove single value in Preferences
    public void RemoveUserItemPreference(String itemID) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        String listOfValues = sharedPref.getString(LIST_ITEM_PREFERENCE_KEY, null);
        Log.v("ITEM ID: ", itemID);
        //Convert from String to Array
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(listOfValues);
        JsonArray array = element.getAsJsonArray();
        Log.v("ARRAY FROM SHARED PREF: ", array.toString());

        for (int i = 0; i < array.size(); i++) {
            String singleItem = array.get(i).getAsString();
            if (singleItem.equals(itemID)) {
                Log.v("ITEM TO REMOVE IS: ", singleItem);
                array.remove(i);
            }
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LIST_ITEM_PREFERENCE_KEY, array.toString());
        editor.apply();
    }

    public void ClearTempPreferences(){
        ClearSharedPreference(CATEGORY_PREFERENCE_KEY);
        ClearSharedPreference(LIST_ITEM_PREFERENCE_KEY);
    }

    //Clear all sharedPreferences
    //TODO: add other keys like session token
    public void ClearAllSharedPreferences() {
        ClearSharedPreference(USER_ID_PREFERENCE_KEY);
        ClearSharedPreference(CATEGORY_PREFERENCE_KEY);
        ClearSharedPreference(LIST_ITEM_PREFERENCE_KEY);
    }

    //Create object to send in Category Volley Request
    public JSONObject createCategoryListObject() {
        //Create JSON Object
        JSONObject categoryListObject = new JSONObject();
        JSONArray userPreferences = RetrieveSharedPreference
                (CATEGORY_PREFERENCE_KEY);

        try {
            categoryListObject.put(CATEGORY_PREFERENCE_KEY, userPreferences);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return categoryListObject;
    }

    //Create object to send in User’s List Items Volley Request
    public JSONObject createUserItemsObject () {
        //Create JSON Object
        //TODO: remove this with real API
        JSONObject userItemObject = new JSONObject();
        JSONArray userPreferences = RetrieveSharedPreference
                (LIST_ITEM_PREFERENCE_KEY);
        JSONArray intPreferences = new JSONArray();

        for(int i = 0; i <userPreferences.length(); i++ ) {

            try {
                int item = Integer.valueOf(userPreferences.getString(i));
                intPreferences.put(i,item);

            } catch (JSONException e) {
                Log.v(TAG, e.getMessage());
            }
        }

        try {
            Log.v(TAG, intPreferences.toString());
            userItemObject.put(LIST_ITEM_PREFERENCE_KEY, intPreferences);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userItemObject;
    }

    //Retrieve Shared preferences as JSONArray
    public JSONArray RetrieveCategorySharedPreference (){
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        String value = sharedPref.getString(CATEGORY_PREFERENCE_KEY, null);

        //TODO: Switch to json library (JSONNNN)
        //Convert from String to Array
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(value);
        JsonArray array = element.getAsJsonArray();

        //Make usable as JSONArray
        List<Integer> catIds = new ArrayList<Integer>();
        for (int i = 0; i < array.size(); i++) {
            catIds.add(array.get(i).getAsInt());
        }

        return new JSONArray(catIds);
    }

    public String RetrieveUserToken(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(USER_TOKEN_PREFERENCE_KEY, null);
        return token;
    }

    //Retrieve User Item Preference
    public JSONArray RetrieveUserItemPreference() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        String value = sharedPref.getString(LIST_ITEM_PREFERENCE_KEY, null);

        if(value == null) {
            return new JSONArray();
        } else {
            //TODO: Switch to json library (JSONNNN)
            //Convert from String to Array
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(value);
            JsonArray array = element.getAsJsonArray();

            //Make usable as JSONArray
            List<Integer> itemIds = new ArrayList<Integer>();
            for (int i = 0; i < array.size(); i++) {
                itemIds.add(array.get(i).getAsInt());
            }
            return new JSONArray(itemIds);
        }
    }

}
