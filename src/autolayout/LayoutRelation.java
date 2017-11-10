/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout;

/**
 * The relation between the first and second attributes in a constraint.
 */
public enum LayoutRelation
{
	lessThanOrEqual, equal, greaterThanOrEqual;

	@Override
	public String toString()
	{
		switch (this)
		{
			case lessThanOrEqual: return "<=";

			case equal: return "==";

			case greaterThanOrEqual: return ">=";
		}
		return "NULL";
	}
}