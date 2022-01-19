# javamod
V3.2

Code Compliance Level: JDK 1.8
Build with openJDK 1.8 update 311
However: run with modern Java (openJDK >11, 64BIT) to gain full
speed - 200+ NNAs are no problem then
Heap Size with JDK8 (default of 256M) not sufficient with bigger mods. Either
set -Xmx1024m parameter or use newer JDK 
 
Supported file types:
Mods (NST, MOD, WOW, XM, STM, S3M, IT, PowerPacker)
SID
MP3 (Files and Streams)
FLAC
APE (ape, apl, mac)
OGG/Vorbis (ogg, oga)
WAV, AU, AIFF
MIDI (MID, RMF, RMI) with SF2 soundfont files
OPL2/3 (ROL, LAA, CMF, DRO, SCI)
Playlists PLS, M3U, M3U8, ZIP, CUE

Known issues:
* reading midi devices in MidiContainer can take a long time on Linux
  as here we can have a whole bunch of devices. Asynchronous loading
  is not an option. Lazy loading does not help as the available MidiDevices 
  must be present when creating the config drop down list for selection.
* Clean up effects - some are for IT only, some for XM only (copy&paste...)
* On Linux gapless audio streams do not work if SourceLine Buffers drain out
* Tray Icon: mouse wheel (volume control) & keyboard shortcuts does not work

Planned:
* WavPack and MusePack support
* MO3 support
* Midi, AdLib with Mods
* read 7z archives
* maybe: follow song with mod files (pattern display)

New in Version 3.2
* FIX: Sustain-Loop and normal Loop not correctly differed in
       BasicModMixer::fitIntoLoops. Resulted in a Devision by Zero, if
       sustain loop present, but no normal loop
* FIX: Impulse Tracker Mods, saved by OpenModPlug, were often identified as
       legacy Modplug Tracker files with then wrong settings in global
       volume and preamp. New ModPlug-Songs were played fare to silent then
* FIX: end of envelopes now correctly identified, count of active NNAs, that 
       are not active anymore, does not explode anymore
* FIX: volume column effects for IT and XM, many fixes on IT mods necessary
* FIX: s3m load volume column as panning, if above 128
* FIX: also stm and s3m (with IT Compatmode OFF) share Porta memories now (like
       IT did already)
* added Fine Midi Macros
* added MidiMacros to XM
* Loading is much faster now. Provided RandomAccessStreamImpl with a buffer so
  reading and seeking is done on the buffer. Using a buffer of 8K currently.
       
New in Version 3.1
* FIX: Pattern/Sample/Instrument dialogs should only be created, if parent
       JDialog is present. Otherwise these will never get destroyed

New in Version 3.0
* FIX: IT set global volume: normalize to 0x80 for *non*-IT Mods
* FIX: MP3 info panel: Label "Duration" is "Bit Rate"
* FIX: Detailed progress bar while export did not restart properly
* FIX: Exported MP3, FLAC, WAV,... in chunks
       -> did not stop exactly at end position but at end of chunk
       -> MP3: counting samples now, recalculating milliseconds from that
          (previously added milliseconds send to buffer, which is not accurate)
* FIX: Export file name generation allowed illegal characters
* UNFIX FIX: pattern break with row index stays in last pattern now, but only if
       "ignore loop" is not checked
* FIX: Playlist numbering - amount of leading zeros
* FIX: Display samples without SampleInterpolationLookAheads
* FIX: SampleInterpolationLookAhead with stereo samples

New in Version 2.9
* FIX: saving a relativized path into a playlist failed on Linux. Here a file
       path is case sensitive!
* FIX: some ROL songs did not load properly
* FIX: Skipping at end of ROL leaded to mixed up mixers
* FIX: loading a playlist of ROLs, last file in list was played, not the
       highlighted one
* FIX: Technical song info for FLAC
* FIX: APE TAG in Footer - loading failed due to wrong file pointer calculation
* FIX: Pattern Delay and Tick Delay for IT fixed - no other effects are replayed
       in this combination. Schism 17.it did not test that
