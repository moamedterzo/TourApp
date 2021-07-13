package it.uniba.gruppo5.tourapp.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
implements DatePickerDialog.OnDateSetListener {

    public final static String FORMAT_EDIT_TEXT = "%02d/%02d/%04d";

    public interface OnDateSelectedListener
    {
        void onDateSelected(int year, int month, int day, String tag);
    }


    private WeakReference<OnDateSelectedListener> mOnDateSelectedListener;
    private int mYear;
    private int mMonth;
    private int mDay;

    public  DatePickerFragment(){

    }

    public DatePickerFragment(String textDate){

        Date date = null;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
            date = parser.parse(textDate);
        }
        catch (ParseException e){
            //procedo avanti
        }

        //valori di default
        final Calendar c = Calendar.getInstance();
        if(date != null)
        {
            c.setTime(date);
        }

        this.mYear = c.get(Calendar.YEAR);
        this.mMonth = c.get(Calendar.MONTH);
        this.mDay = c.get(Calendar.DAY_OF_MONTH);

    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if(context instanceof OnDateSelectedListener){
            this.mOnDateSelectedListener = new WeakReference<>((OnDateSelectedListener)context);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        return new DatePickerDialog(getActivity(),this,mYear,mMonth,mDay);
    }

    public void onDateSet(DatePicker view, int year,int month,int day){

        if(this.mOnDateSelectedListener!= null){

            //il mese deve partire da 1, no da 0
            this.mOnDateSelectedListener.get().onDateSelected(year,month + 1 ,day, getTag());
        }
    }
}
