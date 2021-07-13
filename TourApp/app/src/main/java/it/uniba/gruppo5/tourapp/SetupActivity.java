package it.uniba.gruppo5.tourapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Button btnOspite = findViewById(R.id.btnOspite);
        btnOspite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SetupActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button btnIscriviti = findViewById(R.id.btnIscriviti);
        btnIscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this,RegistrazioneActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
