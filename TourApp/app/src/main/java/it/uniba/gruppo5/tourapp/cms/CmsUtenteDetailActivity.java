package it.uniba.gruppo5.tourapp.cms;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.uniba.gruppo5.tourapp.BaseActivity;
import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;
import it.uniba.gruppo5.tourapp.fragments.DatePickerFragment;
import it.uniba.gruppo5.tourapp.utilities.CustomSpinnerHelper;

public class CmsUtenteDetailActivity extends CmsBaseActivity
        implements DatePickerFragment.OnDateSelectedListener {

    private final static String BUNDLE_IMAGE = "DB_image";
    private final static String BUNDLE_EDIT_MODE = "DB_edit_mode";

    //validatore dei dati
    private AwesomeValidation awesomeValidation;

    Dialog dialog;
    ImageView imageProfilo;

    EditText et_nominativo;
    EditText et_email;
    EditText et_data;
    TextInputEditText passwordEditText;

    String pathImmagine;

    Spinner spinner_tipo_utente;
    CustomSpinnerHelper customSpinnerHelperTipoUtente;

    boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_utente_detail);
        setMenuAndDrawer();

        imageProfilo = findViewById(R.id.imgProfilo);

        et_nominativo = findViewById(R.id.et_nominativo);
        et_email = findViewById(R.id.et_email);
        et_data = findViewById(R.id.et_data);
        passwordEditText = findViewById(R.id.textInput_pass);

        spinner_tipo_utente = findViewById(R.id.spinner_tipo_utente);
        customSpinnerHelperTipoUtente = new CustomSpinnerHelper();

        //imposto validazione
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(et_nominativo, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_email, Patterns.EMAIL_ADDRESS, getResources().getString(R.string.errore_email_valida));
        awesomeValidation.addValidation(passwordEditText, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_data, "^[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}$", getResources().getString(R.string.errore_campo_obbligatorio));

        et_data.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String text = et_data.getText().toString();

                DialogFragment fragment = new DatePickerFragment(text);

                //apertura dialog
                fragment.show(getSupportFragmentManager(), "dateBirthPicker");
            }
        });


        //primo caricamento
        if (savedInstanceState == null) {
            loadDati();
        }

        //imposto spinner tipo utente
        List<Pair<String, String>> valoriSpinnerTipiUtente = new ArrayList<>();
        if (!editMode)
            valoriSpinnerTipiUtente.add(new Pair<>(getString(R.string.seleziona), ""));
        valoriSpinnerTipiUtente.add(new Pair<String, String>(getString(R.string.utente_tipo_admin), UtenteDAO.TIPO_UTENTE_ADMIN));
        valoriSpinnerTipiUtente.add(new Pair<String, String>(getString(R.string.utente_tipo_operatore), UtenteDAO.TIPO_UTENTE_OPERATORE));
        valoriSpinnerTipiUtente.add(new Pair<String, String>(getString(R.string.utente_tipo_visitatore), UtenteDAO.TIPO_UTENTE_VISITATORE));
        customSpinnerHelperTipoUtente.setSpinnerAndValues(this, spinner_tipo_utente, valoriSpinnerTipiUtente);
    }


    private void loadDati() {

        Intent intent = getIntent();
        String idUtente = intent.getStringExtra("ID");

        if (idUtente == null) {
            //modalità nuovo
            editMode = false;
        } else {
            //modalità modifica
            editMode = true;

            UtenteDAO.getSingleUtente(this, new ReadValueListener<UtenteDAO.Utente>() {
                @Override
                public void onDataRead(UtenteDAO.Utente result) {

                    et_data.setText(result.DataNascita);
                    et_email.setText(result.Email);
                    et_nominativo.setText(result.Nominativo);
                    passwordEditText.setText(result.Password);
                    customSpinnerHelperTipoUtente.setSpinnerSelectedValue(result.Tipo);

                    //lettura file
                    result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                        @Override
                        public void onFileRead(String absolutePath) {

                            CmsUtenteDetailActivity.this.pathImmagine = absolutePath;

                            Glide.with(CmsUtenteDetailActivity.this).load(new File(absolutePath)).into(imageProfilo);
                        }
                    });
                }
            }, idUtente);
        }
    }


    @Override
    public void onDateSelected(int year, int month, int day, String tag) {
        et_data.setText(String.format(Locale.ITALIAN, DatePickerFragment.FORMAT_EDIT_TEXT, day, month, year));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio stato corrente
        savedInstanceState.putString(BUNDLE_IMAGE, pathImmagine);
        savedInstanceState.putBoolean(BUNDLE_EDIT_MODE, editMode);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato corrente
        pathImmagine = savedInstanceState.getString(BUNDLE_IMAGE);

        if (pathImmagine != null && !pathImmagine.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(pathImmagine);
            imageProfilo.setImageBitmap(bitmap);
        }

        editMode = savedInstanceState.getBoolean(BUNDLE_EDIT_MODE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cms_utente_detail, menu);

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
                eliminaUtente();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void salvaDati() {

        if (awesomeValidation.validate()
                & customSpinnerHelperTipoUtente.validateRequiredValue(this)) {
            if (editMode) {

                //modifica
                Intent intent = getIntent();
                String idUtente = intent.getStringExtra("ID");

                UtenteDAO.getSingleUtente(this, new ReadValueListener<UtenteDAO.Utente>() {
                    @Override
                    public void onDataRead(UtenteDAO.Utente result) {

                        result.Email = et_email.getText().toString();
                        result.DataNascita = et_data.getText().toString();
                        result.Nominativo = et_nominativo.getText().toString();
                        result.Password = passwordEditText.getText().toString();
                        result.Tipo = customSpinnerHelperTipoUtente.getSpinnerSelectedValue().toString();

                        UtenteDAO.updateUtente(result);

                        Toast.makeText(CmsUtenteDetailActivity.this, getResources().getString(R.string.dati_salv), Toast.LENGTH_LONG).show();
                    }
                }, idUtente);
            } else {

                //creazione
                UtenteDAO.Utente result = new UtenteDAO.Utente();
                result.Email = et_email.getText().toString();
                result.DataNascita = et_data.getText().toString();
                result.Nominativo = et_nominativo.getText().toString();
                result.Password = passwordEditText.getText().toString();
                result.Tipo = customSpinnerHelperTipoUtente.getSpinnerSelectedValue().toString();

                //i nuovi utenti sono di tipo admin
                result.Tipo = UtenteDAO.TIPO_UTENTE_ADMIN;

                UtenteDAO.addUtente(result);

                Intent listIntent = new Intent(this, CmsUtentiActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsUtenteDetailActivity.this, getResources().getString(R.string.ut_insert), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void eliminaUtente() {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CmsUtenteDetailActivity.this, R.style.myDialog));
        builder.setCancelable(true);
        builder.setTitle("Elimina utente");
        builder.setMessage("Sei sicuro di voler eliminare questo utente?");
        builder.setIcon(R.drawable.ic_warning_black_24dp);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                String idUtente = intent.getStringExtra("ID");

                UtenteDAO.removeUtente(idUtente);

                Intent listIntent = new Intent(CmsUtenteDetailActivity.this, CmsUtentiActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsUtenteDetailActivity.this, getResources().getString(R.string.ut_elim), Toast.LENGTH_LONG).show();

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