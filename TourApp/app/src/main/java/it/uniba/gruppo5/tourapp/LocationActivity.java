package it.uniba.gruppo5.tourapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.cms.CmsTagAttrazioneDetailActivity;
import it.uniba.gruppo5.tourapp.firebase.AttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.CategoriaAttrazioneDAO;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;


public class LocationActivity extends BaseActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    GoogleMap mMap;
    LocationManager locationManager;


    ArrayList<LatLng> posizioni;
    ArrayList<AttrazioneDAO.Attrazione> attrazioni;
    private static ArrayList<String> nomi_attr;
    Marker[] mark_posizioni;


    private static final int MY_PERMISSIONS_REQUEST_POSITION = 101;
    private static String filterCategoria;
    private static String filterDescrizione;

    private RecyclerView categorieRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setMenuAndDrawer();


        categorieRecyclerView = findViewById(R.id.rec_view_categorie);
        //divisore degli elementi
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(LocationActivity.this, LinearLayoutManager.HORIZONTAL, false);
        categorieRecyclerView.setLayoutManager(horizontalLayoutManager);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        this.loadCategorieList();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mMap!=null&&locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(),"GPS Attivo",Toast.LENGTH_SHORT).show();

            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
            mMap.moveCamera( CameraUpdateFactory.newLatLng(new LatLng(41.1258,16.8666)) );

        }
    }



        /*
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }
        */

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_POSITION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                    } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                      Toast.makeText(getApplicationContext(),"GPS Attivo",Toast.LENGTH_SHORT).show();

                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(this);
                        mMap.setOnMyLocationClickListener(this);
                        mMap.moveCamera( CameraUpdateFactory.newLatLng(new LatLng(41.1258,16.8666)) );

                    }
                    } else {

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera( CameraUpdateFactory.newLatLng(new LatLng(41.1258,16.8666)) );

        if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_POSITION);
            return;
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getApplicationContext(),"GPS Attivo",Toast.LENGTH_SHORT).show();

                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMyLocationClickListener(this);
                mMap.moveCamera( CameraUpdateFactory.newLatLng(new LatLng(41.1258,16.8666)) );
            }


       /* // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */

        }
    }



    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        // (the camera animates to the user's current position).
        return false;
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(LocationActivity.this, R.style.myDialog));
        builder.setMessage(R.string.dialog_gps)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void loadCategorieList() {

        CategoriaAttrazioneDAO.getCategorie(null, new ReadValueListener<ArrayList<CategoriaAttrazioneDAO.Categoria>>() {
            @Override
            public void onDataRead(ArrayList<CategoriaAttrazioneDAO.Categoria> result) {

                LocationActivity.CategoriaAdapter categoriaAdapter = new LocationActivity.CategoriaAdapter(result, itemSelectedListenerCategoria);
                LocationActivity.this.categorieRecyclerView.setAdapter(categoriaAdapter);
            }
        });
    }


    private void loadAttrazioniList() {

        posizioni= new ArrayList<>();
        attrazioni=new ArrayList<>();

        AttrazioneDAO.getAttrazioni(filterDescrizione,filterCategoria, new ReadValueListener<ArrayList<AttrazioneDAO.Attrazione>>() {
            @Override
            public void onDataRead(ArrayList<AttrazioneDAO.Attrazione> result) {


                attrazioni.addAll(result);
                mark_posizioni = new Marker[attrazioni.size()];

                for (int i = 0; i < attrazioni.size(); i++) {


                    if (filterCategoria != null) {

                        if (filterCategoria.equals("-LXym4Gsis6RXGKTWwpK")) {

                            mark_posizioni[i] = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(attrazioni.get(i).Latitudine, attrazioni.get(i).Longitudine)).icon(BitmapDescriptorFactory.fromResource(R.drawable.burger))
                                    .title(attrazioni.get(i).Titolo));
                            mark_posizioni[i].setTag(0);


                        } else if (filterCategoria.equals("-LXynl-fuevlOq-RBuJn")) {
                            mark_posizioni[i] = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(attrazioni.get(i).Latitudine, attrazioni.get(i).Longitudine)).icon(BitmapDescriptorFactory.fromResource(R.drawable.monument))
                                    .title(attrazioni.get(i).Titolo));
                            mark_posizioni[i].setTag(0);

                        } else if (filterCategoria.equals("-LYIruc5w7WK9PO9Puvf")) {
                            mark_posizioni[i] = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(attrazioni.get(i).Latitudine, attrazioni.get(i).Longitudine)).icon(BitmapDescriptorFactory.fromResource(R.drawable.lodging))
                                    .title(attrazioni.get(i).Titolo));
                            mark_posizioni[i].setTag(0);

                        } else {
                            mark_posizioni[i] = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(attrazioni.get(i).Latitudine, attrazioni.get(i).Longitudine)).icon(BitmapDescriptorFactory.fromResource(R.drawable.dance))
                                    .title(attrazioni.get(i).Titolo));
                            mark_posizioni[i].setTag(0);

                        }
                    } else {
                        mMap.clear();
                    }
                }
            }
        });
    }

    //listener utilizzato per il click del coupon
    CategoriaAdapter.OnItemClickListener itemSelectedListenerCategoria = new CategoriaAdapter.OnItemClickListener() {

        @Override
        public boolean onItemClick(CategoriaAttrazioneDAO.Categoria categoria) {

            boolean isItemSelected = true;
            if(categoria.ID.equals(filterCategoria)){
                //vuol dire che è stato cliccato nuovamente lo stesso elemento, allora deseleziono
                filterCategoria = null;
                isItemSelected = false;
            }
            else {
                if(filterCategoria != null && !filterCategoria.isEmpty()){
                    //sto selezionando un nuovo elemento, effettuo nuovamente il binding degli elementi per deselezionare lo stato
                    LocationActivity.this.loadCategorieList();
                }

                filterCategoria = categoria.ID;
            }

            LocationActivity.this.loadAttrazioniList();
            posizioni.clear();
            attrazioni.clear();
            mMap.clear();


            return isItemSelected;
        }


    };

    protected final static class CategoriaViewHolder extends RecyclerView.ViewHolder{

        private TextView mCategoriaTitolo;
        private ImageView mImage;

        public CategoriaViewHolder(View itemView){

            super(itemView);

            mCategoriaTitolo = itemView.findViewById(R.id.label);
            mImage = itemView.findViewById(R.id.image);
        }

        public void bind(final CategoriaAttrazioneDAO.Categoria categoria,
                         final CategoriaAdapter.OnItemClickListener listener){

            mCategoriaTitolo.setText(categoria.getTitoloByLingua());

            categoria.immaginePrincipale = new FileStorageDAO(this.itemView.getContext(), categoria.FilePathImmagine);
            categoria.immaginePrincipale.readFile(new ReadFileStorageListener() {
                @Override
                public void onFileRead(String absolutePath) {
                    Glide.with(itemView).load(new File(absolutePath)).into(mImage);
                }
            });


            //controllo se l'item corrente sia stato selezionato dall'utente
            LocationActivity gps_activity = (LocationActivity) itemView.getContext();
            if(categoria.ID.equals(LocationActivity.filterCategoria)){
                //selected
                itemView.setBackground(itemView.getContext().getDrawable(R.drawable.shape_selected));
                mCategoriaTitolo.setTextColor(R.color.colorBackgraound);
            }


            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (listener != null) {
                        if(listener.onItemClick(categoria)){

                            itemView.setBackground(itemView.getContext().getDrawable(R.drawable.shape_selected));
                            mCategoriaTitolo.setTextColor(R.color.colorBackgraound);
                        }
                        else{
                            itemView.setBackground(itemView.getContext().getDrawable(R.color.colorBackgraound));
                            mCategoriaTitolo.setTextColor(R.color.colorGiall);
                        }
                    }

                }
            });
        }
    }

    protected final static class CategoriaAdapter extends RecyclerView.Adapter<LocationActivity.CategoriaViewHolder>{

        private final List<CategoriaAttrazioneDAO.Categoria> mModel;

        CategoriaAdapter(final List<CategoriaAttrazioneDAO.Categoria> model, OnItemClickListener listener){
            mModel = model;
            mListener = listener;
        }

        @Override
        public LocationActivity.CategoriaViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            final View layout = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.listitem_horizontal,viewGroup,false);

            return new LocationActivity.CategoriaViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(LocationActivity.CategoriaViewHolder categoriaViewHolder, int position){
            categoriaViewHolder.bind(mModel.get(position), mListener);
        }

        @Override
        public int getItemCount(){
            return mModel.size();
        }

        public interface OnItemClickListener {

            boolean onItemClick(CategoriaAttrazioneDAO.Categoria categoria);
        }

        private OnItemClickListener mListener;
    }

}
