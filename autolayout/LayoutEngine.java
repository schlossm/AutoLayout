package uikit.autolayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.Integer.max;

@SuppressWarnings("ConstantConditions")
public class LayoutEngine
{
	public static final LayoutEngine current = new LayoutEngine();
	private LayoutEngine() { }

	public void processConstraintsIn(Constrainable view)
	{
		if (view instanceof Container)
		{
			final LayoutConstraint[] allConstraints = view.allConstraints();
			Map<Component, ArrayList<LayoutConstraint>> map = new HashMap<>();

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
			for (Component subComp : ((Container)view).getComponents())
			{
				if (map.get(subComp) == null)
				{
					String[] classes = subComp.getClass().toString().substring(6).split(Pattern.quote("."));
					System.out.println(classes[max(0, classes.length - 1)] + ":" + System.identityHashCode(subComp) + " does not have any constraints.  LayoutEngine does not know how to render this view on screen.");
				}
			}

			process(map, ((Container)view));
			return;
		}


		JComponent component;
		try
		{
			component = ((JComponent)view);
		}
		catch (ClassCastException e)
		{
			System.out.println("The constrainable view must be a descendant of JComponent");
			e.printStackTrace();
			System.exit(0);
			return;
		}

		final LayoutConstraint[] allConstraints = view.allConstraints();
		Map<Component, ArrayList<LayoutConstraint>> map = new HashMap<>();

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
		while (map.size() != 0)
		{
			Component viewToConstrain = map.keySet().iterator().next();
			ArrayList<LayoutConstraint> constraints = map.get(viewToConstrain);

			sort(constraints, viewToConstrain);

			constrainPreferredSizeIfNeeded(constraints, viewToConstrain);

			ArrayList<LayoutAttribute> attributesSatisfied = new ArrayList<>();

			for (LayoutConstraint constraint : constraints)
			{
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
				constraint.hasBeenProcessed = true;
			}
			viewToConstrain.setPreferredSize(new Dimension(viewToConstrain.getBounds().width, viewToConstrain.getBounds().height));
			map.remove(viewToConstrain);
		}
	}

