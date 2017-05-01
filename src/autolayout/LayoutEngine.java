package autolayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.Integer.max;

/**
 * The Auto Layout Engine for processing constraints in a view.
 * <p>
 * <b>This engine is only compatible with Swing.</b>
 * <p>
 * <u>This engine is currently in <b>beta</b>.</u>
 * <p>
 * The engine processes all constraints in order they were inserted into the view.  Currently, the engine places an equal priority on all constraints, and will warn if the constraints produce an illegal layout.
 * <p>
 * The engine respects grater than or equal(>=) and less than or equal(<=) constraint relations.
 * <p>
 * When processing constraints, <code>LayoutEngine</code> will process constraints in order or relation to other views.  Views with constraints that do not have reliance on other views will be processed first, then the rest of the layout will be built.
 * <p>
 * The engine processes all constraints on relayout, as it does not yet know how the view was changed.
 * <p>
 * <code>LayoutEngine</code> will attempt to warn if illegal constraints are found, or if a view is not positionable.  In some cases, layouts will cause a system crash, mostly due to multiple of the same attribute or if the view attempting to be positioned is not a JComponent.
 */
@SuppressWarnings("ConstantConditions")
public class LayoutEngine
{
	public static final LayoutEngine current = new LayoutEngine();

	private LayoutEngine() { }

	public static String getClassAndHashCode(Object object)
	{
		String[] classes = object.getClass().toString().substring(6).split(Pattern.quote("."));
		String returnString = classes[max(0, classes.length - 1)] + ":" + System.identityHashCode(object);
		if (object instanceof JLabel)
		{
			returnString += ":" + ((JLabel) object).getText();
		}
		return returnString;
	}

	public void processConstraintsIn(Constrainable view)
	{
		JComponent component;
		try
		{
			component = ((JComponent) view);
		}
		catch (ClassCastException e)
		{
			System.out.println("The constrainable view must be a descendant of JComponent");
			e.printStackTrace();
			System.exit(0);
			return;
		}

		final LayoutConstraint[] allConstraints = view.allConstraints().clone();
		Map<Component, ArrayList<LayoutConstraint>> map = new LinkedHashMap<>();

		//Build Map
		for (LayoutConstraint constraint : allConstraints)
		{
			ArrayList<LayoutConstraint> constraintsForView = map.get(constraint.viewOne);
			if (constraintsForView == null)
			{
				constraintsForView = new ArrayList<>();
			}
			constraintsForView.add(constraint);
			map.put(constraint.viewOne, constraintsForView);
		}

		//Check for any views with no constraints
		for (Component subComp : component.getComponents())
		{
			if (map.get(subComp) == null)
			{
				System.out.println(getClassAndHashCode(subComp) + " does not have any constraints.  It will not be displayed on screen");
				component.remove(subComp);
			}
		}

		process(map, component);
	}

	private void process(Map<Component, ArrayList<LayoutConstraint>> map, Component parent)
	{
		int height = 0;
		int width = 0;

		while (map.size() != 0)
		{
			Component viewToConstrain = map.keySet().iterator().next();
			ArrayList<LayoutConstraint> constraints = map.get(viewToConstrain);
			sort(constraints, viewToConstrain);

			constrainPreferredSizeIfNeeded(constraints, viewToConstrain);

			ArrayList<LayoutAttribute[]> attributesSatisfied = new ArrayList<>();

			while (!constraints.isEmpty())
			{
				LayoutConstraint constraint = constraints.iterator().next();
				//MARK: - Perform routine checks to make sure an illegal constraint isn't going to be created
				if (constraint.attributeOne != LayoutAttribute.width && constraint.attributeOne != LayoutAttribute.height)
				{
					if (constraint.multiplier == 0 || constraint.viewTwo == null)
					{
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  A multiplier of 0 or a null second item together with a location for the first attribute creates an illegal constraint of a location equal to a constant. Location attributes must be specified in pairs.");
						continue;
					}
				}

				if (constraint.viewTwo == parent)
				{
					processConstraintOnParent(constraint, attributesSatisfied, parent);
				}
				else if (constraint.viewTwo == null)
				{
					processConstraintOnNullSecondView(constraint, attributesSatisfied);
				}
				else
				{
					processConstraint(constraint, attributesSatisfied);
				}

				if (constraint.hasBeenProcessed)
				{
					if (constraint.viewOne.getY() + constraint.viewOne.getHeight() > height)
					{
						height = constraint.viewOne.getY() + constraint.viewOne.getHeight();
					}
					if (constraint.viewOne.getX() + constraint.viewOne.getWidth() > width)
					{
						width = constraint.viewOne.getX() + constraint.viewOne.getWidth();
					}
					constraints.remove(constraint);
				}
			}
			map.remove(viewToConstrain);
		}

		((Constrainable) parent).setCalculatedHeight(height);
		((Constrainable) parent).setCalculatedWidth(width);
	}

