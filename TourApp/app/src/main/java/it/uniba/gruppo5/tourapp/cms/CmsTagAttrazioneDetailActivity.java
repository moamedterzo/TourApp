package it.uniba.gruppo5.tourapp.cms;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.CategoriaAttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.TagAttrazioneDAO;
import it.uniba.gruppo5.tourapp.utilities.CustomSpinnerHelper;
import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

public class CmsTagAttrazioneDetailActivity extends CmsBaseActivity {

    private final static String BUNDLE_IMAGE = "DB_image";
    private final static String BUNDLE_EDIT_MODE   = "DB_edit_mode";

    //validatore dei dati
    private AwesomeValidation awesomeValidation;


    ImageView immaginePrincipale;
    private Uri filePath;
    Dialog dialog;
    Spinner spinner_categoria;
    CustomSpinnerHelper customSpinnerHelperCategoria;

    EditText et_titolo;
    EditText et_titoloItaliano;

    String pathImmagine;
    boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_tag_attrazione_detail);
        setMenuAndDrawer();

        immaginePrincipale = findViewById(R.id.immagine);

        et_titolo = findViewById(R.id.titolo);
        et_titoloItaliano = findViewById(R.id.titoloItaliano);

        spinner_categoria = findViewById(R.id.spinner_categoria);
        customSpinnerHelperCategoria = new CustomSpinnerHelper();

        //imposto validazione
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(et_titolo, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_titoloItaliano, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));


        CategoriaAttrazioneDAO.getCategorie(null, new ReadValueListener<ArrayList<CategoriaAttrazioneDAO.Categoria>>() {
            @Override
            public void onDataRead(ArrayList<CategoriaAttrazioneDAO.Categoria> result) {

                List<Pair<String, String>> valoriSpinner = new ArrayList<>();

                if(!editMode)
                    valoriSpinner.add(new Pair<>(getString(R.string.seleziona), ""));

                for(CategoriaAttrazioneDAO.Categoria categoria : result)
                {
                    valoriSpinner.add(new Pair<>(categoria.Titolo ,categoria.ID));
                }

                customSpinnerHelperCategoria.setSpinnerAndValues(CmsTagAttrazioneDetailActivity.this,spinner_categoria, valoriSpinner);
            }
        });

        //primo caricamento
        if(savedInstanceState==null){
            loadDati();
        }
    }


    private void loadDati(){

        Intent intent = getIntent();
        String idTag = intent.getStringExtra("ID");

        if(idTag == null){
            //modalità nuovo
            editMode = false;
        }
        else {
            //modalità modifica
            editMode = true;

            TagAttrazioneDAO.getSingleTag(this, new ReadValueListener<TagAttrazioneDAO.Tag>() {
                @Override
                public void onDataRead(TagAttrazioneDAO.Tag result) {

                    et_titolo.setText(result.Titolo);
                    et_titoloItaliano.setText(result.TitoloItaliano);
                    customSpinnerHelperCategoria.setSpinnerSelectedValue(result.IDCategoria);

                    //lettura file
                    result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                        @Override
                        public void onFileRead(String absolutePath) {

                            //salvataggio in memoria
                            pathImmagine = absolutePath;

                            Glide.with(CmsTagAttrazioneDetailActivity.this).load(new File(absolutePath)).into(immaginePrincipale);
                        }
                    });
                }
            }, idTag);
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio immagine
        savedInstanceState.putString(BUNDLE_IMAGE, pathImmagine);
        savedInstanceState.putBoolean(BUNDLE_EDIT_MODE, editMode);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato corrente
        pathImmagine = savedInstanceState.getString(BUNDLE_IMAGE);
        if(pathImmagine != null && !pathImmagine.isEmpty()){
            Bitmap bitmap = BitmapFactory.decodeFile(pathImmagine);
            immaginePrincipale.setImageBitmap(bitmap);
        }

        editMode = savedInstanceState.getBoolean(BUNDLE_EDIT_MODE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cms_tag_attrazione_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_elimina);
        menuItem.setVisible(editMode);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_salva:
                salvaDati();
                return true;

            case R.id.menu_elimina:
                eliminaTag();
                return true;

            case R.id.menu_carica_immagine:
                popmenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //modificare colori e testo del DialogFragment
    void popmenu() {

        dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.cameradialog);

        dialog.getWindow().setBackgroundDrawable(

                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView mCamerabtn = dialog.findViewById(R.id.cameradialogbtn);

        ImageView mGallerybtn = dialog.findViewById(R.id.gallerydialogbtn);

        TextView btnCancel = dialog.findViewById(R.id.canceldialogbtn);


        dialog.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT,

                ViewGroup.LayoutParams.FILL_PARENT);


        mCamerabtn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                dispatchTakePictureIntent();
            }

        });


        mGallerybtn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {


                pickPhotoFromGallery();

            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                dialog.cancel(); // dismissing the popup

            }

        });

        dialog.show();
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int RESULT_LOAD_IMAGE = 2;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void pickPhotoFromGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            File fileImage = ImageSaver.saveBitmapImage(this, imageBitmap);

            //salvataggio memoria
            filePath = Uri.fromFile(fileImage);
            pathImmagine = fileImage.getAbsolutePath();

            //mostro immagine
            Bitmap bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());
            immaginePrincipale.setImageBitmap(bitmap);

            Toast.makeText(this, getResources().getString(R.string.foto_ins), Toast.LENGTH_SHORT);

            dialog.cancel();

        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri pathFile = data.getData();
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pathFile);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            File fileImage = ImageSaver.saveBitmapImage(this, imageBitmap);

            //salvataggio memoria
            filePath = Uri.fromFile(fileImage);
            pathImmagine = fileImage.getAbsolutePath();

            //mostro immagine
            Bitmap bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());
            immaginePrincipale.setImageBitmap(bitmap);

            dialog.cancel();
        }
    }



    private void salvaDati(){

        if (awesomeValidation.validate()
                & customSpinnerHelperCategoria.validateRequiredValue(this)) {

            if (editMode) {

                //modifica
                Intent intent = getIntent();
                String idTag = intent.getStringExtra("ID");

                TagAttrazioneDAO.getSingleTag(this, new ReadValueListener<TagAttrazioneDAO.Tag>() {
                    @Override
                    public void onDataRead(TagAttrazioneDAO.Tag result) {

                        result.Titolo = et_titolo.getText().toString();
                        result.TitoloItaliano = et_titoloItaliano.getText().toString();
                        result.IDCategoria = customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();

                        if (filePath != null)
                            result.immaginePrincipale.setNewUriFile(filePath);

                        TagAttrazioneDAO.updateTag(result);

                        Toast.makeText(CmsTagAttrazioneDetailActivity.this, getResources().getString(R.string.dati_salv), Toast.LENGTH_LONG).show();
                    }
                }, idTag);
            } else {

                //creazione
                TagAttrazioneDAO.Tag result = new TagAttrazioneDAO.Tag();
                result.Titolo = et_titolo.getText().toString();
                result.TitoloItaliano = et_titoloItaliano.getText().toString();
                result.IDCategoria = customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();

                if (filePath != null) {
                    result.immaginePrincipale = new FileStorageDAO(this, result.FilePathImmagine);
                    result.immaginePrincipale.setNewUriFile(filePath);
                }
                TagAttrazioneDAO.addTag(result);

                Intent listIntent = new Intent(this, CmsTagAttrazioneActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsTagAttrazioneDetailActivity.this, getResources().getString(R.string.tag_insert), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void eliminaTag(){

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CmsTagAttrazioneDetailActivity.this,R.style.myDialog));
        builder.setCancelable(true);
        builder.setTitle("Elimina tag");
        builder.setMessage("Sei sicuro di voler eliminare questo tag?");
        builder.setIcon(R.drawable.ic_warning_black_24dp);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                String idTag = intent.getStringExtra("ID");

                TagAttrazioneDAO.removeTag(idTag);

                Intent listIntent = new Intent(CmsTagAttrazioneDetailActivity.this, CmsTagAttrazioneDetailActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsTagAttrazioneDetailActivity.this,
                        getResources().getString(R.string.tag_elim), Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();

    }
}
