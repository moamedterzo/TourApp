package it.uniba.gruppo5.tourapp.sqllite;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.util.ArrayList;

/**
 * GESTIONE ERRORI:
 * Inserire il nome ErrorDB all'interno di Logcat
 */

public class DBLocaleDiario {


    //il percorso completo della cartella dove sono le immagini
    public static final String IMMAGINI_DIARIO_DIRECTORY =  Environment.getExternalStorageDirectory().toString() +"/images_diario";

    static final String NOME_DB_SD = "db_local"; // nome del database all'interno del dispositivo

    //nome della tabella per le immagini
    static final String TB_IMG = "ImmaginiDiario";

    //colonne per la tabella IMMAGINI_DIARIO
    public static final String KEY_ID = "ID"; //l'id dell'immagine
    public static final String KEY_DATA = "Data"; //la data della immagine
    public static final String KEY_PATH_IMMAGINE = "Nome"; //il nome dell'immagine
    public static final String KEY_DETTAGLI = "Dettagli"; //dettagli dell'immagine
    public static final String KEY_LUOGO = "Luogo";


    //versione del database , se devo aggiungere in seguito una tabella devo cambiare la versione del database
    static final int DATABASE_VERSION = 2;

    //creazione della tabella TB_IMMAGINI
    static final String CREA_TB_IMG = "CREATE TABLE IF NOT EXISTS " + TB_IMG + " (" + KEY_ID + " integer primary key autoincrement," +
            KEY_PATH_IMMAGINE + " text not null," + KEY_DETTAGLI + " text," + KEY_DATA + " text," + KEY_LUOGO + " text);";

    final Context mContext;
    DatabaseHelper DBHelper;
    SQLiteDatabase db_gestion;

    public DBLocaleDiario(Context context) {
        this.mContext = context;
        DBHelper = new DatabaseHelper(context);
    }

    /**
     * Crea o apre il database per la lettura e la scrittura.
     *
     * @return
     * @throws SQLException
     */
    public DBLocaleDiario open() throws SQLException {
        db_gestion = DBHelper.getWritableDatabase();
        return this;
    }

    /**
     * Chiude il database , da chiamare ogni volta che si finisce di scrivere sul database
     */
    public void close() {
        DBHelper.close();
    }

    /**
     * @return la tabella ImmaginiDiario
     */
    public Cursor getImmaginiDiario() {
        return db_gestion.query(TB_IMG, new String[]{KEY_ID, KEY_PATH_IMMAGINE, KEY_DETTAGLI, KEY_DATA, KEY_LUOGO}, null, null, null, null, null);

    }

    public long inserisciImmagine(String nomeImmagine, String dettagli, String data, String luogo) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PATH_IMMAGINE, nomeImmagine);
        initialValues.put(KEY_DETTAGLI, dettagli);
        initialValues.put(KEY_DATA, data);
        initialValues.put(KEY_LUOGO, luogo);

        return db_gestion.insert(TB_IMG, null, initialValues);

    }

    public boolean modificaImmagine(String nomeImmagine, String dettagli, String luogo) {
        Cursor cursor = restituisciRiga(nomeImmagine);
        ContentValues initialValues = new ContentValues();
        int id;
        cursor.moveToFirst();
        id = cursor.getInt(0);
        initialValues.put(KEY_ID, id);
        initialValues.put(KEY_PATH_IMMAGINE, nomeImmagine);
        initialValues.put(KEY_DETTAGLI, dettagli);
        initialValues.put(KEY_DATA, cursor.getString(3));       //" like '%"+id+"%'"
        initialValues.put(KEY_LUOGO, luogo);
        return db_gestion.update(TB_IMG, initialValues, KEY_ID + "=" + id, null) > 0;

    }


    public Cursor restituisciRiga(String nomeImmagine) {

        return db_gestion.query(true, TB_IMG, new String[]{KEY_ID, KEY_PATH_IMMAGINE, KEY_DETTAGLI, KEY_DATA, KEY_LUOGO}, KEY_PATH_IMMAGINE + " like '%" + nomeImmagine + "%'", null, null, null, null, null);

    }

    public boolean eliminaImmagine(String nomeImmagine) {
        return db_gestion.delete(TB_IMG, KEY_PATH_IMMAGINE + "=?", new String[]{nomeImmagine}) > 0;
    }

    public ArrayList<String> getImmaginiArryList() {
        ArrayList<String> nomiImg = new ArrayList<>();
        Cursor c = getImmaginiDiario();
        if (c.moveToFirst()) {
            do {
                nomiImg.add(c.getString(1));
            } while (c.moveToNext());
        } else {
            Log.i("Error_returnArray", "Nessun elemento nella tabella per caricare l'array");
        }

        return nomiImg;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, NOME_DB_SD, null, DATABASE_VERSION);
        }

        /**
         * si occupa di eseguire le query
         *
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREA_TB_IMG);
            } catch (SQLException e) {
                Log.i("ErrorDB_1", "Errore nell' onCreate , tutte o alcune query non sono state eseguite ");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DatabaseHelper.class.getName(), "Aggiornamento database dalla versione " + oldVersion + " alla "
                    + newVersion + ". I dati esistenti verranno eliminati.");
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TB_IMG);
                onCreate(db);
            } catch (SQLException e) {

                Log.i("ErrorDB_2", "Errore nell' onUpgrade , tutte o alcune query non sono state aggiornate ");
            }

        }


    }

}
