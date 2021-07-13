package it.uniba.gruppo5.tourapp.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import it.uniba.gruppo5.tourapp.R;
import it.uniba.gruppo5.tourapp.firebase.FileStorageDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadFileStorageListener;

/**
 * Created by Sadruddin on 12/24/2017.
 */

public class HorizontalImageAdapter extends RecyclerView.Adapter<HorizontalImageAdapter.GroceryViewHolder>{

    public interface OnLongClickItemListener {

        void onItemLongClick(String image);
    }

    private OnLongClickItemListener mListener;


    public ArrayList<String> mModel;
    Context context;

    public HorizontalImageAdapter(ArrayList<String> mModel, Context context, OnLongClickItemListener listener){
        this.mModel = mModel;
        this.context = context;
        this.mListener = listener;
    }

    public HorizontalImageAdapter(ArrayList<String> mModel, Context context){
        this.mModel = mModel;
        this.context = context;
    }


    @Override
    public GroceryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate the layout file
        View groceryProductView = LayoutInflater.from(parent.getContext()).inflate(R.layout.img_normal, parent, false);
        GroceryViewHolder gvh = new GroceryViewHolder(groceryProductView);
        return gvh;
    }

    @Override
    public void onBindViewHolder(final GroceryViewHolder holder, final int position) {

       holder.bind(mModel.get(position), mListener);
    }


    public void addImmagine(String image) {

        mModel.add(image);
        notifyItemInserted(mModel.size() - 1);
    }

    public void removeImmagine(String image){

        int position = mModel.indexOf(image);
        mModel.remove(image);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mModel.size();
    }


    public static class GroceryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        LinearLayout scroll_item;
        public GroceryViewHolder(View view) {
            super(view);
            imageView=view.findViewById(R.id.imgNormal);
            scroll_item= view.findViewById(R.id.scroll_item);
        }


        public void bind(final String image,
                         final OnLongClickItemListener listener){

            FileStorageDAO fileStorageDAO = new FileStorageDAO(itemView.getContext(), image);
            fileStorageDAO.readFile(new ReadFileStorageListener() {
                @Override
                public void onFileRead(String absolutePath) {
                    Bitmap image = BitmapFactory.decodeFile(absolutePath);
                    imageView.setImageBitmap(image);
                }
            });



            // Click listener
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if(listener!=null) {
                        listener.onItemLongClick(image);
                        return true;
                    }
                    return false;
                }
            });
        }

    }
}
