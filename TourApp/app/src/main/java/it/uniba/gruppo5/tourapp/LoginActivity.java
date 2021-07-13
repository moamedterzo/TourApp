package it.uniba.gruppo5.tourapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.cms.CmsHomeActivity;
//import 'supplemental/cut_corners_border.dart';


public class LoginActivity extends BaseActivity {


    //validatore dei dati
    private AwesomeValidation awesomeValidation;


    Button btnSign_in;
    Button btnLogin;
    EditText etLogin;
    EditText etPsw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setMenuAndDrawer();

        etLogin= findViewById(R.id.txtLogin);
        etPsw= findViewById(R.id.txtPsw);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(btnLoginClickListener);

        btnSign_in = findViewById(R.id.btnIscriviti);


        //imposto validazione
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(etLogin, Patterns.EMAIL_ADDRESS, getResources().getString(R.string.errore_email_valida));
        awesomeValidation.addValidation(etPsw, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));

        btnSign_in.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               Intent  intent = new Intent(LoginActivity.this, RegistrazioneActivity.class);
                startActivity(intent);
            }
        });



    }

    private View.OnClickListener btnLoginClickListener = new View.OnClickListener(){
        public void onClick(View view){

            if (awesomeValidation.validate()) {

                UserAuthenticationManager.AuthenticateUser(getApplicationContext(), etLogin.getText().toString(), etPsw.getText().toString(), new UserAuthenticationManager.AuthenticationListener() {
                    @Override
                    public void onUserAuthenticationRequestCompleted(boolean isUserAuthenticated) {
                        if (isUserAuthenticated) {
                            //ok, loggato
                            Intent cmsHomeIntent = new Intent(getApplicationContext(), CmsHomeActivity.class);
                            startActivity(cmsHomeIntent);

                            Toast.makeText(getApplicationContext(), getResources().getString(R.string. log_suc), Toast.LENGTH_LONG).show();
                            finish();
                        } else {

                            Toast.makeText(getApplicationContext(), getResources().getString(R.string. cred_err), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    };
}
