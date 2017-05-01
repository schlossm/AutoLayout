package autolayout.ALJTable;

import autolayout.LayoutAttribute;
import autolayout.LayoutConstraint;
import autolayout.LayoutRelation;
import autolayout.uiobjects.ALJPanel;
import autolayout.util.MLMDelegate;
import autolayout.util.MLMEventType;
import autolayout.util.MouseListenerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static javax.imageio.ImageIO.read;

interface ALJTableCellDelegate
{
	void accessoryViewClicked(ALJTableCellAccessoryViewType accessoryViewType, ALJTableIndex atIndex);
}

@SuppressWarnings({"WeakerAccess", "unused"})
public class ALJTableCell extends ALJPanel implements MLMDelegate
{
	public final JLabel titleLabel;
	protected JLabel accessoryView;
	ALJTableIndex currentIndex;
	ALJTableCellDelegate delegate;
	private ALJTableCellAccessoryViewType _accessoryViewType = ALJTableCellAccessoryViewType.none;
	private boolean isClicked = false;

	public ALJTableCell(ALJTableCellAccessoryViewType accessoryViewType)
	{
		setBackground(Color.white);
		titleLabel = new JLabel();
		titleLabel.setFocusable(false);
		titleLabel.setVerticalAlignment(SwingConstants.CENTER);

		setAccessoryType(accessoryViewType);
		_accessoryViewType = accessoryViewType;
		add(titleLabel);

		addConstraint(new LayoutConstraint(titleLabel, LayoutAttribute.leading, LayoutRelation.equal, this, LayoutAttribute.leading, 1.0, 8));
		addConstraint(new LayoutConstraint(titleLabel, LayoutAttribute.top, LayoutRelation.equal, this, LayoutAttribute.top, 1.0, 8));
		addConstraint(new LayoutConstraint(titleLabel, LayoutAttribute.bottom, LayoutRelation.equal, this, LayoutAttribute.bottom, 1.0, -8));
	}

	public void setAccessoryType(ALJTableCellAccessoryViewType accessoryViewType)
	{
		switch (accessoryViewType)
		{
			case delete:
			{
				if (accessoryView != null)
				{
					remove(accessoryView);
					accessoryView = null;
				}
				accessoryView = new JLabel(new ImageIcon(new ALJTableCellAccessoryViewImage("delete").image));
				accessoryView.addMouseListener(new MouseListenerManager(this));
				add(accessoryView);
				break;
			}

			case detail:
			{
				if (accessoryView != null)
				{
					remove(accessoryView);
					accessoryView = null;
				}
				accessoryView = new JLabel(new ImageIcon(new ALJTableCellAccessoryViewImage("detail").image));
				accessoryView.addMouseListener(new MouseListenerManager(this));
				add(accessoryView);
				break;
			}

			case info:
			{
				if (accessoryView != null)
				{
					remove(accessoryView);
					accessoryView = null;
				}
				accessoryView = new JLabel(new ImageIcon(new ALJTableCellAccessoryViewImage("menu").image));
				accessoryView.addMouseListener(new MouseListenerManager(this));
				add(accessoryView);
				break;
			}

			case move:
			{
				if (accessoryView != null)
				{
					remove(accessoryView);
					accessoryView = null;
				}
				accessoryView = new JLabel(new ImageIcon(new ALJTableCellAccessoryViewImage("move").image));
				accessoryView.addMouseListener(new MouseListenerManager(this));
				add(accessoryView);
				break;
			}

			case none:
			{
				if (accessoryView != null)
				{
					remove(accessoryView);
					accessoryView = null;
				}
				addConstraint(new LayoutConstraint(titleLabel, LayoutAttribute.trailing, LayoutRelation.equal, this, LayoutAttribute.trailing, 1.0, 0));
				break;
			}
		}

		if (accessoryView != null)
		{
			addConstraint(new LayoutConstraint(accessoryView, LayoutAttribute.trailing, LayoutRelation.equal, this, LayoutAttribute.trailing, 1.0, 0));
			addConstraint(new LayoutConstraint(accessoryView, LayoutAttribute.top, LayoutRelation.equal, this, LayoutAttribute.top, 1.0, 0));
			addConstraint(new LayoutConstraint(accessoryView, LayoutAttribute.bottom, LayoutRelation.equal, this, LayoutAttribute.bottom, 1.0, 0));
			addConstraint(new LayoutConstraint(accessoryView, LayoutAttribute.width, LayoutRelation.equal, null, LayoutAttribute.width, 1.0, 44));
			addConstraint(new LayoutConstraint(titleLabel, LayoutAttribute.trailing, LayoutRelation.equal, accessoryView, LayoutAttribute.leading, 1.0, -8));
		}
	}

	protected void registerComponentForClicking(Component component)
	{
		component.addMouseListener(new MouseListenerManager(this));
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(super.getPreferredSize().width, titleLabel.getPreferredSize().height + 16);
	}

	@Override
	public void mousePoint(MouseEvent action, MLMEventType eventType)
	{
		if (eventType == MLMEventType.pressed)
		{
			isClicked = true;
			if (action.getSource() != accessoryView) { return; }
			accessoryView.setOpaque(true);
			accessoryView.setBackground(Color.lightGray);
		}
		else if (eventType == MLMEventType.draggedIn)
		{
			if (isClicked) { return; }
			if (action.getSource() != accessoryView) { return; }
			accessoryView.setOpaque(true);
			accessoryView.setBackground(Color.lightGray);
		}
		else if (eventType == MLMEventType.released)
		{
			if (!isClicked) { return; }
			isClicked = false;
			if (accessoryView == null)
			{
				delegate.accessoryViewClicked(ALJTableCellAccessoryViewType.none, currentIndex);
				return;
			}
			if (action.getSource() != accessoryView && !accessoryView.isOpaque())
			{
				delegate.accessoryViewClicked(ALJTableCellAccessoryViewType.none, currentIndex);
				return;
			}
			accessoryView.setOpaque(false);
			accessoryView.setBackground(new Color(0, 0, 0, 0));
			delegate.accessoryViewClicked(_accessoryViewType, currentIndex);
		}
		else if (eventType == MLMEventType.draggedOut)
		{
			if (!isClicked) { return; }
			isClicked = false;
			if (action.getSource() != accessoryView) { return; }
			accessoryView.setOpaque(false);
			accessoryView.setBackground(new Color(0, 0, 0, 0));
		}
	}
}

class ALJTableCellAccessoryViewImage
{
	BufferedImage image;

	ALJTableCellAccessoryViewImage(String imageTitle)
	{
		String path = "images/";
		try
		{
			image = read(this.getClass().getResource(path + imageTitle + ".png"));
		}
		catch (IOException ignored) { }
	}
}