	private void processConstraintOnParent(LayoutConstraint constraint, ArrayList<LayoutAttribute> attributesSatisfied, Component parent)
	{
		switch (constraint.attributeOne)
		{
			case top:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.bottom) || attributesSatisfied.contains(LayoutAttribute.centerY)) && attributesSatisfied.contains(LayoutAttribute.height))
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
						top = (int) (parent.getPreferredSize().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;
					}

					case bottom:
					{
						top = (int) (parent.getPreferredSize().getHeight() * constraint.multiplier) + constraint.constant;
						break;
					}

					default:
					{
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .top can only be constrained to .top, .centerY, or .bottom");
						break;
					}
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, top, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(LayoutAttribute.top);
				break;
			}

			case bottom:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.top) || attributesSatisfied.contains(LayoutAttribute.centerY)) && attributesSatisfied.contains(LayoutAttribute.height))
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

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, bottom - constraint.viewOne.getBounds().y);
				attributesSatisfied.add(LayoutAttribute.bottom);
				break;
			}

			case leading:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.trailing) || attributesSatisfied.contains(LayoutAttribute.centerX)) && attributesSatisfied.contains(LayoutAttribute.width))
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
				attributesSatisfied.add(LayoutAttribute.leading);
				break;
			}

			case trailing:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.leading) || attributesSatisfied.contains(LayoutAttribute.centerX)) && attributesSatisfied.contains(LayoutAttribute.width))
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
						trailing = constraint.constant;
						break;

					case centerX:
						trailing = (int) (parent.getPreferredSize().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						trailing = (int) (parent.getPreferredSize().getWidth() * constraint.multiplier) + constraint.constant;
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
				attributesSatisfied.add(LayoutAttribute.trailing);
				break;
			}

			case width:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.leading) || attributesSatisfied.contains(LayoutAttribute.centerX)) && attributesSatisfied.contains(LayoutAttribute.trailing))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.leading or .centerX) and .trailing.  Setting .width would break the constraints");
					break;
				}

				int width = 0;
				switch (constraint.attributeTwo)
				{
					case width:
						width = (int)(parent.getPreferredSize().getWidth() * constraint.multiplier) + constraint.constant;

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
				attributesSatisfied.add(LayoutAttribute.width);
				break;
			}

			case height:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.top) || attributesSatisfied.contains(LayoutAttribute.centerY)) && attributesSatisfied.contains(LayoutAttribute.bottom))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.top or .centerY) and .bottom.  Setting .height would break the constraints");
					break;
				}

				int height = 0;
				switch (constraint.attributeTwo)
				{
					case height:
						height = (int)(parent.getPreferredSize().getHeight() * constraint.multiplier) + constraint.constant;
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
				attributesSatisfied.add(LayoutAttribute.height);
				break;
			}

			case centerX:
			{
				if (((attributesSatisfied.contains(LayoutAttribute.trailing) || attributesSatisfied.contains(LayoutAttribute.leading)) && attributesSatisfied.contains(LayoutAttribute.width)) || (attributesSatisfied.contains(LayoutAttribute.trailing) && attributesSatisfied.contains(LayoutAttribute.leading)))
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
						centerX = (int) (parent.getPreferredSize().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						centerX = (int) (parent.getPreferredSize().getWidth() * constraint.multiplier) + constraint.constant;
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

				if (attributesSatisfied.contains(LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, (centerX - constraint.viewOne.getBounds().x) * 2, constraint.viewOne.getBounds().height);
				}
				else if (attributesSatisfied.contains(LayoutAttribute.width))
				{
					constraint.viewOne.setBounds(centerX - (constraint.viewOne.getBounds().width/2), constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(LayoutAttribute.centerX);
				break;
			}

			case centerY:
			{
				if (((attributesSatisfied.contains(LayoutAttribute.top) || attributesSatisfied.contains(LayoutAttribute.bottom)) && attributesSatisfied.contains(LayoutAttribute.height)) || (attributesSatisfied.contains(LayoutAttribute.top) && attributesSatisfied.contains(LayoutAttribute.bottom)))
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
						centerY = (int) (parent.getPreferredSize().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case bottom:
						centerY = (int) (parent.getPreferredSize().getHeight() * constraint.multiplier) + constraint.constant;
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

				if (attributesSatisfied.contains(LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, (centerY - constraint.viewOne.getBounds().y) * 2);
				}
				else if (attributesSatisfied.contains(LayoutAttribute.width))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, centerY - (constraint.viewOne.getBounds().height/2), constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(LayoutAttribute.centerX);
				break;
			}
		}
	}

	private void processConstraint(LayoutConstraint constraint, ArrayList<LayoutAttribute> attributesSatisfied)
	{
		switch (constraint.attributeOne)
		{
			case top:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.bottom) || attributesSatisfied.contains(LayoutAttribute.centerY)) && attributesSatisfied.contains(LayoutAttribute.height))
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
						top = constraint.viewTwo.getBounds().y + (int)(constraint.viewTwo.getPreferredSize().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;
					}

					case bottom:
					{
						top = constraint.viewTwo.getBounds().y + (int)(constraint.viewTwo.getPreferredSize().getHeight() * constraint.multiplier) + constraint.constant;
						break;
					}

					default:
					{
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .top can only be constrained to .top, .centerY, or .bottom");
						break;
					}
				}

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, top, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(LayoutAttribute.top);
				break;
			}

			case bottom:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.top) || attributesSatisfied.contains(LayoutAttribute.centerY)) && attributesSatisfied.contains(LayoutAttribute.height))
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
						bottom = constraint.viewTwo.getBounds().y + (int)(constraint.viewTwo.getPreferredSize().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case bottom:
						bottom = constraint.viewTwo.getBounds().y + (int)(constraint.viewTwo.getPreferredSize().getHeight() * constraint.multiplier) + constraint.constant;
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
				attributesSatisfied.add(LayoutAttribute.bottom);
				break;
			}

			case leading:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.trailing) || attributesSatisfied.contains(LayoutAttribute.centerX)) && attributesSatisfied.contains(LayoutAttribute.width))
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
						leading = constraint.viewTwo.getBounds().x + (int)(constraint.viewTwo.getPreferredSize().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						leading = constraint.viewTwo.getBounds().x + (int)(constraint.viewTwo.getPreferredSize().getWidth() * constraint.multiplier) + constraint.constant;
						break;

					default:
						System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  .leading can only be constrained to .leading, .centerX, or .trailing");
						break;
				}

				constraint.viewOne.setBounds(leading, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(LayoutAttribute.leading);
				break;
			}

			case trailing:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.leading) || attributesSatisfied.contains(LayoutAttribute.centerX)) && attributesSatisfied.contains(LayoutAttribute.width))
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
						trailing = constraint.constant;
						break;

					case centerX:
						trailing = constraint.viewTwo.getBounds().x + (int)(constraint.viewTwo.getPreferredSize().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						trailing = constraint.viewTwo.getBounds().x + (int)(constraint.viewTwo.getPreferredSize().getWidth() * constraint.multiplier) + constraint.constant;
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
				attributesSatisfied.add(LayoutAttribute.trailing);
				break;
			}

			case width:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.leading) || attributesSatisfied.contains(LayoutAttribute.centerX)) && attributesSatisfied.contains(LayoutAttribute.trailing))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.leading or .centerX) and .trailing.  Setting .width would break the constraints");
					break;
				}

				int width = 0;
				switch (constraint.attributeTwo)
				{
					case width:
						width = constraint.viewTwo.getBounds().x + (int)(constraint.viewTwo.getPreferredSize().getWidth() * constraint.multiplier) + constraint.constant;

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
				attributesSatisfied.add(LayoutAttribute.width);
				break;
			}

			case height:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.top) || attributesSatisfied.contains(LayoutAttribute.centerY)) && attributesSatisfied.contains(LayoutAttribute.bottom))
				{
					System.out.println("Cannot satisfy constraint: " + constraint.toString() + ".  You have already specified (.top or .centerY) and .bottom.  Setting .height would break the constraints");
					break;
				}

				int height = 0;
				switch (constraint.attributeTwo)
				{
					case height:
						height = constraint.viewTwo.getBounds().y + (int)(constraint.viewTwo.getPreferredSize().getHeight() * constraint.multiplier) + constraint.constant;
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
				attributesSatisfied.add(LayoutAttribute.height);
				break;
			}

			case centerX:
			{
				if (((attributesSatisfied.contains(LayoutAttribute.trailing) || attributesSatisfied.contains(LayoutAttribute.leading)) && attributesSatisfied.contains(LayoutAttribute.width)) || (attributesSatisfied.contains(LayoutAttribute.trailing) && attributesSatisfied.contains(LayoutAttribute.leading)))
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
						centerX = constraint.viewTwo.getBounds().x + (int)(constraint.viewTwo.getPreferredSize().getWidth() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case trailing:
						centerX = constraint.viewTwo.getBounds().x + (int)(constraint.viewTwo.getPreferredSize().getWidth() * constraint.multiplier) + constraint.constant;
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

				if (attributesSatisfied.contains(LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, (centerX - constraint.viewOne.getBounds().x) * 2, constraint.viewOne.getBounds().height);
				}
				else if (attributesSatisfied.contains(LayoutAttribute.width))
				{
					constraint.viewOne.setBounds(centerX - (constraint.viewOne.getBounds().width/2), constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(LayoutAttribute.centerX);
				break;
			}

			case centerY:
			{
				if (((attributesSatisfied.contains(LayoutAttribute.top) || attributesSatisfied.contains(LayoutAttribute.bottom)) && attributesSatisfied.contains(LayoutAttribute.height)) || (attributesSatisfied.contains(LayoutAttribute.top) && attributesSatisfied.contains(LayoutAttribute.bottom)))
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
						centerY = constraint.viewTwo.getBounds().y + (int)(constraint.viewTwo.getPreferredSize().getHeight() / 2.0 * constraint.multiplier) + constraint.constant;
						break;

					case bottom:
						centerY = constraint.viewTwo.getBounds().y + (int)(constraint.viewTwo.getPreferredSize().getHeight() * constraint.multiplier) + constraint.constant;
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

				if (attributesSatisfied.contains(LayoutAttribute.leading))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, constraint.viewOne.getBounds().width, (centerY - constraint.viewOne.getBounds().y) * 2);
				}
				else if (attributesSatisfied.contains(LayoutAttribute.width))
				{
					constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, centerY - (constraint.viewOne.getBounds().height/2), constraint.viewOne.getBounds().width, constraint.viewOne.getBounds().height);
				}
				attributesSatisfied.add(LayoutAttribute.centerX);
				break;
			}
		}
	}

	private void processConstraintOnNullSecondView(LayoutConstraint constraint, ArrayList<LayoutAttribute> attributesSatisfied)
	{
		switch (constraint.attributeOne)
		{
			case width:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.leading) || attributesSatisfied.contains(LayoutAttribute.centerX)) && attributesSatisfied.contains(LayoutAttribute.trailing))
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

				constraint.viewOne.setBounds(constraint.viewOne.getBounds().x, constraint.viewOne.getBounds().y, width, constraint.viewOne.getBounds().height);
				attributesSatisfied.add(LayoutAttribute.width);
				break;
			}

			case height:
			{
				if ((attributesSatisfied.contains(LayoutAttribute.top) || attributesSatisfied.contains(LayoutAttribute.centerY)) && attributesSatisfied.contains(LayoutAttribute.bottom))
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
				attributesSatisfied.add(LayoutAttribute.height);
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
			                 //Top/Leading
			                 if (o1.attributeOne == LayoutAttribute.top)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.top)
				                 {
					                 System.out.println("There are multiple constraints for .top for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 return -1;
			                 }
			                 else if (o1.attributeOne == LayoutAttribute.leading)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.leading)
				                 {
					                 System.out.println("There are multiple constraints for .leading for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.top)
				                 {
					                 return 1;
				                 }
				                 return -1;
			                 }
			                 //Bottom/Left
			                 else if (o1.attributeOne == LayoutAttribute.bottom)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.bottom)
				                 {
					                 System.out.println("There are multiple constraints for .bottom for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.top || o2.attributeOne == LayoutAttribute.leading)
				                 {
					                 return 1;
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
				                 else if (o2.attributeOne == LayoutAttribute.top || o2.attributeOne == LayoutAttribute.leading || o2.attributeOne == LayoutAttribute.bottom)
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
				                 else if (o2.attributeOne != LayoutAttribute.centerY)
				                 {
					                 return -1;
				                 }
				                 return 1;
			                 }
			                 else if (o1.attributeOne == LayoutAttribute.centerY)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.centerY)
				                 {
					                 System.out.println("There are multiple constraints for .centerY for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 return 1;
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
				                 else if (o2.attributeOne == LayoutAttribute.centerY || o2.attributeOne == LayoutAttribute.centerX || o2.attributeOne == LayoutAttribute.height)
				                 {
					                 return -1;
				                 }
				                 return 1;
			                 }
			                 else if (o1.attributeOne == LayoutAttribute.height)
			                 {
				                 if (o2.attributeOne == LayoutAttribute.height)
				                 {
					                 System.out.println("There are multiple constraints for .height for " + viewToConstrain.toString() + ".  AutoLayout cannot position this view.  Please check your constraints and find the one you don't want and remove it.");
					                 System.exit(-1);
					                 return 0;
				                 }
				                 else if (o2.attributeOne == LayoutAttribute.centerY || o2.attributeOne == LayoutAttribute.centerX)
				                 {
					                 return -1;
				                 }
				                 return 1;
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

	public static String getClassAndHashCode(Object object)
	{
		String[] classes = object.getClass().toString().substring(6).split(Pattern.quote("."));
		return classes[max(0, classes.length - 1)] + ":" + System.identityHashCode(object);
	}
}