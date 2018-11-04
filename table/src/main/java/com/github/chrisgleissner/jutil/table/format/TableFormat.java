package com.github.chrisgleissner.jutil.table.format;

public interface TableFormat {
    char getTopLeftCorner();

    char getTopRightCorner();

    char getBottomLeftCorner();

    char getBottomRightCorner();

    char getTopEdgeBorderDivider();

    char getBottomEdgeBorderDivider();

    char getLeftEdgeBorderDivider(boolean underHeaders);

    char getRightEdgeBorderDivider(boolean underHeaders);

    char getHorizontalBorderFill(boolean edge, boolean underHeaders);

    char getVerticalBorderFill(boolean edge);

    char getCross(boolean underHeaders, boolean emptyData);
}
