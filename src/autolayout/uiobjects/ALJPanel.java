package autolayout.uiobjects;

import autolayout.Constrainable;
import autolayout.LayoutConstraint;
import autolayout.LayoutEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

/**
 * ALJPanel is a subclass of JLayeredPane, and provides automatic inheritance of AutoLayout support.  This class is subclassable, but can be used as is.
 * <p>
 * When adding views, you call <code>addConstraints(_:)</code> to add a constraint for processing.
 * <p>
 * Every view on screen needs a full set of layout constraints, or <code>LayoutEngine</code> will not be able to layout the view.  Some views, such as JLabel provide automatic preferredSizes, and do not need explicit constraints for specifying width and height, but these constraints can be added without issue.
 * <p>
 * If you attempt to add <code>Components</code> that are not of type <code>JComponent</code>, <code>LayoutEngine</code> will crash as these are not Swing compatible views.
 * <p>
 * <b>Beta Notes:</b>
 * <p>
 * Currently, the panel's compression resistance is not honored as each constraint is given full priority.  The calculated height and width are managed however, and can be used to determine width and height after a layout pass.
 */
@SuppressWarnings({"unchecked", "unused"})
public class ALJPanel extends JLayeredPane implements Constrainable
{
	private final ArrayList<LayoutConstraint> _constraints = new ArrayList<>();
	private int panelCompressionWidth = 750;
	private int panelCompressionHeight = 750;
	private int calculatedHeight = 0;
	private int calculatedWidth = 0;

	public ALJPanel()
	{
		setLayout(null);
		setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBackground(Color.white);
		setOpaque(true);
	}

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
			constraintsToReturn[count] = (LayoutConstraint) storedConstraint;
			count++;
		}
		return constraintsToReturn;
	}

	public void removeAllConstraints()
	{
		_constraints.removeIf(layoutConstraint -> true);
	}

	public void removeConstraintsFor(JComponent component)
	{
		for (LayoutConstraint constraint : (ArrayList<LayoutConstraint>) _constraints.clone())
		{
			if (constraint.viewOne == component)
			{
				_constraints.remove(constraint);
			}
		}
	}

	public void removeConstraint(LayoutConstraint constraint)
	{
		_constraints.remove(constraint);
	}

	@Override
	public void remove(Component comp)
	{
		super.remove(comp);

		for (LayoutConstraint constraint : (ArrayList<LayoutConstraint>) _constraints.clone())
		{
			if (constraint.viewOne == comp || constraint.viewTwo == comp)
			{
				_constraints.remove(constraint);
			}
		}
	}

	@Override
	public void removeAll()
	{
		super.removeAll();
		_constraints.removeAll((ArrayList<LayoutConstraint>) _constraints.clone());
	}

	@Override
	public void layoutSubviews()
	{
		LayoutEngine.current.processConstraintsIn(this);
		for (Component component : getComponents())
		{
			if (component instanceof ALJPanel)
			{
				((ALJPanel) component).layoutSubviews();
			}
			else if (component instanceof JPanel)
			{
				try
				{
					((ComponentListener) component).componentResized(null);
				}
				catch (Exception ignored) { }
				component.revalidate();

			}
			component.repaint();
		}
		repaint();
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

	@Override
	public void setCalculatedHeight(int calculatedHeight)
	{
		this.calculatedHeight = calculatedHeight;
	}

	@Override
	public void setCalculatedWidth(int calculatedWidth)
	{
		this.calculatedWidth = calculatedWidth;
	}

	@Override
	public int calculatedHeight()
	{
		return calculatedHeight;
	}

	@Override
	public int calculatedWidth()
	{
		return calculatedWidth;
	}
}
