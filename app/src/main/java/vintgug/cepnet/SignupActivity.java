package vintgug.cepnet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends Activity {

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        builder = new AlertDialog.Builder(SignupActivity.this);

        final EditText UsernameItem = (EditText) findViewById(R.id.UsernameEditText);
        final EditText PasswordItem = (EditText) findViewById(R.id.PasswordEditText);
        final EditText PasswordItem2 = (EditText) findViewById(R.id.PasswordEditText2);
        final EditText EmailItem = (EditText) findViewById(R.id.EmailEditText);
        TextView LoginButton = (TextView) findViewById(R.id.LoginButton);
        TextView SignupButton = (TextView) findViewById(R.id.SignupButton);
        final ProgressBar SignupProgress=(ProgressBar)findViewById(R.id.SignupProgress);

        LoginButton.setClickable(true);
        SignupButton.setClickable(true);
        SignupProgress.setVisibility(View.GONE);


        SignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mUsername = UsernameItem.getText().toString().trim();
                String mPassword = PasswordItem.getText().toString();
                String mPassword2 = PasswordItem2.getText().toString();
                final String mEmail = EmailItem.getText().toString().trim();
                String errorMsg="";
                if(mUsername.equals("")){
                    errorMsg=getString(R.string.no_username);
                }
                else if(mEmail.equals("")){
                    errorMsg=getString(R.string.no_email);
                }
                else if(mPassword.equals("")){
                    errorMsg=getString(R.string.no_password);
                }
                else if(!mPassword2.equals(mPassword)){
                    errorMsg=getString(R.string.password_doesnt_match);
                }
                if(!errorMsg.equals("")) {
                    builder=new AlertDialog.Builder(SignupActivity.this);
                    builder.setMessage(errorMsg)
                            .setTitle(R.string.error_title)
                            .setPositiveButton(R.string.ok, null);
                    AlertDialog alert = builder.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();
                    return;
                }

                ParseUser newUser = new ParseUser();
                newUser.setUsername(mUsername);
                newUser.setPassword(mPassword);
                newUser.setEmail(mEmail);

                SignupProgress.setVisibility(View.VISIBLE);

                newUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        SignupProgress.setVisibility(View.GONE);
                        if (e == null) {
                            //Success!
                            builder=new AlertDialog.Builder(SignupActivity.this);
                            builder.setTitle(R.string.signup_success_title);
                            builder.setMessage(getString(R.string.signup_success_1) + " " + mEmail + " " + getString(R.string.signup_success_2));
                            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    intent.putExtra(LoginActivity.INTENT_USERNAME, mUsername);
                                    finish();
                                }
                            });
                            AlertDialog alert= builder.create();
                            alert.setCanceledOnTouchOutside(false);
                            alert.setCancelable(false);
                            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    intent.putExtra(LoginActivity.INTENT_USERNAME, mUsername);
                                    finish();
                                }
                            });
                            alert.show();
                        }
                        else {
                            String errorMsg;
                            switch(e.getCode()) {
                                case ParseException.USERNAME_TAKEN:
                                    errorMsg=getString(R.string.username_taken);
                                    break;
                                case ParseException.INVALID_EMAIL_ADDRESS:
                                    errorMsg=getString(R.string.invalid_email);
                                    break;
                                case ParseException.EMAIL_TAKEN:
                                    errorMsg=getString(R.string.username_taken);
                                    break;
                                default:
                                    errorMsg=getString(R.string.signup_failed);
                                    break;
                            }
                            builder=new AlertDialog.Builder(SignupActivity.this);
                            builder.setMessage(errorMsg)
                                    .setTitle(R.string.error_title)
                                    .setPositiveButton(R.string.ok, null);
                            AlertDialog alert=builder.create();
                            alert.setCanceledOnTouchOutside(false);
                            alert.show();
                        }
                    }
                });
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
