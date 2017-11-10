/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout;

/**
 * The part of the object’s visual representation that should be used to get the value for the constraint.
 */
public enum LayoutAttribute
{
	top, bottom, leading, trailing, width, height, centerX, centerY;

	@Override
	public String toString()
	{
		return this.name();
	}
}
