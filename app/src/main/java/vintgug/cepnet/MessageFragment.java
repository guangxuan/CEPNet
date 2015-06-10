package vintgug.cepnet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MessageFragment extends android.support.v4.app.Fragment {
    private static final String MESSAGE_TO = "conversation_parter";
    private ParseUser mMessageTo;

    ProgressBar mLoadProgressBar;
    LinearLayout mNoMessagesLayout;
    ListView mMessageListView;
    EditText mNewMessage;
    ImageButton mSendButton;
    ArrayList<ParseObject> mMessages;

    messageAdapter adapter;

    private OnFragmentInteractionListener mListener;

    public static MessageFragment newInstance(ParseUser messageTo) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_TO, messageTo.getObjectId());
        fragment.setArguments(args);
        return fragment;
    }

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMessages=new ArrayList<>();
        if (getArguments() != null) {
            mMessageTo=getUserWithId(getArguments().getString(MESSAGE_TO));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_message, container, false);
        mLoadProgressBar=(ProgressBar)view.findViewById(R.id.loadProgress);
        mNoMessagesLayout=(LinearLayout)view.findViewById(R.id.noMessagesLayout);
        mMessageListView=(ListView)view.findViewById(R.id.messageList);
        mNewMessage=(EditText)view.findViewById(R.id.newMessageText);
        mSendButton=(ImageButton)view.findViewById(R.id.newMessageSend);

        refreshMessages();

        adapter=new messageAdapter(getActivity(),R.layout.message_item,mMessages);
        mMessageListView.setAdapter(adapter);

        mMessageListView.post(new Runnable() {
            public void run() {
                mMessageListView.setSelection(mMessageListView.getCount() - 1);
            }
        });

        mMessageListView.setDivider(null);
        mMessageListView.setDividerHeight(0);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessageContent = mNewMessage.getText().toString().trim();
                mNewMessage.setText("");
                final ParseObject newMessage = new ParseObject(HomeActivity.MESSAGE);
                newMessage.put(HomeActivity.MESSAGE_TEXT, newMessageContent);
                newMessage.put(HomeActivity.MESSAGE_AUTHOR, ParseUser.getCurrentUser());
                newMessage.put(HomeActivity.MESSAGE_RECIPIENT, mMessageTo);
                mSendButton.setClickable(false);//prevent user from sending multiple messages before one saves
                newMessage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        mSendButton.setClickable(true);
                        if (e == null) {
                            newMessage.fetchInBackground(new GetCallback<ParseObject>() { //need to fetch fields like createdAt
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    mMessages.add(newMessage);//add to list
                                    if (adapter != null) {
                                        if(mMessages.size()==0){
                                            mNoMessagesLayout.setVisibility(View.VISIBLE);
                                            mMessageListView.setVisibility(View.INVISIBLE);
                                        }
                                        else{
                                            mNoMessagesLayout.setVisibility(View.INVISIBLE);
                                            mMessageListView.setVisibility(View.VISIBLE);
                                        }
                                        adapter.notifyDataSetChanged();
                                        mMessageListView.post(new Runnable() {
                                            public void run() {
                                                mMessageListView.setSelection(mMessageListView.getCount() - 1);
                                            }
                                        });
                                    }
                                }
                            });

                        }
                    }
                });
            }
        });

        return view;
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

    void refreshMessages(){
        //gets an updated messagelist
        mSendButton.setClickable(false);
        mMessages.clear();
        mNoMessagesLayout.setVisibility(View.INVISIBLE);
        mMessageListView.setVisibility(View.INVISIBLE);
        mLoadProgressBar.setVisibility(View.VISIBLE);

        ParseObject[] values={ParseUser.getCurrentUser(),mMessageTo};
        ParseQuery<ParseObject> query = ParseQuery.getQuery(HomeActivity.MESSAGE);
        query.whereContainedIn(HomeActivity.MESSAGE_AUTHOR, Arrays.asList(values));
        query.whereContainedIn(HomeActivity.MESSAGE_RECIPIENT, Arrays.asList(values)); //Either currentuser send to mMessageTo or mMessageTo send to currentuser
        query.orderByAscending("createdAt"); //sort by increasing order of date created at
        query.setLimit(200);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    mMessages.addAll(list);
                    mLoadProgressBar.setVisibility(View.GONE);
                    //updates visibilities
                    if (mMessages.size() == 0) {
                        mNoMessagesLayout.setVisibility(View.VISIBLE);
                        mMessageListView.setVisibility(View.INVISIBLE);
                    } else {
                        mNoMessagesLayout.setVisibility(View.INVISIBLE);
                        mMessageListView.setVisibility(View.VISIBLE);
                    }
                    //notifies list
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    mSendButton.setClickable(true);
                }
            }
        });

    }

