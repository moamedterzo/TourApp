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

public class UtenteDAO extends BaseDAO {

    public static final String TIPO_UTENTE_ADMIN = "A";
    public static final String TIPO_UTENTE_VISITATORE = "V";
    public static final String TIPO_UTENTE_OPERATORE = "O";


    public static void getUtenti(final String filterDescrizione, final String filterTipoUtente,
                                 @NonNull final it.uniba.gruppo5.tourapp.firebase.ReadValueListener<ArrayList<Utente>> listener) {

        final ValueEventListener utentiDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Utente> result = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Utente data = postSnapshot.getValue(Utente.class);

                    //filtraggio
                    if((filterDescrizione == null
                            || filterDescrizione.isEmpty()
                            || (data.Nominativo != null && data.Nominativo.toLowerCase().contains(filterDescrizione.toLowerCase()))
                            || (data.Email != null && data.Email.toLowerCase().contains(filterDescrizione.toLowerCase())))
                            &&(filterTipoUtente == null
                            || filterTipoUtente.isEmpty()
                            || (data.Tipo != null && filterTipoUtente.toLowerCase().equals(data.Tipo.toLowerCase())))) {

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

        mReference.child(UTENTI_CHILD).addListenerForSingleValueEvent(utentiDataListener);
    }

    public static void getSingleUtente(final Context context, @NonNull final it.uniba.gruppo5.tourapp.firebase.ReadValueListener<Utente> listener, String key) {

        final ValueEventListener utentiDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Utente result = dataSnapshot.getValue(Utente.class);

                if(result != null) {
                    result.ID = dataSnapshot.getKey();
                    result.immaginePrincipale = new FileStorageDAO(context, result.FilePathImmagine);

                    listener.onDataRead(result);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(UTENTI_CHILD).child(key).addListenerForSingleValueEvent(utentiDataListener);
    }

    public static void getUtenteFromCredentials(final String username, final String password,
                                 @NonNull final it.uniba.gruppo5.tourapp.firebase.ReadValueListener<Utente> listener) {

        final ValueEventListener utentiDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Utente data = postSnapshot.getValue(Utente.class);
                    data.ID = postSnapshot.getKey();

                    if (data.Email != null && data.Email.equals(username)) {

                        if (data.Password != null && data.Password.equals(password)) {
                            //ok, utente trovato
                            listener.onDataRead(data);
                            return;
                        } else {
                            //password errata
                            listener.onDataRead(null);
                            return;
                        }
                    }
                }

                //nessun utente trovato
                listener.onDataRead(null);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:getUtenteFromCredentials", databaseError.toException());
            }
        };

        mReference.child(UTENTI_CHILD).addListenerForSingleValueEvent(utentiDataListener);
    }

    public static void getUtenteFromEmail(final String username,
                                                @NonNull final it.uniba.gruppo5.tourapp.firebase.ReadValueListener<Utente> listener) {

        final ValueEventListener utentiDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Utente data = postSnapshot.getValue(Utente.class);
                    data.ID = postSnapshot.getKey();

                    if (data.Email != null && data.Email.equals(username)) {

                        //ok, utente trovato
                        listener.onDataRead(data);
                        return;
                    }
                }

                //nessun utente trovato
                listener.onDataRead(null);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:getUtenteFromEmail", databaseError.toException());
            }
        };

        mReference.child(UTENTI_CHILD).addListenerForSingleValueEvent(utentiDataListener);
    }

    public static void addUtente(final Utente utente){
        final DatabaseReference objRef=mReference.child(UTENTI_CHILD).push();
        objRef.setValue(utente);

        utente.ID = objRef.getKey();

        if(utente.immaginePrincipale != null)
            utente.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
                @Override
                public void onImageUploaded(String resultPath) {

                    //salvataggio immagine
                    utente.FilePathImmagine = resultPath;
                    objRef.setValue(utente);
                }
            });
    }


    public static void updateUtente(final Utente utente){
        final DatabaseReference objRef = mReference.child(UTENTI_CHILD).child(utente.ID);
        objRef.setValue(utente);

        utente.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
            @Override
            public void onImageUploaded(String resultPath) {

                String oldImage = utente.FilePathImmagine;

                //salvataggio immagine
                utente.FilePathImmagine = resultPath;
                objRef.setValue(utente);

                //cancello file precedente
                FileStorageDAO.deleteFile(oldImage);
            }
        });
    }

    public static void removeUtente(String IDUtente) {
        DatabaseReference objRef = mReference.child(UTENTI_CHILD).child(IDUtente);
        objRef.removeValue();

    }



    public static class Utente
    {
        @Exclude
        public String ID;

        public String Nominativo;

        public String Password;

        public String DataNascita;

        public String Email;

        public String Tipo;

        public String Stato;

        public String FilePathImmagine;

        @Exclude
        public FileStorageDAO immaginePrincipale;

        public Utente(){
            // Default constructor required for calls to DataSnapshot.getValue
        }
        public Utente(String Nominativo,String Email,String DataNascita,String Password){
            this.Nominativo=Nominativo;
            this.Email=Email;
            this.DataNascita=DataNascita;
            this.Password=Password;

        }
    }
}


