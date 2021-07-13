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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import it.uniba.gruppo5.tourapp.utilities.DateHelper;

public class CouponDAO extends BaseDAO{

    public static final String TIPO_SCONTO_PERCENTUALE = "P";
    public static final String TIPO_SCONTO_FISSO = "F";

    public static void getCouponsAreaPrivata(final String filterDescrizione,
                                             final String filterCategoria,
                                             @NonNull final ReadValueListener<ArrayList<CouponDAO.Coupon>> listener) {

        final ValueEventListener couponsDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<CouponDAO.Coupon> result = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CouponDAO.Coupon data = postSnapshot.getValue(CouponDAO.Coupon.class);

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

        mReference.child(COUPON_CHILD).addListenerForSingleValueEvent(couponsDataListener);
    }

    public static void getCouponsPreferiti(final ArrayList<String> idsCoupons,
                                           @NonNull final ReadValueListener<ArrayList<CouponDAO.Coupon>> listener) {

        final ValueEventListener couponsDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<CouponDAO.Coupon> result = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CouponDAO.Coupon data = postSnapshot.getValue(CouponDAO.Coupon.class);
                    data.ID = postSnapshot.getKey();

                    for (int i = 0; i < idsCoupons.size(); i++) {
                        if (data.ID.contains(idsCoupons.get(i))) {
                            result.add(data);
                            break;
                        }
                    }
                }

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(COUPON_CHILD).addListenerForSingleValueEvent(couponsDataListener);
    }


    public static void getCouponsPartePubblica(final String filterCategoria,
                                  @NonNull final ReadValueListener<ArrayList<CouponDAO.Coupon>> listener) {

        final ValueEventListener couponsDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //vengono presi i coupon che hanno la data di validità fine maggiore della data corrente
                //e la data validità inizio minore della data corrente
                Date dataCorrente =  Calendar.getInstance().getTime();

                ArrayList<CouponDAO.Coupon> result = new ArrayList<>();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CouponDAO.Coupon data = postSnapshot.getValue(CouponDAO.Coupon.class);

                    //filtraggio
                    if(DateHelper.getDateFromString(data.DataInizioValidita).compareTo(dataCorrente) <= 0 &&
                            DateHelper.getDateFromString(data.DataFineValidita).compareTo(dataCorrente) >= 0 &&
                            (filterCategoria == null
                            || filterCategoria.isEmpty()
                            || (data.IDCategoria != null && data.IDCategoria.toLowerCase().equals(filterCategoria.toLowerCase())))){

                        data.ID = postSnapshot.getKey();
                        result.add(data);
                    }
                }

                //ordinamento per data di scadenza crescente
                result.sort(new Comparator<Coupon>() {
                    @Override
                    public int compare(Coupon o1, Coupon o2) {

                        Date dataFine1 = DateHelper.getDateFromString(o1.DataFineValidita);
                        Date dataFine2 = DateHelper.getDateFromString(o2.DataFineValidita);

                        return dataFine1.compareTo(dataFine2);
                    }
                });

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCouponsPartePubblica:onCancelled", databaseError.toException());
            }
        };

        mReference.child(COUPON_CHILD).addListenerForSingleValueEvent(couponsDataListener);
    }


    public static void getSingleCoupon(final Context context, @NonNull final ReadValueListener<CouponDAO.Coupon> listener, String key) {

        final ValueEventListener couponDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                CouponDAO.Coupon result = dataSnapshot.getValue(CouponDAO.Coupon.class);
                result.ID = dataSnapshot.getKey();
                result.immaginePrincipale =  new FileStorageDAO(context, result.FilePathImmagine);

                result.Utilizzi = new ArrayList<>();
                for(DataSnapshot children : dataSnapshot.child(COUPON_UTILIZZO_CHILD).getChildren())
                {
                    result.Utilizzi.add(children.getKey());
                }

                listener.onDataRead(result);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        mReference.child(COUPON_CHILD).child(key).addListenerForSingleValueEvent(couponDataListener);
    }


    public static void addCoupon(final CouponDAO.Coupon coupon){
        final DatabaseReference objRef=mReference.child(COUPON_CHILD).push();
        objRef.setValue(coupon);

        coupon.ID = objRef.getKey();

        if(coupon.immaginePrincipale != null)
            coupon.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
                @Override
                public void onImageUploaded(String resultPath) {

                    //salvataggio immagine
                    coupon.FilePathImmagine = resultPath;
                    objRef.setValue(coupon);
                }
            });
    }


    public static void updateCoupon(final CouponDAO.Coupon coupon){
        final DatabaseReference objRef = mReference.child(COUPON_CHILD).child(coupon.ID);
        objRef.setValue(coupon);

        //aggiunta utilizzo
        final DatabaseReference objRefTags=  objRef.child(COUPON_UTILIZZO_CHILD);
        for(String idTag : coupon.Utilizzi){
            objRefTags.child(idTag).setValue("1");
        }

        coupon.immaginePrincipale.saveFile(new FileStorageDAO.FileStorageListener() {
            @Override
            public void onImageUploaded(String resultPath) {

                String oldImage = coupon.FilePathImmagine;

                //salvataggio immagine
                coupon.FilePathImmagine = resultPath;
                objRef.setValue(coupon);

                //aggiunta tag
                final DatabaseReference objRefTags=  objRef.child(COUPON_UTILIZZO_CHILD);
                for(String idTag : coupon.Utilizzi){
                    objRefTags.child(idTag).setValue("1");
                }

                //cancello file precedente
                FileStorageDAO.deleteFile(oldImage);
            }
        });
    }

    public static void removeCoupon(String IDUtente) {
        DatabaseReference objRef = mReference.child(COUPON_CHILD).child(IDUtente);
        objRef.removeValue();
    }


    public static void utilizzaCoupon(String idCoupon, String idUtente){

        final DatabaseReference objRef=mReference.child(COUPON_CHILD).child(idCoupon).child(COUPON_UTILIZZO_CHILD);
        objRef.child(idUtente).setValue("1");
    }

    public static void getCouponUtilizzo(String idCoupon, final String idUtente, final ReadValueListener<Boolean> listener){

        final ValueEventListener couponDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean existUtilizzo = false;

                //è true se l'utilizzo è stato effettuato da parte dell'utente
                if (dataSnapshot != null) {

                    DataSnapshot childUtilizzi = dataSnapshot.child(COUPON_UTILIZZO_CHILD);

                    if (childUtilizzi.hasChildren()) {

                        Object childUtilizzoUtenteValue = childUtilizzi.child(idUtente).getValue();

                        if (childUtilizzoUtenteValue != null) {
                            existUtilizzo = childUtilizzoUtenteValue.toString().equals("1");
                        }
                    }
                }

                listener.onDataRead(existUtilizzo);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getCouponUtilizzo:onCancelled", databaseError.toException());
            }
        };

        mReference.child(COUPON_CHILD).child(idCoupon).addListenerForSingleValueEvent(couponDataListener);
    }

    public static class Coupon
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

        public String DataInizioValidita;

        public String DataFineValidita;

        public String TipoSconto; //percentuale o importo

        public float Importo;

        public String IDCategoria;

        public String FilePathImmagine;

        @Exclude
        public FileStorageDAO immaginePrincipale;

        @Exclude
        public ArrayList<String> Utilizzi;

        public Coupon(){
            // Default constructor required for calls to DataSnapshot.getValue
        }

        @Exclude
        public String getImportoLabel(){

            if (this.TipoSconto.equals(TIPO_SCONTO_FISSO)){
                return String.format("%.2f", this.Importo) + " €";
            }
            else {
                return String.format("%.2f", this.Importo) + " %";
            }
        }
    }
}
