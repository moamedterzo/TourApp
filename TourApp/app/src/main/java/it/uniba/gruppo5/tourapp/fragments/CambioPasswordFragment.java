package it.uniba.gruppo5.tourapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import it.uniba.gruppo5.tourapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CambioPasswordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CambioPasswordFragment extends DialogFragment {

    public static final String ARG_VECCHIA_PSW = "AR_VP";

    private OnFragmentInteractionListener mListener;

    private String vecchiaPassword ;

    public CambioPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_cambio_password, container, false);

        //ottenimento vecchia psw
        Bundle bundle = getArguments();
        vecchiaPassword = bundle.getString(ARG_VECCHIA_PSW);

        Button button_cambia_password = rootView.findViewById(R.id.button_cambia_password);
        button_cambia_password.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                TextView etNuovaPassword = rootView.findViewById(R.id.etNuovaPassword);
                TextView etConfermaPassword = rootView.findViewById(R.id.etConfermaPassword);
                TextView etVecchiaPassword = rootView.findViewById(R.id.etVecchiaPassword);

                if(etVecchiaPassword.getText().toString().equals(vecchiaPassword)){

                    //controllo uguaglianza psw
                    if(etConfermaPassword.getText().toString().equals(etNuovaPassword.getText().toString())){

                        //ok
                        mListener.onChangePassword(etNuovaPassword.getText().toString());
                        dismiss();
                    }
                    else {
                        //ko, confronto errato
                        Toast.makeText(CambioPasswordFragment.this.getActivity(),getResources().getString(R.string.inser_psw), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    //ko, vecchia psw errata
                    Toast.makeText(CambioPasswordFragment.this.getActivity(),getResources().getString(R.string.psw_err), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //chiusura dialog
        Button button_annulla = rootView.findViewById(R.id.button_annulla);
        button_annulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onChangePassword(String newPassword);
    }
}
