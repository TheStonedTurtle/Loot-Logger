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
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.QuantityFormatter;

/**
 * Name above the loot grid that roughly matches the RuneLite Loot Tracker LootTrackerBox `logTitle` element
 */
public class LootGridName extends JPanel
{
	private static final int TITLE_PADDING = 5;
	static final Border VISIBLE_BORDER = new EmptyBorder(7, 7, 7, 7);
	static final Border COLLAPSED_BORDER = new CompoundBorder(
		new MatteBorder(0, 0, 5, 0, ColorScheme.DARK_GRAY_COLOR),
		VISIBLE_BORDER
	);

	LootGridName(final String name, final int count, final long price) {

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		this.setBorder(VISIBLE_BORDER);

		final JLabel titleLabel = new JLabel();
		titleLabel.setText(name + " x " + count);
		titleLabel.setFont(FontManager.getRunescapeSmallFont());
		titleLabel.setForeground(Color.WHITE);
		// Set a size to make BoxLayout truncate the name
		titleLabel.setMinimumSize(new Dimension(1, titleLabel.getPreferredSize().height));
		this.add(titleLabel);

		this.add(Box.createHorizontalGlue());
		this.add(Box.createRigidArea(new Dimension(TITLE_PADDING, 0)));

		final JLabel priceLabel = new JLabel();
		priceLabel.setFont(FontManager.getRunescapeSmallFont());
		priceLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		priceLabel.setText(QuantityFormatter.quantityToStackSize(price) + " gp");
		priceLabel.setToolTipText(QuantityFormatter.formatNumber(price) + " gp");
		this.add(priceLabel);
	}
}
