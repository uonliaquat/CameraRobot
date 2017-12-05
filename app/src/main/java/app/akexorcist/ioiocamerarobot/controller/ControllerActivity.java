package app.akexorcist.ioiocamerarobot.controller;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import app.akexorcist.ioiocamerarobot.R;
import app.akexorcist.ioiocamerarobot.constant.Command;
import app.akexorcist.ioiocamerarobot.constant.ExtraKey;
import app.akexorcist.ioiocamerarobot.ioio.OrientationManager;

public class ControllerActivity extends Activity implements ConnectionManager.IOIOResponseListener, ConnectionManager.ConnectionListener, OnClickListener, OnCheckedChangeListener, JoyStickManager.JoyStickEventListener, SurfaceHolder.Callback, CameraManager.CameraManagerListener {
    private ImageView ivCameraImage;
    private ImageView iv;

    private CheckBox cbFlash;

    private ConnectionManager connectionManager;
    private JoyStickManager joyStickManager;

    private Button btnTakePhoto;
    private Button btnAutoFocus;
    private RelativeLayout layoutJoyStick;

    private SurfaceView surfacePreview;
    private CameraManager cameraManager;
    private RelativeLayout layoutParent;
    private OrientationManager orientationManager;
    private int imageQuality;

    private AudioRecord recorder;
    private int sampleRate = 16000;      //8000
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //CHANNEL_CONFIGURATION_MONO
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private byte[] buffer;
    private DatagramPacket audiopacket;
    public int CONTROLLER_PORT = 45000;
    public int mode = 0;
    //Audio Configuration.
    public static DatagramSocket socket;
    private AudioTrack speaker;
    boolean isAvailable = false;
    public NoiseSuppressor echoCanceler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_controller);

        int selectedPreviewSize = getIntent().getExtras().getInt(ExtraKey.PREVIEW_SIZE);
        imageQuality = 35; //getIntent().getExtras().getInt(ExtraKey.QUALITY);

        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        String ipAddress = getIntent().getExtras().getString(ExtraKey.IP_ADDRESS);
        String password = getIntent().getExtras().getString(ExtraKey.TARGET_PASSWORD);

        ivCameraImage = (ImageView) findViewById(R.id.iv_camera_image);
        iv = (ImageView) findViewById(R.id.iv_controller);

        layoutJoyStick = (RelativeLayout) findViewById(R.id.layout_joystick);
        joyStickManager = new JoyStickManager(this, layoutJoyStick, screenHeight);
        joyStickManager.setJoyStickEventListener(this);

















        //Floating Action Button

        ImageView imageView = new ImageView(this); // Create an icon
        imageView.setImageResource(R.drawable.ic_add);
        FloatingActionButton actionButton = new FloatingActionButton.Builder(this)
                .setContentView(imageView)
                .build();

        final ImageView iconMode = new ImageView(this);
        iconMode.setImageResource(R.drawable.ic_mode1);
        final ImageView iconCamera= new ImageView(this);
        iconCamera.setImageResource(R.drawable.ic_camera);
        final ImageView iconRightHand = new ImageView(this);
        iconRightHand.setImageResource(R.drawable.ic_hand0);
        final ImageView iconLeftHand = new ImageView(this);
        iconLeftHand.setImageResource(R.drawable.ic_hand1);

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        final SubActionButton buttonMode = itemBuilder.setContentView(iconMode).build();
        SubActionButton buttonCamera = itemBuilder.setContentView(iconCamera).build();
        SubActionButton buttonRightHand = itemBuilder.setContentView(iconRightHand).build();
        SubActionButton buttonLeftHand = itemBuilder.setContentView(iconLeftHand).build();
        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(buttonMode)
                .addSubActionView(buttonCamera)
                .addSubActionView(buttonLeftHand)
                .addSubActionView(buttonRightHand)
                .attachTo(actionButton)
                .build();


        buttonMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                iconMode.setImageResource(R.drawable.ic_tick);
                iconCamera.setImageResource(R.drawable.ic_camera);
                iconRightHand.setImageResource(R.drawable.ic_hand0);
                iconLeftHand.setImageResource(R.drawable.ic_hand1);
                Toast.makeText(ControllerActivity.this,"Robot Activated!",Toast.LENGTH_SHORT).show();
                mode = 0;
            }
        });
        buttonCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                iconMode.setImageResource(R.drawable.ic_mode);
                iconCamera.setImageResource(R.drawable.ic_tick);
                iconRightHand.setImageResource(R.drawable.ic_hand0);
                iconLeftHand.setImageResource(R.drawable.ic_hand1);
                Toast.makeText(ControllerActivity.this,"Camera Activated!",Toast.LENGTH_SHORT).show();
                mode = 1;
            }
        });
        buttonRightHand.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                iconMode.setImageResource(R.drawable.ic_mode);
                iconCamera.setImageResource(R.drawable.ic_camera);
                iconRightHand.setImageResource(R.drawable.ic_tick);
                iconLeftHand.setImageResource(R.drawable.ic_hand1);
                Toast.makeText(ControllerActivity.this,"Right Hand Activated!",Toast.LENGTH_SHORT).show();
                mode = 2;
            }
        });
        buttonLeftHand.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                iconMode.setImageResource(R.drawable.ic_mode);
                iconCamera.setImageResource(R.drawable.ic_camera);
                iconRightHand.setImageResource(R.drawable.ic_hand0);
                iconLeftHand.setImageResource(R.drawable.ic_tick);
                Toast.makeText(ControllerActivity.this,"Left Hand Activated!",Toast.LENGTH_SHORT).show();
                mode = 3;
            }
        });































        btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(this);

        btnAutoFocus = (Button) findViewById(R.id.btn_auto_focus);
        btnAutoFocus.setOnClickListener(this);
        btnAutoFocus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        cbFlash = (CheckBox) findViewById(R.id.cbFlash);
        cbFlash.setOnCheckedChangeListener(this);

        ////////////////////////////////////

        surfacePreview = (SurfaceView) findViewById(R.id.surface_preview2);
        surfacePreview.getHolder().addCallback(this);
        surfacePreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        layoutParent = (RelativeLayout) findViewById(R.id.layout_parent2);
        layoutParent.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cameraManager.requestAutoFocus();
            }
        });

        orientationManager = new OrientationManager(this);
        cameraManager = new app.akexorcist.ioiocamerarobot.controller.CameraManager(selectedPreviewSize);
        cameraManager.setCameraManagerListener(this);

        ////////////////////
        connectionManager = new ConnectionManager(this, ipAddress, password);
        connectionManager.start();
        connectionManager.setConnectionListener(this);
        connectionManager.setResponseListener(this);

        // audio sending thread create here:
        //
        //
         StartMic();
        //audio receiving thread
        StartSpeaker();

    }
    private void StartSpeaker(){

        Thread istreamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    int minBufSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
                    speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,minBufSize, AudioTrack.MODE_STREAM);
                    speaker.play();
                    byte[] message = new byte[minBufSize];
                    DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
                    DatagramSocket datagramSocket = new DatagramSocket(CONTROLLER_PORT);
