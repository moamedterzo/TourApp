package it.uniba.gruppo5.tourapp.cms;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.AttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.CategoriaAttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.TagAttrazioneDAO;
import it.uniba.gruppo5.tourapp.utilities.CustomSpinnerHelper;
import it.uniba.gruppo5.tourapp.utilities.HorizontalImageAdapter;
import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

public class CmsAttrazioneDetailActivity extends CmsBaseActivity {

    private final static String BUNDLE_IMAGE = "DB_image";
    private final static String BUNDLE_EDIT_MODE   = "DB_edit_mode";
    private final static String BUNDLE_ID_CATEGORIA   = "DB_id_cat";
    private final static String BUNDLE_LISTA_TAG   = "DB_tags";
    private final static String BUNDLE_LISTA_IMMAGINI   = "DB_images";

    //validatore dei dati
    private AwesomeValidation awesomeValidation;

    Dialog dialog;

    EditText et_titolo;
    EditText et_titoloItaliano;
    EditText editTextSitoWeb;
    EditText editTextEmail;
    EditText editTextTelefono;
    EditText editTextDescrizione;
    EditText editTextDescrizioneItaliano;
    EditText editTextOrario;
    EditText editTextLng;
    EditText editTextLat;

    String idCategoria;
    Spinner spinner_categoria;
    CustomSpinnerHelper customSpinnerHelperCategoria;

    Spinner spinner_tag;
    CustomSpinnerHelper customSpinnerHelperTag;

    Spinner spinner_costo;
    CustomSpinnerHelper customSpinnerHelperCosto;

    boolean editMode;

    CmsAttrazioneDetailActivity.TagAdapter tagAdapter;
    RecyclerView recyclerViewTags;

