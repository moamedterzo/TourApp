package it.uniba.gruppo5.tourapp.cms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import it.uniba.gruppo5.tourapp.HomeActivity;
import it.uniba.gruppo5.tourapp.R;

public class CmsHomeActivity extends CmsBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cms_home);
        setMenuAndDrawer();
    }


    public void clickUser (View view){

        final Intent userActivity = new Intent(this, CmsUtentiActivity.class);
        startActivity(userActivity);

    }

    public void clickAttractions (View view){

        final Intent attractionsActivity = new Intent(this, CmsAttrazioniActivity.class);
        startActivity(attractionsActivity);

    }

    public void clickCoupon (View view){

        final Intent couponActivity = new Intent(this, CmsCouponActivity.class);
        startActivity(couponActivity);

    }

    public void clickPublic (View view){

        final Intent publicActivity = new Intent(this, HomeActivity.class);
        startActivity(publicActivity);

    }

}
