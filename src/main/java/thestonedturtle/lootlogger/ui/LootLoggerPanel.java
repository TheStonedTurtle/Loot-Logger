/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thestonedturtle.lootlogger.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.loottracker.LootRecordType;
import thestonedturtle.lootlogger.LootLoggerPlugin;
import thestonedturtle.lootlogger.data.BossTab;
import thestonedturtle.lootlogger.data.LootLog;
import thestonedturtle.lootlogger.localstorage.LTRecord;

@Slf4j
public class LootLoggerPanel extends PluginPanel
{
	private static final BufferedImage ICON_DELETE;
	private static final BufferedImage ICON_REFRESH;
	private static final BufferedImage ICON_BACK;
	private static final BufferedImage ICON_REPLAY;

	private final static Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private final static Color BUTTON_HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	static
	{
		ICON_DELETE = ImageUtil.loadImageResource(LootLoggerPlugin.class, "delete-white.png");
		ICON_REFRESH = ImageUtil.loadImageResource(LootLoggerPlugin.class, "refresh-white.png");
		ICON_BACK = ImageUtil.loadImageResource(LootLoggerPlugin.class, "back-arrow-white.png");
		ICON_REPLAY = ImageUtil.loadImageResource(LootLoggerPlugin.class, "replay-white.png");
	}

	private final ItemManager itemManager;
	private final LootLoggerPlugin plugin;

	private LootPanel lootPanel;
	@Getter
	private LootLog lootLog;
	private SelectionPanel selectionPanel;

	public LootLoggerPanel(final ItemManager itemManager, final LootLoggerPlugin plugin)
	{
		super(false);
		this.itemManager = itemManager;
		this.plugin = plugin;

		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.setLayout(new BorderLayout());

		showSelectionView();
	}

	public void useLog(final LootLog log)
	{
		lootLog = log;
		showLootView();
	}

	public void requestLootLog(final LootRecordType type, final String name)
	{
		// For some reason removing all the components when there's a lot of names in the selectionPanel causes lag.
		// Removing them here seems to mitigate the lag
		if (selectionPanel != null)
		{
			selectionPanel.getNamePanel().removeAll();
		}

		plugin.requestLootLog(type, name);
	}

	// Loot Selection view
	public void showSelectionView()
	{
		this.removeAll();
		lootLog = null;
		lootPanel = null;

		final PluginErrorPanel errorPanel = new PluginErrorPanel();
		errorPanel.setBorder(new EmptyBorder(10, 25, 10, 25));
		errorPanel.setContent("Loot Logger", "Select the Activity, Player, or NPC you wish to view loot for");

		selectionPanel = new SelectionPanel(plugin.config.bossButtons(), plugin.getLootNames(), this, itemManager);

		this.add(errorPanel, BorderLayout.NORTH);
		this.add(wrapContainer(selectionPanel), BorderLayout.CENTER);

		this.revalidate();
		this.repaint();
	}

	// Loot breakdown view
	public void showLootView()
	{
		this.removeAll();
		selectionPanel = null;

		final JPanel title = createLootViewTitle();
		lootPanel = new LootPanel(lootLog, plugin.config, itemManager, (lootRecordType, name) -> {
			if (clearData(lootRecordType, name))
			{
				requestLootLog(lootRecordType, lootLog.getName());
			}
		});

		this.add(title, BorderLayout.NORTH);
		this.add(wrapContainer(lootPanel), BorderLayout.CENTER);

		this.revalidate();
		this.repaint();
	}

