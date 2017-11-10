/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout.uiobjects;

import autolayout.LayoutConstraint;
import autolayout.LayoutEngine;
import autolayout.LayoutEngineConstrainable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

/**
 * ALJPanel provides built-in AutoLayout support.  This class is fully subclassable.
 * <p>
 * When adding views, you call <code>addConstraints(_:)</code> to add a constraint for processing.
 * </p> <p>
 * Every view on screen needs a full set of layout constraints, or the layout engine will not be able to layout the view.  Some views, such as <code>JLabel</code> provide automatic sizes, and only require positional constraints.
 * </p>
 * <h1><b>Beta Notes:</b></h1>
 * <p>
 * Currently, the panel's compression resistance and content hugging are not honored.
 * </p> <p>
 * If you attempt to add <code>Components</code> that are not of type <code>JComponent</code>, the layout engine will not render them.
 * </p> <p> </p>
 */
public class ALJPanel extends JLayeredPane implements LayoutEngineConstrainable
{
	private final ArrayList<LayoutConstraint> constraints = new ArrayList<>();

	int horizontalContentCompressionResistancePriority = 250;
	int verticalContentCompressionResistancePriority = 250;

	int horizontalContentHuggingPriority = 750;
	int verticalContentHuggingPriority = 750;

	private int calculatedHeight = 0;
	private int calculatedWidth = 0;

	public ALJPanel()
	{
		setLayout(null);
		setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBackground(Color.white);
		setOpaque(true);
	}

	public final void addConstraint(LayoutConstraint constraint) { constraints.add(constraint); }

	public final void removeConstraint(LayoutConstraint constraint) { constraints.remove(constraint); }

	public final void removeAllConstraints() { constraints.clear(); }

	public final LayoutConstraint[] allConstraints() { return (LayoutConstraint[]) constraints.toArray(); }

	public final void removeConstraintsFor(JComponent component)
	{
		constraints.removeIf(layoutConstraint -> layoutConstraint.viewOne == component || layoutConstraint.viewTwo == component);
	}

	@Override
	public void remove(Component comp)
	{
		super.remove(comp);
		if (comp instanceof JComponent) { removeConstraintsFor((JComponent) comp); }
	}

	public void layoutSubviews()
	{
		LayoutEngine.active.processConstraintsIn(this);
		for (Component component : getComponents())
		{
			if (component instanceof ALJPanel) { ((ALJPanel) component).layoutSubviews(); }
			else if (component instanceof JPanel)
			{
				try { ((ComponentListener) component).componentResized(null); }
				catch (Exception ignored) { }
				component.revalidate();
			}
			component.repaint();
		}
		repaint();
	}

	public final void setCalculatedHeight(int calculatedHeight) { this.calculatedHeight = calculatedHeight; }

	public final void setCalculatedWidth(int calculatedWidth) { this.calculatedWidth = calculatedWidth; }

	public final int calculatedHeight() { return calculatedHeight; }

	public final int calculatedWidth() { return calculatedWidth; }
}
