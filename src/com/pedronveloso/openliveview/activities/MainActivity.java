package com.pedronveloso.openliveview.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.nio.ByteOrder;
import java.util.Set;

import com.pedronveloso.openliveview.R;
import com.pedronveloso.openliveview.Utils.Constants;
import com.pedronveloso.openliveview.protocol.*;
import com.pedronveloso.openliveview.server.BtServer;

public class MainActivity extends Activity
                          implements BtServer.Callback, OnClickListener
                          
{
    BluetoothAdapter mBluetoothAdapter;
    TextView output;
    Button btnVibrate;
    Button btnLED;
    
    public void addToOutput(final String line){
    	Log.d(Constants.LOG_TAG, line);
    	runOnUiThread(new Runnable() {
			
			public void run() {
				output.setText(output.getText()+"\n"+line);	
			}
		});
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);

        
        output = (TextView) findViewById(R.id.tv_output);
        
        btnVibrate = (Button)findViewById(R.id.btnVibrate);
        btnVibrate.setOnClickListener(this);
        btnLED = (Button)findViewById(R.id.btnLED);
        btnLED.setOnClickListener(this);
        
        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
            addToOutput("Current platform byte order is: BigEndian");
        else
            addToOutput("Current platform byte order is: LittleEndian");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(Constants.LOG_TAG,"does not support bluetooth devices");
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(Constants.LOG_TAG, "bluetooth not enabled");
            finish();
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice liveView = null;
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if ("LiveView".equals(device.getName())) {
                	addToOutput(device.getName() + " : " + device.getAddress());
                	liveView = device;
                }
            }
        }

        if (liveView != null) {
        	BtServer.instance().setContext(this);
        	BtServer.instance().setCallback(this);
        	BtServer.instance().start(liveView);        	        	
        }

    }

    public void isReadyChanged(boolean isReady) {
    	if (isReady)
    		mBluetoothAdapter.cancelDiscovery();
    	btnVibrate.setEnabled(isReady);
    	btnLED.setEnabled(isReady);
    	addToOutput("IsReadyChanged: "+isReady);
    }
	
    public void handleResponse(Response aResponse) {
		if (aResponse instanceof ScreenPropertiesResponse) {
			addToOutput("Protocol Version: "+((ScreenPropertiesResponse)aResponse).getProtocolVersion());
		} else if (aResponse instanceof SWVersionResponse) {
			addToOutput("SW Version: "+((SWVersionResponse)aResponse).getVersion());
		} else if (aResponse instanceof UnknownResponse) {
			addToOutput("Unknown Response: "+((UnknownResponse)aResponse).getMsgId());
		} else
			addToOutput("handling: "+ aResponse.getClass().getSimpleName());
	}

    @Override
    protected void onDestroy() {
    	BtServer.instance().stop();
    	super.onDestroy();
    }

	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.btnVibrate) {
			BtServer.instance().write(new VibrateRequest((short)0, (short)500));
		} else if (id == R.id.btnLED) {
			BtServer.instance().write(new LEDRequest(Color.RED, (short)0, (short)500));
		}
		
	}


}