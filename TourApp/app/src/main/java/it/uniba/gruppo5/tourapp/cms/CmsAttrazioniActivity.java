package it.uniba.gruppo5.tourapp.cms;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.AttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.fragments.FilterDialogFragment;

public class CmsAttrazioniActivity extends CmsBaseActivity
        implements FilterDialogFragment.FilterDialogListener {

    private final static String BUNDLE_DESCRIZIONE_SEARCH = "desc";

    //Textview dello stato corrente della ricerca
    private TextView mCurrentSearchView;

    //fragment dialog per i filtri
    private FilterDialogFragment mCustomDialogFragment;

    EditText etDescrizione;
    String filterDescrizione;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_attrazioni);
        setMenuAndDrawer();

        //ottenimento riferimenti viste
        mCurrentSearchView = findViewById(R.id.text_current_search);

        //al click della card mostro il dialog fragment
        CardView filter_bar = findViewById(R.id.filter_bar);
        filter_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCustomDialogFragment = new FilterDialogFragment();
                mCustomDialogFragment.show(getSupportFragmentManager(), CmsAttrazioniActivity.class.getName());
            }
        });

        //immagine annullamento filtri
        ImageView image_clear_filter = findViewById(R.id.image_clear_filter);
        image_clear_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //annullamento filtri
                if(etDescrizione!= null)
                    etDescrizione.setText("");

                CmsAttrazioniActivity.this.onFilter();
            }
        });

        //impostazioni recycler view
        recyclerView = findViewById(R.id.attrazioni_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = findViewById(R.id.fab_nuovo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nuovoAttrazione = new Intent(CmsAttrazioniActivity.this,CmsAttrazioneDetailActivity.class);
                startActivity(nuovoAttrazione);
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
        savedInstanceState.putString(BUNDLE_DESCRIZIONE_SEARCH, filterDescrizione);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato ricerca corrente
        filterDescrizione = savedInstanceState.getString(BUNDLE_DESCRIZIONE_SEARCH);
    }


    private void setTextCurrentSearch(){

        StringBuilder desc = new StringBuilder();

        if (filterDescrizione != null && !filterDescrizione.isEmpty()) {
            desc.append("<i>");
            desc.append(filterDescrizione);
            desc.append("</i> ");
        }

        //Mostro la ricerca nella textview
        mCurrentSearchView.setText(Html.fromHtml(desc.toString()));
    }

    @Override
    public int getIDLayout(){
        //layout custom dei filtri
        return R.layout.dialog_filters_cms_attrazioni;
    }

    @Override
    public void onCreateViewFilterDialog(View dialogView){

        //ottenimento riferimenti delle view dei filtri
        etDescrizione = dialogView.findViewById(R.id.et_descrizione);

        //binding dati
        etDescrizione.setText(filterDescrizione);
    }

    @Override
    public void onFilter(){

        //ottenimento filtri
        filterDescrizione = getFilterDescrizione();

        setTextCurrentSearch();

        //ricaricamento dati
        loadDataList();
    }

    private String getFilterDescrizione() {

        if(etDescrizione!=null)
            return etDescrizione.getText().toString();
        else
            return null;
    }



    private void loadDataList() {

        AttrazioneDAO.getAttrazioni(filterDescrizione, null, new ReadValueListener<ArrayList<AttrazioneDAO.Attrazione>>() {
            @Override
            public void onDataRead(ArrayList<AttrazioneDAO.Attrazione> result) {

                AttrazioneAdapter attrazioneAdapter = new AttrazioneAdapter(result, itemSelectedListener);
                CmsAttrazioniActivity.this.recyclerView.setAdapter(attrazioneAdapter);
            }
        });
    }



    AttrazioneAdapter.OnItemSelectedListener itemSelectedListener = new  AttrazioneAdapter.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AttrazioneDAO.Attrazione attrazione) {
            // Go to the details page for the selected restaurant
            Intent intent = new Intent(CmsAttrazioniActivity.this, CmsAttrazioneDetailActivity.class);
            intent.putExtra("ID",attrazione.ID);

            startActivity(intent);
        }

    };



    protected final static class AttrazioneViewHolder extends RecyclerView.ViewHolder{

        private TextView mAttrazioneTitolo;

        public AttrazioneViewHolder(View itemView){

            super(itemView);

            mAttrazioneTitolo = itemView.findViewById(R.id.titolo);
        }

        public void bind(final AttrazioneDAO.Attrazione attrazione,
                         final CmsAttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener listener){

            mAttrazioneTitolo.setText(attrazione.Titolo);

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

    protected final static class AttrazioneAdapter extends RecyclerView.Adapter<AttrazioneViewHolder>{

        private final List<AttrazioneDAO.Attrazione> mModel;

        AttrazioneAdapter(final List<AttrazioneDAO.Attrazione> model, CmsAttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public AttrazioneViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_cms_attrazione,viewGroup,false);

            return new AttrazioneViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(AttrazioneViewHolder attrazioneViewHolder, int position){
            attrazioneViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            void onItemSelected(AttrazioneDAO.Attrazione attrazione);
        }

        private CmsAttrazioniActivity.AttrazioneAdapter.OnItemSelectedListener mListener;
    }
}
