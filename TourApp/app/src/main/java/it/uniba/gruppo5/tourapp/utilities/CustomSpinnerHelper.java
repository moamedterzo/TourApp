package it.uniba.gruppo5.tourapp.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import it.uniba.gruppo5.tourapp.R;

public class CustomSpinnerHelper {


    Spinner mSpinner;

    String[] spinnerTexts;
    Object[] spinnerValues;

    public void setSpinnerAndValues(@NonNull Context context, Spinner spinner, List<Pair<String,String>> spinnerTextValues) {

       setOnlyValues(spinnerTextValues);
       setOnlySpinner(context,spinner);
    }

    //Utilizzato quando non si possiede ancora il riferimento dello spinner
    public void setOnlyValues(List<Pair<String,String>> spinnerTextValues){

        spinnerTexts = new String[spinnerTextValues.size()];
        spinnerValues = new Object[spinnerTextValues.size()];

        int i = 0;
        //ottengo array testi e valori
        for (Pair<String, String> item : spinnerTextValues) {
            spinnerTexts[i] = item.first;
            spinnerValues[i] = item.second;

            i++;
        }
    }

    public void setOnlySpinner(@NonNull Context context, Spinner spinner) {

        if (spinner != null) {
            mSpinner = spinner;

            //binding elementi
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerTexts);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
        }
    }

    public Object getSpinnerSelectedValue(){
        if(mSpinner == null)
            return null;
        else
            return spinnerValues[mSpinner.getSelectedItemPosition()];
    }

    public String getTextFromValue(Object value){
        if (value != null && spinnerValues != null) {
            int i = 0;
            for (Object spinnerValue : spinnerValues) {
                if (value.equals(spinnerValue)) {
                   return spinnerTexts[i];
                }
                i++;
            }
        }
        return null;
    }

    public Object getSpinnerSelectedText(){
        if(mSpinner == null)
            return null;
        else
            return mSpinner.getSelectedItem().toString();
    }

    public void setSpinnerSelectedValue(Object value){

        if(mSpinner!=null) {
            if (value != null) {
                int i = 0;
                for (Object spinnerValue : spinnerValues) {
                    if (value.equals(spinnerValue)) {
                        mSpinner.setSelection(i);
                        break;
                    }
                    i++;
                }
            }
        }
    }

    public boolean validateRequiredValue(@NonNull Context context){

        View selectedView = mSpinner.getSelectedView();
        if (selectedView != null && selectedView instanceof TextView) {
            TextView selectedTextView = (TextView) selectedView;
            if (this.getSpinnerSelectedValue().equals("")) {
                String messageError = context.getString(R.string.errore_campo_obbligatorio);
                selectedTextView.setError(messageError);
                return false;
            }
        }
        return true;
    }



}
