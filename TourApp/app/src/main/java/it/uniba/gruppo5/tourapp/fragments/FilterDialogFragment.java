package it.uniba.gruppo5.tourapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;

import it.uniba.gruppo5.tourapp.R;

public class FilterDialogFragment extends DialogFragment
{
    FilterDialogListener mFilterDialogListener;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View mRootView = inflater.inflate(R.layout.dialog_filters, container, false);

        //inserimento del layout dei filtri personalizzati
        ViewStub stub = mRootView.findViewById(R.id.view_stub);
        stub.setLayoutResource(this.mFilterDialogListener.getIDLayout());
        stub.inflate();

        //richiama il listener
        this.mFilterDialogListener.onCreateViewFilterDialog(mRootView);


        //bottone conferma
        Button button_search = mRootView.findViewById(R.id.button_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //chiamata del metodo di filtraggio
                mFilterDialogListener.onFilter();
                dismiss();
            }
        });

        //bottone chiudi
        Button button_cancel = mRootView.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //registra activity come listener
        mFilterDialogListener = (FilterDialogListener)context;
    }


    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFilterDialogListener = null;
    }

    public interface FilterDialogListener
    {
        void onFilter();

        void onCreateViewFilterDialog(View dialogView);

        int getIDLayout();
    }

}
