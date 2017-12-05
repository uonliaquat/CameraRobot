package app.akexorcist.ioiocamerarobot.ioio;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import app.akexorcist.ioiocamerarobot.constant.Command;

public class IOIOService extends AsyncTask<Void, Bitmap, Void> {
    private static final String TAG = "IOIOService";
    private static final int PORT = 21111;

    private static final String ROBOT_IP = "192.168.43.127";
    private static final int ROBOT_PORT = 4210;

    private static final String OBSERVER_IP = "192.168.43.122";
    private static final int OBSERVER_PORT = 4023;

    private boolean isTaskRunning = true;
    private ServerSocket serverSocket;
    private Socket socket;
    private String password;

    private DataInputStream dataInputStream;
    private InputStream inputStream;
    private Handler handler;

    private IOIOControllerActivity activity;
    private boolean ready = false;


    private AudioTrack speaker;
    private int sampleRate = 16000;      //8000
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //CHANNEL_CONFIGURATION_MONO
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private byte[] buffer;
    //private DatagramPacket audiopacket;




    public IOIOService(Handler handler, String password, IOIOControllerActivity a) {
        this.handler = handler;
        this.password = password;
        activity = a;
    }


    public String getCommandString(int x, int y, char mode){
        String ret = mode+"x;" +x + ";y;" + y;
        //ret[0] = mode;
        return ret;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        activity.ivCameraImage2.setImageBitmap(values[0]);
    }

