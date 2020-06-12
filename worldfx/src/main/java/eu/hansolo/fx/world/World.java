/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.world;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.*;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import static eu.hansolo.fx.world.WorldProjectionUtils.toXPosition;
import static eu.hansolo.fx.world.WorldProjectionUtils.toYPosition;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

@DefaultProperty("children")
public class World extends Region {

    public static final double MAP_WIDTH = 1009;
    public static final double MAP_HEIGHT = 665;
    protected static final double ASPECT_RATIO = MAP_HEIGHT / MAP_WIDTH;
    private static final StyleablePropertyFactory<World> FACTORY = new StyleablePropertyFactory<>(
            Region.getClassCssMetaData());
    private static final String WORLD_JSON = "eu/hansolo/fx/world/world_md.json";
    private static final double MINIMUM_WIDTH = 100;
    private static final double MINIMUM_HEIGHT = 66;
    private static final double MAXIMUM_WIDTH = 2018;
    private static final double MAXIMUM_HEIGHT = 1330;
    private static final CssMetaData<World, Color> BACKGROUND_COLOR = FACTORY.createColorCssMetaData(
            "-background-color",
            s -> s.backgroundColor,
            Color.web("#3f3f4f"),
            false);
    private static final CssMetaData<World, Color> FILL_COLOR = FACTORY.createColorCssMetaData(
            "-fill-color",
            s -> s.fillColor,
            Color.web("#d9d9dc"),
            false);
    private static final CssMetaData<World, Color> STROKE_COLOR = FACTORY.createColorCssMetaData(
            "-stroke-color",
            s -> s.strokeColor,
            Color.BLACK,
            false);
    private static final CssMetaData<World, Color> HOVER_COLOR = FACTORY.createColorCssMetaData(
            "-hover-color",
            s -> s.hoverColor,
            Color.web("#456acf"),
            false);
    private static final CssMetaData<World, Color> PRESSED_COLOR = FACTORY.createColorCssMetaData(
            "-pressed-color",
            s -> s.pressedColor,
            Color.web("#789dff"),
            false);
    private static final CssMetaData<World, Color> SELECTED_COLOR = FACTORY.createColorCssMetaData(
            "-selected-color",
            s -> s.selectedColor,
            Color.web("#9dff78"),
            false);
    private static final CssMetaData<World, Color> LOCATION_COLOR = FACTORY.createColorCssMetaData(
            "-location-color",
            s -> s.locationColor,
            Color.web("#ff0000"),
            false);
    public static double MIN_CLAMP = 1.0;
    public static double MAX_CLAMP = 100.0;
    static double MAP_OFFSET_X = -MAP_WIDTH * 0.0285;
    static double MAP_OFFSET_Y = MAP_HEIGHT * 0.195;
    private final StyleableProperty<Color> backgroundColor;
    private final StyleableProperty<Color> fillColor;
    private final StyleableProperty<Color> strokeColor;
    private final StyleableProperty<Color> hoverColor;
    private final StyleableProperty<Color> pressedColor;
    private final StyleableProperty<Color> selectedColor;
    private final StyleableProperty<Color> locationColor;
    protected double width;
    protected double height;
    protected Ikon locationIconCode;
    protected Pane pane;
    protected Group group;
    protected Multimap<String, CountryPath> countryPaths;
    protected ObservableMap<Location, Shape> locations;
    // internal event handlers
    protected EventHandler<MouseEvent> _mouseEnterHandler;
    protected EventHandler<MouseEvent> _mousePressHandler;
    protected EventHandler<MouseEvent> _mouseExitHandler;
    private BooleanProperty hoverEnabled;
    private BooleanProperty selectionEnabled;
    private ObjectProperty<Country> selectedCountry;
    private BooleanProperty zoomEnabled;
    private DoubleProperty scaleFactor;
    private Properties resolutionProperties;
    private EventHandler<ScrollEvent> _scrollEventHandler;
    // exposed event handlers
    private EventHandler<MouseEvent> mouseEnterHandler;
    private EventHandler<MouseEvent> mousePressHandler;
    private EventHandler<MouseEvent> mouseExitHandler;
    /*---------Added var : Clément -------*/
    private double mouseClicPosX;
    private double mouseClicPosY;
    private EventHandler<MouseEvent> _mouseDragHandler;
    private EventHandler<MouseEvent> mouseDragHandler;