* FIX: pattern break with row index stays in last pattern now, but only if
       "ignore loop" is not checked
* FIX: Loading XMs with no samples for instruments, reset defaults then
* FIX: Many wrong settings with S7x Effects - those need to be temporary!
       (Blue Flame.IT -- thanks for the hint, David)
* Added technical song info for MP3 and APE
* Optimized the ParentDialog setting for the mod info panel
* Supporting OPL2, Dual OPL2 and OPL3 in DRO correctly now. Do not try OPL3 DRO
  with OPL2 Emulation - you will receive an error
* Dual OPL2 and OPL3 work with Virtual Stereo now, no virtual stereo for mono 
  OPL2
* supporting ProTracker STK file now - are like STM

New in Version 2.8
* fixed NullPointer when deleting whole playlist and adding new entries
* Clean Up:
  moved all mod constants from class Helper to new class ModConstants.
  Unused conversion methods in class Helper were documented out
* ModFileInputStream and RandomAccessStream cleaned up
* added OPL3 support
  - added OPL3.java from Robson Cozendey
  - moved it to "de.quippy.opl3" package inside the project
  - enabled reuse by removing statics - subclasses receive "their" own OPL3
    instance
  - added switch statement "case off" in getEnvelope
  - Volume of TomTom, Snare, Cymbals, high hats and base drum raised
    (dirty hack! Needs introspection!)
* added/removed FMOPL 0.37a from Tatsuyuki Satoh (Java-Port)
  - updated to FMOPL 0.72 from Jarek Burczynski (& Tatsuyuki Satoh)
	from the M.A.M.E Project
  - no Y8950 support (yet)
  - no call back support for listeners - not needed here
* added support for rol, laa, cmf, dro, sci by migration of mid.cpp, dro[2].cpp
  and rol.cpp from adplug project + effekter.c from own (very old) ROL project

New in Version 2.7.1
* The OPL3 support was already implemented - but as pure beta yet not activated,
  Release because:
* fixed: nasty NullPointerException when pattern, sample and instrument dialog
  have never been opened yet and switching back from other media file than mod.
  They get initialized than to get the loaded information data, but the 
  ModInfoPanel is not yet added to its JFrame so: no RootPane at that moment.

