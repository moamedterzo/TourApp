package it.uniba.gruppo5.tourapp.cms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.bumptech.glide.Glide;

import java.io.File;

import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.DatiGeneraliDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

public class CmsDatiGeneraliActivity extends CmsBaseActivity {

    private final static String BUNDLE_IMAGE = "DB_image";

    //validatore dei dati
    private AwesomeValidation awesomeValidation;

    private EditText etNomeCitta;
    private ImageView imageView;

    private Uri filePath;
    String pathImmagine;
    private final int PICK_IMAGE_REQUEST = 71;

    private DatiGeneraliDAO.DatiGenerali datiGenerali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_dati_generali);
        setMenuAndDrawer();


        imageView = findViewById(R.id.imgView);
        etNomeCitta = findViewById(R.id.etNomeCitta);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(etNomeCitta, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));


        //primo caricamento
        if (savedInstanceState == null) {
            loadDati();
        }
    }

    private void loadDati() {
        DatiGeneraliDAO.getDatiGenerali(this, new ReadValueListener<DatiGeneraliDAO.DatiGenerali>() {

            @Override
            public void onDataRead(DatiGeneraliDAO.DatiGenerali result) {

                datiGenerali = result;

                etNomeCitta.setText(result.NomeCitta);

                //lettura file
                result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                    @Override
                    public void onFileRead(String absolutePath) {

                        pathImmagine = absolutePath;
                        Glide.with(CmsDatiGeneraliActivity.this).load(new File(absolutePath)).into(imageView);
                    }
                });
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio immagine
        savedInstanceState.putString(BUNDLE_IMAGE, pathImmagine);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato corrente
        pathImmagine = savedInstanceState.getString(BUNDLE_IMAGE);
        if (pathImmagine != null && !pathImmagine.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(pathImmagine);
            imageView.setImageBitmap(bitmap);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dati_generali, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_salva:
                salvaDati();
                return true;

            case R.id.menu_carica_immagine:
                caricaImmagine();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void caricaImmagine() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            Uri pathFile = data.getData();
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pathFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            File fileImage = ImageSaver.saveBitmapImage(this, imageBitmap);

            //salvataggio memoria
            filePath = Uri.fromFile(fileImage);
            pathImmagine = fileImage.getAbsolutePath();

            //mostro immagine
            Bitmap bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());
            imageView.setImageBitmap(bitmap);

        }
    }

    private void salvaDati() {

        if (awesomeValidation.validate()) {
            datiGenerali.NomeCitta = etNomeCitta.getText().toString();

            if (filePath != null)
                datiGenerali.immaginePrincipale.setNewUriFile(filePath);

            DatiGeneraliDAO.updateDatiGenerali(datiGenerali);

            Toast.makeText(CmsDatiGeneraliActivity.this,
                    getResources().getString(R.string. dati_salv), Toast.LENGTH_LONG).show();
        }
    }

}

