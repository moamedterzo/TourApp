package it.uniba.gruppo5.tourapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.firebase.AttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.CategoriaAttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.TagAttrazioneDAO;
import it.uniba.gruppo5.tourapp.fragments.FilterDialogFragment;
import it.uniba.gruppo5.tourapp.utilities.CustomSpinnerHelper;

public class AttrazioniActivity extends BaseActivity
        implements FilterDialogFragment.FilterDialogListener{


    public final static String INTENT_ID_CATEGORIA = "id_cat";
    public final static String INTENT_DESCRIZIONE = "desc";


    private final static String BUNDLE_DESCRIZIONE_SEARCH = "desc";
    private final static String BUNDLE_FILTER_TAG = "bd_ft_tag";
    private final static String BUNDLE_FILTER_COSTO = "bd_ft_cos";

    
    //Textview dello stato corrente della ricerca
    private TextView mCurrentSearchView;

    //fragment dialog per i filtri
    private FilterDialogFragment mCustomDialogFragment;

    EditText etDescrizione;
    String filterDescrizione;
    String filterTag;

    RecyclerView recyclerView;
    RecyclerView recyclerViewTag;

    //Ricerca per categoria
    Spinner mCategoriaSpinner;
    String filterCategoria = null;
    CustomSpinnerHelper customSpinnerHelperCategoria;

    //Ricerca per economicità
    Spinner mCostoSpinner;
    String filterCosto;
    CustomSpinnerHelper customSpinnerHelperCosto;
    static int color;
    static int color2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_lista_attrazioni);
        setMenuAndDrawer();
        color = R.color.colorBackgraound;
        color2 = R.color.colorGiall;

        //ottenimento riferimenti viste
        mCurrentSearchView = findViewById(R.id.text_current_search);

        //al click della card mostro il dialog fragment
        CardView filter_bar = findViewById(R.id.filter_bar);
        filter_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCustomDialogFragment = new FilterDialogFragment();
                mCustomDialogFragment.show(getSupportFragmentManager(), AttrazioniActivity.class.getName());
            }
        });

        //immagine annullamento filtri
        ImageView image_clear_filter = findViewById(R.id.image_clear_filter);
        image_clear_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //annullamento filtri
                if(mCategoriaSpinner != null)
                    customSpinnerHelperCategoria.setSpinnerSelectedValue("");
                if(etDescrizione!= null)
                    etDescrizione.setText("");
                if(mCostoSpinner != null)
                    customSpinnerHelperCosto.setSpinnerSelectedValue("");

                AttrazioniActivity.this.onFilter();
            }
        });

        if(savedInstanceState == null) {
            Intent intent = getIntent();
            filterCategoria = intent.getStringExtra(INTENT_ID_CATEGORIA);

            filterDescrizione = intent.getStringExtra(INTENT_DESCRIZIONE);
        }


        //impostazioni recycler view
        recyclerView = findViewById(R.id.attrazioni_recycler_view);
        recyclerViewTag = findViewById(R.id.recview_tags);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getBaseContext());
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager2.scrollToPosition(0);
        recyclerViewTag.setLayoutManager(layoutManager2);

        //caricamento categorie
        customSpinnerHelperCategoria = new CustomSpinnerHelper();

        CategoriaAttrazioneDAO.getCategorie(null, new ReadValueListener<ArrayList<CategoriaAttrazioneDAO.Categoria>>() {
            @Override
            public void onDataRead(ArrayList<CategoriaAttrazioneDAO.Categoria> result) {
                List<Pair<String, String>> valoriSpinner = new ArrayList<>();
                valoriSpinner.add(new Pair<>(getString(R.string.seleziona), ""));

                for(CategoriaAttrazioneDAO.Categoria categoria : result)
                {
                    valoriSpinner.add(new Pair<>(categoria.getTitoloByLingua(), categoria.ID));
                }

                customSpinnerHelperCategoria.setOnlyValues(valoriSpinner);

                //effettuato per sicurezza
                setTextCurrentSearch();
            }
        });

        //helper costo
        customSpinnerHelperCosto= new CustomSpinnerHelper();

        List<Pair<String, String>> valoriSpinnerCosto = new ArrayList<>();
        valoriSpinnerCosto.add(new Pair<>(getString(R.string.seleziona), ""));
        valoriSpinnerCosto.add(new Pair<String, String>(getString(R.string.costo_economico),AttrazioneDAO.PREZZO_ECONOMICO));
        valoriSpinnerCosto.add(new Pair<String, String>(getString(R.string.costo_normale),AttrazioneDAO.PREZZO_NELLA_MEDIA));
        valoriSpinnerCosto.add(new Pair<String, String>(getString(R.string.costo_costoso),AttrazioneDAO.PREZZO_COSTOSO));
        customSpinnerHelperCosto.setOnlyValues(valoriSpinnerCosto);

    }


    private void loadDataList() {

  /*      AttrazioneDAO.getAttrazioniPubbliche(this, filterDescrizione, filterCategoria, filterTag, filterCosto, new ReadValueListener<ArrayList<AttrazioneDAO.Attrazione>>() {
            @Override
            public void onDataRead(ArrayList<AttrazioneDAO.Attrazione> result) {

                AttrazioniActivity.AttrazioneAdapter attrazioneAdapter = new AttrazioniActivity.AttrazioneAdapter(result, itemSelectedListener);
                AttrazioniActivity.this.recyclerView.setAdapter(attrazioneAdapter);
            }
        });*/

        final AttrazioniActivity.AttrazioneAdapter attrazioneAdapter = new AttrazioniActivity.AttrazioneAdapter(new ArrayList<AttrazioneDAO.Attrazione>(), itemSelectedListener);
        AttrazioniActivity.this.recyclerView.setAdapter(attrazioneAdapter);

        AttrazioneDAO.getAttrazioniPubbliche(this, filterDescrizione, filterCategoria, filterTag, filterCosto, new ReadValueListener<AttrazioneDAO.Attrazione>() {
            @Override
            public void onDataRead(AttrazioneDAO.Attrazione result) {

                if(!attrazioneAdapter.mModel.contains(result)) {
                    attrazioneAdapter.mModel.add(result);
                    attrazioneAdapter.notifyItemInserted(attrazioneAdapter.mModel.size() - 1);
                }
            }
        });
    }



    private void loadTagList() {

        TagAttrazioneDAO.getTag(null, filterCategoria, new ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>>() {
            @Override
            public void onDataRead(ArrayList<TagAttrazioneDAO.Tag> result) {

                TagAdapter tagAdapter = new TagAdapter(result, itemSelectedListenerTag);
                AttrazioniActivity.this.recyclerViewTag.setAdapter(tagAdapter);
            }
        });
    }



    @Override
    protected void onResume(){
        super.onResume();
        setTextCurrentSearch();

        this.loadDataList();
        this.loadTagList();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio ricerca corrente
        savedInstanceState.putString(BUNDLE_DESCRIZIONE_SEARCH, filterDescrizione);
        savedInstanceState.putString(BUNDLE_FILTER_TAG, filterTag);
        savedInstanceState.putString(BUNDLE_FILTER_COSTO, filterCosto);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato ricerca corrente
        filterDescrizione = savedInstanceState.getString(BUNDLE_DESCRIZIONE_SEARCH);
        filterTag = savedInstanceState.getString(BUNDLE_FILTER_TAG);
        filterCosto = savedInstanceState.getString(BUNDLE_FILTER_COSTO);
    }


    private void setTextCurrentSearch(){

        StringBuilder desc = new StringBuilder();

        if (filterDescrizione != null && !filterDescrizione.isEmpty()) {
            desc.append("<i>");
            desc.append(filterDescrizione);
            desc.append("</i> ");
        }


        if (filterCategoria != null && !filterCategoria.isEmpty()) {
            desc.append("<b>");
            desc.append(customSpinnerHelperCategoria.getTextFromValue(filterCategoria));
            desc.append("</b> ");
        }

        if (filterCosto != null && !filterCosto.isEmpty()) {
            desc.append("<i>");
            desc.append(customSpinnerHelperCosto.getTextFromValue(filterCosto));
            desc.append("</i>");
        }


        //Mostro la ricerca nella textview
        mCurrentSearchView.setText(Html.fromHtml(desc.toString()));
    }

    @Override
    public int getIDLayout(){
        //layout custom dei filtri
        return R.layout.dialog_filters_attrazioni;
    }

    @Override
    public void onCreateViewFilterDialog(View dialogView){

        //ottenimento riferimenti delle view dei filtri
        mCategoriaSpinner = dialogView.findViewById(R.id.spinner_categorie_attrz);
        mCostoSpinner = dialogView.findViewById(R.id.spinner_cat_costo);
        etDescrizione = dialogView.findViewById(R.id.et_descrizione);

        //imposto spinner tipo utente
        customSpinnerHelperCategoria.setOnlySpinner(this,mCategoriaSpinner);
        customSpinnerHelperCategoria.setSpinnerSelectedValue(filterCategoria);

        customSpinnerHelperCosto.setOnlySpinner(this,mCostoSpinner);
        customSpinnerHelperCosto.setSpinnerSelectedValue(filterCosto);

        //binding dati
        etDescrizione.setText(filterDescrizione);
    }

    @Override
    public void onFilter(){

        //ottenimento filtri
        filterDescrizione = getFilterDescrizione();
        filterCosto = getFilterCosto();

        String newFilterCategoria = getFilterCategoria();

        if(((newFilterCategoria == null||newFilterCategoria.isEmpty()) && filterCategoria != null) || (newFilterCategoria != null && !newFilterCategoria.equals(filterCategoria))){
            //ricarico i tag se la categoria è cambiata

            filterCategoria = newFilterCategoria;
            this.loadTagList();
        }
        else {
            filterCategoria = newFilterCategoria;
        }

        setTextCurrentSearch();

        //ricaricamento dati
        loadDataList();
    }

    private String getFilterCosto(){

        if(mCostoSpinner!=null){
            return customSpinnerHelperCosto.getSpinnerSelectedValue().toString();
        }
        else{
            return null;
        }
    }

    private String getFilterDescrizione() {

        if(etDescrizione!=null)
            return etDescrizione.getText().toString();
        else
            return null;
    }

    private String getFilterCategoria(){

        if(mCategoriaSpinner!=null){
            return customSpinnerHelperCategoria.getSpinnerSelectedValue().toString();
        }
        else{
            return null;
        }
    }


    AttrazioniActivity.TagAdapter.OnItemSelectedListener itemSelectedListenerTag = new  AttrazioniActivity.TagAdapter.OnItemSelectedListener(){

        @Override
        public boolean onItemSelected(TagAttrazioneDAO.Tag tag) {
            boolean isItemSelected = true;

            if(tag.ID.equals(AttrazioniActivity.this.filterTag)){
                //vuol dire che è stato cliccato nuovamente lo stesso elemento, allora deseleziono
                filterTag = null;
                isItemSelected = false;
            }
            else {
                if(filterTag != null && !filterTag.isEmpty()){
                    //sto selezionando un nuovo elemento, effettuo nuovamente il binding degli elementi per deselezionare lo stato
                    AttrazioniActivity.this.loadTagList();
                }

                filterTag = tag.ID;
            }

            AttrazioniActivity.this.loadDataList();

            return isItemSelected;
        }

    };



    AttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener itemSelectedListener = new  AttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AttrazioneDAO.Attrazione attrazione) {
           Intent intent = new Intent(AttrazioniActivity.this, DettaglioAttrazioneActivity.class);
           intent.putExtra("ID", attrazione.ID);
           startActivity(intent);
        }

    };

    protected final static class AttrazioneViewHolder extends RecyclerView.ViewHolder{

        private TextView mAttrazioneTitolo;
        private ImageView imgDettagliAttr;
        private  TextView txtClickDettagli;

        public AttrazioneViewHolder(View itemView){

            super(itemView);

            mAttrazioneTitolo = itemView.findViewById(R.id.txtDettagliAttrazione);
            imgDettagliAttr = itemView.findViewById(R.id.imgDettagliAttr);
            txtClickDettagli = itemView.findViewById(R.id.txtClickDettagli);
        }

        public void bind(final AttrazioneDAO.Attrazione attrazione,
                         final AttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener listener){

            mAttrazioneTitolo.setText(attrazione.getTitoloByLingua());

            String descrizione = attrazione.getDescrizioneByLingua();
            if(descrizione!=null){

                if(descrizione.length() > 100){
                    txtClickDettagli.setText(descrizione.substring(0, 100) + "...");
                }else {
                    txtClickDettagli.setText(descrizione);
                }
            }

            if(attrazione.Immagini != null && attrazione.Immagini.size() > 0){

               FileStorageDAO fileStorageDAO = new FileStorageDAO(itemView.getContext(), attrazione.Immagini.get(0));
               fileStorageDAO.readFile(new ReadFileStorageListener() {
                   @Override
                   public void onFileRead(String absolutePath) {

                        Glide.with(itemView).load(new File(absolutePath)).into(imgDettagliAttr);
                   }
               });
            }

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(attrazione);
                    }
                }
            });
        }
    }




    protected final static class AttrazioneAdapter extends RecyclerView.Adapter<AttrazioniActivity.AttrazioneViewHolder>{

        private final List<AttrazioneDAO.Attrazione> mModel;

        AttrazioneAdapter(final List<AttrazioneDAO.Attrazione> model, AttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public AttrazioniActivity.AttrazioneViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_attrazioni,viewGroup,false);

            return new AttrazioniActivity.AttrazioneViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(AttrazioniActivity.AttrazioneViewHolder attrazioneViewHolder, int position){
            attrazioneViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            void onItemSelected(AttrazioneDAO.Attrazione attrazione);
        }

        private AttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener mListener;
    }


    protected final static class TagViewHolder extends RecyclerView.ViewHolder{

        private TextView mTagTitolo;
        private ImageView mImage;

        public TagViewHolder(View itemView){

            super(itemView);

            mTagTitolo = itemView.findViewById(R.id.label);
            mImage = itemView.findViewById(R.id.image);
        }

        public void bind(final TagAttrazioneDAO.Tag tag,
                         final AttrazioniActivity.TagAdapter.OnItemSelectedListener listener){

            mTagTitolo.setText(tag.getTitoloByLingua());

            tag.immaginePrincipale = new FileStorageDAO(this.itemView.getContext(), tag.FilePathImmagine);
            tag.immaginePrincipale.readFile(new ReadFileStorageListener() {
                @Override
                public void onFileRead(String absolutePath) {

                    Glide.with(itemView).load(new File(absolutePath)).into(mImage);
                }
            });

            //controllo se l'item corrente sia stato selezionato dall'utente
            AttrazioniActivity attrazioniActivity = (AttrazioniActivity)itemView.getContext();
            if(tag.ID.equals(attrazioniActivity.filterTag)){
                //selected
                itemView.setBackground(itemView.getContext().getDrawable(R.drawable.shape_selected));
                mTagTitolo.setTextColor(color);
            }

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                        if(listener.onItemSelected(tag)){
                            itemView.setBackground(itemView.getContext().getDrawable(R.drawable.shape_selected));
                            mTagTitolo.setTextColor(color);
                        }
                        else{
                            itemView.setBackground(itemView.getContext().getDrawable(R.color.colorBackgraound));
                            mTagTitolo.setTextColor(color2);
                        }
                    }
                }
            });
        }
    }

    protected final static class TagAdapter extends RecyclerView.Adapter<AttrazioniActivity.TagViewHolder>{

        private final List<TagAttrazioneDAO.Tag> mModel;

        TagAdapter(final List<TagAttrazioneDAO.Tag> model, AttrazioniActivity.TagAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public AttrazioniActivity.TagViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_horizontal,viewGroup,false);

            return new AttrazioniActivity.TagViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(AttrazioniActivity.TagViewHolder tagViewHolder, int position){
            tagViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            boolean onItemSelected(TagAttrazioneDAO.Tag tag);
        }

        private AttrazioniActivity.TagAdapter.OnItemSelectedListener mListener;
    }

}
