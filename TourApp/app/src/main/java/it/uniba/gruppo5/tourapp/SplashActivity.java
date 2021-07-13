package it.uniba.gruppo5.tourapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import it.uniba.gruppo5.tourapp.firebase.BaseDAO;
import it.uniba.gruppo5.tourapp.firebase.DatiGeneraliDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;

public class SplashActivity extends AppCompatActivity {

    private static final String SHARED_PREFS_FIRST_RUN = "sp_fr";

    boolean okAhead = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){
           goAhead();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //inizializzazione dell'app necessaria per firebase
        BaseDAO.initializeApp(this);
    }



    @Override
    protected void onStart() {
        super.onStart();

        //LETTURA NOME CITTA'
        DatiGeneraliDAO.getDatiGenerali(this, new ReadValueListener<DatiGeneraliDAO.DatiGenerali>() {
            @Override
            public void onDataRead(DatiGeneraliDAO.DatiGenerali result) {
                //immagine
                result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                    @Override
                    public void onFileRead(String absolutePath) {

                        //ok, immagine scaricata, procedo sulla prossima activity
                        goAhead();
                    }
                });
            }
        });


        //dopo 4 secondi procedo alla prossima schermata in ogni caso
        mHandler.sendMessageDelayed( mHandler.obtainMessage(),4000);
    }


    private void goAhead()
    {
        if(!okAhead) {
            //controllo se è la prima volta che viene aperta l'app
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean previouslyStarted = prefs.getBoolean(SHARED_PREFS_FIRST_RUN, false);

            Intent intent;

            if (!previouslyStarted) {
                //salvo impostazione
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(SHARED_PREFS_FIRST_RUN, Boolean.TRUE);
                edit.commit();

                //setup activity
                intent = new Intent(SplashActivity.this, SetupActivity.class);
            } else {
                //home activity
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            }

            okAhead = true;

            //redirect to correct activity
            startActivity(intent);
            finish();
        }
    }
}
