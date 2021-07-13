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
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.TagAttrazioneDAO;
import it.uniba.gruppo5.tourapp.fragments.FilterDialogFragment;

public class CmsTagAttrazioneActivity extends CmsBaseActivity
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
        setContentView(R.layout.activity_cms_tag_attrazione);
        setMenuAndDrawer();

        //ottenimento riferimenti viste
        mCurrentSearchView = findViewById(R.id.text_current_search);

        //al click della card mostro il dialog fragment
        CardView filter_bar = findViewById(R.id.filter_bar);
        filter_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCustomDialogFragment = new FilterDialogFragment();
                mCustomDialogFragment.show(getSupportFragmentManager(), CmsTagAttrazioneActivity.class.getName());
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

                CmsTagAttrazioneActivity.this.onFilter();
            }
        });

        //impostazioni recycler view
        recyclerView = findViewById(R.id.cat_attrazione_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = findViewById(R.id.fab_nuovo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nuovaTag = new Intent(CmsTagAttrazioneActivity.this,CmsTagAttrazioneDetailActivity.class);
                startActivity(nuovaTag);
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
        return R.layout.dialog_filters_cms_cat_attrazione;
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

        TagAttrazioneDAO.getTag(filterDescrizione, null, new ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>>() {
            @Override
            public void onDataRead(ArrayList<TagAttrazioneDAO.Tag> result) {

                TagAdapter tagAdapter = new TagAdapter(result, itemSelectedListener);
                CmsTagAttrazioneActivity.this.recyclerView.setAdapter(tagAdapter);
            }
        });
    }



    TagAdapter.OnItemSelectedListener itemSelectedListener = new  TagAdapter.OnItemSelectedListener(){

        @Override
        public void onItemSelected(TagAttrazioneDAO.Tag tag) {
            // Go to the details page for the selected restaurant
            Intent intent = new Intent(CmsTagAttrazioneActivity.this, CmsTagAttrazioneDetailActivity.class);
            intent.putExtra("ID",tag.ID);

            startActivity(intent);
        }

    };



    protected final static class TagViewHolder extends RecyclerView.ViewHolder{

        private TextView mTagTitolo;

        public TagViewHolder(View itemView){

            super(itemView);

            mTagTitolo = itemView.findViewById(R.id.titolo);
        }

        public void bind(final TagAttrazioneDAO.Tag tag,
                         final CmsTagAttrazioneActivity.TagAdapter.OnItemSelectedListener listener){

            mTagTitolo.setText(tag.Titolo);

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(tag);
                    }
                }
            });
        }
    }

    protected final static class TagAdapter extends RecyclerView.Adapter<TagViewHolder>{

        private final List<TagAttrazioneDAO.Tag> mModel;

        TagAdapter(final List<TagAttrazioneDAO.Tag> model, CmsTagAttrazioneActivity.TagAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public TagViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_cms_tag_attrazione,viewGroup,false);

            return new TagViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(TagViewHolder tagViewHolder, int position){
            tagViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            void onItemSelected(TagAttrazioneDAO.Tag tag);
        }

        private CmsTagAttrazioneActivity.TagAdapter.OnItemSelectedListener mListener;
    }
}
