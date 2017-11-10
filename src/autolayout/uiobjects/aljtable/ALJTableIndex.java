/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout.uiobjects.aljtable;

@SuppressWarnings("unused")
public class ALJTableIndex
{
	public final int item;
	public final int section;

	ALJTableIndex(int section, int item)
	{
		this.item = item;
		this.section = section;
	}
}