//                    InetSocketAddress socketAddress = new InetSocketAddress()

//                    datagramSocket.setReuseAddress(true);
//                    datagramSocket.setBroadcast(true);
//                    datagramSocket.bind(new InetSocketAddress(CONTROLLER_PORT));


                    while(1==1){
                        try{
                            datagramSocket.setSoTimeout(3000);
                            datagramSocket.receive(datagramPacket);
                            speaker.write(message,0,minBufSize);
                        }
                        catch (Exception d){
                            d.printStackTrace();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        });
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        istreamThread.start();
    }


    private void StartMic() {

        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    buffer = new byte[minBufSize];
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);
                    Log.d("VS", "Recorder initialized");
                   // echoCanceler = NoiseSuppressor.create(recorder.getAudioSessionId());
                    //isAvailable = echoCanceler.getEnabled();
                    DatagramSocket socket = new DatagramSocket();
                    recorder.startRecording();


                    while(1==1){
                        minBufSize = recorder.read(buffer, 0, buffer.length);
                        audiopacket = new DatagramPacket (buffer,buffer.length, InetAddress.getByName(connectionManager.ipAddress),connectionManager.PORT);
                        socket.send(audiopacket);

                    }


                }
                catch(Exception e){

                }
            }
        });
       // Process.setThreadPriority(100);
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        streamThread.start();

    }

    @Override
    public void onStop() {
        super.onStop();
        connectionManager.stop();
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_auto_focus) {
            requestAutoFocus();
        } else if (id == R.id.btn_take_photo) {
            requestTakePhoto();
        }
    }

    public void requestAutoFocus() {
        connectionManager.sendCommand(Command.FOCUS);
    }

    public void requestTakePhoto() {
        connectionManager.sendCommand(Command.SNAP);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            connectionManager.sendCommand(Command.LED_ON);
        } else {
            connectionManager.sendCommand(Command.LED_OFF);
        }
    }

    @Override
    public void onPictureTaken() {
        showToast(getString(R.string.photo_taken));
    }

    @Override
    public void onFlashUnavailable() {
        showToast(getString(R.string.unsupport_flash));
    }

    @Override
    public void onCameraImageIncoming(Bitmap bitmap) {
        ivCameraImage.setImageBitmap(bitmap);
    }

    @Override
    public void onConnectionDown() {
        showToast(getString(R.string.connection_down));
        finish();
    }

    @Override
    public void onConnectionFailed() {
        showToast(getString(R.string.connection_failed));
        finish();
    }

    @Override
    public void onWrongPassword() {
        showToast(getString(R.string.wrong_password));
        finish();
    }

    @Override
    public void onIOIOConnected() {
        showToast(getString(R.string.connection_accepted));
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public String getCommandString(int x, int y){
        if(mode == 1) {
            String ret = "C" + "x;" + x + ";y;" + y;
            return ret;
        }
        else if (mode == 2){
            String ret = "R" + "x;" + x + ";y;" + y;
            return ret;
        }
        else if (mode == 3){
            String ret = "L" + "x;" + x + ";y;" + y;
            return ret;
        }
        else {
            String ret = "M" + "x;" + x + ";y;" + y;
            //String ret = "x;" + x + ";y;" + y;
            return ret;
        }

    }

    //umar
    @Override
    public void onJoyStickUp(int x, int y) {
        //send x and y

        connectionManager.sendMovement(getCommandString(x,y));// Command.FORWARD + speed);
    }

    @Override
    public void onJoyStickUpRight(int x, int y) {
        connectionManager.sendMovement(getCommandString(x,y));
    }

    @Override
    public void onJoyStickUpLeft(int x, int y) {
        connectionManager.sendMovement(getCommandString(x,y));
    }

    @Override
    public void onJoyStickDown(int x, int y) {
        connectionManager.sendMovement(getCommandString(x,y));
    }

    @Override
    public void onJoyStickDownRight(int x, int y) {
        connectionManager.sendMovement(getCommandString(x,y));
    }

    @Override
    public void onJoyStickDownLeft(int x, int y) {
        connectionManager.sendMovement(getCommandString(x,y));
    }

    @Override
    public void onJoyStickRight(int x, int y) {
        connectionManager.sendMovement(getCommandString(x,y));
    }

    @Override
    public void onJoyStickLeft(int x, int y) {
        connectionManager.sendMovement(getCommandString(x,y));
    }

    @Override
    public void onJoyStickNone() {
        connectionManager.sendMovement(getCommandString(0,0));
        connectionManager.sendMovement(getCommandString(0,0));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cameraManager.createCameraInstance(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (surfacePreview == null)
            return;

        cameraManager.stopCameraPreview();
        cameraManager.initCameraParameter();

        setupPreviewLayout();

        cameraManager.setCameraOrientation(orientationManager.getOrientation());
        cameraManager.startCameraPreview(surfacePreview);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        cameraManager.destroyCameraInstance();
    }

    @Override
    public void onPictureTaken(String filename, String path) {
        //connectionManager.sendCommand(Command.SNAP);
    }

    @Override
    public void onPreviewTaken(Bitmap bitmap) {
//        if (isConnected) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, bos);
            byte[] buffer = bos.toByteArray();
            connectionManager.sendImageData(buffer);

            iv.setImageBitmap(bitmap);
            //bitmap.recycle();

//            //totaka: bitmap > imageview iv_camera_image2
//            Bitmap bitmap2 = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
//            ivCameraImage2.setImageBitmap(bitmap2);

//        }
    }

    @Override
    public void onPreviewOutOfMemory(OutOfMemoryError e) {
        e.printStackTrace();
        showToast(getString(R.string.out_of_memory));
        finish();
    }

    @SuppressWarnings("deprecation")
    public void setupPreviewLayout() {
        Display display = getWindowManager().getDefaultDisplay();
        ViewGroup.LayoutParams lp = layoutParent.getLayoutParams();

        float previewWidth = cameraManager.getPreviewSize().width;
        float previewHeight = cameraManager.getPreviewSize().height;

        int orientation = orientationManager.getOrientation();
        float ratio = 0;
        if (orientation == OrientationManager.LANDSCAPE_NORMAL
                || orientation == OrientationManager.LANDSCAPE_REVERSE) {
            ratio = previewWidth / previewHeight;
        } else if (orientation == OrientationManager.PORTRAIT_NORMAL
                || orientation == OrientationManager.PORTRAIT_REVERSE) {
            ratio = previewHeight / previewWidth;
        }

        if ((int) ((float) surfacePreview.getWidth() / ratio) >= display.getHeight()) {
            lp.height = (int) ((float) surfacePreview.getWidth() / ratio);
            lp.width = surfacePreview.getWidth();
        } else {
            lp.height = surfacePreview.getHeight();
            lp.width = (int) ((float) surfacePreview.getHeight() * ratio);
        }

        layoutParent.setLayoutParams(lp);
        int locationX = (int) (lp.width / 2.0 - surfacePreview.getWidth() / 2.0);
        layoutParent.animate().translationX(-locationX);
    }

}
