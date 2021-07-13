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
import android.support.v4.app.DialogFragment;
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
import java.util.Locale;

import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.CategoriaCouponDAO;
import it.uniba.gruppo5.tourapp.firebase.CouponDAO;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.fragments.DatePickerFragment;
import it.uniba.gruppo5.tourapp.utilities.CustomSpinnerHelper;
import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

public class CmsCouponDetailActivity extends CmsBaseActivity
     implements   DatePickerFragment.OnDateSelectedListener  {

    private final static String BUNDLE_IMAGE = "DB_image";
    private final static String BUNDLE_EDIT_MODE   = "DB_edit_mode";

    //validatore dei dati
    private AwesomeValidation awesomeValidation;

    ImageView immaginePrincipale;
    private Uri filePath;
    Dialog dialog;

    EditText et_titolo;
    EditText et_titoloItaliano;
    EditText et_data_inizio;
    EditText et_data_fine;
    EditText et_importo;
    Spinner spinner_categoria;
    CustomSpinnerHelper customSpinnerHelperCategoria;
    Spinner spinner_tipo_sconto;
    CustomSpinnerHelper customSpinnerHelperTipoSconto;

    String pathImmagine;
    boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_coupon_detail);
        setMenuAndDrawer();

        immaginePrincipale = findViewById(R.id.immagine);

        et_titolo = findViewById(R.id.titolo);
        et_titoloItaliano = findViewById(R.id.titoloItaliano);
        et_data_inizio = findViewById(R.id.et_data_inizio);
        et_data_fine = findViewById(R.id.et_data_fine);
        et_importo = findViewById(R.id.et_importo);
        spinner_categoria = findViewById(R.id.spinner_categoria);
        customSpinnerHelperCategoria = new CustomSpinnerHelper();
        spinner_tipo_sconto = findViewById(R.id.spinner_tipo_sconto);
        customSpinnerHelperTipoSconto = new CustomSpinnerHelper();

        //imposto validazione
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(et_titolo, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_titoloItaliano, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_data_inizio, "^[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_data_fine, "^[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_importo, "^[0-9.]+$", getResources().getString(R.string.errore_campo_obbligatorio));


        et_data_inizio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String text = et_data_inizio.getText().toString();

                DialogFragment fragment = new DatePickerFragment(text);

                //apertura dialog
                fragment.show(getSupportFragmentManager(),"dateStartPicker");
            }
        });

        et_data_fine.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String text = et_data_fine.getText().toString();

                DialogFragment fragment = new DatePickerFragment(text);

                //apertura dialog
                fragment.show(getSupportFragmentManager(),"dateEndPicker");
            }
        });

        //imposto spinner sconto
        List<Pair<String, String>> valoriSpinnerSconto = new ArrayList<>();
        valoriSpinnerSconto.add(new Pair<String, String>("Percentuale", CouponDAO.TIPO_SCONTO_PERCENTUALE));
        valoriSpinnerSconto.add(new Pair<String, String>("Fisso", CouponDAO.TIPO_SCONTO_FISSO));
        customSpinnerHelperTipoSconto.setSpinnerAndValues(this,spinner_tipo_sconto, valoriSpinnerSconto);

        CategoriaCouponDAO.getCategorie(null, new ReadValueListener<ArrayList<CategoriaCouponDAO.Categoria>>() {
            @Override
            public void onDataRead(ArrayList<CategoriaCouponDAO.Categoria> result) {

                List<Pair<String, String>> valoriSpinner = new ArrayList<>();

                if(!editMode)
                    valoriSpinner.add(new Pair<>(getString(R.string.seleziona), ""));

                for(CategoriaCouponDAO.Categoria categoria : result)
                {
                    valoriSpinner.add(new Pair<>(categoria.Titolo ,categoria.ID));
                }

                customSpinnerHelperCategoria.setSpinnerAndValues(CmsCouponDetailActivity.this,spinner_categoria, valoriSpinner);
            }
        });

        //primo caricamento
        if(savedInstanceState==null){
            loadDati();
        }
    }


    private void loadDati(){

        Intent intent = getIntent();
        String idCoupon = intent.getStringExtra("ID");

        if(idCoupon == null){
            //modalità nuovo
            editMode = false;
        }
        else {
            //modalità modifica
            editMode = true;

            CouponDAO.getSingleCoupon(this, new ReadValueListener<CouponDAO.Coupon>() {
                @Override
                public void onDataRead(CouponDAO.Coupon result) {

                    et_titolo.setText(result.Titolo);
                    et_titoloItaliano.setText(result.TitoloItaliano);
                    et_data_inizio.setText(result.DataInizioValidita);
                    et_data_fine.setText(result.DataFineValidita);
                    et_importo.setText(String.valueOf(result.Importo));
                    customSpinnerHelperCategoria.setSpinnerSelectedValue(result.IDCategoria);
                    customSpinnerHelperTipoSconto.setSpinnerSelectedValue(result.TipoSconto);

                    //lettura file
                    result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                        @Override
                        public void onFileRead(String absolutePath) {

                            //salvataggio in memoria
                            pathImmagine = absolutePath;

                            Glide.with(CmsCouponDetailActivity.this).load(new File(absolutePath)).into(immaginePrincipale);
                        }
                    });
                }
            }, idCoupon);
        }
    }

    @Override
    public void onDateSelected(int year, int month,int day, String tag){

        if(tag.equals("dateEndPicker"))
            et_data_fine.setText(String.format(Locale.ITALIAN, DatePickerFragment.FORMAT_EDIT_TEXT,day,month ,year));
        else
            et_data_inizio.setText(String.format(Locale.ITALIAN, DatePickerFragment.FORMAT_EDIT_TEXT,day,month ,year));
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
        getMenuInflater().inflate(R.menu.menu_cms_coupon_detail, menu);

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
                eliminaCoupon();
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

            Toast.makeText(this,
                    getResources().getString(R.string. foto_ins)
                    , Toast.LENGTH_SHORT);

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
                & customSpinnerHelperCategoria.validateRequiredValue(this)
                & customSpinnerHelperTipoSconto.validateRequiredValue(this)) {

            if (editMode) {

                //modifica
                Intent intent = getIntent();
                String idCoupon = intent.getStringExtra("ID");

                CouponDAO.getSingleCoupon(this, new ReadValueListener<CouponDAO.Coupon>() {
                    @Override
                    public void onDataRead(CouponDAO.Coupon result) {

                        result.Titolo = et_titolo.getText().toString();
                        result.TitoloItaliano = et_titoloItaliano.getText().toString();
                        result.DataInizioValidita = et_data_inizio.getText().toString();
                        result.DataFineValidita = et_data_fine.getText().toString();
                        result.Importo = Float.parseFloat(et_importo.getText().toString());
                        result.IDCategoria = customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();
                        result.TipoSconto = customSpinnerHelperTipoSconto.getSpinnerSelectedValue().toString();

                        if (filePath != null)
                            result.immaginePrincipale.setNewUriFile(filePath);

                        CouponDAO.updateCoupon(result);

                        Toast.makeText(CmsCouponDetailActivity.this, getResources().getString(R.string.dati_salv), Toast.LENGTH_LONG).show();
                    }
                }, idCoupon);
            } else {

                //creazione
                CouponDAO.Coupon result = new CouponDAO.Coupon();
                result.Titolo = et_titolo.getText().toString();
                result.TitoloItaliano = et_titoloItaliano.getText().toString();
                result.DataInizioValidita = et_data_inizio.getText().toString();
                result.DataFineValidita = et_data_fine.getText().toString();
                result.Importo = Float.parseFloat(et_importo.getText().toString());
                result.IDCategoria = customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();
                result.TipoSconto = customSpinnerHelperTipoSconto.getSpinnerSelectedValue().toString();

                if (filePath != null) {
                    result.immaginePrincipale = new FileStorageDAO(this, result.FilePathImmagine);
                    result.immaginePrincipale.setNewUriFile(filePath);
                }
                CouponDAO.addCoupon(result);

                Intent listIntent = new Intent(this, CmsCouponActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsCouponDetailActivity.this, getResources().getString(R.string.insert_coupon)
                        , Toast.LENGTH_LONG).show();
            }
        }
    }


    private void eliminaCoupon(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CmsCouponDetailActivity.this,R.style.myDialog));
        builder.setCancelable(true);
        builder.setTitle("Elimina coupon");
        builder.setMessage("Sei sicuro di voler eliminare questo coupon?");
        builder.setIcon(R.drawable.ic_warning_black_24dp);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                String idCoupon = intent.getStringExtra("ID");

                CouponDAO.removeCoupon(idCoupon);

                Intent listIntent = new Intent(CmsCouponDetailActivity.this, CmsCouponActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsCouponDetailActivity.this,
                        getResources().getString(R.string.elim_coupon)
                        , Toast.LENGTH_LONG).show();
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
