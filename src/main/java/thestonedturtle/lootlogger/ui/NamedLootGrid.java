/*
 * Copyright (c) 2021, TheStonedTurtle <https://github.com/TheStonedTurtle>
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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.http.api.loottracker.LootRecordType;
import thestonedturtle.lootlogger.data.LootLog;
import thestonedturtle.lootlogger.localstorage.LTItemEntry;

public class NamedLootGrid extends JPanel
{
	private boolean isCollapsed = false;
	private final LootGridName namePanel;
	private final LootGrid grid;
	@Getter
	private long price;

	NamedLootGrid(final String name, final int count, final long price, final LootGrid grid,
				 final LootRecordType type, final BiConsumer<LootRecordType, String> clearData) {
		this.grid = grid;
		this.price = price;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

		namePanel = new LootGridName(name, count, price);

		// Clear data popup-menu
		final JPopupMenu menu = new JPopupMenu();
		final JMenuItem delete = new JMenuItem("Clear stored data");
		delete.addActionListener(ev -> clearData.accept(type, name));
		menu.add(delete);
		namePanel.setComponentPopupMenu(menu);
		namePanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					return;
				}

				changeCollapse();
			}
		});

		this.add(namePanel);

		grid.setBorder(new EmptyBorder(0, 0, 5, 0));
		this.add(grid);
	}

	void updateGrid(final LootLog log, final LTItemEntry[] itemsToDisplay, final ItemManager itemManager)
	{
		price = log.getLootValue(false);
		namePanel.updateLabel(log.getName(), log.getRecords().size(), price);

		grid.updateGrid(itemsToDisplay, itemManager);

		if (isCollapsed)
		{
			changeCollapse();
		}
	}

	void changeCollapse()
	{
		isCollapsed = !isCollapsed;
		grid.setVisible(!isCollapsed);

		// Copied from RuneLite's LootTrackerBox::applyDimmer
		for (Component component : namePanel.getComponents())
		{
			Color color = component.getForeground();
			component.setForeground(isCollapsed ? color.darker() : color.brighter());
		}
	}
}