	// Title element for Loot breakdown view
	private JPanel createLootViewTitle()
	{
		final String name = lootLog.getName();
		final JPanel title = new JPanel();
		title.setBorder(new CompoundBorder(
				new EmptyBorder(10, 8, 8, 8),
				new MatteBorder(0, 0, 1, 0, Color.GRAY)
		));
		title.setLayout(new BorderLayout());
		title.setBackground(BACKGROUND_COLOR);

		final JPanel first = new JPanel();
		first.setBackground(BACKGROUND_COLOR);

		// Back Button
		final JLabel back = createIconLabel(ICON_BACK);
		back.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				showSelectionView();
			}
		});
		back.setToolTipText("Back to selection screen");

		final JLabel text = new JLabel(name);
		text.setForeground(Color.WHITE);

		first.add(back);
		first.add(text);

		final JPanel second = new JPanel();
		second.setBackground(BACKGROUND_COLOR);

		// Refresh Data button
		final JLabel refresh = createIconLabel(ICON_REFRESH);
		refresh.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				requestLootLog(lootLog.getType(), name);
			}
		});
		refresh.setToolTipText("Refresh panel");

		// Clear data button
		final JLabel clear = createIconLabel(ICON_DELETE);
		clear.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (clearData(lootLog.getType(), name))
				{
					showSelectionView();
				}
			}
		});
		clear.setToolTipText("Clear stored data");

		// Clear data button
		final JLabel replay = createIconLabel(ICON_REPLAY);
		replay.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				playbackLoot();
			}
		});
		replay.setToolTipText("Replay Loot");

		second.add(refresh);
		second.add(clear);
		second.add(replay);

		title.add(first, BorderLayout.WEST);
		title.add(second, BorderLayout.EAST);

		return title;
	}

	private JLabel createIconLabel(final BufferedImage icon)
	{
		final JLabel label = new JLabel();
		label.setIcon(new ImageIcon(icon));
		label.setOpaque(true);
		label.setBackground(BACKGROUND_COLOR);

		label.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				label.setBackground(BUTTON_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				label.setBackground(BACKGROUND_COLOR);
			}
		});

		return label;
	}

	// Wrap the panel inside a scroll pane
	private JScrollPane wrapContainer(final JPanel container)
	{
		final JPanel wrapped = new JPanel(new BorderLayout());
		wrapped.add(container, BorderLayout.NORTH);
		wrapped.setBackground(BACKGROUND_COLOR);

		final JScrollPane scroller = new JScrollPane(wrapped);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scroller.setBackground(BACKGROUND_COLOR);

		return scroller;
	}

	// Clear stored data and return to selection screen
	private boolean clearData(final LootRecordType type, final String name)
	{
		// Confirm delete action
		final int delete = JOptionPane.showConfirmDialog(this.getRootPane(), "<html>Are you sure you want to clear all data for this tab?<br/>There is no way to undo this action.</html>", "Warning", JOptionPane.YES_NO_OPTION);
		if (delete == JOptionPane.YES_OPTION)
		{
			boolean deletedAtleastOne = plugin.clearStoredDataByName(type, name);

			final BossTab tab = BossTab.getByName(name);
			if (tab != null)
			{
				for (final String alias : tab.getAliases())
				{
					deletedAtleastOne |= plugin.clearStoredDataByName(type, alias);
				}
			}

			if (!deletedAtleastOne)
			{
				JOptionPane.showMessageDialog(this.getRootPane(), "Unable to clear stored data, please try again.");
				return false;
			}

			return true;
		}

		return false;
	}

	public void addLog(final LTRecord r)
	{
		if (lootLog == null)
		{
			requestLootLog(r.getType(), r.getName());
		}
		else if (lootLog.getName().equalsIgnoreCase(r.getName()))
		{
			lootPanel.addedRecord(r);
		}
		else if (lootLog.getName().equalsIgnoreCase(LootLoggerPlugin.SESSION_NAME)
			|| lootPanel.getMinionGridMap().containsKey(r.getName().toLowerCase()))
		{
			lootPanel.addMinionRecord(r);
		}
	}

	// Refresh panel when config options are changed
	public void refreshUI()
	{
		log.debug("Refreshing UI");
		if (lootLog == null)
		{
			showSelectionView();
		}
		else
		{
			showLootView();
		}
	}

	private void playbackLoot()
	{
		if (lootPanel == null)
		{
			return;
		}

		// Create a new thread for this so it doesn't cause swing freezes
		final ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
		if (lootLog != null)
		{
			ex.schedule(lootPanel::playback, 0, TimeUnit.SECONDS);
		}
	}
}
