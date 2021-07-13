package it.uniba.gruppo5.tourapp.firebase;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class BaseDAO {

    protected static DatabaseReference mReference;

    public static void initializeApp(Context context){
        FirebaseApp.initializeApp(context);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        mReference = FirebaseDatabase.getInstance().getReference();
    }


    protected static String TAG = "DAOFirebase";

    protected static String DATI_GENERALI_CHILD ="DatiGenerali";
    protected static String UTENTI_CHILD ="Utenti";
    protected static String CATEGORIE_COUPON_CHILD ="CategorieCoupon";

    protected static String COUPON_CHILD ="Coupon";
    protected static String COUPON_UTILIZZO_CHILD ="UtentiUtilizzatori";

    protected static String CATEGORIE_ATTRAZIONI_CHILD ="CategorieAttrazioni";
    protected static String CATEGORIE_TAG_CHILD ="TagAttrazioni";

    protected static String ATTRAZIONI_CHILD ="Attrazioni";
    protected static String ATTRAZIONI_TAG_CHILD ="Tags";
    protected static String ATTRAZIONI_IMMAGINI_CHILD ="Immagini";
    protected static String ATTRAZIONI_COMMENTI_CHILD ="Commenti";

    protected static boolean isLanguageItalian(){
        return Locale.getDefault().getCountry().equals("IT");
    }
}
