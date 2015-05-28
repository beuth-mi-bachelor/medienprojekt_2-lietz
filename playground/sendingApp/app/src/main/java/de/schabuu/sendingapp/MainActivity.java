package de.schabuu.sendingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import net.nanocosmos.nanoStream.streamer.AdaptiveBitrateControlSettings;
import net.nanocosmos.nanoStream.streamer.Logging;
import net.nanocosmos.nanoStream.streamer.NanostreamEvent;
import net.nanocosmos.nanoStream.streamer.NanostreamEventListener;
import net.nanocosmos.nanoStream.streamer.NanostreamException;
import net.nanocosmos.nanoStream.streamer.nanoResults;
import net.nanocosmos.nanoStream.streamer.nanoStream;

import android.preference.PreferenceManager;

import de.schabuu.sendingapp.ui.StreamPreview;
import de.schabuu.sendingapp.util.PreferenceEnum;
import de.schabuu.sendingapp.util.VideoCamera;


public class MainActivity extends ActionBarActivity implements NanostreamEventListener {

    private StreamPreview surface;

    private nanoStream streamLib;

    private int width = 640;
    private int height = 480;
    private int BIT_RATE = 500000;
    private int FRAME_RATE = 15;

    private String license = "nlic:1.2:LiveEnc:1.1:LivePlg=1,H264ENC=1,MP4=1,RTMPsrc=1,RtmpMsg=1,RTMPm=4,RTMPx=3,Resz=1,Demo=1,Ic=1,NoMsg=1:adr:20150429,20151213::0:0:nanocosmos-471231-28:ncpt:28cd49a163eaf61a48484c9e17a5d808";
    private String serverUrl = "rtmp://141.64.64.14/live";
    private String streamName = "nanoStream";
    private String authUser = "schabuu";
    private String authPass = "1qayse4";
    private AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.DISABLED;

    private VideoCamera mVideoCam = null;
    private nanoStream.VideoSourceType vsType = nanoStream.VideoSourceType.EXTERNAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        surface = (StreamPreview) findViewById(R.id.surface);

        loadPreferences();

        AdaptiveBitrateControlSettings abcSettings = new AdaptiveBitrateControlSettings(abcMode);
        Logging.LogSettings logSettings = new Logging.LogSettings(Logging.LogLevel.VERBOSE, 1);

        try
        {
            streamLib = new nanoStream(vsType, width, height, BIT_RATE, FRAME_RATE, surface.getHolder(), 2, license, serverUrl, streamName, authUser, authPass,
                    this, abcSettings, logSettings);
            mVideoCam = new VideoCamera(width, height, FRAME_RATE, surface.getHolder());
            mVideoCam.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            streamLib.setVideoSource(mVideoCam);
        } catch (NanostreamException en)
        {
            Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
        }
        try
        {
            if (streamLib != null)
            {
                streamLib.init();
            }
        } catch (NanostreamException en)
        {
            Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
        }

