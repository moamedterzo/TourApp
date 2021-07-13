package it.uniba.gruppo5.tourapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.firebase.AttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.TagAttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;
import it.uniba.gruppo5.tourapp.utilities.DateHelper;
import it.uniba.gruppo5.tourapp.utilities.HorizontalImageAdapter;


public class DettaglioAttrazioneActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Dialog dialog;
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE=1;
    Intent call;


    private RecyclerView imgRecyclerView;
    private HorizontalImageAdapter imgAdapter;

    private RecyclerView recensioniRecyclerView;
    CommentoAttrazioneAdapter commentoAttrazioneAdapter;

    double cordX , cordY ;
    String luogo;
    String numTelefono;
    String e_mail;
    String sito;

    TextView txtDescrizione;
    TextView txtOrariMatt;
    TextView txtLvlCosto;
    TextView ins_recensione;
    RatingBar ratingBar;
    EditText recensioni;

    String idAttrazioni;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettaglio_attrazione);
        setMenuAndDrawer();

        Button  esplora= findViewById(R.id.btn_explora);
        esplora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DettaglioAttrazioneActivity.this, CategorieAttrazioniActivity.class);
                startActivity(intent);
            }
        });
        ins_recensione= findViewById(R.id.txt_recensione);
        ratingBar = findViewById(R.id.RatingBar); // avvia una barra di valutazione
        recensioni = findViewById(R.id.recensioni);
        final Button salva = findViewById(R.id.salva_rec);

        boolean isUserAuthenticated = UserAuthenticationManager.IsUserAuthenticated(this);
        if(isUserAuthenticated){
            ins_recensione.setVisibility(View.VISIBLE);
            ratingBar.setVisibility(View.VISIBLE);
        }
        else{
            ratingBar.setVisibility(View.GONE);
            ins_recensione.setVisibility(View.GONE);
        }

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override    // ottiene il numero di valutazione da una barra di valutazione
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                // avvia l'edit text delle recensioni
                recensioni.setVisibility(View.VISIBLE);
                salva.setVisibility(View.VISIBLE);
                recensioni.requestFocus();
            }
        });

        salva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    //salvataggio recensione
                AttrazioneDAO.CommentoAttrazione commentoAttrazione = new AttrazioneDAO.CommentoAttrazione();
                commentoAttrazione.Data = DateHelper.getCurrentDateString();
                commentoAttrazione.IDUtente = UserAuthenticationManager.getUserID(DettaglioAttrazioneActivity.this);
                commentoAttrazione.Rating = ratingBar.getRating();
                commentoAttrazione.Testo = recensioni.getText().toString();
                
                AttrazioneDAO.addCommentoAttrazione(commentoAttrazione,idAttrazioni,DettaglioAttrazioneActivity.this);
                commentoAttrazioneAdapter.mModel.add(0, commentoAttrazione);
                commentoAttrazioneAdapter.notifyItemInserted(0);

                //resetto stato view
                ratingBar.setEnabled(false);
                recensioni.setEnabled(false);
                salva.setEnabled(false);
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Orari
        txtOrariMatt = findViewById(R.id.txtOrariMattina);

        //Descrizione
        txtDescrizione = findViewById(R.id.txtDescrizione);
        txtLvlCosto = findViewById(R.id.txtCostosità);


        //TextView del dialog
        TextView txtContatti = findViewById(R.id.txtContatti);
        txtContatti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popmenu();
            }
        });

        loadDati();
        loadRecensioni();
    }


    private void loadDati() {

        Intent intent = getIntent();
        idAttrazioni = intent.getStringExtra("ID");
        AttrazioneDAO.getSingleAttrazione(this, new ReadValueListener<AttrazioneDAO.Attrazione>() {
            @Override
            public void onDataRead(AttrazioneDAO.Attrazione result) {

                getSupportActionBar().setTitle(result.getTitoloByLingua().toUpperCase());
                txtDescrizione.setText(result.getDescrizioneByLingua());
                txtOrariMatt.setText(result.Orario);
                e_mail = result.Email;
                numTelefono ="tel:"+ result.Telefono;
                sito = result.Sitoweb;

                cordX = result.Latitudine;
                cordY = result.Longitudine;
                luogo = result.getTitoloByLingua();


                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(cordX, cordY);

                mMap.addMarker(new MarkerOptions().position(sydney).title(luogo));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


                if(result.Prezzo != null && !result.Prezzo.isEmpty()) {
                    int lvlDiCosto = Integer.parseInt(result.Prezzo);
                    try {
                        String lvlDiCostoCalcolato = lvlCosto(lvlDiCosto);
                        txtLvlCosto.setText(lvlDiCostoCalcolato);

                    } catch (IllegalArgumentException e) {
                        e.getMessage();
                        txtLvlCosto.setText("");
                    }
                }

                //tags
                loadTagList(result);
                setAdapterImages(result.Immagini);
            }
        }, idAttrazioni);
    }
    
    private void loadRecensioni(){
        AttrazioneDAO.readCommentiAttrazione(idAttrazioni, new ReadValueListener<ArrayList<AttrazioneDAO.CommentoAttrazione>>() {
            @Override
            public void onDataRead(ArrayList<AttrazioneDAO.CommentoAttrazione> result) {

                recensioniRecyclerView = findViewById(R.id.recview_recensioni);

                recensioniRecyclerView.setLayoutManager(new LinearLayoutManager(DettaglioAttrazioneActivity.this));

                commentoAttrazioneAdapter = new CommentoAttrazioneAdapter(result, null);
                recensioniRecyclerView.setAdapter(commentoAttrazioneAdapter);

            }
        });
    }

    private void loadTagList(AttrazioneDAO.Attrazione attrazione) {

        AttrazioneDAO.readTagAttrazioni(attrazione, new ReadValueListener<ArrayList<TagAttrazioneDAO.Tag>>() {
            @Override
            public void onDataRead(ArrayList<TagAttrazioneDAO.Tag> result) {

                setAdapterTags(result);
            }
        });
    }
    private void setAdapterTags(ArrayList<TagAttrazioneDAO.Tag> tags){

    }



    private void setAdapterImages(ArrayList<String> images){
        imgRecyclerView = findViewById(R.id.recview_Img);
        // add a divider after each item for more clarity
        imgRecyclerView.addItemDecoration(new DividerItemDecoration(DettaglioAttrazioneActivity.this, LinearLayoutManager.HORIZONTAL));
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(DettaglioAttrazioneActivity.this, LinearLayoutManager.HORIZONTAL, false);
        imgRecyclerView.setLayoutManager(horizontalLayoutManager);

        imgAdapter = new HorizontalImageAdapter(images, getApplicationContext());
        imgRecyclerView.setAdapter(imgAdapter);
    }


    public String lvlCosto(int lvl){ //lvl -> livello di costo , numero da 1 a 5;
        StringBuilder s ;
        if(lvl<=0)
            throw new IllegalArgumentException("il livello del costo non può essere minore o uguale a 0");

            s = new StringBuilder();
            for (int i = 0; i < lvl; i++) {
                s.append("€");
            }
            return s.toString();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    void popmenu() {

        dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.dialog_contatti);

        dialog.getWindow().setBackgroundDrawable(

                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView mPhone = dialog.findViewById(R.id.txtPhone);

        TextView mEmail = dialog.findViewById(R.id.txtEmail);

        TextView mWeb = dialog.findViewById(R.id.txtWeb);

        TextView mAnnulla = dialog.findViewById(R.id.txtAnnulla);

        mPhone.setText(numTelefono);
        mEmail.setText(e_mail);
        mWeb.setText(sito);


        dialog.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT,

                ViewGroup.LayoutParams.FILL_PARENT);


        mPhone.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                call = new Intent(Intent.ACTION_CALL , Uri.parse(numTelefono) );
                //call.setData(Uri.parse("tel:(+39)3451173456"));
                // startActivity(call);

                if (ContextCompat.checkSelfPermission(DettaglioAttrazioneActivity.this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(DettaglioAttrazioneActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);

                } else {
                    //You already have permission
                    try {
                        startActivity(call);
                    } catch(SecurityException e) {
                        e.printStackTrace();
                    }
                }

            }

        });

        mEmail.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Intent intenzione = new Intent();
                intenzione.setAction(Intent.ACTION_SEND);
                intenzione.putExtra(Intent.EXTRA_EMAIL,  new String[]{e_mail});
                intenzione.setType("message/rfc822");
                startActivity(intenzione);



            }

        });

        mWeb.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",Uri.parse(sito));
                startActivity(intent);

            }

        });

        mAnnulla.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                dialog.cancel(); // dismissing the popup

            }

        });

        dialog.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //se i permessi sono stati confermati allora chiama (LA PRIMA VOLTA PER NON RICLICCARE ), controllo essenziale per effettare la chiamata , senza , la chiamata non avviene
                    if (ContextCompat.checkSelfPermission(DettaglioAttrazioneActivity.this,
                            Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED) {
                        startActivity(call);
                        Toast.makeText(this, "Accesso a chiamare confermato",
                                Toast.LENGTH_SHORT).show();
                        // permission was granted, yay! Do the phone call}
                    }
                } else {
                    Toast.makeText(this ,"Accesso a chiamare NON confermato" , Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }

        }

    }

    protected final static class CommentoAttrazioneViewHolder extends RecyclerView.ViewHolder{

        private TextView mCommento;
        private TextView mData;
        private TextView mNomeUtente;
        private ImageView mImage;
        private RatingBar ratingBar;

        public CommentoAttrazioneViewHolder(View itemView){

            super(itemView);

            mCommento = itemView.findViewById(R.id.txtCommento);
            mImage = itemView.findViewById(R.id.imgUtente);
            mData = itemView.findViewById(R.id.txtData);
            mNomeUtente = itemView.findViewById(R.id.txtNomeUtente);
            ratingBar = itemView.findViewById(R.id.rating);
        }

        public void bind(final AttrazioneDAO.CommentoAttrazione commentoAttrazione,
                         final DettaglioAttrazioneActivity.CommentoAttrazioneAdapter.OnItemSelectedListener listener){

            mCommento.setText(commentoAttrazione.Testo);
            mData.setText(commentoAttrazione.Data);
            ratingBar.setNumStars(5);
            ratingBar.setRating(commentoAttrazione.Rating);
            ratingBar.setEnabled(false);

            UtenteDAO.getSingleUtente(itemView.getContext(), new ReadValueListener<UtenteDAO.Utente>() {
                @Override
                public void onDataRead(UtenteDAO.Utente result) {
                    
                    mNomeUtente.setText(result.Nominativo);
                    result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                        @Override
                        public void onFileRead(String absolutePath) {

                            Glide.with(itemView).load(new File(absolutePath)).into(mImage);
                        }
                    });
                }
            }, commentoAttrazione.IDUtente);

        }
    }

    protected final static class CommentoAttrazioneAdapter extends RecyclerView.Adapter<DettaglioAttrazioneActivity.CommentoAttrazioneViewHolder>{

        private final List<AttrazioneDAO.CommentoAttrazione> mModel;

        CommentoAttrazioneAdapter(final ArrayList<AttrazioneDAO.CommentoAttrazione> model, DettaglioAttrazioneActivity.CommentoAttrazioneAdapter.OnItemSelectedListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public DettaglioAttrazioneActivity.CommentoAttrazioneViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_commento_attrazioni,viewGroup,false);

            return new DettaglioAttrazioneActivity.CommentoAttrazioneViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(DettaglioAttrazioneActivity.CommentoAttrazioneViewHolder commentoAttrazioneViewHolder, int position){
            commentoAttrazioneViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemSelectedListener {

            boolean onItemSelected(AttrazioneDAO.CommentoAttrazione commentoAttrazione);
        }

        private DettaglioAttrazioneActivity.CommentoAttrazioneAdapter.OnItemSelectedListener mListener;
    }

}






