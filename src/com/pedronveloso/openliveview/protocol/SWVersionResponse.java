package com.pedronveloso.openliveview.protocol;

import com.pedronveloso.openliveview.Utils.Constants;

import java.io.DataInputStream;
import java.io.IOException;

public class SWVersionResponse extends Response {

	private String mVersion;
	
	@Override
	protected void readPayload(DataInputStream input, int payloadLength) throws IOException {
		byte[] buffer = new byte[payloadLength];
		input.read(buffer);
		mVersion = Utils.getString(buffer);
	}

	public String getVersion() {
		return mVersion;
	}

}
