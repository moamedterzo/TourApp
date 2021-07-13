package it.uniba.gruppo5.tourapp.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class DateHelper {
    //calcolo la data attuale

    public static String getCurrentDateString(){
        GregorianCalendar gc = new GregorianCalendar();
        int g =  gc.get(Calendar.DAY_OF_MONTH);
        int m =  gc.get(Calendar.MONTH)+1;
        int y =  gc.get(Calendar.YEAR) ;
        String dataAttuale = g+"/"+m+"/"+y;
        return dataAttuale;
    }

    public static Date getDateFromString(String date){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return format.parse(date);
        } catch (ParseException e) {
        }
        return null;
    }
}
