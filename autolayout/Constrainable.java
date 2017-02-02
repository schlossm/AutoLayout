package uikit.autolayout;

public interface Constrainable
{
	void addConstraint(LayoutConstraint constraint);

	LayoutConstraint[] allConstraints();

	void layoutSubviews();

	int compressionResistanceWidth();
	int compressionResistanceHeight();
	void setCompressionResistanceWidth(int compressionResistanceWidth);
	void setCompressionResistanceHeight(int compressionResistanceHeight);
}