    // ******************** Constructors **************************************
    public World() {
        backgroundColor = new StyleableObjectProperty<Color>(BACKGROUND_COLOR.getInitialValue(World.this)) {
            @Override
            protected void invalidated() {
                setBackground(new Background(new BackgroundFill(get(), CornerRadii.EMPTY, Insets.EMPTY)));
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "backgroundColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                return BACKGROUND_COLOR;
            }
        };
        fillColor = new StyleableObjectProperty<Color>(FILL_COLOR.getInitialValue(World.this)) {
            @Override
            protected void invalidated() {
                setFillAndStroke();
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "fillColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                return FILL_COLOR;
            }
        };
        strokeColor = new StyleableObjectProperty<Color>(STROKE_COLOR.getInitialValue(World.this)) {
            @Override
            protected void invalidated() {
                setFillAndStroke();
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "strokeColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                return STROKE_COLOR;
            }
        };
        hoverColor = new StyleableObjectProperty<Color>(HOVER_COLOR.getInitialValue(World.this)) {
            @Override
            protected void invalidated() {
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "hoverColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                return HOVER_COLOR;
            }
        };
        pressedColor = new StyleableObjectProperty<Color>(PRESSED_COLOR.getInitialValue(this)) {
            @Override
            protected void invalidated() {
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "pressedColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                return PRESSED_COLOR;
            }
        };
        selectedColor = new StyleableObjectProperty<Color>(SELECTED_COLOR.getInitialValue(this)) {
            @Override
            protected void invalidated() {
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "selectedColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                return SELECTED_COLOR;
            }
        };
        locationColor = new StyleableObjectProperty<Color>(LOCATION_COLOR.getInitialValue(this)) {
            @Override
            protected void invalidated() {
                locations.forEach((location, shape) -> shape.setFill(null == location.getColor() ?
                        get() :
                        location.getColor()));
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "locationColor";
            }

            @Override
            public CssMetaData<? extends Styleable, Color> getCssMetaData() {
                return LOCATION_COLOR;
            }
        };
        hoverEnabled = new BooleanPropertyBase(true) {
            @Override
            protected void invalidated() {
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "hoverEnabled";
            }
        };
        selectionEnabled = new BooleanPropertyBase(false) {
            @Override
            protected void invalidated() {
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "selectionEnabled";
            }
        };
        selectedCountry = new ObjectPropertyBase<Country>() {
            @Override
            protected void invalidated() {
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "selectedCountry";
            }
        };
        zoomEnabled = new BooleanPropertyBase(false) {
            @Override
            protected void invalidated() {
                if (null == getScene()) {
                    return;
                }
                if (get()) {
                    getParent().addEventFilter(ScrollEvent.ANY, _scrollEventHandler);
                } else {
                    getParent().removeEventFilter(ScrollEvent.ANY, _scrollEventHandler);
                }
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "zoomEnabled";
            }
        };
        scaleFactor = new DoublePropertyBase(1.0) {
            @Override
            protected void invalidated() {
                if (isZoomEnabled()) {
                    setScaleX(get());
                    setScaleY(get());
                }
            }

            @Override
            public Object getBean() {
                return World.this;
            }

            @Override
            public String getName() {
                return "scaleFactor";
            }
        };
        countryPaths = createCountryPaths();
        locations = FXCollections.observableHashMap();

        locationIconCode = MaterialDesign.MDI_CHECKBOX_BLANK_CIRCLE;
        pane = new Pane();
        group = new Group();
        group.setAutoSizeChildren(false);
        _mouseDragHandler = evt -> handleMouseEventDragAndDrop(evt);
        _mousePressHandler = evt -> handleMouseEventDragAndDrop(evt);
        _scrollEventHandler = this::handleMouseScrollHandler;

        initGraphics();
        registerListeners();
    }

    public static double[] XYToLatLon(final double X, final double Y) {
        final double LONGITUDE = (X - MAP_OFFSET_X) * 360 / MAP_WIDTH - 180;

        final double LATITUDE =
                Math.toDegrees(
                        (
                                Math.atan(
                                        Math.exp(
                                                -(2 * Math.PI * (Y - MAP_OFFSET_Y - MAP_HEIGHT / 2))
                                                        / MAP_WIDTH
                                        ))
                                        - Math.PI / 4
                        ) * 2);
        return new double[]{roundDouble(LATITUDE, 2), roundDouble(LONGITUDE, 2)};
    }

