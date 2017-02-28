package uikit.autolayout;

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
