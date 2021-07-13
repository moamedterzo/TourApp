package it.uniba.gruppo5.tourapp.cms;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;
import it.uniba.gruppo5.tourapp.fragments.FilterDialogFragment;
import it.uniba.gruppo5.tourapp.utilities.CustomSpinnerHelper;

public class CmsUtentiActivity extends CmsBaseActivity
        implements FilterDialogFragment.FilterDialogListener{

    private final static String BUNDLE_TIPO_UTENTE_SEARCH = "tp_ut";
    private final static String BUNDLE_DESCRIZIONE_SEARCH = "desc";


    //Textview dello stato corrente della ricerca
    private TextView mCurrentSearchView;

    //fragment dialog per i filtri
    private FilterDialogFragment mCustomDialogFragment;

    //Ricerca per tipo utente
    Spinner mTipoUtenteSpinner;
    String filterTipoUtente;
    CustomSpinnerHelper customSpinnerHelperTipoUtente;

    EditText etDescrizione;
    String filterDescrizione;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_utenti);
        setMenuAndDrawer();

        //ottenimento riferimenti viste
        mCurrentSearchView = findViewById(R.id.text_current_search);

        //helper tipo utente
        customSpinnerHelperTipoUtente = new CustomSpinnerHelper();

        List<Pair<String, String>> valoriSpinnerTipiUtente = new ArrayList<>();
        valoriSpinnerTipiUtente.add(new Pair<>(getString(R.string.seleziona), ""));
        valoriSpinnerTipiUtente.add(new Pair<String, String>(getString(R.string.utente_tipo_admin),UtenteDAO.TIPO_UTENTE_ADMIN));
        valoriSpinnerTipiUtente.add(new Pair<String, String>(getString(R.string.utente_tipo_operatore),UtenteDAO.TIPO_UTENTE_OPERATORE));
        valoriSpinnerTipiUtente.add(new Pair<String, String>(getString(R.string.utente_tipo_visitatore),UtenteDAO.TIPO_UTENTE_VISITATORE));
        customSpinnerHelperTipoUtente.setOnlyValues(valoriSpinnerTipiUtente);


        //al click della card mostro il dialog fragment
        CardView filter_bar = findViewById(R.id.filter_bar);
        filter_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCustomDialogFragment = new FilterDialogFragment();
                mCustomDialogFragment.show(getSupportFragmentManager(), CmsUtentiActivity.class.getName());
            }
        });

        //immagine annullamento filtri
        ImageView image_clear_filter = findViewById(R.id.image_clear_filter);
        image_clear_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //annullamento filtri
                if(mTipoUtenteSpinner != null)
                    customSpinnerHelperTipoUtente.setSpinnerSelectedValue("");
                if(etDescrizione!= null)
                    etDescrizione.setText("");

                CmsUtentiActivity.this.onFilter();
            }
        });

        //impostazioni recycler view
        recyclerView = findViewById(R.id.utenti_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = findViewById(R.id.fab_nuovo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nuovoUtente = new Intent(CmsUtentiActivity.this,CmsUtenteDetailActivity.class);
                startActivity(nuovoUtente);
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        setTextCurrentSearch();

        this.loadDataList();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio ricerca corrente
        savedInstanceState.putString(BUNDLE_TIPO_UTENTE_SEARCH, filterTipoUtente);
        savedInstanceState.putString(BUNDLE_DESCRIZIONE_SEARCH, filterDescrizione);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato ricerca corrente
        filterTipoUtente = savedInstanceState.getString(BUNDLE_TIPO_UTENTE_SEARCH);
        filterDescrizione = savedInstanceState.getString(BUNDLE_DESCRIZIONE_SEARCH);
    }


    private void setTextCurrentSearch(){

        StringBuilder desc = new StringBuilder();

        if (filterDescrizione != null && !filterDescrizione.isEmpty()) {
            desc.append("<i>");
            desc.append(filterDescrizione);
            desc.append("</i> ");
        }

        if (filterTipoUtente != null && !filterTipoUtente.isEmpty()) {
            desc.append("<b>");
            desc.append(customSpinnerHelperTipoUtente.getTextFromValue(filterTipoUtente));
            desc.append("</b>");
        }

        //Mostro la ricerca nella textview
        mCurrentSearchView.setText(Html.fromHtml(desc.toString()));
    }

    @Override
    public int getIDLayout(){
        //layout custom dei filtri
        return R.layout.dialog_filters_cms_utenti;
    }

    @Override
    public void onCreateViewFilterDialog(View dialogView){

        //ottenimento riferimenti delle view dei filtri
        mTipoUtenteSpinner = dialogView.findViewById(R.id.spinner_tipo_utente);
        etDescrizione = dialogView.findViewById(R.id.et_descrizione);

        //binding dati
        etDescrizione.setText(filterDescrizione);

        //imposto spinner tipo utente
        customSpinnerHelperTipoUtente.setOnlySpinner(this,mTipoUtenteSpinner);
        customSpinnerHelperTipoUtente.setSpinnerSelectedValue(filterTipoUtente);
    }


    @Override
    public void onFilter(){

        //ottenimento filtri
        filterTipoUtente = getFilterTipoUtente();
        filterDescrizione = getFilterDescrizione();

        setTextCurrentSearch();

        //ricaricamento dati
        loadDataList();
    }

    private String getFilterTipoUtente(){

        if(mTipoUtenteSpinner!=null){
            return customSpinnerHelperTipoUtente.getSpinnerSelectedValue().toString();
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



    private void loadDataList() {

        UtenteDAO.getUtenti(filterDescrizione, filterTipoUtente, new ReadValueListener<ArrayList<UtenteDAO.Utente>>() {
            @Override
            public void onDataRead(ArrayList<UtenteDAO.Utente> result) {

                UtenteAdapter utenteAdapter = new UtenteAdapter(result, itemSelectedListener);
                CmsUtentiActivity.this.recyclerView.setAdapter(utenteAdapter);

            }
        });
    }



    UtenteAdapter.OnItemSelectedListener itemSelectedListener = new  UtenteAdapter.OnItemSelectedListener(){

        @Override
        public void onItemSelected(UtenteDAO.Utente utente) {
            // Go to the details page for the selected restaurant
            Intent intent = new Intent(CmsUtentiActivity.this, CmsUtenteDetailActivity.class);
            intent.putExtra("ID",utente.ID);

            startActivity(intent);
        }

    };



    protected final static class UtenteViewHolder extends RecyclerView.ViewHolder{

        private TextView mUtenteNominativo;
        private TextView mUtenteEmail;

        public UtenteViewHolder(View itemView){

            super(itemView);

            mUtenteNominativo = itemView.findViewById(R.id.utente_nominativo);
            mUtenteEmail = itemView.findViewById(R.id.utente_email);
        }

        public void bind(final UtenteDAO.Utente utente,
                         final UtenteAdapter.OnItemSelectedListener listener){

            mUtenteNominativo.setText(utente.Nominativo);
            mUtenteEmail.setText(utente.Email);

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(utente);
                    }
                }
            });
        }
    }

    protected final static class UtenteAdapter extends RecyclerView.Adapter<UtenteViewHolder>{

        private final List<UtenteDAO.Utente> mModel;

        UtenteAdapter(final List<UtenteDAO.Utente> model, OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public UtenteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_cms_utente,viewGroup,false);

            return new UtenteViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(UtenteViewHolder utenteViewHolder, int position){
            utenteViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            void onItemSelected(UtenteDAO.Utente utente);
        }

        private OnItemSelectedListener mListener;
    }
}