    HorizontalImageAdapter imagesAdapter;
    RecyclerView recyclerViewImmagini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_attrazione_detail);
        setMenuAndDrawer();

        et_titolo = findViewById(R.id.titolo);
        et_titoloItaliano = findViewById(R.id.titoloItaliano);
        editTextDescrizione = findViewById(R.id.editTextDescrizione);
        editTextDescrizioneItaliano = findViewById(R.id.editTextDescrizioneItaliano);
        editTextOrario = findViewById(R.id.editTextOrario);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextLng= findViewById(R.id.editTextLng);
        editTextLat = findViewById(R.id.editTextLat);
        editTextTelefono = findViewById(R.id.editTextTelefono);
        editTextSitoWeb = findViewById(R.id.editTextSitoWeb);

        spinner_categoria = findViewById(R.id.spinner_categoria);
        customSpinnerHelperCategoria = new CustomSpinnerHelper();

        spinner_tag = findViewById(R.id.spinner_tags);
        customSpinnerHelperTag = new CustomSpinnerHelper();

        spinner_costo = findViewById(R.id.spinner_costo);
        customSpinnerHelperCosto = new CustomSpinnerHelper();

        //rec view tags
        recyclerViewTags = findViewById(R.id.cat_attrazione_recycler_view);

        LinearLayoutManager  layoutManagerTags = new LinearLayoutManager(getBaseContext());
        layoutManagerTags.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManagerTags.scrollToPosition(0);
        recyclerViewTags.setLayoutManager(layoutManagerTags);

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

                customSpinnerHelperCategoria.setSpinnerAndValues(CmsAttrazioneDetailActivity.this,spinner_categoria, valoriSpinner);
                customSpinnerHelperCategoria.setSpinnerSelectedValue(idCategoria);
            }
        });

        spinner_categoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                idCategoria = customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();

                if (idCategoria != null && !idCategoria.isEmpty()) {

                    spinner_tag.setEnabled(true);
                    loadTagDisponibili();
                }
                else {
                    spinner_tag.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });


        loadTagDisponibili();

        spinner_tag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String idTag = customSpinnerHelperTag.getSpinnerSelectedValue().toString();

                if (idTag != null && !idTag.isEmpty()) {
                    String text = customSpinnerHelperTag.getSpinnerSelectedText().toString();

                    TagAttrazioneDAO.Tag tag = new TagAttrazioneDAO.Tag();
                    tag.ID = idTag;
                    tag.Titolo = text;

                    tagAdapter.addTag(tag);

                    //resetto spinner
                    customSpinnerHelperTag.setSpinnerSelectedValue("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        //immagini
        recyclerViewImmagini = findViewById(R.id.recview_Img);
        recyclerViewImmagini.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewImmagini.setLayoutManager(horizontalLayoutManager);

        //imposto validazione
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(et_titolo, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(et_titoloItaliano, "^.+$", getResources().getString(R.string.errore_campo_obbligatorio));
        awesomeValidation.addValidation(editTextTelefono, "^[+0-9\\(\\)]*$", getResources().getString(R.string.errore_campo_obbligatorio));


        //primo caricamento
        if(savedInstanceState==null){
            loadDati();
        }


        //imposto spinner sconto
        List<Pair<String, String>> valoriSpinnerCosto = new ArrayList<>();
        if(!editMode)
            valoriSpinnerCosto.add(new Pair<>(getString(R.string.seleziona), ""));
        valoriSpinnerCosto.add(new Pair<String, String>(getString(R.string.costo_economico),AttrazioneDAO.PREZZO_ECONOMICO));
        valoriSpinnerCosto.add(new Pair<String, String>(getString(R.string.costo_normale),AttrazioneDAO.PREZZO_NELLA_MEDIA));
        valoriSpinnerCosto.add(new Pair<String, String>(getString(R.string.costo_costoso),AttrazioneDAO.PREZZO_COSTOSO));
        customSpinnerHelperCosto.setSpinnerAndValues(this, spinner_costo, valoriSpinnerCosto);
    }

    private void loadTagDisponibili(){

        TagAttrazioneDAO.getTag(null,idCategoria, new ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>>() {
            @Override
            public void onDataRead(ArrayList<TagAttrazioneDAO.Tag> result) {

                List<Pair<String, String>> valoriSpinner = new ArrayList<>();

                valoriSpinner.add(new Pair<>(getString(R.string.seleziona), ""));

                for(TagAttrazioneDAO.Tag categoria : result)
                {
                    valoriSpinner.add(new Pair<>(categoria.Titolo ,categoria.ID));
                }

                customSpinnerHelperTag.setSpinnerAndValues(CmsAttrazioneDetailActivity.this,spinner_tag, valoriSpinner);
            }
        });
    }


    private void loadDati(){

        Intent intent = getIntent();
        String idAttrazioni = intent.getStringExtra("ID");

        if(idAttrazioni == null){
            //modalità nuovo
            editMode = false;

            //istanzio adapter tag
            setAdapterTags(new ArrayList<TagAttrazioneDAO.Tag>());
            setAdapterImages(new ArrayList<String>());
        }
        else {
            //modalità modifica
            editMode = true;

            AttrazioneDAO.getSingleAttrazione(this, new ReadValueListener<AttrazioneDAO.Attrazione>() {
                @Override
                public void onDataRead(AttrazioneDAO.Attrazione result) {

                    et_titolo.setText(result.Titolo);
                    et_titoloItaliano.setText(result.TitoloItaliano);
                    editTextDescrizione.setText(result.Descrizione);
                    editTextDescrizioneItaliano.setText(result.DescrizioneItaliano);
                    editTextOrario.setText(result.Orario);
                    editTextEmail.setText(result.Email);
                    editTextTelefono.setText(result.Telefono);
                    editTextSitoWeb.setText(result.Sitoweb);

                    if(result.Latitudine!=0)
                        editTextLat.setText(String.valueOf(result.Latitudine));

                    if(result.Longitudine != 0)
                        editTextLng.setText(String.valueOf(result.Longitudine));

                    customSpinnerHelperCategoria.setSpinnerSelectedValue(result.IDCategoria);
                    customSpinnerHelperCosto.setSpinnerSelectedValue(result.Prezzo);

                    //tags
                    loadTagList(result);
                    setAdapterImages(result.Immagini);
                }
            }, idAttrazioni);
        }
    }

    private void loadTagList(AttrazioneDAO.Attrazione attrazione) {

        AttrazioneDAO.readTagAttrazioni(attrazione, new ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>>() {
            @Override
            public void onDataRead(ArrayList<TagAttrazioneDAO.Tag> result) {

                setAdapterTags(result);
            }
        });
    }
    private void setAdapterTags(ArrayList<TagAttrazioneDAO.Tag> tags){
        tagAdapter = new CmsAttrazioneDetailActivity.TagAdapter(tags, tagSelectedListener);
        CmsAttrazioneDetailActivity.this.recyclerViewTags.setAdapter(tagAdapter);
    }
    CmsAttrazioneDetailActivity.TagAdapter.OnItemSelectedListener tagSelectedListener = new  CmsAttrazioneDetailActivity.TagAdapter.OnItemSelectedListener(){

        @Override
        public void onItemSelected(TagAttrazioneDAO.Tag tag) {
            tagAdapter.removeTag(tag);
        }
    };

    private void setAdapterImages(ArrayList<String> images){

        imagesAdapter = new HorizontalImageAdapter(images, getApplicationContext(),  imageSelectedListener);
        recyclerViewImmagini.setAdapter(imagesAdapter);
    }

    HorizontalImageAdapter.OnLongClickItemListener imageSelectedListener = new  HorizontalImageAdapter.OnLongClickItemListener(){

        @Override
        public void onItemLongClick(final String tag) {

            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CmsAttrazioneDetailActivity.this,R.style.myDialog));
            builder.setCancelable(true);
            builder.setTitle("Elimina immagine");
            builder.setMessage("Sei sicuro di voler eliminare questa immagine?");
            builder.setIcon(R.drawable.ic_warning_black_24dp);

            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    imagesAdapter.removeImmagine(tag);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.create().show();
        }
    };


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio immagine
        savedInstanceState.putBoolean(BUNDLE_EDIT_MODE, editMode);
        savedInstanceState.putString(BUNDLE_ID_CATEGORIA, idCategoria);
        savedInstanceState.putParcelableArrayList(BUNDLE_LISTA_TAG, tagAdapter.mModel);
        savedInstanceState.putStringArrayList(BUNDLE_LISTA_IMMAGINI, imagesAdapter.mModel);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        editMode = savedInstanceState.getBoolean(BUNDLE_EDIT_MODE);
        idCategoria = savedInstanceState.getString(BUNDLE_ID_CATEGORIA);

        ArrayList<TagAttrazioneDAO.Tag> tags = savedInstanceState.getParcelableArrayList(BUNDLE_LISTA_TAG);
        if(tags == null)
            tags = new ArrayList<>();
        setAdapterTags(tags);

        ArrayList<String> immagini = savedInstanceState.getStringArrayList(BUNDLE_LISTA_IMMAGINI);
        if(immagini == null)
            immagini = new ArrayList<>();
        setAdapterImages(immagini);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cms_attrazione_detail, menu);

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
                eliminaAttrazioni();
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

        AppCompatImageView mCamerabtn = dialog.findViewById(R.id.cameradialogbtn);

        AppCompatImageView mGallerybtn = dialog.findViewById(R.id.gallerydialogbtn);

        AppCompatTextView btnCancel = dialog.findViewById(R.id.canceldialogbtn);


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

        String filename = UUID.randomUUID().toString();
        String fileLocalPath = getFilesDir() + "/" + filename;

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ImageSaver.saveToPath( imageBitmap, fileLocalPath);

            //salvataggio memoria
            imagesAdapter.addImmagine(filename);

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

            ImageSaver.saveToPath(imageBitmap, fileLocalPath);

            //salvataggio memoria
            imagesAdapter.addImmagine(filename);

            dialog.cancel();
        }
    }



    private void salvaDati(){


        if (awesomeValidation.validate()
                & customSpinnerHelperCategoria.validateRequiredValue(this)) {

            if (editMode) {

                //modifica
                Intent intent = getIntent();
                String idAttrazioni = intent.getStringExtra("ID");

                AttrazioneDAO.getSingleAttrazione(this, new ReadValueListener<AttrazioneDAO.Attrazione>() {
                    @Override
                    public void onDataRead(AttrazioneDAO.Attrazione result) {

                        result.Titolo = et_titolo.getText().toString();
                        result.TitoloItaliano= et_titoloItaliano.getText().toString();
                        result.Descrizione = editTextDescrizione.getText().toString();
                        result.DescrizioneItaliano = editTextDescrizioneItaliano.getText().toString();
                        result.Email = editTextEmail.getText().toString();
                        result.Telefono= editTextTelefono.getText().toString();
                        result.Sitoweb = editTextSitoWeb.getText().toString();
                        result.Orario = editTextOrario.getText().toString();

                        if(!editTextLng.getText().toString().isEmpty())
                            result.Longitudine = Float.parseFloat(editTextLng.getText().toString());
                        if(!editTextLat.getText().toString().isEmpty())
                            result.Latitudine = Float.parseFloat(editTextLat.getText().toString());

                        result.Prezzo = customSpinnerHelperCosto.getSpinnerSelectedValue().toString();
                        result.IDCategoria = customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();

                        result.Tags = tagAdapter.getIDsTags();
                        result.Immagini = imagesAdapter.mModel;

                        AttrazioneDAO.updateAttrazione(result, CmsAttrazioneDetailActivity.this);

                        Toast.makeText(CmsAttrazioneDetailActivity.this, "Dati salvati con successo", Toast.LENGTH_LONG).show();
                    }
                }, idAttrazioni);
            } else {

                //creazione
                AttrazioneDAO.Attrazione result = new AttrazioneDAO.Attrazione();
                result.Titolo = et_titolo.getText().toString();
                result.TitoloItaliano= et_titoloItaliano.getText().toString();
                result.Descrizione = editTextDescrizione.getText().toString();
                result.DescrizioneItaliano = editTextDescrizioneItaliano.getText().toString();
                result.Email = editTextEmail.getText().toString();
                result.Telefono= editTextTelefono.getText().toString();
                result.Sitoweb = editTextSitoWeb.getText().toString();
                result.Orario = editTextOrario.getText().toString();

                if(!editTextLng.getText().toString().isEmpty())
                    result.Longitudine = Float.parseFloat(editTextLng.getText().toString());
                if(!editTextLat.getText().toString().isEmpty())
                    result.Latitudine = Float.parseFloat(editTextLat.getText().toString());

                result.Prezzo = customSpinnerHelperCosto.getSpinnerSelectedValue().toString();
                result.IDCategoria = customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();

                result.Tags = tagAdapter.getIDsTags();
                result.Immagini = imagesAdapter.mModel;

                AttrazioneDAO.addAttrazione(result, this);

                Intent listIntent = new Intent(this, CmsAttrazioniActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsAttrazioneDetailActivity.this, "Attrazione inserita con successo", Toast.LENGTH_LONG).show();
            }
        }
    }




    private void eliminaAttrazioni(){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CmsAttrazioneDetailActivity.this,R.style.myDialog));
        builder.setCancelable(true);
        builder.setTitle("Elimina immagine");
        builder.setMessage("Sei sicuro di voler eliminare questa immagine?");

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                String idAttrazioni = intent.getStringExtra("ID");

                AttrazioneDAO.removeAttrazione(idAttrazioni);

                Intent listIntent = new Intent(CmsAttrazioneDetailActivity.this, CmsAttrazioniActivity.class);
                startActivity(listIntent);
                finish();

                Toast.makeText(CmsAttrazioneDetailActivity.this, "Attrazione eliminata con successo", Toast.LENGTH_LONG).show();

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
           }



    protected final static class TagViewHolder extends RecyclerView.ViewHolder{

        private TextView mTagTitolo;
        private ImageView image_clear;

        public TagViewHolder(View itemView){

            super(itemView);

            mTagTitolo = itemView.findViewById(R.id.titolo);
            image_clear = itemView.findViewById(R.id.image_clear);
        }

        public void bind(final TagAttrazioneDAO.Tag tag,
                         final CmsAttrazioneDetailActivity.TagAdapter.OnItemSelectedListener listener){

            mTagTitolo.setText(tag.Titolo);

            // Click listener
            image_clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(tag);
                    }
                }
            });
        }
    }

    protected final static class TagAdapter extends RecyclerView.Adapter<CmsAttrazioneDetailActivity.TagViewHolder>{

        private final ArrayList<TagAttrazioneDAO.Tag> mModel;

        TagAdapter(final ArrayList<TagAttrazioneDAO.Tag> model, CmsAttrazioneDetailActivity.TagAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public CmsAttrazioneDetailActivity.TagViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_cms_attrazione_tag_list,viewGroup,false);

            return new CmsAttrazioneDetailActivity.TagViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(CmsAttrazioneDetailActivity.TagViewHolder tagViewHolder, int position){
            tagViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }


        public ArrayList<String> getIDsTags(){
            ArrayList<String> result = new ArrayList<>();
            for(TagAttrazioneDAO.Tag tag : mModel){
                result.add(tag.ID);
            }
            return result;
        }

        void addTag(TagAttrazioneDAO.Tag newTag) {

            //controllo se sia stato già inserito il tag
            boolean checkExists = false;
            for (TagAttrazioneDAO.Tag tag : mModel) {
                if(tag.ID.equals(newTag.ID)){
                    checkExists=true;
                    break;
                }
            }

            if (!checkExists) {
                mModel.add(newTag);
                notifyItemInserted(mModel.size() - 1);
            }
        }

        void removeTag(TagAttrazioneDAO.Tag tag){

            int position = mModel.indexOf(tag);
            mModel.remove(tag);
            notifyItemRemoved(position);
        }


        public interface OnItemSelectedListener {

            void onItemSelected(TagAttrazioneDAO.Tag tag);
        }

        private CmsAttrazioneDetailActivity.TagAdapter.OnItemSelectedListener mListener;
    }
}
