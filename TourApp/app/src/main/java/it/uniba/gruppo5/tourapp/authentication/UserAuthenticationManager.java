package it.uniba.gruppo5.tourapp.authentication;

import android.content.Context;
import android.content.SharedPreferences;

import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;

public final class UserAuthenticationManager {

    private static String UserAuthPreferences = "UAPrefs" ;
    private static String UsernamePreference = "Username";
    private static String TipoPreference = "Tipo";
    private static String IDPreference = "ID";

    public static void AuthenticateUser(final Context context, String username, String password, final AuthenticationListener listener){

        UtenteDAO.getUtenteFromCredentials(username, password, new ReadValueListener<UtenteDAO.Utente>() {

            @Override
            public void onDataRead(UtenteDAO.Utente result) {

                if(result != null){

                    //ok, salvo valori
                    SharedPreferences sharedPreferences = context.getSharedPreferences(UserAuthPreferences, Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(UsernamePreference, result.Email);
                    editor.putString(TipoPreference, result.Tipo);
                    editor.putString(IDPreference, result.ID);
                    editor.commit();

                    if(listener!= null)
                        listener.onUserAuthenticationRequestCompleted(true);
                }
                else if(listener!= null) {
                    //ko
                    listener.onUserAuthenticationRequestCompleted(false);
                }
            }
        });
    }

    public static String getUserID(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserAuthPreferences, Context.MODE_PRIVATE);

        return sharedPreferences.getString(IDPreference,null);
    }

    public static String getUserTipo(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserAuthPreferences, Context.MODE_PRIVATE);

        return sharedPreferences.getString(TipoPreference,null);
    }

    public static boolean IsUserAuthenticated(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserAuthPreferences, Context.MODE_PRIVATE);

        return sharedPreferences.getString(UsernamePreference, null) != null;
    }

    public static void LogoutUser(Context context){

        //Rimozione valori
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserAuthPreferences, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(UsernamePreference);
        editor.remove(TipoPreference);
        editor.remove(IDPreference);
        editor.commit();
    }

    public interface AuthenticationListener{

        void onUserAuthenticationRequestCompleted(boolean isUserAuthenticated);
    }
}
