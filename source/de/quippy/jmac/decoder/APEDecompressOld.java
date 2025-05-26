/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package de.quippy.jmac.decoder;

import java.io.IOException;
import java.util.Arrays;

import de.quippy.jmac.info.APEFileInfo;
import de.quippy.jmac.info.APEInfo;
import de.quippy.jmac.info.APETag;
import de.quippy.jmac.info.WaveFormat;
import de.quippy.jmac.info.WaveHeader;
import de.quippy.jmac.tools.ByteBuffer;
import de.quippy.jmac.tools.File;
import de.quippy.jmac.tools.JMACException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APEDecompressOld extends IAPEDecompress {
    public APEDecompressOld(final APEInfo pAPEInfo) {
        this(pAPEInfo, -1, -1);
    }

    public APEDecompressOld(final APEInfo pAPEInfo, final int nStartBlock) {
        this(pAPEInfo, nStartBlock, -1);
    }

    public APEDecompressOld(final APEInfo pAPEInfo, final int nStartBlock, final int nFinishBlock) {
        // open / analyze the file
        m_spAPEInfo = pAPEInfo;

        // version check (this implementation only works with 3.92 and earlier files)
        if (getApeInfoFileVersion() > 3920)
            throw new JMACException("Wrong Version");

        // create the buffer
        m_nBlockAlign = getApeInfoBlockAlign();

        // initialize other stuff
        m_nBufferTail = 0;
        m_bDecompressorInitialized = false;
        m_nCurrentFrame = 0;
        m_nCurrentBlock = 0;

        // set the "real" start and finish blocks
        m_nStartBlock = (nStartBlock < 0) ? 0 : Math.min(nStartBlock, getApeInfoTotalBlocks());
        m_nFinishBlock = (nFinishBlock < 0) ? getApeInfoTotalBlocks() : Math.min(nFinishBlock, getApeInfoTotalBlocks());
        m_bIsRanged = (m_nStartBlock != 0) || (m_nFinishBlock != getApeInfoTotalBlocks());
    }

    @Override
	public int GetData(final byte[] pBuffer, int nBlocks) throws IOException {
        InitializeDecompressor();

        // cap
        final int nBlocksUntilFinish = m_nFinishBlock - m_nCurrentBlock;
        nBlocks = Math.min(nBlocks, nBlocksUntilFinish);

        int nBlocksRetrieved = 0;

        //fulfill as much of the request as possible
        final int nTotalBytesNeeded = nBlocks * m_nBlockAlign;
        int nBytesLeft = nTotalBytesNeeded;
        int nBlocksDecoded = 1;

        while (nBytesLeft > 0 && nBlocksDecoded > 0) {
            //empty the buffer
            final int nBytesAvailable = m_nBufferTail;
            final int nIntialBytes = Math.min(nBytesLeft, nBytesAvailable);
            if (nIntialBytes > 0) {
                System.arraycopy(m_spBuffer, 0, pBuffer, nTotalBytesNeeded - nBytesLeft, nIntialBytes);

                if ((m_nBufferTail - nIntialBytes) > 0)
                    System.arraycopy(m_spBuffer, nIntialBytes, m_spBuffer, 0, m_nBufferTail - nIntialBytes);

                nBytesLeft -= nIntialBytes;
                m_nBufferTail -= nIntialBytes;

            }

            //decode more
            if (nBytesLeft > 0) {
                output.reset(m_spBuffer, m_nBufferTail);
                nBlocksDecoded = m_UnMAC.DecompressFrame(output, m_nCurrentFrame++);
                m_nBufferTail += (nBlocksDecoded * m_nBlockAlign);
            }
        }

        nBlocksRetrieved = (nTotalBytesNeeded - nBytesLeft) / m_nBlockAlign;

        // update the position
        m_nCurrentBlock += nBlocksRetrieved;

        return nBlocksRetrieved;
    }

    @Override
	public void Seek(int nBlockOffset) throws IOException {
        InitializeDecompressor();

        // use the offset
        nBlockOffset += m_nStartBlock;

        // cap (to prevent seeking too far)
        if (nBlockOffset >= m_nFinishBlock)
            nBlockOffset = m_nFinishBlock - 1;
        if (nBlockOffset < m_nStartBlock)
            nBlockOffset = m_nStartBlock;

        // flush the buffer
        m_nBufferTail = 0;

        // seek to the perfect location
        final int nBaseFrame = nBlockOffset / getApeInfoBlocksPerFrame();
        final int nBlocksToSkip = nBlockOffset % getApeInfoBlocksPerFrame();
        final int nBytesToSkip = nBlocksToSkip * m_nBlockAlign;

        // skip necessary blocks
        final int nMaximumDecompressedFrameBytes = m_nBlockAlign * getApeInfoBlocksPerFrame();
        final byte[] pTempBuffer = new byte[nMaximumDecompressedFrameBytes + 16];
        Arrays.fill(pTempBuffer, (byte) 0);

        m_nCurrentFrame = nBaseFrame;

        output.reset(pTempBuffer);
        final int nBlocksDecoded = m_UnMAC.DecompressFrame(output, m_nCurrentFrame++);

        if (nBlocksDecoded == -1)
            throw new JMACException("Error While Decoding");

        final int nBytesToKeep = (nBlocksDecoded * m_nBlockAlign) - nBytesToSkip;
        System.arraycopy(pTempBuffer, nBytesToSkip, m_spBuffer, m_nBufferTail, nBytesToKeep);
        m_nBufferTail += nBytesToKeep;

        m_nCurrentBlock = nBlockOffset;
    }

    // buffer
    protected byte[] m_spBuffer;
    protected int m_nBufferTail;
    protected ByteBuffer output = new ByteBuffer();

    // file info
    protected int m_nBlockAlign;
    protected int m_nCurrentFrame;

    // start / finish information
    protected int m_nStartBlock;
    protected int m_nFinishBlock;
    protected int m_nCurrentBlock;
    protected boolean m_bIsRanged;

    // decoding tools
    protected UnMAC m_UnMAC = new UnMAC();
    protected APEInfo m_spAPEInfo;

    protected boolean m_bDecompressorInitialized;

    protected void InitializeDecompressor() throws IOException {
        // check if we have anything to do
        if (m_bDecompressorInitialized)
            return;

        // initialize the core
        m_UnMAC.Initialize(this);

        final int nMaximumDecompressedFrameBytes = m_nBlockAlign * getApeInfoBlocksPerFrame();
        final int nTotalBufferBytes = Math.max(65536, (nMaximumDecompressedFrameBytes + 16) * 2);
        m_spBuffer = new byte[nTotalBufferBytes];

        // update the initialized flag
        m_bDecompressorInitialized = true;

        // seek to the beginning
        Seek(0);
    }

    @Override
	public int getApeInfoDecompressCurrentBlock() {
        return m_nCurrentBlock - m_nStartBlock;
    }

    @Override
	public int getApeInfoDecompressCurrentMS() {
        final int nSampleRate = m_spAPEInfo.getApeInfoSampleRate();
        if (nSampleRate > 0)
            return (int) ((m_nCurrentBlock * 1000L) / nSampleRate);
        return 0;
    }

    @Override
	public int getApeInfoDecompressTotalBlocks() {
        return m_nFinishBlock - m_nStartBlock;
    }

    @Override
	public int getApeInfoDecompressLengthMS() {
        final int nSampleRate = m_spAPEInfo.getApeInfoSampleRate();
        if (nSampleRate > 0)
            return (int) (((m_nFinishBlock - m_nStartBlock) * 1000L) / nSampleRate);
        return 0;
    }

    @Override
	public int getApeInfoDecompressCurrentBitRate() throws IOException {
        return m_spAPEInfo.getApeInfoFrameBitrate(m_nCurrentFrame);
    }

    @Override
	public int getApeInfoDecompressAverageBitrate() throws IOException {
        if (m_bIsRanged) {
            // figure the frame range
            final int nBlocksPerFrame = m_spAPEInfo.getApeInfoBlocksPerFrame();
            final int nStartFrame = m_nStartBlock / nBlocksPerFrame;
            final int nFinishFrame = (m_nFinishBlock + nBlocksPerFrame - 1) / nBlocksPerFrame;

            // get the number of bytes in the first and last frame
            int nTotalBytes = (m_spAPEInfo.getApeInfoFrameBytes(nStartFrame) * (m_nStartBlock % nBlocksPerFrame)) / nBlocksPerFrame;
            if (nFinishFrame != nStartFrame)
                nTotalBytes += (m_spAPEInfo.getApeInfoFrameBytes(nFinishFrame) * (m_nFinishBlock % nBlocksPerFrame)) / nBlocksPerFrame;

            // get the number of bytes in between
            final int nTotalFrames = m_spAPEInfo.getApeInfoTotalFrames();
            for (int nFrame = nStartFrame + 1; (nFrame < nFinishFrame) && (nFrame < nTotalFrames); nFrame++)
                nTotalBytes += m_spAPEInfo.getApeInfoFrameBytes(nFrame);

            // figure the bitrate
            final int nTotalMS = (int) (((m_nFinishBlock - m_nStartBlock) * 1000L) / m_spAPEInfo.getApeInfoSampleRate());
            if (nTotalMS != 0)
                return (nTotalBytes * 8) / nTotalMS;
        } else {
            return m_spAPEInfo.getApeInfoAverageBitrate();
        }
        return 0;
    }

    @Override
	public int getApeInfoWavHeaderBytes() {
        if (m_bIsRanged)
            return WaveHeader.WAVE_HEADER_BYTES;
        return m_spAPEInfo.getApeInfoWavHeaderBytes();
    }

    @Override
	public byte[] getApeInfoWavHeaderData(final int nMaxBytes) {
        if (m_bIsRanged) {
            if (WaveHeader.WAVE_HEADER_BYTES > nMaxBytes)
                return null;
            else {
                final WaveFormat wfeFormat = m_spAPEInfo.getApeInfoWaveFormatEx();
                final WaveHeader WAVHeader = new WaveHeader();
                WaveHeader.FillWaveHeader(WAVHeader, (m_nFinishBlock - m_nStartBlock) * m_spAPEInfo.getApeInfoBlockAlign(), wfeFormat, 0);
                return WAVHeader.write();
            }
        }
        return m_spAPEInfo.getApeInfoWavHeaderData(nMaxBytes);
    }

    @Override
	public int getApeInfoWavTerminatingBytes() {
        if (m_bIsRanged)
            return 0;
        else
            return m_spAPEInfo.getApeInfoWavTerminatingBytes();
    }

    @Override
	public byte[] getApeInfoWavTerminatingData(final int nMaxBytes) throws IOException {
        if (m_bIsRanged)
            return null;
        else
            return m_spAPEInfo.getApeInfoWavTerminatingData(nMaxBytes);
    }

    @Override
	public WaveFormat getApeInfoWaveFormatEx() {
        return m_spAPEInfo.getApeInfoWaveFormatEx();
    }

    @Override
	public File getApeInfoIoSource() {
        return m_spAPEInfo.getApeInfoIoSource();
    }

    @Override
	public int getApeInfoBlocksPerFrame() {
        return m_spAPEInfo.getApeInfoBlocksPerFrame();
    }

    @Override
	public int getApeInfoFileVersion() {
        return m_spAPEInfo.getApeInfoFileVersion();
    }

    @Override
	public int getApeInfoCompressionLevel() {
        return m_spAPEInfo.getApeInfoCompressionLevel();
    }

    @Override
	public int getApeInfoFormatFlags() {
        return m_spAPEInfo.getApeInfoFormatFlags();
    }

    @Override
	public int getApeInfoSampleRate() {
        return m_spAPEInfo.getApeInfoSampleRate();
    }

    @Override
	public int getApeInfoBitsPerSample() {
        return m_spAPEInfo.getApeInfoBitsPerSample();
    }

    @Override
	public int getApeInfoBytesPerSample() {
        return m_spAPEInfo.getApeInfoBytesPerSample();
    }

    @Override
	public int getApeInfoChannels() {
        return m_spAPEInfo.getApeInfoChannels();
    }

    @Override
	public int getApeInfoBlockAlign() {
        return m_spAPEInfo.getApeInfoBlockAlign();
    }

    @Override
	public int getApeInfoFinalFrameBlocks() {
        return m_spAPEInfo.getApeInfoFinalFrameBlocks();
    }

    @Override
	public int getApeInfoTotalFrames() {
        return m_spAPEInfo.getApeInfoTotalFrames();
    }

    @Override
	public int getApeInfoWavDataBytes() {
        return m_spAPEInfo.getApeInfoWavDataBytes();
    }

    @Override
	public int getApeInfoWavTotalBytes() {
        return m_spAPEInfo.getApeInfoWavTotalBytes();
    }

    @Override
	public int getApeInfoApeTotalBytes() {
        return m_spAPEInfo.getApeInfoApeTotalBytes();
    }

    @Override
	public int getApeInfoTotalBlocks() {
        return m_spAPEInfo.getApeInfoTotalBlocks();
    }

    @Override
	public int getApeInfoLengthMs() {
        return m_spAPEInfo.getApeInfoLengthMs();
    }

    @Override
	public int getApeInfoAverageBitrate() {
        return m_spAPEInfo.getApeInfoAverageBitrate();
    }

    @Override
	public int getApeInfoSeekByte(final int nFrame) {
        return m_spAPEInfo.getApeInfoSeekByte(nFrame);
    }

    @Override
	public int getApeInfoFrameBytes(final int nFrame) throws IOException {
        return m_spAPEInfo.getApeInfoFrameBytes(nFrame);
    }

    @Override
	public int getApeInfoFrameBlocks(final int nFrame) {
        return m_spAPEInfo.getApeInfoFrameBlocks(nFrame);
    }

    @Override
	public int getApeInfoFrameBitrate(final int nFrame) throws IOException {
        return m_spAPEInfo.getApeInfoFrameBitrate(nFrame);
    }

    @Override
	public int getApeInfoDecompressedBitrate() {
        return m_spAPEInfo.getApeInfoDecompressedBitrate();
    }

    @Override
	public int getApeInfoPeakLevel() {
        return m_spAPEInfo.getApeInfoPeakLevel();
    }

    @Override
	public int getApeInfoSeekBit(final int nFrame) {
        return m_spAPEInfo.getApeInfoSeekBit(nFrame);
    }

    @Override
	public APETag getApeInfoTag() {
        return m_spAPEInfo.getApeInfoTag();
    }

    @Override
	public APEFileInfo getApeInfoInternalInfo() {
        return m_spAPEInfo.getApeInfoInternalInfo();
    }
}
