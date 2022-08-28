/**
 * Unlicensed code created by A Softer Space, 2022
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.mediaTextCreator;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.gui.MenuItemForMainMenu;
import com.asofterspace.toolbox.Utils;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Robot;
import java.io.IOException;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class GUI extends MainWindow {

	private final static String CONFIG_KEY_WIDTH = "mainFrameWidth";
	private final static String CONFIG_KEY_HEIGHT = "mainFrameHeight";
	private final static String CONFIG_KEY_LEFT = "mainFrameLeft";
	private final static String CONFIG_KEY_TOP = "mainFrameTop";

	private Database database;

	private TimingCtrl timingCtrl;

	private JTextArea inputField;

	private JMenuItem close;
	private MenuItemForMainMenu playBtn;

	private ConfigFile configuration;

	private boolean currentlyPlaying = true;


	public GUI(Database database, ConfigFile config) {

		this.database = database;

		this.configuration = config;

		timingCtrl = new TimingCtrl(this);
	}

	@Override
	public void run() {

		super.create();

		refreshTitleBar();

		createMenu(mainFrame);

		createMainPanel(mainFrame);

		// do not call super.show, as we are doing things a little bit
		// differently around here (including restoring from previous
		// position...)
		// super.show();

		final Integer lastWidth = configuration.getInteger(CONFIG_KEY_WIDTH, -1);
		final Integer lastHeight = configuration.getInteger(CONFIG_KEY_HEIGHT, -1);
		final Integer lastLeft = configuration.getInteger(CONFIG_KEY_LEFT, -1);
		final Integer lastTop = configuration.getInteger(CONFIG_KEY_TOP, -1);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Stage everything to be shown
				mainFrame.pack();

				// Actually display the whole jazz
				mainFrame.setVisible(true);

				if ((lastWidth < 1) || (lastHeight < 1)) {
					GuiUtils.maximizeWindow(mainFrame);
				} else {
					mainFrame.setSize(lastWidth, lastHeight);

					mainFrame.setPreferredSize(new Dimension(lastWidth, lastHeight));

					mainFrame.setLocation(new Point(lastLeft, lastTop));
				}

				mainFrame.addComponentListener(new ComponentAdapter() {
					public void componentResized(ComponentEvent componentEvent) {
						configuration.set(CONFIG_KEY_WIDTH, mainFrame.getWidth());
						configuration.set(CONFIG_KEY_HEIGHT, mainFrame.getHeight());
					}

					public void componentMoved(ComponentEvent componentEvent) {
						configuration.set(CONFIG_KEY_LEFT, mainFrame.getLocation().x);
						configuration.set(CONFIG_KEY_TOP, mainFrame.getLocation().y);
					}
				});
			}
		});
	}

	private JMenuBar createMenu(JFrame parent) {

		JMenuBar menu = new JMenuBar();

		JMenu file = new JMenu("File");
		menu.add(file);

		close = new JMenuItem("Exit");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(close);

		playBtn = new MenuItemForMainMenu("Pause");

		playBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				togglePlaying();
			}
		});
		menu.add(playBtn);

		MenuItemForMainMenu copyAllBtn = new MenuItemForMainMenu("Copy All");

		copyAllBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				inputField.selectAll();
				inputField.copy();
				inputField.setCaretPosition(inputField.getText().length());
			}
		});
		menu.add(copyAllBtn);

		JMenu huh = new JMenu("?");

		JMenuItem openConfigPath = new JMenuItem("Open Config Path");
		openConfigPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(configuration.getParentDirectory().getJavaFile());
				} catch (IOException ex) {
					// do nothing
				}
			}
		});
		huh.add(openConfigPath);

		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aboutMessage = "This is the " + Utils.getProgramTitle() + ".\n" +
					"Version: " + Utils.getVersionNumber() + " (" + Utils.getVersionDate() + ")\n" +
					"Brought to you by: A Softer Space";
				JOptionPane.showMessageDialog(mainFrame, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		huh.add(about);
		menu.add(huh);

		parent.setJMenuBar(menu);

		return menu;
	}

	private JPanel createMainPanel(JFrame parent) {

		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(800, 500));
		GridBagLayout mainPanelLayout = new GridBagLayout();
		mainPanel.setLayout(mainPanelLayout);

		inputField = new JTextArea();
		inputField.setLineWrap(true);

		// listen to text updates
		inputField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				receivedUpdate();
			}
			public void removeUpdate(DocumentEvent e) {
				receivedUpdate();
			}
			public void insertUpdate(DocumentEvent e) {
				receivedUpdate();
			}
			private void receivedUpdate() {
				// stop playing while anything is being typed
				stopPlaying();

				timingCtrl.setInputReceivedNow();
			}
		});

		mainPanel.add(inputField, new Arrangement(0, 0, 0.2, 1.0));

		parent.add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	private void refreshTitleBar() {
		mainFrame.setTitle(Utils.getProgramTitle());
	}

	private void togglePlaying() {

		currentlyPlaying = !currentlyPlaying;

		if (currentlyPlaying) {
			playBtn.setText("Pause");
		} else {
			playBtn.setText("Play");
		}

		try {
			Robot robot = new Robot();

			// bring media player to front
			// (which we assume to be the next window after this one)
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_ALT);

			// send space bar keystroke
			robot.keyPress(KeyEvent.VK_SPACE);
			robot.keyRelease(KeyEvent.VK_SPACE);

		} catch (AWTException ae) {
			ae.printStackTrace();
		}

		// bring this program back to front
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				mainFrame.toFront();
				mainFrame.requestFocus();
				mainFrame.repaint();
				inputField.requestFocus();
			}
		});
	}

	public void startPlaying() {

		if (!currentlyPlaying) {
			togglePlaying();
		}
	}

	public void stopPlaying() {

		if (currentlyPlaying) {
			togglePlaying();
		}
	}

}
