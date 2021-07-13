package it.uniba.gruppo5.tourapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.util.Locale;
import java.util.regex.Pattern;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;
import it.uniba.gruppo5.tourapp.fragments.DatePickerFragment;
import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

import static it.uniba.gruppo5.tourapp.fragments.DatePickerFragment.FORMAT_EDIT_TEXT;

public class RegistrazioneActivity extends BaseActivity
implements DatePickerFragment.OnDateSelectedListener {

    private final static String BUNDLE_IMAGE = "DB_image";


    //validatore dei dati
    private AwesomeValidation awesomeValidation;


    private static final int RC_SIGN_IN = 4;
    TextInputLayout nomeInputLayout, emailInputLayout, dateInputLayout, passwordInputLayout;
    TextInputEditText nomeEditText, emailEditText, dateEditText, passEditText;
    Button registrati, scatta_foto;
    ImageView foto_profilo;
    TextView text_foto;
    GoogleSignInClient mGoogleSignInClient;
    private Uri filePath;
    Dialog dialog;

    String pathImmagine;


    static Pattern EMAIL_ADDRESS_PATTERN = Pattern
            .compile("[a-zA-Z0-9+._%-+]{1,256}" + "@"
                    + "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" + "(" + "."
                    + "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" + ")+");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);
        setMenuAndDrawer();

        nomeEditText = findViewById(R.id.textInput_nome);
        nomeInputLayout = findViewById(R.id.nomeInputLayout);

        emailEditText = findViewById(R.id.textInput__email);
        emailInputLayout = findViewById(R.id.emailInputLayout);

        dateEditText = findViewById(R.id.textInput__date);
        dateInputLayout = findViewById(R.id.dateInputLayout);

        passEditText = findViewById(R.id.textInput_pass);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        //imposto validazione
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(nomeEditText, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(emailEditText, Patterns.EMAIL_ADDRESS, getResources().getString(R.string.errore_email_valida));
        awesomeValidation.addValidation(dateEditText, "^[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(passEditText, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));


        scatta_foto = findViewById(R.id.btn_foto);
        registrati = findViewById(R.id.btn_registrati);
        foto_profilo = findViewById(R.id.imageView1);
        text_foto = findViewById(R.id.text_foto);
        dialog = new Dialog(RegistrazioneActivity.this);


        //da vedere se funziona
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        dateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String text = dateEditText.getText().toString();

                DialogFragment fragment = new DatePickerFragment(text);

                //apertura dialog
                fragment.show(getSupportFragmentManager(),"dateBirthPicker");

            }
        });


        //controllo finale sul corretto inserimento di tutti i campi
        registrati.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (awesomeValidation.validate()) {

                    //controllo esistenza dell'email
                    UtenteDAO.getUtenteFromEmail(emailEditText.getText().toString(), new ReadValueListener<UtenteDAO.Utente>() {
                        @Override
                        public void onDataRead(UtenteDAO.Utente result) {

                            //se non c'è un risultato vuol dire che l'email è inutilizzata
                            if(result == null) {
                                UtenteDAO.Utente utente = new UtenteDAO.Utente(nomeEditText.getText().toString(), emailEditText.getText().toString(), dateEditText.getText().toString(), passEditText.getText().toString());

                                //il tipo di utente creato è un visitatore
                                utente.Tipo = UtenteDAO.TIPO_UTENTE_VISITATORE;

                                if(filePath!=null) {
                                    utente.immaginePrincipale =  new FileStorageDAO(RegistrazioneActivity.this, utente.FilePathImmagine);
                                    utente.immaginePrincipale.setNewUriFile(filePath);
                                }

                                UtenteDAO.addUtente(utente);

                                //autentico il nuovo utente
                                UserAuthenticationManager.AuthenticateUser(RegistrazioneActivity.this, utente.Email, utente.Password, new UserAuthenticationManager.AuthenticationListener() {
                                    @Override
                                    public void onUserAuthenticationRequestCompleted(boolean isUserAuthenticated) {

                                        //ritorna alla home
                                        Intent homeIntent = new Intent(RegistrazioneActivity.this, HomeActivity.class);
                                        startActivity(homeIntent);

                                        Toast.makeText(RegistrazioneActivity.this, getResources().getString(R.string. reg_succ), Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(RegistrazioneActivity.this, getResources().getString(R.string. email_spec), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

            }
        });


        scatta_foto.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                popmenu();
            }
        });



    }

    @Override
    public void onDateSelected(int year, int month,int day, String tag){
        dateEditText.setText(String.format(Locale.ITALIAN, FORMAT_EDIT_TEXT,day,month ,year));
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
        if(pathImmagine != null && !pathImmagine.isEmpty()){
            Bitmap bitmap = BitmapFactory.decodeFile(pathImmagine);
            foto_profilo.setImageBitmap(bitmap);
        }
    }


    //modificare colori e testo del DialogFragment
    void popmenu() {

        dialog = new Dialog(RegistrazioneActivity.this);

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




    //metodo per log con google account modificare il caso in cui l utente è gia loggato
    //controllando che venga implementato correttamente dopo fase di login
    GoogleSignInAccount account;
    private void signIn() {
        //da migliorare questa parte e controllare che controlli correttamente ,ho sostituito la variabile e l ho messa globale
        if(( account= GoogleSignIn.getLastSignedInAccount(this)) ==null){
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else {
            Toast.makeText(RegistrazioneActivity.this,getResources().getString(R.string. ut_google),
                    Toast.LENGTH_LONG).show();

        }

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
            foto_profilo.setImageBitmap(bitmap);

            text_foto.setText("Foto inserita con Successo");
            dialog.cancel();

        }
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null ){

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
            foto_profilo.setImageBitmap(bitmap);

            dialog.cancel();
        }

        //controllo richesta colllegamento account google
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }


    //Handle request Google sign_in

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            nomeEditText.setText(account.getDisplayName());
            emailEditText.setText(account.getEmail());
            //UtenteDAO.Utente google_account= new UtenteDAO.Utente(account.getDisplayName (),account.getEmail()," "," ");
            //UtenteDAO.addUtente(google_account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(RegistrazioneActivity.this,getResources().getString(R.string.ut_non_esist),
                    Toast.LENGTH_LONG).show();

        }
    }
}




