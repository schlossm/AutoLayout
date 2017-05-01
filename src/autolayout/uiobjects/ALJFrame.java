package autolayout.uiobjects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Use ALJFrame in place of JFrame to inherit AutoLayout properties on JFrames.  This class is subclassable, but can be used as is.
 */
@SuppressWarnings({"unused"})
public class ALJFrame extends JFrame implements ComponentListener
{
	public ALJFrame(String title)
	{
		super(title);
		addComponentListener(this);
	}

	private void layoutSubviews()
	{
		if (!isVisible()) { return; }
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		layoutSubviews();
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
		layoutSubviews();
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
		process();
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	@Override
	public void componentHidden(ComponentEvent e) { }

	private void process()
	{
		for (Component component : getContentPane().getComponents())
		{
			component.setBounds(0, component.getBounds().y, getContentPane().getWidth(), getContentPane().getHeight() - component.getBounds().y);
			if (component instanceof ALJPanel)
			{
				((ALJPanel) component).layoutSubviews();
			}
			else if (component instanceof JPanel)
			{
				((ComponentListener) component).componentResized(null);
			}
		}
	}
}
