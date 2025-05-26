/*
 * @(#) MainForm.java
 *
 * Created on 22.06.2006 by Daniel Becker
 *
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
package de.quippy.javamod.main.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import de.quippy.javamod.io.GaplessSoundOutputStreamImpl;
import de.quippy.javamod.io.SoundOutputStream;
import de.quippy.javamod.io.SoundOutputStreamImpl;
import de.quippy.javamod.main.gui.components.DoubleProgressDialog;
import de.quippy.javamod.main.gui.components.LEDScrollPanel;
import de.quippy.javamod.main.gui.components.ProgressDialog;
import de.quippy.javamod.main.gui.components.RoundSlider;
import de.quippy.javamod.main.gui.components.SAMeterPanel;
import de.quippy.javamod.main.gui.components.SeekBarPanel;
import de.quippy.javamod.main.gui.components.SeekBarPanelListener;
import de.quippy.javamod.main.gui.components.SimpleProgressDialog;
import de.quippy.javamod.main.gui.components.VUMeterPanel;
import de.quippy.javamod.main.gui.playlist.PlayListGUI;
import de.quippy.javamod.main.gui.playlist.PlaylistGUIChangeListener;
import de.quippy.javamod.main.gui.tools.FileChooserFilter;
import de.quippy.javamod.main.gui.tools.FileChooserResult;
import de.quippy.javamod.main.gui.tools.PlaylistDropListener;
import de.quippy.javamod.main.gui.tools.PlaylistDropListenerCallBack;
import de.quippy.javamod.main.playlist.PlayList;
import de.quippy.javamod.main.playlist.PlayListEntry;
import de.quippy.javamod.mixer.Mixer;
import de.quippy.javamod.mixer.dsp.AudioProcessor;
import de.quippy.javamod.mixer.dsp.DspProcessorCallBack;
import de.quippy.javamod.mixer.dsp.iir.GraphicEQ;
import de.quippy.javamod.mixer.dsp.iir.GraphicEqGUI;
import de.quippy.javamod.mixer.dsp.pitchshift.PitchShift;
import de.quippy.javamod.mixer.dsp.pitchshift.PitchShiftGUI;
import de.quippy.javamod.multimedia.MultimediaContainer;
import de.quippy.javamod.multimedia.MultimediaContainerEvent;
import de.quippy.javamod.multimedia.MultimediaContainerEventListener;
import de.quippy.javamod.multimedia.MultimediaContainerManager;
import de.quippy.javamod.multimedia.mod.ModConstants;
import de.quippy.javamod.system.Helpers;
import de.quippy.javamod.system.Log;
import de.quippy.javamod.system.LogMessageCallBack;

/**
 * @author Daniel Becker
 * @since 22.06.2006
 */
public class MainForm extends JFrame implements DspProcessorCallBack, PlayThreadEventListener, MultimediaContainerEventListener, PlaylistGUIChangeListener, PlaylistDropListenerCallBack
{
	private static final long serialVersionUID = -2737074464335059959L;

	private static final String DEFAULTWINDOWICONPATH = "/de/quippy/javamod/main/gui/ressources/quippy_the_kangaroo_about.gif";
	private static final String DEFAULTTRAYICONPATH = "/de/quippy/javamod/main/gui/ressources/quippy_the_kangaroo_about.gif";

	public static final String BUTTONPLAY_INACTIVE = "/de/quippy/javamod/main/gui/ressources/play.gif";
	public static final String BUTTONPLAY_ACTIVE = "/de/quippy/javamod/main/gui/ressources/play_aktiv.gif";
	public static final String BUTTONPLAY_NORMAL = "/de/quippy/javamod/main/gui/ressources/play_normal.gif";
	public static final String BUTTONPAUSE_INACTIVE = "/de/quippy/javamod/main/gui/ressources/pause.gif";
	public static final String BUTTONPAUSE_ACTIVE = "/de/quippy/javamod/main/gui/ressources/pause_aktiv.gif";
	public static final String BUTTONPAUSE_NORMAL = "/de/quippy/javamod/main/gui/ressources/pause_normal.gif";
	public static final String BUTTONSTOP_INACTIVE = "/de/quippy/javamod/main/gui/ressources/stop.gif";
	public static final String BUTTONSTOP_ACTIVE = "/de/quippy/javamod/main/gui/ressources/stop_aktiv.gif";
	public static final String BUTTONSTOP_NORMAL = "/de/quippy/javamod/main/gui/ressources/stop_normal.gif";
	public static final String BUTTONPREV_INACTIVE = "/de/quippy/javamod/main/gui/ressources/prev.gif";
	public static final String BUTTONPREV_ACTIVE = "/de/quippy/javamod/main/gui/ressources/prev_aktiv.gif";
	public static final String BUTTONPREV_NORMAL = "/de/quippy/javamod/main/gui/ressources/prev_normal.gif";
	public static final String BUTTONNEXT_INACTIVE = "/de/quippy/javamod/main/gui/ressources/next.gif";
	public static final String BUTTONNEXT_ACTIVE = "/de/quippy/javamod/main/gui/ressources/next_aktiv.gif";
	public static final String BUTTONNEXT_NORMAL = "/de/quippy/javamod/main/gui/ressources/next_normal.gif";

	private static final String PROPERTYFILENAME = ".javamod.properties";
	private static final String PROPERTY_SEARCHPATH = "javamod.path.loadpath";
	private static final String PROPERTY_EXPORTPATH = "javamod.path.exportpath";
	private static final String PROPERTY_LOOKANDFEEL = "javamod.lookandfeel.classname";
	private static final String PROPERTY_LASTLOADED = "javamod.path.lastloaded";
	private static final String PROPERTY_SYSTEMTRAY = "javamod.systemtray";
	private static final String PROPERTY_AUTOUPDATECHECK = "javamod.autoupdatecheck";
	private static final String PROPERTY_MAINDIALOG_POS = "javamod.dialog.position.main";
	private static final String PROPERTY_SETUPDIALOG_POS = "javamod.dialog.position.setup";
	private static final String PROPERTY_PROPERTIESDIALOG_POS = "javamod.dialog.position.properties";
	private static final String PROPERTY_PLAYLISTDIALOG_POS = "javamod.dialog.position.playlist";
	private static final String PROPERTY_EFFECTDIALOG_POS = "javamod.dialog.position.equalizer";
	private static final String PROPERTY_MAINDIALOG_SIZE = "javamod.dialog.size.main";
	private static final String PROPERTY_SAMETER_LEFT_DRAWTYPE = "javamod.dialog.sameter.left.draw";
	private static final String PROPERTY_SAMETER_RIGHT_DRAWTYPE = "javamod.dialog.sameter.right.draw";
	private static final String PROPERTY_SETUPDIALOG_SIZE = "javamod.dialog.size.setup";
	private static final String PROPERTY_PROPERTIESDIALOG_SIZE = "javamod.dialog.size.properties";
	private static final String PROPERTY_PLAYLISTDIALOG_SIZE = "javamod.dialog.size.playlist";
	private static final String PROPERTY_EFFECTDIALOG_SIZE = "javamod.dialog.size.equalizer";
	private static final String PROPERTY_VOLUME_VALUE = "javamod.dialog.volume.value";
	private static final String PROPERTY_BALANCE_VALUE = "javamod.dialog.balance.value";
	private static final String PROPERTY_SETUPDIALOG_VISABLE = "javamod.dialog.open.setup";
	private static final String PROPERTY_PROPERTIESDIALOG_VISABLE = "javamod.dialog.open.properties";
	private static final String PROPERTY_PLAYLIST_VISABLE = "javamod.dialog.open.playlist";
	private static final String PROPERTY_EFFECT_VISABLE = "javamod.dialog.open.equalizer";
	private static final String PROPERTY_XMASCONFIGDIALOG_POS = "javamod.dialog.position.xmasconfig";
	private static final String PROPERTY_XMASCONFIGDIALOG_SIZE = "javamod.dialog.size.xmasconfig";
	private static final String PROPERTY_XMASCONFIG_VISABLE = "javamod.dialog.open.xmasconfig";

	private static final String PROPERTY_EFFECTS_PASSTHROUGH = "javamod.player.effects.passthrough";
	private static final String PROPERTY_EFFECTS_USEGAPLESS = "javamod.player.effects.usegapless";
	private static final String PROPERTY_EQUALIZER_PREAMP = "javamod.player.equalizer.preamp";
	private static final String PROPERTY_EQUALIZER_BAND_PREFIX = "javamod.player.equalizer.band.";
	private static final String PROPERTY_EQUALIZER_ISACTIVE = "javamod.player.equalizer.isactive";
	private static final String PROPERTY_PITCHSHIFT_ISACTIVE = "javamod.player.pitchshift.isactive";
	private static final String PROPERTY_PITCHSHIFT_PITCH = "javamod.player.pitchshift.pitch";
	private static final String PROPERTY_PITCHSHIFT_SAMPLESCALE = "javamod.player.pitchshift.scale";
	private static final String PROPERTY_PITCHSHIFT_FRAMESIZE = "javamod.player.pitchshift.framesize";
	private static final String PROPERTY_PITCHSHIFT_OVERSAMPLING = "javamod.player.pitchshift.oversampling";

	private static final int PROPERTY_LASTLOADED_MAXENTRIES = 10;
	private static final String PROPERTY_LAST_UPDATECHECK = "javamod.last_update_check";

	private static final String WINDOW_TITLE = Helpers.FULLVERSION;
	private static final String WINDOW_NAME = "JavaMod";

	private static final int DEFAULT_REFRESH_RATE = 60;

	private transient final MakeMainWindowVisible makeMainWindowVisiable = new MakeMainWindowVisible();

	private static FileFilter fileFilterExport[];
	private static FileFilter fileFilterLoad[];

	private ImageIcon buttonPlay_Active = null;
	private ImageIcon buttonPlay_Inactive = null;
	private ImageIcon buttonPlay_normal = null;
	private ImageIcon buttonPause_Active = null;
	private ImageIcon buttonPause_Inactive = null;
	private ImageIcon buttonPause_normal = null;
	private ImageIcon buttonStop_Active = null;
	private ImageIcon buttonStop_Inactive = null;
	private ImageIcon buttonStop_normal = null;
	private ImageIcon buttonPrev_Active = null;
	private ImageIcon buttonPrev_Inactive = null;
	private ImageIcon buttonPrev_normal = null;
	private ImageIcon buttonNext_Active = null;
	private ImageIcon buttonNext_Inactive = null;
	private ImageIcon buttonNext_normal = null;

	private JButton button_Play = null;
	private JButton button_Pause = null;
	private JButton button_Stop = null;
	private JButton button_Prev = null;
	private JButton button_Next = null;

	private RoundSlider volumeSlider = null;
	private JLabel volumeLabel = null;
	private RoundSlider balanceSlider = null;
	private JLabel balanceLabel = null;

	private JPanel baseContentPane = null;
	private JPanel mainContentPane = null;
	private JPanel musicDataPane = null;
	private JPanel playerControlPane = null;
	private JPanel playerDataPane = null;

	private ArrayList<Image> windowIcons = null;
	private JDialog multimediaInfoDialog = null;
	private JDialog playerSetUpDialog = null;
	private JDialog playlistDialog = null;
	private JDialog equalizerDialog = null;
	private JDialog xmasConfigDialog = null;
	private PlayerConfigPanel playerConfigPanel = null;
	private JPanel multimediaInfoPane = null;
	private JPanel playerSetUpPane = null;
	private JPanel playlistPane = null;
	private JPanel effectPane = null;
	private XmasConfigPanel xmasConfigPanel = null;

	private Point mainDialogLocation = null;
	private Dimension mainDialogSize = null;
	private Point multimediaInfoDialogLocation = null;
	private Dimension multimediaInfoDialogSize = null;
	private boolean multimediaInfoDialogVisable = false;
	private Point playerSetUpDialogLocation = null;
	private Dimension playerSetUpDialogSize = null;
	private boolean playerSetUpDialogVisable = false;
	private Point playlistDialogLocation = null;
	private Dimension playlistDialogSize = null;
	private boolean playlistDialogVisable = false;
	private Point effectsDialogLocation = null;
	private Dimension effectsDialogSize = null;
	private boolean effectDialogVisable = false;
	private Point xmasConfigDialogLocation = null;
	private Dimension xmasConfigDialogSize = null;
	private boolean xmasConfigDialogVisable = false;


	private SimpleProgressDialog downloadDialog = null;
	private DoubleProgressDialog exportDialog = null;

	private VUMeterPanel vuLMeterPanel = null;
	private VUMeterPanel vuRMeterPanel = null;
	private SAMeterPanel saLMeterPanel = null;
	private SAMeterPanel saRMeterPanel = null;
	private LEDScrollPanel ledScrollPanel = null;

	private SeekBarPanel seekBarPanel = null;

	private JTextField messages = null;

	private JMenuBar baseMenuBar = null;
	private JMenu menu_File = null;
	private JMenu menu_View = null;
	private JMenu menu_LookAndFeel = null;
	private JMenu menu_Help = null;
	private JMenu menu_File_RecentFiles = null;
	private JMenuItem menu_File_openMod = null;
	private JMenuItem menu_File_openURL = null;
	private JMenuItem menu_File_exportWave = null;
	private JMenuItem menu_File_exportFromPlayList = null;
	private JMenuItem menu_File_copyFilesInPlayListOrder = null;
	private JMenuItem menu_File_Close = null;
	private JMenuItem menu_View_ArrangeWindows = null;
	private JMenuItem menu_View_Info = null;
	private JMenuItem menu_View_Setup = null;
	private JMenuItem menu_View_Playlist = null;
	private JMenuItem menu_View_GraphicEQ = null;
	private JMenuItem menu_View_XMAS_mode_config = null;
	private JCheckBoxMenuItem menu_View_UseSystemTray = null;
	private JMenuItem menu_Help_CheckUpdate = null;
	private JCheckBoxMenuItem menu_Help_AutoUpdateCheck = null;
	private JMenuItem menu_Help_ShowSoundHardware = null;
	private JMenuItem menu_Help_ShowVersionHistory = null;
	private JMenuItem menu_Help_About = null;
	private JCheckBoxMenuItem [] menu_LookAndFeel_Items = null;

	private MenuItem aboutItem = null;
	private MenuItem playItem = null;
	private MenuItem pauseItem = null;
	private MenuItem stopItem = null;
	private MenuItem prevItem = null;
	private MenuItem nextItem = null;
	private MenuItem closeItem = null;

	private TrayIcon javaModTrayIcon = null;

	private JavaModAbout about = null;
	private UrlDialog urlDialog = null;
	private SimpleTextViewerDialog simpleTextViewerDialog = null;
	private PlayListGUI playlistGUI = null;
	private EffectsPanel effectGUI = null;
	private GraphicEqGUI equalizerGUI = null;
	private PitchShiftGUI pitchShiftGUI = null;

	private MultimediaContainer currentContainer;
	private PlayThread playerThread;
	private PlayList currentPlayList;
	private final GraphicEQ currentEqualizer;
	private final PitchShift currentPitchShift;

	private ArrayList<DropTarget> dropTargetList;
	private final AudioProcessor audioProcessor;
	private transient SoundOutputStream soundOutputStream;

