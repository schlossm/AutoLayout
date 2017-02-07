package uikit.autolayout;

import java.awt.*;

import static uikit.autolayout.LayoutEngine.getClassAndHashCode;

@SuppressWarnings({"unused", "WeakerAccess"})
public class LayoutConstraint
{
	public final Component viewOne;
	public final Component viewTwo;

	public final LayoutAttribute attributeOne;
	public final LayoutAttribute attributeTwo;

	public final LayoutRelation relation;
	public double multiplier;
	public int constant;

	public int priority = 1000;

	boolean hasBeenProcessed = false;

	@Override
	public String toString()
	{
		return getClassAndHashCode(viewOne) + "." + attributeOne.toString() + relation.toString() + (viewTwo != null ? getClassAndHashCode(viewTwo) : "null") + "." + attributeTwo.toString() + " * " + multiplier + " + " + constant;
	}

	public LayoutConstraint(Component view1, LayoutAttribute attr1, LayoutRelation relation, Component view2, LayoutAttribute attr2, double multiplier, int constant)
	{
		this.viewOne = view1;
		this.viewTwo = view2;
		this.relation = relation;
		this.attributeOne = attr1;
		this.attributeTwo = attr2;
		this.multiplier = multiplier;
		this.constant = constant;
	}
}
