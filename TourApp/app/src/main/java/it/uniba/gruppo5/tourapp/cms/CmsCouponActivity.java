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
import it.uniba.gruppo5.tourapp.firebase.CouponDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.fragments.FilterDialogFragment;

public class CmsCouponActivity extends CmsBaseActivity
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
        setContentView(R.layout.activity_cms_coupon);
        setMenuAndDrawer();

        //ottenimento riferimenti viste
        mCurrentSearchView = findViewById(R.id.text_current_search);

        //al click della card mostro il dialog fragment
        CardView filter_bar = findViewById(R.id.filter_bar);
        filter_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCustomDialogFragment = new FilterDialogFragment();
                mCustomDialogFragment.show(getSupportFragmentManager(), CmsCouponActivity.class.getName());
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

                CmsCouponActivity.this.onFilter();
            }
        });

        //impostazioni recycler view
        recyclerView = findViewById(R.id.cat_coupon_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);

        FloatingActionButton fab = findViewById(R.id.fab_nuovo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nuovoCoupon = new Intent(CmsCouponActivity.this,CmsCouponDetailActivity.class);
                startActivity(nuovoCoupon);
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
        return R.layout.dialog_filters_cms_coupon;
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

        CouponDAO.getCouponsAreaPrivata(filterDescrizione, null, new ReadValueListener<ArrayList<CouponDAO.Coupon>>() {
            @Override
            public void onDataRead(ArrayList<CouponDAO.Coupon> result) {

                CouponAdapter couponAdapter = new CouponAdapter(result, itemSelectedListener);
                CmsCouponActivity.this.recyclerView.setAdapter(couponAdapter);
            }
        });
    }



    CouponAdapter.OnItemSelectedListener itemSelectedListener = new  CouponAdapter.OnItemSelectedListener(){

        @Override
        public void onItemSelected(CouponDAO.Coupon coupon) {
            // Go to the details page for the selected restaurant
            Intent intent = new Intent(CmsCouponActivity.this, CmsCouponDetailActivity.class);
            intent.putExtra("ID",coupon.ID);

            startActivity(intent);
        }

    };



    protected final static class CouponViewHolder extends RecyclerView.ViewHolder{

        private TextView mCouponTitolo;
        private TextView mCouponSconto;

        public CouponViewHolder(View itemView){

            super(itemView);

            mCouponTitolo = itemView.findViewById(R.id.titolo);
            mCouponSconto = itemView.findViewById(R.id.sconto);
        }

        public void bind(final CouponDAO.Coupon coupon,
                         final CmsCouponActivity.CouponAdapter.OnItemSelectedListener listener){

            mCouponTitolo.setText(coupon.Titolo);
            mCouponSconto.setText(coupon.getImportoLabel());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(coupon);
                    }
                }
            });
        }
    }

    protected final static class CouponAdapter extends RecyclerView.Adapter<CouponViewHolder>{

        private final List<CouponDAO.Coupon> mModel;

        CouponAdapter(final List<CouponDAO.Coupon> model, CmsCouponActivity.CouponAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public CouponViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_cms_coupon,viewGroup,false);

            return new CouponViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(CouponViewHolder couponViewHolder, int position){
            couponViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            void onItemSelected(CouponDAO.Coupon coupon);
        }

        private CmsCouponActivity.CouponAdapter.OnItemSelectedListener mListener;
    }
}
