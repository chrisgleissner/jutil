package com.github.chrisgleissner.jutil.table.format;

/**
 * @author JakeWharton
 * @author MitchTalmadge
 */
public class Utf8TableFormat implements TableFormat {

    @Override
    public char getTopLeftCorner() {
        return '╔';
    }

    @Override
    public char getTopRightCorner() {
        return '╗';
    }

    @Override
    public char getBottomLeftCorner() {
        return '╚';
    }

    @Override
    public char getBottomRightCorner() {
        return '╝';
    }

    @Override
    public char getTopEdgeBorderDivider() {
        return '╤';
    }

    @Override
    public char getBottomEdgeBorderDivider() {
        return '╧';
    }

    @Override
    public char getLeftEdgeBorderDivider(boolean underHeaders) {
        return underHeaders ? '╠' : '╟';
    }

    @Override
    public char getRightEdgeBorderDivider(boolean underHeaders) {
        return underHeaders ? '╣' : '╢';
    }

    @Override
    public char getHorizontalBorderFill(boolean edge, boolean underHeaders) {
        return edge || underHeaders ? '═' : '─';
    }

    @Override
    public char getVerticalBorderFill(boolean edge) {
        return edge ? '║' : '│';
    }

    @Override
    public char getCross(boolean underHeaders, boolean emptyData) {
        if (underHeaders)
            return emptyData ? '╧' : '╪';
        else
            return '┼';
    }
}
