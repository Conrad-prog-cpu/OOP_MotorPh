package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for laying out components in a compact grid using SpringLayout.
 * - Computes per-column widths and per-row heights (better than uniform cells).
 * - Defensive checks to avoid runtime errors.
 */
public final class SpringUtilities {

    private SpringUtilities() {} // utility class

    public static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {

        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("rows and cols must be > 0");
        }

        LayoutManager lm = parent.getLayout();
        if (!(lm instanceof SpringLayout layout)) {
            throw new IllegalArgumentException("Container must use SpringLayout.");
        }

        int expected = rows * cols;
        int count = parent.getComponentCount();

        if (count < expected) {
            throw new IllegalArgumentException(
                    "Not enough components. Expected " + expected + " but found " + count + "."
            );
        }
        // If you prefer ignoring extras instead of throwing, keep this line:
        // int countToLayout = Math.min(count, expected);
        int countToLayout = expected;

        Spring xPadSpring = Spring.constant(xPad);
        Spring yPadSpring = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);

        // ----- compute max width per column -----
        Spring[] colWidth = new Spring[cols];
        for (int c = 0; c < cols; c++) colWidth[c] = Spring.constant(0);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int i = r * cols + c;
                if (i >= countToLayout) break;

                SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
                colWidth[c] = Spring.max(colWidth[c], cons.getWidth());
            }
        }

        // ----- compute max height per row -----
        Spring[] rowHeight = new Spring[rows];
        for (int r = 0; r < rows; r++) rowHeight[r] = Spring.constant(0);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int i = r * cols + c;
                if (i >= countToLayout) break;

                SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
                rowHeight[r] = Spring.max(rowHeight[r], cons.getHeight());
            }
        }

        // ----- set bounds for each cell -----
        Spring y = initialYSpring;
        for (int r = 0; r < rows; r++) {
            Spring x = initialXSpring;

            for (int c = 0; c < cols; c++) {
                int i = r * cols + c;
                if (i >= countToLayout) break;

                SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

                cons.setX(x);
                cons.setY(y);
                cons.setWidth(colWidth[c]);
                cons.setHeight(rowHeight[r]);

                x = Spring.sum(x, Spring.sum(colWidth[c], xPadSpring));
            }

            y = Spring.sum(y, Spring.sum(rowHeight[r], yPadSpring));
        }

        // ----- set parent container size -----
        SpringLayout.Constraints parentCons = layout.getConstraints(parent);

        // total width = initialX + sum(colWidths) + xPad*(cols-1)
        Spring totalWidth = Spring.constant(0);
        for (int c = 0; c < cols; c++) {
            totalWidth = Spring.sum(totalWidth, colWidth[c]);
        }
        totalWidth = Spring.sum(totalWidth, Spring.constant(xPad * (cols - 1)));
        totalWidth = Spring.sum(totalWidth, initialXSpring);

        // total height = initialY + sum(rowHeights) + yPad*(rows-1)
        Spring totalHeight = Spring.constant(0);
        for (int r = 0; r < rows; r++) {
            totalHeight = Spring.sum(totalHeight, rowHeight[r]);
        }
        totalHeight = Spring.sum(totalHeight, Spring.constant(yPad * (rows - 1)));
        totalHeight = Spring.sum(totalHeight, initialYSpring);

        parentCons.setConstraint(SpringLayout.EAST, totalWidth);
        parentCons.setConstraint(SpringLayout.SOUTH, totalHeight);
    }
}
