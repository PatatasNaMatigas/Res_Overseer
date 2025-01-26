package org.g5.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.g5.overseer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(getFilesDir(), "accounts.txt")))) {
            String username = reader.readLine();
            String password = reader.readLine();

            if (username != null && password != null && username.contains("[un]:") && password.contains("[pw]:"))
                Login.setAccount(username, password);
            startActivity(new Intent(this, Home.class));
        } catch (IOException e) {
            startActivity(new Intent(this, Home.class));
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
                try (FileWriter fileWriter = new FileWriter(accountFile)) {
                    fileWriter.write("[un]:" + usernameField.getText().toString());
                    fileWriter.write("\n");
                    fileWriter.write("[pw]:" + passwordField.getText().toString());
                    fileWriter.write("\n");

                    accountInfo = new String[] {
                            usernameField.getText().toString(),
                            passwordField.getText().toString()
                    };
                } catch (IOException e) {}
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
