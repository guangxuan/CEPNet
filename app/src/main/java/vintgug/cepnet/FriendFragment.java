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

public class FriendFragment extends android.support.v4.app.Fragment {
    private OnFragmentInteractionListener mListener;

    static final int FRAGMENT_ID=NavigationDrawerFragment.NAV_ENTRY_2;

    final static String USER_ID="user_id";
    private View mFragmentView;
    final ArrayList<ParseUser> mFriendUsers =new ArrayList<>();
    ListView mUserList;
    LinearLayout mEmptyLayout;
    friendAdapter adapter;
    ProgressBar mLoadProgress;
    ParseUser currUser;

    Toast mToast;

    public static FriendFragment newInstance(String user_id) {
        FriendFragment fragment = new FriendFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID,user_id);
        fragment.setArguments(args);
        return fragment;
    }

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragmentView=inflater.inflate(R.layout.fragment_friend, container, false);
        mListener.onSectionAttached(FRAGMENT_ID);
        mUserList=(ListView)mFragmentView.findViewById(R.id.friendList);
        mEmptyLayout=(LinearLayout)mFragmentView.findViewById(R.id.noFriendsLayout);
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
        adapter = new friendAdapter(getActivity(), R.layout.friend_list_item, mFriendUsers);
        mUserList.setAdapter(adapter);
        mUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseUser selectedUser=mFriendUsers.get(position);
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

    void refreshList(){
        mLoadProgress.setVisibility(View.VISIBLE);
        mFriendUsers.clear();

//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//        query.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> list, ParseException e) {
//                mLoadProgress.setVisibility(View.GONE);
//                if (e == null) {
//                    mFriendUsers.addAll(list);
//                    updateListVis();
//                }
//            }
//        });

        ParseRelation<ParseUser> relation=currUser.getRelation(HomeActivity.FRIENDS);
        relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                mLoadProgress.setVisibility(View.GONE);
                if(e==null){
                    mFriendUsers.addAll(list);
                    updateListVis();
                }
            }
        });

    }

    void updateListVis() {
        if (mFriendUsers.size() == 0) {
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

    private class friendAdapter extends ArrayAdapter<ParseUser> {
        Context context;
        int mResource;
        ArrayList<ParseUser> mUserList;

        public friendAdapter(Context c, int resource, ArrayList<ParseUser> users){
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
            ImageView profilePicView=(ImageView)row.findViewById(R.id.friendProfilePicture);
            TextView nameView=(TextView)row.findViewById(R.id.friendName);
            ImageButton deleteButton=(ImageButton)row.findViewById(R.id.deleteFriendButton);
            ImageButton messageButton=(ImageButton)row.findViewById(R.id.messageFriendButton);
            deleteButton.setFocusable(false);
            messageButton.setFocusable(false);
            //Set stuff

            final ParseUser user=mUserList.get(position);

            nameView.setText(user.getUsername());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseRelation<ParseUser> relation = currUser.getRelation(HomeActivity.FRIENDS);
                    relation.remove(user);
                    currUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            //display toast
                            refreshList();
                            if (mToast != null) {
                                mToast.cancel();
                            }
                            mToast = Toast.makeText(getActivity(), getString(R.string.removed_friend_1) + " " + user.getUsername() + " " + getString(R.string.removed_friend_2), Toast.LENGTH_SHORT);
                            mToast.show();
                        }
                    });
                }
            });

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

            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.messageFriend(user);
                }
            });

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
        void messageFriend(ParseUser user);
        void onSectionAttached(int position);
    }

}
