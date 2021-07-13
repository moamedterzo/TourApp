package it.uniba.gruppo5.tourapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.firebase.CouponDAO;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.utilities.DateHelper;


public class CouponPreferitiActivity extends BaseActivity {

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String PREFS_COUPON_PREFERITI = "PreferitiCoupon";


    private RecyclerView recyclerView;


    //cambio utente e poi visualizzazzione crash pero poi dopo apertura vabene
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_preferiti);
        setMenuAndDrawer();

        recyclerView = findViewById(R.id.coupon_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.loadData();
    }

    public void loadData() {


        SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        Set<String> idsCouponSet = prefs.getStringSet(PREFS_COUPON_PREFERITI, null);

        if (idsCouponSet != null && idsCouponSet.size() > 0) {

            ArrayList<String> idsCouponArray = new ArrayList<>();
            idsCouponArray.addAll(idsCouponSet);

            //retrieving coupon by key
            CouponDAO.getCouponsPreferiti(idsCouponArray, new ReadValueListener<ArrayList<CouponDAO.Coupon>>() {
                @Override
                public void onDataRead(ArrayList<CouponDAO.Coupon> result) {

                    CouponAdapter couponAdapter = new CouponAdapter( result, itemSelectedListener);
                    TextView txtEmpity = findViewById(R.id.empty_view);
                    if(couponAdapter.getItemCount()>0) {
                        CouponPreferitiActivity.this.recyclerView.setAdapter(couponAdapter);
                        CouponPreferitiActivity.this.recyclerView.setVisibility(View.VISIBLE);
                        txtEmpity.setVisibility(View.INVISIBLE);
                    }else{
                        CouponPreferitiActivity.this.recyclerView.setVisibility(View.INVISIBLE);
                        txtEmpity.setVisibility(View.VISIBLE);
                    }

                }
            });
        }
    }


    CouponPreferitiActivity.CouponAdapter.OnItemClickListener itemSelectedListener = new CouponPreferitiActivity.CouponAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(CouponDAO.Coupon coupon) {
            Intent intent = new Intent(CouponPreferitiActivity.this, DettaglioCouponActivity.class);
            intent.putExtra("ID", coupon.ID);

            startActivity(intent);
        }

    };


    protected static class CouponViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView textViewTitle, textViewImporto, textViewRating;
        private String idUtente;
        private Date dataCorrente;

        public CouponViewHolder(View view) {
            super(view);

            image = itemView.findViewById(R.id.coupon_card);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewImporto = itemView.findViewById(R.id.textViewShortDesc);
            textViewRating = itemView.findViewById(R.id.textViewRating);

            dataCorrente =  Calendar.getInstance().getTime();
            idUtente = UserAuthenticationManager.getUserID(view.getContext());
        }

        public void bind(final CouponDAO.Coupon coupon, final CouponPreferitiActivity.CouponAdapter.OnItemClickListener listener) {

            textViewTitle.setText(coupon.getTitoloByLingua());
            textViewImporto.setText(coupon.getImportoLabel());

            if(DateHelper.getDateFromString(coupon.DataFineValidita).compareTo(dataCorrente) < 0
                    || DateHelper.getDateFromString(coupon.DataInizioValidita).compareTo(dataCorrente) > 0) {
                //scaduto
                textViewRating.setText(itemView.getContext().getString(R.string.coupon_scaduto_label));

            }
            else {
                //ancora utilizzabile, controllo se è stato già utilizzato
                CouponDAO.getCouponUtilizzo(coupon.ID, idUtente, new ReadValueListener<Boolean>() {
                    @Override
                    public void onDataRead(Boolean result) {

                        if (result) {
                            //utilizzato
                            textViewRating.setText(itemView.getContext().getString(R.string.coupon_utilizzato_label));
                        } else {
                            //non utilizzato
                            textViewRating.setText(coupon.DataFineValidita);

                        }
                    }
                });
            }

            coupon.immaginePrincipale = new FileStorageDAO(this.itemView.getContext(), coupon.FilePathImmagine);
            coupon.immaginePrincipale.readFile(new ReadFileStorageListener() {
                @Override
                public void onFileRead(String absolutePath) {

                    Glide.with(itemView).load(new File(absolutePath)).into(image);
                }
            });

            //solo se è possibile utilizzarlo importo il listener al click per andare nel dettaglio
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(coupon);
                }
            });
        }
    }

    protected static class CouponAdapter extends RecyclerView.Adapter<CouponPreferitiActivity.CouponViewHolder> {

        public interface OnItemClickListener {
            void onItemClick(CouponDAO.Coupon item);
        }

        private CouponPreferitiActivity.CouponAdapter.OnItemClickListener clickListener;
        private ArrayList<CouponDAO.Coupon> couponList;

        public CouponAdapter(ArrayList<CouponDAO.Coupon> couponList, CouponPreferitiActivity.CouponAdapter.OnItemClickListener listener) {
            this.couponList = couponList;
            this.clickListener = listener;
        }

        @NonNull
        @Override
        public CouponPreferitiActivity.CouponViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_coupon, viewGroup, false);
            return new CouponPreferitiActivity.CouponViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CouponPreferitiActivity.CouponViewHolder viewHolder, int i) {

            viewHolder.bind(couponList.get(i), clickListener);
        }

        @Override
        public int getItemCount() {
            return couponList.size();
        }
    }


}




