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
		if (this == top)
		{
			return "top";
		}
		else if (this == bottom)
		{
			return "bottom";
		}
		else if (this == leading)
		{
			return "leading";
		}
		else if (this == trailing)
		{
			return "trailing";
		}
		else if (this == width)
		{
			return "width";
		}
		else if (this == height)
		{
			return "height";
		}
		else if (this == centerX)
		{
			return "centerX";
		}
		else if (this == centerY)
		{
			return "centerY";
		}
		return "NULL";
	}
}
