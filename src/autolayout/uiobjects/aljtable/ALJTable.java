/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout.uiobjects.aljtable;

import autolayout.LayoutAttribute;
import autolayout.LayoutConstraint;
import autolayout.LayoutRelation;
import autolayout.uiobjects.ALJPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

import static autolayout.LayoutEngine.getClassAndHashCode;

@SuppressWarnings("unused")
public class ALJTable extends ALJPanel implements ComponentListener, ALJTableCellDelegate
{
	private final ALJPanel tableView;
	private final JScrollPane scrollPane;
	public ALJTableDataSource dataSource;
	public ALJTableDelegate delegate;
	public int heightForRow = -1;
	private boolean _isLoaded = false;

	public ALJTable()
	{
		setBackground(Color.white);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		tableView = new ALJPanel();
		tableView.setBackground(Color.white);

		scrollPane = new JScrollPane();
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		ScrollPaneLayout layout = (ScrollPaneLayout) (scrollPane.getLayout());
		layout.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		layout.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
		scrollPane.getVerticalScrollBar().setVisible(false);
		scrollPane.setWheelScrollingEnabled(true);
		add(scrollPane);

		scrollPane.getViewport().setView(tableView);

		addConstraint(new LayoutConstraint(scrollPane, LayoutAttribute.leading, LayoutRelation.equal, this, LayoutAttribute.leading, 1.0, 0));
		addConstraint(new LayoutConstraint(scrollPane, LayoutAttribute.top, LayoutRelation.equal, this, LayoutAttribute.top, 1.0, 0));
		addConstraint(new LayoutConstraint(scrollPane, LayoutAttribute.trailing, LayoutRelation.equal, this, LayoutAttribute.trailing, 1.0, 0));
		addConstraint(new LayoutConstraint(scrollPane, LayoutAttribute.bottom, LayoutRelation.equal, this, LayoutAttribute.bottom, 1.0, 0));
	}

	public boolean isLoaded()
	{
		return _isLoaded;
	}

	public void layoutSubviews()
	{
		super.layoutSubviews();

		tableView.setPreferredSize(new Dimension(scrollPane.getBounds().width, tableView.calculatedHeight()));
		scrollPane.setPreferredSize(new Dimension(scrollPane.getBounds().width, tableView.calculatedHeight()));
		tableView.layoutSubviews();
		tableView.setBounds(0, 0, getBounds().width, 1000000);
		repaint();
		tableView.layoutSubviews();
		tableView.setBounds(0, 0, getBounds().width, tableView.calculatedHeight());
		repaint();
		tableView.layoutSubviews();
		tableView.setBounds(0, 0, getBounds().width, tableView.calculatedHeight());
		repaint();
		tableView.repaint();
		scrollPane.repaint();
		scrollPane.revalidate();
	}

