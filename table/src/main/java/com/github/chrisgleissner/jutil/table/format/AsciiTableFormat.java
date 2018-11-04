package com.github.chrisgleissner.jutil.table.format;

/**
 * @author bitsofinfo
 * @author MitchTalmadge
 */
public class AsciiTableFormat implements TableFormat {

    @Override
    public char getTopLeftCorner() {
        return '+';
    }

    @Override
    public char getTopRightCorner() {
        return '+';
    }

    @Override
    public char getBottomLeftCorner() {
        return '+';
    }

    @Override
    public char getBottomRightCorner() {
        return '+';
    }

    @Override
    public char getTopEdgeBorderDivider() {
        return '+';
    }

    @Override
    public char getBottomEdgeBorderDivider() {
        return '+';
    }

    @Override
    public char getLeftEdgeBorderDivider(boolean underHeaders) {
        return '|';
    }

    @Override
    public char getRightEdgeBorderDivider(boolean underHeaders) {
        return '|';
    }

    @Override
    public char getHorizontalBorderFill(boolean edge, boolean underHeaders) {
        return edge || underHeaders ? '=' : '-';
    }

    @Override
    public char getVerticalBorderFill(boolean edge) {
        return '|';
    }

    @Override
    public char getCross(boolean underHeaders, boolean emptyData) {
        return emptyData ? '=' : '|';
    }
}
