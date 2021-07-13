package it.uniba.gruppo5.tourapp.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

public class DatiGeneraliDAO extends BaseDAO {

    public static void getDatiGenerali(final Context context, @NonNull final it.uniba.gruppo5.tourapp.firebase.ReadValueListener<DatiGenerali> listener) {

        final ValueEventListener vehicleDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DatiGenerali result = dataSnapshot.getValue(DatiGenerali.class);
                result.immaginePrincipale =  new FileStorageDAO(context, result.FilePathImmaginePrincipale);

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(DATI_GENERALI_CHILD).addListenerForSingleValueEvent(vehicleDataListener);
    }


    public static void updateDatiGenerali(final DatiGenerali datiGenerali){

        final DatabaseReference objRef = mReference.child(DATI_GENERALI_CHILD);
        objRef.setValue(datiGenerali);

        datiGenerali.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
            @Override
            public void onImageUploaded(String resultPath) {

                String oldImage = datiGenerali.FilePathImmaginePrincipale;

                //salvataggio immagine
                datiGenerali.FilePathImmaginePrincipale = resultPath;
                objRef.setValue(datiGenerali);

                //cancello file precedente
                FileStorageDAO.deleteFile(oldImage);
            }
        });
    }


    public static class DatiGenerali
    {
        public String NomeCitta;

        public String FilePathImmaginePrincipale;

        @Exclude
        public FileStorageDAO immaginePrincipale;

        public DatiGenerali(){
            // Default constructor required for calls to DataSnapshot.getValue

        }
    }
}