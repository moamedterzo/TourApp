package it.uniba.gruppo5.tourapp.firebase;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

import it.uniba.gruppo5.tourapp.utilities.DateHelper;

public class AttrazioneDAO extends BaseDAO{


    public static final String PREZZO_ECONOMICO = "1";
    public static final String PREZZO_NELLA_MEDIA = "2";
    public static final String PREZZO_COSTOSO = "3";

    public static void getAttrazioni(final String filterDescrizione,
                                     final String filterCategoria,
                                     @NonNull final ReadValueListener<ArrayList<AttrazioneDAO.Attrazione>> listener) {

        final ValueEventListener attrazioniDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<AttrazioneDAO.Attrazione> result = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    AttrazioneDAO.Attrazione data = postSnapshot.getValue(AttrazioneDAO.Attrazione.class);

                    //filtraggio
                    if((filterDescrizione == null
                            || filterDescrizione.isEmpty()
                            || (data.Titolo != null && data.Titolo.toLowerCase().contains(filterDescrizione.toLowerCase())))
                            && (filterCategoria == null
                            || filterCategoria.isEmpty()
                            || (data.IDCategoria != null && data.IDCategoria.toLowerCase().equals(filterCategoria.toLowerCase())))){

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

        mReference.child(ATTRAZIONI_CHILD).addListenerForSingleValueEvent(attrazioniDataListener);
    }

    public static void getAttrazioniPubbliche(final Context context,
                                              final String filterDescrizione,
                                              final String filterCategoria,
                                              final String filterTag,
                                              final String filterCosto,
                                     @NonNull final ReadValueListener<AttrazioneDAO.Attrazione> listener) {

        final ValueEventListener attrazioniDataListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    final AttrazioneDAO.Attrazione data = postSnapshot.getValue(AttrazioneDAO.Attrazione.class);

                    //filtraggio
                    if((filterCategoria == null
                            || filterCategoria.isEmpty()
                            || (data.IDCategoria != null && data.IDCategoria.toLowerCase().equals(filterCategoria.toLowerCase())))
                            && (filterCosto == null
                            || filterCosto.isEmpty()
                            || (data.Prezzo != null && data.Prezzo.toLowerCase().equals(filterCosto.toLowerCase())))){

                        data.ID = postSnapshot.getKey();

                        data.Tags = new ArrayList<>();
                        for(DataSnapshot children : postSnapshot.child(ATTRAZIONI_TAG_CHILD).getChildren())
                        {
                            data.Tags.add(children.getKey());
                        }

                        if(filterTag != null && !filterTag.isEmpty()){
                            if(!data.Tags.contains(filterTag)){
                                //elemento scartato
                                continue;
                            }
                        }

                        data.Immagini = new ArrayList<>();
                        for(DataSnapshot children : postSnapshot.child(ATTRAZIONI_IMMAGINI_CHILD).getChildren())
                        {
                            data.Immagini.add(children.getKey());
                        }

                        if(filterDescrizione == null
                                || filterDescrizione.isEmpty()){
                            listener.onDataRead(data);
                        }
                        else {
                            //è necessario effettuare una ricerca per titolo, categoria e tags
                            if(data.getTitoloByLingua() != null && data.getTitoloByLingua().toLowerCase().contains(filterDescrizione.toLowerCase())){
                                listener.onDataRead(data);
                            }
                            else {

                                //categoria, solo se non si è già filtrato per categoria
                                CategoriaAttrazioneDAO.getSingleCategoria(context, new ReadValueListener<CategoriaAttrazioneDAO.Categoria>() {
                                    @Override
                                    public void onDataRead(CategoriaAttrazioneDAO.Categoria categoria) {
                                        if (categoria.getTitoloByLingua() != null && categoria.getTitoloByLingua().toLowerCase().equals(filterDescrizione.toLowerCase())) {
                                            listener.onDataRead(data);
                                        }
                                    }
                                }, data.IDCategoria);


                                //tag, solo se non si è già filtrato per tags
                                readTagAttrazioni(data, new ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>>() {
                                    @Override
                                    public void onDataRead(ArrayList<TagAttrazioneDAO.Tag> tags) {

                                        for (TagAttrazioneDAO.Tag tag : tags) {
                                            if (tag.getTitoloByLingua() != null && tag.getTitoloByLingua().toLowerCase().equals(filterDescrizione.toLowerCase())) {
                                                listener.onDataRead(data);
                                                break;
                                            }
                                        }
                                    }
                                });

                            }
                        }
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(ATTRAZIONI_CHILD).addListenerForSingleValueEvent(attrazioniDataListener);
    }

    public static void getSingleAttrazione(final Context context, @NonNull final ReadValueListener<AttrazioneDAO.Attrazione> listener, String key) {

        final ValueEventListener attrazioneDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                AttrazioneDAO.Attrazione result = dataSnapshot.getValue(AttrazioneDAO.Attrazione.class);
                result.ID = dataSnapshot.getKey();

                result.Tags = new ArrayList<>();
                for(DataSnapshot children : dataSnapshot.child(ATTRAZIONI_TAG_CHILD).getChildren())
                {
                    result.Tags.add(children.getKey());
                }

                result.Immagini = new ArrayList<>();
                for(DataSnapshot children : dataSnapshot.child(ATTRAZIONI_IMMAGINI_CHILD).getChildren())
                {
                    result.Immagini.add(children.getKey());
                }

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(ATTRAZIONI_CHILD).child(key).addListenerForSingleValueEvent(attrazioneDataListener);
    }


    public static void addAttrazione(final AttrazioneDAO.Attrazione attrazione, Context context){
        final DatabaseReference objRef = mReference.child(ATTRAZIONI_CHILD).push();
        objRef.setValue(attrazione);

        attrazione.ID = objRef.getKey();

        //aggiunta tag
        final DatabaseReference objRefTags= objRef.child(ATTRAZIONI_TAG_CHILD);
        for(String idTag : attrazione.Tags){
            objRefTags.child(idTag).setValue("1");
        }

        //aggiunta immagini
        final DatabaseReference objRefImmagini= objRef.child(ATTRAZIONI_IMMAGINI_CHILD);
        for(String img : attrazione.Immagini){
            objRefImmagini.child(img).setValue("1");

            //salvataggio firestore
            FileStorageDAO fileStorageDAO = new FileStorageDAO(context, img);
            fileStorageDAO.setNewUriFile(Uri.fromFile(new File(context.getFilesDir() + "/" + img)));
            fileStorageDAO.saveFile(null);
        }

    }





    public static void updateAttrazione(final AttrazioneDAO.Attrazione attrazione, final Context context){

        readCommentiAttrazione(attrazione.ID, new ReadValueListener<ArrayList<CommentoAttrazione>>() {
            @Override
            public void onDataRead(ArrayList<CommentoAttrazione> result) {

                final DatabaseReference objRef = mReference.child(ATTRAZIONI_CHILD).child(attrazione.ID);
                objRef.setValue(attrazione);

                //aggiunta tag
                final DatabaseReference objRefTags= objRef.child(ATTRAZIONI_TAG_CHILD);
                for(String idTag : attrazione.Tags){
                    objRefTags.child(idTag).setValue("1");
                }

                //aggiunta immagini
                final DatabaseReference objRefImmagini= objRef.child(ATTRAZIONI_IMMAGINI_CHILD);
                for(String img : attrazione.Immagini){
                    objRefImmagini.child(img).setValue("1");

                    //salvataggio firestore
                    Uri file = Uri.fromFile(new File(context.getFilesDir() + "/" + img));
                    FileStorageDAO fileStorageDAO = new FileStorageDAO(context, img);
                    fileStorageDAO.setNewUriFile(file);
                    fileStorageDAO.doSaveFile(img, null);
                }

                for (CommentoAttrazione commentoAttrazione : result){
                    addCommentoAttrazione(commentoAttrazione, attrazione.ID, context);
                }
            }
        });


    }

    public static void removeAttrazione(String IDUtente) {
        DatabaseReference objRef = mReference.child(ATTRAZIONI_CHILD).child(IDUtente);
        objRef.removeValue();
    }



    public static void readTagAttrazioni(final Attrazione attrazione, final ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>> listener) {

        final ArrayList<TagAttrazioneDAO.Tag> resultTags = new ArrayList<>();

        if (attrazione.Tags != null && attrazione.Tags.size() > 0) {

            for (String id : attrazione.Tags) {
                mReference.child(CATEGORIE_TAG_CHILD).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //lettura e aggiunta del tag
                        TagAttrazioneDAO.Tag result = dataSnapshot.getValue(TagAttrazioneDAO.Tag.class);
                        result.ID = dataSnapshot.getKey();

                        resultTags.add(result);

                        if (resultTags.size() == attrazione.Tags.size()) {
                            //ho letto tutti gli elementi, notifico il listener
                            listener.onDataRead(resultTags);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Log.w(TAG, "readTagAttrazioni:onCancelled", databaseError.toException());
                    }
                });
            }
        }
        else
        {
            listener.onDataRead(resultTags);
        }
    }

