package vintgug.cepnet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;


public class HomeActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks , FriendFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, MessageFragment.OnFragmentInteractionListener, UsersFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    public static final String FRIENDS="friends";
    public static final String FIELD_STATUS="status";
    public static final String FIELD_PICTURE="profile_picture";

    public static final String MESSAGE="message";
    public static final String MESSAGE_RECIPIENT="message_recipient";
    public static final String MESSAGE_AUTHOR="message_author";
    public static final String MESSAGE_TEXT="message_text";

    public static final String NOTIFICATION="notification";
    public static final String NOTIFICATION_RECIPIENTS="notification_recipient";
    public static final String NOTIFICATION_TEXT="notification_text";

    public static final int DEFAULT_PICTURE_ID=R.drawable.user;

    public static final int NAV_MESSAGE=40234988;
    public static final int NAV_FRIEND_PROFILE=120912471;

    static final int SELECT_PICTURE=83;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private String mTitle;
    int mCurrentPos;

    ParseUser currentUser;
    AlertDialog.Builder builder;
    ProfileFragment fragmentToReturnOutput;
    Bitmap mProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ParseUser.getCurrentUser();
        if(currentUser==null) {
            //show login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            setContentView(R.layout.activity_home);
            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

            builder = new AlertDialog.Builder(HomeActivity.this);

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

            mTitle = getString(R.string.app_name);
            getSupportActionBar().setTitle(mTitle);

            onNavigationDrawerItemSelected(NavigationDrawerFragment.NAV_ENTRY_1);
            mCurrentPos = NavigationDrawerFragment.NAV_ENTRY_1;

            mProfilePicture=updateProfilePicture();
            mNavigationDrawerFragment.setProfilePicView(mProfilePicture);

            getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if (!mNavigationDrawerFragment.isDrawerOpen()) {
                        restoreActionBar();
                    }
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                fragmentToReturnOutput.onProfilePictureSelected(getPath(selectedImageUri));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction=fragmentManager.beginTransaction();
        switch(position){
            case NavigationDrawerFragment.NAV_PROFILE:
                if(mCurrentPos!=NavigationDrawerFragment.NAV_PROFILE) {
                    transaction.replace(R.id.container, ProfileFragment.newInstance(ParseUser.getCurrentUser().getObjectId()));
                    transaction.addToBackStack("Navigation");
                }
                //mTitle = getString(R.string.title_sectionProfile);
                break;

            //Note: dont add to backstack for other nav tabs
            case NavigationDrawerFragment.NAV_ENTRY_1:
                if(mCurrentPos!=NavigationDrawerFragment.NAV_ENTRY_1) {
                    transaction.replace(R.id.container, HomeFragment.newInstance());
                    transaction.addToBackStack("Navigation");
                }
                //mTitle = getString(R.string.nav_entry_1);
                break;

            case NavigationDrawerFragment.NAV_ENTRY_2:
                if(mCurrentPos!=NavigationDrawerFragment.NAV_ENTRY_2) {
                    transaction.replace(R.id.container, FriendFragment.newInstance(ParseUser.getCurrentUser().getObjectId()));
                    transaction.addToBackStack("Navigation");
                }
                //mTitle = getString(R.string.nav_entry_2);
                break;

            case NavigationDrawerFragment.NAV_ENTRY_3:
                if(mCurrentPos!=NavigationDrawerFragment.NAV_ENTRY_3) {
                    transaction.replace(R.id.container, UsersFragment.newInstance(ParseUser.getCurrentUser().getObjectId()));
                    transaction.addToBackStack("Navigation");
                }
                //mTitle = getString(R.string.nav_entry_3);
                break;

            case NavigationDrawerFragment.NAV_ENTRY_4:
                //mTitle = getString(R.string.nav_entry_4);
                break;

            default:
                //mTitle = getString(R.string.nav_entry_1); //go to home as default
                break;
        }
        mCurrentPos=position;
        transaction.commit();
    }

    public void onSectionAttached(int position) {
        mCurrentPos=position;
        mNavigationDrawerFragment.setHighlightSection(position);
        switch (position) {
            case NavigationDrawerFragment.NAV_PROFILE:
                mTitle = getString(R.string.title_sectionProfile);
                break;
            case NavigationDrawerFragment.NAV_ENTRY_1:
                mTitle = getString(R.string.nav_entry_1);
                break;
            case NavigationDrawerFragment.NAV_ENTRY_2:
                mTitle = getString(R.string.nav_entry_2);
                break;
            case NavigationDrawerFragment.NAV_ENTRY_3:
                mTitle = getString(R.string.nav_entry_3);
                break;
            case NavigationDrawerFragment.NAV_ENTRY_4:
                mTitle = getString(R.string.nav_entry_4);
                break;
            case NAV_FRIEND_PROFILE:
                mTitle = getString(R.string.title_sectionOtherProfile);
                break;
            case NAV_MESSAGE:
                //get username of messagee
                MessageFragment fragment=(MessageFragment)getSupportFragmentManager().findFragmentById(R.id.container);
                mTitle=fragment.getTitle();
                break;
            default:
                mTitle = getString(R.string.title_default);
                break;
        }
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }
    }

    public void createNotification(String message,ArrayList<ParseUser> recipients){ //call this when adding you friend, updating profile status or picture or name
        ParseObject newNotification=new ParseObject(NOTIFICATION);
        newNotification.put(NOTIFICATION_TEXT,message);
        newNotification.put(NOTIFICATION_RECIPIENTS,recipients);
        newNotification.saveInBackground();
    }

    @Override
    public void onBackPressed() {
        if(mCurrentPos==NavigationDrawerFragment.NAV_ENTRY_1){
            finish();
        }
        super.onBackPressed();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.action_logout){
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void logout(){
        final Context context=HomeActivity.this;
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.logout_failed_title);
                    builder.setMessage(R.string.logout_failed);
                    builder.setPositiveButton(getString(R.string.ok), null);
                    AlertDialog alert = builder.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.setCancelable(false);
                    alert.show();
                }
            }
        });
    }

    public void userSelected(ParseUser user){
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction=fragmentManager.beginTransaction();

        transaction.replace(R.id.container, ProfileFragment.newInstance(user.getObjectId()));
        transaction.addToBackStack("Friend selected");
        transaction.commit();
        mCurrentPos=NAV_FRIEND_PROFILE;
    }

    public void messageFriend(ParseUser user){
        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction=fragmentManager.beginTransaction();
        //open the messaging fragment
        transaction.replace(R.id.container, MessageFragment.newInstance(user));
        transaction.addToBackStack("Messaging friend");
        transaction.commit();
        mCurrentPos=NAV_MESSAGE;
    }

    public void selectProfilePicture(){
        fragmentToReturnOutput=(ProfileFragment)getSupportFragmentManager().findFragmentById(R.id.container);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    public void profilePictureChanged(){
        createNotification(ParseUser.getCurrentUser().getUsername()+" "+getString(R.string.notif_picture_changed),getFriendList());
        mProfilePicture=updateProfilePicture();
        mNavigationDrawerFragment.setProfilePicView(mProfilePicture);
    }

    ArrayList<ParseUser> getFriendList(){
        ArrayList<ParseUser> result=new ArrayList<>();
        ParseRelation<ParseUser> relation=ParseUser.getCurrentUser().getRelation(HomeActivity.FRIENDS);
        try {
            result.addAll(relation.getQuery().find());
            return result;
        }catch(Exception e){
            return result;
        }
    }

    Bitmap updateProfilePicture(){
        ParseFile profilePic=(ParseFile)currentUser.get(HomeActivity.FIELD_PICTURE);
        if(profilePic==null){
            return null;
        }
        else {
            try {
                byte[] data = profilePic.getData();
                return BitmapFactory.decodeByteArray(data, 0, data.length);
            }catch(Exception e){
                return null;
            }
        }
    }

//
//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_home2, container, false);
//            return rootView;
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((HomeActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }

}
