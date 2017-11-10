/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout;

public interface LayoutEngineConstrainable
{
	LayoutConstraint[] allConstraints();

	void layoutSubviews();

	void setCalculatedHeight(int calculatedHeight);

	void setCalculatedWidth(int calculatedWidth);
}
