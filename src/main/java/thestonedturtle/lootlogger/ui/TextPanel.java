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
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;

@Getter
class TextPanel extends JPanel
{
	private static final Color PANEL_BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final Border PANEL_BORDER = BorderFactory.createMatteBorder(8, 15, 8, 0, PANEL_BACKGROUND_COLOR);

	private final JLabel textLabel = new JLabel("", SwingConstants.LEFT);
	private final JLabel valueLabel = new JLabel("", SwingConstants.LEFT);

	TextPanel()
	{
		this.setLayout(new BorderLayout());
		this.setBackground(PANEL_BACKGROUND_COLOR);
		this.setBorder(PANEL_BORDER);

		textLabel.setForeground(Color.WHITE);
		valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		this.add(textLabel, BorderLayout.LINE_START);
		this.add(valueLabel, BorderLayout.CENTER);
	}

	// Long value should be for Total Value
	public void updatePanel(final String text, final long totalValue)
	{
		textLabel.setText(text);

		// Item Values (Colored off Total Value of item)
		valueLabel.setText(QuantityFormatter.quantityToStackSize(totalValue) + " gp");
		valueLabel.setForeground(getRSValueColor(totalValue));

		this.setToolTipText(QuantityFormatter.formatNumber(totalValue));
	}

	public void updatePanel(final String text, final int value)
	{
		textLabel.setText(text);
		valueLabel.setText(QuantityFormatter.formatNumber(value));
	}

	private static Color getRSValueColor(long val)
	{
		return (val >= 10000000) ? Color.GREEN : (val >= 100000) ? Color.WHITE : Color.YELLOW;
	}
}
