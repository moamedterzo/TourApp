package it.uniba.gruppo5.tourapp.firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import it.uniba.gruppo5.tourapp.utilities.ImageSaver;

public class FileStorageDAO {

    protected static String TAG = "DAOStorage";

    //Nome del file
    private String fileName;

    //Eventuale nuovo uri del file
    private Uri newUriFile;

    //Utilizzato per ottenere il percorso dei file temporanei
    private Context context;

    static FirebaseStorage storage = FirebaseStorage.getInstance();

    public FileStorageDAO(Context _context, String _pathFile){
        this.fileName = _pathFile;
        this.context = _context;
    }

    public void setNewUriFile(Uri _uriFile){
        this.newUriFile = _uriFile;
    }

    public void readFile(@NonNull final ReadFileStorageListener listener){

        try {
            if(fileName !=null && !fileName.isEmpty()) {

                //percorso del file locale
                String fileLocalPath = context.getFilesDir() + "/" + fileName;

                //ottengo il file e controllo se esiste o meno in locale
                File file = new File(fileLocalPath);
                if (!file.exists()) {

                    //nuovo file da creare
                    final File newFile = new File(fileLocalPath);

                    //ottengo referenza del file online
                    final StorageReference ref = storage.getReference().child(this.fileName);


                    ref.getFile(newFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            listener.onFileRead(newFile.getAbsolutePath());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.w(TAG, "readFile:onFailure", exception);
                        }
                    });
                }
                else {
                    //se esiste restituisco già il percorso
                    listener.onFileRead(file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "readFile:onException", e);
        }

    }

    public void saveFile(final FileStorageListener listener){

        if(this.newUriFile != null) {

            //genero nome file casuale
            final String fileName = UUID.randomUUID().toString();

            doSaveFile(fileName, listener);
        }
    }

    public void doSaveFile(String fileName, final FileStorageListener listener){
        //salvataggio online
        final StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(this.newUriFile).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri result = task.getResult();
                    if(listener!=null)
                        //il nome del file viene restituito
                        listener.onImageUploaded(result.getLastPathSegment());

                } else {
                    Log.w(TAG, "writeFile:onFailure");
                }
            }
        });
    }

    public static void deleteFile(String fileName) {

        if (fileName != null && !fileName.isEmpty()) {
            //cancellazione locale
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }

            //cancellazione online
            final StorageReference ref = storage.getReference().child(fileName);

            ref.delete();
        }
    }

    public interface FileStorageListener{

        void onImageUploaded(String resultPath);
    }

}
