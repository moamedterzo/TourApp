package it.uniba.gruppo5.tourapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import it.uniba.gruppo5.tourapp.sqllite.DBLocaleDiario;

public class DettaglioDiarioActivity extends BaseActivity {

    public final static String INTENT_PATH_IMAGE = "percorsoImg";

    EditText editDettagli ;
    EditText editLuogo ;
    TextView txtData;
    String nomeImg;

    DBLocaleDiario db_local;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettaglio_diario);
        setMenuAndDrawer();

        //apertura db
        db_local = new DBLocaleDiario(this).open();

        editDettagli = findViewById(R.id.txtDettagli);
        editLuogo = findViewById(R.id.txtLuogo);
        txtData = findViewById(R.id.txtData);


        //gestisco l'intent ricevuto e assegno il valore a nomeImg
        Intent intent_img = getIntent();
        nomeImg = intent_img.getStringExtra(INTENT_PATH_IMAGE);

        if(nomeImg!=null) {
            Cursor c = db_local.restituisciRiga(nomeImg); // restituisco la riga corrispondente a nomeImg

            if (c.moveToNext()) {
                String dettagli = c.getString(c.getColumnIndex(DBLocaleDiario.KEY_DETTAGLI));
                String data = c.getString(c.getColumnIndex(DBLocaleDiario.KEY_DATA));
                String luogo = c.getString(c.getColumnIndex(DBLocaleDiario.KEY_LUOGO));

                editDettagli.setText(dettagli);
                editLuogo.setText(luogo);
                txtData.setText(data);
            }

            Bitmap bmp = BitmapFactory.decodeFile(DBLocaleDiario.IMMAGINI_DIARIO_DIRECTORY + File.separator + nomeImg);

            //setto l'immagine attuale , essa cambierà im base all'immagine cliccata
            ImageView imgDettaglio = findViewById(R.id.imgDettaglio);
            imgDettaglio.setImageBitmap(bmp);
            imgDettaglio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent expandeImage = new Intent(DettaglioDiarioActivity.this ,MostraImmagineActivity.class);
                    expandeImage.putExtra(INTENT_PATH_IMAGE,nomeImg);
                    startActivity(expandeImage);

                }
            });
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        if(db_local!= null)
            db_local.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dettaglio_diario, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_salva:
                salvaDati();
                return true;

            case R.id.menu_elimina:
                eliminaImmagine();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void salvaDati(){
        db_local.modificaImmagine(nomeImg , editDettagli.getText().toString() , editLuogo.getText().toString());

        Toast.makeText(this,getString(R.string.dati_salv),Toast.LENGTH_LONG).show();
    }

    private void eliminaImmagine(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DettaglioDiarioActivity.this,R.style.myDialog));
        builder.setCancelable(true);
        builder.setTitle("Elimina Immagine");
        builder.setMessage("Sei sicuro di voler eliminare questa immagine?");
        builder.setIcon(R.drawable.ic_warning_black_24dp);

        builder.setPositiveButton("Conferma",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //elimino dal database
                        db_local.eliminaImmagine(nomeImg);

                        //elimino dalla cartella contente l'immagine
                        File myDir = new File(DBLocaleDiario.IMMAGINI_DIARIO_DIRECTORY);
                        File img = new File (myDir, nomeImg);
                        if(img.exists()){
                            img.delete();
                        }

                        Intent intent = new Intent(DettaglioDiarioActivity.this , DiarioActivity.class);
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();

    }

}
