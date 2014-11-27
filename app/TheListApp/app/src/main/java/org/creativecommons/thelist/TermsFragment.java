package org.creativecommons.thelist;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

public class TermsFragment extends Fragment {
    public static final String TAG = TermsFragment.class.getSimpleName();
//    RequestMethods requestMethods = new RequestMethods(getActivity());
//    SharedPreferencesMethods sharedPreferencesMethods = new SharedPreferencesMethods(getActivity());
//    ListUser mCurrentUser = new ListUser();

    protected Button mNextButton;
    protected CheckBox mCheckBox;

    //Interface with Activity
    TermsClickListener mCallback;

    //LISTENER
    public interface TermsClickListener {
        public void onTermsClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (TermsClickListener) activity;
        } catch(ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + activity.getString(R.string.terms_callback_exception_message));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terms, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();


        mCheckBox = (CheckBox)getView().findViewById(R.id.checkBox);
        mNextButton = (Button)getView().findViewById(R.id.nextButton);

        if(mCheckBox.isChecked()) {
            mNextButton.setVisibility(View.VISIBLE);
        } else {
            mNextButton.setVisibility(View.INVISIBLE);
        }

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: load new fragment


                //TODO: tell activity to send the user object + upload the photo
            }
        });



    }
}
