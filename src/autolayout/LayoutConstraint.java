/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout;

import java.awt.*;

/**
 * A constraint defines a relationship between two user interface objects that must be satisfied by the constraint-based layout system. Each constraint is a linear equation with the following format:
 * <p>
 * <tab></tab><code>item1.attribute1 = multiplier × item2.attribute2 + constant</code>
 * <p>
 * In this equation, attribute1 and attribute2 are the variables that Auto Layout can adjust when solving these constraints. The other values are defined when you create the constraint. For example, If you’re defining the relative position of two buttons, you might say “the leading edge of the second button should be 8 points after the trailing edge of the first button.” The linear equation for this relationship is shown below:
 * <p>
 * <code>
 * // positive values move to the right in left-to-right languages like English.
 * <p>
 * button2.leading = 1.0 × button1.trailing + 8.0
 * </code>
 * <p>
 * Auto Layout then modifies the values of the specified leading and trailing edges until both sides of the equation are equal. Note that Auto Layout does not simply assign the value of the right side of this equation to the left side. Instead, the system can modify either attribute or both attributes as needed to solve for this constraint.
 * <p>
 * The fact that constraints are equations (and not assignment operators) means that you can switch the order of the items in the equation as needed to more clearly express the desired relationship. However, if you switch the order, you must also invert the multiplier and constant. For example, the following two equations produce identical constraints:
 * <p>
 * <code>
 * // These equations produce identical constraints
 * <p>
 * button2.leading = 1.0 × button1.trailing + 8.0
 * <p>
 * button1.trailing = 1.0 × button2.leading - 8.0
 * </code>
 * <p>
 * A valid layout is defined as a set constraints with one and only one possible solution. Valid layouts are also referred to as a nonambiguous, nonconflicting layouts. Constraints with more than one solution are ambiguous. Constraints with no valid solutions are conflicting.
 * <p>
 * Additionally, constraints are not limited to equality relationships. They can also use greater than or equal to (>=) or less than or equal to (<=) to describe the relationship between the two attributes.
 * <p>
 * This combination of inequalities and equalities gives you a great amount of flexibility and power. By combining multiple constraints, you can define layouts that dynamically adapt as the size and location of the elements in your user interface change.
 */
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

	@Override
	public String toString()
	{
		return LayoutEngine.getClassAndHashCode(viewOne) + "." + attributeOne.toString() + relation.toString() + (viewTwo != null ? LayoutEngine.getClassAndHashCode(viewTwo) : "null") + "." + attributeTwo.toString() + " * " + multiplier + " + " + constant;
	}
}
