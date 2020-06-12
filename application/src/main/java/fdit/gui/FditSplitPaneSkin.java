package fdit.gui;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.collect.Lists.newArrayList;

@SuppressWarnings("OverlyComplexClass")
public class FditSplitPaneSkin extends BehaviorSkinBase<SplitPane, BehaviorBase<SplitPane>> {

    private final ThreadLocal<ObservableList<Content>> contentRegions;
    private final ThreadLocal<ObservableList<ContentDivider>> contentDividers;
    private final Rectangle followingDivider = new Rectangle(10, 10);
    private boolean horizontal;
    private double delta;
    private double previousSize = -1;
    private int lastDividerUpdate;
    private boolean resize;
    private boolean checkDividerPos = true;

    public FditSplitPaneSkin(final SplitPane splitPane) {
        super(splitPane, new BehaviorBase<>(splitPane, Collections.emptyList()));
//        splitPane.setManaged(false);
        horizontal = getSkinnable().getOrientation() == Orientation.HORIZONTAL;

        contentRegions = ThreadLocal.withInitial(FXCollections::observableArrayList);
        contentDividers = ThreadLocal.withInitial(FXCollections::observableArrayList);

        int index = 0;
        for (final Node node : getSkinnable().getItems()) {
            addContent(index++, node);
        }
        initializeContentListener();

        for (final Divider divider : getSkinnable().getDividers()) {
            addDivider(divider);
        }
        getChildren().add(followingDivider);
        followingDivider.setVisible(false);
        followingDivider.getStyleClass().add("next-divider-position");
        registerChangeListener(splitPane.orientationProperty(), "ORIENTATION");
        registerChangeListener(splitPane.widthProperty(), "WIDTH");
        registerChangeListener(splitPane.heightProperty(), "HEIGHT");
    }

    private void addContent(final int index, final Node node) {
        final Content content = new Content(node);
        contentRegions.get().add(index, content);
        getChildren().add(index, content);
    }

    private void removeContent(final Node node) {
        for (final Content content : contentRegions.get()) {
            if (content.getContent().equals(node)) {
                getChildren().remove(content);
                contentRegions.get().remove(content);
                break;
            }
        }
    }

