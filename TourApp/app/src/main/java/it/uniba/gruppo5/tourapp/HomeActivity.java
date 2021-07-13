package it.uniba.gruppo5.tourapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import it.uniba.gruppo5.tourapp.firebase.DatiGeneraliDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;


public class HomeActivity extends BaseActivity {
    Button explore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setMenuAndDrawer();

          explore = findViewById(R.id.explorButton);

        final EditText txtSearch = HomeActivity.this.findViewById(R.id.txtSearch);

        //LETTURA NOME CITTA'
        DatiGeneraliDAO.getDatiGenerali(this, new ReadValueListener<DatiGeneraliDAO.DatiGenerali>() {
            @Override
            public void onDataRead(DatiGeneraliDAO.DatiGenerali result) {


                String testoRicerca = getResources().getString(R.string.search_def) + " " + result.NomeCitta;

                txtSearch.setHint(testoRicerca);

                //immagine

                result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                    @Override
                    public void onFileRead(String absolutePath) {

                        Drawable bitmap = Drawable.createFromPath(absolutePath);

                        ConstraintLayout rltLay = findViewById(R.id.rltLay);
                        rltLay.setBackground(bitmap);
                    }
                });
            }
        });


        //bottone per accedere all'activity esplora
        explore.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                Intent exploreIntent = new Intent(HomeActivity.this, AttrazioniActivity.class);
                exploreIntent.putExtra(AttrazioniActivity.INTENT_DESCRIZIONE,txtSearch.getText().toString());
                startActivity(exploreIntent);
            }
        });
    }


}


