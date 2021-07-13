package it.uniba.gruppo5.tourapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.cms.CmsDatiGeneraliActivity;
import it.uniba.gruppo5.tourapp.firebase.CouponDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.utilities.DateHelper;


public class DettaglioCouponActivity extends BaseActivity {

    ImageView image_coupon, maps;
    TextView description, title, importo_coupon;
    boolean enabledStar;
    String idCoupon;
    Set<String> idsCouponPreferiti;
    SharedPreferences prefs;
    boolean isCouponUsable = true;


    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettaglio_coupon);
        setMenuAndDrawer();

        //binding controlli
        image_coupon = findViewById(R.id.image_coupon);
        description = findViewById(R.id.description_coupon);
        importo_coupon = findViewById(R.id.importo_coupon);
        title = findViewById(R.id.text_title);

        loadData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_coupon_preferito, menu);

        getPreferitiCoupon();

        //voce di menu visibile soltanto se l'utente è autenticato
        MenuItem menu_preferiti_aggiungi = menu.findItem(R.id.menu_aggiungi_preferiti);
        MenuItem menu_utilizza = menu.findItem(R.id.menu_utilizza);
        MenuItem menu_preferiti = menu.findItem(R.id.menu_preferiti);


        if (UserAuthenticationManager.IsUserAuthenticated(this)) {
            menu_preferiti.setVisible(true);
            menu_preferiti_aggiungi.setVisible(true);
            menu_utilizza.setVisible(isCouponUsable);

            //il base allo stato preferito, imposto l'icona
            if(enabledStar) {
                menu_preferiti_aggiungi.setIcon(R.drawable.ic_star_selected);
            }
            else{
                menu_preferiti_aggiungi.setIcon(R.drawable.star_blank);
            }
        } else {
            menu_preferiti.setVisible(false);
            menu_utilizza.setVisible(false);
            menu_preferiti_aggiungi.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_aggiungi_preferiti:

                aggiungiAiPreferiti();
                return true;

            case R.id.menu_utilizza:
                utilizzaCoupon();
                return true;

            case R.id.menu_preferiti:
                //si va nell'activity dei preferiti
                Intent intent= new Intent(DettaglioCouponActivity.this,CouponPreferitiActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {

        Intent intent = getIntent();
        idCoupon = intent.getStringExtra("ID");

        CouponDAO.getSingleCoupon(this, new ReadValueListener<CouponDAO.Coupon>() {
            @Override
            public void onDataRead(final CouponDAO.Coupon result) {

                title.setText(result.getTitoloByLingua());
                importo_coupon.setText(result.getImportoLabel());

                isCouponUsable = true;

                Date dataCorrente = Calendar.getInstance().getTime();
                if(DateHelper.getDateFromString(result.DataFineValidita).compareTo(dataCorrente) < 0
                        || DateHelper.getDateFromString(result.DataInizioValidita).compareTo(dataCorrente) > 0) {
                    //scaduto
                    description.setText(DettaglioCouponActivity.this.getString(R.string.coupon_scaduto_label));
                    isCouponUsable = false;
                    invalidateOptionsMenu();
                }
                else {
                    if (UserAuthenticationManager.IsUserAuthenticated(DettaglioCouponActivity.this)) {
                        //ancora utilizzabile, controllo se è stato già utilizzato
                        CouponDAO.getCouponUtilizzo(result.ID, UserAuthenticationManager.getUserID(DettaglioCouponActivity.this), new ReadValueListener<Boolean>() {
                            @Override
                            public void onDataRead(Boolean resultUtilizzo) {

                                if (resultUtilizzo) {
                                    //utilizzato
                                    description.setText(DettaglioCouponActivity.this.getString(R.string.coupon_utilizzato_label));
                                    isCouponUsable = false;
                                    invalidateOptionsMenu();
                                } else {
                                    //non utilizzato
                                    String periodoValidita = getString(R.string.coupon_validita_da);
                                    periodoValidita += " ";
                                    periodoValidita += result.DataInizioValidita;
                                    periodoValidita += " ";
                                    periodoValidita += getString(R.string.coupon_validita_a);
                                    periodoValidita += " ";
                                    periodoValidita += result.DataFineValidita;
                                    description.setText(periodoValidita);
                                }
                            }
                        });
                    }
                    else{
                        //non utilizzato
                        String periodoValidita = getString(R.string.coupon_validita_da);
                        periodoValidita += " ";
                        periodoValidita += result.DataInizioValidita;
                        periodoValidita += " ";
                        periodoValidita += getString(R.string.coupon_validita_a);
                        periodoValidita += " ";
                        periodoValidita += result.DataFineValidita;
                        description.setText(periodoValidita);
                    }
                }
                //lettura file
                result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                    @Override
                    public void onFileRead(String absolutePath) {


                        Glide.with(DettaglioCouponActivity.this).load(new File(absolutePath)).into(image_coupon);
                    }
                });
            }
        }, idCoupon);

    }


    private void getPreferitiCoupon() {

        prefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        idsCouponPreferiti = prefs.getStringSet(CouponPreferitiActivity.PREFS_COUPON_PREFERITI, null);

        if (idsCouponPreferiti != null) {
            enabledStar = idsCouponPreferiti.contains(idCoupon);
        }
        else{
            idsCouponPreferiti = new HashSet<>();
        }
    }


    private void aggiungiAiPreferiti(){

        enabledStar = !enabledStar;
        if (enabledStar) {

            //aggiungo in memoria il coupon
            idsCouponPreferiti.add(idCoupon);

            Toast.makeText(DettaglioCouponActivity.this, getString(R.string.coupon_aggiunto), Toast.LENGTH_SHORT).show();
        } else {

            //rimuovo in memoria il coupon
            idsCouponPreferiti.remove(idCoupon);

            Toast.makeText(DettaglioCouponActivity.this,  getString(R.string.coupon_rimosso), Toast.LENGTH_SHORT).show();
        }

        //aggiorno stato shared preferences
        SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(CouponPreferitiActivity.PREFS_COUPON_PREFERITI, idsCouponPreferiti);
        editor.apply();

        //viene aggiornato il menu, in quanto lo stato del preferito è cambiato
        invalidateOptionsMenu();
    }

    private void utilizzaCoupon() {
        //vai alla schermata di utilizzo
        Intent intent = new Intent(DettaglioCouponActivity.this, UtilizzoCouponActivity.class);
        String idUtente= UserAuthenticationManager.getUserID(DettaglioCouponActivity.this);

        //stringa idCoupon+idUtente da utilizzare
        intent.putExtra("ID_COUPON_ID_UTENTE", idCoupon + ";" +idUtente);
        startActivity(intent);
    }
}