    private void initializeContentListener() {
        getSkinnable().getItems().addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.wasPermutated() || change.wasUpdated()) {
                    getChildren().clear();
                    contentRegions.get().clear();
                    int index = 0;
                    for (final Node node : change.getList()) {
                        addContent(index++, node);
                    }

                } else {
                    for (final Node node : change.getRemoved()) {
                        removeContent(node);
                    }

                    int index = change.getFrom();
                    for (final Node node : change.getAddedSubList()) {
                        addContent(index++, node);
                    }
                }
            }
            // TODO there may be a more efficient way than rebuilding all the dividers
            // everytime the list changes.
            removeAllDividers();
            for (final Divider divider : getSkinnable().getDividers()) {
                addDivider(divider);
            }
        });
    }

    private void checkDividerPosition(final ContentDivider divider, final double newPos, final double oldPos) {
        final double dividerWidth = divider.prefWidth(-1);
        final Content left = getLeft(divider);
        final Content right = getRight(divider);
        final double minLeft = getMinContent(left);
        final double minRight = getMinContent(right);
        final double maxLeft = getMaxContent(left);
        final double maxRight = getMaxContent(right);

        double previousDividerPos = 0;
        double nextDividerPos = getSize();
        final int index = contentDividers.get().indexOf(divider);

        if (index - 1 >= 0) {
            previousDividerPos = contentDividers.get().get(index - 1).getDividerPos();
            if (previousDividerPos == -1) {
                // Get the divider position if it hasn't been initialized.
                previousDividerPos = getAbsoluteDividerPos(contentDividers.get().get(index - 1));
            }
        }
        if (index + 1 < contentDividers.get().size()) {
            nextDividerPos = contentDividers.get().get(index + 1).getDividerPos();
            if (nextDividerPos == -1) {
                // Get the divider position if it hasn't been initialized.
                nextDividerPos = getAbsoluteDividerPos(contentDividers.get().get(index + 1));
            }
        }

        // Set the divider into the correct position by looking at the max and min content sizes.
        checkDividerPos = false;
        if (newPos > oldPos) {
            final double max = previousDividerPos == 0 ? maxLeft : previousDividerPos + dividerWidth + maxLeft;
            final double min = nextDividerPos - minRight - dividerWidth;
            final double stopPos = Math.min(max, min);
            if (newPos >= stopPos) {
                setAbsoluteDividerPos(divider, stopPos);
            } else {
                final double rightMax = nextDividerPos - maxRight - dividerWidth;
                if (newPos <= rightMax) {
                    setAbsoluteDividerPos(divider, rightMax);
                } else {
                    setAbsoluteDividerPos(divider, newPos);
                }
            }
        } else {
            final double max = nextDividerPos - maxRight - dividerWidth;
            final double min = previousDividerPos == 0 ? minLeft : previousDividerPos + minLeft + dividerWidth;
            final double stopPos = Math.max(max, min);
            if (newPos <= stopPos) {
                setAbsoluteDividerPos(divider, stopPos);
            } else {
                final double leftMax = previousDividerPos + maxLeft + dividerWidth;
                setAbsoluteDividerPos(divider, Math.min(newPos, leftMax));
            }
        }
        checkDividerPos = true;
    }

    private double getMaxContent(Content content) {
        if (content == null || content.getContent() == null) {
            return 0;
        } else if (horizontal) {
            return content.getContent().maxWidth(-1);
        } else {
            return content.getContent().maxHeight(-1);
        }
    }

    private double getMinContent(Content content) {
        if (content == null) {
            return 0;
        } else if (horizontal) {
            return content.minWidth(-1);
        } else {
            return content.minHeight(-1);
        }
    }

    private void addDivider(final Divider divider) {
        final ContentDivider contentDivider = new ContentDivider(divider);
        contentDivider.setInitialPos(divider.getPosition());
        contentDivider.setDividerPos(-1);
        final ChangeListener<Number> posPropertyListener = new PosPropertyListener(contentDivider);
        contentDivider.setPosPropertyListener(posPropertyListener);
        divider.positionProperty().addListener(posPropertyListener);
        initializeDivderEventHandlers(contentDivider);
        contentDividers.get().add(contentDivider);
        getChildren().add(contentDivider);
    }

    private void removeAllDividers() {
        final ListIterator<ContentDivider> dividers = contentDividers.get().listIterator();
        while (dividers.hasNext()) {
            final ContentDivider contentDivider = dividers.next();
            getChildren().remove(contentDivider);
            contentDivider.getDivider().positionProperty().removeListener(contentDivider.getPosPropertyListener());
            dividers.remove();
        }
        lastDividerUpdate = 0;
    }

    private void initializeDivderEventHandlers(final ContentDivider contentDivider) {
        // they only bubble to the skin which consumes them by default
        contentDivider.addEventHandler(MouseEvent.ANY, Event::consume);

        contentDivider.setOnMousePressed(e -> {
            followingDivider.setVisible(true);
            followingDivider.setWidth(contentDivider.getWidth());
            followingDivider.setHeight(contentDivider.getHeight());
            final Point2D localMousePosition = contentDivider.getParent().screenToLocal(e.getScreenX(), e.getScreenY());
            if (horizontal) {
                contentDivider.setInitialPos(contentDivider.getDividerPos());
                contentDivider.setPressPos(e.getSceneX());
                contentDivider.setPressPos(getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT
                        ? getSkinnable().getWidth() - e.getSceneX() : e.getSceneX());
                followingDivider.setX(localMousePosition.getX());
            } else {
                contentDivider.setInitialPos(contentDivider.getDividerPos());
                contentDivider.setPressPos(e.getSceneY());
                followingDivider.setY(localMousePosition.getY());
            }
            e.consume();
        });

        contentDivider.setOnMouseDragged(e -> {
            final Point2D localMousePosition = contentDivider.getParent().screenToLocal(e.getScreenX(), e.getScreenY());
            if (horizontal) {
                delta = getSkinnable().getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT
                        ? getSkinnable().getWidth() - e.getSceneX() : e.getSceneX();
                followingDivider.setX(localMousePosition.getX());
            } else {
                delta = e.getSceneY();
                followingDivider.setY(localMousePosition.getY());
            }
            delta -= contentDivider.getPressPos();
            e.consume();
        });

        contentDivider.setOnMouseReleased(e -> {
            setAndCheckAbsoluteDividerPos(contentDivider, Math.ceil(contentDivider.getInitialPos() + delta));
            delta = 0;
            followingDivider.setVisible(false);
            e.consume();
        });
    }

    private Content getLeft(final ContentDivider contentDivider) {
        final int index = contentDividers.get().indexOf(contentDivider);
        if (index != -1) {
            return contentRegions.get().get(index);
        }
        return null;
    }

    private Content getRight(final ContentDivider contentDivider) {
        final int index = contentDividers.get().indexOf(contentDivider);
        if (index != -1) {
            return contentRegions.get().get(index + 1);
        }
        return null;
    }

    @Override
    protected void handleControlPropertyChanged(final String property) {
        super.handleControlPropertyChanged(property);
        if ("ORIENTATION".equals(property)) {
            horizontal = getSkinnable().getOrientation() == Orientation.HORIZONTAL;
            previousSize = -1;
            for (final ContentDivider contentDivider : contentDividers.get()) {
                contentDivider.setGrabberStyle(horizontal);
            }
            getSkinnable().requestLayout();
        } else if ("WIDTH".equals(property) || "HEIGHT".equals(property)) {
            getSkinnable().requestLayout();
        }
    }

    // Value is the left edge of the divider
    private void setAbsoluteDividerPos(final ContentDivider divider, final double value) {
        if (getSkinnable().getWidth() > 0 && getSkinnable().getHeight() > 0 && divider != null) {
            final Divider paneDivider = divider.getDivider();
            divider.setDividerPos(value);
            final double size = getSize();
            if (size == 0) {
                paneDivider.setPosition(0);
            } else {
                // Adjust the position to the center of the
                // divider and convert its position to a percentage.
                final double pos = value + divider.prefWidth(-1) / 2;
                paneDivider.setPosition(pos / size);
            }
        }
    }

    // Updates the divider with the SplitPane.Divider's position
    // The value updated to SplitPane.Divider will be the center of the divider.
    // The returned position will be the left edge of the divider
    private double getAbsoluteDividerPos(final ContentDivider divider) {
        if (getSkinnable().getWidth() > 0 && getSkinnable().getHeight() > 0 && divider != null) {
            final Divider paneDivider = divider.getDivider();
            final double newPos = posToDividerPos(divider, paneDivider.getPosition());
            divider.setDividerPos(newPos);
            return newPos;
        }
        return 0;
    }

    // Returns the left edge of the divider at pos
    // Pos is the percentage location from SplitPane.Divider.
    private double posToDividerPos(final Node divider, final double pos) {
        double newPos = getSize() * pos;
        if (pos == 1) {
            newPos -= divider.prefWidth(-1);
        } else {
            newPos -= divider.prefWidth(-1) / 2;
        }
        return Math.round(newPos);
    }

    private double totalMinSize() {
        final double dividerWidth = contentDividers.get().isEmpty() ?
                0 :
                contentDividers.get().size() * contentDividers.get().get(0).prefWidth(-1);
        double minSize = 0;
        for (final Content content : contentRegions.get()) {
            if (horizontal) {
                minSize += content.minWidth(-1);
            } else {
                minSize += content.minHeight(-1);
            }
        }
        return minSize + dividerWidth;
    }

    private double getSize() {
        final SplitPane splitPane = getSkinnable();
        double size = totalMinSize();
        if (horizontal) {
            if (splitPane.getWidth() > size) {
                size = splitPane.getWidth() - snappedLeftInset() - snappedRightInset();
            }
        } else {
            if (splitPane.getHeight() > size) {
                size = splitPane.getHeight() - snappedTopInset() - snappedBottomInset();
            }
        }
        return size;
    }

    // Evenly distribute the size to the available list.
    // size is the amount to distribute.
    private double distributeTo(final Collection<Content> available, double size) {
        if (available.isEmpty()) {
            return size;
        }

        size = snapSize(size);
        int portion = (int) size / available.size();

        while (size > 0 && !available.isEmpty()) {
            for (final Content content : available) {
                final double max = Math.min(horizontal ? content.maxWidth(-1) : content.maxHeight(-1),
                        Double.MAX_VALUE);
                final double min = horizontal ? content.minWidth(-1) : content.minHeight(-1);

                // We have too much space
                if (content.getArea() >= max) {
                    content.setAvailable(content.getArea() - min);
                    break;
                }
                // Not enough space
                if (portion >= max - content.getArea()) {
                    size -= max - content.getArea();
                    content.setArea(max);
                    content.setAvailable(max - min);
                    break;
                }
                // Enough space
                content.setArea(content.getArea() + portion);
                content.setAvailable(content.getArea() - min);
                size -= portion;
                if ((int) size == 0) {
                    return size;
                }
            }

            if (available.isEmpty()) {
                // We reached the max size for everything just return
                return size;
            }
            portion = (int) size / available.size();
            final int remainder = (int) size % available.size();
            if (portion == 0 && remainder != 0) {
                portion = remainder;
            }
        }
        return size;
    }

    // Evenly distribute the size from the available list.
    // size is the amount to distribute.
    private void distributeFrom(double size, final Collection<Content> available) {
        if (available.isEmpty()) {
            return;
        }
        size = snapSize(size);
        int portion = (int) size / available.size();

        while (size > 0 && !available.isEmpty()) {
            for (final Content content : available) {
                //not enough space taking available and setting min
                if (portion >= content.getAvailable()) {
                    content.setArea(content.getArea() - content.getAvailable()); // Min size
                    size -= content.getAvailable();
                    content.setAvailable(0);
                    break;
                }
                //enough space
                content.setArea(content.getArea() - portion);
                content.setAvailable(content.getAvailable() - portion);
                size -= portion;
                if ((int) size == 0) {
                    return;
                }
            }
            if (available.isEmpty()) {
                // We reached the min size for everything just return
                return;
            }
            portion = (int) size / available.size();
            final int remainder = (int) size % available.size();
            if (portion == 0 && remainder != 0) {
                portion = remainder;
            }
        }
    }

    @SuppressWarnings("ReuseOfLocalVariable")
    private void setupContentAndDividerForLayout() {
        // Set all the value to prepare for layout
        final double dividerWidth = contentDividers.get().isEmpty() ? 0 : contentDividers.get().get(0).prefWidth(-1);
        double startX = 0;
        double startY = 0;
        for (final Content content : contentRegions.get()) {
            if (resize && !content.isResizableWithParent()) {
                content.setArea(content.getResizableWithParentArea());
            }
            content.setX(startX);
            content.setY(startY);
            if (horizontal) {
                startX += content.getArea() + dividerWidth;
            } else {
                startY += content.getArea() + dividerWidth;
            }
        }

        startX = 0;
        startY = 0;
        // The dividers are already in the correct positions.  Disable
        // checking the divider positions.
        checkDividerPos = false;
        for (int i = 0; i < contentDividers.get().size(); i++) {
            final ContentDivider contentDivider = contentDividers.get().get(i);
            final Content left = getLeft(contentDivider);
            if (horizontal) {
                startX += (left == null ? 0 : left.getArea()) + (i == 0 ? 0 : dividerWidth);
            } else {
                startY += (left == null ? 0 : left.getArea()) + (i == 0 ? 0 : dividerWidth);
            }
            contentDivider.setX(startX);
            contentDivider.setY(startY);
            setAbsoluteDividerPos(contentDivider, horizontal ? contentDivider.getX() : contentDivider.getY());
            contentDivider.posExplicit = false;
        }
        checkDividerPos = true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void layoutDividersAndContent(final double width, final double height) {
        final double paddingX = snappedLeftInset();
        final double paddingY = snappedTopInset();
        final double dividerWidth = contentDividers.get().isEmpty() ? 0 : contentDividers.get().get(0).prefWidth(-1);

        for (final Content content : contentRegions.get()) {
            if (horizontal) {
                content.setClipSize(content.getArea(), height);
                layoutInArea(content, content.getX() + paddingX, content.getY() + paddingY, content.getArea(), height,
                        0/*baseline*/, HPos.CENTER, VPos.CENTER);
            } else {
                content.setClipSize(width, content.getArea());
                layoutInArea(content, content.getX() + paddingX, content.getY() + paddingY, width, content.getArea(),
                        0/*baseline*/, HPos.CENTER, VPos.CENTER);
            }
        }
        for (final ContentDivider contentDivider : contentDividers.get()) {
            if (horizontal) {
                contentDivider.resize(dividerWidth, height);
                positionInArea(contentDivider,
                        contentDivider.getX() + paddingX,
                        contentDivider.getY() + paddingY,
                        dividerWidth,
                        height,
                        /*baseline ignored*/
                        0,
                        HPos.CENTER,
                        VPos.CENTER);
            } else {
                contentDivider.resize(width, dividerWidth);
                positionInArea(contentDivider,
                        contentDivider.getX() + paddingX,
                        contentDivider.getY() + paddingY,
                        width,
                        dividerWidth,
                        /*baseline ignored*/
                        0,
                        HPos.CENTER,
                        VPos.CENTER);
            }
        }
    }

    @Override
    protected void layoutChildren(final double x, final double y,
                                  final double w, final double h) {
        final SplitPane splitPane = getSkinnable();
        final double sw = splitPane.getWidth();
        final double sh = splitPane.getHeight();

        if (!splitPane.isVisible() ||
                (horizontal ? sw : sh) == 0 ||
                contentRegions.get().isEmpty()) {
            return;
        }

        final double dividerWidth = contentDividers.get().isEmpty() ? 0 : contentDividers.get().get(0).prefWidth(-1);

        if (!contentDividers.get().isEmpty() && previousSize != -1 && previousSize != (horizontal ? sw : sh)) {
            //This algorithm adds/subtracts a little to each panel on every resize
            final Collection<Content> resizeList = newArrayList();
            for (final Content content : contentRegions.get()) {
                if (content.isResizableWithParent()) {
                    resizeList.add(content);
                }
            }

            double delta = (horizontal ? splitPane.getWidth() : splitPane.getHeight()) - previousSize;
            final boolean growing = delta > 0;

            delta = Math.abs(delta);

            if (delta != 0 && !resizeList.isEmpty()) {
                int portion = (int) delta / resizeList.size();
                int remainder = (int) delta % resizeList.size();
                int size;
                if (portion == 0) {
                    portion = remainder;
                    size = remainder;
                    remainder = 0;
                } else {
                    size = portion * resizeList.size();
                }

                while (size > 0 && !resizeList.isEmpty()) {
                    if (growing) {
                        lastDividerUpdate++;
                    } else {
                        lastDividerUpdate--;
                        if (lastDividerUpdate < 0) {
                            lastDividerUpdate = contentRegions.get().size() - 1;
                        }
                    }
                    final int id = lastDividerUpdate % contentRegions.get().size();
                    final Content content = contentRegions.get().get(id);
                    if (content.isResizableWithParent() && resizeList.contains(content)) {
                        double area = content.getArea();
                        if (growing) {
                            final double max = horizontal ? content.maxWidth(-1) : content.maxHeight(-1);
                            if (area + portion <= max) {
                                area += portion;
                            } else {
                                resizeList.remove(content);
                                continue;
                            }
                        } else {
                            final double min = horizontal ? content.minWidth(-1) : content.minHeight(-1);
                            if (area - portion >= min) {
                                area -= portion;
                            } else {
                                resizeList.remove(content);
                                continue;
                            }
                        }
                        content.setArea(area);
                        size -= portion;
                        if (size == 0 && remainder != 0) {
                            portion = remainder;
                            size = remainder;
                            remainder = 0;
                        } else if (size == 0) {
                            break;
                        }
                    }
                }

                // If we are resizing the window save the current area into
                // resizableWithParentArea.  We use this value during layout.
                for (final Content content : contentRegions.get()) {
                    content.setResizableWithParentArea(content.getArea());
                    content.setAvailable(0);
                }
                resize = true;
            }

            previousSize = horizontal ? sw : sh;
        } else {
            previousSize = horizontal ? sw : sh;
        }

        // If the window is less than the min size we want to resize
        // proportionally
        final double minSize = totalMinSize();
        if (minSize != 0 && minSize > (horizontal ? w : h)) {
            for (int i = 0; i < contentRegions.get().size(); i++) {
                final Content content = contentRegions.get().get(i);
                final double min = horizontal ? content.minWidth(-1) : content.minHeight(-1);
                final double percentage = min / minSize;
                content.setArea(snapSpace(percentage * (horizontal ? w : h)));
                content.setAvailable(0);
            }
            setupContentAndDividerForLayout();
            layoutDividersAndContent(w, h);
            resize = false;
            return;
        }

        for (int trys = 0; trys < 10; trys++) {
            // Compute the area in between each divider.
            ContentDivider previousDivider = null;
            ContentDivider divider = null;
            for (int i = 0; i < contentRegions.get().size(); i++) {
                double space = 0;
                if (i < contentDividers.get().size()) {
                    divider = contentDividers.get().get(i);
                    if (divider.posExplicit) {
                        checkDividerPosition(divider, posToDividerPos(divider, divider.divider.getPosition()),
                                divider.getDividerPos());
                    }
                    if (i == 0) {
                        // First panel
                        space = getAbsoluteDividerPos(divider);
                    } else {
                        final double newPos = getAbsoluteDividerPos(previousDivider) + dividerWidth;
                        // Middle panels
                        if (getAbsoluteDividerPos(divider) <= getAbsoluteDividerPos(previousDivider)) {
                            // The current divider and the previous divider share the same position
                            // or the current divider position is less than the previous position.
                            // We will set the divider next to the previous divider.
                            setAndCheckAbsoluteDividerPos(divider, newPos);
                        }
                        space = getAbsoluteDividerPos(divider) - newPos;
                    }
                } else if (i == contentDividers.get().size()) {
                    // Last panel
                    space = (horizontal ? w : h) -
                            (previousDivider != null ? getAbsoluteDividerPos(previousDivider) + dividerWidth : 0);
                }
                if (!resize || divider.posExplicit) {
                    contentRegions.get().get(i).setArea(space);
                }
                previousDivider = divider;
            }

            // Compute the amount of space we have available.
            // Available is amount of space we can take from a panel before we reach its min.
            // If available is negative we don't have enough space and we will
            // proportionally take the space from the other availables.  If we have extra space
            // we will porportionally give it to the others
            double spaceRequested = 0;
            double extraSpace = 0;
            for (final Content content : contentRegions.get()) {
                double max = 0;
                double min = 0;
                if (content != null) {
                    max = horizontal ? content.maxWidth(-1) : content.maxHeight(-1);
                    min = horizontal ? content.minWidth(-1) : content.minHeight(-1);
                }

                if (content.getArea() >= max) {
                    // Add the space that needs to be distributed to the others
                    extraSpace += content.getArea() - max;
                    content.setArea(max);
                }
                content.setAvailable(content.getArea() - min);
                if (content.getAvailable() < 0) {
                    spaceRequested += content.getAvailable();
                }
            }

            spaceRequested = Math.abs(spaceRequested);

            // Add the panels where we can take space from
            final List<Content> availableList = newArrayList();
            final List<Content> storageList = newArrayList();
            final List<Content> spaceRequestor = newArrayList();
            double available = 0;
            for (final Content content : contentRegions.get()) {
                if (content.getAvailable() >= 0) {
                    available += content.getAvailable();
                    availableList.add(content);
                }

                if (resize && !content.isResizableWithParent()) {
                    // We are making the SplitPane bigger and will need to
                    // distribute the extra space.
                    if (content.getArea() >= content.getResizableWithParentArea()) {
                        extraSpace += content.getArea() - content.getResizableWithParentArea();
                    } else {
                        // We are making the SplitPane smaller and will need to
                        // distribute the space requested.
                        spaceRequested += content.getResizableWithParentArea() - content.getArea();
                    }
                    content.setAvailable(0);
                }
                // Add the panels where we can add space to;
                if (resize) {
                    if (content.isResizableWithParent()) {
                        storageList.add(content);
                    }
                } else {
                    storageList.add(content);
                }
                // List of panels that need space.
                if (content.getAvailable() < 0) {
                    spaceRequestor.add(content);
                }
            }

            if (extraSpace > 0) {
                extraSpace = distributeTo(storageList, extraSpace);
                // After distributing add any panels that may still need space to the
                // spaceRequestor list.
                spaceRequested = 0;
                spaceRequestor.clear();
                available = 0;
                availableList.clear();
                for (final Content content : contentRegions.get()) {
                    if (content.getAvailable() < 0) {
                        spaceRequested += content.getAvailable();
                        spaceRequestor.add(content);
                    } else {
                        available += content.getAvailable();
                        availableList.add(content);
                    }
                }
                spaceRequested = Math.abs(spaceRequested);
            }

            if (available >= spaceRequested) {
                for (final Content requestor : spaceRequestor) {
                    final double min = horizontal ? requestor.minWidth(-1) : requestor.minHeight(-1);
                    requestor.setArea(min);
                    requestor.setAvailable(0);
                }
                // After setting all the space requestors to their min we have to
                // redistribute the space requested to any panel that still
                // has available space.
                if (spaceRequested > 0 && !spaceRequestor.isEmpty()) {
                    distributeFrom(spaceRequested, availableList);
                }

                // Only for resizing.  We should have all the panel areas
                // available computed.  We can total them up and see
                // how much space we have left or went over and redistribute.
                if (resize) {
                    double total = 0;
                    for (final Content content : contentRegions.get()) {
                        if (content.isResizableWithParent()) {
                            total += content.getArea();
                        } else {
                            total += content.getResizableWithParentArea();
                        }
                    }
                    total += dividerWidth * contentDividers.get().size();
                    if (total < (horizontal ? w : h)) {
                        extraSpace += (horizontal ? w : h) - total;
                        distributeTo(storageList, extraSpace);
                    } else {
                        spaceRequested += total - (horizontal ? w : h);
                        distributeFrom(spaceRequested, storageList);
                    }
                }
            }

            setupContentAndDividerForLayout();

            // Check the bounds of every panel
            boolean passed = true;
            for (final Content content : contentRegions.get()) {
                final double max = horizontal ? content.maxWidth(-1) : content.maxHeight(-1);
                final double min = horizontal ? content.minWidth(-1) : content.minHeight(-1);
                if (content.getArea() < min || content.getArea() > max) {
                    passed = false;
                    break;
                }
            }
            if (passed) {
                break;
            }
        }

        layoutDividersAndContent(w, h);
        resize = false;
    }

    private void setAndCheckAbsoluteDividerPos(final ContentDivider divider, final double value) {
        final double oldPos = divider.getDividerPos();
        setAbsoluteDividerPos(divider, value);
        checkDividerPosition(divider, value, oldPos);
    }

    @Override
    protected double computeMinWidth(final double height,
                                     final double topInset,
                                     final double rightInset,
                                     final double bottomInset,
                                     final double leftInset) {
        double minWidth = 0;
        double maxMinWidth = 0;
        for (final Content content : contentRegions.get()) {
            minWidth += content.minWidth(-1);
            maxMinWidth = Math.max(maxMinWidth, content.minWidth(-1));
        }
        for (final ContentDivider contentDivider : contentDividers.get()) {
            minWidth += contentDivider.prefWidth(-1);
        }
        if (horizontal) {
            return minWidth + leftInset + rightInset;
        } else {
            return maxMinWidth + leftInset + rightInset;
        }
    }

    @Override
    protected double computeMinHeight(final double width,
                                      final double topInset,
                                      final double rightInset,
                                      final double bottomInset,
                                      final double leftInset) {
        double minHeight = 0;
        double maxMinHeight = 0;
        for (final Content content : contentRegions.get()) {
            minHeight += content.minHeight(-1);
            maxMinHeight = Math.max(maxMinHeight, content.minHeight(-1));
        }
        for (final ContentDivider contentDivider : contentDividers.get()) {
            minHeight += contentDivider.prefWidth(-1);
        }
        if (horizontal) {
            return maxMinHeight + topInset + bottomInset;
        } else {
            return minHeight + topInset + bottomInset;
        }
    }

    @Override
    protected double computePrefWidth(final double height,
                                      final double topInset,
                                      final double rightInset,
                                      final double bottomInset,
                                      final double leftInset) {
        double prefWidth = 0;
        double prefMaxWidth = 0;
        for (final Content content : contentRegions.get()) {
            prefWidth += content.prefWidth(-1);
            prefMaxWidth = Math.max(prefMaxWidth, content.prefWidth(-1));
        }
        for (final ContentDivider contentDivider : contentDividers.get()) {
            prefWidth += contentDivider.prefWidth(-1);
        }
        if (horizontal) {
            return prefWidth + leftInset + rightInset;
        } else {
            return prefMaxWidth + leftInset + rightInset;
        }
    }

    @Override
    protected double computePrefHeight(final double width,
                                       final double topInset,
                                       final double rightInset,
                                       final double bottomInset,
                                       final double leftInset) {
        double prefHeight = 0;
        double maxPrefHeight = 0;
        for (final Content content : contentRegions.get()) {
            prefHeight += content.prefHeight(-1);
            maxPrefHeight = Math.max(maxPrefHeight, content.prefHeight(-1));
        }
        for (final ContentDivider contentDivider : contentDividers.get()) {
            prefHeight += contentDivider.prefWidth(-1);
        }
        if (horizontal) {
            return maxPrefHeight + topInset + bottomInset;
        } else {
            return prefHeight + topInset + bottomInset;
        }
    }

    static class Content extends StackPane {
        private final Node content;
        private final Rectangle clipRect;
        private double x;
        private double y;
        private double area;
        private double resizableWithParentArea;
        private double available;

        Content(final Node node) {
            clipRect = new Rectangle();
            setClip(clipRect);
            content = node;
            if (node != null) {
                getChildren().add(node);
            }
            x = 0;
            y = 0;
        }

        public Node getContent() {
            return content;
        }

        public double getX() {
            return x;
        }

        public void setX(final double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(final double y) {
            this.y = y;
        }

        // This is the area of the panel.  This will be used as the
        // width/height during layout.
        public double getArea() {
            return area;
        }

        public void setArea(final double area) {
            this.area = area;
        }

        // This is the minimum available area for other panels to use
        // if they need more space.
        public double getAvailable() {
            return available;
        }

        public void setAvailable(final double available) {
            this.available = available;
        }

        public boolean isResizableWithParent() {
            return SplitPane.isResizableWithParent(content);
        }

        public double getResizableWithParentArea() {
            return resizableWithParentArea;
        }

        // This is used to save the current area during resizing when
        // isResizeableWithParent equals false.
        public void setResizableWithParentArea(final double resizableWithParentArea) {
            if (isResizableWithParent()) {
                this.resizableWithParentArea = 0;
            } else {
                this.resizableWithParentArea = resizableWithParentArea;
            }
        }

        void setClipSize(final double w, final double h) {
            clipRect.setWidth(w);
            clipRect.setHeight(h);
        }

        @Override
        protected double computeMaxWidth(final double height) {
            return snapSize(content.maxWidth(height));
        }

        @Override
        protected double computeMaxHeight(final double width) {
            return snapSize(content.maxHeight(width));
        }
    }

    // This listener is to be removed from 'removed' dividers and added to 'added' dividers
    @SuppressWarnings("NonStaticInnerClassInSecureContext")
    class PosPropertyListener implements ChangeListener<Number> {
        ContentDivider divider;

        PosPropertyListener(final ContentDivider divider) {
            this.divider = divider;
        }

        @Override
        public void changed(final ObservableValue<? extends Number> observable,
                            final Number oldValue,
                            final Number newValue) {
            if (checkDividerPos) {
                // When checking is enforced, we know that the position was set explicitly
                divider.posExplicit = true;
            }
            getSkinnable().requestLayout();
        }
    }

    @SuppressWarnings("NonStaticInnerClassInSecureContext")
    public final class ContentDivider extends StackPane {
        private final Divider divider;
        private double initialPos;
        private double dividerPos;
        private double pressPos;
        private double x;
        private double y;
        private boolean posExplicit;
        private ChangeListener<Number> listener;

        ContentDivider(final Divider divider) {
            getStyleClass().setAll("Split-pane-divider");
            this.divider = divider;
            initialPos = 0;
            dividerPos = 0;
            pressPos = 0;

            setGrabberStyle(horizontal);
        }

        public Divider getDivider() {
            return divider;
        }

        public final void setGrabberStyle(final boolean horizontal) {
            if (horizontal) {
                setCursor(Cursor.H_RESIZE);
            } else {
                setCursor(Cursor.V_RESIZE);
            }
        }

        public double getInitialPos() {
            return initialPos;
        }

        public void setInitialPos(final double initialPos) {
            this.initialPos = initialPos;
        }

        public double getDividerPos() {
            return dividerPos;
        }

        public void setDividerPos(final double dividerPos) {
            this.dividerPos = dividerPos;
        }

        public double getPressPos() {
            return pressPos;
        }

        public void setPressPos(final double pressPos) {
            this.pressPos = pressPos;
        }

        public double getX() {
            return x;
        }

        public void setX(final double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(final double y) {
            this.y = y;
        }

        public ChangeListener<Number> getPosPropertyListener() {
            return listener;
        }

        public void setPosPropertyListener(final ChangeListener<Number> listener) {
            this.listener = listener;
        }

        @Override
        protected double computeMinWidth(final double height) {
            return computePrefWidth(height);
        }

        @Override
        protected double computeMinHeight(final double width) {
            return computePrefHeight(width);
        }

        @Override
        protected double computePrefWidth(final double height) {
            return snappedLeftInset() + snappedRightInset();
        }

        @Override
        protected double computePrefHeight(final double width) {
            return snappedTopInset() + snappedBottomInset();
        }

        @Override
        protected double computeMaxWidth(final double height) {
            return computePrefWidth(height);
        }

        @Override
        protected double computeMaxHeight(final double width) {
            return computePrefHeight(width);
        }

        @Override
        protected void layoutChildren() {
        }
    }
}
