/* The List powered by Creative Commons

   Copyright (C) 2014, 2015 Creative Commons

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

package org.creativecommons.thelist.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.creativecommons.thelist.R;
import org.creativecommons.thelist.adapters.CategoryListAdapter;
import org.creativecommons.thelist.adapters.CategoryListItem;
import org.creativecommons.thelist.utils.ApiConstants;
import org.creativecommons.thelist.utils.ListApplication;
import org.creativecommons.thelist.utils.ListUser;
import org.creativecommons.thelist.utils.MessageHelper;
import org.creativecommons.thelist.utils.RequestMethods;
import org.creativecommons.thelist.utils.SharedPreferencesMethods;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CategoryListActivity extends ActionBarActivity {
    public static final String TAG = CategoryListActivity.class.getSimpleName();
    //Helper Methods
    RequestMethods mRequestMethods;
    SharedPreferencesMethods mSharedPref;
    MessageHelper mMessageHelper;
    ListUser mCurrentUser;

    protected Context mContext;

    //GET Request
    protected JSONArray mCategoryData;
    protected JSONArray mJsonCategories;
    //PUT request (if user is logged in)
    protected JSONObject mPutResponse;

    //GridView
    protected GridView mGridView;
    private List<CategoryListItem> mCategoryList = new ArrayList<>();
    protected CategoryListAdapter adapter;

    //UI Elements
    protected ProgressBar mProgressBar;
    protected Button mNextButton;

    // --------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);
        mContext = this;
        mMessageHelper = new MessageHelper(mContext);
        mRequestMethods = new RequestMethods(mContext);
        mSharedPref = new SharedPreferencesMethods(mContext);
        mCurrentUser = new ListUser(CategoryListActivity.this);

        //Google Analytics Tracker
        ((ListApplication) getApplication()).getTracker(ListApplication.TrackerName.GLOBAL_TRACKER);

        //Load UI Elements
        //mProgressBar = (ProgressBar) findViewById(R.id.category_progressBar);
        mGridView = (GridView) findViewById(R.id.categoryGrid);
        mNextButton = (Button) findViewById(R.id.nextButton);
        mNextButton.setVisibility(View.GONE);

        //Set List Adapter
        adapter = new CategoryListAdapter(this,mCategoryList);
        mGridView.setAdapter(adapter);

        //Category Selection
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView checkmarkView = (ImageView)view.findViewById(R.id.checkmark);
                if(mGridView.isItemChecked(position)) {
                    checkmarkView.setVisibility(View.VISIBLE);
                } else {
                    checkmarkView.setVisibility(View.GONE);
                }
                //Count how many items are checked: if at least 3, show Next Button
                SparseBooleanArray positions = mGridView.getCheckedItemPositions();
                int length = positions.size();
                int ItemsChecked = 0;
                if (positions.size() > 0) {
                    for (int i = 0; i < length; i++) {
                        if (positions.get(positions.keyAt(i))) {
                            ItemsChecked++;
                        }
                    }
                }
                if (ItemsChecked >= 3) {
                    mNextButton.setVisibility(View.VISIBLE);
                }
                else {
                    mNextButton.setVisibility(View.GONE);
                }
            }
        });

        //If Network Connection is available, Execute getDataTask
        if(mRequestMethods.isNetworkAvailable()) {
            //mProgressBar.setVisibility(View.VISIBLE);
            getCategoriesRequest();
        }
        else {
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }

        //Next Button: handle User’s Category Preferences
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If logged in, put to user profile, else create sharedPreference
                if((mCurrentUser.isTempUser())) {
                    Log.v(TAG, "User is logged in so no preferences are being saved");
                    //storeCategoriesRequest();
                } else {
                    SparseBooleanArray positions = mGridView.getCheckedItemPositions();
                    int length = positions.size();
                    //Array of user selected categories
                    List<Integer> userCategories = new ArrayList<>();

                    for(int i = 0; i < length; i++) {
                        int itemPosition = positions.keyAt(i);
                        CategoryListItem item = (CategoryListItem) mGridView.getItemAtPosition(itemPosition);
                        int id = item.getCategoryID();
                        userCategories.add(id);
                    }
                    //Save user categories to shared preferences
                    mSharedPref.SaveSharedPreference
                            (SharedPreferencesMethods.CATEGORY_PREFERENCE_KEY, userCategories.toString());
                }
                //Navigate to Random Activity
                //TODO: make category list activity into a fragment so there is no need for this
                if(mCurrentUser.isTempUser() && mSharedPref.getUserItemCount() < 3){
                    Intent intent = new Intent(CategoryListActivity.this, RandomActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CategoryListActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    } //onCreate

    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    //UPDATE LIST WITH CONTENT
    private void updateList() {
        //mProgressBar.setVisibility(View.INVISIBLE);
        if (mCategoryData == null) {
            mMessageHelper.showDialog(mContext, getString(R.string.error_title),
                    getString(R.string.error_message));
        }
        else {
            try {
                mJsonCategories = mCategoryData;
                for(int i = 0; i<mJsonCategories.length(); i++) {
                    JSONObject jsonSingleCategory = mJsonCategories.getJSONObject(i);
                    CategoryListItem categoryListItem = new CategoryListItem();
                    categoryListItem.setCategoryName(jsonSingleCategory.getString(ApiConstants.CATEGORY_NAME));
                    categoryListItem.setCategoryID(jsonSingleCategory.getInt(ApiConstants.CATEGORY_ID));

                    //Adding to array of List Items
                    mCategoryList.add(categoryListItem);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception Caught: ", e);
            }
            adapter.notifyDataSetChanged();
        }
    } //updateList

    //GET Categories from API
    private void getCategoriesRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);
        //Genymotion Emulator
        String url = ApiConstants.GET_CATEGORIES;

        JsonArrayRequest getCategoriesRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.v(TAG, "> getCategoriesRequest > onResponse: " + response);
                        //Handle Data
                        mCategoryData = response;
                        updateList();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "> getCategoriesRequest > onErrorResponse: " + error.getMessage());
                mMessageHelper.showDialog(mContext, getString(R.string.error_title),
                        getString(R.string.error_message));
            }
        });
        queue.add(getCategoriesRequest);
    } //getCategoriesRequest


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}