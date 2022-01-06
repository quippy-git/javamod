package de.quippy.sidplay.libsidplay.components.sidtune;

public class SmartPtrBase_sidtt {

	public SmartPtrBase_sidtt(short[] /* T* */buffer,
			int /* ulint_smartpt */bufferLen, boolean bufOwner)
	{
		dummy = (0);
		doFree = bufOwner;
		if (bufferLen >= 1) {
			bufBegin = buffer;
			pBufCurrent = 0;
			bufEnd = bufferLen;
			bufLen = bufferLen;
			status = true;
		} else {
			bufBegin = null;
			pBufCurrent = (bufEnd = 0);
			bufLen = 0;
			status = false;
		}
	}

	public short[] tellBegin() {
		return bufBegin;
	}

	public int /* ulint_smartpt */tellLength() {
		return bufLen;
	}

	public int /* ulint_smartpt */tellPos() {
		return (int /* ulint_smartpt */) (pBufCurrent);
	}

	public boolean checkIndex(int /* ulint_smartpt */index) {
		return ((pBufCurrent + index) < bufEnd);
	}

	public boolean reset() {
		if (bufLen >= 1) {
			pBufCurrent = 0;
			return (status = true);
		} else {
			return (status = false);
		}
	}

	public boolean good() {
		return (pBufCurrent < bufEnd);
	}

	public boolean fail() {
		return (pBufCurrent == bufEnd);
	}

	public void operatorPlusPlus() {
		if (good()) {
			pBufCurrent++;
		} else {
			status = false;
		}
	}

	public void operatorMinusMinus() {
		if (!fail()) {
			pBufCurrent--;
		} else {
			status = false;
		}
	}

	public void operatorPlusGleich(int /* ulint_smartpt */offset) {
		if (checkIndex(offset)) {
			pBufCurrent += offset;
		} else {
			status = false;
		}
	}

	public void operatorMinusGleich(int /* ulint_smartpt */offset) {
		if ((pBufCurrent - offset) >= 0) {
			pBufCurrent -= offset;
		} else {
			status = false;
		}
	}

	public short operatorMal() {
		if (good()) {
			return bufBegin[pBufCurrent];
		} else {
			status = false;
			return dummy;
		}
	}

	public short operatorAt(int /* ulint_smartpt */index) {// &
		if (checkIndex(index)) {
			return bufBegin[pBufCurrent + index];
		} else {
			status = false;
			return dummy;
		}
	}

	public boolean operatorBool() {
		return status;
	}

	protected short[] bufBegin;

	protected int bufEnd;

	protected int pBufCurrent;

	protected int /* ulint_smartpt */bufLen;

	protected boolean status;

	protected boolean doFree;

	protected short dummy;
}
