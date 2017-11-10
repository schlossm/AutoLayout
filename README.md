# AutoLayout
A fully fledged auto layout engine for Java's Swing platform

Current status: **Beta 1.5**

Designed to be used in place of Swing's `LayoutManager` and `LayoutManager2` classes.  AutoLayout provides full scale custom layouts with built in automated resizing.

## Release Notes
__**Beta 1.5**__

*

## Beta Notes

### Current Limitations
**This project is in Beta.**  Many things work, and many things do not.

* All constraints are processed at each resizing.  I have not yet implemented efficient resizing of constraints.
	* As a result, the `hasBeenProcessed` attribute of `LayoutConstraint` is set, but never read.  This will change once I implement more efficient constraint processing.

* `LayoutEngine` does not respect the `compressionResistanceWidth` and `compressionResistanceHeight` of `ALJPanel`.  This is intentional to place priority on the layout engine working properly before diving into resizing different items at different times.

* Error checking is not fully completed.  `LayoutEngine` does not yet print out the things I want it to, and in edge cases it sometimes misses errors in layout constraining.

* `LayoutEngine` cannot yet recover itself.  The default behavior is telling you there's an error, and then either ending constraint processing or, if the error is great enough, terminating execution.  This feature is coming.

* Constraining on just centerX and centerY is having some difficulties showing the view on screen.  Currently working on getting that fixed.

### Things that work
* Can constrain to any part of a view.  Can constrain to null second view as long as first attribute is width or height.

* Error checking for duplicate constraints, and illegal single constraints.  Error checking on illegal groups of constraints is there, but it requires more testing.

* ALJPanel and ALJFrame work in conjunction with other JPanels that use LayoutManager.  It will call their componentResized method to revalidate the layout if the LayoutManager natively supports that.  If it doesn't, you can implement `ComponentListener` and manually update layout.

* `LayoutEngine` attempts to respect preferredSize if `.greaterThanOrEqual` or `.lessThanOrEqual` is used as the relation.

## Use

AutoLayout was designed to mimic Apple'sⓒ NSAutoLayout rendering engine.

All you need to do is use an ALJPanel instance instead of JPanel.  ALJPanel is able to be subclassed, so you may add extended features to your panels without breaking the AutoLayout feature.

1. Initialize a constraint using `LayoutConstraint(Component viewOne, LayoutAttribute attributeOne, LayoutRelation relation, Component viewTwo, LayoutAttribute attributeTwo, double multiplier, int constant)`
1. Add all the constraints using the `addConstraint()` method of `ALJPanel`.
2. That's it.  `ALJPanel` will render itself as needed.  The `layoutSubviews()` method in `ALJPanel` is provided to you to explicitly relayout the views on screen after removing or adding constraints.


## Disclaimers
This project is in beta.  It is not intended for production builds, unless you have had major experience developing on iOS, and know your way around the layout engine.

I will update this project as frequently as I can.

If you wish to contribute to this project, feel free to fork, pull request, and get in contact with me.  This is a living, breathing project and will change as needs change.  My goal in the end is to create a fully usable LayoutEngine that anyone working with Swing can adopt.
