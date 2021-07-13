package it.uniba.gruppo5.tourapp;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.cms.CmsHomeActivity;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;

//Classe base che gestisce delle funzionalità generali delle activity
public abstract class BaseActivity extends AppCompatActivity {

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    protected void setMenuAndDrawer(){

        Toolbar toolbar = findViewById(R.id.toolbar);

        if(toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

           /* navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cmsIntent = new Intent(BaseActivity.this, HomeActivity.class);
                    startActivity(cmsIntent);
                }
            });*/

            final View menuHeader = navigationView.getHeaderView(0);
            ImageView imageView = menuHeader.findViewById(R.id.imageView);
            final TextView profileName = menuHeader.findViewById(R.id.profileName);
            final ImageView imageProfile = menuHeader.findViewById(R.id.imageProfile);

            if(UserAuthenticationManager.IsUserAuthenticated(this)) {

                profileName.setVisibility(View.VISIBLE);
                imageProfile.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);

                imageProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BaseActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                });

                UtenteDAO.getSingleUtente(this, new ReadValueListener<UtenteDAO.Utente>() {
                            @Override
                            public void onDataRead(UtenteDAO.Utente result) {

                                profileName.setText(result.Nominativo);
                                result.immaginePrincipale.readFile(new ReadFileStorageListener() {
                                    @Override
                                    public void onFileRead(String absolutePath) {

                                        Glide.with(BaseActivity.this).load(new File(absolutePath)).into(imageProfile);
                                        imageProfile.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(BaseActivity.this , SettingsActivity.class);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                });
                            }
                        }, UserAuthenticationManager.getUserID(this));
            }
            else {

                profileName.setVisibility(View.GONE);
                imageProfile.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BaseActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                });
            }

            navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
            setMenuItemVisibility();

            //immagine che fa aprire il drawer
            ImageView imageDrawer = findViewById(R.id.imageDrawer);

            if(imageDrawer!=null) {
                imageDrawer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(drawerLayout!=null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMenuItemVisibility(){

        //nascondi elementi in base allo stato di autenticazione dell'utente
        Menu nav_Menu = navigationView.getMenu();

        if(UserAuthenticationManager.IsUserAuthenticated(this)){
            nav_Menu.findItem(R.id.nav_login).setVisible(false);
            nav_Menu.findItem(R.id.nav_signup).setVisible(false);

            //visibilità area privata solo è admin
            String tipoUtente = UserAuthenticationManager.getUserTipo(this);
            if(tipoUtente.equals(UtenteDAO.TIPO_UTENTE_ADMIN) ){
                nav_Menu.findItem(R.id.nav_cms).setVisible(true);
            }
            else {
                nav_Menu.findItem(R.id.nav_cms).setVisible(false);
            }

            //visibile solo se è un operatore
            nav_Menu.findItem(R.id.nav_operator).setVisible(tipoUtente.equals(UtenteDAO.TIPO_UTENTE_OPERATORE));

            nav_Menu.findItem(R.id.nav_settings).setVisible(true);
            nav_Menu.findItem(R.id.nav_logout).setVisible(true);
        }
        else{
            //non autenticato
            nav_Menu.findItem(R.id.nav_login).setVisible(true);
            nav_Menu.findItem(R.id.nav_signup).setVisible(true);
            nav_Menu.findItem(R.id.nav_cms).setVisible(false);
            nav_Menu.findItem(R.id.nav_settings).setVisible(false);
            nav_Menu.findItem(R.id.nav_logout).setVisible(false);
            nav_Menu.findItem(R.id.nav_operator).setVisible(false);
        }
    }

    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {


        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.


            int id = item.getItemId();


            if (id == R.id.nav_login) {
                Intent loginIntent = new Intent(BaseActivity.this, LoginActivity.class);
                startActivity(loginIntent);


            } else if (id == R.id.nav_signup) {

                Intent signupIntent = new Intent(BaseActivity.this, RegistrazioneActivity.class);
                startActivity(signupIntent);

            } else if (id == R.id.nav_logout) {

                UserAuthenticationManager.LogoutUser(BaseActivity.this);
                Intent homeIntent = new Intent(BaseActivity.this, HomeActivity.class);
                startActivity(homeIntent);

                Toast.makeText(BaseActivity.this,getResources().getString(R.string.logout_succ)
                        ,Toast.LENGTH_LONG).show();

            } else if (id == R.id.nav_settings) {

                Intent settingsIntent = new Intent(BaseActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);

            }else if (id == R.id.nav_operator) {

                Intent cmsIntent = new Intent(BaseActivity.this, UtilizzoCouponActivity.class);
                startActivity(cmsIntent);
            }
            else if (id == R.id.nav_cms) {

                Intent cmsIntent = new Intent(BaseActivity.this, CmsHomeActivity.class);
                startActivity(cmsIntent);
            }
            else if (id == R.id.nav_diari) {

                Intent cmsIntent = new Intent(BaseActivity.this, DiarioActivity.class);
                startActivity(cmsIntent);
            }else if(id==R.id.nav_coutpon){

                Intent cmsIntent = new Intent(BaseActivity.this, CouponActivity.class);
                startActivity(cmsIntent);
            }else if(id==R.id.nav_esplora){

                Intent cmsIntent = new Intent(BaseActivity.this, CategorieAttrazioniActivity.class);
                startActivity(cmsIntent);

            }else if(id==R.id.nav_location) {
                Intent intent = new Intent(BaseActivity.this, LocationActivity.class);
                startActivity(intent);
            }else if(id==R.id.nav_home){
                Intent intent = new Intent(BaseActivity.this, HomeActivity.class);
                startActivity(intent);
            }
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

    };

}
