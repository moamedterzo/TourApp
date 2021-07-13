package it.uniba.gruppo5.tourapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.File;

import it.uniba.gruppo5.tourapp.sqllite.DBLocaleDiario;

public class MostraImmagineActivity extends AppCompatActivity {

    public final static String INTENT_ALL_IMG_PATH = "percorsoImg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostra_immagine);
        Intent intent = getIntent();

        String nomeImg = intent.getStringExtra(INTENT_ALL_IMG_PATH);
        if(nomeImg!=null) {
            Bitmap bmp = BitmapFactory.decodeFile(DBLocaleDiario.IMMAGINI_DIARIO_DIRECTORY + File.separator + nomeImg);
            ImageView imgDettaglio = findViewById(R.id.imgExpand);
            imgDettaglio.setImageBitmap(bmp);
        }

    }
}
