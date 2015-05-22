package vintgug.cepnet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView=inflater.inflate(R.layout.fragment_profile, container, false);

        ParseUser currentUser = ParseUser.getCurrentUser();

        TextView UsernameTextView= (TextView)rootView.findViewById(R.id.UsernameTextView);
        TextView EmailTextView= (TextView)rootView.findViewById(R.id.EmailTextView);
        UsernameTextView.setText(currentUser.getUsername());
        EmailTextView.setText(currentUser.getEmail());
        return rootView;

    }

}