//    List<ParseObject> getMessages(ParseUser otherUser){ //messages are parseobjects with fields fromUser,toUser,date created,text
//
//        ParseObject[] values={ParseUser.getCurrentUser(),otherUser};
//        ParseQuery<ParseObject> query = ParseQuery.getQuery(HomeActivity.MESSAGE);
//        query.whereContainedIn(HomeActivity.MESSAGE_AUTHOR, Arrays.asList(values));
//        query.whereContainedIn(HomeActivity.MESSAGE_RECIPIENT, Arrays.asList(values)); //Either currentuser send to mMessageTo or mMessageTo send to currentuser
//        query.orderByAscending("createdAt"); //sort by increasing order of date created at
//        query.setLimit(200);
//        try{
//            return query.find();
//        }catch(Exception e){
//            return null;
//        }
//
//    }

    private class messageAdapter extends ArrayAdapter<ParseObject> {
        Context context;
        int mResource;
        ArrayList<ParseObject> mMessageList;

        public messageAdapter(Context c, int resource, ArrayList<ParseObject> messages){
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
            Date messageDate=message.getCreatedAt();

            SimpleDateFormat dateFormat=new SimpleDateFormat("h:mma EEE,d MMM");

            TextView messageContentView=(TextView)row.findViewById(R.id.messageContentView);
            TextView messageDateView=(TextView)row.findViewById(R.id.messageDateView);
            LinearLayout messageLayout1=(LinearLayout)row.findViewById(R.id.messageLayout1);
            LinearLayout messageLayout2=(LinearLayout)row.findViewById(R.id.messageLayout2);

            messageContentView.setText(messageContent);
            messageDateView.setText(dateFormat.format(messageDate));

            if(message.getParseObject(HomeActivity.MESSAGE_AUTHOR)==ParseUser.getCurrentUser()){ //message from currentuser to otheruser
                messageContentView.setGravity(Gravity.END);
                messageDateView.setGravity(Gravity.END);
                messageLayout1.setGravity(Gravity.END);
                messageLayout2.setGravity(Gravity.END);
                messageContentView.setTextColor(getResources().getColor(R.color.message_1_text));
                messageDateView.setTextColor(getResources().getColor(R.color.message_1_text));
                messageContentView.setBackgroundColor(getResources().getColor(R.color.message_1_background));
                messageDateView.setBackgroundColor(getResources().getColor(R.color.message_1_background));
            }
            else{ //message from otheruser to currentuser
                messageContentView.setGravity(Gravity.START);
                messageDateView.setGravity(Gravity.START);
                messageLayout1.setGravity(Gravity.START);
                messageLayout2.setGravity(Gravity.START);
                messageContentView.setTextColor(getResources().getColor(R.color.message_2_text));
                messageDateView.setTextColor(getResources().getColor(R.color.message_2_text));
                messageContentView.setBackgroundColor(getResources().getColor(R.color.message_2_background));
                messageDateView.setBackgroundColor(getResources().getColor(R.color.message_2_background));
            }

            return row;
        }


    }

    @Override
    public void onCreateOptionsMenu(final Menu menu,final MenuInflater inflater) {
        inflater.inflate(R.menu.messages, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_refresh_message){
            refreshMessages();
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
    }



}
