/*
 * Created on Jun 28, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.quippy.jflac;

import java.util.HashSet;

import de.quippy.jflac.frame.Frame;
import de.quippy.jflac.metadata.Metadata;


/**
 * Class to handle frame listeners.
 * @author kc7bfi
 */
class FrameListeners implements FrameListener {
    private final HashSet<FrameListener> frameListeners = new HashSet<>();

    /**
     * Add a frame listener.
     * @param listener  The frame listener to add
     */
    public void addFrameListener(final FrameListener listener) {
        synchronized (frameListeners) {
            frameListeners.add(listener);
        }
    }

    /**
     * Remove a frame listener.
     * @param listener  The frame listener to remove
     */
    public void removeFrameListener(final FrameListener listener) {
        synchronized (frameListeners) {
            frameListeners.remove(listener);
        }
    }

    /**
     * Process metadata records.
     * @param metadata the metadata block
     * @see de.quippy.jflac.FrameListener#processMetadata(de.quippy.jflac.metadata.MetadataBase)
     */
    @Override
	public void processMetadata(final Metadata metadata) {
        synchronized (frameListeners) {
            for (final FrameListener listener : frameListeners)
			{
                listener.processMetadata(metadata);
            }
        }
    }

    /**
     * Process data frames.
     * @param frame the data frame
     * @see de.quippy.jflac.FrameListener#processFrame(de.quippy.jflac.frame.Frame)
     */
    @Override
	public void processFrame(final Frame frame) {
        synchronized (frameListeners) {
            for (final FrameListener listener : frameListeners)
			{
                listener.processFrame(frame);
            }
        }
    }

    /**
     * Called for each frame error detected.
     * @param msg   The error message
     * @see de.quippy.jflac.FrameListener#processError(java.lang.String)
     */
    @Override
	public void processError(final String msg) {
        synchronized (frameListeners) {
            for (final FrameListener listener : frameListeners)
			{
                listener.processError(msg);
            }
        }
    }

}
