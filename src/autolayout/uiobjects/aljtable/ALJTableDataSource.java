/*
 * Copyright (c) 2017 Michael Schloss.  All rights reserved.
 */

package autolayout.uiobjects.aljtable;

@SuppressWarnings({"SameParameterValue", "unused", "SameReturnValue"})
public interface ALJTableDataSource
{
	int numberOfSectionsIn(ALJTable table);

	int numberOfRowsInSectionForTable(ALJTable table, int section);

	int heightForRow(ALJTable table, int inSection);

	ALJTableCell cellForRowAtIndexInTable(ALJTable table, ALJTableIndex index);

	String titleForHeaderInSectionInTable(ALJTable table, int section);

	String titleForFooterInSectionInTable(ALJTable table, int section);

	void tableView(ALJTable table, ALJTableCellEditingStyle commit, ALJTableIndex forRowAt);
}