New in Version 2.7
* added second detail progress bar with wav export or playlist copy
* fixed update of playlist for songname and duration blocking whole UI
  (Things in EventQueue.invokeLater should't take long... learned something)  
* fixed possible null-pointer when closing stream
* fixed possible null-pointer in wave display at stream end
* fixed note fade and key off, when mod has only samples
* fixed creation of two AudioProcessors. One is enough! This resulted in a
  NullPointerException as the outputbuffer was deleted - one Thread was informed,
  the second did not know about this and ran into a nullpointer...
* fixed loading error in S3M / IT parapointers loaded as shorts - results in
  negative seeking values in big songs (>0x8000 is negative as short - and
  stays negative when converted to integer)
* fixed Protracker / XM Fine VolSlide - forgot to check borders
  (was there once, was gone... Oo)
* fixed and optimized NNA: using a silent channel per default is not a good 
  idea. Now also considering instruments being far beyond vol envelope end.
  Copied channel is stopped now.
* fixed / optimized seeking and time measurement of mod files. GUI loading is
  much faster now, because time measurement is faster.
  This has a drawback: seeking without rendering samples will keep sample and 
  envelope pointers at default. That can cause unwanted artifacts - but none 
  were heard yet.
* fixed swingVolume for IT: uses instrument global volume as reference. MPT uses
  the current channel volume
* added resonance and cut off swing, but yet MPT extended instruments are not
  loaded - so far this is dead code 
* stereo samples with S3Ms are supported now
* recognition of openModPlug and ModPlug for IT-Mods added 
* PreAmp and attenuation optimized - considering OMPT
* added song restart for XMs and ProTracker inclusive sanity checks
* added "Loop song" switch to avoid looping with ProTracker and XM Mods due to
  song restart
* Effects-Panel has new switch: useGaplessAudio (I gave up!)
  Linux bug with AudioOutput: if the playback buffer drains (either because of
  "drain" or "flush" or if it runs dry) next input is crippled. See try in
  fixing this in comments below (V2.6)
  This means loading of next piece should never take longer than the buffer has
  sound to play. Use high buffers (>200ms) to avoid this.
  Alternatively deactivate the gapless audio in effect panel
* added display dialogs for mods: pattern data, samples and instruments
  They are reachable by pressing the buttons in the mod info dialog box

New in Version 2.6
* fixed icon size in tray icon
* save selected SA-Meter visual style 
* fixed a playback issue if hardware buffers are smaller than the mixing buffer
* fixed (hopefully) swing gui errors from playlist
* fix for KDE Bugs in Drag&Drop with wrongly encoded URLs
* fix with GaplessAudioStream - it's now really gapless. 
* Fix for issue with JDK >1.8 (tested JDK11, JDK14) on Linux: re-using a 
  SourceDataLine after line.flush() or line.drain() results in crippled sound.
  Environment: OpenJDK(11|14) on OpenSuse 15.(1|2) with PulseAudio. Does not 
  occure under windows
* Midi playback: added support for SF2 SoundFont files
* fixed loading of S3Ms with adlib instruments (do not try to load sampledata!!)
* added a sound hardware information box
* enabled 32-Bit mixing with mods - output 32 bit possible (only if
  sound hardware supports it - is not checked in advance)
* integrated dithering and noise-shift for reduction in bit depth - primary
  noticeable with 8Bit playback
* vast improvement / speed up on time measurement (length of song) - still not
  optimal yet (da geht noch was!)
* linear table support XM and especially in ITs now fully implemented
  (porta 2 note, porta,(extra) fine porta, autovibrato, vibrato) 
* added New Note Actions for IT (YES!) This was not easy in Java as
  copying a channel by re-assigning pointers like in C is not possible here
  We need to copy all relevant data to a separate channel.
* Hence also Duplicate Checks added
* Therefore needed to redevelop all volume envelope handling and to support
  note_cut, note_fade and key_off. Now completely working for IT and XMs 
  with all differences
* Plus rewrite of envelope looping code (differ between IT and XM)
* Resonance Filter is now working for cut off frequencies and resonance values.
  On and Off works as intended
* introduced Z00-Z7F and Z80-ZFF (cutOff and resonance set) for ITs
  Therefore midi makros are now introduced (loaded or set to default)
  No further midi support yet
* Added pitch pan separation
* added loading of song texts from ITs and XMs
* added global volume, mixing volume, panning, random panning/volume variation
* rewrote note and instrument settings completely. FT2.09 && IT2.14 compatible
  (and Protracker & Scream Tracker compatible as well - of course)
* Mixing does not need to complete sampling a whole tick anymore. We mix till
  buffers are full and continue at that point
* Stereo sample support added
* Player_Abuse_Tests (https://github.com/schismtracker/schismtracker/wiki/Player-abuse-tests)
  are all working now --> 100% IT2.14 compatible. At least with those.
* Looping samples needed some re-write (had mistakes with pingpong anyways)
* Interpolation optimized with real effort - however it seems, that the old
  Laissez-faire way was quite enough...
* added Period borders for S3M (Amiga-Limits)
* fixed glissando
* several other small bug fixes in playback
  
New in Version 2.5
* fixed Volume Slide in s3m impulse- and fasttracker - boy what a mess!
* added *2/3 table for Qxy in Fasttracker Mods
* Retrig note memory reseted to zero with Q00 instead of keeping memory
* Retrig starts on tick Zero!
* fixed an issue with SID output settings virtual stereo and 44.1MHz
* changing virtual stereo during playback is fixed

New in Version 2.4
* radio header encoding is translated by ISO-8859-1 and not UTF-8
* radio stream description is now displayed as well
* new "Export selected files to wav" - is usefull if e.g. a cue sheet with one
  flac file is played and the single songs are needed (come as wav then)
  If no files are selected, the whole playlist is exported
* copy files in playlist order moved here
* Export to wav is renamed to Export while playing and intended for
  radio streams
* corrected time index with mod files. Estimation of duration is more correct
  now
* switched repositories from CVS to SVN

New in Version 2.3
* now also https radio streams are supported
* initially wrong positioned icy-metaint points in streams are now supported
* relative file names in playlists at saving time to location of playlist

New in Version 2.3
* Pattern Pos Jump did not work if infinite loops are disabled
  Furthermore Pattern Jump Position set is channel specific
* Streaming of OGG Streams works - sort of...
* Saving radio / Internet stream playlists works now
* "All Playable files" is on top of selection now
* TextAreaFont and DialogFont are not set as statics any more to prevent errors
  when UI is not used and needed in server like environments
* Optimized load of URLs from playlists - no "re-location" for http-files - these
  are always absolute
* introducing HttpResource for web-Radio - this supports also 302, moved

New in Version 2.2
* added internal mod buffer length in config dialog
* Fixed a bug in ID3v2 Tag decoding with Strings and missing encoding
* MP3 playback optimized, dropping zero padded samples
* Fixed a Bug in central URL creation routine
* Fixed playlist write conversion error (ISO-8859-1 - not UTF-8)
* Added m3u8 for UTF-8 Playlists
* reading pls files can handle empty lines and "NumberOfEntries" at end of file
* drag&drop into playlist calculation of index fixed
* Midi: a bug in playback was fixed. (simulation of played samples, 
  correct close of all sound devices)
* added support for SQSH packed mod files
* fixed drawing issues in peak meters
* added clipping to Graphical EQ calculations
* removing illegal characters from Strings (<0x20)
* updating streaming of mp3 radio streams

New in Version 2.1
* copy all files in a playlist to a certain directory - in playlist order
  sometimes useful when creating music collections on USB sticks
* deletion of elements in the playlists resets index
* changed to UTF-8 - encoding for playlists is set to ISO-8859-1
* dropped files into the playlist were not playable anymore - fixed
* fixed mouse consume event when double clicks center sliders
* fixed a runtime exception with corrupt mp3s
* fixed a bug (AGAIN *grrr*) with privileged access to "user.home" in applets
* changed logger
* "Code too large" Error with NetBeans and IWave6581 and IWave8580
  --> Eclipse uses its own compiler and will not create that error
  --> JavaC and NetBeans nevertheless will
  --> arrays are now loaded as a resource from files
* SampleOffset now playes only with a note given

New in Version 2.0
* Needs Java 6 now!!!
* Tray Icon support added (--> JDK6)
* Seeking speedup with FLAC, APE and wav
* XM/IT: 
  * added HighSampleOffset
  * added panbrello (was read with IT, effect never build...)
  * added fine pattern delay (# of ticks) to XM
    and fixed a potential bug here...
  * fixed IT_AMIGA_TABLE: one octave too high
  * fixed Pattern Delay effects
* Playists: 
  * Saving playlist as m3u as default works now
  * still some problems with the caret when an entry was selected or moved
  * added Support for CUE-Sheets (read and write)
* PitchShift: overSampling and FrameSize are editable
* Flac uses vorbis comment now
* MP3 info panel switches to ID3v2-Tab if id3v2-Tag exists
* fixed the scrolly-problem, that chars "pop up" rightmost but not invisible
* fixed a problem with converting local files to URLs
* fixed a null byte flaw with C++ Strings and Unicode
* fixed a thread problem when starting javamod ui with a playlist as a parameter
* fixed a problem with converting absolute path entries in playlists to 
  a relative path regarding the saving place of the playlist
* fixed a problem with alt-tab usage that only popped up the playlist or
  info panel but not the main dialog
* fixed a potential bug with comparing two URLs (does a dns lookup)

New in Version 1.9.4.7
* Export of whole playlist: if a playlist is present, all files of this list
  will be exported
* reorganized the audio processor for DSP-visual effects. Samples only need to
  be send to the SoundOutputStream - audio processor converts by itself
* visual: added more colors to the meters
* DSP: added the graphics equalizer
* DSP: added the pitch shift (change the pitch without tempo change)
* Modplayer: loops can now be skipped (ignored, fadeout, keep endless loop)
* Jump pattern effect did not work... (it has never so far actually...)
* CAUTION: CHANGED COMMANDLINE AND APPLET VALUE FOR -l PARAMETER TO 0, 1, 2

New in Version 1.9.4.6
* many minor errors
* SID Config: 
  * Filter setting had no affect
  * added a scrollpane to enable scrolling
  * all elements can be changed during playback
  * changing virtual stereo does work
* play and stop with SIDs does not automaticly increase song number anymore
* changed listeners of checkboxes from ChangeListeners to ItemListeners
* fixed several bugs with the playlist gui
  * Editing entries produced encoded URL-Strings
  * Entries not found were set to NULL
  * saving playlists with NULL-Entries did not work
  * synchronizing methods to get parallel changes handled
  * active element is always visable now
  * moving entries in large lists works now
  * marking and demarking (i.e. ctrl-A) does not move the visiable rect anymore

New in Version 1.9.4.5
* added Playlist Repeat for GUI, commandline and applet. Use parameter "j" to set state
* Config Panel is now using a tab panel to make all configs accessible
* sid has a config panel now

New in Version 1.9.4.4
* Applets loading of Mods did not work any more
* Applets create security exception with Java 6 - only optimization of error handling
* initial Volume with applets or command line will work
* balance is now also set after loading (in GUI, like volume)
* midis with capturing will stop (draining a line from which is never read will lead into an endless loop!)

New in Version 1.9.4.3
* The applet JavaModApplet_OldSchoolDemo moved to a separate project
* added public methods to the applet to allow applet control via javascript
  see also the applet test page on quippy.de
* saving a new playlist did not work anymore
* export to wave did not change name to lokal file pattern (utf-8)
* Adding files into a cleaned playlist created a false index beeing too big
* Calculation the length of wave files which need conversion did not work
* key support in playlists added
* OGG did not work any more - correcting CRC determination back to original
* saving Playlist changes filenames relativ to playlist location
* Dropping a folder into playlist will collect all Files getting found from there on
* working on MIDI capturing - hope it helps...

New in Version 1.9.4.2
* after correcting the file and url handling with applets, the local
  files and playlists did not work any more... fixed
  --> NEVER HTML-encode your playlists
  --> it was found out that with streaming files to the applet
      the webserver needs to provide a MIME-Type
* new Applet: de.quippy.javamod.main.applet.JavaModApplet_OldSchoolDemo - try it out ;)
* reconstructing Applet class hierarchy 

New in Version 1.9.4.1
* File-Handling with applets is somewhat difficult. The algorithem guessing the
  correct filename or url was improved. Relative URL / local files are allowed now
  and tried to be found using the URL of the playlist.
* masking of illegal character in URLs (like white spaces) is done automatically
* mp3-streaming was recognized through HTTP / against FILE protocol. That is not
  correct - streaming is now recognized by contentlength==-1 (this might 
  also fail though...)

New in Version 1.9.4.0
* After installing ape support, FLAC as the more widly spreaded codec needs 
  to be supported as well
* Updated About-Dialog, mentioning all decoders and versions used
* Implemented ZIP-Support by reorganising the IO classes
* ZIP-Files are handled as playlists
* A Path like "C:\Dir\Zipfile.zip\path\to\file.mp3 is now supported
* Bug fix with XMs/ITs using empty noteIndex or SampleIndex-Arrays
* GUI for PlayList implemented
  - Droping files into playlist (at a position)
  - moving files around
  - reading file infos through thread (updating lists)
  - saving pls or m3u
  - editing of entries
* Everything is handled through a playlist (even single files)
* enabled multi file selection in open dialog
* #EXTINF and PLS: Infos are now read
* Several BugFixes (due to findbug)
* Version history dialog has now correct size again
* Drag&Drop 
  - supports filelists as a playlist now
  - Dropped Playlists are "unpacked"
  - even combinations of Files and Playlists can be dropped, result is a new Playlist
  - added Dropspace into the playlist
* implemented a GaplessSoundOutputStream so pieces with same output quality will
  get rendered directly into the same line - this results in gapless rendering
  as appreciated with e.g. audio books

New in Version 1.9.3.5
* as stumbling over an ape decoder I decided to implement that too
  APE-Decoding is slow - very slow! You will see while seeking.
* added command line option for the GUI - can now instantly load a file or
  playlist file

New in Version 1.9.3.4
* improvement of midi playback
* export to wav for midi implemented (port selection is implemented)
  --> this all will only work with one sound card installed or lucky selections
  --> full beta state!
* added support for RMI-Files (RIFF Midi)
* setup dialog will now refresh between changes at playback
* playback while wav-export is supported (usefull with streams) - the user will
  now be asked if playback during wav export is wished
* added OGG / Vorbis-Support (fully implemented...)
* fixed error with unloadable files - they are now skipped in playlist
* fixed that "all playable files" also contains playlists

New in Version 1.9.3.3
* Bugfix with PitchEnvelopes: are used as filter- or pitchenvelopes. Reading
  flag now.
* Reconstructing volume handling
* added volume ramping
* Bug: songarrangement contains illegal pattern num - are now dropped during
  loading of modules
  
New in Version 1.9.3.2
* Bugfix with XMs: 
  - Envelopes need to be processed at new rows 
  - and the fade out volume hits only with active envelope; otherwise note cut
* Applet got the seekbar included

New in Version 1.9.3.1
* Bugfix when loading certain STMs - ID was unknown. Fix with a fall back
  strategy now - loading with all known loaders - the one who can load without
  error winns - if no loader can load, mod is corrupt.

New in Version 1.9.3
* Moving packages for modplayback to de.quippy.javamod.mod.*
* Bugfixing of code inserted with 1.9.2 
  - Problem with NoteDelay after insertion of wrong code
  - bad sound with instruments reaching volume 0 (VolSlide)

New in Version 1.9.2
* super.paintComponent() in MeterPanelBase is not necessary - speedup gui repaint
* generating local file names from playlist files improved
* Threads are now final
* SerialVersionUID inserted where needed
* Non used Variables or methods commented
* MIDIs are now seekable
* MP3s are now seekable
* Faster Seeking of Mods enabled
* WAV-Playback - Seekbar fixed
* WAV-Playback - Peek Display synchronized
* After reading a non existent Wave-File, the SeekBarPanel ran into an Exception
* Peeks: ArrayIndexOutOfBounds fixed
* With ITs:
  * Loading of compressed IT-Samples did not alwayes work - fixed
  * Added Envelope Processing (AutoVibrato/Volume/Panning) for IT-Mods
  * NoteCut and NoteOff are Different - Now differing (S3Ms and ITs)
  * Resonance-Filter included
  * NNA prepared - but big bug - so far disabled
  * maxChannelCount with ITs was 1 less
  * and a lot more with ITs

New in Version 1.9.1
* PowerPacker 2.0-Files are now supported
* Mod-Loader is selected by header
* file extension for mods are only needed to differ between mods and mp3s or others
* WOW-Mods are now reckognized correctly
* preceeding "extension" (mod.modfile) are loaded correctly
* IT-Instrument decompression "EOF-Exception" fixed
* IT Instruments Looping fixed
* Version-Checking now realy checks version
* Version-History now in javamod_version.txt
* Displaying version history as menu or if version is newer

New in Version 1.9
* SID-Support
* displaying time code of mods (can take some time with ITs or long pieces)
* jump in wavs and mods
* no jumping in mp3s :(