    protected Void doInBackground(Void... params) {
        Runnable run = new Runnable() {
            public void run() {

                try {

                    int minBufSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
                    speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,minBufSize, AudioTrack.MODE_STREAM);
                    speaker.play();

                    byte[] message = new byte[minBufSize];
                    int x,y; x=y=0;
                    DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                    DatagramSocket datagramSocket = new DatagramSocket(null);
                    datagramSocket.setReuseAddress(true);
                    datagramSocket.setBroadcast(true);
                    datagramSocket.bind(new InetSocketAddress(PORT));





                    while (isTaskRunning) {
                        try {
                            datagramSocket.setSoTimeout(3000);
                            datagramSocket.receive(datagramPacket);
                            activity.CONTROLLER_IP = datagramPacket.getAddress();
                            String text = new String(message, 0, datagramPacket.getLength());

                            String command = text.substring(1, 3);
                            if (command.equalsIgnoreCase("x;")) {
                                try{
                                    String val = text.substring(3);
                                    val = val.substring(0,val.indexOf(";"));
                                    x = Integer.parseInt(val);
                                    y = Integer.parseInt(text.substring(text.lastIndexOf(";")+1));
                                    x = (x*100)/230; x =(x>100)? 100: x; x =(x<-100)? -100: x; //scale and limit x
                                    y = -1*y; y = (y*100)/230; y =(y>100)? 100: y; y =(y<-100)? -100: y; //invert, scale and limit y
                                    text = getCommandString(x, y,text.charAt(0));
                                    //int speed = Integer.parseInt(text.substring(2, text.length()));
                                    //umar
                                    // send udp packet to robot
                                    InetAddress robot_address = InetAddress.getByName(ROBOT_IP);
                                    DatagramSocket ds = new DatagramSocket();
                                    DatagramPacket dp = new DatagramPacket(text.getBytes(),text.length(),robot_address,ROBOT_PORT);
                                    ds.setBroadcast(false);
                                    ds.send(dp);
                                    //echo to observer
                                    InetAddress observer_address = InetAddress.getByName(OBSERVER_IP);
                                    dp = new DatagramPacket(text.getBytes(),text.length(),observer_address,OBSERVER_PORT);
                                    ds.setBroadcast(false);
                                    ds.send(dp);

                                    String original = new String(message, 0, datagramPacket.getLength());
                                    Log.d("x:",x+": y:"+y+" ORIGINAL:"+original);

                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                    String original = new String(message, 0, datagramPacket.getLength());
                                    Log.d("::: x:",x+": y:"+y+" ORIGINAL:"+original);
                                }



                                //handler.obtainMessage(Command.MESSAGE_UP, speed - 50).sendToTarget();
                            }

                            // receive audio datagram here in else
                            else { //message.getlength = minbufsize???
                                //buffer = new byte[datagramPacket.getLength()];
                                //buffer = datagramPacket.getData();
                                Log.d("output",message.toString());
                               speaker.write(message,0,minBufSize);


                            }





//                            else if (command.equalsIgnoreCase(Command.FORWARD)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_UP, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.FORWARD_RIGHT)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_UPRIGHT, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.FORWARD_LEFT)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_UPLEFT, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.BACKWARD)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_DOWN, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.BACKWARD_RIGHT)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_DOWNRIGHT, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.BACKWARD_LEFT)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_DOWNLEFT, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.RIGHT)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_RIGHT, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.LEFT)) {
//                                int speed = Integer.parseInt(text.substring(2, text.length()));
//                                handler.obtainMessage(Command.MESSAGE_LEFT, speed - 50).sendToTarget();
//                            } else if (command.equalsIgnoreCase(Command.STOP)) {
//                                handler.obtainMessage(Command.MESSAGE_STOP).sendToTarget();
//                            }

                        }
//                        catch (SocketTimeoutException e) {
//                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    datagramSocket.close();
                    Log.e(TAG, "Kill Task");
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(run).start();

        try {
            serverSocket = new ServerSocket(21111);
            serverSocket.setSoTimeout(2000);
            Log.i(TAG, "Waiting for connect");
            while (socket == null && isTaskRunning) {
                try {
                    socket = serverSocket.accept();
                    socket.setSoTimeout(2000);
                } catch (InterruptedIOException e) {
                    Log.i(TAG, "Waiting for connect");
                } catch (SocketException e) {
                   e.printStackTrace();
                }
            }

            if (isTaskRunning) {
                inputStream = socket.getInputStream();
                dataInputStream = new DataInputStream(inputStream);
                int size = dataInputStream.readInt();
                byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);
                if ((new String(buffer)).equalsIgnoreCase(password)) {
                    handler.obtainMessage(Command.MESSAGE_PASS, socket).sendToTarget();
                } else {
                    handler.obtainMessage(Command.MESSAGE_WRONG, socket).sendToTarget();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (isTaskRunning) {
            try {
                int size = dataInputStream.readInt();
                byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);
                String data = new String(buffer);
                Bitmap bitmap2;

                if (data.equalsIgnoreCase(Command.SNAP)) {
                    handler.obtainMessage(Command.MESSAGE_SNAP).sendToTarget();
                } else if (data.equalsIgnoreCase(Command.LED_ON) || data.equalsIgnoreCase(Command.LED_OFF)) {
                    handler.obtainMessage(Command.MESSAGE_FLASH, data).sendToTarget();
                } else if (data.equalsIgnoreCase(Command.FOCUS)) {
                    handler.obtainMessage(Command.MESSAGE_FOCUS).sendToTarget();
                } else if (data.length()>200){
                    try {
                        // read stream for video
                        bitmap2 = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                        //activity.ivCameraImage2.setImageBitmap(bitmap2);
                        publishProgress(bitmap2);
                        //bitmap2.recycle();
                    } catch(Exception e) {
                        //bitmap2.recycle();
                        e.printStackTrace();
                    }
                }

            } catch (EOFException e) {
                e.printStackTrace();
                handler.obtainMessage(Command.MESSAGE_CLOSE).sendToTarget();
                break;
            } catch (IOException e) {
            }

            if (!socket.isConnected()) {
                handler.obtainMessage(Command.MESSAGE_DISCONNECTED).sendToTarget();
            }
        }
        try {
            serverSocket.close();
            socket.close();
            inputStream.close();
            dataInputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Service was killed");
        return null;
    }

    public void killTask() {
        isTaskRunning = false;
    }
}
