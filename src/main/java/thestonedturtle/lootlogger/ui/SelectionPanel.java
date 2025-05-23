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

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.Text;
import net.runelite.http.api.loottracker.LootRecordType;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import thestonedturtle.lootlogger.LootLoggerPlugin;
import thestonedturtle.lootlogger.data.BossTab;

class SelectionPanel extends JPanel
{
	private final static Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private final static Color BUTTON_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private final static Color BUTTON_HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR;
	private static final JaroWinklerDistance DISTANCE = new JaroWinklerDistance();

	private final SetMultimap<LootRecordType, String> names;
	private final LootLoggerPanel parent;
	private final ItemManager itemManager;

	private final IconTextField searchBar = new IconTextField();
	@Getter
	private final JPanel namePanel = new JPanel();
	private final List<JPanel> panels = new ArrayList<>();

	private boolean configToggle;

	SelectionPanel(
		final boolean configToggle,
		final SetMultimap<LootRecordType, String> names,
		final LootLoggerPanel parent,
		final ItemManager itemManager)
	{
		this.names = names == null ? HashMultimap.create() : names;
		this.parent = parent;
		this.itemManager = itemManager;
		this.configToggle = configToggle;

		this.setLayout(new GridBagLayout());
		this.setBackground(BACKGROUND_COLOR);

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				onSearchBarChanged();
			}
		});

		namePanel.setLayout(new GridBagLayout());

		createPanel();
	}

	private void createPanel()
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 0, 0, 0);

		// Add Session Data
		this.add(createNamePanel(LootRecordType.UNKNOWN, LootLoggerPlugin.SESSION_NAME), c);
		c.gridy++;

		// Add the bosses tabs, by category, to tabGroup
		if (configToggle)
		{
			this.add(createBossTabPanel(), c);
			c.gridy++;
		}

		this.add(searchBar, c);
		c.gridy++;

		addNamesToPanel(this.names);
		this.add(namePanel, c);
	}

	private JPanel createBossTabPanel()
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 0, 0, 0);

		final JPanel container = new JPanel(new GridBagLayout());
		container.setBorder(new EmptyBorder(0, 0, 10, 0));

		for (final String categoryName : BossTab.getCategories())
		{
			container.add(createTabCategory(categoryName), c);
			c.gridy++;
		}

		return container;
	}

	private void addNamesToPanel(final SetMultimap<LootRecordType, String> names)
	{
		removeAllCollapsableSections();
		namePanel.removeAll();

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 4, 0);

		for (final LootRecordType type : LootRecordType.values())
		{
			if (!names.containsKey(type))
			{
				continue;
			}

			final JPanel section = createCollapseableSection(type, names.get(type));
			if (section != null)
			{
				namePanel.add(section, c);
				c.gridy++;
			}
		}

		namePanel.revalidate();
	}

	@Nullable
	private JPanel createCollapseableSection(final LootRecordType type, Set<String> names)
	{
		// Filter out boss tabs if the config toggle is enabled
		names = names.stream().filter((n) ->
		{
			if (!configToggle)
			{
				return true;
			}

			BossTab b = BossTab.getByName(n);
			return b == null || !b.getType().equals(type);
		}).collect(Collectors.toSet());
		if (names.isEmpty())
		{
			return null;
		}

		final JPanel container = new JPanel(new DynamicGridLayout(0, 1));
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new EmptyBorder(0, 8, 0, 8));

		final JLabel headerLabel = new JLabel(Text.titleCase(type));
		headerLabel.setToolTipText("Click to toggle this section");
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		headerLabel.setBackground(BACKGROUND_COLOR);
		headerLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(4, 0, 2, 0),
			BorderFactory.createMatteBorder(0, 0, 2, 0, BUTTON_HOVER_COLOR)
		));
		headerLabel.setOpaque(true);
		headerLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				headerLabel.setBackground(BUTTON_HOVER_COLOR);
				panel.setBackground(BUTTON_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				headerLabel.setBackground(BACKGROUND_COLOR);
				panel.setBackground(BACKGROUND_COLOR);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				panel.setVisible(!panel.isVisible());
				if (panel.isVisible())
				{
					container.add(panel);
				}
				else
				{
					container.remove(panel);
				}
				container.revalidate();
				container.repaint();
			}
		});
		container.add(headerLabel);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 0, 2, 0);

		for (final String name : new TreeSet<>(names))
		{
			panel.add(createNamePanel(type, name), c);
			c.gridy++;
		}
		container.add(panel);
		panels.add(panel);
		
		return container;
	}

	private JPanel createNamePanel(final LootRecordType type, final String name)
	{
		final JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		p.add(new JLabel(name));
		p.setBackground(BUTTON_COLOR);
		p.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				p.setBackground(BUTTON_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				p.setBackground(BUTTON_COLOR);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				parent.requestLootLog(type, name);
			}
		});

		return p;
	}

	// Creates all tabs for a specific category
	private JPanel createTabCategory(final String categoryName)
	{
		final JPanel container = new JPanel();
		container.setLayout(new GridBagLayout());
		container.setBorder(new EmptyBorder(0, 5, 0, 5));

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;

		final MaterialTabGroup thisTabGroup = new MaterialTabGroup();
		thisTabGroup.setLayout(new GridLayout(0, 4, 7, 7));
		thisTabGroup.setBorder(new EmptyBorder(0, 0, 4, 0));

		final JLabel name = new JLabel(categoryName);
		name.setBorder(new EmptyBorder(8, 0, 0, 0));
		name.setForeground(Color.WHITE);
		name.setVerticalAlignment(SwingConstants.CENTER);

		final Collection<BossTab> categoryTabs = BossTab.getByCategoryName(categoryName);
		for (final BossTab tab : categoryTabs)
		{
			// Create tab (with hover effects/text)
			final MaterialTab materialTab = new MaterialTab("", thisTabGroup, null);
			materialTab.setName(tab.getName());
			materialTab.setToolTipText(tab.getName());
			materialTab.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseEntered(MouseEvent e)
				{
					materialTab.setBackground(BUTTON_HOVER_COLOR);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					materialTab.setBackground(BUTTON_COLOR);
				}
			});

			materialTab.setOnSelectEvent(() ->
			{
				parent.requestLootLog(tab.getType(), tab.getName());
				return true;
			});

			// Attach Icon to the Tab
			final AsyncBufferedImage image = itemManager.getImage(tab.getItemID());
			final Runnable resize = () ->
			{
				materialTab.setIcon(new ImageIcon(image.getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
				materialTab.setOpaque(true);
				materialTab.setBackground(BUTTON_COLOR);
				materialTab.setHorizontalAlignment(SwingConstants.CENTER);
				materialTab.setVerticalAlignment(SwingConstants.CENTER);
				materialTab.setPreferredSize(new Dimension(35, 35));
			};
			image.onLoaded(resize);
			resize.run();

			thisTabGroup.addTab(materialTab);
		}

		container.add(name, c);
		c.gridy++;
		container.add(thisTabGroup, c);

		return container;
	}

	private void onSearchBarChanged()
	{
		final String text = searchBar.getText();
		if (Strings.isNullOrEmpty(text))
		{
			addNamesToPanel(this.names);
		}
		else
		{
			addNamesToPanel(filterNames(this.names, text));
		}
	}

	private SetMultimap<LootRecordType, String> filterNames(final SetMultimap<LootRecordType, String> nameMap, final String searchText)
	{
		final String[] searchTerms = searchText.toLowerCase().split(" ");
		return nameMap.entries().stream()
			.filter(entry -> matchesSearchTerm(entry.getValue(), searchTerms))
			.collect(Multimaps.toMultimap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				HashMultimap::create
				)
			);
	}

	private static boolean matchesSearchTerm(final String name, final String[] terms)
	{
		for (final String term : terms)
		{
			if (Arrays.stream(name.toLowerCase().split(" ")).noneMatch((nameTerm ->
				nameTerm.contains(term) || DISTANCE.apply(nameTerm, term) > 0.9)))
			{
				return false;
			}
		}

		return true;
	}

	public void removeAllCollapsableSections()
	{
		// Removing each panel individually drastically increases performance when there's a large number of name panels
		for (final JPanel p: panels)
		{
			p.removeAll();
		}
	}
}
