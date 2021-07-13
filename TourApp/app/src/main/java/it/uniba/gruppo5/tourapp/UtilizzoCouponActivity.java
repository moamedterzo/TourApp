package it.uniba.gruppo5.tourapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.uniba.gruppo5.tourapp.bluetooth.BluetoothChatService;
import it.uniba.gruppo5.tourapp.bluetooth.MessageAdapter;
import it.uniba.gruppo5.tourapp.authentication.UserAuthenticationManager;
import it.uniba.gruppo5.tourapp.firebase.CouponDAO;
import it.uniba.gruppo5.tourapp.firebase.ReadValueListener;
import it.uniba.gruppo5.tourapp.firebase.UtenteDAO;

//aggiungere i colori anche
public class UtilizzoCouponActivity extends BaseActivity {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private CardView mSendButton;
    private CardView btn_connect;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MessageAdapter mAdapter;


    public int counter = 0;

    private List<it.uniba.gruppo5.tourapp.bluetooth.Message> messageList = new ArrayList<it.uniba.gruppo5.tourapp.bluetooth.Message>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_utilizzo_coupon);
        setMenuAndDrawer();

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter(getApplicationContext(), messageList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mSendButton=findViewById(R.id.button_send);
        btn_connect = findViewById(R.id.btn_connect);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.bluetooth_not_avalaible), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        discoverable();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }
//controllare la visualizzazzione degli elementi
    private void setupChat() {

        mSendButton = findViewById(R.id.button_send);

        if (UserAuthenticationManager.getUserTipo(UtilizzoCouponActivity.this).equals(UtenteDAO.TIPO_UTENTE_OPERATORE)) {
            //continuare
            mSendButton.setVisibility(View.GONE);
            mSendButton.setFocusable(false);
        }

        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(UtilizzoCouponActivity.this, R.style.myDialog));
                builder.setCancelable(true);
                builder.setTitle(R.string.use_coupon);
                builder.setMessage(R.string.use_coupon_desc);
                builder.setIcon(R.drawable.ic_warning_black_24dp);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //intent per ricevere l id coupon e Id utente
                        Intent intent = getIntent();
                        String validate = intent.getStringExtra("ID_COUPON_ID_UTENTE");

                        sendMessage(validate);
                    }

                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }


    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
    }

    private void sendMessage(String message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    return true;
                }
            };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer

                    String writeMessage = new String(writeBuf);

                    final String idCoupon = writeMessage.split(";")[0];
                    final String idUtente = writeMessage.split(";")[1];


                    //CONTROLLARE UTILIZZO DEL COUPON  DA FIREBASE
                    CouponDAO.getCouponUtilizzo(idCoupon, idUtente, new ReadValueListener<Boolean>() {
                        @Override
                        public void onDataRead(Boolean result) {

                            if (result) {
                                //utilizzato
                                mAdapter.notifyDataSetChanged();
                                messageList.add(new it.uniba.gruppo5.tourapp.bluetooth.Message(counter++, getString(R.string.coupon_gia_utilizzato), "Me"));

                            } else {
                                //non utilizzato
                                mAdapter.notifyDataSetChanged();
                                messageList.add(new it.uniba.gruppo5.tourapp.bluetooth.Message(counter++,  getString(R.string.coupon_utilizzato), "Me"));
                                CouponDAO.utilizzaCoupon(idCoupon,idUtente);

                            }
                        }
                    });


                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mAdapter.notifyDataSetChanged();
                    //funzione per visualizzare i messaggi commentare per non visualizzare i messaggi
                    //SE RICEVE UTILIZZATO CORRETTAMENTE ESCE E CONVALIDA COUPON
                    final String Coupon = readMessage.split(";")[0];
                    final String Utente = readMessage.split(";")[1];

                    CouponDAO.getCouponUtilizzo(Coupon, Utente, new ReadValueListener<Boolean>() {
                        @Override
                        public void onDataRead(Boolean result) {

                            if (result) {
                                //utilizzato
                                mAdapter.notifyDataSetChanged();
                                messageList.add(new it.uniba.gruppo5.tourapp.bluetooth.Message(counter++, getString(R.string.coupon_già_util), mConnectedDeviceName));

                            } else {
                                //non utilizzato
                                mAdapter.notifyDataSetChanged();
                                messageList.add(new it.uniba.gruppo5.tourapp.bluetooth.Message(counter++,  getString(R.string.coup_accept), mConnectedDeviceName));

                            }
                        }
                    });

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When ElencoDispositiviActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(ElencoDispositiviActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);

                    //visibile bottone invio
                    mSendButton.setVisibility(View.VISIBLE);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void connect(View v) {

        if(UserAuthenticationManager.getUserTipo(UtilizzoCouponActivity.this).equals(UtenteDAO.TIPO_UTENTE_OPERATORE)) {
            //operatore
            discoverable();
            Toast.makeText(this,getString(R.string.bluetooth_ok_discoverable), Toast.LENGTH_LONG).show();
        }
        else{
            Intent serverIntent = new Intent(this, ElencoDispositiviActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
    }

    public void discoverable() {

        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        //ok, si può procedere con la connessione al dispositivo
        btn_connect.setVisibility(View.VISIBLE);
    }
}