        PreferenceManager.getDefaultSharedPreferences(sendingApp.getAppContext()).registerOnSharedPreferenceChangeListener(new PreferenceChangeListener());
    }

    @Override
    protected void onRestart()
    {
        Log.i(this.getClass().getName(), "restart");
        super.onRestart();
        SurfaceView surface = (SurfaceView) findViewById(R.id.surface);
        surface.setVisibility(View.INVISIBLE);
        loadPreferences();

        AdaptiveBitrateControlSettings abcSettings = new AdaptiveBitrateControlSettings(abcMode);
        Logging.LogSettings logSettings = new Logging.LogSettings(Logging.LogLevel.VERBOSE, 1);

        try
        {
            streamLib = new nanoStream(vsType, width, height, BIT_RATE, FRAME_RATE, surface.getHolder(), 2, license, serverUrl, streamName, authUser, authPass,
                    this, abcSettings, logSettings);
            mVideoCam = new VideoCamera(width, height, FRAME_RATE, surface.getHolder());
            mVideoCam.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            streamLib.setVideoSource(mVideoCam);
        } catch (NanostreamException en)
        {
            Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
        }

        try
        {
            if (streamLib != null)
            {
                streamLib.init();
            }
        } catch (NanostreamException en)
        {
            Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
        }
        surface.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * "OnClick"-Methode f�r den "Start"-Men�-Eintrag.
     *
     * @param clicked
     *            - Der geclickte Menu-Eintrag.
     */
    public void toggleStreaming(MenuItem clicked)
    {
        if (streamLib == null)
        {
            Toast.makeText(getApplicationContext(), "nanoStream failed to initialize", Toast.LENGTH_LONG).show();
            return;
        }

        if (!streamLib.hasState(nanoStream.EncoderState.RUNNING))
        {
            if (!isNetworkAvailable())
            {
                Toast.makeText(getApplicationContext(), "Cannot find available network. Please check your device settings.", Toast.LENGTH_LONG).show();
                return;
            } else
            {
                Toast.makeText(getApplicationContext(), "Starting...", Toast.LENGTH_SHORT).show();
            }
            if (streamLib.hasState(nanoStream.EncoderState.STOPPED) || streamLib.hasState(nanoStream.EncoderState.CREATED))
            {
                try
                {
                    Log.d("StreamToogle", "init");
                    streamLib.init();
                } catch (NanostreamException en)
                {
                    Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
                    return;
                }
            }

            try
            {
                streamLib.start();
            } catch (NanostreamException en)
            {
                Toast.makeText(getApplicationContext(), en.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            //clicked.setIcon(getResources().getDrawable(R.drawable.but_stop));
        } else
        {
            Toast.makeText(getApplicationContext(), "Stopping...", Toast.LENGTH_SHORT).show();

            streamLib.stop();
            //clicked.setIcon(getResources().getDrawable(R.drawable.but_start));
        }
    }

    public void toggleCamera(MenuItem clicked)
    {
        if (streamLib == null)
        {
            Toast.makeText(getApplicationContext(), "nanoStream failed to initialize", Toast.LENGTH_LONG).show();
            return;
        }

        try
        {
            streamLib.rotateCamera();
        } catch (NanostreamException e)
        {
            if (e.getCode() == nanoResults.N_CAMERA_NOSECOND)
            {
                Toast.makeText(getApplicationContext(), nanoResults.GetDescription(nanoResults.N_CAMERA_NOSECOND), Toast.LENGTH_LONG).show();
            } else
            {
                e.printStackTrace();
            }
        }
        if (!streamLib.hasState(nanoStream.EncoderState.RUNNING))
        {
            invalidateOptionsMenu();
        }

    }

    @Override
    // NanostreamEventListener
    public void onNanostreamEvent(NanostreamEvent event)
    {
        this.runOnUiThread(new NotificationRunable(event));
    }

    private class NotificationRunable implements Runnable
    {
        private NanostreamEvent m_event;

        public NotificationRunable(NanostreamEvent event)
        {
            m_event = event;
        }

        @Override
        public void run()
        {
            if (m_event.GetType() != NanostreamEvent.TYPE_RTMP_QUALITY)
            {
                Toast.makeText(getApplicationContext(), m_event.GetDescription(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener
    {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {

            Log.i(this.getClass().getName(), "Preference changed");

            if (PreferenceEnum.PREF_RESOLUTION_KEY.equalsValue(key))
            {
                String size = sharedPreferences.getString(key, "640x480");
                String[] sizes = size.split("x");

                if (sizes.length > 2)
                {
                    throw new RuntimeException(new IllegalArgumentException("Wrong resolution value."));
                }

                width = Integer.parseInt(sizes[0]);
                height = Integer.parseInt(sizes[1]);
            }

            if (PreferenceEnum.PREF_BITRATE_KEY.equalsValue(key))
            {
                String value = sharedPreferences.getString(key, "500000");
                BIT_RATE = Integer.parseInt(value);
            }

            if (PreferenceEnum.PREF_FPS_KEY.equalsValue(key))
            {
                FRAME_RATE = Integer.parseInt(sharedPreferences.getString(key, "15"));
            }

            if (PreferenceEnum.PREF_URI_KEY.equalsValue(key))
            {
                serverUrl = sharedPreferences.getString(PreferenceEnum.PREF_URI_KEY.getValue(), serverUrl);
            }

            if (PreferenceEnum.PREF_CODE_KEY.equalsValue(key))
            {
                streamName = sharedPreferences.getString(PreferenceEnum.PREF_CODE_KEY.getValue(), streamName);
            }

            if (PreferenceEnum.PREF_AUTH_USER_KEY.equalsValue(key))
            {
                authUser = sharedPreferences.getString(PreferenceEnum.PREF_AUTH_USER_KEY.getValue(), authUser);
            }

            if (PreferenceEnum.PREF_AUTH_PASS_KEY.equalsValue(key))
            {
                authPass = sharedPreferences.getString(PreferenceEnum.PREF_AUTH_PASS_KEY.getValue(), authPass);
            }

			/*
			 * if(PreferenceEnum.PREF_ABC_MIN_BITRATE.equalsValue(key)) {
			 * abcMinBitrate =
			 * Integer.parseInt(sharedPreferences.getString(PreferenceEnum
			 * .PREF_ABC_MIN_BITRATE.getValue(),
			 * String.valueOf(abcMinBitrate))); }
			 *
			 * if(PreferenceEnum.PREF_ABC_MIN_FRAMERATE.equalsValue(key)) {
			 * abcMinFramerate =
			 * Integer.parseInt(sharedPreferences.getString(PreferenceEnum
			 * .PREF_ABC_MIN_FRAMERATE.getValue(),
			 * String.valueOf(abcMinFramerate))); }
			 *
			 * if(PreferenceEnum.PREF_ABC_FLUSH_BUFFER_THRESHOLD.equalsValue(key)
			 * ) { abcFlushBufferThreshold =
			 * Integer.parseInt(sharedPreferences.getString
			 * (PreferenceEnum.PREF_ABC_FLUSH_BUFFER_THRESHOLD.getValue(),
			 * String.valueOf(abcFlushBufferThreshold))); }
			 */

            if (PreferenceEnum.PREF_ABC_MODE.equalsValue(key))
            {
                switch (Integer.parseInt(sharedPreferences.getString(PreferenceEnum.PREF_ABC_MODE.getValue(), String.valueOf(0))))
                {
                    case 0:
                    {
                        abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.DISABLED;
                        break;
                    }
                    case 1:
                    {
                        abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.QUALITY_DEGRADE;
                        break;
                    }
                    case 2:
                    {
                        abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.FRAME_DROP;
                        break;
                    }
                    case 3:
                    {
                        abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.QUALITY_DEGRADE_AND_FRAME_DROP;
                        break;
                    }
                    default:
                    {
                        abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.DISABLED;
                        break;
                    }
                }
            }
        }
    }

    private void loadPreferences()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sendingApp.getAppContext());

        BIT_RATE = Integer.parseInt(prefs.getString(PreferenceEnum.PREF_BITRATE_KEY.getValue(), String.valueOf(BIT_RATE)));
        FRAME_RATE = Integer.parseInt(prefs.getString(PreferenceEnum.PREF_FPS_KEY.getValue(), String.valueOf(FRAME_RATE)));
        String size = prefs.getString(PreferenceEnum.PREF_RESOLUTION_KEY.getValue(), width + "x" + height);
        String[] sizeParts = size.split("x");
        width = Integer.parseInt(sizeParts[0]);
        height = Integer.parseInt(sizeParts[1]);
        serverUrl = prefs.getString(PreferenceEnum.PREF_URI_KEY.getValue(), serverUrl);
        streamName = prefs.getString(PreferenceEnum.PREF_CODE_KEY.getValue(), streamName);
        authUser = prefs.getString(PreferenceEnum.PREF_AUTH_USER_KEY.getValue(), authUser);
        authPass = prefs.getString(PreferenceEnum.PREF_AUTH_PASS_KEY.getValue(), authPass);

		/*
		 * abcMinBitrate = AdaptiveBitrateControlSettings.DEFAULT_MIN_BITRATE;
		 * //
		 * Integer.parseInt(prefs.getString(PreferenceEnum.PREF_ABC_MIN_BITRATE
		 * .getValue(), String.valueOf(abcMinBitrate))); abcMinFramerate =
		 * AdaptiveBitrateControlSettings.DEFAULT_MIN_FRAMERATE;
		 * //Integer.parseInt
		 * (prefs.getString(PreferenceEnum.PREF_ABC_MIN_FRAMERATE.getValue(),
		 * String.valueOf(abcMinFramerate))); abcFlushBufferThreshold =
		 * AdaptiveBitrateControlSettings.DEFAULT_FLUSH_BUFFER_THRESHOLD;
		 * //Integer
		 * .parseInt(prefs.getString(PreferenceEnum.PREF_ABC_FLUSH_BUFFER_THRESHOLD
		 * .getValue(), String.valueOf(abcFlushBufferThreshold)));
		 */

        switch (Integer.parseInt(prefs.getString(PreferenceEnum.PREF_ABC_MODE.getValue(), String.valueOf(0))))
        {
            case 0:
            {
                abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.DISABLED;
                break;
            }
            case 1:
            {
                abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.QUALITY_DEGRADE;
                break;
            }
            case 2:
            {
                abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.FRAME_DROP;
                break;
            }
            case 3:
            {
                abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.QUALITY_DEGRADE_AND_FRAME_DROP;
                break;
            }
            default:
            {
                abcMode = AdaptiveBitrateControlSettings.AdaptiveBitrateControlMode.DISABLED;
                break;
            }
        }
    }

    public boolean isNetworkAvailable()
    {
        Context context = getApplicationContext();

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}