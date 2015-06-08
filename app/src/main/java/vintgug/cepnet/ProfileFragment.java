package vintgug.cepnet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    final static String USER_ID="user_id";
    TextView mUsernameTextView;
    TextView mEmailTextView;
    TextView mVerifiedTextView;
    TextView mStatusTextView;
    ParseUser currentUser;
    Toast mToast;

    Dialog mEditDialog;

    public static ProfileFragment newInstance(String user_id) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID,user_id);
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_profile, container, false);
        String currUserId=getArguments().getString(USER_ID);
        currentUser=getUserWithId(currUserId);
        if(currentUser==null){return rootView;}

        mUsernameTextView= (TextView)rootView.findViewById(R.id.UsernameTextView);
        mEmailTextView= (TextView)rootView.findViewById(R.id.EmailTextView);
        mVerifiedTextView=(TextView)rootView.findViewById(R.id.EmailVerifiedTextView);
        mStatusTextView=(TextView)rootView.findViewById(R.id.StatusTextView);

        mUsernameTextView.setText(currentUser.getUsername());
        if(currentUser==ParseUser.getCurrentUser()){
            mVerifiedTextView.setVisibility(View.VISIBLE);
            mEmailTextView.setText(currentUser.getEmail());
            if(currentUser.getBoolean("emailVerified")){
                mVerifiedTextView.setText(getString(R.string.verified));
                mVerifiedTextView.setTextColor(getResources().getColor(R.color.email_verified));
            }
            else{
                mVerifiedTextView.setText(getString(R.string.unverified));
                mVerifiedTextView.setTextColor(getResources().getColor(R.color.email_unverified));
            }
        }
        else{
            mVerifiedTextView.setVisibility(View.GONE);
        }

        String status=currentUser.getString(HomeActivity.FIELD_STATUS);
        if(status==null){
            status=getString(R.string.no_status);
        }
        mStatusTextView.setText(status);

//        android.support.v7.app.ActionBar actionBar=((ActionBarActivity)getActivity()).getSupportActionBar();
//        actionBar.setTitle(getString(R.string.title_sectionProfile));
        return rootView;

    }

    ParseUser getUserWithId(String id){
        ParseQuery<ParseUser> query=ParseUser.getQuery();
        try{
            return query.get(id);
        }
        catch(Exception e){
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu,final MenuInflater inflater) {

        //if profile is of self, allow editing
        if(getArguments().getString(USER_ID).equals(ParseUser.getCurrentUser().getObjectId())){
            inflater.inflate(R.menu.profile, menu);
        }

        else {
            ParseRelation<ParseUser> relation = ParseUser.getCurrentUser().getRelation(HomeActivity.FRIENDS);
            List<ParseUser> list;
            try {
                list = relation.getQuery().find();
                if (list.contains(getUserWithId(getArguments().getString(USER_ID)))) {
                    //if profile is of friend, allow unfriend and messaging
                    inflater.inflate(R.menu.profile_friend, menu);
                } else {
                    //if profile is of stranger, allow adding friend
                    inflater.inflate(R.menu.profile_stranger, menu);
                }
            } catch (Exception e) {
                inflater.inflate(R.menu.profile_stranger, menu);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(currentUser==null){
            currentUser=getUserWithId(getArguments().getString(USER_ID));
        }

        if(item.getItemId()==R.id.action_friend_profile){
            ParseRelation<ParseUser>relation=ParseUser.getCurrentUser().getRelation(HomeActivity.FRIENDS);
            relation.add(currentUser);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    getActivity().supportInvalidateOptionsMenu();
                    //display toast
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(getActivity(), getString(R.string.added_friend_1) + " " + currentUser.getUsername() + " " + getString(R.string.added_friend_2), Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });


        }
        else if(item.getItemId()==R.id.action_unfriend_profile){
            ParseRelation<ParseUser>relation=ParseUser.getCurrentUser().getRelation(HomeActivity.FRIENDS);
            relation.remove(currentUser);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    getActivity().supportInvalidateOptionsMenu();
                    //display toast
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(getActivity(), getString(R.string.removed_friend_1) + " " + currentUser.getUsername() + " " + getString(R.string.removed_friend_2), Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });

        }
        else if(item.getItemId()==R.id.action_edit_profile){
            mEditDialog=new Dialog(getActivity());
            mEditDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mEditDialog.setCanceledOnTouchOutside(false);
            mEditDialog.setContentView(R.layout.edit_profile_dialog);
            //set stuff and shit

            mEditDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

}