	public void reloadData()
	{
		if (dataSource == null)
		{
			System.out.println(getClassAndHashCode(this) + " does not have a dataSource set yet.");
			return;
		}

		tableView.removeAll();

		int totalNumSections = dataSource.numberOfSectionsIn(this);

		JComponent previous = null;

		for (int section = 0; section < totalNumSections; section++)
		{
			if (!Objects.equals(dataSource.titleForHeaderInSectionInTable(this, section), "") && dataSource.titleForHeaderInSectionInTable(this, section) != null)
			{
				JLabel sectionTitle = new JLabel(dataSource.titleForHeaderInSectionInTable(this, section).toUpperCase());
				sectionTitle.setForeground(Color.lightGray);
				tableView.add(sectionTitle);

				tableView.addConstraint(new LayoutConstraint(sectionTitle, LayoutAttribute.leading, LayoutRelation.equal, tableView, LayoutAttribute.leading, 1.0, 40));
				if (previous == null)
				{
					tableView.addConstraint(new LayoutConstraint(sectionTitle, LayoutAttribute.top, LayoutRelation.equal, tableView, LayoutAttribute.top, 1.0, 80));
				}
				else
				{
					tableView.addConstraint(new LayoutConstraint(sectionTitle, LayoutAttribute.top, LayoutRelation.equal, previous, LayoutAttribute.bottom, 1.0, 80));
				}

				previous = sectionTitle;
			}

			int totalNumRowsInSection = dataSource.numberOfRowsInSectionForTable(this, section);
			for (int item = 0; item < totalNumRowsInSection; item++)
			{
				ALJTableCell cell = dataSource.cellForRowAtIndexInTable(this, new ALJTableIndex(section, item));
				cell.delegate = this;
				cell.currentIndex = new ALJTableIndex(section, item);
				tableView.add(cell);

				int finalSection = section;
				int finalItem = item;
				ALJTable table = this;
				cell.addMouseListener(new MouseListener()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{

					}

					@Override
					public void mousePressed(MouseEvent e)
					{

					}

					@Override
					public void mouseReleased(MouseEvent e)
					{
						if (delegate != null)
						{ delegate.didSelectItemAtIndexInTable(table, new ALJTableIndex(finalSection, finalItem)); }
					}

					@Override
					public void mouseEntered(MouseEvent e)
					{

					}

					@Override
					public void mouseExited(MouseEvent e)
					{

					}
				});

				tableView.addConstraint(new LayoutConstraint(cell, LayoutAttribute.leading, LayoutRelation.equal, tableView, LayoutAttribute.leading, 1.0, 0));
				tableView.addConstraint(new LayoutConstraint(cell, LayoutAttribute.trailing, LayoutRelation.equal, tableView, LayoutAttribute.trailing, 1.0, 0));

				if (dataSource.heightForRow(this, section) > 0)
				{
					tableView.addConstraint(new LayoutConstraint(cell, LayoutAttribute.height, LayoutRelation.equal, null, LayoutAttribute.height, 1.0, dataSource.heightForRow(this, section)));
				}
				else
				{
					if (heightForRow > 44)
					{
						tableView.addConstraint(new LayoutConstraint(cell, LayoutAttribute.height, LayoutRelation.equal, null, LayoutAttribute.height, 1.0, heightForRow));
					}
					else
					{
						tableView.addConstraint(new LayoutConstraint(cell, LayoutAttribute.height, LayoutRelation.equal, null, LayoutAttribute.height, 1.0, 44));
					}
				}
				if (previous == null)
				{
					tableView.addConstraint(new LayoutConstraint(cell, LayoutAttribute.top, LayoutRelation.equal, tableView, LayoutAttribute.top, 1.0, 0));
				}
				else
				{
					tableView.addConstraint(new LayoutConstraint(cell, LayoutAttribute.top, LayoutRelation.equal, previous, LayoutAttribute.bottom, 1.0, ((previous instanceof JLabel) ? 8 : 0)));
				}

				previous = cell;
			}
		}

		if (previous != null)
		{ setPreferredSize(new Dimension(tableView.getPreferredSize().width, previous.getBounds().y + previous.getPreferredSize().height)); }
		_isLoaded = true;
		layoutSubviews();
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		tableView.layoutSubviews();
	}

	@Override
	public void componentMoved(ComponentEvent e) { }

	@Override
	public void componentShown(ComponentEvent e)
	{
		tableView.layoutSubviews();
	}

	@Override
	public void componentHidden(ComponentEvent e) { }

	@Override
	public void accessoryViewClicked(ALJTableCellAccessoryViewType accessoryViewType, ALJTableIndex atIndex)
	{
		switch (accessoryViewType)
		{
			case none:
			{
				if (delegate != null) { delegate.didSelectItemAtIndexInTable(this, atIndex); }
				break;
			}

			case delete:
			{
				dataSource.tableView(this, ALJTableCellEditingStyle.delete, atIndex);
				reloadData();
				break;
			}

			case detail:
			{
				if (delegate != null) { delegate.didSelectItemAtIndexInTable(this, atIndex); }
				break;
			}

			case info:
			{
				dataSource.tableView(this, ALJTableCellEditingStyle.info, atIndex);
				break;
			}

			case move:
			{
				break;
			}
		}
	}
}
