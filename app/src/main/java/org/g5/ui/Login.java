package org.g5.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;
import org.g5.util.LineIO;

import java.io.File;
import java.io.IOException;

public class Login extends AppCompatActivity {

    private Button agree;
    private Button noAgree;
    private EditText usernameField;
    private EditText passwordField;
    private Button submit;
    private static File fileDirectory;
    private static String[] accountInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        fileDirectory = new File(getFilesDir().toString());

        File accountFile = new File(fileDirectory, "accounts.txt");

        try {
            if (!accountFile.exists())
                accountFile.createNewFile();
        } catch (IOException e) {}

        LineIO accountWriter = new LineIO(accountFile);

        String username = accountWriter.getLine(0);
        String password = accountWriter.getLine(1);
        Log.d("Login--", "Username: " + username + " Password: " + password);

        if (username.contains("[un]:") && password.contains("[pw]:")) {
            username = username.replace("[un]:", "");
            password = password.replace("[pw]:", "");
            if (!username.isEmpty() && !password.isEmpty()) {
                Login.setAccount(username, password);
                startActivity(new Intent(this, Home.class));
                finish();
            }
        }

        agree = findViewById(R.id.agree);
        noAgree = findViewById(R.id.noAgree);

        agree.setOnClickListener(view -> {
            findViewById(R.id.permission).setVisibility(View.INVISIBLE);
            findViewById(R.id.filter).setVisibility(View.INVISIBLE);
            findViewById(R.id.signUp).setVisibility(View.VISIBLE);
        });
        noAgree.setOnClickListener(view -> {
            finishAffinity();
        });

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        submit = findViewById(R.id.signUp);

        submit.setOnClickListener(view -> {
            if (!usernameField.getText().toString().isEmpty() && !passwordField.getText().toString().isEmpty()) {
                accountWriter.writeLine("[un]:" + usernameField.getText().toString());
                accountWriter.writeLine("[pw]:" + passwordField.getText().toString());
                accountInfo = new String[] {
                        usernameField.getText().toString(),
                        passwordField.getText().toString()
                };
                startActivity(new Intent(Login.this, Home.class));
            } else {
                findViewById(R.id.missingFieldText).setVisibility(View.VISIBLE);
            }
        });
    }

    public static String[] getAccount() {
        return (accountInfo != null) ? accountInfo : new String[] {"", ""};
    }

    public static void setAccount(String name, String password) {
        accountInfo = new String[] {
                name.replace("[un]:", ""), password.replace("[pw]:", "")
        };
    }
}
