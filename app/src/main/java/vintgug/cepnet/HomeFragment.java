package vintgug.cepnet;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HomeFragment extends android.support.v4.app.Fragment {

    static final int FRAGMENT_ID=NavigationDrawerFragment.NAV_ENTRY_1;

    private OnFragmentInteractionListener mListener;
    ProgressBar mNotifLoadProgress;
    ProgressBar mConvLoadProgress;
    LinearLayout mNoConversationLayout;
    FrameLayout mNotificationLayout;
    TextView mNotificationLabel;
    ListView mNotificationView;
    ListView mConversationView;
    ArrayList<ParseObject> mConversations;
    ArrayList<ParseObject> mNotifications;

    conversationAdapter convAdapter;
    notificationAdapter notifAdapter;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mConversations=new ArrayList<>();
        mNotifications=new ArrayList<>();
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home, container, false);
        mListener.onSectionAttached(FRAGMENT_ID);
        mNotifLoadProgress=(ProgressBar)view.findViewById(R.id.notificationLoadProgress);
        mConvLoadProgress=(ProgressBar)view.findViewById(R.id.convLoadProgress);
        mNoConversationLayout=(LinearLayout)view.findViewById(R.id.noConversationLayout);
        mNotificationLayout=(FrameLayout)view.findViewById(R.id.notificationLayout);
        mNotificationLabel=(TextView)view.findViewById(R.id.notificationLabel);
        mNotificationView=(ListView)view.findViewById(R.id.notificationListView);
        mConversationView=(ListView)view.findViewById(R.id.conversationListView);

        getConversationMessages();
        convAdapter=new conversationAdapter(getActivity(),R.layout.conversation_item,mConversations);
        mConversationView.setAdapter(convAdapter);
        mConversationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject message = mConversations.get(position);
                if (message.getParseUser(HomeActivity.MESSAGE_AUTHOR) == ParseUser.getCurrentUser()) {
                    mListener.messageFriend(message.getParseUser(HomeActivity.MESSAGE_RECIPIENT));
                } else {
                    mListener.messageFriend(message.getParseUser(HomeActivity.MESSAGE_AUTHOR));
                }
            }
        });

        getNotifications();
        notifAdapter=new notificationAdapter(getActivity(),R.layout.notification_item,mNotifications);
        mNotificationView.setAdapter(notifAdapter);

        return view;
    }

    void updateConvVis(){
        if(mConversations.size()==0){
            mNoConversationLayout.setVisibility(View.VISIBLE);
            mConversationView.setVisibility(View.GONE);
        }
        else{
            mNoConversationLayout.setVisibility(View.GONE);
            mConversationView.setVisibility(View.VISIBLE);
        }
    }

    void updateNotifVis(){
        if(mNotifications.size()==0){
            mNotificationLayout.setVisibility(View.GONE);
            mNotificationLabel.setVisibility(View.GONE);
        }
        else{
            mNotificationLayout.setVisibility(View.VISIBLE);
            mNotificationLabel.setVisibility(View.VISIBLE);
        }
    }

    void getNotifications(){
        mNotifLoadProgress.setVisibility(View.VISIBLE);
        mNotificationView.setVisibility(View.INVISIBLE);
        ParseQuery<ParseObject> notificationQuery=ParseQuery.getQuery(HomeActivity.NOTIFICATION);
        notificationQuery.whereEqualTo(HomeActivity.NOTIFICATION_RECIPIENTS,ParseUser.getCurrentUser());
        notificationQuery.orderByDescending("createdAt");
        notificationQuery.setLimit(30);
        notificationQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                mNotificationView.setVisibility(View.VISIBLE);
                mNotifLoadProgress.setVisibility(View.GONE);
                if(e==null) {
                    mNotifications.clear();
                    mNotifications.addAll(list);
                    //notify adapter
                    if(notifAdapter!=null){
                        notifAdapter.notifyDataSetChanged();
                    }
                    updateNotifVis();
                }
            }
        });
    }

    void getConversationMessages(){

        final ArrayList<ParseUser> friendList=new ArrayList<>();

        mConvLoadProgress.setVisibility(View.VISIBLE);
        mNoConversationLayout.setVisibility(View.GONE);
        mConversationView.setVisibility(View.GONE);

        ParseQuery<ParseUser> allUsersQuery=ParseUser.getQuery();
        allUsersQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                //gets the friends list
                if (e == null) {
                    friendList.addAll(list);
                    final ArrayList<ParseObject> unsortedConversations = new ArrayList<>();

                    for (ParseUser friend : friendList) { //gets up to one message for each friend in friendlist (most recent sent/received msg)

                        ParseQuery<ParseObject> query1 = ParseQuery.getQuery(HomeActivity.MESSAGE);
                        query1.whereEqualTo(HomeActivity.MESSAGE_AUTHOR, ParseUser.getCurrentUser());
                        query1.whereEqualTo(HomeActivity.MESSAGE_RECIPIENT, friend);

                        ParseQuery<ParseObject> query2 = ParseQuery.getQuery(HomeActivity.MESSAGE);
                        query2.whereEqualTo(HomeActivity.MESSAGE_RECIPIENT, ParseUser.getCurrentUser());
                        query2.whereEqualTo(HomeActivity.MESSAGE_AUTHOR, friend);

                        ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<>();
                        queries.add(query1);
                        queries.add(query2);

                        ParseQuery<ParseObject> query = ParseQuery.or(queries);
                        query.orderByDescending("createdAt"); //latest messages first
                        query.setLimit(1); //get the latest message for each friend

                        try {
                            List<ParseObject> result = query.find();
                            if (result != null && result.get(0) != null) {
                                unsortedConversations.add(result.get(0));
                            }
                        } catch (Exception findMessageException) {
                        }//do nothing

                    }

                    mConversations.clear();
                    mConversations.addAll(unsortedConversations);
                    Collections.sort(mConversations, new MessageDateComparator());
                    mConvLoadProgress.setVisibility(View.GONE);
                    updateConvVis();
                    if (convAdapter != null) {
                        convAdapter.notifyDataSetChanged();
                    }

                } else {
                    mConvLoadProgress.setVisibility(View.GONE);
                    updateConvVis();
                }
            }
        });


    }

    public class MessageDateComparator implements Comparator<ParseObject> { //Returns negative when left is more urgent than right
        public int compare(ParseObject left,ParseObject right){
            if((left.getCreatedAt().getTime()-right.getCreatedAt().getTime())<0){
                return 1;
            }
            else if((left.getCreatedAt().getTime()-right.getCreatedAt().getTime())>0){
                return -1;
            }
            else{return 0;}
        }
    }

    private class notificationAdapter extends ArrayAdapter<ParseObject> {
        Context context;
        int mResource;
        ArrayList<ParseObject> mNotificationList;

        public notificationAdapter(Context c, int resource, ArrayList<ParseObject> notifications){
            super(c,resource,notifications);
            context=c;
            mResource=resource;
            mNotificationList =notifications;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent){
            if(row==null){
                row=((Activity)context).getLayoutInflater().inflate(mResource,parent,false);
            }
            ParseObject notification= mNotificationList.get(position);
            String notificationContent=notification.getString(HomeActivity.NOTIFICATION_TEXT);
            Date notificationDate=notification.getCreatedAt();

            SimpleDateFormat dateFormat=new SimpleDateFormat("h:mma EEE,d MMM");

            TextView notifContentView=(TextView)row.findViewById(R.id.notifContentView);
            TextView notifDateView=(TextView)row.findViewById(R.id.notifDateView);

            notifContentView.setText(notificationContent);
            notifDateView.setText(dateFormat.format(notificationDate));

            return row;
        }


    }

    private class conversationAdapter extends ArrayAdapter<ParseObject> {
        Context context;
        int mResource;
        ArrayList<ParseObject> mMessageList;

        public conversationAdapter(Context c, int resource, ArrayList<ParseObject> messages){
            super(c,resource,messages);
            context=c;
            mResource=resource;
            mMessageList=messages;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent){
            if(row==null){
                row=((Activity)context).getLayoutInflater().inflate(mResource,parent,false);
            }
            ParseObject message=mMessageList.get(position);
            String messageContent=message.getString(HomeActivity.MESSAGE_TEXT);
            ParseUser sender=message.getParseUser(HomeActivity.MESSAGE_AUTHOR);
            ParseUser receiver=message.getParseUser(HomeActivity.MESSAGE_RECIPIENT);
            ParseUser otherParty;

            TextView messageContentView=(TextView)row.findViewById(R.id.messageContentView);
            TextView conversationNameView=(TextView)row.findViewById(R.id.convName);
            ImageView isUserSenderView=(ImageView)row.findViewById(R.id.isUserSendView);
            ImageView conversationPictureView=(ImageView)row.findViewById(R.id.conversationPicture);

            messageContentView.setText(messageContent);

            if(sender == ParseUser.getCurrentUser()){ //message from currentuser to otheruser
                conversationNameView.setText(receiver.getUsername());
                otherParty=receiver;
                isUserSenderView.setVisibility(View.VISIBLE);
            }
            else{ //message from otheruser to currentuser
                conversationNameView.setText(sender.getUsername());
                otherParty=sender;
                isUserSenderView.setVisibility(View.GONE);
            }

            //set picture
            ParseFile profilePic=(ParseFile)otherParty.get(HomeActivity.FIELD_PICTURE);
            if(profilePic==null){
                conversationPictureView.setImageDrawable(getResources().getDrawable(HomeActivity.DEFAULT_PICTURE_ID));
            }
            else {
                try {
                    byte[] data = profilePic.getData();
                    conversationPictureView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                }catch(Exception e){
                    conversationPictureView.setImageDrawable(getResources().getDrawable(HomeActivity.DEFAULT_PICTURE_ID));
                }
            }

            return row;
        }


    }

    @Override
    public void onCreateOptionsMenu(final Menu menu,final MenuInflater inflater) {
        inflater.inflate(R.menu.home_frag, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            getConversationMessages();
        }
        return super.onOptionsItemSelected(item);
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
        void messageFriend(ParseUser user);
        void onSectionAttached(int position);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}
