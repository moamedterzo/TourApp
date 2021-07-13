package it.uniba.gruppo5.tourapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.cms.CmsDatiGeneraliActivity;
import it.uniba.gruppo5.tourapp.firebase.CategoriaCouponDAO;
import it.uniba.gruppo5.tourapp.firebase.CouponDAO;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;


public class CouponActivity extends BaseActivity {

    private final static String BUNDLE_FILTER_CATEGORIA = "bd_ft_cat";

    private RecyclerView categorieRecyclerView, recyclerView;
    String filterCategoria;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        setMenuAndDrawer();


        //creo Reycler view dei coupon 
        recyclerView = findViewById(R.id.coupon_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        //recycler view delle categorie dei coupon
        categorieRecyclerView = findViewById(R.id.rec_view_categorie);

        //divisore degli elementi
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(CouponActivity.this, LinearLayoutManager.HORIZONTAL, false);
        categorieRecyclerView.setLayoutManager(horizontalLayoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_coupon, menu);

        //voce di menu visibile soltanto se l'utente è autenticato
        MenuItem menu_preferiti = menu.findItem(R.id.menu_preferiti);
        menu_preferiti.setVisible(UserAuthenticationManager.IsUserAuthenticated(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preferiti:
                //si va nell'activity dei preferiti
                Intent intent= new Intent(CouponActivity.this,CouponPreferitiActivity.class);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        this.loadCouponList();
        this.loadCategorieList();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        //salvataggio ricerca corrente
        savedInstanceState.putString(BUNDLE_FILTER_CATEGORIA, filterCategoria);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //ottenimento stato ricerca corrente
        filterCategoria = savedInstanceState.getString(BUNDLE_FILTER_CATEGORIA);
    }

    public void loadCouponList() {

        CouponDAO.getCouponsPartePubblica(filterCategoria, new ReadValueListener<ArrayList<CouponDAO.Coupon>>() {
            @Override
            public void onDataRead(ArrayList<CouponDAO.Coupon> result) {

                CouponAdapter couponAdapter = new CouponAdapter(result, couponSelectedListener);
                CouponActivity.this.recyclerView.setAdapter(couponAdapter);
            }
        });
    }

    //listener utilizzato per il click del coupon
    CouponAdapter.OnItemClickListener couponSelectedListener = new CouponAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(CouponDAO.Coupon coupon) {
            Intent intent = new Intent(CouponActivity.this, DettaglioCouponActivity.class);
            intent.putExtra("ID", coupon.ID);
            startActivity(intent);
        }

    };


    private void loadCategorieList() {

        CategoriaCouponDAO.getCategorie(null, new ReadValueListener<ArrayList<CategoriaCouponDAO.Categoria>>() {
            @Override
            public void onDataRead(ArrayList<CategoriaCouponDAO.Categoria> result) {

                CouponActivity.CategoriaAdapter categoriaAdapter = new CouponActivity.CategoriaAdapter(result, itemSelectedListenerCategoria);
                CouponActivity.this.categorieRecyclerView.setAdapter(categoriaAdapter);
            }
        });
    }

    //listener utilizzato per il click del coupon
    CategoriaAdapter.OnItemClickListener itemSelectedListenerCategoria = new CategoriaAdapter.OnItemClickListener() {

        @Override
        public boolean onItemClick(CategoriaCouponDAO.Categoria categoria) {

            boolean isItemSelected = true;

            if(categoria.ID.equals(CouponActivity.this.filterCategoria)){
                //vuol dire che è stato cliccato nuovamente lo stesso elemento, allora deseleziono
                filterCategoria = null;
                isItemSelected = false;
            }
            else {
                if(filterCategoria != null && !filterCategoria.isEmpty()){
                    //sto selezionando un nuovo elemento, effettuo nuovamente il binding degli elementi per deselezionare lo stato
                    CouponActivity.this.loadCategorieList();
                }

                filterCategoria = categoria.ID;
            }

            CouponActivity.this.loadCouponList();

            return isItemSelected;
        }

    };


    protected static class CouponViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView textViewTitle, textViewImporto, textViewRating;


        public CouponViewHolder(View view) {
            super(view);

            image = itemView.findViewById(R.id.coupon_card);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewImporto = itemView.findViewById(R.id.textViewShortDesc);
            textViewRating = itemView.findViewById(R.id.textViewRating);
        }

        public void bind(final CouponDAO.Coupon coupon, final CouponAdapter.OnItemClickListener listener) {

            textViewTitle.setText(coupon.getTitoloByLingua());
            textViewImporto.setText(coupon.getImportoLabel());
            textViewRating.setText(coupon.DataFineValidita);

            coupon.immaginePrincipale =  new FileStorageDAO(this.itemView.getContext(), coupon.FilePathImmagine);
            coupon.immaginePrincipale.readFile(new ReadFileStorageListener() {
                @Override
                public void onFileRead(String absolutePath) {


                    Glide.with(itemView).load(new File(absolutePath)).into(image);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(coupon);
                }
            });
        }
    }

