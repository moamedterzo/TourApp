package it.uniba.gruppo5.tourapp.firebase;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TagAttrazioneDAO extends BaseDAO{

    public static void getTag(final String filterDescrizione,final String filterCategoria,
                              @NonNull final ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>> listener) {

        final ValueEventListener tagDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<TagAttrazioneDAO.Tag> result = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    TagAttrazioneDAO.Tag data = postSnapshot.getValue(TagAttrazioneDAO.Tag.class);

                    //filtraggio
                    if((filterDescrizione == null
                            || filterDescrizione.isEmpty()
                            || (data.Titolo != null && data.Titolo.toLowerCase().contains(filterDescrizione.toLowerCase())))
                            &&(filterCategoria == null
                            || filterCategoria.isEmpty()
                            || (data.IDCategoria != null && data.IDCategoria.equals(filterCategoria)))){

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

        mReference.child(CATEGORIE_TAG_CHILD).addListenerForSingleValueEvent(tagDataListener);
    }

    public static void getSingleTag(final Context context, @NonNull final ReadValueListener<TagAttrazioneDAO.Tag> listener, String key) {

        final ValueEventListener attrazioneDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TagAttrazioneDAO.Tag result = dataSnapshot.getValue(TagAttrazioneDAO.Tag.class);
                result.ID = dataSnapshot.getKey();
                result.immaginePrincipale =  new FileStorageDAO(context, result.FilePathImmagine);

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(CATEGORIE_TAG_CHILD).child(key).addListenerForSingleValueEvent(attrazioneDataListener);
    }


    public static void addTag(final TagAttrazioneDAO.Tag tag){
        final DatabaseReference objRef=mReference.child(CATEGORIE_TAG_CHILD).push();
        objRef.setValue(tag);

        tag.ID = objRef.getKey();

        if(tag.immaginePrincipale != null)
            tag.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
                @Override
                public void onImageUploaded(String resultPath) {

                    //salvataggio immagine
                    tag.FilePathImmagine = resultPath;
                    objRef.setValue(tag);
                }
            });
    }


    public static void updateTag(final TagAttrazioneDAO.Tag tag){
        final DatabaseReference objRef = mReference.child(CATEGORIE_TAG_CHILD).child(tag.ID);
        objRef.setValue(tag);

        tag.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
            @Override
            public void onImageUploaded(String resultPath) {

                String oldImage = tag.FilePathImmagine;

                //salvataggio immagine
                tag.FilePathImmagine = resultPath;
                objRef.setValue(tag);

                //cancello file precedente
                FileStorageDAO.deleteFile(oldImage);
            }
        });
    }

    public static void removeTag(String IDTag) {
        DatabaseReference objRef = mReference.child(CATEGORIE_TAG_CHILD).child(IDTag);
        objRef.removeValue();
    }



    public static class Tag implements Parcelable
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

        public String IDCategoria;

        public String FilePathImmagine;

        @Exclude
        public FileStorageDAO immaginePrincipale;

        public Tag(){
            // Default constructor required for calls to DataSnapshot.getValue
        }

        public Tag(Parcel in){
            ID = in.readString();
            Titolo = in.readString();
            TitoloItaliano = in.readString();
            IDCategoria = in.readString();
            FilePathImmagine = in.readString();
        }

        @Override
        public int describeContents(){
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            dest.writeString(ID);
            dest.writeString(Titolo);
            dest.writeString(TitoloItaliano);
            dest.writeString(IDCategoria);
            dest.writeString(FilePathImmagine);
        }
    }
}
