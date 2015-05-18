package vintgug.cepnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class LoginActivity extends Activity {

    Toast mToast;
    AlertDialog.Builder builder;
    static final String INTENT_USERNAME="username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        builder=new AlertDialog.Builder(LoginActivity.this);

        final EditText UsernameItem = (EditText) findViewById(R.id.UsernameEditText);
        final EditText PasswordItem = (EditText) findViewById(R.id.PasswordEditText);

        final ProgressBar loginProgress=(ProgressBar)findViewById(R.id.loginProgress);

        Intent intent=getIntent();
        if(intent!=null){
            String username=intent.getStringExtra(INTENT_USERNAME);
            UsernameItem.setText(username);
        }

        ImageButton LoginButton = (ImageButton) findViewById(R.id.LoginButton);
        LoginButton.setClickable(true);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String errorMsg;

                final String mUsername = UsernameItem.getText().toString().trim();
                String mPassword = PasswordItem.getText().toString();
                loginProgress.setVisibility(View.VISIBLE);

                if(mUsername.equals("")){
                    builder=new AlertDialog.Builder(LoginActivity.this);
                    errorMsg=getString(R.string.no_username);
                    builder.setMessage(errorMsg)
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alert=builder.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();
                    return;
                }
                if(mPassword.equals("")){
                    builder=new AlertDialog.Builder(LoginActivity.this);
                    errorMsg=getString(R.string.no_password);
                    builder.setMessage(errorMsg)
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    builder.create().show();
                    AlertDialog alert=builder.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();
                    return;
                }

                ParseUser.logInInBackground(mUsername, mPassword,
                        new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                loginProgress.setVisibility(View.GONE);
                                if (e == null) {
                                    // If user exist and authenticated, send user to home

                                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                                    query.whereEqualTo("Username", mUsername);
                                    query.setLimit(1);
                                    query.findInBackground(new FindCallback<ParseUser>() {
                                        public void done(List<ParseUser> objects, ParseException e) {
                                            if (e == null) {
                                                // The query was successful.
                                                if (!objects.get(0).getBoolean("emailVerified")) {
                                                    builder=new AlertDialog.Builder(LoginActivity.this);
                                                    builder.setMessage(R.string.not_verified)
                                                            .setTitle(R.string.error_title)
                                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent intent = new Intent(
                                                                            LoginActivity.this,
                                                                            HomeActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            });
                                                    AlertDialog alert=builder.create();
                                                    alert.setCanceledOnTouchOutside(false);
                                                    alert.setCancelable(false);
                                                    alert.show();
                                                }
                                            }
                                        }
                                    });
                                }
                                else {
                                    builder=new AlertDialog.Builder(LoginActivity.this);
                                    builder.setMessage(getString(R.string.login_failed))
                                            .setTitle(R.string.error_title)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog alert=builder.create();
                                    alert.setCanceledOnTouchOutside(false);
                                    alert.show();

                                    PasswordItem.setText("");
                                }
                            }
                        });
            }
        });
        TextView SignupButton = (TextView) findViewById(R.id.SignupButton);
        SignupButton.setClickable(true);
        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void showToast(String text){
        if(mToast!=null){
            mToast.cancel();
        }
        mToast=Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }
    void showToast(int text){
        if(mToast!=null){
            mToast.cancel();
        }
        mToast=Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }

}
