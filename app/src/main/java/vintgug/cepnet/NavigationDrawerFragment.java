package vintgug.cepnet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    public static final int NAV_PROFILE=R.id.navProfile;

    public static final int NAV_ENTRY_1=R.id.navEntry1;
    public static final int NAV_ENTRY_2=R.id.navEntry2;
    public static final int NAV_ENTRY_3=R.id.navEntry3;
    public static final int NAV_ENTRY_4=R.id.navEntry4;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks mCallbacks;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mDrawerView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = NAV_ENTRY_1;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    ArrayList<TextView> navTextList = new ArrayList<>();
    ArrayList<ImageView> navIconList = new ArrayList<>();
    ArrayList<FrameLayout> navEntryList = new ArrayList<>();

    TextView navText1;
    TextView navText2;
    TextView navText3;
    TextView navText4;
    ImageView navIcon1;
    ImageView navIcon2;
    ImageView navIcon3;
    ImageView navIcon4;

    FrameLayout navProfile;
    FrameLayout nav1;
    FrameLayout nav2;
    FrameLayout nav3;
    FrameLayout nav4;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerView = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        navText1=(TextView)mDrawerView.findViewById(R.id.navText1);
        navText2=(TextView)mDrawerView.findViewById(R.id.navText2);
        navText3=(TextView)mDrawerView.findViewById(R.id.navText3);
        navText4=(TextView)mDrawerView.findViewById(R.id.navText4);

        navIcon1=(ImageView)mDrawerView.findViewById(R.id.navIcon1);
        navIcon2=(ImageView)mDrawerView.findViewById(R.id.navIcon2);
        navIcon3=(ImageView)mDrawerView.findViewById(R.id.navIcon3);
        navIcon4=(ImageView)mDrawerView.findViewById(R.id.navIcon4);

        navProfile=(FrameLayout)mDrawerView.findViewById(NAV_PROFILE);
        nav1=(FrameLayout)mDrawerView.findViewById(NAV_ENTRY_1);
        nav2=(FrameLayout)mDrawerView.findViewById(NAV_ENTRY_2);
        nav3=(FrameLayout)mDrawerView.findViewById(NAV_ENTRY_3);
        nav4=(FrameLayout)mDrawerView.findViewById(NAV_ENTRY_4);

        navTextList.clear();
        navIconList.clear();
        navEntryList.clear();

        navTextList.add(navText1);
        navTextList.add(navText2);
        navTextList.add(navText3);
        navTextList.add(navText4);

        navIconList.add(navIcon1);
        navIconList.add(navIcon2);
        navIconList.add(navIcon3);
        navIconList.add(navIcon4);

        navEntryList.add(navProfile);
        navEntryList.add(nav1);
        navEntryList.add(nav2);
        navEntryList.add(nav3);
        navEntryList.add(nav4);

        for(FrameLayout f:navEntryList){
            f.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItem(v.getId());
                }
            });
        }

        //Edit this stuff to change functionality
//        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                selectItem(position);
//            }
//        });
//        mDrawerListView.setAdapter(new ArrayAdapter<String>(
//                getActionBar().getThemedContext(),
//                android.R.layout.simple_list_item_activated_1,
//                android.R.id.text1,
//                new String[]{
//                        getString(R.string.title_section1),
//                        getString(R.string.title_section2),
//                        getString(R.string.title_section3),
//                }));
//        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);


        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);

        return mDrawerView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        //change menu colours
        unselectAll();
        if(position==NAV_ENTRY_1){
            navText1.setTextColor(getResources().getColor(R.color.nav_entry_selected));
            navIcon1.setColorFilter(getResources().getColor(R.color.nav_entry_selected));
        }
        else if(position==NAV_ENTRY_2){
            navText2.setTextColor(getResources().getColor(R.color.nav_entry_selected));
            navIcon2.setColorFilter(getResources().getColor(R.color.nav_entry_selected));
        }
        else if(position==NAV_ENTRY_3){
            navText3.setTextColor(getResources().getColor(R.color.nav_entry_selected));
            navIcon3.setColorFilter(getResources().getColor(R.color.nav_entry_selected));
        }
        else if(position==NAV_ENTRY_4){
            navText4.setTextColor(getResources().getColor(R.color.nav_entry_selected));
            navIcon4.setColorFilter(getResources().getColor(R.color.nav_entry_selected));
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    private void unselectAll(){
        for(TextView t:navTextList){
            t.setTextColor(getResources().getColor(R.color.nav_entry_unselected));
        }
        for(ImageView i:navIconList){
            i.setColorFilter(getResources().getColor(R.color.nav_entry_unselected));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //selects the action bar thingies
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

//        if (item.getItemId() == R.id.action_example) {
//            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
}