	private void processConstraintOnParent(LayoutConstraint constraint, ArrayList<LayoutAttribute[]> attributesSatisfied, Component parent)
	{
		constraint.hasBeenProcessed = true;
		switch (constraint.attributeOne)
		{
			case leading:
			{
				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int leading = 0;
				switch (constraint.attributeTwo)
				{
					case leading:
						leading = constraint.constant;
						break;

					case centerX:
						leading = (int) (parent.getBounds().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						leading = (int) (parent.getBounds().getWidth() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .leading can only be constrained to .leading, .centerX, or .trailing");
						break;
				}
				constraint.viewOne.setBounds(leading, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.leading, constraint.attributeTwo});
				break;
			}

			case trailing:
			{
				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int trailing = 0;
				switch (constraint.attributeTwo)
				{
					case leading:
						trailing = constraint.constant;
						break;

					case centerX:
						trailing = (int) (parent.getBounds().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						trailing = (int) (parent.getBounds().getWidth() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .trailing can only be constrained to .leading, .centerX, or .trailing");
						break;
				}

				if (trailing - constraint.viewOne.getBounds().x < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				if (!contains(attributesSatisfied, LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(trailing - constraint.viewOne.getWidth(), constraint.viewOne.getBounds().y, constraint.viewOne.getWidth(), constraint.viewOne.getBounds().height);
				}
				else
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, trailing - constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.trailing, constraint.attributeTwo});
				break;
			}

			case top:
			{
				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int top = 0;
				switch (constraint.attributeTwo)
				{
					case top:
					{
						top = constraint.constant;
						break;
					}

					case centerY:
					{
						top = (int) (parent.getBounds().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;
					}

					case bottom:
					{
						top = (int) (parent.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						break;
					}

					default:
					{
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .top can only be constrained to .top, .centerY, or .bottom");
						break;
					}
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, top, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.top, constraint.attributeTwo});
				break;
			}

			case bottom:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.top) || contains(attributesSatisfied, LayoutAttribute.centerY)) && contains(attributesSatisfied, LayoutAttribute.height))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.top or .centerY) and .height.  Setting .bottom would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int bottom = 0;
				switch (constraint.attributeTwo)
				{
					case top:
						bottom = constraint.constant;
						break;

					case centerY:
						bottom = (int) (parent.getBounds().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case bottom:
						bottom = (int) (parent.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .bottom can only be constrained to .top, .centerY, or .bottom");
						break;
				}

				if (bottom - constraint.viewOne.getBounds().y < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The height is negative.");
					break;
				}

				if (!contains(attributesSatisfied, LayoutAttribute.top))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, bottom - constraint.viewOne.getHeight(), constraint.viewOne.getWidth(), constraint.viewOne.getBounds().height);
				}
				else
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, bottom - constraint.viewOne.getBounds().y);
				}

				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.bottom, constraint.attributeTwo});
				break;
			}

			case centerX:
			{
				if (contains(attributesSatisfied, LayoutAttribute.trailing) && contains(attributesSatisfied, LayoutAttribute.leading))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified .leading and .trailing.  Setting .centerX would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int centerX = 0;
				switch (constraint.attributeTwo)
				{
					case leading:
						centerX = constraint.constant;
						break;

					case centerX:
						centerX = (int) (parent.getBounds().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						centerX = (int) (parent.getBounds().getWidth() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .centerX can only be constrained to .leading, .centerX, or .trailing");
						break;
				}

				if (centerX < 0 || (centerX - constraint.viewOne.getBounds().x) * 2 < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				if (contains(attributesSatisfied, LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, (centerX - constraint.viewOne.getBounds().x) * 2, constraint.viewOne.getBounds().height);
				}
				else
				{
					constraint.viewOne.setBounds(centerX - (constraint.viewOne.getBounds().width / 2), constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.centerX, constraint.attributeTwo});
				break;
			}

			case centerY:
			{
				if (((contains(attributesSatisfied, LayoutAttribute.top) || contains(attributesSatisfied, LayoutAttribute.bottom)) && contains(attributesSatisfied, LayoutAttribute.height)) || (contains(attributesSatisfied, LayoutAttribute.top) && contains(attributesSatisfied, LayoutAttribute.bottom)))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified ((.top or .bottom) and .height) or (.top and .bottom).  Setting .centerY would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int centerY = 0;
				switch (constraint.attributeTwo)
				{
					case top:
						centerY = constraint.constant;
						break;

					case centerY:
						centerY = (int) (parent.getBounds().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case bottom:
						centerY = (int) (parent.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .centerY can only be constrained to .top, .centerY, or .bottom");
						break;
				}

				if (centerY < 0 || (centerY - constraint.viewOne.getBounds().y) * 2 < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				if (contains(attributesSatisfied, LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, (centerY - constraint.viewOne.getBounds().y) * 2);
				}
				else
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, centerY - (constraint.viewOne.getBounds().height / 2), constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.centerX, constraint.attributeTwo});
				break;
			}

			case width:
			{
				if (contains(attributesSatisfied, LayoutAttribute.leading) && contains(attributesSatisfied, LayoutAttribute.trailing))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified .leading and .trailing.  Setting .width would break these constraints");
					break;
				}

				int width = 0;
				switch (constraint.attributeTwo)
				{
					case width:
						width = (int) (parent.getBounds().getWidth() * constraint.multiplier) + constraint.constant;

						if (constraint.relation == LayoutRelation.greaterThanOrEqual)
						{
							width = max(width, constraint.viewOne.getPreferredSize().width);
						}
						else if (constraint.relation == LayoutRelation.lessThanOrEqual)
						{
							width = Integer.min(width, constraint.viewOne.getPreferredSize().width);
						}
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .width can only be constrained to .width");
						break;
				}

				if (width < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.width, constraint.attributeTwo});
				break;
			}

			case height:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.top) || contains(attributesSatisfied, LayoutAttribute.centerY)) && contains(attributesSatisfied, LayoutAttribute.bottom))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.top or .centerY) and .bottom.  Setting .height would break the constraints");
					break;
				}

				int height = 0;
				switch (constraint.attributeTwo)
				{
					case height:
						height = (int) (parent.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						if (constraint.relation == LayoutRelation.greaterThanOrEqual)
						{
							height = max(height, constraint.viewOne.getPreferredSize().height);
						}
						else if (constraint.relation == LayoutRelation.lessThanOrEqual)
						{
							height = Integer.min(height, constraint.viewOne.getPreferredSize().height);
						}
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .height can only be constrained to .height");
						break;
				}

				if (height < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The height is negative.");
					break;
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.height, constraint.attributeTwo});
				break;
			}
		}
	}

	private void processConstraint(LayoutConstraint constraint, ArrayList<LayoutAttribute[]> attributesSatisfied)
	{
		constraint.hasBeenProcessed = true;
		switch (constraint.attributeOne)
		{
			case top:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.bottom) || contains(attributesSatisfied, LayoutAttribute.centerY)) && contains(attributesSatisfied, LayoutAttribute.height))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.bottom or .centerY) and .height.  Setting .top would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int top = 0;
				switch (constraint.attributeTwo)
				{
					case top:
					{
						top = constraint.constant;
						break;
					}

					case centerY:
					{
						top = constraint.viewTwo.getBounds().y + (int) (constraint.viewTwo.getBounds().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;
					}

					case bottom:
					{
						top = constraint.viewTwo.getBounds().y + (int) (constraint.viewTwo.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						break;
					}

					default:
					{
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .top can only be constrained to .top, .centerY, or .bottom");
						break;
					}
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, top, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.top, constraint.attributeTwo});
				break;
			}

			case bottom:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.top) || contains(attributesSatisfied, LayoutAttribute.centerX)) && contains(attributesSatisfied, LayoutAttribute.height))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.top or .centerY) and .height.  Setting .bottom would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int bottom = 0;
				switch (constraint.attributeTwo)
				{
					case top:
						bottom = constraint.constant;
						break;

					case centerY:
						bottom = constraint.viewTwo.getBounds().y + (int) (constraint.viewTwo.getBounds().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case bottom:
						bottom = constraint.viewTwo.getBounds().y + (int) (constraint.viewTwo.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .bottom can only be constrained to .top, .centerY, or .bottom");
						break;
				}

				if (bottom - constraint.viewOne.getBounds().y < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The height is negative.");
					break;
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, bottom - constraint.viewOne.getBounds().y);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.bottom, constraint.attributeTwo});
				break;
			}

			case leading:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.trailing) || contains(attributesSatisfied, LayoutAttribute.centerX)) && contains(attributesSatisfied, LayoutAttribute.width))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.trailing or .centerX) and .width.  Setting .leading would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int leading = 0;
				switch (constraint.attributeTwo)
				{
					case leading:
						leading = constraint.constant;
						break;

					case centerX:
						leading = constraint.viewTwo.getBounds().x + (int) (constraint.viewTwo.getBounds().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						leading = constraint.viewTwo.getBounds().x + (int) (constraint.viewTwo.getBounds().getWidth() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .leading can only be constrained to .leading, .centerX, or .trailing");
						break;
				}

				constraint.viewOne.setBounds(leading, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.leading, constraint.attributeTwo});
				break;
			}

			case trailing:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.leading) || contains(attributesSatisfied, LayoutAttribute.centerX)) && contains(attributesSatisfied, LayoutAttribute.width))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.leading or .centerX) and .width.  Setting .trailing would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int trailing = 0;
				switch (constraint.attributeTwo)
				{
					case leading:
						trailing = (int) (constraint.viewTwo.getBounds().getX() * constraint.multiplier) + constraint.constant;
						break;

					case centerX:
						trailing = constraint.viewTwo.getBounds().x + (int) (constraint.viewTwo.getBounds().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						trailing = constraint.viewTwo.getBounds().x + (int) (constraint.viewTwo.getBounds().getWidth() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .trailing can only be constrained to .leading, .centerX, or .trailing");
						break;
				}

				if (trailing - constraint.viewOne.getBounds().x < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, trailing - constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.trailing, constraint.attributeTwo});
				break;
			}

			case width:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.leading) || contains(attributesSatisfied, LayoutAttribute.centerX)) && contains(attributesSatisfied, LayoutAttribute.trailing))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.leading or .centerX) and .trailing.  Setting .width would break the constraints");
					break;
				}

				int width = 0;
				switch (constraint.attributeTwo)
				{
					case width:
						width = constraint.viewTwo.getBounds().x + (int) (constraint.viewTwo.getBounds().getWidth() * constraint.multiplier) + constraint.constant;

						if (constraint.relation == LayoutRelation.greaterThanOrEqual)
						{
							width = max(width, constraint.viewOne.getPreferredSize().width);
						}
						else if (constraint.relation == LayoutRelation.lessThanOrEqual)
						{
							width = Integer.min(width, constraint.viewOne.getPreferredSize().width);
						}
						break;

					case height:
						if (constraint.viewOne == constraint.viewTwo)
						{
							if (contains(attributesSatisfied, LayoutAttribute.height) || (contains(attributesSatisfied, LayoutAttribute.top) && contains(attributesSatisfied, LayoutAttribute.bottom)))
							{
								width = constraint.viewOne.getHeight();
							}
							else
							{
								//Needs another chance to get the height made up
								constraint.hasBeenProcessed = false;
							}
						}
						else
						{
							width = constraint.viewTwo.getHeight();
						}
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .width can only be constrained to .width or .height");
						break;
				}

				if (width < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				if (contains(attributesSatisfied, LayoutAttribute.trailing))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x - width, constraint.viewOne.getBounds().y, width, constraint.viewOne.getBounds().height);
				}
				else
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.width, constraint.attributeTwo});
				break;
			}

			case height:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.top) || contains(attributesSatisfied, LayoutAttribute.centerY)) && contains(attributesSatisfied, LayoutAttribute.bottom))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.top or .centerY) and .bottom.  Setting .height would break the constraints");
					break;
				}

				int height = 0;
				switch (constraint.attributeTwo)
				{
					case height:
						height = constraint.viewTwo.getBounds().y + (int) (constraint.viewTwo.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						if (constraint.relation == LayoutRelation.greaterThanOrEqual)
						{
							height = max(height, constraint.viewOne.getPreferredSize().height);
						}
						else if (constraint.relation == LayoutRelation.lessThanOrEqual)
						{
							height = Integer.min(height, constraint.viewOne.getPreferredSize().height);
						}
						break;

					case width:
					{
						if (constraint.viewOne == constraint.viewTwo)
						{
							if (contains(attributesSatisfied, LayoutAttribute.width) || (contains(attributesSatisfied, LayoutAttribute.leading) && contains(attributesSatisfied, LayoutAttribute.trailing)))
							{
								height = constraint.viewOne.getWidth();
							}
							else
							{
								//Needs another chance to get the width made up
								constraint.hasBeenProcessed = false;
							}
						}
						else
						{
							height = constraint.viewTwo.getHeight();
						}
					}

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .height can only be constrained to .height");
						break;
				}

				if (height < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The height is negative.");
					break;
				}

				if (contains(attributesSatisfied, LayoutAttribute.bottom))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y - height, constraint.viewOne.getBounds().width, height);
				}
				else
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.height, constraint.attributeTwo});
				break;
			}

			case centerX:
			{
				if (((contains(attributesSatisfied, LayoutAttribute.trailing) || contains(attributesSatisfied, LayoutAttribute.leading)) && contains(attributesSatisfied, LayoutAttribute.width)) || (contains(attributesSatisfied, LayoutAttribute.trailing) && contains(attributesSatisfied, LayoutAttribute.leading)))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified ((.leading or .trailing) and .width) or (.leading and .trailing).  Setting .centerX would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int centerX = 0;
				switch (constraint.attributeTwo)
				{
					case leading:
						centerX = constraint.constant;
						break;

					case centerX:
						centerX = constraint.viewTwo.getBounds().x + (int) (constraint.viewTwo.getBounds().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						centerX = constraint.viewTwo.getBounds().x + (int) (constraint.viewTwo.getBounds().getWidth() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .centerX can only be constrained to .leading, .centerX, or .trailing");
						break;
				}

				if (centerX < 0 || (centerX - constraint.viewOne.getBounds().x) * 2 < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				if (contains(attributesSatisfied, LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, (centerX - constraint.viewOne.getBounds().x) * 2, constraint.viewOne.getBounds().height);
				}
				else if (contains(attributesSatisfied, LayoutAttribute.width))
				{
					constraint.viewOne.setBounds(centerX - (constraint.viewOne.getBounds().width / 2), constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.centerX, constraint.attributeTwo});
				break;
			}

			case centerY:
			{
				if (((contains(attributesSatisfied, LayoutAttribute.top) || contains(attributesSatisfied, LayoutAttribute.bottom)) && contains(attributesSatisfied, LayoutAttribute.height)) || (contains(attributesSatisfied, LayoutAttribute.top) && contains(attributesSatisfied, LayoutAttribute.bottom)))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified ((.top or .bottom) and .height) or (.top and .bottom).  Setting .centerY would break the constraints");
					break;
				}

				if (constraint.relation != LayoutRelation.equal)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You must set the relation to .equal");
					break;
				}

				int centerY = 0;
				switch (constraint.attributeTwo)
				{
					case top:
						centerY = constraint.constant;
						break;

					case centerY:
						centerY = constraint.viewTwo.getBounds().y + (int) (constraint.viewTwo.getBounds().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case bottom:
						centerY = constraint.viewTwo.getBounds().y + (int) (constraint.viewTwo.getBounds().getHeight() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .centerY can only be constrained to .top, .centerY, or .bottom");
						break;
				}

				if (centerY < 0 || (centerY - constraint.viewOne.getBounds().y) * 2 < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				if (contains(attributesSatisfied, LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, (centerY - constraint.viewOne.getBounds().y) * 2);
				}
				else if (contains(attributesSatisfied, LayoutAttribute.width))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, centerY - (constraint.viewOne.getBounds().height / 2), constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.centerX, constraint.attributeTwo});
				break;
			}
		}
	}

	private void processConstraintOnNullSecondView(LayoutConstraint constraint, ArrayList<LayoutAttribute[]> attributesSatisfied)
	{
		constraint.hasBeenProcessed = true;
		switch (constraint.attributeOne)
		{
			case width:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.leading) || contains(attributesSatisfied, LayoutAttribute.centerX)) && contains(attributesSatisfied, LayoutAttribute.trailing))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.leading or .centerX) and .trailing.  Setting .width would break the constraints");
					break;
				}

				int width = constraint.constant;

				if (constraint.relation == LayoutRelation.greaterThanOrEqual)
				{
					width = max(width, constraint.viewOne.getPreferredSize().width);
				}
				else if (constraint.relation == LayoutRelation.lessThanOrEqual)
				{
					width = Integer.min(width, constraint.viewOne.getPreferredSize().width);
				}

				if (width < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The width is negative.");
					break;
				}

				if (contains(attributesSatisfied, LayoutAttribute.trailing))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x - width, constraint.viewOne.getBounds().y, width, constraint.viewOne.getBounds().height);
				}
				else
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.width, constraint.attributeTwo});
				break;
			}

			case height:
			{
				if ((contains(attributesSatisfied, LayoutAttribute.top) || contains(attributesSatisfied, LayoutAttribute.centerY)) && contains(attributesSatisfied, LayoutAttribute.bottom))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.top or .centerY) and .bottom.  Setting .height would break the constraints");
					break;
				}
				int height = constraint.constant;
				if (constraint.relation == LayoutRelation.greaterThanOrEqual)
				{
					height = max(height, constraint.viewOne.getPreferredSize().height);
				}
				else if (constraint.relation == LayoutRelation.lessThanOrEqual)
				{
					height = Integer.min(height, constraint.viewOne.getPreferredSize().height);
				}

				if (height < 0)
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  The height is negative.");
					break;
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, height);
				attributesSatisfied.add(new LayoutAttribute[]{LayoutAttribute.height, constraint.attributeTwo});
				break;
			}

			default:
			{
				System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  A multiplier of 0 or a nil second item together with a location for the first attribute creates an illegal constraint of a location equal to a constant. Location attributes must be specified in pairs.");
				break;
			}
		}
	}

	private void sort(ArrayList<LayoutConstraint> constraints, Component viewToConstrain)
	{
		constraints.sort((o1, o2) ->
		                 {
			                 //Leading/Trailing
			                 if (o1.attributeOne == LayoutAttribute.leading)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.leading)
				                 {
					                 System.out.println("There are multiple constraints for .leading for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 return -1;
			                 }
			                 else if (o1.attributeOne == LayoutAttribute.trailing)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.trailing)
				                 {
					                 System.out.println("There are multiple constraints for .trailing for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.leading)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 //Top/Bottom
			                 else if (o1.attributeOne == LayoutAttribute.top)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.top)
				                 {
					                 System.out.println("There are multiple constraints for .top for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.leading || o2.attributeOne == LayoutAttribute.trailing)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 else if (o1.attributeOne == LayoutAttribute.bottom)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.bottom)
				                 {
					                 System.out.println("There are multiple constraints for .bottom for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.leading || o2.attributeOne == LayoutAttribute.trailing || o2.attributeOne == LayoutAttribute.top)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 //Width/Height
			                 else if (o1.attributeOne == LayoutAttribute.width)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.width)
				                 {
					                 System.out.println("There are multiple constraints for .width for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.leading || o2.attributeOne == LayoutAttribute.trailing || o2.attributeOne == LayoutAttribute.top || o2.attributeOne == LayoutAttribute.bottom)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 else if (o1.attributeOne == LayoutAttribute.height)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.height)
				                 {
					                 System.out.println("There are multiple constraints for .height for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.leading || o2.attributeOne == LayoutAttribute.trailing || o2.attributeOne == LayoutAttribute.top || o2.attributeOne == LayoutAttribute.bottom || o2.attributeOne == LayoutAttribute.width)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 //Centers
			                 else if (o1.attributeOne == LayoutAttribute.centerX)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.centerX)
				                 {
					                 System.out.println("There are multiple constraints for .centerX for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.leading || o2.attributeOne == LayoutAttribute.trailing || o2.attributeOne == LayoutAttribute.top || o2.attributeOne == LayoutAttribute.bottom || o2.attributeOne == LayoutAttribute.width || o2.attributeOne == LayoutAttribute.height)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 else if (o1.attributeOne == LayoutAttribute.centerY)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.centerY)
				                 {
					                 System.out.println("There are multiple constraints for .centerY for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.leading || o2.attributeOne == LayoutAttribute.trailing || o2.attributeOne == LayoutAttribute.top || o2.attributeOne == LayoutAttribute.bottom || o2.attributeOne == LayoutAttribute.centerX || o2.attributeOne == LayoutAttribute.width || o2.attributeOne == LayoutAttribute.height)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 else
			                 {
				                 System.out.println("Unsupported Constraint: " + o1.toString());
				                 System.exit(-1);
				                 return 0;
			                 }
		                 });
	}

	private void constrainPreferredSizeIfNeeded(ArrayList<LayoutConstraint> constraints, Component viewToConstrain)
	{
		ArrayList<LayoutAttribute> foundAttributes = new ArrayList<>();
		for (LayoutConstraint constraint : constraints)
		{
			if (constraint.attributeOne == LayoutAttribute.width)
			{
				foundAttributes.add(LayoutAttribute.width);
			}
			else if (constraint.attributeOne == LayoutAttribute.height)
			{
				foundAttributes.add(LayoutAttribute.height);
			}
			else if (constraint.attributeOne == LayoutAttribute.leading)
			{
				foundAttributes.add(LayoutAttribute.leading);
				if (foundAttributes.contains(LayoutAttribute.trailing))
				{
					foundAttributes.add(LayoutAttribute.width);
				}
			}
			else if (constraint.attributeOne == LayoutAttribute.trailing)
			{
				foundAttributes.add(LayoutAttribute.trailing);
				if (foundAttributes.contains(LayoutAttribute.leading))
				{
					foundAttributes.add(LayoutAttribute.width);
				}
			}
			else if (constraint.attributeOne == LayoutAttribute.top)
			{
				foundAttributes.add(LayoutAttribute.top);
				if (foundAttributes.contains(LayoutAttribute.bottom))
				{
					foundAttributes.add(LayoutAttribute.height);
				}
			}
			else if (constraint.attributeOne == LayoutAttribute.bottom)
			{
				foundAttributes.add(LayoutAttribute.bottom);
				if (foundAttributes.contains(LayoutAttribute.top))
				{
					foundAttributes.add(LayoutAttribute.height);
				}
			}
		}

		if (!foundAttributes.contains(LayoutAttribute.width))
		{
			viewToConstrain.setBounds(viewToConstrain.getBounds().x, viewToConstrain.getBounds().y, viewToConstrain.getPreferredSize().width, viewToConstrain.getBounds().height);
		}
		if (!foundAttributes.contains(LayoutAttribute.height))
		{
			viewToConstrain.setBounds(viewToConstrain.getBounds().x, viewToConstrain.getBounds().y, viewToConstrain.getBounds().width, viewToConstrain.getPreferredSize().height);
		}
	}

	private boolean contains(ArrayList<LayoutAttribute[]> attributes, LayoutAttribute attribute)
	{
		for (LayoutAttribute[] pair : attributes)
		{
			if (pair[0] == attribute) { return true; }
		}
		return false;
	}
}