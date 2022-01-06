package de.quippy.sidplay.libsidplay.components.sidtune;

public class SmartPtr_sidtt extends SmartPtrBase_sidtt {

	public SmartPtr_sidtt(short[] buffer, int bufferLen, boolean bufOwner) {
		super(buffer, bufferLen, bufOwner);
	}

	public SmartPtr_sidtt() {
		super(null, 0, false);
	}

	public SmartPtr_sidtt(short[] buffer, int fileOffset, int bufferLen) {
		super(buffer, bufferLen - fileOffset, false);
		pBufCurrent = fileOffset;
	}

	public void setBuffer(short[] buffer, int bufferLen) {
		if (bufferLen >= 1) {
			pBufCurrent = 0;
			bufEnd = bufferLen;
			bufLen = bufferLen;
			status = true;
		} else {
			pBufCurrent = bufEnd = 0;
			bufLen = 0;
			status = false;
		}
	}
}
