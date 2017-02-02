package uikit.autolayout.uiobjects;

import uikit.autolayout.Constrainable;
import uikit.autolayout.LayoutConstraint;
import uikit.autolayout.LayoutEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

public class ALJPanel extends JPanel implements Constrainable
{
	private final ArrayList<LayoutConstraint> _constraints = new ArrayList<>();

	public ALJPanel()
	{
		setLayout(null);
	}

	private int panelCompressionWidth = 750;
	private int panelCompressionHeight = 750;

	@Override
	public void addConstraint(LayoutConstraint constraint)
	{
		_constraints.add(constraint);
	}

	@Override
	public LayoutConstraint[] allConstraints()
	{
		LayoutConstraint[] constraintsToReturn = new LayoutConstraint[_constraints.size()];
		Object[] storedConstraints = _constraints.toArray();
		int count = 0;
		for (Object storedConstraint : storedConstraints)
		{
			constraintsToReturn[count] = (LayoutConstraint)storedConstraint;
			count++;
		}
		return constraintsToReturn;
	}

	@Override
	public void layoutSubviews()
	{
		LayoutEngine.current.processConstraintsIn(this);
		for (Component component : getComponents())
		{
			component.setBounds(0, component.getBounds().y, getWidth(), component.getPreferredSize().height);
			if (component instanceof ALJPanel)
			{
				((ALJPanel)component).layoutSubviews();
			}
			else if (component instanceof JPanel)
			{
				((ComponentListener)component).componentResized(null);
			}
		}
	}

	@Override
	public int compressionResistanceWidth()
	{
		return panelCompressionWidth;
	}

	@Override
	public int compressionResistanceHeight()
	{
		return panelCompressionHeight;
	}

	@Override
	public void setCompressionResistanceWidth(int compressionResistanceWidth)
	{
		panelCompressionWidth = compressionResistanceWidth;
	}

	@Override
	public void setCompressionResistanceHeight(int compressionResistanceHeight)
	{
		panelCompressionHeight = compressionResistanceHeight;
	}
}
