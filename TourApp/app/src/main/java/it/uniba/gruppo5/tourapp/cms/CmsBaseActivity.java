package it.uniba.gruppo5.tourapp.cms;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import it.uniba.gruppo5.tourapp.HomeActivity;
import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;

public abstract class CmsBaseActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //redirect alla pagina home pubblica, se l'utente non è un admin autenticato
        if(!UserAuthenticationManager.IsUserAuthenticated(this)
                || !UserAuthenticationManager.getUserTipo(this).equals(UtenteDAO.TIPO_UTENTE_ADMIN)) {

            Intent homeIntent = new Intent(this,HomeActivity.class);
            startActivity(homeIntent);
        }
    }

    //E' richiesto un controllo di tipo drawerlayout, e uno di tipo navigationview
    protected void setMenuAndDrawer(){

        Toolbar toolbar = findViewById(R.id.toolbar);

        if(toolbar != null) {
            setSupportActionBar(toolbar);

            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);
        }

        drawerLayout = findViewById(R.id.drawer_layout_cms);

        NavigationView navigationView = findViewById(R.id.nav_view_cms);
        if (navigationView != null) {

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cmsIntent = new Intent(CmsBaseActivity.this, CmsHomeActivity.class);
                    startActivity(cmsIntent);
                }
            });

            navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
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

    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {


        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.


            int id = item.getItemId();


            if (id == R.id.nav_utenti) {
                Intent intent = new Intent(CmsBaseActivity.this, CmsUtentiActivity.class);
                startActivity(intent);


            } else if (id == R.id.nav_dati_generali) {

                Intent intent = new Intent(CmsBaseActivity.this, CmsDatiGeneraliActivity.class);
                startActivity(intent);

            } else if (id == R.id.nav_logout) {

                UserAuthenticationManager.LogoutUser(CmsBaseActivity.this);

                Intent intent = new Intent(CmsBaseActivity.this, HomeActivity.class);
                startActivity(intent);

                //chiudo questa activity
                finish();

            } else if (id == R.id.nav_area_pubblica) {

                Intent intent = new Intent(CmsBaseActivity.this, HomeActivity.class);
                startActivity(intent);

            }
            else if (id == R.id.nav_cat_coupon) {
                Intent intent = new Intent(CmsBaseActivity.this, CmsCategorieCouponActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_coupon) {
                Intent intent = new Intent(CmsBaseActivity.this, CmsCouponActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_cat_attrazioni) {
                Intent intent = new Intent(CmsBaseActivity.this, CmsCategorieAttrazioneActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_attrazioni) {
                Intent intent = new Intent(CmsBaseActivity.this, CmsAttrazioniActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.nav_tag_attrazioni) {
                Intent intent = new Intent(CmsBaseActivity.this, CmsTagAttrazioneActivity.class);
                startActivity(intent);
            }

            DrawerLayout drawer = findViewById(R.id.drawer_layout_cms);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

    };
}
