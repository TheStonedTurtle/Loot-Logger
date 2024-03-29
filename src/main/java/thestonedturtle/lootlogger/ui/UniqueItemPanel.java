/*
 * Copyright (c) 2018, TheStonedTurtle <www.github.com/TheStonedTurtle>
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import thestonedturtle.lootlogger.data.UniqueItem;

@Getter
class UniqueItemPanel extends JPanel
{
	final JPanel panel = new JPanel();

	UniqueItemPanel(final Collection<UniqueItem> items, final ItemManager itemManager, final int itemMissingAlpha)
	{
		this.setLayout(new BorderLayout());
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.setBorder(new EmptyBorder(3, 0, 3, 0));
		this.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 10, 40));

		panel.setLayout(new GridBagLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		this.add(panel, BorderLayout.NORTH);

		updatePanel(items, itemManager, itemMissingAlpha);
	}

	public void updatePanel(final Collection<UniqueItem> items, final ItemManager itemManager, final int itemMissingAlpha)
	{
		panel.removeAll();
		float alphaMissing = itemMissingAlpha / 100f;

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 5;

		// Add each Unique Item icon to the panel
		for (final UniqueItem l : items)
		{
			final int quantity = l.getQty();
			final AsyncBufferedImage image = itemManager.getImage(l.getItemID(), quantity, quantity > 1);

			final float alpha = (quantity > 0 ? 1.0f : alphaMissing);
			final BufferedImage opaque = ImageUtil.alphaOffset(image, alpha);

			final JLabel icon = new JLabel();
			icon.setToolTipText(buildToolTip(l, quantity));
			icon.setIcon(new ImageIcon(opaque));
			icon.setVerticalAlignment(SwingConstants.CENTER);
			icon.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(icon, c);
			c.gridx++;

			// in case the image is blank we will refresh it upon load
			// Should only trigger if image hasn't been added
			image.onLoaded(() ->
			{
				icon.setIcon(new ImageIcon(ImageUtil.alphaOffset(image, alpha)));
				icon.revalidate();
				icon.repaint();
			});
		}
	}

	private static String buildToolTip(final UniqueItem item, final int qty)
	{
		String s = "<html>" + item.getName();
		if (qty > 0)
		{
			s += " x " + QuantityFormatter.formatNumber(qty);
		}
		if (item.getPrice() > 0)
		{
			s += "<br/>Price: " + QuantityFormatter.quantityToStackSize(item.getPrice());
			// Check for qty here as well as we should only show Total if the item has a value as well
			if (qty > 0)
			{
				s += "<br/>Total: " + QuantityFormatter.quantityToStackSize(qty * item.getPrice()) + "</html";
			}
		}
		s += "</html>";

		return s;
	}
}
