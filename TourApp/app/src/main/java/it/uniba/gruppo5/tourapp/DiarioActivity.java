package it.uniba.gruppo5.tourapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import it.uniba.gruppo5.tourapp.sqllite.DBLocaleDiario;
import it.uniba.gruppo5.tourapp.sqllite.DiarioAdapter;
import it.uniba.gruppo5.tourapp.sqllite.DiarioImmagine;
import it.uniba.gruppo5.tourapp.utilities.DateHelper;
import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

import static it.uniba.gruppo5.tourapp.SettingsActivity.REQUEST_IMAGE_CAPTURE;
import static it.uniba.gruppo5.tourapp.SettingsActivity.RESULT_LOAD_IMAGE;

/**
 * GESTIONE ERRORI
 * su logcat la parola chiava "diario_" dovrebbe riportare ai vari controlli fatti con Log.i
 */
public class DiarioActivity extends BaseActivity {

    ArrayList<String> nomiImmagini;
    RecyclerView recyclerView;
    Dialog dialog;

    DBLocaleDiario db_local;

    DiarioAdapter adapterImg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diario);
        setMenuAndDrawer();

        //apro database e ottengo immagini
        db_local = new DBLocaleDiario(this).open();
        ottieniImmaginiDaDB();

        // creo il RecycleView con il RV del layout acrivity_diario
        recyclerView = findViewById(R.id.image_gallery);
        setAdapterForRecyclerView();
        setAdapterListener();

        //Al clic del bottone chiedo in input la foto
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    popmenu();
                }

            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(db_local!= null)
            db_local.close();
    }



    private void pickPhotoFromGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
    }

    private void dispatchTakePictureIntent() {
        //Azione per aprire la raccolta di immagini
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //cattura l'intent
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //dopo che clicco sull'immagine devo salvarla solo nella directory opportuna e far ripartire l'onCreate

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            File fileImage = ImageSaver.saveBitmapImage(this, imageBitmap);

            //mostro immagine
            Bitmap bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());

            //salvataggio dell'immagine
            if (isStoragePermissionGranted()) {
                aggiungiImmagine(bitmap);
                Toast.makeText(this, getResources().getString(R.string.foto_ins), Toast.LENGTH_SHORT);
            }
            dialog.cancel();

        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pathFile = data.getData();
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pathFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            File fileImage = ImageSaver.saveBitmapImage(this, imageBitmap);

            //salvataggio memoria

            //mostro immagine
            Bitmap bitmap = BitmapFactory.decodeFile(fileImage.getAbsolutePath());

            if (isStoragePermissionGranted()) {
                aggiungiImmagine(bitmap);
                Toast.makeText(this, getResources().getString(R.string.foto_ins), Toast.LENGTH_SHORT);
            }

            dialog.cancel();
        }
    }

    //per aprire il menu e selezionare galleria o foto
    void popmenu() {

        dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.cameradialog);

        dialog.getWindow().setBackgroundDrawable(

                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView mCamerabtn = dialog.findViewById(R.id.cameradialogbtn);

        ImageView mGallerybtn = dialog.findViewById(R.id.gallerydialogbtn);

        TextView btnCancel = dialog.findViewById(R.id.canceldialogbtn);


        dialog.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT,

                ViewGroup.LayoutParams.FILL_PARENT);


        mCamerabtn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                //prendo la foto APPENA SCATTATA

                dispatchTakePictureIntent();
            }

        });


        mGallerybtn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                /**
                 * prendo la foto DALLA GALLERIA
                 */

                pickPhotoFromGallery();

            }

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                dialog.cancel(); // dismissing the popup

            }

        });

        dialog.show();
    }


    private void aggiungiImmagine(Bitmap finalBitmap) {

         // rappresenta il percorso completo dove sono le immagini che salviamo sulla memoria
        File myDir = new File(DBLocaleDiario.IMMAGINI_DIARIO_DIRECTORY); //istanzio il file per

        //crearlo eventualmente non ci sia
        myDir.mkdirs();

         //il nome con il quale l'immagine sarà salvata
         String fname = "Image_" + UUID.randomUUID().toString() + ".jpg";
         nomiImmagini.add(fname);

        // salva il nome nel DB locale
        salvaImmagineNelDB(fname);

        // salva l'immagine nella memoria
        try {
            File file = new File (myDir, fname);
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Log.i("Error",e.getMessage());
            e.printStackTrace();
        }

        setAdapterForRecyclerView();
        apriDettaglio(nomiImmagini.indexOf(fname));
    }

    private void ottieniImmaginiDaDB() {
        nomiImmagini = new ArrayList<>();
        nomiImmagini = db_local.getImmaginiArryList();
    }

    //Utilizzato per salvare i nomi delle immagini ( un nome alla volta )
    private void salvaImmagineNelDB(String fname) {
        String dataAttuale = DateHelper.getCurrentDateString();

        //inserisce le informazioni di un immagine nel database , i dettagli e il luogo inizialmente sono vuoto
        db_local.inserisciImmagine(fname, "", dataAttuale, "");
    }


    private void setAdapterForRecyclerView(){

        ArrayList<DiarioImmagine> listaImg = new ArrayList<>();

        if(nomiImmagini.size()>0) {
            //riempio immagini con tutti i nomi
            for(String s : nomiImmagini){
                listaImg.add(new DiarioImmagine(s));
            }
        }

        adapterImg = new DiarioAdapter(getApplicationContext(), listaImg);
        TextView txtEmpity = findViewById(R.id.empty_view);
        if(adapterImg.getItemCount()>0) {
            recyclerView.setAdapter(adapterImg);
            recyclerView.setVisibility(View.VISIBLE);
            txtEmpity.setVisibility(View.INVISIBLE);
        }else{
            recyclerView.setVisibility(View.INVISIBLE);
            txtEmpity.setVisibility(View.VISIBLE);
        }
    }

    private void setAdapterListener(){
        adapterImg.setOnItemClickListener(new DiarioAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //dettaglio diario
                Intent intent = new Intent(DiarioActivity.this, DettaglioDiarioActivity.class);
                intent.putExtra(DettaglioDiarioActivity.INTENT_PATH_IMAGE, nomiImmagini.get(position));
                startActivity(intent);
            }

            @Override
            public void onItemLonkClick(int position, View v) {

            }
        });
    }


    private void apriDettaglio(int position){
        Intent intent = new Intent(DiarioActivity.this , DettaglioDiarioActivity.class);
        intent.putExtra("percorsoImg",nomiImmagini.get(position));
        startActivity(intent);
    }



    //veririca i permessi per scrivere sulla memoria
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i("MSG1","Permission is granted");
                return true;
            } else {

                Log.i("MSG2","Permission is revoked");
                ActivityCompat.requestPermissions(DiarioActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("MSG3","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(DiarioActivity.this, getResources().getString(R.string. perm_g), Toast.LENGTH_SHORT).show();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(DiarioActivity.this, getResources().getString(R.string.perm_d), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case 2: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(DiarioActivity.this, getResources().getString(R.string. perm_g), Toast.LENGTH_SHORT).show();
                    popmenu();
                } else {
                    Toast.makeText(DiarioActivity.this, getResources().getString(R.string. perm_d), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onResume() {
        setAdapterForRecyclerView();
        super.onResume();
    }
}



