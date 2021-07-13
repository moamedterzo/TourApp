package it.uniba.gruppo5.tourapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.firebase.CategoriaAttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;

public class CategorieAttrazioniActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorie_attrazioni);
        setMenuAndDrawer();

        final RecyclerView recyclerView = findViewById(R.id.elenco_categorie);


        CategoriaAttrazioneDAO.getCategorie(null, new ReadValueListener<ArrayList<CategoriaAttrazioneDAO.Categoria>>() {
            @Override
            public void onDataRead(ArrayList<CategoriaAttrazioneDAO.Categoria> result) {

                CategorieAttrazioniActivity.CategoriaAdapter categoriaAdapter = new CategorieAttrazioniActivity.CategoriaAdapter(result, itemSelectedListener);
                recyclerView.setAdapter(categoriaAdapter);
            }
        });
    }

    CategorieAttrazioniActivity.CategoriaAdapter.OnItemSelectedListener itemSelectedListener = new  CategorieAttrazioniActivity.CategoriaAdapter.OnItemSelectedListener(){

        @Override
        public void onItemSelected(CategoriaAttrazioneDAO.Categoria categoria) {
            // Go to the details page for the selected restaurant
            Intent intent = new Intent(CategorieAttrazioniActivity.this, AttrazioniActivity.class);
            intent.putExtra(AttrazioniActivity.INTENT_ID_CATEGORIA ,categoria.ID);

            startActivity(intent);
        }

    };



    protected final static class CategoriaViewHolder extends RecyclerView.ViewHolder{

        private Button bottone_categoria;
        private TextView txtVwCategoria;

        public CategoriaViewHolder(View itemView){

            super(itemView);

            bottone_categoria = itemView.findViewById(R.id.bottone_categoria);
            txtVwCategoria = itemView.findViewById(R.id.textViewBottoneCategoria);
        }

        public void bind(final CategoriaAttrazioneDAO.Categoria categoria,
                         final CategorieAttrazioniActivity.CategoriaAdapter.OnItemSelectedListener listener){

            txtVwCategoria.setText(categoria.getTitoloByLingua());

            categoria.immaginePrincipale = new FileStorageDAO(this.itemView.getContext(),categoria.FilePathImmagine);
            categoria.immaginePrincipale.readFile(new ReadFileStorageListener() {
                @Override
                public void onFileRead(String absolutePath) {
                    Drawable drawableImage = Drawable.createFromPath(absolutePath);
                    bottone_categoria.setBackground(drawableImage);
                }
            });


            // Click listener
            bottone_categoria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onItemSelected(categoria);
                    }
                }
            });
        }
    }

    protected final static class CategoriaAdapter extends RecyclerView.Adapter<CategorieAttrazioniActivity.CategoriaViewHolder>{

        private final List<CategoriaAttrazioneDAO.Categoria> mModel;

        CategoriaAdapter(final List<CategoriaAttrazioneDAO.Categoria> model, CategorieAttrazioniActivity.CategoriaAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public CategorieAttrazioniActivity.CategoriaViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_categoria_attrazione,viewGroup,false);

            return new CategorieAttrazioniActivity.CategoriaViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(CategorieAttrazioniActivity.CategoriaViewHolder categoriaViewHolder, int position){
            categoriaViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            void onItemSelected(CategoriaAttrazioneDAO.Categoria categoria);
        }

        private CategorieAttrazioniActivity.CategoriaAdapter.OnItemSelectedListener mListener;
    }
}
