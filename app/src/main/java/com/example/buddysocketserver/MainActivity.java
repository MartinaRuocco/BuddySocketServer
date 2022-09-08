package com.example.buddysocketserver;

import static com.bfr.buddy.ui.shared.FacialExpression.HEARING;
import static com.bfr.buddy.ui.shared.FacialExpression.LISTENING;
import static com.bfr.buddy.ui.shared.FacialExpression.LOVE;
import static com.bfr.buddy.ui.shared.FacialExpression.NEUTRAL;
import static com.bfr.buddy.ui.shared.FacialExpression.NONE;
import static com.bfr.buddy.ui.shared.FacialExpression.SCARED;
import static com.bfr.buddy.ui.shared.FacialExpression.SICK;
import static com.bfr.buddy.ui.shared.FacialExpression.THINKING;
import static com.bfr.buddy.ui.shared.FacialExpression.TIRED;

import android.os.Bundle;

import com.bfr.buddy.speech.shared.ITTSCallback;
import com.bfr.buddy.utils.events.EventItem;
import com.bfr.buddysdk.BuddyCompatActivity;
import com.bfr.buddysdk.BuddySDK;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressLint("SetTextI18n")

public class MainActivity extends BuddyCompatActivity {

    ServerSocket serverSocket;
    Thread Thread1 = null;
//    TextView tvIP, tvPort;
//    TextView tvMessages;
//    EditText etMessage;
//    Button btnSend;
    public static String SERVER_IP = "";
    public static final int SERVER_PORT = 8080;
    String message;

    final String  TAG = "BuddySocketServer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MYTAG", "onCreate");

//        tvIP = findViewById(R.id.tvIP);
//        tvPort = findViewById(R.id.tvPort);
//        tvMessages = findViewById(R.id.tvMessages);
//        etMessage = findViewById(R.id.etMessage);
//        btnSend = findViewById(R.id.btnSend);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Thread1 = new Thread(new Thread1());
        Thread1.start();

//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("MYTAG", "[onCLick] Button clicked!");
//                message = etMessage.getText().toString().trim();
//                if (!message.isEmpty()) {
//                    new Thread(new Thread3(message)).start();
//                }
//            }
//        });
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    private PrintWriter output;
    private BufferedReader input;

    class Thread1 implements Runnable {
        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        tvMessages.setText("Not connected");
//                        tvIP.setText("IP: " + SERVER_IP);
//                        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));
                    }
                });
                try {
                    socket = serverSocket.accept();

                    output = new PrintWriter(socket.getOutputStream());
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            tvMessages.setText("Connected\n");
                        }
                    });
                    new Thread(new Thread2()).start();

                    try {
                        serverSocket.close();
                        Log.i("MYTAG", "[T1] socket closed!");
                    } catch (IOException e) { e.printStackTrace(); }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class Thread2 implements Runnable {
        @Override
        public void run() {
            Log.i("MYTAG", "[T2] in Thread2");
            while (true) {
                Log.i("MYTAG", "[T2] ...in the while...");
                try {
                    final String message = input.readLine();
                    Log.i("MYTAG", "[T2] ...almost there...");
                    if (message != null) {
                        Log.i("MYTAG", "[T2] we are officially here: "+message);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                tvMessages.append("client:" + message + "\n");

//                                if (message.equals("A")) {
//                                    onStartSpeak("\\pause=10000\\ sentence 1");}
                                if (message.contains("A-")) {
                                    String[] temp = message.split("-");
                                    if (temp[1].equals("0")){      onStartSpeak("\\pause=1000\\ sentence 1");}
                                    else if (temp[1].equals("1")){ onStartSpeak("\\pause=1500\\ sentence 2");}
                                }
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            Log.i("MYTAG", "[T3] In Thread3");
            output.write(message);
            output.flush();
            Log.i("MYTAG", "[T3] ...after flush...");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    tvMessages.append("server: " + message + "\n");
//                    etMessage.setText("");
                    Log.i("MYTAG", "[T3] server replied!");
                }
            });
            try {
                serverSocket.close();
                Log.i("MYTAG", "[T3] socket closed!");
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    /////////////////////// BUDDY FUNCTIONS ///////////////////////////////

    // onCreate is at the beginning of the file.

    public void onSDKReady() {
        BuddySDK.UI.setViewAsFace(findViewById(R.id.view_face));
        BuddySDK.UI.setFacialExpression(NEUTRAL);
        Log.i("MYTAG", "onSDKReady");
    }

    public void onEvent(EventItem iEvent) {}

    /////////////////////// CUSTOM FUNCTIONS //////////////////////////////

    private void onStartSpeak(String message) {  // called in Thread2
        BuddySDK.Speech.setSpeakerVoice("kate");
        // if ready to speak
        if(BuddySDK.Speech.isReadyToSpeak())
        {
            BuddySDK.Speech.startSpeaking(
//                    "Bonjour \\pause=1200\\ Je suis un robot qui parle",
//                    to_say.getText().toString(),
                    message,
                    new ITTSCallback.Stub() {
                        @Override
                        public void onSuccess(String s) throws RemoteException {Log.i(TAG, "Message received : "+ s);}
                        @Override
                        public void onPause() throws RemoteException {}
                        @Override
                        public void onResume() throws RemoteException {}
                        @Override
                        public void onError(String s) throws RemoteException {Log.i(TAG, "Message received : "+ s);}
                    });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "TTS Not initialized", Toast.LENGTH_SHORT).show();
        }

    }

}