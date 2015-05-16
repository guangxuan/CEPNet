package vintgug.cepnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        final EditText UsernameItem = (EditText) findViewById(R.id.UsernameEditText);
        final EditText PasswordItem = (EditText) findViewById(R.id.PasswordEditText);
        final EditText EmailItem = (EditText) findViewById(R.id.EmailEditText);

        Button SignupButton = (Button) findViewById(R.id.SignupButton);
        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mUsername = UsernameItem.getText().toString();
                String mPassword = PasswordItem.getText().toString();
                String mEmail = EmailItem.getText().toString();
                Parse.enableLocalDatastore(SignupActivity.this);
                Parse.initialize(SignupActivity.this, "JtaKlQ5NurWM8FbL9IHNnMmmvq849WObMPnpycZS", "qCreENjPxQSE6BVW6S3gV60ojU0lCisyGudJATqD");
                ParseUser newUser = new ParseUser();
                newUser.setUsername(mUsername);
                newUser.setPassword(mPassword);
                newUser.setEmail(mEmail);
                newUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e == null) {
                            //Success!
                            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                            builder.setMessage(e.getMessage())
                                    .setTitle("Oops!")
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
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
}
