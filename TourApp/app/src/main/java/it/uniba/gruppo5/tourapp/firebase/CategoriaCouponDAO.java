package it.uniba.gruppo5.tourapp.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoriaCouponDAO extends BaseDAO{

    public static void getCategorie(final String filterDescrizione,
                                 @NonNull final it.uniba.gruppo5.tourapp.firebase.ReadValueListener<ArrayList<CategoriaCouponDAO.Categoria>> listener) {

        final ValueEventListener categorieDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<CategoriaCouponDAO.Categoria> result = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CategoriaCouponDAO.Categoria data = postSnapshot.getValue(CategoriaCouponDAO.Categoria.class);

                    //filtraggio
                    if((filterDescrizione == null
                            || filterDescrizione.isEmpty()
                            || (data.Titolo!= null && data.Titolo.toLowerCase().contains(filterDescrizione.toLowerCase())))){

                        data.ID = postSnapshot.getKey();
                        result.add(data);
                    }
                }

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(CATEGORIE_COUPON_CHILD).addListenerForSingleValueEvent(categorieDataListener);
    }

    public static void getSingleCategoria(final Context context, @NonNull final it.uniba.gruppo5.tourapp.firebase.ReadValueListener<CategoriaCouponDAO.Categoria> listener, String key) {

        final ValueEventListener couponDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                CategoriaCouponDAO.Categoria result = dataSnapshot.getValue(CategoriaCouponDAO.Categoria.class);
                result.ID = dataSnapshot.getKey();
                result.immaginePrincipale =  new FileStorageDAO(context, result.FilePathImmagine);

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(CATEGORIE_COUPON_CHILD).child(key).addListenerForSingleValueEvent(couponDataListener);
    }


    public static void addCategoria(final CategoriaCouponDAO.Categoria categoria){
        final DatabaseReference objRef=mReference.child(CATEGORIE_COUPON_CHILD).push();
        objRef.setValue(categoria);

        categoria.ID = objRef.getKey();

        if(categoria.immaginePrincipale != null)
            categoria.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
                @Override
                public void onImageUploaded(String resultPath) {

                    //salvataggio immagine
                    categoria.FilePathImmagine = resultPath;
                    objRef.setValue(categoria);
                }
            });
    }


    public static void updateCategoria(final CategoriaCouponDAO.Categoria categoria){
        final DatabaseReference objRef = mReference.child(CATEGORIE_COUPON_CHILD).child(categoria.ID);
        objRef.setValue(categoria);

        categoria.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
            @Override
            public void onImageUploaded(String resultPath) {

                String oldImage = categoria.FilePathImmagine;

                //salvataggio immagine
                categoria.FilePathImmagine = resultPath;
                objRef.setValue(categoria);

                //cancello file precedente
                FileStorageDAO.deleteFile(oldImage);
            }
        });
    }

    public static void removeCategoria(String IDUtente) {
        DatabaseReference objRef = mReference.child(CATEGORIE_COUPON_CHILD).child(IDUtente);
        objRef.removeValue();
    }



    public static class Categoria
    {
        @Exclude
        public String ID;

        public String Titolo;

        public String TitoloItaliano;

        @Exclude
        public String getTitoloByLingua(){
            if(BaseDAO.isLanguageItalian() && TitoloItaliano != null && !TitoloItaliano.isEmpty())
                return TitoloItaliano;
            else
                return Titolo;
        }

        public String FilePathImmagine;

        @Exclude
        public FileStorageDAO immaginePrincipale;

        public Categoria(){
            // Default constructor required for calls to DataSnapshot.getValue
        }
    }
}