	private final String propertyFilePath;
	private String searchPath;
	private String exportPath;
	private String uiClassName;
	private boolean useSystemTray = false;
	private boolean automaticUpdateCheck = true;
	private float currentVolume; /* 0.0 - 1.0 */
	private float currentBalance; /* -1.0 - 1.0 */

	private LocalDate lastUpdateCheck;
	private static final LocalDate today = LocalDate.now();

	private ArrayList<URL> lastLoaded;
	private ArrayList<Window> windows;
	private boolean[] windowsVisibleState;

	private JPanel oInfoPanel = null;

	private boolean useGaplessAudio;

	private final class LookAndFeelChanger implements ActionListener
	{
		private final String uiClassName;
		private final JCheckBoxMenuItem parent;

		public LookAndFeelChanger(final JCheckBoxMenuItem parent, final String uiClassName)
		{
			this.uiClassName = uiClassName;
			this.parent = parent;
		}
		private void setSelection()
		{
			for (final JCheckBoxMenuItem menu_LookAndFeel_Item : menu_LookAndFeel_Items)
			{
				if (menu_LookAndFeel_Item==parent)
					menu_LookAndFeel_Item.setSelected(true);
				else
					menu_LookAndFeel_Item.setSelected(false);
			}
		}
		@Override
		public void actionPerformed(final ActionEvent event)
		{
			setSelection();
			MainForm.this.uiClassName = uiClassName;
			MainForm.this.updateLookAndFeel(uiClassName);
		}
	}
	private final class MouseWheelVolumeControl implements MouseWheelListener
	{
		@Override
		public void mouseWheelMoved(final MouseWheelEvent e)
		{
			if (!e.isConsumed() && e.getScrollType()==MouseWheelEvent.WHEEL_UNIT_SCROLL)
			{
				final RoundSlider volSlider = getVolumeSlider();
				volSlider.setValue(volSlider.getValue() + (e.getWheelRotation() / 100f));
				e.consume();
			}
		}
	}
	/**
	 * @author Daniel Becker
	 * @since 01.07.2006
	 * If child windows gain focus, bring main windows to front
	 */
	private final class MakeMainWindowVisible implements WindowFocusListener
	{
		@Override
		public void windowLostFocus(final WindowEvent e) { /*NOOP*/ }
		@Override
		public void windowGainedFocus(final WindowEvent e)
		{
			MainForm.this.setFocusableWindowState(false);
			MainForm.this.toFront();
			MainForm.this.setFocusableWindowState(true);
		}
	}
	/**
	 * Constructor for MainForm
	 * @param title
	 * @throws HeadlessException
	 */
	public MainForm() throws HeadlessException
	{
		super();
		propertyFilePath = Helpers.HOMEDIR;
		currentPlayList = null;
		currentEqualizer = new GraphicEQ();
		currentPitchShift = new PitchShift();
	    audioProcessor = new AudioProcessor(2048, 70);
	    audioProcessor.addListener(this);
	    audioProcessor.addEffectListener(currentEqualizer);
	    audioProcessor.addEffectListener(currentPitchShift);
		initialize();
	}
	/**
	 * Read the properties from file. Use default values, if not set or file not available
	 * @since 01.07.2006
	 */
	private void readPropertyFile()
	{
		final java.util.Properties props = new java.util.Properties();
	    try
	    {
	        final File propertyFile = new File(propertyFilePath + File.separator + PROPERTYFILENAME);
	        if (propertyFile.exists())
	        {
	        	java.io.FileInputStream fis = null;
	        	try
	        	{
			    	fis = new java.io.FileInputStream(propertyFile);
			        props.load(fis);
	        	}
	        	finally
	        	{
	        		if (fis!=null) try { fis.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
	        	}
	        }

	        searchPath = props.getProperty(PROPERTY_SEARCHPATH, Helpers.HOMEDIR);
			exportPath = props.getProperty(PROPERTY_EXPORTPATH, Helpers.HOMEDIR);
			useSystemTray = Boolean.parseBoolean(props.getProperty(PROPERTY_SYSTEMTRAY, "FALSE"));
			automaticUpdateCheck = Boolean.parseBoolean(props.getProperty(PROPERTY_AUTOUPDATECHECK, "TRUE"));

			uiClassName = props.getProperty(PROPERTY_LOOKANDFEEL, UIManager.getSystemLookAndFeelClassName());
		    setLookAndFeel(uiClassName); // set the Look&Feel to be used, so when creating the menu we select the current Look&Feel set

		    currentVolume = Float.parseFloat(props.getProperty(PROPERTY_VOLUME_VALUE, "1.0"));
			currentBalance = Float.parseFloat(props.getProperty(PROPERTY_BALANCE_VALUE, "0.0"));
			lastLoaded = new ArrayList<>(PROPERTY_LASTLOADED_MAXENTRIES);
			for (int i=0; i<PROPERTY_LASTLOADED_MAXENTRIES; i++)
			{
				final String url = props.getProperty(PROPERTY_LASTLOADED+'.'+i, null);
				if (url!=null) lastLoaded.add(Helpers.createURLfromString(url)); else lastLoaded.add(null);
			}
			setDSPEnabled(Boolean.parseBoolean(props.getProperty(PROPERTY_EFFECTS_PASSTHROUGH, "FALSE")));
			setUseGaplessAudio(Boolean.parseBoolean(props.getProperty(PROPERTY_EFFECTS_USEGAPLESS, "TRUE")));
			mainDialogLocation = Helpers.getPointFromString(props.getProperty(PROPERTY_MAINDIALOG_POS, "-1x-1"));
			mainDialogSize = Helpers.getDimensionFromString(props.getProperty(PROPERTY_MAINDIALOG_SIZE, "320x410"));
			playerSetUpDialogLocation = Helpers.getPointFromString(props.getProperty(PROPERTY_SETUPDIALOG_POS, "-1x-1"));
			playerSetUpDialogSize = Helpers.getDimensionFromString(props.getProperty(PROPERTY_SETUPDIALOG_SIZE, "720x230"));
			playerSetUpDialogVisable = Boolean.parseBoolean(props.getProperty(PROPERTY_SETUPDIALOG_VISABLE, "false"));
			multimediaInfoDialogLocation = Helpers.getPointFromString(props.getProperty(PROPERTY_PROPERTIESDIALOG_POS, "-1x-1"));
			multimediaInfoDialogSize = Helpers.getDimensionFromString(props.getProperty(PROPERTY_PROPERTIESDIALOG_SIZE, "520x630"));
			multimediaInfoDialogVisable = Boolean.parseBoolean(props.getProperty(PROPERTY_PROPERTIESDIALOG_VISABLE, "false"));
			playlistDialogLocation = Helpers.getPointFromString(props.getProperty(PROPERTY_PLAYLISTDIALOG_POS, "-1x-1"));
			playlistDialogSize = Helpers.getDimensionFromString(props.getProperty(PROPERTY_PLAYLISTDIALOG_SIZE, "400x400"));
			playlistDialogVisable = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYLIST_VISABLE, "false"));
			effectsDialogLocation = Helpers.getPointFromString(props.getProperty(PROPERTY_EFFECTDIALOG_POS, "-1x-1"));
			effectsDialogSize = Helpers.getDimensionFromString(props.getProperty(PROPERTY_EFFECTDIALOG_SIZE, "560x470"));
			effectDialogVisable = Boolean.parseBoolean(props.getProperty(PROPERTY_EFFECT_VISABLE, "false"));
			xmasConfigDialogLocation = Helpers.getPointFromString(props.getProperty(PROPERTY_XMASCONFIGDIALOG_POS, "-1x-1"));
			xmasConfigDialogSize = Helpers.getDimensionFromString(props.getProperty(PROPERTY_XMASCONFIGDIALOG_SIZE, "415x215"));
			xmasConfigDialogVisable = Boolean.parseBoolean(props.getProperty(PROPERTY_XMASCONFIG_VISABLE, "false"));
			final int saMeterLeftDrawType = Integer.parseInt(props.getProperty(PROPERTY_SAMETER_LEFT_DRAWTYPE, "0"));
			final int saMeterRightDrawType = Integer.parseInt(props.getProperty(PROPERTY_SAMETER_RIGHT_DRAWTYPE, "0"));
			getSALMeterPanel().setDrawWhatTo(saMeterLeftDrawType);
			getSARMeterPanel().setDrawWhatTo(saMeterRightDrawType);

			lastUpdateCheck = LocalDate.from(ModConstants.DATE_FORMATER.parse(props.getProperty(PROPERTY_LAST_UPDATECHECK, ModConstants.DATE_FORMATER.format(today))));

			if (currentEqualizer!=null)
			{
				final boolean isActive = Boolean.parseBoolean(props.getProperty(PROPERTY_EQUALIZER_ISACTIVE, "FALSE"));
				currentEqualizer.setIsActive(isActive);
				final float preAmpValueDB = Float.parseFloat(props.getProperty(PROPERTY_EQUALIZER_PREAMP, "0.0"));
				currentEqualizer.setPreAmp(preAmpValueDB);
				for (int i=0; i<currentEqualizer.getBandCount(); i++)
				{
					final float bandValueDB = Float.parseFloat(props.getProperty(PROPERTY_EQUALIZER_BAND_PREFIX + Integer.toString(i), "0.0"));
					currentEqualizer.setBand(i, bandValueDB);
				}
			}
			if (currentPitchShift!=null)
			{
				final boolean isActive = Boolean.parseBoolean(props.getProperty(PROPERTY_PITCHSHIFT_ISACTIVE, "FALSE"));
				currentPitchShift.setIsActive(isActive);
				final float pitchValue = Float.parseFloat(props.getProperty(PROPERTY_PITCHSHIFT_PITCH, "1.0"));
				currentPitchShift.setPitchScale(pitchValue);
				final float scaleValue = Float.parseFloat(props.getProperty(PROPERTY_PITCHSHIFT_SAMPLESCALE, "1.0"));
				currentPitchShift.setSampleScale(scaleValue);
				final int overSampling = Integer.parseInt(props.getProperty(PROPERTY_PITCHSHIFT_OVERSAMPLING, "32"));
				currentPitchShift.setFFTOversampling(overSampling);
				final int frameSize = Integer.parseInt(props.getProperty(PROPERTY_PITCHSHIFT_FRAMESIZE, "8192"));
				currentPitchShift.setFFTFrameSize(frameSize);
			}

			MultimediaContainerManager.configureContainer(props);
			getXmasConfigPanel().readProperties(props);
	    }
	    catch (final Throwable ex)
	    {
			Log.error("[MainForm]", ex);
	    }
	}
	/**
	 * Write back to a File
	 * @since 01.07.2006
	 */
	private void writePropertyFile()
	{
	    try
	    {
	    	final java.util.Properties props = new java.util.Properties();

	    	MultimediaContainerManager.getContainerConfigs(props);
	    	getXmasConfigPanel().writeProperties(props);

			props.setProperty(PROPERTY_SEARCHPATH, searchPath);
			props.setProperty(PROPERTY_EXPORTPATH, exportPath);
			props.setProperty(PROPERTY_LOOKANDFEEL, uiClassName);
			props.setProperty(PROPERTY_SYSTEMTRAY, Boolean.toString(useSystemTray));
			props.setProperty(PROPERTY_AUTOUPDATECHECK, Boolean.toString(automaticUpdateCheck));
			props.setProperty(PROPERTY_VOLUME_VALUE, Float.toString(currentVolume));
			props.setProperty(PROPERTY_BALANCE_VALUE, Float.toString(currentBalance));
			for (int i=0; i<PROPERTY_LASTLOADED_MAXENTRIES; i++)
			{
				final URL element = lastLoaded.get(i);
				if (element!=null)
					props.setProperty(PROPERTY_LASTLOADED+'.'+i, element.toString());
			}
			props.setProperty(PROPERTY_EFFECTS_PASSTHROUGH, Boolean.toString(isDSPEnabled()));
			props.setProperty(PROPERTY_EFFECTS_USEGAPLESS, Boolean.toString(useGaplessAudio()));
			props.setProperty(PROPERTY_MAINDIALOG_POS, Helpers.getStringFromPoint(getLocation()));
			props.setProperty(PROPERTY_MAINDIALOG_SIZE, Helpers.getStringFromDimension(getSize()));
			props.setProperty(PROPERTY_SETUPDIALOG_POS, Helpers.getStringFromPoint(getPlayerSetUpDialog().getLocation()));
			props.setProperty(PROPERTY_SETUPDIALOG_SIZE, Helpers.getStringFromDimension(getPlayerSetUpDialog().getSize()));
			props.setProperty(PROPERTY_SETUPDIALOG_VISABLE, Boolean.toString(getPlayerSetUpDialog().isVisible()));
			props.setProperty(PROPERTY_PROPERTIESDIALOG_POS, Helpers.getStringFromPoint(getMultimediaInfoDialog().getLocation()));
			props.setProperty(PROPERTY_PROPERTIESDIALOG_SIZE, Helpers.getStringFromDimension(getMultimediaInfoDialog().getSize()));
			props.setProperty(PROPERTY_PROPERTIESDIALOG_VISABLE, Boolean.toString(getMultimediaInfoDialog().isVisible()));
			props.setProperty(PROPERTY_PLAYLISTDIALOG_POS, Helpers.getStringFromPoint(getPlaylistDialog().getLocation()));
			props.setProperty(PROPERTY_PLAYLISTDIALOG_SIZE, Helpers.getStringFromDimension(getPlaylistDialog().getSize()));
			props.setProperty(PROPERTY_PLAYLIST_VISABLE, Boolean.toString(getPlaylistDialog().isVisible()));
			props.setProperty(PROPERTY_EFFECTDIALOG_POS, Helpers.getStringFromPoint(getEffectDialog().getLocation()));
			props.setProperty(PROPERTY_EFFECTDIALOG_SIZE, Helpers.getStringFromDimension(getEffectDialog().getSize()));
			props.setProperty(PROPERTY_EFFECT_VISABLE, Boolean.toString(getEffectDialog().isVisible()));
			props.setProperty(PROPERTY_XMASCONFIGDIALOG_POS, Helpers.getStringFromPoint(getXmasConfigDialog().getLocation()));
			props.setProperty(PROPERTY_XMASCONFIGDIALOG_SIZE, Helpers.getStringFromDimension(getXmasConfigDialog().getSize()));
			props.setProperty(PROPERTY_XMASCONFIG_VISABLE, Boolean.toString(getXmasConfigDialog().isVisible()));
			props.setProperty(PROPERTY_SAMETER_LEFT_DRAWTYPE, Integer.toString(getSALMeterPanel().getDrawWhat()));
			props.setProperty(PROPERTY_SAMETER_RIGHT_DRAWTYPE, Integer.toString(getSARMeterPanel().getDrawWhat()));
			props.setProperty(PROPERTY_LAST_UPDATECHECK, ModConstants.DATE_FORMATER.format(lastUpdateCheck));

			if (currentEqualizer!=null)
			{
				props.setProperty(PROPERTY_EQUALIZER_ISACTIVE, Boolean.toString(currentEqualizer.isActive()));
				props.setProperty(PROPERTY_EQUALIZER_PREAMP, Float.toString(currentEqualizer.getPreAmpDB()));
				for (int i=0; i<currentEqualizer.getBandCount(); i++)
				{
					props.setProperty(PROPERTY_EQUALIZER_BAND_PREFIX + Integer.toString(i), Float.toString(currentEqualizer.getBand(i)));
				}
			}
			if (currentPitchShift!=null)
			{
				props.setProperty(PROPERTY_PITCHSHIFT_ISACTIVE, Boolean.toString(currentPitchShift.isActive()));
				props.setProperty(PROPERTY_PITCHSHIFT_PITCH, Float.toString(currentPitchShift.getPitchScale()));
				props.setProperty(PROPERTY_PITCHSHIFT_SAMPLESCALE, Float.toString(currentPitchShift.getSampleScale()));
				props.setProperty(PROPERTY_PITCHSHIFT_FRAMESIZE, Integer.toString(currentPitchShift.getFftFrameSize()));
				props.setProperty(PROPERTY_PITCHSHIFT_OVERSAMPLING, Integer.toString(currentPitchShift.getFFTOversampling()));
			}

			final File propertyFile = new File(propertyFilePath + File.separator + PROPERTYFILENAME);
	        if (propertyFile.exists())
	        {
	        	boolean ok = propertyFile.delete();
	        	if (ok) ok = propertyFile.createNewFile();
	        	if (!ok) Log.error("Could not create property file: " + propertyFile.getCanonicalPath());
	        }
	        java.io.FileOutputStream fos = null;
	        try
	        {
		    	fos = new java.io.FileOutputStream(propertyFile);
			    props.store(fos, WINDOW_TITLE);
	        }
	        finally
	        {
	        	if (fos!=null) try { fos.close(); } catch (final IOException ex) { /* Log.error("IGNORED", ex); */ }
	        }
	    }
	    catch (final Throwable ex)
	    {
			Log.error("MainForm]", ex);
	    }
	}
	private UIManager.LookAndFeelInfo [] getInstalledLookAndFeels()
	{
//		java.util.ArrayList<UIManager.LookAndFeelInfo> allLAFs = new java.util.ArrayList<UIManager.LookAndFeelInfo>();
//		allLAFs.add(new UIManager.LookAndFeelInfo("Kunststoff", "com.incors.plaf.kunststoff.KunststoffLookAndFeel"));
//		allLAFs.add(new UIManager.LookAndFeelInfo("Oyoaha", "com.oyoaha.swing.plaf.oyoaha.OyoahaLookAndFeel"));
//		allLAFs.add(new UIManager.LookAndFeelInfo("MacOS", "it.unitn.ing.swing.plaf.macos.MacOSLookAndFeel"));
//		allLAFs.add(new UIManager.LookAndFeelInfo("GTK", "org.gtk.java.swing.plaf.gtk.GtkLookAndFeel"));
//		UIManager.LookAndFeelInfo [] installedLAFs = UIManager.getInstalledLookAndFeels();
//		for (int i=0; i<installedLAFs.length; i++)
//		{
//			allLAFs.add(installedLAFs[i]);
//		}
//		return allLAFs.toArray(new UIManager.LookAndFeelInfo[allLAFs.size()]);
		return UIManager.getInstalledLookAndFeels();
	}
	/**
	 * Create the file filters so that we do have them for
	 * the dialogs
	 * @since 05.01.2008
	 */
	private void createFileFilter()
	{
		final Map<String, String[]> extensionMap = MultimediaContainerManager.getSupportedFileExtensionsPerContainer();

		final ArrayList<FileFilter> chooserFilterArray = new ArrayList<>(extensionMap.size() + 1);

		// add all single file extensions grouped by container
		final Set<String> containerNameSet = extensionMap.keySet();
		for (final String containerName : containerNameSet)
		{
			final String [] extensions = extensionMap.get(containerName);
			final StringBuilder fileText = new StringBuilder(containerName);
			fileText.append(" (");
			final int ende = extensions.length-1;
			for (int i=0; i<=ende; i++)
			{
				fileText.append("*.").append(extensions[i]);
				if (i<ende) fileText.append(", ");
			}
			fileText.append(')');
			chooserFilterArray.add(new FileChooserFilter(extensions, fileText.toString()));
		}
		// now add playlist as group of files
		chooserFilterArray.add(PlayList.PLAYLIST_FILE_FILTER);

		// now add all playable files at the last step (container extensions and playlist files)
		final String [] containerExtensions = MultimediaContainerManager.getSupportedFileExtensions();
		final String [] fullSupportedExtensions = new String[containerExtensions.length + PlayList.SUPPORTEDPLAYLISTS.length];
		System.arraycopy(PlayList.SUPPORTEDPLAYLISTS, 0, fullSupportedExtensions, 0, PlayList.SUPPORTEDPLAYLISTS.length);
		System.arraycopy(containerExtensions, 0, fullSupportedExtensions, PlayList.SUPPORTEDPLAYLISTS.length, containerExtensions.length);
		chooserFilterArray.add(new FileChooserFilter(fullSupportedExtensions, "All playable files"));

		fileFilterLoad = new FileFilter[chooserFilterArray.size()];
		chooserFilterArray.toArray(fileFilterLoad);

		fileFilterExport = new FileFilter[1];
		fileFilterExport[0] = new FileChooserFilter(javax.sound.sampled.AudioFileFormat.Type.WAVE.getExtension(), javax.sound.sampled.AudioFileFormat.Type.WAVE.toString());
	}
	/**
	 * Do main initials
	 * @since 22.06.2006
	 */
	private void initialize()
	{
		MultimediaContainerManager.setIsHeadlessMode(false);

		Log.addLogListener(new LogMessageCallBack()
		{
			@Override
			public void debug(final String message)
			{
				showMessage(message);
			}
			@Override
			public void info(final String message)
			{
				showMessage(message);
			}
			@Override
			public void error(final String message, final Throwable ex)
			{
				if (ex!=null)
					showMessage(message+' '+ex.getMessage());
				else
					showMessage(message);
			}
		});

		readPropertyFile();

		setSystemTray();

		setName(WINDOW_NAME);
		setTitle(WINDOW_TITLE);
		setTrayIconToolTip(WINDOW_TITLE);

    	setIconImages(getWindowIconImages(DEFAULTWINDOWICONPATH));

	    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				doClose();
			}
			/**
			 * @param e
			 * @see WindowAdapter#windowIconified(WindowEvent)
			 * @since 07.02.2012
			 */
			@Override
			public void windowIconified(final WindowEvent e)
			{
				doIconify();
			}
			/**
			 * @param e
			 * @see WindowAdapter#windowDeiconified(WindowEvent)
			 * @since 07.02.2012
			 */
			@Override
			public void windowDeiconified(final WindowEvent e)
			{
				doDeIconify();
				toFront(); // MakeMainWindowVisible does this also, if other child windows are open and gain focus
			}
		});
	    setSize(mainDialogSize);
		setPreferredSize(mainDialogSize);

		setJMenuBar(getBaseMenuBar());
		setContentPane(getBaseContentPane());
	    setPlayListIcons();
		// Volumecontrol by mousewheel:
	    addMouseWheelListener(new MouseWheelVolumeControl());
	    pack();

		createAllWindows(); // create all windows
		updateLookAndFeel(uiClassName); // and change to L&F

		if (mainDialogLocation == null || (mainDialogLocation.getX()==-1 || mainDialogLocation.getY()==-1))
			mainDialogLocation = Helpers.getFrameCenteredLocation(this, null);
	    setLocation(mainDialogLocation);
	    getMultimediaInfoDialog().setVisible(multimediaInfoDialogVisable);
		getPlaylistDialog().setVisible(playlistDialogVisable);
		getEffectDialog().setVisible(effectDialogVisable);
		getXmasConfigDialog().setVisible(xmasConfigDialogVisable);
		getPlayerSetUpDialog().setVisible(playerSetUpDialogVisable);

		dropTargetList = new ArrayList<>();
	    final PlaylistDropListener myListener = new PlaylistDropListener(this);
	    Helpers.registerDropListener(dropTargetList, this, myListener);

		MultimediaContainerManager.addMultimediaContainerEventListener(this);

		createFileFilter();

	    currentContainer = null; //set Back to null!

		showMessage("Ready...");

		doAutomaticUpdateCheck();
	}
	private void createAllWindows()
	{
		windows = new ArrayList<>();
		windows.add(getJavaModAbout());
		windows.add(getMultimediaInfoDialog());
		windows.add(getPlayerSetUpDialog());
		windows.add(getURLDialog());
		windows.add(getSimpleTextViewerDialog());
		windows.add(getPlaylistDialog());
		windows.add(getEffectDialog());
		windows.add(getXmasConfigDialog());
	}
	/**
	 * @param dtde
	 * @param dropResult
	 * @param addToLastLoaded
	 * @see de.quippy.javamod.main.gui.tools.PlaylistDropListenerCallBack#playlistRecieved(dnd.DropTargetDropEvent, de.quippy.javamod.main.playlist.PlayList, java.net.URL)
	 * @since 08.03.2011
	 */
	@Override
	public void playlistRecieved(final DropTargetDropEvent dtde, final PlayList dropResult, final URL addToLastLoaded)
	{
		if (addToLastLoaded!=null) addFileToLastLoaded(addToLastLoaded);
		if (dropResult!=null)
		{
			doStopPlaying();
    		getPlaylistGUI().setNewPlaylist(currentPlayList = dropResult);
			final boolean ok = doNextPlayListEntry();
			if (playerThread==null && ok) doStartPlaying();
		}
	}
	/**
	 * @see de.quippy.javamod.main.gui.playlist.PlaylistGUIChangeListener#playListChanged()
	 * @since 08.03.2011
	 */
	@Override
	public void playListChanged(final PlayList newPlayList)
	{
		if (newPlayList!=null)
		{
			if (newPlayList!=currentPlayList)
			{
				final boolean playListWasEmpty = currentPlayList == null;
				currentPlayList = newPlayList;
				if (playListWasEmpty) doNextPlayListEntry();
			}
			setPlayListIcons();
			//if (playerThread==null) doStartPlaying();
		}
	}
	/**
	 * set the selected look and feel
	 * @since 01.07.2006
	 * @param lookAndFeelClassName
	 * @return
	 */
	private void setLookAndFeel(String lookAndFeelClassName)
	{
		try
		{
	        UIManager.setLookAndFeel(lookAndFeelClassName);
		}
		catch (final Throwable e)
		{
			showMessage("The selected Look&Feel is not supported or not reachable through the classpath. Switching to system default...");
	        try
	        {
	        	lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
	            UIManager.setLookAndFeel(lookAndFeelClassName);
	        }
	        catch (final Throwable e1)
	        {
				Log.error("[MainForm]", e1);
	        }
		}
	}
	/**
	 * Changes the look and feel to the new ClassName
	 * @since 22.06.2006
	 * @param lookAndFeelClassName
	 * @return
	 */
	private void updateLookAndFeel(final String lookAndFeelClassName)
	{
	    setLookAndFeel(lookAndFeelClassName);
	    MultimediaContainerManager.updateLookAndFeel();
		SwingUtilities.updateComponentTreeUI(this);
		setPreferredSize(getSize());
		pack();
	    for (final Window window : windows)
	    {
			SwingUtilities.updateComponentTreeUI(window);
			window.setPreferredSize(window.getSize());
			window.pack();
	    }
	}
	/**
	 * Change the info panel in the ModInfoPane to the new panel according
	 * to loading of a file.
	 * However, if we will set the same info panel again, do not change it.
	 * @since 22.06.2006
	 */
	private void changeInfoPane()
	{
		final JPanel infoPanel = getCurrentContainer().getInfoPanel();
		if (oInfoPanel==null || !oInfoPanel.equals(infoPanel))
		{
			oInfoPanel = infoPanel;
			getMultimediaInfoPane().removeAll();
			if (infoPanel!=null) getMultimediaInfoPane().add(infoPanel, BorderLayout.CENTER);
			getMultimediaInfoDialog().pack();
			getMultimediaInfoDialog().repaint();
		}
	}
	private void changeConfigPane()
	{
		getPlayerConfigPanel().selectTabForContainer(getCurrentContainer());
	}
	private void changeExportMenu()
	{
		getMenu_File_exportWave().setEnabled(getCurrentContainer().canExport());
		getMenu_File_exportFilesFromPlaylist().setEnabled(getPlaylistGUI().getPlayList()!=null);
		getMenu_File_exportFilesInPlaylistOrder().setEnabled(getPlaylistGUI().getPlayList()!=null);
	}
	/**
	 * @since 15.01.2012
	 * @return
	 */
	public boolean isDSPEnabled()
	{
		if (audioProcessor!=null) return audioProcessor.isDspEnabled();
		return false;
	}
	/**
	 * @since 15.01.2012
	 * @param dspEnabled
	 */
	public void setDSPEnabled(final boolean dspEnabled)
	{
		if (audioProcessor!=null) audioProcessor.setDspEnabled(dspEnabled);
	}
	/* Element Getter Methods ---------------------------------------------- */
	public JMenuBar getBaseMenuBar()
	{
		if (baseMenuBar == null)
		{
			baseMenuBar = new JMenuBar();
			baseMenuBar.setName("baseMenuBar");
			baseMenuBar.add(getMenu_File());
			baseMenuBar.add(getMenu_View());
			baseMenuBar.add(getMenu_LookAndFeel());
			baseMenuBar.add(getMenu_Help());
		}
		return baseMenuBar;
	}
	public JMenu getMenu_File()
	{
		if (menu_File == null)
		{
			menu_File = new JMenu();
			menu_File.setName("menu_File");
			menu_File.setMnemonic('f');
			menu_File.setText("File");
			menu_File.setFont(Helpers.getDialogFont());
			menu_File.add(getMenu_File_openMod());
			menu_File.add(getMenu_File_openURL());
			menu_File.add(new JSeparator());
			menu_File.add(getMenu_File_exportWave());
			menu_File.add(getMenu_File_exportFilesFromPlaylist());
			menu_File.add(getMenu_File_exportFilesInPlaylistOrder());
			menu_File.add(new JSeparator());
			menu_File.add(getMenu_File_RecentFiles());
			menu_File.add(new JSeparator());
			menu_File.add(getMenu_File_Close());
		}
		return menu_File;
	}
	public JMenu getMenu_View()
	{
		if (menu_View == null)
		{
			menu_View = new JMenu();
			menu_View.setName("menu_View");
			menu_View.setMnemonic('v');
			menu_View.setText("View");
			menu_View.setFont(Helpers.getDialogFont());
			menu_View.add(getMenu_View_ArrangeWindows());
			menu_View.add(new JSeparator());
			menu_View.add(getMenu_View_Info());
			menu_View.add(getMenu_View_Setup());
			menu_View.add(getMenu_View_Playlist());
			menu_View.add(getMenu_View_GraphicEQ());
			menu_View.add(new JSeparator());
			menu_View.add(getMenu_View_XMAS_mode_config());
			menu_View.add(new JSeparator());
			menu_View.add(getMenu_View_UseSystemTray());
		}
		return menu_View;
	}
	public JMenu getMenu_LookAndFeel()
	{
		if (menu_LookAndFeel == null)
		{
			menu_LookAndFeel = new JMenu();
			menu_LookAndFeel.setName("menu_LookAndFeel");
			menu_LookAndFeel.setMnemonic('l');
			menu_LookAndFeel.setText("Look&Feel");
			menu_LookAndFeel.setFont(Helpers.getDialogFont());

			final String currentUIClassName = UIManager.getLookAndFeel().getClass().getName();
			final UIManager.LookAndFeelInfo [] lookAndFeels = getInstalledLookAndFeels();
			menu_LookAndFeel_Items = new JCheckBoxMenuItem[lookAndFeels.length];
			for (int i=0; i<lookAndFeels.length; i++)
			{
				menu_LookAndFeel_Items[i] = new JCheckBoxMenuItem();
				menu_LookAndFeel_Items[i].setName("newMenuItem_"+i);
				menu_LookAndFeel_Items[i].setText(lookAndFeels[i].getName());
				menu_LookAndFeel_Items[i].setFont(Helpers.getDialogFont());
				menu_LookAndFeel_Items[i].setToolTipText("Change to " + lookAndFeels[i].getName() + " look and feel");
				final String uiClassName = lookAndFeels[i].getClassName();
				if (uiClassName.equals(currentUIClassName)) menu_LookAndFeel_Items[i].setSelected(true);
				menu_LookAndFeel_Items[i].addActionListener(new LookAndFeelChanger(menu_LookAndFeel_Items[i], uiClassName));
				menu_LookAndFeel.add(menu_LookAndFeel_Items[i]);
			}

		}
		return menu_LookAndFeel;
	}
	private JMenu getMenu_Help()
	{
		if (menu_Help == null)
		{
			menu_Help = new JMenu();
			menu_Help.setName("menu_Help");
			menu_Help.setMnemonic('h');
			menu_Help.setText("Help");
			menu_Help.setFont(Helpers.getDialogFont());
			menu_Help.add(getMenu_Help_CheckUpdate());
			menu_Help.add(getMenu_Help_AutomaticUpdateCheck());
			menu_Help.add(new JSeparator());
			menu_Help.add(getMenu_Help_ShowSoundHardware());
			menu_Help.add(getMenu_Help_ShowVersionHistory());
			menu_Help.add(new JSeparator());
			menu_Help.add(getMenu_Help_About());
		}
		return menu_Help;
	}
	private JMenuItem getMenu_File_openMod()
	{
		if (menu_File_openMod == null)
		{
			menu_File_openMod = new JMenuItem();
			menu_File_openMod.setName("menu_File_openMod");
			menu_File_openMod.setMnemonic('o');
			menu_File_openMod.setText("Open Sound File...");
			menu_File_openMod.setFont(Helpers.getDialogFont());
			menu_File_openMod.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doOpenFile();
				}
			});
		}
		return menu_File_openMod;
	}
	private JMenuItem getMenu_File_openURL()
	{
		if (menu_File_openURL == null)
		{
			menu_File_openURL = new JMenuItem();
			menu_File_openURL.setName("menu_File_openURL");
			menu_File_openURL.setMnemonic('u');
			menu_File_openURL.setText("Open an URL...");
			menu_File_openURL.setFont(Helpers.getDialogFont());
			menu_File_openURL.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doOpenURL();
				}
			});
		}
		return menu_File_openURL;
	}
	private JMenuItem getMenu_File_exportWave()
	{
		if (menu_File_exportWave == null)
		{
			menu_File_exportWave = new JMenuItem();
			menu_File_exportWave.setName("menu_File_exportWave");
			menu_File_exportWave.setMnemonic('x');
			menu_File_exportWave.setText("Export to wave while playing...");
			menu_File_exportWave.setFont(Helpers.getDialogFont());
			menu_File_exportWave.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doExportToWave();
				}
			});
		}
		return menu_File_exportWave;
	}
	private JMenuItem getMenu_File_exportFilesFromPlaylist()
	{
		if (menu_File_exportFromPlayList == null)
		{
			menu_File_exportFromPlayList = new JMenuItem();
			menu_File_exportFromPlayList.setName("menu_File_exportFromPlayList");
			menu_File_exportFromPlayList.setMnemonic('e');
			menu_File_exportFromPlayList.setText("Export selected to wave...");
			menu_File_exportFromPlayList.setFont(Helpers.getDialogFont());
			menu_File_exportFromPlayList.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doExportFromPlaylist(true);
				}
			});
		}
		return menu_File_exportFromPlayList;
	}
	private JMenuItem getMenu_File_exportFilesInPlaylistOrder()
	{
		if (menu_File_copyFilesInPlayListOrder == null)
		{
			menu_File_copyFilesInPlayListOrder = new JMenuItem();
			menu_File_copyFilesInPlayListOrder.setName("menu_File_copyFilesInPlayListOrder");
			menu_File_copyFilesInPlayListOrder.setMnemonic('c');
			menu_File_copyFilesInPlayListOrder.setText("Copy selected files in playlist order...");
			menu_File_copyFilesInPlayListOrder.setFont(Helpers.getDialogFont());
			menu_File_copyFilesInPlayListOrder.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doExportFromPlaylist(false);
				}
			});
		}
		return menu_File_copyFilesInPlayListOrder;
	}
	private JMenu getMenu_File_RecentFiles()
	{
		if (menu_File_RecentFiles == null)
		{
			menu_File_RecentFiles = new JMenu();
			menu_File_RecentFiles.setName("menu_File_RecentFiles");
			menu_File_RecentFiles.setMnemonic('r');
			menu_File_RecentFiles.setText("Recent files");
			menu_File_RecentFiles.setFont(Helpers.getDialogFont());

			createRecentFileMenuItems();
		}
		return menu_File_RecentFiles;
	}
	private void createRecentFileMenuItems()
	{
		final JMenu recent = getMenu_File_RecentFiles();
		recent.removeAll();
		for (int i=0, index=1; i<PROPERTY_LASTLOADED_MAXENTRIES; i++)
		{
			final URL element = lastLoaded.get(i);
			if (element!=null)
			{
				String displayName = null;
				// convert to a local filename if possible (that looks better!)
				if (Helpers.isFile(element))
				{
					try
					{
						final File f = new File(element.toURI());
						displayName = f.getAbsolutePath();
					}
					catch (final URISyntaxException ex)
					{
					}
				}

				if (displayName==null) displayName = lastLoaded.get(i).toString();
				final JMenuItem lastLoadURL = new JMenuItem();
				lastLoadURL.setName("menu_File_RecentFiles_File"+i);
				lastLoadURL.setText(((index<10)?"  ":Helpers.EMPTY_STING) + (index++) + " " + displayName);
				lastLoadURL.setFont(Helpers.getDialogFont());
				lastLoadURL.setToolTipText(element.toString());
				lastLoadURL.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						try
						{
							final URL url = Helpers.createURLfromString(((JMenuItem)e.getSource()).getToolTipText());
							loadMultimediaOrPlayListFile(url);
						}
						catch (final Exception ex)
						{
							Log.error("Load recent error", ex);
						}
					}
				});
				recent.add(lastLoadURL);
			}
		}
	}
	private JMenuItem getMenu_File_Close()
	{
		if (menu_File_Close == null)
		{
			menu_File_Close = new JMenuItem();
			menu_File_Close.setName("menu_File_Close");
			menu_File_Close.setMnemonic('c');
			menu_File_Close.setText("Close");
			menu_File_Close.setFont(Helpers.getDialogFont());
			menu_File_Close.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doClose();
				}
			});
		}
		return menu_File_Close;
	}
	private JMenuItem getMenu_View_ArrangeWindows()
	{
		if (menu_View_ArrangeWindows == null)
		{
			menu_View_ArrangeWindows = new JMenuItem();
			menu_View_ArrangeWindows.setName("menu_View_ArrangeWindows");
			menu_View_ArrangeWindows.setMnemonic('a');
			menu_View_ArrangeWindows.setText("Arrange Windows");
			menu_View_ArrangeWindows.setFont(Helpers.getDialogFont());
			menu_View_ArrangeWindows.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doArrangeWindows();
				}
			});
		}
		return menu_View_ArrangeWindows;
	}
	private JMenuItem getMenu_View_Info()
	{
		if (menu_View_Info == null)
		{
			menu_View_Info = new JMenuItem();
			menu_View_Info.setName("menu_View_Info");
			menu_View_Info.setMnemonic('p');
			menu_View_Info.setText("Properties...");
			menu_View_Info.setFont(Helpers.getDialogFont());
			menu_View_Info.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					getMultimediaInfoDialog().setVisible(true);
				}
			});
		}
		return menu_View_Info;
	}
	private JMenuItem getMenu_View_Setup()
	{
		if (menu_View_Setup == null)
		{
			menu_View_Setup = new JMenuItem();
			menu_View_Setup.setName("menu_View_Setup");
			menu_View_Setup.setMnemonic('s');
			menu_View_Setup.setText("Setup...");
			menu_View_Setup.setFont(Helpers.getDialogFont());
			menu_View_Setup.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					getPlayerSetUpDialog().setVisible(true);
				}
			});
		}
		return menu_View_Setup;
	}
	private JMenuItem getMenu_View_Playlist()
	{
		if (menu_View_Playlist == null)
		{
			menu_View_Playlist = new JMenuItem();
			menu_View_Playlist.setName("menu_View_Playlist");
			menu_View_Playlist.setMnemonic('p');
			menu_View_Playlist.setText("Playlist...");
			menu_View_Playlist.setFont(Helpers.getDialogFont());
			menu_View_Playlist.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					getPlaylistDialog().setVisible(true);
				}
			});
		}
		return menu_View_Playlist;
	}
	private JMenuItem getMenu_View_GraphicEQ()
	{
		if (menu_View_GraphicEQ == null)
		{
			menu_View_GraphicEQ = new JMenuItem();
			menu_View_GraphicEQ.setName("menu_View_GraphicEQ");
			menu_View_GraphicEQ.setMnemonic('e');
			menu_View_GraphicEQ.setText("Effect...");
			menu_View_GraphicEQ.setFont(Helpers.getDialogFont());
			menu_View_GraphicEQ.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					getEffectDialog().setVisible(true);
				}
			});
		}
		return menu_View_GraphicEQ;
	}
	private JMenuItem getMenu_View_XMAS_mode_config()
	{
		if (menu_View_XMAS_mode_config == null)
		{
			menu_View_XMAS_mode_config = new JMenuItem();
			menu_View_XMAS_mode_config.setName("menu_View_XMAS_mode_config");
			menu_View_XMAS_mode_config.setMnemonic('x');
			menu_View_XMAS_mode_config.setText("X-Mas mode...");
			menu_View_XMAS_mode_config.setFont(Helpers.getDialogFont());
			menu_View_XMAS_mode_config.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					getXmasConfigDialog().setVisible(true);
				}
			});
		}
		return menu_View_XMAS_mode_config;
	}
	private JCheckBoxMenuItem getMenu_View_UseSystemTray()
	{
		if (menu_View_UseSystemTray == null)
		{
			menu_View_UseSystemTray = new JCheckBoxMenuItem();
			menu_View_UseSystemTray.setName("menu_View_UseSystemTray");
			menu_View_UseSystemTray.setMnemonic('t');
			menu_View_UseSystemTray.setText("Use system tray");
			menu_View_UseSystemTray.setFont(Helpers.getDialogFont());
			menu_View_UseSystemTray.setEnabled(SystemTray.isSupported());
			menu_View_UseSystemTray.setSelected(useSystemTray);
			menu_View_UseSystemTray.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					useSystemTray = getMenu_View_UseSystemTray().isSelected();
					setSystemTray();
				}
			});
		}
		return menu_View_UseSystemTray;
	}
	private JMenuItem getMenu_Help_CheckUpdate()
	{
		if (menu_Help_CheckUpdate == null)
		{
			menu_Help_CheckUpdate = new JMenuItem();
			menu_Help_CheckUpdate.setName("menu_Help_CheckUpdate");
			menu_Help_CheckUpdate.setMnemonic('c');
			menu_Help_CheckUpdate.setText("Check for update...");
			menu_Help_CheckUpdate.setFont(Helpers.getDialogFont());
			menu_Help_CheckUpdate.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doCheckUpdate(false);
				}
			});
		}
		return menu_Help_CheckUpdate;
	}
	private JCheckBoxMenuItem getMenu_Help_AutomaticUpdateCheck()
	{
		if (menu_Help_AutoUpdateCheck == null)
		{
			menu_Help_AutoUpdateCheck = new JCheckBoxMenuItem();
			menu_Help_AutoUpdateCheck.setName("menu_Help_AutoUpdateCheck");
			menu_Help_AutoUpdateCheck.setMnemonic('a');
			menu_Help_AutoUpdateCheck.setText("Automatic update check");
			menu_Help_AutoUpdateCheck.setFont(Helpers.getDialogFont());
			menu_Help_AutoUpdateCheck.setSelected(automaticUpdateCheck);
			menu_Help_AutoUpdateCheck.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					automaticUpdateCheck = getMenu_Help_AutomaticUpdateCheck().isSelected();
					doCheckUpdate(true);
				}
			});
		}
		return menu_Help_AutoUpdateCheck;
	}
	private JMenuItem getMenu_Help_ShowSoundHardware()
	{
		if (menu_Help_ShowSoundHardware == null)
		{
			menu_Help_ShowSoundHardware = new JMenuItem();
			menu_Help_ShowSoundHardware.setName("menu_Help_ShowSoundHardware");
			menu_Help_ShowSoundHardware.setMnemonic('s');
			menu_Help_ShowSoundHardware.setText("Show sound hardware info...");
			menu_Help_ShowSoundHardware.setFont(Helpers.getDialogFont());
			menu_Help_ShowSoundHardware.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					final SimpleTextViewerDialog dialog = getSimpleTextViewerDialog();
					dialog.setDisplayText(Helpers.getAudioInfos());
					dialog.setVisible(true);
				}
			});
		}
		return menu_Help_ShowSoundHardware;
	}
	private JMenuItem getMenu_Help_ShowVersionHistory()
	{
		if (menu_Help_ShowVersionHistory == null)
		{
			menu_Help_ShowVersionHistory = new JMenuItem();
			menu_Help_ShowVersionHistory.setName("menu_Help_showVersionHistory");
			menu_Help_ShowVersionHistory.setMnemonic('s');
			menu_Help_ShowVersionHistory.setText("Show version history...");
			menu_Help_ShowVersionHistory.setFont(Helpers.getDialogFont());
			menu_Help_ShowVersionHistory.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					final SimpleTextViewerDialog dialog = getSimpleTextViewerDialog();
					dialog.setDisplayTextFromURL(Helpers.VERSION_URL);
					dialog.setVisible(true);
				}
			});
		}
		return menu_Help_ShowVersionHistory;
	}
	private JMenuItem getMenu_Help_About()
	{
		if (menu_Help_About == null)
		{
			menu_Help_About = new JMenuItem();
			menu_Help_About.setName("menu_Help_About");
			menu_Help_About.setMnemonic('a');
			menu_Help_About.setText("About...");
			menu_Help_About.setFont(Helpers.getDialogFont());
			menu_Help_About.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doShowAbout();
				}
			});
		}
		return menu_Help_About;
	}
	private MenuItem getAboutItem()
	{
		if (aboutItem==null)
		{
			aboutItem = new MenuItem("About");
			aboutItem.setFont(Helpers.getDialogFont());
			aboutItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doShowAbout();
				}
			});
		}
		return aboutItem;
	}
	private MenuItem getPlayItem()
	{
		if (playItem==null)
		{
			playItem = new MenuItem("Play");
			playItem.setFont(Helpers.getDialogFont());
			playItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doStartPlaying();
				}
			});
		}
		return playItem;
	}
	private MenuItem getPauseItem()
	{
		if (pauseItem==null)
		{
			pauseItem = new MenuItem("Pause");
			pauseItem.setFont(Helpers.getDialogFont());
			pauseItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doPausePlaying();
				}
			});
		}
		return pauseItem;
	}
	private MenuItem getStopItem()
	{
		if (stopItem==null)
		{
			stopItem = new MenuItem("Stop");
			stopItem.setFont(Helpers.getDialogFont());
			stopItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doStopPlaying();
				}
			});
		}
		return stopItem;
	}
	private MenuItem getPrevItem()
	{
		if (prevItem==null)
		{
			prevItem = new MenuItem("Previous");
			prevItem.setFont(Helpers.getDialogFont());
			prevItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doPrevPlayListEntry();
				}
			});
		}
		return prevItem;
	}
	private MenuItem getNextItem()
	{
		if (nextItem==null)
		{
			nextItem = new MenuItem("Next");
			nextItem.setFont(Helpers.getDialogFont());
			nextItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doNextPlayListEntry();
				}
			});
		}
		return nextItem;
	}
	private MenuItem getCloseItem()
	{
		if (closeItem==null)
		{
			closeItem = new MenuItem("Close");
			closeItem.setFont(Helpers.getDialogFont());
			closeItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doClose();
				}
			});
		}
		return closeItem;
	}
	/**
	 * Do what is needed to iconify the MainForm, if in system tray.
	 * Because with Linux (or KDE) a "setVisible(false)" with
	 * a "setVisible(true)" at deiconify does not work,
	 * we need to dispose and restore afterwards
	 * @since 26.01.2022
	 */
	private void doIconify()
	{
		if (useSystemTray)
		{
			// no check, if iconified, as that *will* be true already!
			// remember visible state of all windows
			windowsVisibleState = new boolean[windows.size()];
			for (int x=0; x<windows.size(); x++)
				windowsVisibleState[x] = windows.get(x).isVisible();
			// Needed for KDE - so that JavaMod disappears in task bar when moved
			// to the System Tray
			// But has the downside, that that pattern, sample, instrument
			// dialog get hidden before visibility status can be saved
			//dispose();
			setVisible(false);
		}
	}
	/**
	 * Do what is needed to deiconify the MainForm, if in system tray.
	 * This will result in the event "windowDeiconified", which
	 * is handled above (see initialize, anonymous class)
	 * Is also called in "doClose" when frame is iconified into the
	 * system tray - we need to restore windows to save positions and visible
	 * state to the property file.
	 * @since 26.01.2022
	 */
	private void doDeIconify()
	{
		if (useSystemTray && (getExtendedState() & ICONIFIED)!=0)
		{
			setVisible(true);
			setExtendedState(getExtendedState() & ~ICONIFIED); // JFrame.setState is obsolete!!

			if (useSystemTray && windowsVisibleState!=null)
			{
				for (int x=0; x<windows.size(); x++)
					windows.get(x).setVisible(windowsVisibleState[x]);
				windowsVisibleState = null;
			}
		}
	}
	/**
	 *
	 * @since 07.02.2012
	 */
	private TrayIcon getTrayIcon()
	{
		if (javaModTrayIcon==null && SystemTray.isSupported())
		{
			final java.net.URL iconURL = MainForm.class.getResource(DEFAULTTRAYICONPATH);
			if (iconURL!=null)
			{
				final Image trayIconImage = Toolkit.getDefaultToolkit().getImage(iconURL);
				final Dimension trayIconSize = SystemTray.getSystemTray().getTrayIconSize();
				// The icon is not quadratic so to keep aspect ratio, the smaller width is set to -1
				javaModTrayIcon = new TrayIcon(trayIconImage.getScaledInstance(-1, trayIconSize.height, Image.SCALE_SMOOTH));
				javaModTrayIcon.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(final MouseEvent e)
					{
						if (SwingUtilities.isLeftMouseButton(e))
						{
							MainForm.this.doDeIconify();
						}
					}
				});
				// Add components to pop-up menu
				final PopupMenu popUp = new PopupMenu();
				popUp.add(getAboutItem());
				popUp.addSeparator();
				popUp.add(getPlayItem());
				popUp.add(getPauseItem());
				popUp.add(getStopItem());
				popUp.add(getPrevItem());
				popUp.add(getNextItem());
				popUp.addSeparator();
				popUp.add(getCloseItem());
				javaModTrayIcon.setPopupMenu(popUp);
			}
		}
		return javaModTrayIcon;
	}
	/**
	 * @since 12.12.2023
	 * @param newToolTip
	 */
	private void setTrayIconToolTip(final String newToolTip)
	{
		if (SystemTray.isSupported())
		{
			final TrayIcon trayIcon = getTrayIcon();
			if (trayIcon!=null) trayIcon.setToolTip(newToolTip);
		}
	}
	/**
	 *
	 * @since 07.02.2012
	 */
	private void setSystemTray()
	{
		try
		{
			// Check the SystemTray is supported
			if (SystemTray.isSupported())
			{
				final SystemTray tray = SystemTray.getSystemTray();
				final TrayIcon trayIcon = getTrayIcon();
				if (tray!=null && trayIcon!=null)
				{
					tray.remove(trayIcon);
					if (useSystemTray) tray.add(trayIcon);
				}
			}
		}
		catch (final AWTException e)
		{
			Log.error("TrayIcon could not be added.", e);
		}
	}
	/**
	 * Get a List of sized window icons
	 * @since 10.04.2020
	 * @return
	 */
	private ArrayList<Image> getWindowIconImages(final String path)
	{
		if (windowIcons==null)
		{
			final java.net.URL iconURL = MainForm.class.getResource(path);
			if (iconURL!=null)
			{
				final Image tempImage = Toolkit.getDefaultToolkit().getImage(iconURL);
				// The icon is not quadratic so to keep aspect ratio, the smaller width is set to -1
				windowIcons = new ArrayList<>();
				// Create some typical dimensions of our Icon for Java to use.
				windowIcons.add(tempImage.getScaledInstance(-1,  16, Image.SCALE_SMOOTH));
				windowIcons.add(tempImage.getScaledInstance(-1,  20, Image.SCALE_SMOOTH));
				windowIcons.add(tempImage.getScaledInstance(-1,  32, Image.SCALE_SMOOTH));
				windowIcons.add(tempImage.getScaledInstance(-1,  40, Image.SCALE_SMOOTH));
				windowIcons.add(tempImage.getScaledInstance(-1,  64, Image.SCALE_SMOOTH));
				windowIcons.add(tempImage.getScaledInstance(-1, 128, Image.SCALE_SMOOTH));
				// create all sizes from 16 - 128
//				for (int size=16; size<=128; size+=2)
//					windowIcons.add(tempImage.getScaledInstance(-1,  size, Image.SCALE_SMOOTH));
			}
		}
		return windowIcons;
	}
	public JPanel getBaseContentPane()
	{
		if (baseContentPane==null)
		{
			baseContentPane = new JPanel();
			baseContentPane.setName("baseContentPane");
			baseContentPane.setLayout(new BorderLayout());

			baseContentPane.add(getMessages(), BorderLayout.SOUTH);
			baseContentPane.add(getMainContentPane(), BorderLayout.CENTER);
		}
		return baseContentPane;
	}
	public JTextField getMessages()
	{
		if (messages==null)
		{
			messages = new JTextField();
			messages.setName("messages");
			messages.setEditable(false);
			messages.setFont(Helpers.getDialogFont());
		}
		return messages;
	}
	public JPanel getMainContentPane()
	{
		if (mainContentPane==null)
		{
			mainContentPane = new JPanel();
			mainContentPane.setName("mainContentPane");
			mainContentPane.setLayout(new GridBagLayout());

			mainContentPane.add(getMusicDataPane(),		Helpers.getGridBagConstraint(0, 0, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 0.0, 1.0));
			mainContentPane.add(getPlayerDataPane(),	Helpers.getGridBagConstraint(0, 1, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 0.0, 1.0));
			mainContentPane.add(getPlayerControlPane(),	Helpers.getGridBagConstraint(0, 2, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 0.0, 0.0));
		}
		return mainContentPane;
	}
	private JavaModAbout getJavaModAbout()
	{
		if (about == null)
		{
			about = new JavaModAbout(this, true);
			about.addWindowFocusListener(makeMainWindowVisiable);
		}
		else
			about.setLocation(Helpers.getFrameCenteredLocation(about, this));
		return about;
	}
	private UrlDialog getURLDialog()
	{
		if (urlDialog == null)
		{
			urlDialog = new UrlDialog(this, true, Helpers.EMPTY_STING);
			urlDialog.addWindowFocusListener(makeMainWindowVisiable);
		}
		else
			urlDialog.setLocation(Helpers.getFrameCenteredLocation(urlDialog, this));
		return urlDialog;
	}
	public JDialog getEffectDialog()
	{
		if (equalizerDialog==null)
		{
			equalizerDialog = new JDialog(this, "Effect", false);
			equalizerDialog.setName("equalizerDialog");
			equalizerDialog.setSize(effectsDialogSize);
			equalizerDialog.setPreferredSize(effectsDialogSize);
			equalizerDialog.setContentPane(getEffectPane());
			if (effectsDialogLocation == null || (effectsDialogLocation.getX()==-1 || effectsDialogLocation.getY()==-1))
				effectsDialogLocation = Helpers.getFrameCenteredLocation(equalizerDialog, null);
			equalizerDialog.setLocation(effectsDialogLocation);
			equalizerDialog.addWindowFocusListener(makeMainWindowVisiable);
		}
		return equalizerDialog;
	}
	public JDialog getXmasConfigDialog()
	{
		if (xmasConfigDialog==null)
		{
			xmasConfigDialog = new JDialog(this, false);
			xmasConfigDialog.setTitle("X-Mas config");
			xmasConfigDialog.setName("equalizerDialog");
			xmasConfigDialog.setSize(xmasConfigDialogSize);
			xmasConfigDialog.setPreferredSize(xmasConfigDialogSize);
			xmasConfigDialog.setContentPane(getXmasConfigPanel());
			if (xmasConfigDialogLocation == null || (xmasConfigDialogLocation.getX()==-1 || xmasConfigDialogLocation.getY()==-1))
				xmasConfigDialogLocation = Helpers.getFrameCenteredLocation(xmasConfigDialog, null);
			xmasConfigDialog.setLocation(xmasConfigDialogLocation);
			xmasConfigDialog.addWindowFocusListener(makeMainWindowVisiable);
		}
		return xmasConfigDialog;
	}
	public JDialog getPlayerSetUpDialog()
	{
		if (playerSetUpDialog==null)
		{
			playerSetUpDialog = new JDialog(this, "Configuration", false);
			playerSetUpDialog.setName("playerSetUpDialog");
			playerSetUpDialog.setSize(playerSetUpDialogSize);
			playerSetUpDialog.setPreferredSize(playerSetUpDialogSize);
			playerSetUpDialog.setContentPane(getPlayerSetUpPane());
			if (playerSetUpDialogLocation == null || (playerSetUpDialogLocation.getX()==-1 || playerSetUpDialogLocation.getY()==-1))
				playerSetUpDialogLocation = Helpers.getFrameCenteredLocation(playerSetUpDialog, null);
			playerSetUpDialog.setLocation(playerSetUpDialogLocation);
			playerSetUpDialog.addWindowFocusListener(makeMainWindowVisiable);
		}
		return playerSetUpDialog;
	}
	public JDialog getMultimediaInfoDialog()
	{
		if (multimediaInfoDialog==null)
		{
			multimediaInfoDialog = new JDialog(this, "File properties", false);
			multimediaInfoDialog.setName("multimediaInfoDialog");
			multimediaInfoDialog.setSize(multimediaInfoDialogSize);
			multimediaInfoDialog.setPreferredSize(multimediaInfoDialogSize);
			if (multimediaInfoDialogLocation == null || (multimediaInfoDialogLocation.getX()==-1 || multimediaInfoDialogLocation.getY()==-1))
				multimediaInfoDialogLocation = Helpers.getFrameCenteredLocation(multimediaInfoDialog, null);
		    multimediaInfoDialog.setLocation(multimediaInfoDialogLocation);
		    multimediaInfoDialog.addWindowFocusListener(makeMainWindowVisiable);

		    multimediaInfoDialog.setContentPane(getMultimediaInfoPane());
		}
		return multimediaInfoDialog;
	}
	public JPanel getMultimediaInfoPane()
	{
		if (multimediaInfoPane==null)
		{
			multimediaInfoPane = new JPanel();
			multimediaInfoPane.setName("multimediaInfoPane");
			multimediaInfoPane.setLayout(new BorderLayout());
			multimediaInfoPane.setBorder(new TitledBorder(null, "Multimedia File Info", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			changeInfoPane();
		}
		return multimediaInfoPane;
	}
	public JDialog getPlaylistDialog()
	{
		if (playlistDialog==null)
		{
			playlistDialog = new JDialog(this, "Playlist", false);
			playlistDialog.setName("playlistDialog");
			playlistDialog.setSize(playlistDialogSize);
			playlistDialog.setPreferredSize(playlistDialogSize);
			playlistDialog.setContentPane(getPlaylistPane());
			if (playlistDialogLocation == null || (playlistDialogLocation.getX()==-1 || playlistDialogLocation.getY()==-1))
				playlistDialogLocation = Helpers.getFrameCenteredLocation(playlistDialog, null);
			playlistDialog.setLocation(playlistDialogLocation);
			playlistDialog.addWindowFocusListener(makeMainWindowVisiable);
		}
		return playlistDialog;
	}
	public JPanel getPlaylistPane()
	{
		if (playlistPane==null)
		{
			playlistPane = new JPanel();
			playlistPane.setName("playlistPane");
			playlistPane.setLayout(new BorderLayout());
			playlistPane.setBorder(new TitledBorder(null, "Playlist", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			playlistPane.add(getPlaylistGUI());
		}
		return playlistPane;
	}
	public PlayListGUI getPlaylistGUI()
	{
		if (playlistGUI==null)
		{
			playlistGUI = new PlayListGUI(getPlaylistDialog());
			playlistGUI.addPlaylistGUIChangeListener(this);
		}
		return playlistGUI;
	}
	public JPanel getEffectPane()
	{
		if (effectPane==null)
		{
			effectPane = new JPanel();
			effectPane.setName("effectPane");
			effectPane.setLayout(new BorderLayout());
			effectPane.setBorder(new TitledBorder(null, "Effects", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			effectPane.add(getEffectsPanel());
		}
		return effectPane;
	}
	public XmasConfigPanel getXmasConfigPanel()
	{
		if (xmasConfigPanel==null)
		{
			final DisplayMode mode = Helpers.getScreenInfoOf(this);
			final int refreshRate = (mode!=null && mode.getRefreshRate()!=DisplayMode.REFRESH_RATE_UNKNOWN)?mode.getRefreshRate():DEFAULT_REFRESH_RATE;
			xmasConfigPanel = new XmasConfigPanel(refreshRate);
			xmasConfigPanel.setName("xmasConfigPane");
		}
		return xmasConfigPanel;
	}
	private GraphicEqGUI getEqualizerGui()
	{
		if (equalizerGUI==null)
		{
			equalizerGUI = new GraphicEqGUI(currentEqualizer);
		}
		return equalizerGUI;
	}
	private PitchShiftGUI getPitchShiftGui()
	{
		if (pitchShiftGUI==null)
		{
			pitchShiftGUI = new PitchShiftGUI(currentPitchShift);
		}
		return pitchShiftGUI;
	}
	public EffectsPanel getEffectsPanel()
	{
		if (effectGUI==null)
		{
			final JPanel [] effectPanels =
			{
			 	getEqualizerGui(),
			 	getPitchShiftGui()
			};
			effectGUI = new EffectsPanel(this, effectPanels, audioProcessor);
		}
		return effectGUI;
	}
	public JPanel getPlayerSetUpPane()
	{
		if (playerSetUpPane==null)
		{
			playerSetUpPane = new JPanel();
			playerSetUpPane.setName("playerSetUpPane");
			playerSetUpPane.setLayout(new BorderLayout());
			playerSetUpPane.setBorder(new TitledBorder(null, "Mixer Control", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));
			playerSetUpPane.add(getPlayerConfigPanel());
			changeConfigPane();
		}
		return playerSetUpPane;
	}
	/**
	 * @since 10.12.2011
	 * @return
	 */
	public PlayerConfigPanel getPlayerConfigPanel()
	{
		if (playerConfigPanel==null)
		{
			playerConfigPanel = new PlayerConfigPanel();
		}
		return playerConfigPanel;
	}
	public SimpleProgressDialog getDownloadDialog()
	{
		if (downloadDialog==null)
		{
			downloadDialog = new SimpleProgressDialog(this, "Download progress");
			downloadDialog.setSize(350, 90);
			downloadDialog.setPreferredSize(downloadDialog.getSize());
			downloadDialog.pack();
		}
		return downloadDialog;
	}
	public DoubleProgressDialog getExportDialog()
	{
		if (exportDialog==null)
		{
			exportDialog = new DoubleProgressDialog(this, "Export progress");
			exportDialog.setSize(350, 130);
			exportDialog.setPreferredSize(exportDialog.getSize());
			exportDialog.pack();
		}
		return exportDialog;
	}
	public SAMeterPanel getSALMeterPanel()
	{
		if (saLMeterPanel==null)
		{
			final DisplayMode mode = Helpers.getScreenInfoOf(this);
			final int refreshRate = (mode!=null && mode.getRefreshRate()!=DisplayMode.REFRESH_RATE_UNKNOWN)?mode.getRefreshRate():DEFAULT_REFRESH_RATE;
			saLMeterPanel = new SAMeterPanel(refreshRate, 25);
			final Dimension d = new Dimension(104, 60);
			saLMeterPanel.setSize(d);
			saLMeterPanel.setMaximumSize(d);
			saLMeterPanel.setMinimumSize(d);
			saLMeterPanel.setPreferredSize(d);
			saLMeterPanel.setDoubleBuffered(true);
			saLMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return saLMeterPanel;
	}
	public SAMeterPanel getSARMeterPanel()
	{
		if (saRMeterPanel==null)
		{
			final DisplayMode mode = Helpers.getScreenInfoOf(this);
			final int refreshRate = (mode!=null && mode.getRefreshRate()!=DisplayMode.REFRESH_RATE_UNKNOWN)?mode.getRefreshRate():DEFAULT_REFRESH_RATE;
			saRMeterPanel = new SAMeterPanel(refreshRate, 25);
			final Dimension d = new Dimension(104, 60);
			saRMeterPanel.setSize(d);
			saRMeterPanel.setMaximumSize(d);
			saRMeterPanel.setMinimumSize(d);
			saRMeterPanel.setPreferredSize(d);
			saRMeterPanel.setDoubleBuffered(true);
			saRMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return saRMeterPanel;
	}
	public VUMeterPanel getVULMeterPanel()
	{
		if (vuLMeterPanel==null)
		{
			final DisplayMode mode = Helpers.getScreenInfoOf(this);
			final int refreshRate = (mode!=null && mode.getRefreshRate()!=DisplayMode.REFRESH_RATE_UNKNOWN)?mode.getRefreshRate():DEFAULT_REFRESH_RATE;
			vuLMeterPanel = new VUMeterPanel(refreshRate);
			final Dimension d = new Dimension(20, 100);
			vuLMeterPanel.setSize(d);
			vuLMeterPanel.setMaximumSize(d);
			vuLMeterPanel.setMinimumSize(d);
			vuLMeterPanel.setPreferredSize(d);
			vuLMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return vuLMeterPanel;
	}
	public VUMeterPanel getVURMeterPanel()
	{
		if (vuRMeterPanel==null)
		{
			final DisplayMode mode = Helpers.getScreenInfoOf(this);
			final int refreshRate = (mode!=null && mode.getRefreshRate()!=DisplayMode.REFRESH_RATE_UNKNOWN)?mode.getRefreshRate():DEFAULT_REFRESH_RATE;
			vuRMeterPanel = new VUMeterPanel(refreshRate);
			final Dimension d = new Dimension(20, 100);
			vuRMeterPanel.setSize(d);
			vuRMeterPanel.setMaximumSize(d);
			vuRMeterPanel.setMinimumSize(d);
			vuRMeterPanel.setPreferredSize(d);
			vuRMeterPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return vuRMeterPanel;
	}
	public JPanel getMusicDataPane()
	{
		if (musicDataPane==null)
		{
			musicDataPane = new JPanel();
			musicDataPane.setName("musicDataPane");
			musicDataPane.setLayout(new GridBagLayout());
			musicDataPane.setBorder(new TitledBorder(null, "Name", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));

			musicDataPane.add(getLEDScrollPanel(), Helpers.getGridBagConstraint(0, 0, 1, 0, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
		}
		return musicDataPane;
	}
	public LEDScrollPanel getLEDScrollPanel()
	{
		final int chars = 15; // show 15 chars
		final int brick = 3;  // one brick is 3x3 pixel
		if (ledScrollPanel==null)
		{
			final DisplayMode mode = Helpers.getScreenInfoOf(this);
			final int refreshRate = (mode!=null && mode.getRefreshRate()!=DisplayMode.REFRESH_RATE_UNKNOWN)?mode.getRefreshRate():DEFAULT_REFRESH_RATE;
			ledScrollPanel = new LEDScrollPanel(refreshRate, Helpers.FULLVERSION + ' ' + Helpers.COPYRIGHT + "                  ", chars, Color.GREEN, Color.GRAY);
			final Dimension d = new Dimension((chars*brick*6)+4, (brick*8)+4);
			ledScrollPanel.setSize(d);
			ledScrollPanel.setMaximumSize(d);
			ledScrollPanel.setMinimumSize(d);
			ledScrollPanel.setPreferredSize(d);
			ledScrollPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		}
		return ledScrollPanel;
	}
	public JPanel getPlayerDataPane()
	{
		if (playerDataPane==null)
		{
			playerDataPane = new JPanel();
			playerDataPane.setName("playerDataPane");
			playerDataPane.setLayout(new GridBagLayout());
			playerDataPane.setBorder(new TitledBorder(null, "Player Data", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));

			playerDataPane.add(getVULMeterPanel(), Helpers.getGridBagConstraint(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerDataPane.add(getSALMeterPanel(), Helpers.getGridBagConstraint(1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerDataPane.add(getSARMeterPanel(), Helpers.getGridBagConstraint(2, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerDataPane.add(getVURMeterPanel(), Helpers.getGridBagConstraint(3, 0, 1, 0, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
		}
		return playerDataPane;
	}
	public JPanel getPlayerControlPane()
	{
		if (playerControlPane==null)
		{
			playerControlPane = new JPanel();
			playerControlPane.setName("playerControlPane");
			playerControlPane.setLayout(new GridBagLayout());
			playerControlPane.setBorder(new TitledBorder(null, "Player Control", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, Helpers.getDialogFont(), null));

			playerControlPane.add(getButton_Prev(),		Helpers.getGridBagConstraint(0, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerControlPane.add(getButton_Play(),		Helpers.getGridBagConstraint(1, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerControlPane.add(getButton_Next(),		Helpers.getGridBagConstraint(2, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerControlPane.add(getButton_Pause(),	Helpers.getGridBagConstraint(3, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerControlPane.add(getButton_Stop(),		Helpers.getGridBagConstraint(4, 0, 2, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerControlPane.add(getVolumeSlider(),	Helpers.getGridBagConstraint(5, 0, 1, 1, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 0.0, 1.0));
			playerControlPane.add(getBalanceSlider(),	Helpers.getGridBagConstraint(6, 0, 1, 0, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 0.0, 1.0));
			playerControlPane.add(getVolumeLabel(),		Helpers.getGridBagConstraint(5, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerControlPane.add(getBalanceLabel(),	Helpers.getGridBagConstraint(6, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0));
			playerControlPane.add(getSeekBarPanel(),	Helpers.getGridBagConstraint(0, 2, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, 1.0, 1.0));
		}
		return playerControlPane;
	}
	private SeekBarPanel getSeekBarPanel()
	{
		if (seekBarPanel==null)
		{
			final DisplayMode mode = Helpers.getScreenInfoOf(this);
			final int refreshRate = (mode!=null && mode.getRefreshRate()!=DisplayMode.REFRESH_RATE_UNKNOWN)?mode.getRefreshRate():DEFAULT_REFRESH_RATE;
			seekBarPanel = new SeekBarPanel(refreshRate, false);
			seekBarPanel.setName("SeekBarPanel");
			seekBarPanel.addListener(new SeekBarPanelListener()
			{
				@Override
				public void valuesChanged(final long milliseconds)
				{
					if (currentPlayList!=null && playerThread!=null && playerThread.isRunning())
						currentPlayList.setCurrentElementByTimeIndex(milliseconds);
				}
			});
		}
		return seekBarPanel;
	}
	private JButton getButton_Play()
	{
		if (button_Play == null)
		{
			buttonPlay_normal = new ImageIcon(getClass().getResource(BUTTONPLAY_NORMAL));
			buttonPlay_Inactive = new ImageIcon(getClass().getResource(BUTTONPLAY_INACTIVE));
			buttonPlay_Active = new ImageIcon(getClass().getResource(BUTTONPLAY_ACTIVE));

			button_Play = new JButton();
			button_Play.setName("button_Play");
			button_Play.setText(Helpers.EMPTY_STING);
			button_Play.setToolTipText("play");
			button_Play.setHorizontalTextPosition(SwingConstants.CENTER);
			button_Play.setVerticalTextPosition(SwingConstants.BOTTOM);
			button_Play.setIcon(buttonPlay_normal);
			button_Play.setDisabledIcon(buttonPlay_Inactive);
			button_Play.setPressedIcon(buttonPlay_Active);
			button_Play.setMargin(new Insets(4, 6, 4, 6));
			button_Play.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doStartPlaying();
				}
			});
		}
		return button_Play;
	}
	private JButton getButton_Pause()
	{
		if (button_Pause == null)
		{
			buttonPause_normal = new ImageIcon(getClass().getResource(BUTTONPAUSE_NORMAL));
			buttonPause_Inactive = new ImageIcon(getClass().getResource(BUTTONPAUSE_INACTIVE));
			buttonPause_Active = new ImageIcon(getClass().getResource(BUTTONPAUSE_ACTIVE));

			button_Pause = new JButton();
			button_Pause.setName("button_Pause");
			button_Pause.setText(Helpers.EMPTY_STING);
			button_Pause.setToolTipText("pause");
			button_Pause.setHorizontalTextPosition(SwingConstants.CENTER);
			button_Pause.setVerticalTextPosition(SwingConstants.BOTTOM);
			button_Pause.setIcon(buttonPause_normal);
			button_Pause.setDisabledIcon(buttonPause_Inactive);
			button_Pause.setPressedIcon(buttonPause_Active);
			button_Pause.setMargin(new Insets(4, 6, 4, 6));
			button_Pause.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doPausePlaying();
				}
			});
		}
		return button_Pause;
	}
	private JButton getButton_Stop()
	{
		if (button_Stop == null)
		{
			buttonStop_normal = new ImageIcon(getClass().getResource(BUTTONSTOP_NORMAL));
			buttonStop_Inactive = new ImageIcon(getClass().getResource(BUTTONSTOP_INACTIVE));
			buttonStop_Active = new ImageIcon(getClass().getResource(BUTTONSTOP_ACTIVE));

			button_Stop = new JButton();
			button_Stop.setName("button_Stop");
			button_Stop.setText(Helpers.EMPTY_STING);
			button_Stop.setToolTipText("stop");
			button_Stop.setHorizontalTextPosition(SwingConstants.CENTER);
			button_Stop.setVerticalTextPosition(SwingConstants.BOTTOM);
			button_Stop.setIcon(buttonStop_normal);
			button_Stop.setDisabledIcon(buttonStop_Inactive);
			button_Stop.setPressedIcon(buttonStop_Active);
			button_Stop.setMargin(new Insets(4, 6, 4, 6));
			button_Stop.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doStopPlaying();
				}
			});
		}
		return button_Stop;
	}
	private JButton getButton_Prev()
	{
		if (button_Prev == null)
		{
			buttonPrev_normal = new ImageIcon(getClass().getResource(BUTTONPREV_NORMAL));
			buttonPrev_Inactive = new ImageIcon(getClass().getResource(BUTTONPREV_INACTIVE));
			buttonPrev_Active = new ImageIcon(getClass().getResource(BUTTONPREV_ACTIVE));

			button_Prev = new JButton();
			button_Prev.setName("button_Prev");
			button_Prev.setText(Helpers.EMPTY_STING);
			button_Prev.setToolTipText("previous");
			button_Prev.setHorizontalTextPosition(SwingConstants.CENTER);
			button_Prev.setVerticalTextPosition(SwingConstants.BOTTOM);
			button_Prev.setIcon(buttonPrev_normal);
			button_Prev.setDisabledIcon(buttonPrev_Inactive);
			button_Prev.setPressedIcon(buttonPrev_Active);
			button_Prev.setMargin(new Insets(4, 6, 4, 6));
			button_Prev.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doPrevPlayListEntry();
				}
			});
		}
		return button_Prev;
	}
	private JButton getButton_Next()
	{
		if (button_Next == null)
		{
			buttonNext_normal = new ImageIcon(getClass().getResource(BUTTONNEXT_NORMAL));
			buttonNext_Inactive = new ImageIcon(getClass().getResource(BUTTONNEXT_INACTIVE));
			buttonNext_Active = new ImageIcon(getClass().getResource(BUTTONNEXT_ACTIVE));

			button_Next = new JButton();
			button_Next.setName("button_Next");
			button_Next.setText(Helpers.EMPTY_STING);
			button_Next.setToolTipText("next");
			button_Next.setHorizontalTextPosition(SwingConstants.CENTER);
			button_Next.setVerticalTextPosition(SwingConstants.BOTTOM);
			button_Next.setIcon(buttonNext_normal);
			button_Next.setDisabledIcon(buttonNext_Inactive);
			button_Next.setPressedIcon(buttonNext_Active);
			button_Next.setMargin(new Insets(4, 6, 4, 6));
			button_Next.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					doNextPlayListEntry();
				}
			});
		}
		return button_Next;
	}
	public JLabel getVolumeLabel()
	{
		if (volumeLabel==null)
		{
			volumeLabel = new JLabel("Volume");
			volumeLabel.setFont(Helpers.getDialogFont());
		}
		return volumeLabel;
	}
	public RoundSlider getVolumeSlider()
	{
		if (volumeSlider==null)
		{
			volumeSlider = new RoundSlider();
			volumeSlider.setSize(new Dimension(20,20));
			volumeSlider.setMinimumSize(new Dimension(20,20));
			volumeSlider.setMaximumSize(new Dimension(20,20));
			volumeSlider.setPreferredSize(new Dimension(20,20));
			volumeSlider.setValue(currentVolume);
			volumeSlider.setToolTipText(Float.toString(currentVolume*100f) + '%');
			volumeSlider.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(final MouseEvent e)
				{
					final RoundSlider slider = (RoundSlider) e.getSource();
					if (e.getClickCount()>1)
					{
						slider.setValue(0.5f);
						e.consume();
					}
				}
			});
			volumeSlider.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(final ChangeEvent e)
				{
					final RoundSlider slider = (RoundSlider) e.getSource();
					currentVolume = slider.getValue();
					if (currentVolume<0) currentVolume=0;
					else
					if (currentVolume>1) currentVolume=1;
					slider.setToolTipText(Float.toString(currentVolume*100f) + '%');
					doSetVolumeValue();
				}
			});
		}
		return volumeSlider;
	}
	public JLabel getBalanceLabel()
	{
		if (balanceLabel==null)
		{
			balanceLabel = new JLabel("Balance");
			balanceLabel.setFont(Helpers.getDialogFont());
		}
		return balanceLabel;
	}
	public RoundSlider getBalanceSlider()
	{
		if (balanceSlider==null)
		{
			balanceSlider = new RoundSlider();
			balanceSlider.setSize(new Dimension(20,20));
			balanceSlider.setMinimumSize(new Dimension(20,20));
			balanceSlider.setMaximumSize(new Dimension(20,20));
			balanceSlider.setPreferredSize(new Dimension(20,20));
			balanceSlider.setValue((currentBalance + 1f)/2f);
			balanceSlider.setToolTipText(Float.toString(currentBalance*100f) + '%');
			balanceSlider.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(final MouseEvent e)
				{
					final RoundSlider slider = (RoundSlider) e.getSource();
					if (e.getClickCount()>1)
					{
						slider.setValue(0.5f);
						e.consume();
					}
				}
			});
			balanceSlider.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(final ChangeEvent e)
				{
					final RoundSlider slider = (RoundSlider) e.getSource();
					currentBalance = (slider.getValue()*2f)-1f;
					slider.setToolTipText(Float.toString(currentBalance*100f) + '%');
					doSetBalanceValue();
				}
			});
		}
		return balanceSlider;
	}
	/* DspAudioProcessor CallBack -------------------------------------------*/
	@Override
	public void currentSampleChanged(final float [] leftSample, final float [] rightSample)
	{
		getVULMeterPanel().setVUMeter(leftSample);
		getVURMeterPanel().setVUMeter(rightSample);

		getSALMeterPanel().setMeter(leftSample);
		getSARMeterPanel().setMeter(rightSample);
	}
	@Override
	public void multimediaContainerEventOccured(final MultimediaContainerEvent event)
	{
		if (event.getType() == MultimediaContainerEvent.SONG_NAME_CHANGED)
			getLEDScrollPanel().addScrollText(event.getEvent().toString() + Helpers.SCROLLY_BLANKS);
		else
		if (event.getType() == MultimediaContainerEvent.SONG_NAME_CHANGED_OLD_INVALID)
			getLEDScrollPanel().setScrollTextTo(event.getEvent().toString() + Helpers.SCROLLY_BLANKS);
		setTrayIconToolTip(event.getEvent().toString());
	}
	/**
	 * @param thread
	 * @see de.quippy.javamod.main.gui.PlayThreadEventListener#playThreadEventOccured(de.quippy.javamod.main.gui.PlayThread)
	 */
	@Override
	public void playThreadEventOccured(final PlayThread thread)
	{
		if (thread.isRunning())
		{
			getButton_Play().setIcon(buttonPlay_Active);
		}
		else // Signaling: not running-->Piece finished...
		{
			getButton_Play().setIcon(buttonPlay_normal);
			if (thread.getHasFinishedNormaly())
			{
				final boolean ok = doNextPlayListEntry();
				if (!ok) doStopPlaying();
			}
		}

		final Mixer mixer = thread.getCurrentMixer();
		if (mixer!=null)
		{
			if (mixer.isPaused())
				getButton_Pause().setIcon(buttonPause_Active);
			else
				getButton_Pause().setIcon(buttonPause_normal);
		}
	}
	private void setPlayListIcons()
	{
		if (currentPlayList==null)
		{
			getButton_Prev().setEnabled(false);
			getButton_Next().setEnabled(false);
		}
		else
		{
			getButton_Prev().setEnabled(currentPlayList.hasPrevious());
			getButton_Next().setEnabled(currentPlayList.hasNext());
		}
		getPrevItem().setEnabled(getButton_Prev().isEnabled());
		getNextItem().setEnabled(getButton_Next().isEnabled());
	}
	/* EVENT METHODS --------------------------------------------------------*/
	/**
	 * @since 29.12.2018
	 */
	private void doArrangeWindows()
	{
		final int mainDialogWidth = getWidth();
		final int mainDialogHight = getHeight();
		final int playerSetUpDialogHight = getPlayerSetUpDialog().getHeight();
		final int modInfoDialogWidth = getMultimediaInfoDialog().getWidth();
		final int playlistDialogWidth = getPlaylistDialog().getWidth();
		final int playerSetUpDialogWidth = (mainDialogWidth - getInsets().right + 1) + playlistDialogWidth - getPlaylistDialog().getInsets().left + 1;
		final int modInfoDialogHight = (mainDialogHight - getInsets().bottom + 1) + playerSetUpDialogHight - 1;

		getMultimediaInfoDialog().setSize(modInfoDialogWidth, modInfoDialogHight);
		getPlayerSetUpDialog().setSize(playerSetUpDialogWidth, playerSetUpDialogHight);
		getPlaylistDialog().setSize(playlistDialogWidth, mainDialogHight + 1);

		getPlayerSetUpDialog().setLocation(getX() - 1,getY() + mainDialogHight - getInsets().bottom);
		getMultimediaInfoDialog().setLocation(getX() - modInfoDialogWidth + getInsets().left + getMultimediaInfoDialog().getInsets().right - 1, getY());
		getPlaylistDialog().setLocation(getX() +  mainDialogWidth - getInsets().right - getPlaylistDialog().getInsets().left + 1, getY());
	}
	/**
	 * Default Close Operation
	 * @since 22.06.2006
	 */
	private void doClose()
	{
		// set visible, if system tray active and frame is iconified
		doDeIconify();

		doStopPlaying();

		// Stop update threads on all ThreadUpdatePanels
		getSeekBarPanel().stopThread();
		getVULMeterPanel().stopThread();
		getVURMeterPanel().stopThread();
		getSALMeterPanel().stopThread();
		getSARMeterPanel().stopThread();
		getLEDScrollPanel().stopThread();
		getXmasConfigPanel().stopThreads();

		// Write the property file
		writePropertyFile();
		// remove listeners
		if (audioProcessor!=null) audioProcessor.removeListener(this);
		MultimediaContainerManager.removeMultimediaContainerEventListener(this);
		MultimediaContainerManager.cleanUpAllContainers();

		useSystemTray = false; setSystemTray();

		for (final Window win : windows)
		{
			win.setVisible(false);
			win.dispose();
		}
		setVisible(false);
		dispose();

		System.exit(0); // this should not be needed!
	}
	/**
	 * Open a new ModFile
	 * @since 22.06.2006
	 */
	private void doOpenFile()
	{
		final FileChooserResult selectedFile = Helpers.selectFileNameFor(this, searchPath, "Load a Sound-File", fileFilterLoad, false, 0, true, false);
		if (selectedFile!=null)
			doOpenFile(selectedFile.getSelectedFiles());
	}
	/**
	 * Open a new File
	 * @since 22.06.2006
	 */
	public void doOpenFile(final File[] files)
	{
	    if (files!=null)
	    {
	    	if (files.length==1)
	    	{
	    		final File f = files[0];
		    	if (f.isFile())
		    	{
			    	final String modFileName = f.getAbsolutePath();
			    	final int i = modFileName.lastIndexOf(File.separatorChar);
			    	searchPath = modFileName.substring(0, i);
		    		loadMultimediaOrPlayListFile(Helpers.createURLfromFile(f));
		    	}
		    	else
		    	if (f.isDirectory())
		    	{
			    	searchPath = f.getAbsolutePath();
		    	}
	    	}
	    	else
	    	{
	    		playlistRecieved(null, PlayList.createNewListWithFiles(files, false, false), null);
	    	}
	    }
	}
	/**
	 * Open a new ModFile
	 * @since 17.10.2007
	 */
	private void doOpenURL()
	{
		getURLDialog().setVisible(true);
		final String url = getURLDialog().getURL();
		if (url!=null && !url.isEmpty()) doOpenURL(url);
	}
	/**
	 * Open a new File
	 * @since 22.06.2006
	 */
	public void doOpenURL(final String surl)
	{
	    if (surl!=null)
	    {
	    	loadMultimediaOrPlayListFile(Helpers.createURLfromString(surl));
	    }
	}
	/**
	 * @since 08.11.2019
	 * @param askPlayback
	 */
	private void doExportToWave()
	{
		doStopPlaying();

		if (currentContainer==null)
	    {
	    	JOptionPane.showMessageDialog(this, "You need to load a file first!", "Error", JOptionPane.ERROR_MESSAGE);
	    }
	    else
	    {
			final URL currentFile = currentContainer.getFileURL();
	    	String fileName = (Helpers.isHTTP(currentFile))?Helpers.getFileNameFromURL(currentFile):Helpers.createLocalFileStringFromURL(currentContainer.getFileURL(), true);
			fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar)+1);
			final String exportToWav = exportPath + File.separatorChar + fileName + ".WAV";
	    	final boolean ready = false;
	    	while (!ready)
		    {
				final FileChooserResult selectedFile = Helpers.selectFileNameFor(this, exportToWav, "Export to wave", fileFilterExport, false, 1, false, false);
				if (selectedFile!=null)
				{
					final File f = selectedFile.getSelectedFile();
				    if (f!=null)
				    {
				    	if (f.exists())
				    	{
				    		final int result = JOptionPane.showConfirmDialog(this, "File already exists! Overwrite?", "Overwrite confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				    		if (result==JOptionPane.CANCEL_OPTION) return;
				    		if (result==JOptionPane.NO_OPTION) continue; // Reselect
				    		final boolean ok = f.delete();
				    		if (!ok)
				    		{
		        		    	JOptionPane.showMessageDialog(MainForm.this, "Overwrite failed. Is file write protected or in use?", "Failed", JOptionPane.ERROR_MESSAGE);
		        		    	return;
				    		}
				    	}
			    		final boolean playDuringExport = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Continue playback while exporting?", "Playback?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			    		// set default export path if changed
				    	final String modFileName = f.getAbsolutePath();
				    	final int i = modFileName.lastIndexOf(File.separatorChar);
				    	exportPath = modFileName.substring(0, i);

						final Mixer mixer = createNewMixer();
				    	mixer.setPlayDuringExport(playDuringExport);
				    	mixer.setExportFile(f);
				    	playerThread = new PlayThread(mixer, this);
				    	playerThread.start();
				    }
				}
		    	return;
		    }
	    }
	}

	private void doExportFromPlaylist(final boolean convert)
	{
    	final PlayList playList = getPlaylistGUI().getPlayList();
    	if (playList!=null)
    	{
    		new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					PlayListEntry [] entries = playList.getSelectedEntries();
					if (entries==null) entries = playList.getAllEntries();
				    final FileChooserResult chooserResult = Helpers.selectFileNameFor(MainForm.this, exportPath, "Export here", fileFilterExport, false, 0, false, true);
				    if (chooserResult!=null)
				    {
				    	final File destinationDir = chooserResult.getSelectedFile();
		    			if (destinationDir.isDirectory() && destinationDir.canWrite())
		    			{
		    				int c = ((int)Math.log10(entries.length)) + 1; // amount of leading zeros
		    				if (c<2) c = 2; // always one leading zero
		    				getExportDialog().setLocation(Helpers.getFrameCenteredLocation(getExportDialog(), MainForm.this));
		    				getExportDialog().setVisible(true);
		    				getExportDialog().setGeneralMinimum(0);
		    				getExportDialog().setGeneralMaximum(entries.length);
					    	try
					    	{
			    				for (int i=0; i<entries.length; i++)
			    				{
			    					final PlayListEntry entry = entries[i];
			    					final String newFileName = String.format("%s%c%0"+c+"d - %s",
			    							destinationDir.getAbsolutePath(),
			    							Character.valueOf(File.separatorChar),
			    							Integer.valueOf(i+1),
			    							(convert)?Helpers.sanitizeFilename(entry.getFormattedName()+".wav"):Helpers.getFileNameFromURL(entry.getFile()));
				        			final File destination = new File(newFileName.toString());
				        			getExportDialog().setCurrentFileName(destination.getAbsolutePath());
				        			getExportDialog().setGeneralValue(i);
							    	if (destination.exists())
							    	{
							    		final int owresult = JOptionPane.showConfirmDialog(MainForm.this, destination +"\nalready exists! Overwrite?", "Overwrite confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
							    		if (owresult==JOptionPane.CANCEL_OPTION)
							    		{
							    			getExportDialog().setVisible(false);
							    			return;
							    		}
							    		if (owresult==JOptionPane.NO_OPTION) continue; // next File
							    		final boolean ok = destination.delete();
							    		if (!ok && destination.exists())
							    		{
					        		    	JOptionPane.showMessageDialog(MainForm.this, "Overwrite failed. Is file write protected or in use?", "Failed", JOptionPane.ERROR_MESSAGE);
					        		    	continue;
							    		}
							    	}
							    	if (convert)
							    		exportFileToWave(entry.getFile(), destination, entry.getTimeIndex(), entry.getDuration(), getExportDialog());
							    	else
								    	Helpers.copyFromURL(entry.getFile(), destination, getExportDialog());
			    				}
					    	}
					    	finally
					    	{
					    		getExportDialog().setVisible(false);
					    	}
		    			}
				    }
				}
		    }).start();
    	}
	}
	/**
	 * @author Daniel Becker
	 * @since 15.12.2020
	 * This updater for the progress bars
	 */
	private static class Updater extends Thread
	{
		private volatile boolean finished;
		private Mixer mixer;
		private long fromMillisecondPosition;
		private ProgressDialog progress;

		public Updater(final Mixer mixer, final long fromMillisecondPosition, final long duration, final ProgressDialog progress)
		{
			super();
			if (progress!=null)
			{
				this.mixer = mixer;
				this.fromMillisecondPosition = fromMillisecondPosition;
				this.progress = progress;
				this.progress.setDetailMinimum(0);
				this.progress.setDetailMaximum((int)duration);
				this.finished = false;
			}
			else
				finished = true;
		}
		public void stopMe()
		{
			finished = true;
		}
		@Override
		public void run()
		{
			while (!finished)
			{
				progress.setDetailValue((int)(mixer.getMillisecondPosition() - fromMillisecondPosition));
				try { Thread.sleep(10L); } catch (final InterruptedException ex) { /*NOOP*/ }
			}
		}
	}
	/**
	 * @since 09.11.2019
	 * @param sourceFile
	 * @param targetFile
	 * @param fromMillisecondPosition
	 * @param duration -1: no limit, else will stop playback after duration (in milliseconds) is reached
	 * @param dowloadDialog
	 */
	private void exportFileToWave(final URL sourceFile, final File targetFile, final long fromMillisecondPosition, final long duration, final ProgressDialog progress)
	{
		Updater updater = null;
		try
		{
			final MultimediaContainer newContainer = MultimediaContainerManager.getMultimediaContainer(sourceFile);
			if (newContainer!=null)
			{
				final Mixer mixer = getCurrentContainer().createNewMixer();
				if (mixer!=null)
				{
					mixer.setAudioProcessor(null);
					mixer.setVolume(currentVolume);
					mixer.setBalance(currentBalance);
					mixer.setSoundOutputStream(getSoundOutputStream());
			    	mixer.setPlayDuringExport(false);
			    	mixer.setExportFile(targetFile);
			    	mixer.setMillisecondPosition(fromMillisecondPosition);
			    	if (duration>-1) mixer.setStopMillisecondPosition(fromMillisecondPosition + duration);
		    		if (progress != null)
		    		{
			    		updater = new Updater(mixer, fromMillisecondPosition, duration, progress);
		    			updater.start();
		    		}
		    		mixer.startPlayback();
				}
			}
		}
		catch (final Throwable ex)
		{
			Log.error("[MainForm::exportToWave]", ex);
		}
		finally
		{
	    	if (updater!=null) updater.stopMe();
		}
	}
	private SimpleTextViewerDialog getSimpleTextViewerDialog()
	{
		if (simpleTextViewerDialog==null)
		{
			simpleTextViewerDialog = new SimpleTextViewerDialog(this, true);
		}
		simpleTextViewerDialog.setLocation(Helpers.getFrameCenteredLocation(simpleTextViewerDialog, this));
		return simpleTextViewerDialog;
	}
	private void doAutomaticUpdateCheck()
	{
		if (automaticUpdateCheck && today.minusDays(30).isAfter(lastUpdateCheck))
		{
			doCheckUpdate(true);
		}
	}
	private void doCheckUpdate(final boolean beSilent)
	{
		// This is necessary - to make the progress indicator work
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// first update last check date...
				lastUpdateCheck = LocalDate.now();

				final String serverVersion = Helpers.getCurrentServerVersion();
				final int compareResult = Helpers.compareVersions(Helpers.VERSION, serverVersion);
				if (compareResult<0)
				{
					final File f = new File(".");
		    		final String programmDestination = f.getAbsolutePath();
		    		// Show Version History
					final int resultHistory = JOptionPane.showConfirmDialog(MainForm.this, "There is a new version available!\n\nYour version: "+Helpers.VERSION+" - online verison: " + serverVersion + "\n\nWatch version history?\n\n", "New Version", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (resultHistory == JOptionPane.YES_OPTION)
					{
						final SimpleTextViewerDialog dialog = getSimpleTextViewerDialog();
						dialog.setDisplayTextFromURL(Helpers.VERSION_URL);
						dialog.setVisible(true);
					}
		    		// Ask for download
					final int result = JOptionPane.showConfirmDialog(MainForm.this, "Your version: "+Helpers.VERSION+" - online verison: " + serverVersion + "\n\nShould I start the download?\n\n", "New Version", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    		if (result == JOptionPane.YES_OPTION)
		    		{
		    			final JFileChooser chooser = new JFileChooser();
		    		    chooser.setDialogTitle("Select download destination");
		    		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    		    chooser.setAcceptAllFileFilterUsed(false);
		    		    chooser.setApproveButtonText("Save here");
		    		    do
		    		    {
		    		    	final int chooser_result = chooser.showOpenDialog(MainForm.this);
		    		    	if (chooser_result == JFileChooser.CANCEL_OPTION) return;
			    		    if (chooser_result == JFileChooser.APPROVE_OPTION)
			    		    {
			        		    final File destinationDir = chooser.getSelectedFile();
			        			final File destination = new File(destinationDir.getAbsolutePath() + File.separatorChar + "javamod.jar");
						    	if (destination.exists())
						    	{
						    		final int owresult = JOptionPane.showConfirmDialog(MainForm.this, "File already exists! Overwrite?", "Overwrite confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
						    		if (owresult==JOptionPane.CANCEL_OPTION) return;
						    		if (owresult==JOptionPane.NO_OPTION) continue; // Reselect
						    		final boolean ok = destination.delete();
						    		if (!ok && destination.exists())
						    		{
				        		    	JOptionPane.showMessageDialog(MainForm.this, "Overwrite failed. Is file write protected or in use?", "Failed", JOptionPane.ERROR_MESSAGE);
				        		    	return;
						    		}
						    	}

						    	getDownloadDialog().setLocation(Helpers.getFrameCenteredLocation(getDownloadDialog(), MainForm.this));
						    	getDownloadDialog().setCurrentFileName(Helpers.JAVAMOD_URL);
						    	getDownloadDialog().setVisible(true);
						    	final int copied = Helpers.downloadJavaMod(destination, getDownloadDialog());
			        		    getDownloadDialog().setVisible(false);
			        		    if (copied==-1)
			        		    	JOptionPane.showMessageDialog(MainForm.this, "Download failed!\n"+destination, "Failed", JOptionPane.ERROR_MESSAGE);
			        		    else
			        		    	JOptionPane.showMessageDialog(MainForm.this, "Saved "+copied+" bytes successfully to\n"+destination+"\n\nNow exit JavaMod, move the downloaded file to\n" + programmDestination + "\nand restart javamod.\n\n", "Success", JOptionPane.INFORMATION_MESSAGE);
			        		    return;
			    			}
		    		    }
		    		    while (true);
		    		}
				}
				else
				if (!beSilent)
				{
					if (compareResult>0)
					{
						JOptionPane.showMessageDialog(MainForm.this, "Your version of JavaMod is newer!", "Newer version", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						JOptionPane.showMessageDialog(MainForm.this, "Your version of JavaMod is up-to-date.", "Up-To-Date", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		}).start();
	}
	/**
	 * Display About-Dialog
	 * @since 22.06.2006
	 */
	private void doShowAbout()
	{
		getJavaModAbout().setVisible(true);
	}
	/**
	 * start playback of a audio file
	 * @since 01.07.2006
	 */
	public void doStartPlaying()
	{
		doStartPlaying(false, 0);
	}
	/**
	 * @param initialSeek
	 * @since 13.02.2012
	 */
	public void doStartPlaying(final boolean reuseMixer, final long initialSeek)
	{
		try
		{
			if (currentContainer!=null)
			{
				if (playerThread!=null && !reuseMixer)
				{
					playerThread.stopPlayback();
					playerThread = null;
					removeMixer();
				}

				if (playerThread == null)
				{
					final Mixer mixer = createNewMixer();
					if (mixer!=null)
					{
						if (initialSeek>0) mixer.setMillisecondPosition(initialSeek);
						playerThread = new PlayThread(mixer, this);
						playerThread.start();
					}
				}
				else
				{
					playerThread.getCurrentMixer().setMillisecondPosition(initialSeek);
				}
				showMessage(Helpers.EMPTY_STING);
			}
		}
		catch (final Throwable ex)
		{
			if (playerThread!=null)
			{
				playerThread.stopPlayback();
				playerThread = null;
				removeMixer();
			}
			Log.error("Starting playback did not succeed!", ex);
		}
	}
	/**
	 * stop playback of a mod
	 * @since 01.07.2006
	 */
	private void doStopPlaying()
	{
		if (playerThread!=null)
		{
			playerThread.stopPlayback();
			getSoundOutputStream().closeAllDevices();
			playerThread = null;
			removeMixer();
		}
	}
	/**
	 * pause the playing of a mod
	 * @since 01.07.2006
	 */
	private void doPausePlaying()
	{
		if (playerThread!=null)
		{
			playerThread.pausePlayback();
		}
	}
	private boolean doNextPlayListEntry()
	{
		boolean ok = false;
		while (currentPlayList!=null && currentPlayList.hasNext() && !ok)
		{
			currentPlayList.next();
			ok = loadMultimediaFile(currentPlayList.getCurrentEntry());
		}
		return ok;
	}
	private boolean doPrevPlayListEntry()
	{
		boolean ok = false;
		while (currentPlayList!=null && currentPlayList.hasPrevious() && !ok)
		{
			currentPlayList.previous();
			ok = loadMultimediaFile(currentPlayList.getCurrentEntry());
		}
		return ok;
	}
	/**
	 *
	 * @see de.quippy.javamod.main.gui.playlist.PlaylistGUIChangeListener#userSelectedPlaylistEntry()
	 * @since 13.02.2012
	 */
	@Override
	public void userSelectedPlaylistEntry()
	{
		boolean ok = false;
		while (currentPlayList!=null && !ok)
		{
			final PlayListEntry entry = currentPlayList.getCurrentEntry();
			ok = loadMultimediaFile(entry);
			if (!ok) currentPlayList.next();
			else
			if (playerThread==null) doStartPlaying(true, entry.getTimeIndex());

		}
	}
	private void doSetVolumeValue()
	{
		if (playerThread!=null)
		{
			final Mixer currentMixer = playerThread.getCurrentMixer();
			currentMixer.setVolume(currentVolume);
		}
	}
	private void doSetBalanceValue()
	{
		if (playerThread!=null)
		{
			final Mixer currentMixer = playerThread.getCurrentMixer();
			currentMixer.setBalance(currentBalance);
		}
	}
	/**
	 * @return the useGaplessAudio
	 */
	public boolean useGaplessAudio()
	{
		return useGaplessAudio;
	}
	/**
	 * @param useGaplessAudio the useGaplessAudio to set
	 */
	public void setUseGaplessAudio(final boolean useGaplessAudio)
	{
		this.useGaplessAudio = useGaplessAudio;
	}
	private SoundOutputStream getSoundOutputStream()
	{
		if (soundOutputStream==null)
		{
			if (useGaplessAudio())
				soundOutputStream = new GaplessSoundOutputStreamImpl();
			else
				soundOutputStream = new SoundOutputStreamImpl();
		}
		return soundOutputStream;
	}
	/**
	 * Creates a new Mixer for playback
	 * @since 01.07.2006
	 * @return
	 */
	private Mixer createNewMixer()
	{
		final Mixer mixer = getCurrentContainer().createNewMixer();
		if (mixer!=null)
		{
			mixer.setAudioProcessor(audioProcessor);
			mixer.setVolume(currentVolume);
			mixer.setBalance(currentBalance);
			mixer.setSoundOutputStream(getSoundOutputStream());
			getSeekBarPanel().setCurrentMixer(mixer);
		}
		return mixer;
	}
	private void removeMixer()
	{
		getSeekBarPanel().setCurrentMixer(null);
	}
	/**
	 * @since 14.09.2008
	 * @param mediaPLSFileURL
	 */
	private boolean loadMultimediaOrPlayListFile(final URL mediaPLSFileURL)
	{
		addFileToLastLoaded(mediaPLSFileURL);
		currentPlayList = null;
    	try
    	{
   			currentPlayList = PlayList.createFromFile(mediaPLSFileURL, false, false);
   			if (currentPlayList!=null)
   			{
   				getPlaylistGUI().setNewPlaylist(currentPlayList);
   				return doNextPlayListEntry();
   			}
    	}
    	catch (final Throwable ex)
    	{
			Log.error("[MainForm::loadMultimediaOrPlayListFile]", ex);
			currentPlayList = null;
    	}
    	return false;
	}
	/**
	 * load a mod file and display it
	 * @since 01.07.2006
	 * @param modFileName
	 * @return boolean if loading succeeded
	 */
	private boolean loadMultimediaFile(final PlayListEntry playListEntry)
	{
		final URL mediaFileURL = playListEntry.getFile();
		final boolean reuseMixer = (currentContainer!=null &&
									Helpers.isEqualURL(currentContainer.getFileURL(), mediaFileURL) &&
									playerThread!=null && playerThread.isRunning());
		if (!reuseMixer)
		{
	    	try
	    	{
	    		if (mediaFileURL!=null)
	    		{
	    			final MultimediaContainer newContainer = MultimediaContainerManager.getMultimediaContainer(mediaFileURL);
	    			if (newContainer!=null)
	    			{
	    				currentContainer = newContainer;
	        			getLEDScrollPanel().setScrollTextTo(currentContainer.getSongName() + Helpers.SCROLLY_BLANKS);
	        			setTrayIconToolTip(currentContainer.getSongName());
	    			}
	    		}
	    	}
	    	catch (final Throwable ex)
	    	{
				Log.error("[MainForm::loadMultimediaFile] Loading of " + mediaFileURL + " failed!", ex);
				return false;
	    	}
			changeInfoPane();
			changeConfigPane();
			changeExportMenu();
		}
		setPlayListIcons();
		// if we are currently playing, start the current piece:
		if (playerThread!=null) doStartPlaying(reuseMixer, playListEntry.getTimeIndex());
		return true;
	}
	/**
	 * @since 14.09.2008
	 * @param url
	 */
	private void addFileToLastLoaded(final URL url)
	{
		if (lastLoaded.contains(url)) lastLoaded.remove(url);
		lastLoaded.add(0, url);
		createRecentFileMenuItems();
	}
	/**
	 * @since 14.09.2008
	 * @return
	 */
	private MultimediaContainer getCurrentContainer()
	{
		if (currentContainer == null)
		{
			try
			{
				currentContainer = MultimediaContainerManager.getMultimediaContainerForType("mod");
			}
			catch (final Exception ex)
			{
				Log.error("getCurrentContainer()", ex);
			}
		}
		return currentContainer;
	}
	/**
	 * Shows the given Message
	 * @since 22.06.2006
	 * @param msg
	 */
	private synchronized void showMessage(final String msg)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				getMessages().setText(msg);
			}
		});
	}
}
