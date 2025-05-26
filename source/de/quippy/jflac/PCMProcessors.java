/*
 * Created on Jun 28, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.quippy.jflac;

import java.util.HashSet;

import de.quippy.jflac.metadata.StreamInfo;
import de.quippy.jflac.util.ByteData;


/**
 * Class to handle PCM processors.
 * @author kc7bfi
 */
class PCMProcessors implements PCMProcessor {
    private final HashSet<PCMProcessor> pcmProcessors = new HashSet<>();

    /**
     * Add a PCM processor.
     * @param processor  The processor listener to add
     */
    public void addPCMProcessor(final PCMProcessor processor) {
        synchronized (pcmProcessors) {
            pcmProcessors.add(processor);
        }
    }

    /**
     * Remove a PCM processor.
     * @param processor  The processor listener to remove
     */
    public void removePCMProcessor(final PCMProcessor processor) {
        synchronized (pcmProcessors) {
            pcmProcessors.remove(processor);
        }
    }

    /**
     * Process the StreamInfo block.
     * @param info the StreamInfo block
     * @see de.quippy.jflac.PCMProcessor#processStreamInfo(de.quippy.jflac.metadata.StreamInfo)
     */
    @Override
	public void processStreamInfo(final StreamInfo info) {
        synchronized (pcmProcessors) {
            for (final PCMProcessor processor : pcmProcessors)
			{
                processor.processStreamInfo(info);
            }
        }
    }

    /**
     * Process the decoded PCM bytes.
     * @param pcm The decoded PCM data
     * @see de.quippy.jflac.PCMProcessor#processPCM(de.quippy.jflac.util.ByteSpace)
     */
    @Override
	public void processPCM(final ByteData pcm) {
        synchronized (pcmProcessors) {
            for (final PCMProcessor processor : pcmProcessors)
			{
                processor.processPCM(pcm);
            }
        }
    }

}
