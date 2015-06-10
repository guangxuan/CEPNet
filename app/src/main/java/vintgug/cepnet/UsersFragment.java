package vintgug.cepnet;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends android.support.v4.app.Fragment {
    private OnFragmentInteractionListener mListener;

    final static String USER_ID="user_id";
    private View mFragmentView;
    final ArrayList<ParseUser> mUsersToDisplay =new ArrayList<>();
    ListView mUserList;
    LinearLayout mEmptyLayout;
    usersAdapter adapter;
    ProgressBar mLoadProgress;
    ParseUser currUser;

    Toast mToast;

    public static UsersFragment newInstance(String user_id) {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID,user_id);
        fragment.setArguments(args);
        return fragment;
    }

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFragmentView=inflater.inflate(R.layout.fragment_users, container, false);
        mUserList=(ListView)mFragmentView.findViewById(R.id.usersList);
        mEmptyLayout=(LinearLayout)mFragmentView.findViewById(R.id.noUsersLayout);
        mLoadProgress =(ProgressBar)mFragmentView.findViewById(R.id.loadProgress);

        if (getArguments() != null) {
            currUser=getUserWithId(getArguments().getString(USER_ID));
            if(currUser==null){
                currUser=ParseUser.getCurrentUser();
            }
        }
        else{
            currUser=ParseUser.getCurrentUser();
        }

        mUserList.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.GONE);
        mLoadProgress.setVisibility(View.GONE);

        refreshList();
        adapter = new usersAdapter(getActivity(), R.layout.user_list_item, mUsersToDisplay);
        mUserList.setAdapter(adapter);
        mUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseUser selectedUser=mUsersToDisplay.get(position);
                //callback to main activity, fragment transition
                mListener.userSelected(selectedUser);
            }
        });

//        android.support.v7.app.ActionBar actionBar=((ActionBarActivity)getActivity()).getSupportActionBar();
//        actionBar.setTitle(getString(R.string.nav_entry_2));

        return mFragmentView;
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

    void refreshList(){ //remove friends from userlist
        mLoadProgress.setVisibility(View.VISIBLE);
        mUsersToDisplay.clear();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                mLoadProgress.setVisibility(View.GONE);
                if (e == null) {
                    mUsersToDisplay.addAll(list);
                    mUsersToDisplay.remove(ParseUser.getCurrentUser());

                    ParseRelation<ParseUser> relation=currUser.getRelation(HomeActivity.FRIENDS);
                    relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> list, ParseException e) {
                            mLoadProgress.setVisibility(View.GONE);
                            if (e == null) {
                                mUsersToDisplay.removeAll(list);
                                updateListVis();
                            }
                        }
                    });
                }
            }
        });

    }

    void updateListVis() {
        if (mUsersToDisplay.size() == 0) {
            mUserList.setVisibility(View.GONE);
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mUserList.setVisibility(View.VISIBLE);
            mEmptyLayout.setVisibility(View.GONE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private class usersAdapter extends ArrayAdapter<ParseUser> {
        Context context;
        int mResource;
        ArrayList<ParseUser> mUserList;

        public usersAdapter(Context c, int resource, ArrayList<ParseUser> users){
            super(c,resource,users);
            context=c;
            mResource=resource;
            mUserList=users;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent){
            if(row==null){
                row=((Activity)context).getLayoutInflater().inflate(mResource,parent,false);
            }
            ImageView profilePicView=(ImageView)row.findViewById(R.id.userProfilePicture);
            TextView nameView=(TextView)row.findViewById(R.id.userName);
            ImageButton friendButton=(ImageButton)row.findViewById(R.id.addFriendButton);
            friendButton.setFocusable(false);
            //Set stuff

            final ParseUser user=mUserList.get(position);

            nameView.setText(user.getUsername());

            friendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseRelation<ParseUser> relation = currUser.getRelation(HomeActivity.FRIENDS);
                    relation.add(user);
                    currUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            //display toast
                            refreshList();
                            if (mToast != null) {
                                mToast.cancel();
                            }
                            mToast = Toast.makeText(getActivity(), getString(R.string.added_friend_1) + " " + user.getUsername() + " " + getString(R.string.added_friend_2), Toast.LENGTH_SHORT);
                            mToast.show();
                        }
                    });
                }
            });

            //set profile pics
            ParseFile profilePic=(ParseFile)user.get(HomeActivity.FIELD_PICTURE);
            if(profilePic==null){
                profilePicView.setImageDrawable(getResources().getDrawable(HomeActivity.DEFAULT_PICTURE_ID));
            }
            else {
                try {
                    byte[] data = profilePic.getData();
                    profilePicView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                }catch(Exception e){
                    profilePicView.setImageDrawable(getResources().getDrawable(HomeActivity.DEFAULT_PICTURE_ID));
                }
            }


            return row;
        }


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        //public void onFragmentInteraction(Uri uri);
        void userSelected(ParseUser user);
    }

}