    protected static class CouponAdapter extends RecyclerView.Adapter<CouponViewHolder> {

        public interface OnItemClickListener {
            void onItemClick(CouponDAO.Coupon item);
        }

        private OnItemClickListener clickListener;
        private ArrayList<CouponDAO.Coupon> couponList;

        public CouponAdapter(ArrayList<CouponDAO.Coupon> couponList, OnItemClickListener listener) {
            this.couponList = couponList;
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_coupon, viewGroup, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder viewHolder, int i) {

        viewHolder.bind(couponList.get(i), clickListener);
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }
}


    protected final static class CategoriaViewHolder extends RecyclerView.ViewHolder{

        private TextView mCategoriaTitolo;
        private ImageView mImage;

        public CategoriaViewHolder(View itemView){

            super(itemView);

            mCategoriaTitolo = itemView.findViewById(R.id.label);
            mImage = itemView.findViewById(R.id.image);
        }

        public void bind(final CategoriaCouponDAO.Categoria categoria,
                         final CategoriaAdapter.OnItemClickListener listener){

            mCategoriaTitolo.setText(categoria.getTitoloByLingua());

            categoria.immaginePrincipale = new FileStorageDAO(this.itemView.getContext(), categoria.FilePathImmagine);
            categoria.immaginePrincipale.readFile(new ReadFileStorageListener() {
                @Override
                public void onFileRead(String absolutePath) {

                    Glide.with(itemView).load(new File(absolutePath)).into(mImage);
                }
            });


            //controllo se l'item corrente sia stato selezionato dall'utente
            CouponActivity couponActivity = (CouponActivity)itemView.getContext();
            if(categoria.ID.equals(couponActivity.filterCategoria)){
                //selected
                itemView.setBackground(itemView.getContext().getDrawable(R.drawable.shape_selected));
                mCategoriaTitolo.setTextColor(R.color.background_layout2);

            }


            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                       if(listener.onItemClick(categoria)){
                           itemView.setBackground(itemView.getContext().getDrawable(R.drawable.shape_selected));
                           mCategoriaTitolo.setTextColor(R.color.background_layout2);
                       }
                       else{
                           itemView.setBackground(itemView.getContext().getDrawable(R.color.colorBackgraound));
                           mCategoriaTitolo.setTextColor(R.color.colorGiall);
                       }
                    }

                }
            });
        }
    }

    protected final static class CategoriaAdapter extends RecyclerView.Adapter<CouponActivity.CategoriaViewHolder>{

        private final List<CategoriaCouponDAO.Categoria> mModel;

        CategoriaAdapter(final List<CategoriaCouponDAO.Categoria> model, OnItemClickListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public CouponActivity.CategoriaViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_horizontal,viewGroup,false);

            return new CouponActivity.CategoriaViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(CouponActivity.CategoriaViewHolder categoriaViewHolder, int position){
            categoriaViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemClickListener {

            boolean onItemClick(CategoriaCouponDAO.Categoria categoria);
        }

        private OnItemClickListener mListener;
    }
}
