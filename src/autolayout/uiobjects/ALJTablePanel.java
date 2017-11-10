/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout.uiobjects;

import autolayout.uiobjects.aljtable.*;
import autolayout.LayoutAttribute;
import autolayout.LayoutConstraint;
import autolayout.LayoutRelation;

import java.awt.*;

@SuppressWarnings("unused")
abstract public class ALJTablePanel extends ALJPanel implements ALJTableDataSource, ALJTableDelegate
{
	public final ALJTable table;

	public ALJTablePanel()
	{
		super();
		table = new ALJTable();
		table.heightForRow = 88;
		table.dataSource = this;
		table.delegate = this;

		add(table);
		addConstraint(new LayoutConstraint(table, LayoutAttribute.leading, LayoutRelation.equal, this, LayoutAttribute.leading, 1.0, 0));
		addConstraint(new LayoutConstraint(table, LayoutAttribute.top, LayoutRelation.equal, this, LayoutAttribute.top, 1.0, 0));
		addConstraint(new LayoutConstraint(table, LayoutAttribute.trailing, LayoutRelation.equal, this, LayoutAttribute.trailing, 1.0, 0));
		addConstraint(new LayoutConstraint(table, LayoutAttribute.bottom, LayoutRelation.equal, this, LayoutAttribute.bottom, 1.0, 0));
	}

	@Override
	public Component add(Component comp)
	{
		if (getComponents().length != 0) { System.err.println("Adding components to ALJTablePanel will cause improper rendering issues"); }
		return super.add(comp);
	}

	@Override
	public void layoutSubviews()
	{
		super.layoutSubviews();
		if (!table.isLoaded()) { table.reloadData(); }
		table.layoutSubviews();
		repaint();
	}
}
