package it.uniba.gruppo5.tourapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Locale;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;
import it.uniba.gruppo5.tourapp.fragments.CambioPasswordFragment;
import it.uniba.gruppo5.tourapp.fragments.DatePickerFragment;
import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

import static it.uniba.gruppo5.tourapp.fragments.DatePickerFragment.FORMAT_EDIT_TEXT;

public class SettingsActivity extends BaseActivity
        implements CambioPasswordFragment.OnFragmentInteractionListener,
        DatePickerFragment.OnDateSelectedListener{

    private final static String BUNDLE_IMAGE = "DB_image";
    private static final String BUNDLE_PASSWORD = "PSW";


    //validatore dei dati
    private AwesomeValidation awesomeValidation;

    Dialog dialog;
    ImageView imageProfilo;
    private Uri filePath;
    String pathImmagine;


    EditText et_nominativo;
    EditText et_email;
    EditText et_data;
    String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setMenuAndDrawer();


        imageProfilo = findViewById(R.id.imgProfilo);
        imageProfilo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                popmenu();
            }
        });


        et_nominativo = findViewById(R.id.et_nominativo);
        et_email = findViewById(R.id.et_email);
        et_data = findViewById(R.id.et_data);

        //imposto validazione
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(et_nominativo, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_email, Patterns.EMAIL_ADDRESS, getResources().getString(R.string.errore_email_valida));
        awesomeValidation.addValidation(et_data, "^[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}$", getResources().getString(R.string.errore_campo_obbligatorio));


        et_data.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String text = et_data.getText().toString();

                DialogFragment fragment = new DatePickerFragment(text);

                //apertura dialog
                fragment.show(getSupportFragmentManager(),"dateBirthPicker");

            }
        });

        //primo caricamento
        if(savedInstanceState==null){
           loadDati();
        }
    }

    private void loadDati(){
        String idUtente = UserAuthenticationManager.getUserID(this);

        UtenteDAO.getSingleUtente(this, new ReadValueListener<UtenteDAO.Utente>() {
            @Override
            public void onDataRead(UtenteDAO.Utente result) {

                et_data.setText(result.DataNascita);
                et_email.setText(result.Email);
                et_nominativo.setText(result.Nominativo);

                password = result.Password;

                //lettura file
                result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                    @Override
                    public void onFileRead(String absolutePath) {
                        Glide.with(SettingsActivity.this).load(new File(absolutePath)).into(imageProfilo);
                    }
                });
            }
        }, idUtente);
    }

    @Override
    public void onDateSelected(int year, int month,int day, String tag){
        et_data.setText(String.format(Locale.ITALIAN, FORMAT_EDIT_TEXT,day,month ,year));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio stato corrente
        savedInstanceState.putString(BUNDLE_IMAGE, pathImmagine);
        savedInstanceState.putString(BUNDLE_PASSWORD, password);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato ricerca corrente
        password = savedInstanceState.getString(BUNDLE_PASSWORD);

        pathImmagine = savedInstanceState.getString(BUNDLE_IMAGE);
        if(pathImmagine != null && !pathImmagine.isEmpty()){
            Bitmap bitmap = BitmapFactory.decodeFile(pathImmagine);
            imageProfilo.setImageBitmap(bitmap);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_salva:
                salvaDati();
                return true;

            case R.id.menu_carica_immagine:
                popmenu();
                return true;


            case R.id.menu_modifica_password:
                modificaPassword();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void modificaPassword(){

        CambioPasswordFragment cambioPasswordFragment= new CambioPasswordFragment();

        Bundle bundle = new Bundle();
        bundle.putString(CambioPasswordFragment.ARG_VECCHIA_PSW, password);
        cambioPasswordFragment.setArguments(bundle);

        cambioPasswordFragment.show(getSupportFragmentManager(), SettingsActivity.class.getName());
    }

    @Override
    public void onChangePassword(String newPassword){
        //aggiornamento valore password in memoria
        this.password = newPassword;

        Toast.makeText(this,getResources().getString(R.string.salva_mod),Toast.LENGTH_LONG).show();
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
            imageProfilo.setImageBitmap(bitmap);

            Toast.makeText(this,
                    getResources().getString(R.string.foto_ins), Toast.LENGTH_SHORT);

            dialog.cancel();

        }
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null ){
            filePath = data.getData();

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
            imageProfilo.setImageBitmap(bitmap);

            dialog.cancel();
        }
    }



    private void salvaDati(){

        if (awesomeValidation.validate()) {

            String idUtente = UserAuthenticationManager.getUserID(this);

            UtenteDAO.getSingleUtente(this, new ReadValueListener<UtenteDAO.Utente>() {
                @Override
                public void onDataRead(UtenteDAO.Utente result) {

                    result.Email = et_email.getText().toString();
                    result.DataNascita = et_data.getText().toString();
                    result.Nominativo = et_nominativo.getText().toString();
                    result.Password = password;

                    if (filePath != null)
                        result.immaginePrincipale.setNewUriFile(filePath);

                    UtenteDAO.updateUtente(result);

                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.dati_salv)
                            , Toast.LENGTH_LONG).show();
                }
            }, idUtente);
        }

    }
}




