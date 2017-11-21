/**
 * Copyright (c) 1991-2018 Université catholique de Louvain
 *
 * <p>Center for Operations Research and Econometrics (CORE)
 *
 * <p>http://www.uclouvain.be
 *
 * <p>This file is part of Nodus.
 *
 * <p>Nodus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package edu.uclouvain.core.nodus;

import com.bbn.openmap.BufferedLayerMapBean;
import com.bbn.openmap.Environment;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.dataAccess.shape.ShapeConstants;
import com.bbn.openmap.event.DistanceMouseMode;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.event.NavMouseMode2;
import com.bbn.openmap.event.NodusProjMapBeanKeyListener;
import com.bbn.openmap.event.PanMouseMode;
import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.gui.MapPanel;
import com.bbn.openmap.gui.MouseModeButtonPanel;
import com.bbn.openmap.gui.NodusLayersPanel;
import com.bbn.openmap.gui.NodusOMControlPanel;
import com.bbn.openmap.gui.OpenMapFrame;
import com.bbn.openmap.gui.OverviewMapHandler;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.gui.menu.NodusSaveAsImageMenuItem;
import com.bbn.openmap.gui.menu.PNGImageFormatter;
import com.bbn.openmap.gui.menu.ProjectionMenu;
import com.bbn.openmap.image.AcmeGifFormatter;
import com.bbn.openmap.image.MapBeanPrinter;
import com.bbn.openmap.image.SunJPEGFormatter;
import com.bbn.openmap.layer.highlightedarea.HighlightedAreaLayer;
import com.bbn.openmap.layer.shape.NodusEsriLayer;
import com.bbn.openmap.layer.shape.PoliticalBoundariesLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.OMColorChooser;
import com.bbn.openmap.proj.CADRGLoader;
import com.bbn.openmap.proj.GnomonicLoader;
import com.bbn.openmap.proj.LLXYLoader;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.MercatorLoader;
import com.bbn.openmap.proj.OrthographicLoader;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.proj.ProjectionLoader;
import com.bbn.openmap.proj.ProjectionStack;
import com.bbn.openmap.tools.drawing.NodusOMDrawingTool;
import com.bbn.openmap.tools.drawing.NodusOMDrawingToolLauncher;
import com.bbn.openmap.tools.drawing.NodusOMPointLoader;
import com.bbn.openmap.tools.drawing.NodusOMPolyLoader;
import com.bbn.openmap.tools.drawing.OMDrawingToolMouseMode;
import com.bbn.openmap.util.I18n;
import com.bbn.openmap.util.PropUtils;

import edu.uclouvain.core.nodus.compute.assign.gui.AssignmentDlg;
import edu.uclouvain.core.nodus.compute.results.gui.ResultsDlg;
import edu.uclouvain.core.nodus.compute.scenario.gui.ScenariosDlg;
import edu.uclouvain.core.nodus.database.JDBCUtils;
import edu.uclouvain.core.nodus.database.gui.SQLConsole;
import edu.uclouvain.core.nodus.gui.GlobalPreferencesDlg;
import edu.uclouvain.core.nodus.gui.LanguageChooser;
import edu.uclouvain.core.nodus.gui.LookAndFeelManager;
import edu.uclouvain.core.nodus.gui.ProjectPreferencesDlg;
import edu.uclouvain.core.nodus.gui.SplashDlg;
import edu.uclouvain.core.nodus.helpbrowser.HelpBrowser;
import edu.uclouvain.core.nodus.swing.OSXAdapter;
import edu.uclouvain.core.nodus.swing.OnTopKeeper;
import edu.uclouvain.core.nodus.tools.console.NodusConsole;
import edu.uclouvain.core.nodus.tools.notepad.NodusGroovyConsole;
import edu.uclouvain.core.nodus.tools.notepad.NotePad;
import edu.uclouvain.core.nodus.utils.NodusFileFilter;
import edu.uclouvain.core.nodus.utils.PluginsLoader;
import edu.uclouvain.core.nodus.utils.SoundPlayer;

import foxtrot.Job;
import foxtrot.Worker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.uclouvain.gtm.util.gui.JResourcesMonitor;

/**
 * The NodusMapPanel class initialized the Nodus GUI and is the central place where all the menu
 * actions are intercepted. It is also the main entry to the whole Nodus API.
 *
 * @author Bart Jourquin
 */
public class NodusMapPanel extends MapPanel implements ShapeConstants {

  /** This class is used to hold an image while on the clipboard. */
  private static class ImageSelection implements Transferable {
    private Image image;

    public ImageSelection(Image image) {
      this.image = image;
    }

    // Returns image
    @Override
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
      if (!DataFlavor.imageFlavor.equals(flavor)) {
        throw new UnsupportedFlavorException(flavor);
      }
      return image;
    }

