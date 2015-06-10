package vintgug.cepnet;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    final static String USER_ID="user_id";
    final static String IMAGE_NAME="profile_picture";
    ImageView mPictureView;
    TextView mUsernameTextView;
    TextView mEmailTextView;
    TextView mVerifiedTextView;
    TextView mStatusTextView;
    TextView mChangePictureView;
    ParseUser currentUser;
    Toast mToast;
    String selectedImagePath;

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

        mUsernameTextView= (TextView)rootView.findViewById(R.id.UsernameTextView);
        mEmailTextView= (TextView)rootView.findViewById(R.id.EmailTextView);
        mVerifiedTextView=(TextView)rootView.findViewById(R.id.EmailVerifiedTextView);
        mStatusTextView=(TextView)rootView.findViewById(R.id.StatusTextView);
        mChangePictureView=(TextView)rootView.findViewById(R.id.ChangePictureView);
        mPictureView=(ImageView)rootView.findViewById(R.id.ProfilePictureView);

        refreshFields();
        setProfileImageView();

        mChangePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.selectProfilePicture();
            }
        });

        if(currentUser==null){return rootView;}
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

    public void onProfilePictureSelected(String path){
        selectedImagePath=path;
        Bitmap profileBitmap = decodeFile(new File(path));
        if(profileBitmap==null){
            if(mToast!=null){
                mToast.cancel();
            }
            mToast=Toast.makeText(getActivity(),R.string.missing_picture_file,Toast.LENGTH_SHORT);
            mToast.show();
        }
        else {
            //set picture, upload to Parse server
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            profileBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byteArray = stream.toByteArray();

            ParseFile profilePic=new ParseFile(IMAGE_NAME+".bmp",byteArray);
            currentUser.put(HomeActivity.FIELD_PICTURE,profilePic);
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(getActivity(), R.string.profile_picture_changed, Toast.LENGTH_SHORT);
                    mToast.show();
                    setProfileImageView();
                    mListener.profilePictureChanged();
                }
            });
        }
    }

    void setProfileImageView(){
        ParseFile profilePic=(ParseFile)currentUser.get(HomeActivity.FIELD_PICTURE);
        if(profilePic==null){
            mPictureView.setImageDrawable(getResources().getDrawable(HomeActivity.DEFAULT_PICTURE_ID));
        }
        else {
            profilePic.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                        mPictureView.setImageBitmap(bitmap);
                    } else {
                        mPictureView.setImageDrawable(getResources().getDrawable(HomeActivity.DEFAULT_PICTURE_ID));
                    }
                }
            });
        }
    }

    private Bitmap decodeFile(File f) { //to make sure size of image is manageable
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        return null;}
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
            final EditText editUsername=(EditText)mEditDialog.findViewById(R.id.usernameEdit);
            final EditText editEmail=(EditText)mEditDialog.findViewById(R.id.emailEdit);
            final EditText editStatus=(EditText)mEditDialog.findViewById(R.id.statusEdit);
            final ImageButton saveButton=(ImageButton)mEditDialog.findViewById(R.id.saveProfileEdit);
            final ProgressBar loadProgress=(ProgressBar)mEditDialog.findViewById(R.id.loadProgress);

            loadProgress.setVisibility(View.GONE);

            editUsername.setText(currentUser.getUsername());
            editEmail.setText(currentUser.getEmail());
            editStatus.setText(currentUser.getString(HomeActivity.FIELD_STATUS));

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String errorMsg = "";
                    final String mUsername = editUsername.getText().toString().trim();
                    final String mEmail = editEmail.getText().toString().trim();
                    final String mStatus = editStatus.getText().toString();
                    if (mUsername.equals("")) {
                        errorMsg = getString(R.string.no_username);
                    } else if (mEmail.equals("")) {
                        errorMsg = getString(R.string.no_email);
                    }
                    if (!errorMsg.equals("")) {
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                    currentUser.setUsername(mUsername);
                    currentUser.setEmail(mEmail);
                    currentUser.put(HomeActivity.FIELD_STATUS, mStatus);
                    loadProgress.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.GONE);
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            loadProgress.setVisibility(View.GONE);
                            saveButton.setVisibility(View.VISIBLE);
                            if (e == null) {
                                if (mToast != null) {
                                    mToast.cancel();
                                }
                                mToast = Toast.makeText(getActivity(), R.string.saved_profile_changes, Toast.LENGTH_SHORT);
                                mToast.show();
                                refreshFields();
                                mEditDialog.cancel();
                            } else {
                                if (mToast != null) {
                                    mToast.cancel();
                                }
                                mToast = Toast.makeText(getActivity(), R.string.not_saved_profile_changes, Toast.LENGTH_SHORT);
                                mToast.show();
                            }
                        }
                    });
                }
            });

            mEditDialog.show();

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            mEditDialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        else if(item.getItemId()==R.id.action_message_profile){
            mListener.messageFriend(currentUser);
        }
        return super.onOptionsItemSelected(item);
    }

    void refreshFields(){
        currentUser=getUserWithId(getArguments().getString(USER_ID));
        if(currentUser!=null){
            mEmailTextView.setText(currentUser.getEmail());
            mUsernameTextView.setText(currentUser.getUsername());
            if(currentUser==ParseUser.getCurrentUser()){
                mVerifiedTextView.setVisibility(View.VISIBLE);
                mChangePictureView.setVisibility(View.VISIBLE);
                mChangePictureView.setClickable(true);
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
                mChangePictureView.setVisibility(View.GONE);
                mChangePictureView.setClickable(false);
            }

            String status=currentUser.getString(HomeActivity.FIELD_STATUS);
            if(status==null || status.equals("")){
                status=getString(R.string.no_status);
            }
            mStatusTextView.setText(status);
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
        void selectProfilePicture();
        void profilePictureChanged();
        void messageFriend(ParseUser user);
    }

}
