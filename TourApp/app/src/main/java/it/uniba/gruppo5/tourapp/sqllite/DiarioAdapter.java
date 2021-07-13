package it.uniba.gruppo5.tourapp.sqllite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import it.uniba.gruppo5.tourapp.R;


public class DiarioAdapter extends RecyclerView.Adapter<DiarioAdapter.DiarioViewHolder> {

    private ArrayList<DiarioImmagine> galleryList;
    private Context context;
    private static ClickListener clickListener;

    public DiarioAdapter(Context context, ArrayList<DiarioImmagine> galleryList) {
        this.galleryList = galleryList;
        this.context = context;
    }

    @NonNull
    @Override
    public DiarioViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_diario, viewGroup, false);

        return new DiarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiarioViewHolder viewHolder, int i) {


            viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //posto l'immagine nel layout con il bitmap prendendo l'immagine dalla memoria con il nome dell'immagine
            Bitmap bmp = BitmapFactory.decodeFile(DBLocaleDiario.IMMAGINI_DIARIO_DIRECTORY + File.separator + galleryList.get(i).getImage_perc());

            viewHolder.image.setImageBitmap(bmp);

    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    } //il contenuto di ogni immagine

    public void setOnItemClickListener(ClickListener clickListener){
        DiarioAdapter.clickListener = clickListener;
    }






    public class DiarioViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener , View.OnLongClickListener{

        private ImageView image;

        public DiarioViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            image = view.findViewById(R.id.img);
        }


        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition() , v);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLonkClick(getAdapterPosition(),v);
            return false;
        }
    }

   public interface ClickListener{
        void onItemClick(int position,View v);
        void onItemLonkClick(int position , View v);
   }
}