    // Returns supported flavors
    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] {DataFlavor.imageFlavor};
    }

    // Returns true if flavor is supported
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return DataFlavor.imageFlavor.equals(flavor);
    }
  }

  /** Serial version UID. */
  static final long serialVersionUID = -2848516994072912179L;

  /** Internationalization mechanism. */
  private static I18n i18n = Environment.getI18n();

  /**
   * A static method that creates a MapBean with it's projection set to the values set in the
   * Environment.
   *
   * @return BufferedLayerMapBean A BufferedLayerMapBean (a BufferedMapBean with an additional image
   *     buffer that holds Layers designated as background layers)
   */
  public static BufferedLayerMapBean createMapBean() {
    Projection proj =
        new ProjectionFactory().getDefaultProjectionFromEnvironment(Environment.getInstance());
    MapBean.suppressCopyright = true;
    BufferedLayerMapBean mapBean = new BufferedLayerMapBean();
    mapBean.setBorder(new BevelBorder(BevelBorder.LOWERED));
    mapBean.setProjection(proj);
    mapBean.setPreferredSize(new Dimension(proj.getWidth(), proj.getHeight()));
    return mapBean;
  }

  /**
   * Control variables for the progress bar, as the "setBusy" method can be called several times
   * with "true".
   */
  private int busyDepth = 0;

  /** Control variables for the progress bar. */
  private boolean canceled;

  /** OpenMap component. See OpenMap's documentation for more details. */
  private NodusOMControlPanel controlPanel = new NodusOMControlPanel(this);

  private boolean controlPressed = false;

  /** Control variables for the progress bar. */
  private int currentTask = 0;

  /** Default background color. */
  private Color defaultBackgroundColor;

  /** Control variables for the displayed cursor. */
  private Cursor defaultBeanCursor = null;

  /** Vector of global plugins. */
  private Vector<JMenuItem> globalPluginsMenuItems = new Vector<>();

  /** The browser used for the user guide. */
  HelpBrowser helpBrowser = null;

  /** The browser used for the Nodus API. */
  HelpBrowser javaDocBrowser = null;

  /** Internal layer that displays a highlighted area to which the assignment can be limited. */
  private HighlightedAreaLayer highlightedAreaLayer = null;

  /** The home directory of the Nodus application. */
  private String nodusHomeDir;

  /** OpenMap component. See OpenMap documentation for more details. */
  private InformationDelegator infoDelegator = new InformationDelegator();

  /** JPEG image formatter : See OpenMap documentation for more details. */
  private SunJPEGFormatter jpegFormatter = new SunJPEGFormatter();

  /** OpenMap component. See OpenMap documentation for more details. */
  private LayerHandler layerHandler = new LayerHandler();

  /** Mapbean to use. See OpenMap documentation for more details. */
  private BufferedLayerMapBean mapBean;

  /** MapHandler to use. See OpenMap documentation for more details. */
  private MapHandler mapHandler;

  /** "Control" menu. */
  private JMenu menuControl = new JMenu();

  /** "File" menu. */
  private JMenu menuFile = new JMenu();

  /** "Help" menu. */
  private JMenu menuHelp = new JMenu();

  /** "Control|Background color" menu item. */
  private JMenuItem menuItemControlBackground = new JMenuItem();

  /** "Control|Hide/Display Controlpanel" menu item. */
  private JMenuItem menuItemControlControlpanel = new JMenuItem();

  /** "Control|Add Openmap layers" menu item. */
  /** Is set to true if the license of the app is given for a demo. */
  private boolean demoVersion;

  private JMenuItem menuItemControlLoadLayers = new JMenuItem();

  /** "Control|Hide/Display Toolpanel" menu item. */
  private JMenuItem menuItemControlToolpanel = new JMenuItem();

  /** "File|Close" menu item. */
  private JMenuItem menuItemFileClose = new JMenuItem();

  /** "File|Exit" menu item. */
  private JMenuItem menuItemFileExit = new JMenuItem();

  /** "File|Open" menu item. */
  private JMenuItem menuItemFileOpen = new JMenuItem();

  /** "File|Print" menu item. */
  private JMenuItem menuItemFilePrint = new JMenuItem();

  /** "File|Save" menu item. */
  private JMenuItem menuItemFileSave = new JMenuItem();

  /** "File|Save as" menu item. */
  private JMenu menuItemFileSaveAs = new JMenu();

  /** Save as GIF image : See OpenMap documentation for more details. */
  private NodusSaveAsImageMenuItem menuItemFileSaveMapAsGIF;

  /** Save as JPEG image : See OpenMap documentation for more details. */
  private NodusSaveAsImageMenuItem menuItemFileSaveMapAsJPEG;

  /** Save as JPEG image : See OpenMap documentation for more details. */
  private NodusSaveAsImageMenuItem menuItemFileSaveMapAsPNG;

  /** "Help|About" menu. */
  private JMenuItem menuItemHelpAbout = new JMenuItem();

  /** "Help|API JavaDoc" menu. */
  private JMenuItem menuItemHelpApiDoc = new JMenuItem();

  /** Main help. */
  private JMenuItem menuItemHelpHelp = new JMenuItem();

  /** "Project|Assignment" menu item. */
  private JMenuItem menuItemProjectAssignment = new JMenuItem();

  /** "Project|Cost functions" menu item. */
  private JMenuItem menuItemProjectCosts = new JMenuItem();

  /** "Project|Display results" menu item. */
  private JMenuItem menuItemProjectDisplayResults = new JMenuItem();

  /** "Project|Properties" menu item. */
  private JMenuItem menuItemProjectPreferences = new JMenuItem();

  /** "Project|Scenarios" menu item. */
  private JMenuItem menuItemProjectScenarios = new JMenuItem();

  /** "Project|Services functions" menu item. */
  private JMenuItem menuItemProjectServices = new JMenuItem();

  /** "Project|SQL console" menu item. */
  private JMenuItem menuItemProjectSQLConsole = new JMenuItem();

  /** "File|Open" menu item. */
  private JMenuItem menuItemSystemProperties = new JMenuItem();

  /** "Tools|Console" menu item. */
  private JMenuItem menuItemToolConsole = new JMenuItem();

  /** "Tools|Groovy console" menu item. */
  private JMenuItem menuItemToolGroovyScripts = new JMenuItem();

  /** "Tools|Language" menu item. */
  private JMenuItem menuItemToolLanguage = new JMenuItem();

  /** "Tools|Look And feel" menu item. */
  private JMenuItem menuItemToolLookAndFeel = new JMenuItem();

  /** "Tools|Memory monitor" menu item. */
  private JMenuItem menuItemToolRessourcesMonitor = new JMenuItem();

  /** "Project" menu. */
  private JMenu menuProject = new JMenu();

  /** "Projection" menu. See OpenMap documentation for more details. */
  private ProjectionMenu menuProjection = new ProjectionMenu();

  /** "Tools" menu. */
  private JMenu menuTools = new JMenu();

  /** OpenMap component. See OpenMap documentation for more details. */
  private MouseDelegator mouseDelegator = new MouseDelegator();

  /** OpenMap component. See OpenMap documentation for more details. */
  private NavMouseMode navMouseMode1 = new NavMouseMode();

  /** OpenMap component. See OpenMap documentation for more details. */
  private NavMouseMode2 navMouseMode2 = new NavMouseMode2();

  /** Number of Nodus wide plugins. */
  private int nbNodusPlugins = 0;

  /** OpenMap component. See OpenMap documentation for more details. */
  private NodusOMDrawingTool nodusDrawingTool;

  /** OpenMap component. See OpenMap documentation for more details. */
  private NodusOMDrawingToolLauncher nodusDrawingToolLauncher;

  /** OpenMap component. See OpenMap documentation for more details. */
  private NodusLayersPanel nodusLayersPanel;

  /** Main menu bar. */
  private JMenuBar nodusMenuBar = new JMenuBar();

  /** Array of plugins. */
  private NodusPlugin[] nodusPlugins;

  /** Place holder for the Nodus project that will be opened. */
  private NodusProject nodusProject = null;

  /**
   * Nodus properties, that maintain information about the location and size of the main frame, Look
   * And Feel, ....
   */
  private Properties nodusProperties;

  /** Used to force subframes to remain on top. */
  private OnTopKeeper onTopKeeper;

  /** OpenMap component. See OpenMap documentation for more details */
  private OverviewMapHandler overviewMapHandler = null;

  /** Internal layer that displays the political boundaries. */
  private ShapeLayer politicalBoundariesLayer = null;

  /** OpenMap component. See OpenMap documentation for more details */
  private ProjectionStack projectionStack = new ProjectionStack();

  /** Vector of project specific plugins menu items. */
  private Vector<JMenuItem> projectPluginsMenuItems = new Vector<>();

  /** Combo in tool panel that makes it possible to choose the scenario. */
  private JComboBox<String> scenarioComboBox;

  /** Label associated to the scenario combo. */
  private JLabel scenarioLabel = new JLabel("", SwingConstants.RIGHT);

  /** Sound feedback. */
  private SoundPlayer soundPlayer;

  /** Control variables for the progress bar. */
  private int taskLength = 0;

  /** OpenMap component. See OpenMap documentation for more details. */
  private ToolPanel toolPanel = new ToolPanel();

  /** "User defined menus (used by plugins). */
  private Vector<JMenuItem> userDefinedMenus = new Vector<>();

  /**
   * Max scale above which the links and nodes are not anymore rendered with their specific
   * attributes.
   */
  private float renderingScaleThresold = -1;

  /**
   * Creates all the GUI components needed by Nodus on the application's panel. The application's
   * properties are also passed as a parameter in order to restore and save the application "state".
   * <br>
   * See also OpenMap documentation for more details on the behavior of the MapBeans.
   *
   * @param properties The .nodus7.properties file content
   */
  public NodusMapPanel(Properties properties) {
    MapHandler mh = getMapHandler();
    mh.add(this);
    nodusProperties = properties;

    create();
  }

  /**
   * .
   *
   * @exclude
   */
  @Override
  public void addMapComponent(Object mapComponent) {
    if (mapComponent != null) {
      getMapHandler().add(mapComponent);
    }
  }

  /**
   * Call the class for programmable Fx (F8 or F9) key.
   *
   * @param functionKey Can be "F8" or "F9"
   */
  private void callFunctionKey(String functionKey) {

    String className = null;
    if (nodusProject.isOpen()) {
      className = nodusProject.getProperty(functionKey);
    }

    if (className == null) {
      className = nodusProperties.getProperty(functionKey, null);
    }

    if (className == null) {
      return;
    }

    // Entry point for test features... callable by F8 or F9
    final NodusMapPanel _this = this;
    final String _className = className;
    Worker.post(
        new Job() {
          @Override
          public Object run() {
            if (_className != null) {
              try {
                Class<?> testClass = Class.forName(_className);
                Constructor<?> ctor = testClass.getConstructor(NodusMapPanel.class);
                ctor.newInstance(new Object[] {_this});
              } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
              } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
              } catch (SecurityException e) {
                e.printStackTrace();
                return null;
              } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
              } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
              } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
              } catch (InvocationTargetException e) {
                e.printStackTrace();
                return null;
              }
            }
            return null;
          }
        });
  }

  /**
   * Cancels a ProgressBar. See OpenMap documentation for more details on the progress bar mechanism
   * implemented on the MapBean.
   */
  public void cancelLongTask() {
    if (taskLength > 0) { // If a long task is running
      java.awt.Toolkit.getDefaultToolkit().beep();
      canceled = true;
    }
  }

  /** Closes the main frame after having save its state in the Nodus properties file. */
  public void closeAndSaveState() {

    // Close project
    if (nodusProject != null) {
      nodusProject.close();
    }

    // Save current settings
    int frameWidth = getMainFrame().getWidth();
    int frameHeight = getMainFrame().getHeight();
    Point p = getMainFrame().getLocationOnScreen();
    nodusProperties.setProperty(NodusC.PROP_FRAME_WIDTH, String.valueOf(frameWidth));
    nodusProperties.setProperty(NodusC.PROP_FRAME_HEIGTH, String.valueOf(frameHeight));
    nodusProperties.setProperty(NodusC.PROP_FRAME_X, String.valueOf(p.x));
    nodusProperties.setProperty(NodusC.PROP_FRAME_Y, String.valueOf(p.y));

    String defaultDbEngine = nodusProperties.getProperty(NodusC.PROP_EMBEDDED_DB, "h2");
    nodusProperties.setProperty(NodusC.PROP_EMBEDDED_DB, defaultDbEngine);

    try {
      String home = System.getProperty("user.home") + "/";
      nodusProperties.store(new FileOutputStream(home + ".nodus7.properties"), null);
    } catch (IOException ex) {
      System.err.println("Caught IOException saving nodus7.properties");
    }

    dispose();
    getMainFrame().dispose();
    System.gc();
  }

  /** Copy the content of the MapBean as an image in the clipboard. */
  private void copyMapToClipboard() {
    // Get image from mapBean
    int width = getMapBean().getWidth();
    int height = getMapBean().getHeight();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Graphics g = ge.createGraphics(image);
    g.setClip(0, 0, width, height);
    getMapBean().paintAll(g);
    ImageSelection imgSel = new ImageSelection(image);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
    Toolkit.getDefaultToolkit().beep();
  }

  /** Create the MapPanel and associate some keyboard shortcuts. */
  private void create() {

    setLayout(new BorderLayout());

    try {
      initialize();
    } catch (Exception e) {
      e.printStackTrace();
    }

    onTopKeeper = new OnTopKeeper(this);
    if (getAlwaysOnTop()) {
      onTopKeeper.run(isStickyDrawingTool());
    }

    BufferedLayerMapBean mb = getMapBean();
    mb.setBckgrnd(Environment.getCustomBackgroundColor());

    // Navigate with keys in the map
    NodusProjMapBeanKeyListener kl = new NodusProjMapBeanKeyListener();
    kl.findAndInit(projectionStack);
    kl.setMapBean(mapBean);
    mapBean.addKeyListener(kl);

    KeyAdapter ka =
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent evt) {
            // Escape key to interrupt long tasks
            if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
              cancelLongTask();
            }

            // F8 key for test functions (not documented)
            if (evt.getKeyCode() == KeyEvent.VK_F8) {
              callF8();
            }

            // F9 key to test functions (not documented)
            if (evt.getKeyCode() == KeyEvent.VK_F9) {
              callF9();
            }

            // Ctrl key for drawing tool
            if (evt.getKeyCode() == KeyEvent.VK_CONTROL || evt.getKeyCode() == KeyEvent.VK_META) {
              // nodusDrawingTool.setControlPressed(true);
              controlPressed = true;
            }

            // Ctrl key + C
            if ((evt.getKeyCode() == 67 && evt.isControlDown())
                || (evt.getKeyCode() == 67 && evt.isMetaDown())) {
              copyMapToClipboard();
            }
          }

          @Override
          public void keyReleased(KeyEvent evt) {
            // Ctrl key for drawing tool
            if (evt.getKeyCode() == KeyEvent.VK_CONTROL || evt.getKeyCode() == KeyEvent.VK_META) {
              // nodusDrawingTool.setControlPressed(false);
              controlPressed = false;
            }
          }
        };

    // Intercept a few command keys...
    addKeyListener(ka);
    getMapBean().addKeyListener(ka);

    infoDelegator.setShowWaitCursor(true);

    initializeHelp();
  }

  /** Creates the action listeners to add to the menu items. */
  private void createMenuActionListeners() {
    menuItemFileOpen.setText(i18n.get(NodusMapPanel.class, "Open_project", "Open project"));
    menuItemFileOpen.setAccelerator(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    menuItemFileOpen.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemFileOpenActionPerformed(e);
          }
        });

    menuItemSystemProperties.setText(
        i18n.get(NodusMapPanel.class, "Open_preferences", "Global preferences"));
    menuItemSystemProperties.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemFileGlobalPreferencesActionPerformed();
          }
        });

    menuItemFileSave.setEnabled(false);
    menuItemFileSave.setText(i18n.get(NodusMapPanel.class, "Save_project", "Save project"));
    menuItemFileSave.setAccelerator(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    menuItemFileSave.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemFileSaveActionPerformed(e);
          }
        });

    menuItemFileClose.setEnabled(false);
    menuItemFileClose.setText(i18n.get(NodusMapPanel.class, "Close_project", "Close project"));
    menuItemFileClose.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemFileCloseActionPerformed(e);
          }
        });

    menuItemFilePrint.setText(i18n.get(NodusMapPanel.class, "Print", "Print"));
    menuItemFilePrint.setAccelerator(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    menuItemFilePrint.setEnabled(false);
    menuItemFilePrint.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemFilePrintActionPerformed(e);
          }
        });

    menuItemFileExit.setText(i18n.get(NodusMapPanel.class, "Exit", "Exit"));
    menuItemFileExit.setAccelerator(
        KeyStroke.getKeyStroke(
            KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    menuItemFileExit.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemFileExitActionPerformed();
          }
        });

    menuItemProjectAssignment.setText(i18n.get(NodusMapPanel.class, "Assignment", "Assignment"));
    menuItemProjectAssignment.setAccelerator(
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
    menuItemProjectAssignment.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemProjectAssignmentActionPerformed(e);
          }
        });

    menuItemProjectSQLConsole.setText(i18n.get(NodusMapPanel.class, "SQL_Console", "SQL Console"));
    menuItemProjectSQLConsole.setAccelerator(
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F7, 0));
    menuItemProjectSQLConsole.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemProjectSQLConsoleActionPerformed(e);
          }
        });

    menuItemProjectDisplayResults.setText(
        i18n.get(NodusMapPanel.class, "Display_results", "Display results"));
    menuItemProjectDisplayResults.setAccelerator(
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
    menuItemProjectDisplayResults.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemProjectShowResultsActionPerformed(e);
          }
        });

    menuItemProjectScenarios.setText(i18n.get(NodusMapPanel.class, "Scenarios", "Scenarios"));
    menuItemProjectScenarios.setAccelerator(
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
    menuItemProjectScenarios.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemProjectScenariosActionPerformed(e);
          }
        });

    menuItemProjectCosts.setText(
        i18n.get(NodusMapPanel.class, "Edit_cost_functions", "Edit cost functions"));
    menuItemProjectCosts.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
    menuItemProjectCosts.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemProjectCostsActionPerformed(e);
          }
        });

    menuItemProjectServices.setText(
        i18n.get(NodusMapPanel.class, "Edit_services", "Edit services"));
    menuItemProjectServices.setAccelerator(
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));

    menuItemProjectServices.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemProjectServicesActionPerformed(e);
          }
        });

    menuItemProjectPreferences.setText(
        i18n.get(NodusMapPanel.class, "Project_preferences", "Project preferences"));

    menuItemProjectPreferences.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemProjectPreferencesActionPerformed(e);
          }
        });

    menuItemToolLookAndFeel.setText(i18n.get(NodusMapPanel.class, "Look_&_Feel", "Look & Feel"));

    menuItemToolLookAndFeel.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemToolLookAndFeelActionPerformed(e);
          }
        });

    menuItemToolLanguage.setText(i18n.get(NodusMapPanel.class, "Language", "Language"));

    menuItemToolLanguage.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemToolLanguageActionPerformed(e);
          }
        });

    menuItemToolConsole.setText(i18n.get(NodusMapPanel.class, "Console", "Console"));
    menuItemToolConsole.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String defaultDir = null;
            if (getNodusProject().isOpen()) {
              defaultDir = getNodusProject().getLocalProperty(NodusC.PROP_PROJECT_DOTPATH);
            }

            new NodusConsole(defaultDir);
          }
        });

    menuItemToolGroovyScripts.setText(
        i18n.get(NodusMapPanel.class, "Groovy_scripts", "Groovy scripts"));

    menuItemToolGroovyScripts.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemGroovyConsoleActionPerformed(e);
          }
        });

    menuItemToolRessourcesMonitor.setText(
        i18n.get(NodusMapPanel.class, "Resources_monitor", "Resources monitor"));

    menuItemToolRessourcesMonitor.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {

            // Only create a instance if none exists
            Frame[] frames = Frame.getFrames();

            for (Frame element : frames) {
              if (element instanceof JFrame) {
                JFrame f = (JFrame) element;
                if (f.getClass().toString().endsWith("ResourcesMonitor") && f.isVisible()) {
                  return;
                }
              }
            }

            JResourcesMonitor mm = new JResourcesMonitor();
            // ContextualHelp.setHelp(mm, "org.apache.batik.util.gui.MemoryMonitor");
            mm.setVisible(true);
          }
        });

    menuItemHelpAbout.setText(i18n.get(NodusMapPanel.class, "About", "About"));
    menuItemHelpAbout.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemAboutActionPerformed();
          }
        });

    menuItemControlBackground.setText(
        i18n.get(NodusMapPanel.class, "Set_Background_color", "Set Background color"));

    menuItemControlBackground.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemControlBackgroundActionPerformed(e);
          }
        });

    menuItemControlToolpanel.setText(
        i18n.get(NodusMapPanel.class, "Hide_Tool_Panel", "Hide Tool Panel"));

    menuItemControlToolpanel.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemControlToolpanelActionPerformed(e);
          }
        });

    menuItemControlControlpanel.setText(
        i18n.get(NodusMapPanel.class, "Hide Control Panel", "Hide Control Panel"));

    menuItemControlControlpanel.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemControlControlpanelActionPerformed(e);
          }
        });

    menuItemControlLoadLayers.setText(
        i18n.get(NodusMapPanel.class, "Add_OpenMap_layers", "Add OpenMap layers"));

    menuItemControlLoadLayers.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuItemControlLoadLayersActionPerformed(e);
          }
        });
  }

  /** Setup the overview map using the embedded dcwpo-browse map data. */
  private void createOverviewMap() {
    overviewMapHandler = controlPanel.getOverviewMapHandler();

    // Create the overview layer, with defaults values (dcwpo-browse)
    Properties p = new Properties();
    try {
      InputStream in = this.getClass().getResource("overviewMap.properties").openStream();
      p.load(in);
    } catch (IOException ioe) { // Should never happen
      ioe.printStackTrace();
    }

    // Get the filenames of the dcwpo-browser map data
    String shpFileName = this.getClass().getResource("dcwpo-browse.shp").getFile();

    // Run from within Eclipse or standalone JAR ?
    if (shpFileName.contains("jar!")) {
      shpFileName = "jar:" + shpFileName;
    }

    p.setProperty("overviewLayer.shapeFile", shpFileName);
    p.setProperty(
        "overviewLayer.prettyName", i18n.get(NodusMapPanel.class, "Overview", "Overview"));

    overviewMapHandler.setProperties("overviewMapHandler", p);
    overviewMapHandler.activateMouseMode();

    getMapHandler().add(overviewMapHandler);
  }

  /**
   * Creates a new menu item at the right place for the plugin.
   *
   * @param plugin The plugin for which a menu item must be created
   * @param commandId The command ID to associate to this plugin
   * @param menu The menu to which the item must be added to
   * @param pluginProp The properties associated to the plugin
   * @return The created menu item
   */
  private JMenuItem createPluginMenuItem(
      NodusPlugin plugin, int commandId, JMenu menu, Properties pluginProp) {
    JMenuItem menuItem = null;
    String text;
    text = pluginProp.getProperty(NodusPlugin.MENU_ITEM__TEXT);

    if (text != null) {
      menuItem = new JMenuItem(text);

      // Enable the menu?
      boolean enable = true;
      text = pluginProp.getProperty(NodusPlugin.IS_ENABLED);

      if (text != null) {
        if (text.equalsIgnoreCase(NodusPlugin.FALSE)) {
          enable = false;
        }
      }

      menuItem.setEnabled(enable);

      menuItem.setActionCommand(Integer.toString(commandId));
      menuItem.addActionListener(
          new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              pluginMenuActionPerformed(e);
            }
          });
    } else {
      System.err.println(
          "Plugin " + plugin.getClass().toString() + " doesn't define a MenuItemText");
    }

    // Now add the menu item at the right place
    if (menu != null && menuItem != null) {
      text = pluginProp.getProperty(NodusPlugin.MENU_ITEM_ID);

      int n = -1;

      if (text != null) {
        n = Integer.parseInt(text);
      }

      if (n < -1) {
        System.err.println(
            "Plugin " + plugin.getClass().toString() + " returns invalid menuItemID");
      } else {
        if (n == -1) {
          // Add item just before last separator, if any
          int lastSeparator = -1;

          for (int j = 0; j < menu.getItemCount(); j++) {
            Component c = menu.getMenuComponent(j);

            if (c instanceof JSeparator) {
              lastSeparator = j;
            }
          }

          if (lastSeparator == -1) {
            // Just append to menu
            menu.add(menuItem);
          } else {
            // Insert before separator
            menu.insert(menuItem, lastSeparator);
          }
        } else {
          // Insert at the given place
          menu.insert(menuItem, n);
        }
      }
    }

    return menuItem;
  }

  /**
   * Displays the highlighted area, used to limit the assignment to a given rectangular area.
   *
   * @param added If true, add the layer, else remove it.
   * @param visible If true, display the layer, else hide it.
   */
  public void displayHighlightedAreaLayer(boolean added, boolean visible) {

    // Remove existing if exists
    if (highlightedAreaLayer != null) {
      highlightedAreaLayer.setVisible(false);
      layerHandler.removeLayer(highlightedAreaLayer);
      highlightedAreaLayer = null;
    }

    if (added) {
      highlightedAreaLayer = new HighlightedAreaLayer(nodusProject);
      layerHandler.addLayer(highlightedAreaLayer, 0);
      highlightedAreaLayer.setVisible(visible);
    }

    layerHandler.setLayers();
    nodusLayersPanel.enableButtons(true);
  }

  /**
   * Displays the "build-in" political boundaries.
   *
   * @param added If true, add the layer, else remove it.
   * @param visible If true, display the layer, else hide it.
   */
  public void displayPoliticalBoundaries(boolean added, boolean visible) {

    // Remove existing if exists
    if (politicalBoundariesLayer != null) {
      politicalBoundariesLayer.setVisible(false);
      layerHandler.removeLayer(politicalBoundariesLayer);
      politicalBoundariesLayer = null;
    }

    if (added) {
      politicalBoundariesLayer = PoliticalBoundariesLayer.getLayer(mapBean);
      try {
        layerHandler.addLayer(politicalBoundariesLayer);
        politicalBoundariesLayer.setVisible(visible);
      } catch (Exception e) {
        System.err.println(
            "displayPoliticalBoundaries throws an exception. This should not happen...");
      }
    }

    layerHandler.setLayers();
    nodusLayersPanel.enableButtons(true);
  }

  /**
   * Displays the "scenario" combo box above the MapBean.
   *
   * @param visible If true, display it, else hide it.
   */
  private void displayScenarioCombo(boolean visible) {
    scenarioLabel.setVisible(visible);
    scenarioComboBox.setVisible(visible);
  }

  /** Sets the MapBean variable to null and removes all children. */
  @Override
  public void dispose() {
    setMapBean(null);
    setLayout(null);
    removeAll();
  }

  /**
   * Enable/Disable the menu items that are accessible only when a project is loaded.
   *
   * @param state boolean
   */
  public void enableMenus(boolean state) {
    // Enable some menu items
    menuItemFileSave.setEnabled(state);
    menuItemFileClose.setEnabled(state);
    menuItemFileSaveAs.setEnabled(state);
    menuItemFilePrint.setEnabled(state);
    menuProject.setEnabled(state);
    menuControl.setEnabled(state);
    menuProjection.setEnabled(state);

    // Enable/disable use defined menus (plugins)
    Iterator<JMenuItem> it = userDefinedMenus.iterator();

    while (it.hasNext()) {
      JMenu m = (JMenu) it.next();
      m.setEnabled(state);
    }

    it = globalPluginsMenuItems.iterator();

    while (it.hasNext()) {
      JMenuItem m = it.next();
      m.setEnabled(true);
    }

    it = projectPluginsMenuItems.iterator();

    while (it.hasNext()) {
      JMenuItem m = it.next();
      m.setEnabled(state);
    }
  }

  /** Undocumented property: class to call when F8 is pressed. */
  private void callF8() {
    callFunctionKey("F8");
  }

  /** Undocumented property: class to call when F9 is pressed. */
  private void callF9() {
    callFunctionKey("F9");
  }

  /**
   * Returns the active mouse mode ID (See OpenMap API for more information).
   *
   * @return The mouse mode ID.
   */
  public String getActiveMouseMode() {
    return mouseDelegator.getActiveMouseModeID();
  }

  /**
   * Returns true if the subframes must always remain on top.
   *
   * @return True if the subframes must remain on top.
   */
  public boolean getAlwaysOnTop() {
    String str = nodusProperties.getProperty(NodusC.PROP_SUBFRAMES_ALWAYS_ON_TOP, "true");
    return Boolean.parseBoolean(str);
  }

  /**
   * Returns the "Assignment" menu item.
   *
   * @return The "Assignment" menu item.
   */
  public JMenuItem getAssignmentMenuItem() {
    return menuItemProjectAssignment;
  }

  /**
   * Returns true if the control key is pressed.
   *
   * @return True if pressed.
   */
  public boolean isControlPressed() {
    return controlPressed;
  }

  /**
   * Returns the default background color used by OpenMap.
   *
   * @return Color
   */
  private Color getDefaultBackgroundColor() {
    return defaultBackgroundColor;
  }

  /**
   * Returns true if the full path of the project must be displayed in the title bar.
   *
   * @return True if the full path of the project must be displayed in the title bar.
   */
  public boolean getDisplayFullPath() {
    String str = nodusProperties.getProperty(NodusC.PROP_DISPLAY_FULL_PATH, "false");
    return Boolean.parseBoolean(str);
  }

  /**
   * Returns the interval used for garbage collection during assignments.
   *
   * @return The GC interval expressed in seconds.
   */
  public int getGarbageCollectorInterval() {
    int interval;
    String str = nodusProperties.getProperty(NodusC.PROP_GC_INTERVAL, "0");
    try {
      interval = Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return 0;
    }
    return interval;
  }

  /**
   * Accessor for the highlighted area.
   *
   * @return The highlighted area.
   */
  public HighlightedAreaLayer getHighlightedAreaLayer() {
    return highlightedAreaLayer;
  }

  /**
   * Accessor for the LayerHandler.
   *
   * @return LayerHandler
   */
  public LayerHandler getLayerHandler() {
    return layerHandler;
  }

  /**
   * Returns the main frame of the application.
   *
   * @return The main frame of the application.
   */
  public synchronized Frame getMainFrame() {
    OpenMapFrame omf = getMapHandler().get(com.bbn.openmap.gui.OpenMapFrame.class);
    return omf;
  }

  /**
   * Accessor for the MapBean.
   *
   * @return MapBean
   */
  @Override
  public BufferedLayerMapBean getMapBean() {
    if (mapBean == null) {
      setMapBean(createMapBean());
    }

    return mapBean;
  }

  /**
   * Accessor for the MapHandler.
   *
   * @return MapHandler
   */
  @Override
  public MapHandler getMapHandler() {
    if (mapHandler == null) {
      mapHandler = new MapHandler();
    }
    return mapHandler;
  }

  /**
   * MapPanel method. Get a JMenu containing sub-menus created from properties.
   *
   * @exclude
   */
  @Override
  public JMenu getMapMenu() {
    return null;
  }

  /**
   * MapPanel method. Get a JMenuBar containing menus created from properties.
   *
   * @exclude
   */
  @Override
  public JMenuBar getMapMenuBar() {
    return null;
  }

  /**
   * Accessor for the "File" menu.
   *
   * @return The "File" menu.
   */
  public JMenu getMenuFile() {
    return menuFile;
  }

  /**
   * Accessor for the drawing tool used to edit nodes and links.
   *
   * @return The drawing tool.
   */
  public NodusOMDrawingTool getNodusDrawingTool() {
    return nodusDrawingTool;
  }

  /**
   * Accessor for the drawing tool launcher.
   *
   * @return NodusOMDrawingToolLauncher
   */
  public NodusOMDrawingToolLauncher getNodusDrawingToolLauncher() {
    return nodusDrawingToolLauncher;
  }

  /**
   * Accessor for the layers panel.
   *
   * @return The layers panel.
   */
  public NodusLayersPanel getNodusLayersPanel() {
    return nodusLayersPanel;
  }

  /**
   * Accessor to get the Nodus project.
   *
   * @return The Nodus project.
   */
  public NodusProject getNodusProject() {
    return nodusProject;
  }

  /**
   * Returns the properties used by the application.
   *
   * @return The Nodus global properties.
   */
  public Properties getNodusProperties() {
    return nodusProperties;
  }

  /**
   * Returns the standard menu to which the plugin must be added to..
   *
   * @param plugin The plugin to add.
   * @param menuId The menu number.
   * @return The JMenu that gies access to the plugins.
   */
  private JMenu getPluginMenu(NodusPlugin plugin, int menuId) {
    JMenu menu = null;

    switch (menuId) {
      case NodusPlugin.MENU_FILE:
        menu = menuFile;

        break;

      case NodusPlugin.MENU_PROJECT:
        menu = menuProject;

        break;

      case NodusPlugin.MENU_CONTROL:
        menu = menuControl;

        break;

      case NodusPlugin.MENU_TOOLS:
        menu = menuTools;

        break;

      case NodusPlugin.MENU_HELP:
        menu = menuHelp;

        break;

      default:
        System.err.println(
            "Plugin " + plugin.getClass().toString() + " returns undefined MenuBarID");
    }

    return menu;
  }

  /**
   * Accessor for the projection stack.
   *
   * @return ProjectionStack
   */
  public ProjectionStack getProjectionStack() {
    return projectionStack;
  }

  /**
   * Accessor for the sound player
   *
   * @return The sound player.
   */
  public SoundPlayer getSoundPlayer() {
    return soundPlayer;
  }

  /**
   * Returns true if the drawing tool must stay on the left bottom corner of the main window.
   *
   * @return True if the drawing tool must remain in the corner.
   */
  private boolean isStickyDrawingTool() {
    String str = nodusProperties.getProperty(NodusC.PROP_STICKY_DRAWING_TOOL, "false");
    return Boolean.parseBoolean(str);
  }

  /**
   * Accessor for the tool panel.
   *
   * @return The tool panel
   */
  public ToolPanel getToolPanel() {
    return toolPanel;
  }

  /** Initializes the default XY projection. */
  private void initDefaultProjection() {
    /*
     * Add a set of possible projections. <br>
     * See OpenMap documentation for more details on the ProjectionLoader mechanism.
     */

    LLXYLoader llxyl = new LLXYLoader();
    MercatorLoader mercatorl = new MercatorLoader();
    CADRGLoader cadrgl = new CADRGLoader();
    OrthographicLoader orthol = new OrthographicLoader();
    GnomonicLoader gnomonicl = new GnomonicLoader();

    getMapHandler().add(llxyl);
    getMapHandler().add(mercatorl);
    getMapHandler().add(cadrgl);
    getMapHandler().add(orthol);
    getMapHandler().add(gnomonicl);

    Vector<ProjectionLoader> loaders = new Vector<>();
    loaders.add(llxyl);
    loaders.add(mercatorl);
    loaders.add(cadrgl);
    loaders.add(orthol);
    loaders.add(gnomonicl);

    menuProjection.configure(loaders);

    menuProjection.findAndInit(mapBean);

    // Set default projection
    Projection projection = mapBean.getProjection();
    Point2D ctr = projection.getCenter();
    Projection newProj =
        getMapBean()
            .getProjectionFactory()
            .makeProjection(
                Mercator.class.getName(),
                ctr,
                projection.getScale(),
                projection.getWidth(),
                projection.getHeight());
    mapBean.setProjection(newProj);
  }

  /**
   * Real initialization of the GUI components. See also OpenMap documentation for more details on
   * the OpenMap specific components.
   *
   * @throws Exception on error during GUI initialization
   */
  private void initialize() throws Exception {

    // Initialize sound system
    boolean sound = Boolean.parseBoolean(nodusProperties.getProperty(NodusC.PROP_SOUND, "true"));
    soundPlayer = new SoundPlayer(sound);

    // Create the openMap components
    initOpenMapComponents();

    // Initialize the defaut XY projection
    initDefaultProjection();

    // Create all the menus
    initMenus();

    // Add the plugins, if any
    nodusHomeDir = System.getProperty("NODUS_HOME", ".");
    loadPlugins(nodusHomeDir + "/plugins", false);

    getMapHandler().add(nodusMenuBar);

    // Prepare project
    enableMenus(false);

    nodusProject = new NodusProject(this);

    resetMap();
    displayPoliticalBoundaries(true, true);

    // Add a combo to the tool panel that allows to choose a scenario
    scenarioLabel = new JLabel(i18n.get(NodusMapPanel.class, "Scenario", "Scenario"));
    toolPanel.add(
        scenarioLabel,
        new GridBagConstraints(
            9, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, 0, new Insets(0, 200, 0, 0), 0, 0));
    scenarioComboBox = new JComboBox<>();
    toolPanel.add(
        scenarioComboBox,
        new GridBagConstraints(
            10,
            0,
            1,
            1,
            100,
            100,
            GridBagConstraints.CENTER,
            GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 0),
            0,
            0));

    displayScenarioCombo(false);

    scenarioComboBox.addActionListener(
        new ActionListener() {
          // Set the current scenario
          @Override
          public void actionPerformed(ActionEvent e) {
            // Get the scenario number and update
            String item = (String) scenarioComboBox.getSelectedItem();
            int n = Integer.parseInt(item.substring(0, item.indexOf("-")).trim());
            nodusProject.setLocalProperty(NodusC.PROP_SCENARIO, n);
            updateScenarioComboBox();

            // Update cost function name
            String fileName = nodusProject.getLocalProperty(NodusC.PROP_COST_FUNCTIONS + n, null);
            if (fileName != null) {
              nodusProject.setLocalProperty(NodusC.PROP_COST_FUNCTIONS, fileName);
            }
          }
        });
  }

  /** Initializes the help system. */
  private void initializeHelp() {

    // Nodus help
    menuItemHelpHelp.setText(i18n.get(NodusMapPanel.class, "Help", "Help"));
    menuItemHelpHelp.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
    menuItemHelpHelp.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            if (helpBrowser == null) {
              helpBrowser = new HelpBrowser();
            }
            helpBrowser.launchBrowser(true);
          }
        });
    menuHelp.add(menuItemHelpHelp);

    // API JavaDoc help
    menuItemHelpApiDoc.setText(i18n.get(NodusMapPanel.class, "API_Doc", "API Javadoc"));
    menuItemHelpApiDoc.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (javaDocBrowser == null) {
              javaDocBrowser = new HelpBrowser();
            }
            javaDocBrowser.launchBrowser(false);
          }
        });
    menuHelp.add(menuItemHelpApiDoc);
  }

  /** Create the menus. */
  private void initMenus() {
    // Initialize the action listeners
    createMenuActionListeners();

    menuFile.setText(i18n.get(NodusMapPanel.class, "File", "File"));

    menuItemFileSaveMapAsGIF = new NodusSaveAsImageMenuItem("GIF", new AcmeGifFormatter());

    menuItemFileSaveMapAsPNG = new NodusSaveAsImageMenuItem("PNG", new PNGImageFormatter());

    jpegFormatter.setImageQuality(NodusC.JPEG_QUALITY);
    menuItemFileSaveMapAsJPEG = new NodusSaveAsImageMenuItem("JPEG", jpegFormatter);

    menuProject.setText(i18n.get(NodusMapPanel.class, "Project", "Project"));

    menuProject.setEnabled(false);

    menuTools.setText(i18n.get(NodusMapPanel.class, "Tools", "Tools"));

    menuHelp.setText(i18n.get(NodusMapPanel.class, "Help", "Help"));

    menuItemFileSaveAs.setEnabled(false);
    menuItemFileSaveAs.setText(i18n.get(NodusMapPanel.class, "Save_as_", "Save as..."));

    menuItemFileSaveMapAsGIF.setText("GIF");
    menuItemFileSaveMapAsGIF.setMapHandler(getMapHandler());
    menuItemFileSaveAs.add(menuItemFileSaveMapAsGIF);

    menuItemFileSaveMapAsJPEG.setText("JPEG");
    menuItemFileSaveMapAsJPEG.setMapHandler(getMapHandler());
    menuItemFileSaveAs.add(menuItemFileSaveMapAsJPEG);

    menuItemFileSaveMapAsPNG.setText("PNG");
    menuItemFileSaveMapAsPNG.setMapHandler(getMapHandler());
    menuItemFileSaveAs.add(menuItemFileSaveMapAsPNG);

    menuControl.setText(i18n.get(NodusMapPanel.class, "Control", "Control"));

    menuControl.setEnabled(false);

    menuProjection.setEnabled(false);

    int n = menuProjection.getMenuComponentCount();

    // Translate the projection names
    for (int i = 0; i < n; i++) {

      Component c = menuProjection.getMenuComponent(i);
      if (c instanceof JMenuItem) {
        JMenuItem mi = (JMenuItem) c;
        mi.setText(i18n.get(NodusMapPanel.class, mi.getText(), mi.getText()));
      }
    }

    nodusMenuBar.add(menuFile);
    nodusMenuBar.add(menuProject);
    nodusMenuBar.add(menuControl);
    nodusMenuBar.add(menuProjection);

    nodusMenuBar.add(menuTools);
    nodusMenuBar.add(menuHelp);

    menuFile.add(menuItemFileOpen);

    if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
      // Use the standard Mac preferences
      try {
        OSXAdapter.setPreferencesHandler(
            this,
            getClass()
                .getDeclaredMethod("menuItemFileGlobalPreferencesActionPerformed", (Class[]) null));
      } catch (NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
      }

    } else {
      menuFile.add(menuItemSystemProperties);
    }
    menuFile.add(menuItemFileSave);
    menuFile.add(menuItemFileClose);
    menuFile.add(menuItemFileSaveAs);
    menuFile.add(menuItemFilePrint);

    if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
      // Use the standard Mac exit
      try {
        OSXAdapter.setQuitHandler(
            this, getClass().getDeclaredMethod("menuItemFileExitActionPerformed", (Class[]) null));
      } catch (NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
      }
    } else {
      menuFile.addSeparator();
      menuFile.add(menuItemFileExit);
    }

    menuProject.add(menuItemProjectPreferences);
    menuProject.add(menuItemProjectCosts);
    menuProject.add(menuItemProjectServices);
    menuProject.add(menuItemProjectAssignment);
    menuProject.add(menuItemProjectDisplayResults);
    menuProject.add(menuItemProjectScenarios);
    menuProject.add(menuItemProjectSQLConsole);

    menuControl.add(menuItemControlBackground);
    menuControl.add(menuItemControlToolpanel);
    menuControl.add(menuItemControlControlpanel);
    menuControl.add(menuItemControlLoadLayers);

    menuTools.add(menuItemToolLookAndFeel);
    menuTools.add(menuItemToolLanguage);
    menuTools.add(menuItemToolConsole);
    menuTools.add(menuItemToolGroovyScripts);
    menuTools.add(menuItemToolRessourcesMonitor);

    if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
      // Use the standard Mac About
      try {
        OSXAdapter.setAboutHandler(
            this, getClass().getDeclaredMethod("menuItemAboutActionPerformed", (Class[]) null));
      } catch (NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
      }
    } else {
      menuHelp.add(menuItemHelpAbout);
    }
  }

  /** Initialize the openMap components used in Nodus. */
  private void initOpenMapComponents() {

    // Initialize map handler
    getMapHandler().add(layerHandler);
    getMapHandler().add(projectionStack);

    getMapHandler().add(mouseDelegator);

    // Add tool panel
    getMapHandler().add(toolPanel);
    add(toolPanel, BorderLayout.NORTH);

    // Add mouse modes
    getMapHandler().add(new MouseModeButtonPanel());
    SelectMouseMode selectMouseMode = new SelectMouseMode();
    getMapHandler().add(selectMouseMode);
    PanMouseMode panMouseMode = new PanMouseMode();
    getMapHandler().add(panMouseMode);
    DistanceMouseMode distanceMouseMode = new DistanceMouseMode();
    distanceMouseMode.setUnit(Length.KM);
    getMapHandler().add(distanceMouseMode);
    addNavMouseMode();

    // Add info delegator
    getMapHandler().add(infoDelegator);
    add(infoDelegator, BorderLayout.SOUTH);

    // Add control panel with specific NodusLayersPanel
    nodusLayersPanel = new NodusLayersPanel(this);
    controlPanel.setLayersPanel(nodusLayersPanel);

    getMapHandler().add(controlPanel);

    // Create overview map
    createOverviewMap();

    add(controlPanel, BorderLayout.WEST);

    // Add drawing tool
    nodusDrawingToolLauncher = new NodusOMDrawingToolLauncher();
    nodusDrawingTool = new NodusOMDrawingTool(this, nodusDrawingToolLauncher);
    nodusDrawingTool.addLoader(new NodusOMPointLoader());
    nodusDrawingTool.addLoader(new NodusOMPolyLoader());
    getMapHandler().add(nodusDrawingTool);
    getMapHandler().add(nodusDrawingToolLauncher);

    // Add and hide a drawing tool mouse mode (the drawing layer is also invisible)
    OMDrawingToolMouseMode dtmm = new OMDrawingToolMouseMode();
    dtmm.setVisible(false);
    getMapHandler().add(dtmm);

    // Background color
    defaultBackgroundColor = getMapBean().getBackground();
  }

  /**
   * Returns True if the license is for a demo version.
   *
   * @return True if the license is for a demo version
   */
  public boolean isDemoVersion() {
    return demoVersion;
  }

  /**
   * Returns true if the highlighted area layer is added to the list of layers.
   *
   * @return True if the layer is present.
   */
  public boolean isHighlightedAreaLayerAdded() {
    if (highlightedAreaLayer == null) {
      return false;
    }
    return true;
  }

  /**
   * Returns true if the highlighted area layer is visible.
   *
   * @return True if the layer is visible.
   */
  public boolean isHighlightedAreaLayerVisible() {
    if (highlightedAreaLayer == null) {
      return false;
    } else {
      return highlightedAreaLayer.isVisible();
    }
  }

  /**
   * Returns true if the embedded political boundaries layer is added to the list of layers.
   *
   * @return True if the layer is present.
   */
  public boolean isPoliticalBoundariesAdded() {
    if (politicalBoundariesLayer == null) {
      return false;
    }
    return true;
  }

  /**
   * Returns true if the embedded political boundaries layer is visible.
   *
   * @return True if the layer is visible.
   */
  public boolean isPoliticalBoundariesVisible() {
    if (politicalBoundariesLayer == null) {
      return false;
    } else {
      return politicalBoundariesLayer.isVisible();
    }
  }

  /**
   * Loads all the plugins which jar is stored in the given directory and creates the relevant
   * "plugins" menu items. The boolean parameter is used to qualify the nature of the plugin: when
   * set to true, the plugins are considered to be relevant only for the loaded project, and the
   * related menu items will be removed when the project will be closed.
   *
   * @param dir Place where the plugin is located
   * @param projectPlugin True if plugin a project specific. False for global plugins.
   * @throws NodusPluginNotCreatedException If the plugin was not successfully initialized.
   */
  public void loadPlugins(String dir, boolean projectPlugin) {

    // Load all the plugins
    PluginsLoader nodusPluginLoader = new PluginsLoader(dir);
    LinkedList<Class<NodusPlugin>> availableClasses = nodusPluginLoader.getAvailablePlugins();

    // Global plugins must not be removed when a project is closed
    if (!projectPlugin) {
      nbNodusPlugins = availableClasses.size();
    }

    if (availableClasses.size() > 0) {
      NodusPlugin[] plugins;
      plugins = new NodusPlugin[availableClasses.size()];

      int index = 0;
      Iterator<Class<NodusPlugin>> classIterator = availableClasses.iterator();
      while (classIterator.hasNext()) {
        Class<NodusPlugin> loadableClass = classIterator.next();
        try {

          plugins[index] = loadableClass.getConstructor().newInstance();
          plugins[index].setNodusMapPanel(this);
        } catch (Exception e) {
          e.printStackTrace();
        }

        // Where must the plugin be inserted?
        JMenu menu = null;
        JMenuItem menuItem = null;
        Properties pluginProp = plugins[index].getProperties();

        // Is it a new menu in the menubar?
        String text = pluginProp.getProperty(NodusPlugin.USER_DEFINED_MENUBAR_TEXT);

        if (text != null) {
          // Does this user-defined menu already exists?
          boolean found = false;
          Iterator<JMenuItem> it = userDefinedMenus.iterator();

          while (it.hasNext()) {
            JMenu m = (JMenu) it.next();

            if (m.getText().equals(text)) {
              found = true;
              menu = m;

              break;
            }
          }

          // No. Add it, just before the "help" menu!
          if (!found) {
            menu = new JMenu(text);
            userDefinedMenus.add(menu);
            nodusMenuBar.add(menu, nodusMenuBar.getMenuCount() - 1);
            menu.setEnabled(false);
          }
        } else {
          // Menu item must be added to a Nodus menu
          text = pluginProp.getProperty(NodusPlugin.MENUBAR_ID);

          if (text != null) {
            int n = Integer.parseInt(text);
            menu = getPluginMenu(plugins[index], n);
          } else {
            System.err.println(
                "Plugin "
                    + plugins[index].getClass().toString()
                    + " doesn't define a MenuBarID "
                    + "or a UserDefinedMenubarText");
          }
        }

        // Create the relevant menu item and associate a actionCommand to it
        int commandId = index;

        if (projectPlugin) {
          commandId = nbNodusPlugins + index;
        }

        menuItem = createPluginMenuItem(plugins[index], commandId, menu, pluginProp);

        // Store the menu in a vector in order to enable/disable/remove
        // them easily
        if (!projectPlugin) {
          globalPluginsMenuItems.add(menuItem);
        } else {
          projectPluginsMenuItems.add(menuItem);
        }

        index++;
      }

      /** Add project plugins to global plugins */
      if (!projectPlugin || nodusPlugins == null) {
        nodusPlugins = plugins;
      } else {
        NodusPlugin[] tmp = nodusPlugins.clone();
        int newSize = nodusPlugins.length + plugins.length;
        nodusPlugins = new NodusPlugin[newSize];

        for (int i = 0; i < tmp.length; i++) {
          nodusPlugins[i] = tmp[i];
        }

        for (int i = 0; i < plugins.length; i++) {
          nodusPlugins[tmp.length + i] = plugins[i];
        }
      }
    }
  }

  /** "About Nodus" splash screen and info. */
  public void menuItemAboutActionPerformed() {
    SplashDlg splashDialog = new SplashDlg(this);
    splashDialog.setLocationRelativeTo(this);
    splashDialog.setVisible(true);
  }

  /**
   * Change the MapBean's background color...
   *
   * @param e ActionEvent
   */
  private void menuItemControlBackgroundActionPerformed(ActionEvent e) {
    Paint newPaint =
        OMColorChooser.showDialog(
            this, menuItemControlBackground.getText(), getMapBean().getBackground());

    if (newPaint != null) {
      String colorString = Integer.toString(((java.awt.Color) newPaint).getRGB());
      Environment.set(Environment.BackgroundColor, colorString);
      getMapBean().setBackground((java.awt.Color) newPaint);
      getMapBean().setBckgrnd(newPaint);
    }
  }

  /**
   * Toggle the visibility of the ControlPanel.
   *
   * @param e ActionEvent
   */
  private void menuItemControlControlpanelActionPerformed(ActionEvent e) {
    boolean selected = controlPanel.isVisible();
    controlPanel.setVisible(!selected);

    if (selected) {
      menuItemControlControlpanel.setText(
          i18n.get(NodusMapPanel.class, "Display_Control_Panel", "Display Control Panel"));

    } else {
      menuItemControlControlpanel.setText(
          i18n.get(NodusMapPanel.class, "Hide_Control_Panel", "Hide Control Panel"));
    }
  }

  /**
   * Adds additional OpenMap layers to the MapBean. See OpenMap documentation for more details on
   * how to write the files for these layers.
   *
   * @param a ActionEvent
   */
  private void menuItemControlLoadLayersActionPerformed(ActionEvent a) {
    JFileChooser fileChooser =
        new JFileChooser(nodusProperties.getProperty(NodusC.PROP_LAST_PATH, "."));
    fileChooser.setFileFilter(
        new NodusFileFilter(
            NodusC.TYPE_OPENMAP,
            i18n.get(NodusMapPanel.class, "OpenMap_layers", "OpenMap layers")));

    int returnVal = fileChooser.showOpenDialog(null);

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      Properties props = new Properties();

      try {
        props.load(new FileInputStream(fileChooser.getSelectedFile().getCanonicalPath()));
      } catch (IOException ex) {
        System.out.println(ex.toString());
        return;
      }

      nodusProject.addOpenMapLayers(props);
    } else {
      return;
    }
  }

  /**
   * Toggle the visibility of the Toolpanel.
   *
   * @param e ActionEvent
   */
  private void menuItemControlToolpanelActionPerformed(ActionEvent e) {
    boolean selected = toolPanel.isVisible();
    toolPanel.setVisible(!selected);

    if (selected) {
      menuItemControlToolpanel.setText(
          i18n.get(NodusMapPanel.class, "Display Tool Panel", "Display Tool Panel"));

    } else {
      menuItemControlToolpanel.setText(
          i18n.get(NodusMapPanel.class, "Hide Tool Panel", "Hide Tool Panel"));
    }
  }

  /**
   * Close a open project.
   *
   * @param e ActionEvent
   */
  private void menuItemFileCloseActionPerformed(ActionEvent e) {
    if (nodusProject != null) {
      nodusProject.close();
      displayScenarioCombo(false);
      displayPoliticalBoundaries(true, true);
    }
  }

  /** Tasks that must be performed when the application is closed. */
  public void menuItemFileExitActionPerformed() {
    closeAndSaveState();
    System.exit(0);
  }

  /**
   * Open a new project.
   *
   * @param e ActionEvent
   */
  private void menuItemFileOpenActionPerformed(ActionEvent e) {
    String projectName = nodusProject.openProject();
    openProject(projectName);
  }

  /**
   * Loads a project.
   *
   * @param projectName The project file name (with full path)
   */
  public void openProject(String projectName) {
    if (projectName != null) {
      nodusProject.close(); // In case a project was already loaded

      displayScenarioCombo(true);
      nodusProject.openProject(projectName);

      // Reset mouse mode
      String mouseModeId = nodusProject.getLocalProperty(NodusC.PROP_ACTIVE_MOUSE_MODE, null);
      if (mouseModeId != null) {
        setActiveMouseMode(mouseModeId);
      }
      resetText();
      updateScenarioComboBox();
    }
  }

  /**
   * Print the content of the current MapBean.
   *
   * @param e ActionEvent
   */
  private void menuItemFilePrintActionPerformed(ActionEvent e) {
    MapBeanPrinter.printMap(getMapBean());
  }

  /**
   * Save the project and update/commit into the database.
   *
   * @param e ActionEvent.
   */
  private void menuItemFileSaveActionPerformed(ActionEvent e) {
    nodusProject.saveEsriLayers();
  }

  /** Opens the global preferences dialog box. */
  public void menuItemFileGlobalPreferencesActionPerformed() {
    GlobalPreferencesDlg dlg = new GlobalPreferencesDlg(this);
    dlg.setVisible(true);
  }

  /**
   * Launch Groovy console.
   *
   * @param e ActionEvent
   */
  private void menuItemGroovyConsoleActionPerformed(ActionEvent e) {

    String path = "";
    if (nodusProject.isOpen()) {
      path = nodusProject.getLocalProperty("project.path");
    }

    boolean useGroovyConsole =
        Boolean.parseBoolean(nodusProperties.getProperty(NodusC.PROP_USE_GROOVY_CONSOLE, "false"));
    if (!useGroovyConsole) {
      new NodusGroovyConsole(this, path, "");
    } else {
      groovy.ui.Console console = new groovy.ui.Console();

      // Set some defaults in UI
      console.askToInterruptScript();
      console.setAutoClearOutput(true);
      console.setSaveOnRun(true);

      JCheckBox dummyCheckBox = new JCheckBox();
      dummyCheckBox.setSelected(false);
      console.showScriptInOutput(new EventObject(dummyCheckBox));

      dummyCheckBox.setSelected(true);
      console.threadInterruption(new EventObject(dummyCheckBox));

      console.setCurrentFileChooserDir(new File(path));
      console.setVariable("nodusMapPanel", this);
      console.setVariable("nodusMainFrame", this);
      console.run();
    }
  }

  /**
   * Opens the assignment dialog that will launch a chooosen assignment procedure.
   *
   * @param a ActionEvent
   */
  private void menuItemProjectAssignmentActionPerformed(ActionEvent a) {

    if (nodusProject.isOpen()) {
      JDialog dlg = new AssignmentDlg(this);
      dlg.setVisible(true);
    }
  }

  /**
   * Opens the cost function file in a text editor (embedded Notepad).
   *
   * @param e ActionEvent
   */
  private void menuItemProjectCostsActionPerformed(ActionEvent e) {
    if (nodusProject.isOpen()) {
      // Retrieve the cost function file name for the current scenario
      int currentScenario = nodusProject.getLocalProperty(NodusC.PROP_SCENARIO, 0);
      String fileName = nodusProject.getLocalProperty(NodusC.PROP_COST_FUNCTIONS + currentScenario);
      if (fileName == null) {
        // There is no scenario specific cost function file
        String defaultValue =
            nodusProject.getLocalProperty(NodusC.PROP_PROJECT_DOTNAME) + NodusC.TYPE_COSTS;
        fileName = nodusProject.getLocalProperty(NodusC.PROP_COST_FUNCTIONS, defaultValue);
      }
      String path = nodusProject.getLocalProperty(NodusC.PROP_PROJECT_DOTPATH);

      new NotePad(this, path, fileName);
    }
  }

  /**
   * Opens the "Display results" dialog box, that makes it possible to visualize flows, paths, ...
   *
   * @param e ActionEvent
   */
  private void menuItemProjectShowResultsActionPerformed(ActionEvent e) {
    if (nodusProject.isOpen()) {

      ResultsDlg rl = new ResultsDlg(this);
      rl.setVisible(true);
    }
  }

  /**
   * Opens the "Project preferences" dialog box.
   *
   * @param e ActionEvent
   */
  private void menuItemProjectPreferencesActionPerformed(ActionEvent e) {
    if (nodusProject.isOpen()) {
      JDialog dlg = new ProjectPreferencesDlg(nodusProject);
      dlg.setVisible(true);
    }
  }

  /**
   * Opens the "Compare scenarios" dialog box that makes it possible to generate a new scenario that
   * represents the differences between two existing scenarios.
   *
   * @param e ActionEvent
   */
  private void menuItemProjectScenariosActionPerformed(ActionEvent e) {
    if (nodusProject.isOpen()) {

      ScenariosDlg s = new ScenariosDlg(this);
      s.setVisible(true);
    }
  }

  /**
   * Opens the service editor.
   *
   * @param e Action event
   */
  private void menuItemProjectServicesActionPerformed(ActionEvent e) {
    if (nodusProject.isOpen()) {
      getNodusProject().getServiceEditor().showGUI();
    }
  }

  /**
   * Opens the SQL console on the database that contains the open project.
   *
   * @param a ActionEvent
   */
  private void menuItemProjectSQLConsoleActionPerformed(ActionEvent a) {
    if (nodusProject.isOpen()) {
      new SQLConsole(nodusProject);
    }
  }

  /**
   * Allows the user to make a choice between the available languages.
   *
   * @param e ActionEvent
   */
  private void menuItemToolLanguageActionPerformed(ActionEvent e) {

    new LanguageChooser(this).setVisible(true);
  }

  /**
   * Allows the user to make a choice between the available and supported PLAFs.
   *
   * @param e ActionEvent
   */
  private void menuItemToolLookAndFeelActionPerformed(ActionEvent e) {
    new LookAndFeelManager(this).setVisible(true);
  }

  /**
   * Handles the call to the relevant plugin's "1execute" command.
   *
   * @param e ActionEvent
   */
  private void pluginMenuActionPerformed(ActionEvent e) {
    int n = Integer.parseInt(e.getActionCommand());
    nodusPlugins[n].execute();
  }

  /** Removes the menu items relative to the project plugins. */
  public void removeProjectPlugins() {
    // Remove all the project menu items
    Iterator<JMenuItem> it = projectPluginsMenuItems.iterator();

    while (it.hasNext()) {
      JMenuItem searchedMenuItem = it.next();

      // find it in the menus and remove it
      int nbMenus = nodusMenuBar.getMenuCount();

      for (int i = 0; i < nbMenus; i++) {
        JMenu menu = nodusMenuBar.getMenu(i);
        int nbItems = menu.getMenuComponentCount();

        // Go through the menus, starting from the end
        for (int j = nbItems - 1; j >= 0; j--) {
          JMenuItem menuItem = menu.getItem(j);

          // Could be a separator...
          if (menuItem == null) {
            continue;
          }

          // Remove if found
          if (menuItem.equals(searchedMenuItem)) {
            menu.remove(menuItem);
            userDefinedMenus.remove(menu);
          }
        }
      }
    }

    projectPluginsMenuItems.clear();

    // Remove empty menus in the menubar
    int nbMenus = nodusMenuBar.getMenuCount();

    for (int i = nbMenus - 1; i >= 0; i--) {
      JMenu menu = nodusMenuBar.getMenu(i);

      if (menu.getItemCount() == 0) {
        nodusMenuBar.remove(menu);
      }
    }
  }

  /** Resets/clears the default map. */
  public void resetMap() {
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    float scale = NodusC.SCALE_FACTOR / (float) (dim.getWidth() / NodusC.DEFAULT_SCREEN_WIDTH);
    mapBean.setScale(scale);

    mapBean.setCenter(NodusC.MAPBEAN_CENTER_X, NodusC.MAPBEAN_CENTER_Y);
    mapBean.setBackground(getDefaultBackgroundColor());
    getProjectionStack().clearStacks(true, true);
  }

  /** Resets the text in the info delegator (see OpenMap API for more information). */
  public void resetText() {
    setText("");
  }

  /** Resets the title of the main frame. */
  public void resetTitle() {
    getMainFrame().setTitle(NodusC.APPNAME);
  }

  /**
   * Restores the size and location of the main frame, depending on the information saved in the
   * Nodus properties file.
   */
  public void restoreSizeAndLocation() {
    // Restore last saved size and position
    int frameWidth =
        PropUtils.intFromProperties(nodusProperties, NodusC.PROP_FRAME_WIDTH, Integer.MIN_VALUE);
    int frameHeigth =
        PropUtils.intFromProperties(nodusProperties, NodusC.PROP_FRAME_HEIGTH, Integer.MIN_VALUE);
    int x = PropUtils.intFromProperties(nodusProperties, NodusC.PROP_FRAME_X, Integer.MIN_VALUE);
    int y = PropUtils.intFromProperties(nodusProperties, NodusC.PROP_FRAME_Y, Integer.MIN_VALUE);

    if (x != Integer.MIN_VALUE
        && y != Integer.MIN_VALUE
        && frameWidth != Integer.MIN_VALUE
        && frameHeigth != Integer.MIN_VALUE) {

      getMainFrame().setSize(frameWidth, frameHeigth);
      getMainFrame().setLocation(x, y);

      validate();
    }

    projectionStack.clearStacks(true, true);
  }

  /**
   * Launches (or stops) the "on top keeper" that forces the sub windows to remain on top of main
   * frame.
   *
   * @param run If true, launched the mechanism. Else stops it.
   * @param stickyDrawingTool If true, the drawing tool will be "sticked" at the bottom left of the
   *     main frame.
   */
  public void runOnTopKeeper(boolean run, boolean stickyDrawingTool) {
    if (onTopKeeper != null) {
      if (!run) {
        onTopKeeper.stop();
      } else {
        onTopKeeper.run(stickyDrawingTool);
      }
    }
  }

  /**
   * Sets the active mouse mode (see OpenMap API for more information)
   *
   * @param modeId The ID of the mode mouse.
   */
  public void setActiveMouseMode(String modeId) {

    try {
      mouseDelegator.setActiveMouseModeWithID(modeId);
      Cursor c = mouseDelegator.getActiveMouseMode().getModeCursor();
      mapBean.setCursor(c);
    } catch (Exception e) {
      // Invalid mouse mode ID. Do nothing
    }
  }

  /**
   * Sets the wait cursor in the MapPanel.
   *
   * @param busy If true, set the wait cursor, else sets the default cursor.
   */
  public void setBusy(boolean busy) {

    if (busy) {
      busyDepth++;
    } else {
      busyDepth--;
    }

    if (busyDepth < 0) {
      busyDepth = 0;
    }

    if (busy && busyDepth == 1) {
      defaultBeanCursor = getMapBean().getCursor();
      getRootPane().getGlassPane().setVisible(true);
      getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    } else if (!busy && busyDepth == 0) {
      getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      getRootPane().getGlassPane().setVisible(false);
      getMapBean().setCursor(defaultBeanCursor);
    }
  }

  /**
   * Sets the map bean used in this map panel, replace the map bean in the MapHandler if there isn't
   * already one, or if the policy allows replacement. The MapHandler will be created if it doesn't
   * exist via a getMapHandler() method call.
   *
   * @throws MultipleSoloMapComponentException if there is already a map bean in the map handler and
   *     the policy is to reject duplicates (since the MapBean is a SoloMapComponent).
   */
  public void setMapBean(BufferedLayerMapBean bean) {
    if (bean == null && mapBean != null) {
      // remove the current MapBean from the application...
      getMapHandler().remove(mapBean);
    }

    mapBean = bean;

    if (mapBean != null) {
      getMapHandler().add(mapBean);
      add(mapBean, BorderLayout.CENTER);
    }
  }

  /** Sets the NavModeMouse, depending on the type selected in the global preferences. */
  public void addNavMouseMode() {

    int type = 1;
    try {
      type = Integer.parseInt(nodusProperties.getProperty(NodusC.PROP_NAV_MOUSE_MODE, "1"));
    } catch (NumberFormatException e) {
      type = 1;
    }

    if (type == 2) {
      getMapHandler().remove(navMouseMode1);
      getMapHandler().add(navMouseMode2);
    } else {
      getMapHandler().remove(navMouseMode2);
      getMapHandler().add(navMouseMode1);
    }
  }

  /**
   * Displays a message in the InfoDelegator.
   *
   * @param msg The message to display.
   */
  public void setText(String msg) {
    infoDelegator.setLabel("  " + msg);
  }

  /** Sets the title of the main frame, which contains the db name and the project name. */
  public void setTitle() {
    String title;

    // Display only the project name or also its full path
    if (getDisplayFullPath()) {
      title = nodusProject.getLocalProperty(NodusC.PROP_PROJECT_CANONICAL_NAME);
    } else {
      title = nodusProject.getLocalProperty(NodusC.PROP_PROJECT_DOTNAME) + NodusC.TYPE_NODUS;
    }

    // Get database engine name
    String dbName = JDBCUtils.getDbEngineName(nodusProject.getMainJDBCConnection());

    // Set title
    getMainFrame().setTitle(NodusC.APPNAME + " [(" + dbName + ") " + title + "]");
  }

  /**
   * Starts a new ProgressBar. See OpenMap documentation for more details on the progress bar
   * mechanism implemented on the MapBean.
   *
   * @param finishedValue The max value to reach.
   */
  public void startProgress(int finishedValue) {
    taskLength = finishedValue;
    currentTask = 0;
    canceled = false;
    setBusy(true);

    ProgressEvent evt = new ProgressEvent(getMapBean(), ProgressEvent.START, "", finishedValue, 0);
    infoDelegator.updateProgress(evt);
  }

  /**
   * Ends a ProgressBar. See OpenMap documentation for more details on the progress bar mechanism
   * implemented on the MapBean.
   */
  public void stopProgress() {
    ProgressEvent evt = new ProgressEvent(getMapBean(), ProgressEvent.DONE, "", 0, 0);
    infoDelegator.updateProgress(evt);
    resetText();
    taskLength = 0;
    currentTask = 0;
    setBusy(false);
  }

  /**
   * Updated a ProgressBar. See OpenMap documentation for more details on the progress bar mechanism
   * implemented on the MapBean.
   *
   * @param msg boolean
   * @return boolean
   */
  public boolean updateProgress(String msg) {
    // getMapBean().requestFocusInWindow(true);
    if (canceled) {
      canceled = false;
      if (JOptionPane.showConfirmDialog(
              this,
              i18n.get(NodusMapPanel.class, "Abort_task?", "Abort task?"),
              "Nodus",
              JOptionPane.YES_NO_OPTION)
          == JOptionPane.YES_OPTION) {

        busyDepth = 1;
        stopProgress();
        setText(i18n.get(NodusMapPanel.class, "Task_aborted", "Task aborted"));

        return false;
      }
    }

    ProgressEvent evt =
        new ProgressEvent(
            getMapBean(), ProgressEvent.UPDATE, "  " + msg, taskLength, ++currentTask);
    infoDelegator.updateProgress(evt);

    return true;
  }

  /** Updates the scenario combo with the current scenario. */
  public void updateScenarioComboBox() {

    if (!nodusProject.isOpen()) {
      return;
    }

    // Avoid reentrance by removing the action listeners
    ActionListener[] al = scenarioComboBox.getActionListeners();
    for (ActionListener element : al) {
      scenarioComboBox.removeActionListener(element);
    }

    // If an assignment is running, its descriptions is displayed, even if there is no output yet.
    boolean isAssignmentRunning = true;
    if (getAssignmentMenuItem().isEnabled()) {
      isAssignmentRunning = false;
    }

    JDBCUtils jdbcUtils = new JDBCUtils(nodusProject.getMainJDBCConnection());
    int currentScenario = nodusProject.getLocalProperty(NodusC.PROP_SCENARIO, 0);
    scenarioComboBox.removeAllItems();

    for (int i = 0; i < NodusC.MAXSCENARIOS; i++) {
      String virtualNetTableName =
          nodusProject.getLocalProperty(NodusC.PROP_PROJECT_DOTNAME) + NodusC.SUFFIX_VNET;
      virtualNetTableName =
          nodusProject.getLocalProperty(NodusC.PROP_VNET_TABLE, virtualNetTableName) + i;
      virtualNetTableName = jdbcUtils.getCompliantIdentifier(virtualNetTableName);

      boolean tableExists = jdbcUtils.tableExists(virtualNetTableName);

      if (tableExists || i == currentScenario || isAssignmentRunning) {
        String description = i18n.get(NodusMapPanel.class, "Empty_scenario", "empty scenario");

        if (tableExists || isAssignmentRunning) {
          description = nodusProject.getLocalProperty(NodusC.PROP_ASSIGNMENT_DESCRIPTION + i, "");
          description = description.trim();
          if (description.equals("")) {
            description =
                i18n.get(
                    NodusMapPanel.class, "No_description_available", "No description available");
          }
        }

        description = i + " - " + description;

        scenarioComboBox.addItem(description);
        if (i == currentScenario) {
          scenarioComboBox.setSelectedItem(description);
        }
      }
    }
    setTitle();

    // Reset action listeners
    for (ActionListener element : al) {
      scenarioComboBox.addActionListener(element);
    }
  }

  /**
   * When double-clicking on the "scale" component of the control panel, a scale threshold can be
   * set. When the zoom level results is a lower scale, the nodes and links styles are rendered.
   * With a higher level zoom threshold, only the color of the edges of the nodes or links are
   * rendered.
   *
   * @param renderingScaleThresold The scale under which nodes and links are fully rendered.
   */
  public void setRenderingScaleThreshold(float renderingScaleThresold) {
    this.renderingScaleThresold = renderingScaleThresold;
    controlPanel.refreshScale();

    if (nodusProject.isOpen()) {
      nodusProject.setLocalProperty(NodusC.PROP_RENDERING_SCALE_THRESHOLD, renderingScaleThresold);

      // Refresh all the Nodus layers
      NodusEsriLayer[] nel = nodusProject.getNodeLayers();
      for (int i = 0; i < nel.length; i++) {
        nel[i].doPrepare();
      }
      nel = nodusProject.getLinkLayers();
      for (int i = 0; i < nel.length; i++) {
        nel[i].doPrepare();
      }
    }
  }

  /**
   * Returns the scale under which the nodes and link styles are fully rendered.
   *
   * @return The scale threshold.
   */
  public float getRenderingScaleThreshold() {
    return renderingScaleThresold;
  }
}
