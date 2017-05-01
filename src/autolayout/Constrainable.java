package autolayout;

import autolayout.uiobjects.ALJPanel;

/**
 * The interface used by LayoutEngine to define AutoLayout compatible views.
 * <br><br>
 * You are free to implement this interface in any class you want AutoLayout to be compatible with.
 *
 * @implNote
 * <tab></tab>If you choose to implement this interface, please note:
 * <ul>
 *     <li>You must call LayoutEngine.processConstraintsIn(this); in layoutSubviews()</li>
 * </ul>
 *
 * @see ALJPanel
 * @see ALJPanel#layoutSubviews()
 */
@SuppressWarnings("unused")
public interface Constrainable
{
	void addConstraint(LayoutConstraint constraint);

	LayoutConstraint[] allConstraints();

	void layoutSubviews();

	int compressionResistanceWidth();

	int compressionResistanceHeight();

	void setCompressionResistanceWidth(int compressionResistanceWidth);

	void setCompressionResistanceHeight(int compressionResistanceHeight);

	void setCalculatedHeight(int calculatedHeight);

	void setCalculatedWidth(int calculatedWidth);

	int calculatedHeight();

	int calculatedWidth();
}
