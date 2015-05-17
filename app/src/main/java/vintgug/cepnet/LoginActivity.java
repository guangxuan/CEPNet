package vintgug.cepnet;

import android.app.Activity;
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

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


public class LoginActivity extends Activity {

    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText UsernameItem = (EditText) findViewById(R.id.UsernameEditText);
        final EditText PasswordItem = (EditText) findViewById(R.id.PasswordEditText);

        final ProgressBar loginProgress=(ProgressBar)findViewById(R.id.loginProgress);

        ImageButton LoginButton = (ImageButton) findViewById(R.id.LoginButton);
        LoginButton.setClickable(true);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String errorMsg;

                String mUsername = UsernameItem.getText().toString();
                String mPassword = PasswordItem.getText().toString();
                loginProgress.setVisibility(View.VISIBLE);

                if(mUsername.equals("")){
                    errorMsg=getString(R.string.no_username);
                    showToast(errorMsg);
                }
                if(mPassword.equals("")){
                    errorMsg=getString(R.string.no_password);
                    showToast(errorMsg);
                }

                ParseUser.logInInBackground(mUsername, mPassword,
                        new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                loginProgress.setVisibility(View.GONE);
                                if (e == null) {
                                    // If user exist and authenticated, send user to Welcome.class
                                    Intent intent = new Intent(
                                            LoginActivity.this,
                                            HomeActivity.class);
                                    startActivity(intent);
                                    showToast(R.string.login_success);
                                    finish();
                                } else {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "No such user exist, please signup",
                                            Toast.LENGTH_SHORT).show();
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