    public static void readCommentiAttrazione(final String idAttrazione, final ReadValueListener<ArrayList<CommentoAttrazione>> listener) {

        final ArrayList<CommentoAttrazione> resultCommenti = new ArrayList<>();

        final ValueEventListener attrazioniDataListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    final CommentoAttrazione data = postSnapshot.getValue(CommentoAttrazione.class);
                    resultCommenti.add(data);
                }

                //ordinamento per data, ultimo commento visualizzato per primo
                resultCommenti.sort(new Comparator<CommentoAttrazione>() {
                    @Override
                    public int compare(CommentoAttrazione o1, CommentoAttrazione o2) {
                        return DateHelper.getDateFromString(o2.Data).compareTo(DateHelper.getDateFromString(o1.Data));
                    }
                });

                listener.onDataRead(resultCommenti);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "readCommentiAttrazione:onCancelled", databaseError.toException());
            }
        };

        mReference.child(ATTRAZIONI_CHILD).child(idAttrazione).child(ATTRAZIONI_COMMENTI_CHILD).addListenerForSingleValueEvent(attrazioniDataListener);
    }

    public static void addCommentoAttrazione(final CommentoAttrazione commentoAttrazione, String idAttrazione, Context context){
        final DatabaseReference objRef = mReference.child(ATTRAZIONI_CHILD).child(idAttrazione).child(ATTRAZIONI_COMMENTI_CHILD).push();
        objRef.setValue(commentoAttrazione);
    }


    public static class Attrazione
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

        public String Descrizione;

        public String DescrizioneItaliano;

        @Exclude
        public String getDescrizioneByLingua(){
            if(BaseDAO.isLanguageItalian() && DescrizioneItaliano != null && !DescrizioneItaliano.isEmpty())
                return DescrizioneItaliano;
            else
                return Descrizione;
        }

        public String Orario;

        public String Email;

        public String Telefono;

        public String Sitoweb;

        public String IDCategoria;

        public String Prezzo;

        public float Longitudine;

        public float Latitudine;

        @Exclude
        public ArrayList<String> Tags;

        @Exclude
        public ArrayList<String> Immagini;

        public Attrazione(){
            // Default constructor required for calls to DataSnapshot.getValue
        }
    }

    public static class CommentoAttrazione
    {
        public String IDUtente;

        public String Testo;

        public String Data;

        public float Rating;
    }
}
