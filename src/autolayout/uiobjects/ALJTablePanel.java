package autolayout.uiobjects;

import autolayout.ALJTable.ALJTable;
import autolayout.ALJTable.ALJTableDataSource;
import autolayout.ALJTable.ALJTableDelegate;
import autolayout.LayoutAttribute;
import autolayout.LayoutConstraint;
import autolayout.LayoutRelation;

import java.awt.*;

abstract public class ALJTablePanel extends ALJPanel implements ALJTableDataSource, ALJTableDelegate
{
	protected final ALJTable table;

	protected ALJTablePanel()
	{
		setBackground(Color.white);
		setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		setBackground(Color.white);
		setOpaque(true);

		table = new ALJTable();
		table.heightForRow = 66;
		table.dataSource = this;
		table.delegate = this;

		add(table);

		addConstraint(new LayoutConstraint(table, LayoutAttribute.leading, LayoutRelation.equal, this, LayoutAttribute.leading, 1.0, 0));
		addConstraint(new LayoutConstraint(table, LayoutAttribute.top, LayoutRelation.equal, this, LayoutAttribute.top, 1.0, 0));
		addConstraint(new LayoutConstraint(table, LayoutAttribute.trailing, LayoutRelation.equal, this, LayoutAttribute.trailing, 1.0, 0));
		addConstraint(new LayoutConstraint(table, LayoutAttribute.bottom, LayoutRelation.equal, this, LayoutAttribute.bottom, 1.0, 0));
	}

	@Override
	public void layoutSubviews()
	{
		super.layoutSubviews();
		if (!table.isLoaded())
		{
			table.reloadData();
			table.layoutSubviews();
			repaint();
		}
		else
		{
			table.layoutSubviews();
			repaint();
		}
	}
}
