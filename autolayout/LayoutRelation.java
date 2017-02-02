package uikit.autolayout;

/**
 * Created by michaelschloss on 1/26/17.
 *
 * The relation between the first attribute and the modified second attribute in a constraint.
 */
public enum LayoutRelation
{
	lessThanOrEqual, equal, greaterThanOrEqual;

	@Override
	public String toString()
	{
		if (this == lessThanOrEqual)
		{
			return "<=";
		}
		else if (this == equal)
		{
			return "==";
		}
		else if (this == greaterThanOrEqual)
		{
			return ">=";
		}
		return "NULL";
	}
}