    private static double roundDouble(final double value, final int precision) {
        return Math.round(value * 100) / Math.pow(10, precision);
    }

    private static Multimap<String, CountryPath> createCountryPaths() {
        final Multimap<String, CountryPath> countryPaths = HashMultimap.create();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final InputStream worldInputStream = loader.getResourceAsStream(WORLD_JSON);
        final Reader inputStreamReader = new InputStreamReader(worldInputStream);
        final JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);
        jsonObject.get("countries").getAsJsonArray().forEach(jsonElement -> {
            final JsonObject countryObject = jsonElement.getAsJsonObject();
            final String country = countryObject.get("country").getAsString();
            final JsonArray allCoordinates = countryObject.get("coordinates").getAsJsonArray();
            allCoordinates.forEach(zoneCoordinatesArray -> {
                final CountryPath countryPath = new CountryPath(country);
                zoneCoordinatesArray.getAsJsonArray().forEach(coordinates -> {
                    final JsonArray coordinatesArray = coordinates.getAsJsonArray();
                    final double latitude = coordinatesArray.get(0).getAsDouble();
                    final double longitude = coordinatesArray.get(1).getAsDouble();
                    countryPath.getPoints().add(toXPosition(latitude));
                    countryPath.getPoints().add(toYPosition(longitude));
                });
                countryPaths.put(country, countryPath);
            });
        });
        return countryPaths;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    // ******************** Initialization ************************************
    protected void initGraphics() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
                Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(MAP_WIDTH, MAP_HEIGHT);
            }
        }

        getStyleClass().add("world");

        Color fill = getFillColor();
        Color stroke = getStrokeColor();

        for (final Entry<String, CountryPath> entry : countryPaths.entries()) {
            final Country country = Country.valueOf(entry.getKey());
            final CountryPath countryPath = entry.getValue();
            countryPath.setFill(country.getColor() == null ? fill : country.getColor());
            countryPath.setStroke(stroke);
            countryPath.setStrokeWidth(0.05);
            countryPath.setOnMouseEntered(new WeakEventHandler<>(_mouseEnterHandler));
            countryPath.setOnMouseExited(new WeakEventHandler<>(_mouseExitHandler));
            pane.getChildren().add(countryPath);
        }
        group.getChildren().add(pane);
        getChildren().setAll(group);

        this.setOnMouseDragged(new WeakEventHandler<>(_mouseDragHandler));
        this.setOnMousePressed(new WeakEventHandler<>(_mousePressHandler));
        setBackground(new Background(new BackgroundFill(getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        sceneProperty().addListener(o -> {
            if (!locations.isEmpty()) {
                addShapesToScene(locations.values());
            }

            if (isZoomEnabled()) {
                getParent().addEventFilter(ScrollEvent.ANY, new WeakEventHandler<>(_scrollEventHandler));
            }

            locations.addListener((MapChangeListener<Location, Shape>) CHANGE -> {
                if (CHANGE.wasAdded()) {
                    addShapesToScene(CHANGE.getValueAdded());
                } else if (CHANGE.wasRemoved()) {
                    Platform.runLater(() -> pane.getChildren().remove(CHANGE.getValueRemoved()));
                }
            });
        });
    }

    // ******************** Methods *******************************************
    @Override
    protected double computeMinWidth(final double HEIGHT) {
        return MINIMUM_WIDTH;
    }

    @Override
    protected double computeMinHeight(final double WIDTH) {
        return MINIMUM_HEIGHT;
    }

    @Override
    protected double computePrefWidth(final double HEIGHT) {
        return super.computePrefWidth(HEIGHT);
    }

    @Override
    protected double computePrefHeight(final double WIDTH) {
        return super.computePrefHeight(WIDTH);
    }

    @Override
    protected double computeMaxWidth(final double HEIGHT) {
        return MAXIMUM_WIDTH;
    }

    @Override
    protected double computeMaxHeight(final double WIDTH) {
        return MAXIMUM_HEIGHT;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public void setMouseEnterHandler(final EventHandler<MouseEvent> HANDLER) {
        mouseEnterHandler = HANDLER;
    }

    public void setMouseDragHandler(final EventHandler<MouseEvent> HANDLER) {
        mouseDragHandler = HANDLER;
    }

    public void setMousePressHandler(final EventHandler<MouseEvent> HANDLER) {
        mousePressHandler = HANDLER;
    }

    public void setMouseExitHandler(final EventHandler<MouseEvent> HANDLER) {
        mouseExitHandler = HANDLER;
    }

    public Color getBackgroundColor() {
        return backgroundColor.getValue();
    }

    public void setBackgroundColor(final Color COLOR) {
        backgroundColor.setValue(COLOR);
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return (ObjectProperty<Color>) backgroundColor;
    }

    public Color getFillColor() {
        return fillColor.getValue();
    }

    public void setFillColor(final Color COLOR) {
        fillColor.setValue(COLOR);
    }

    public ObjectProperty<Color> fillColorProperty() {
        return (ObjectProperty<Color>) fillColor;
    }

    public Color getStrokeColor() {
        return strokeColor.getValue();
    }

    public void setStrokeColor(final Color COLOR) {
        strokeColor.setValue(COLOR);
    }

    public ObjectProperty<Color> strokeColorProperty() {
        return (ObjectProperty<Color>) strokeColor;
    }

    public Color getHoverColor() {
        return hoverColor.getValue();
    }

    public void setHoverColor(final Color COLOR) {
        hoverColor.setValue(COLOR);
    }

    public ObjectProperty<Color> hoverColorProperty() {
        return (ObjectProperty<Color>) hoverColor;
    }

    public Color getPressedColor() {
        return pressedColor.getValue();
    }

    public void setPressedColor(final Color COLOR) {
        pressedColor.setValue(COLOR);
    }

    public ObjectProperty<Color> pressedColorProperty() {
        return (ObjectProperty<Color>) pressedColor;
    }

    public Color getSelectedColor() {
        return selectedColor.getValue();
    }

    public void setSelectedColor(final Color COLOR) {
        selectedColor.setValue(COLOR);
    }

    public ObjectProperty<Color> selectedColorProperty() {
        return (ObjectProperty<Color>) selectedColor;
    }

    public Color getLocationColor() {
        return locationColor.getValue();
    }

    public void setLocationColor(final Color COLOR) {
        locationColor.setValue(COLOR);
    }

    public ObjectProperty<Color> locationColorProperty() {
        return (ObjectProperty<Color>) locationColor;
    }

    public boolean isHoverEnabled() {
        return hoverEnabled.get();
    }

    public void setHoverEnabled(final boolean ENABLED) {
        hoverEnabled.set(ENABLED);
    }

    public BooleanProperty hoverEnabledProperty() {
        return hoverEnabled;
    }

    public boolean isSelectionEnabled() {
        return selectionEnabled.get();
    }

    public void setSelectionEnabled(final boolean ENABLED) {
        selectionEnabled.set(ENABLED);
    }

    public BooleanProperty selectionEnabledProperty() {
        return selectionEnabled;
    }

    public Country getSelectedCountry() {
        return selectedCountry.get();
    }

    public void setSelectedCountry(final Country COUNTRY) {
        selectedCountry.set(COUNTRY);
    }

    public ObjectProperty<Country> selectedCountryProperty() {
        return selectedCountry;
    }

    public boolean isZoomEnabled() {
        return zoomEnabled.get();
    }

    public void setZoomEnabled(final boolean ENABLED) {
        zoomEnabled.set(ENABLED);
    }

    public BooleanProperty zoomEnabledProperty() {
        return zoomEnabled;
    }

    public double getScaleFactor() {
        return scaleFactor.get();
    }

    public void setScaleFactor(final double FACTOR) {
        scaleFactor.set(FACTOR);
    }

    public DoubleProperty scaleFactorProperty() {
        return scaleFactor;
    }

    public void resetZoom() {
        setScaleFactor(1.0);
        setTranslateX(0);
        setTranslateY(0);
        group.setTranslateX(0);
        group.setTranslateY(0);
    }

    public Ikon getLocationIconCode() {
        return locationIconCode;
    }

    public void setLocationIconCode(final Ikon ICON_CODE) {
        locationIconCode = ICON_CODE;
    }

    public void addLocation(final Location LOCATION) {
        double x = toXPosition(LOCATION.getLongitude());
        double y = WorldProjectionUtils.toYPosition(LOCATION.getLatitude());

        FontIcon locationIcon = new FontIcon(null == LOCATION.getIconCode() ?
                locationIconCode :
                LOCATION.getIconCode());
        locationIcon.setScaleX(0.25);
        locationIcon.setScaleY(0.25);

        locationIcon.setIconSize(LOCATION.getIconSize());
        locationIcon.setTextOrigin(VPos.CENTER);
        locationIcon.setIconColor(null == LOCATION.getColor() ? getLocationColor() : LOCATION.getColor());
        locationIcon.setX(x - LOCATION.getIconSize() * 0.5);
        locationIcon.setY(y - LOCATION.getIconSize() * 0.5);

        StringBuilder tooltipBuilder = new StringBuilder();
        if (!LOCATION.getName().isEmpty()) {
            tooltipBuilder.append(LOCATION.getName());
        }
        if (!LOCATION.getInfo().isEmpty()) {
            tooltipBuilder.append("\n").append(LOCATION.getInfo());
        }
        String tooltipText = tooltipBuilder.toString();
        if (!tooltipText.isEmpty()) {
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setFont(Font.font(10));
            Tooltip.install(locationIcon, tooltip);
        }

        if (null != LOCATION.getMouseEnterHandler()) {
            locationIcon.setOnMouseEntered(new WeakEventHandler<>(LOCATION.getMouseEnterHandler()));
        }
        if (null != LOCATION.getMousePressHandler()) {
            locationIcon.setOnMousePressed(new WeakEventHandler<>(LOCATION.getMousePressHandler()));
        }
        if (null != LOCATION.getMouseReleaseHandler()) {
            locationIcon.setOnMouseReleased(new WeakEventHandler<>(LOCATION.getMouseReleaseHandler()));
        }
        if (null != LOCATION.getMouseExitHandler()) {
            locationIcon.setOnMouseExited(new WeakEventHandler<>(LOCATION.getMouseExitHandler()));
        }
        locations.put(LOCATION, locationIcon);
    }

    public void removeLocation(final Location LOCATION) {
        locations.remove(LOCATION);
    }

    public void addLocations(final Location... LOCATIONS) {
        for (Location location : LOCATIONS) {
            addLocation(location);
        }
    }

    public void clearLocations() {
        locations.clear();
    }

    public void showLocations(final boolean SHOW) {
        for (Shape shape : locations.values()) {
            shape.setManaged(SHOW);
            shape.setVisible(SHOW);
        }
    }

    public void zoomToCountry(final Country COUNTRY) {
        if (!isZoomEnabled()) {
            return;
        }
        if (null != getSelectedCountry()) {
            setCountryFillAndStroke(getSelectedCountry(), getFillColor(), getStrokeColor());
        }
        zoomToArea(getBounds(COUNTRY));
    }

    public double[] getBounds(final Country... COUNTRIES) {
        return getBounds(Arrays.asList(COUNTRIES));
    }

    protected double[] getBounds(final List<Country> COUNTRIES) {
        double upperLeftX = MAP_WIDTH;
        double upperLeftY = MAP_HEIGHT;
        double lowerRightX = 0;
        double lowerRightY = 0;
        for (Country country : COUNTRIES) {
            final Collection<CountryPath> paths = this.countryPaths.get(country.getName());
            for (final CountryPath path : paths) {
                Bounds bounds = path.getLayoutBounds();
                upperLeftX = Math.min(bounds.getMinX(), upperLeftX);
                upperLeftY = Math.min(bounds.getMinY(), upperLeftY);
                lowerRightX = Math.max(bounds.getMaxX(), lowerRightX);
                lowerRightY = Math.max(bounds.getMaxY(), lowerRightY);
            }
        }
        return new double[]{upperLeftX, upperLeftY, lowerRightX, lowerRightY};
    }

    private void zoomToArea(final double[] BOUNDS) {
        group.setTranslateX(0);
        group.setTranslateY(0);
        double areaWidth = BOUNDS[2] - BOUNDS[0];
        double areaHeight = BOUNDS[3] - BOUNDS[1];
        double areaCenterX = BOUNDS[0] + areaWidth * 0.5;
        double areaCenterY = BOUNDS[1] + areaHeight * 0.5;
        Orientation orientation = areaWidth < areaHeight ? Orientation.VERTICAL : Orientation.HORIZONTAL;
        double sf = 1.0;
        switch (orientation) {
            case VERTICAL:
                sf = clamp(1.0, 10.0, 1 / (areaHeight / height));
                break;
            case HORIZONTAL:
                sf = clamp(1.0, 10.0, 1 / (areaWidth / width));
                break;
        }

        /*
        Rectangle bounds = new Rectangle(BOUNDS[0], BOUNDS[1], areaWidth, areaHeight);
        bounds.setFill(Color.TRANSPARENT);
        bounds.setStroke(Color.RED);
        bounds.setStrokeWidth(0.5);
        bounds.setMouseTransparent(true);
        group.getChildren().add(bounds);
        */

        setScaleFactor(sf);
        group.setTranslateX(width * 0.5 - (areaCenterX));
        group.setTranslateY(height * 0.5 - (areaCenterY));
    }

    public void setPivot(final double X, final double Y) {
        setTranslateX(getTranslateX() - X);
        setTranslateY(getTranslateY() - Y);
    }

    /*-- Handler for the drag and drop --*/
    private void handleMouseEventDragAndDrop(final MouseEvent EVENT) {
        final EventType TYPE = EVENT.getEventType();
        if (MOUSE_PRESSED == TYPE) {
            mouseClicPosX = EVENT.getX();
            mouseClicPosY = EVENT.getY();
        } else if (MOUSE_DRAGGED == TYPE) {
            setTranslateX(getTranslateX() + (EVENT.getX() - mouseClicPosX));
            setTranslateY(getTranslateY() + (EVENT.getY() - mouseClicPosY));
        }
    }

    private void handleMouseScrollHandler(final ScrollEvent EVENT) {
        if (group.getTranslateX() != 0 || group.getTranslateY() != 0) {
            resetZoom();
        }
        double delta = 1.2;
        double scale = getScaleFactor();
        double oldScale = scale;
        scale = EVENT.getDeltaY() < 0 ? scale / delta : scale * delta;
        scale = clamp(MIN_CLAMP, MAX_CLAMP, scale);
        double factor = (scale / oldScale) - 1;
        if (Double.compare(1, getScaleFactor()) == 0) {
            resetZoom();
        }
        double deltaX = (EVENT.getX() - (getBoundsInParent().getWidth() / 2 + getBoundsInParent().getMinX()));
        double deltaY = (EVENT.getY() - (getBoundsInParent().getHeight() / 2 + getBoundsInParent().getMinY()));
        setScaleFactor(scale);
        setPivot(deltaX * factor, deltaY * factor);
        EVENT.consume();
    }

    private void setFillAndStroke() {
        countryPaths.keySet().forEach(name -> {
            Country country = Country.valueOf(name);
            setCountryFillAndStroke(country,
                    null == country.getColor() ? getFillColor() : country.getColor(),
                    getStrokeColor());
        });
    }

    public void setCountryFillAndStroke(final Country COUNTRY, final Color FILL, final Color STROKE) {
        for (CountryPath path : countryPaths.get(COUNTRY.getName())) {
            path.setFill(FILL);
            path.setStroke(STROKE);
        }
    }

    private void addShapesToScene(final Shape... SHAPES) {
        addShapesToScene(Arrays.asList(SHAPES));
    }

    private void addShapesToScene(final Collection<Shape> SHAPES) {
        if (null == getScene()) {
            return;
        }
        Platform.runLater(() -> pane.getChildren().addAll(SHAPES));
    }

    public double clamp(final double MIN, final double MAX, final double VALUE) {
        if (VALUE < MIN) {
            return MIN;
        }
        if (VALUE > MAX) {
            return MAX;
        }
        return VALUE;
    }

    private Properties readProperties(final String FILE_NAME) {
        final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();
        final Properties PROPERTIES = new Properties();
        try (InputStream resourceStream = LOADER.getResourceAsStream(FILE_NAME)) {
            PROPERTIES.load(resourceStream);
        } catch (IOException exception) {
            System.err.println(exception);
        }
        return PROPERTIES;
    }

    // ******************** Style related *************************************
    @Override
    public String getUserAgentStylesheet() {
        return World.class.getResource("world.css").toExternalForm();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    // ******************** Resizing ******************************************
    protected void resize() {
        width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height = getHeight() - getInsets().getTop() - getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setCache(true);
            pane.setCacheHint(CacheHint.SCALE);

            pane.setScaleX(width / MAP_WIDTH);
            pane.setScaleY(height / MAP_HEIGHT);

            group.resize(width, height);
            group.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            pane.setCache(false);
        }
    }

}
