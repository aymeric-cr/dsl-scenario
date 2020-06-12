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

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;

import java.util.HashMap;


/**
 * Created by hansolo on 21.11.16.
 */
public class WorldBuilder<B extends eu.hansolo.fx.world.WorldBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    public WorldBuilder() {
    }


    // ******************** Methods *******************************************
    public static final eu.hansolo.fx.world.WorldBuilder create() {
        return new eu.hansolo.fx.world.WorldBuilder();
    }

    public final B backgroundColor(final Color COLOR) {
        properties.put("backgroundColor", new SimpleObjectProperty<>(COLOR));
        return (B) this;
    }

    public final B fillColor(final Color COLOR) {
        properties.put("fillColor", new SimpleObjectProperty<>(COLOR));
        return (B) this;
    }

    public final B strokeColor(final Color COLOR) {
        properties.put("strokeColor", new SimpleObjectProperty<>(COLOR));
        return (B) this;
    }

    public final B hoverColor(final Color COLOR) {
        properties.put("hoverColor", new SimpleObjectProperty<>(COLOR));
        return (B) this;
    }

    public final B pressedColor(final Color COLOR) {
        properties.put("pressedColor", new SimpleObjectProperty<>(COLOR));
        return (B) this;
    }

    public final B selectedColor(final Color COLOR) {
        properties.put("selectedColor", new SimpleObjectProperty(COLOR));
        return (B) this;
    }

    public final B locationColor(final Color COLOR) {
        properties.put("locationColor", new SimpleObjectProperty<>(COLOR));
        return (B) this;
    }

    public final B hoverEnabled(final boolean ENABLED) {
        properties.put("hoverEnabled", new SimpleBooleanProperty(ENABLED));
        return (B) this;
    }

    public final B selectionEnabled(final boolean ENABLED) {
        properties.put("selectionEnabled", new SimpleBooleanProperty(ENABLED));
        return (B) this;
    }

    public final B zoomEnabled(final boolean ENABLED) {
        properties.put("zoomEnabled", new SimpleBooleanProperty(ENABLED));
        return (B) this;
    }

    public final B mouseEnterHandler(final EventHandler<MouseEvent> HANDLER) {
        properties.put("mouseEnterHandler", new SimpleObjectProperty(HANDLER));
        return (B) this;
    }

    public final B mousePressHandler(final EventHandler<MouseEvent> HANDLER) {
        properties.put("mousePressHandler", new SimpleObjectProperty(HANDLER));
        return (B) this;
    }

    public final B mouseDragHandler(final EventHandler<MouseEvent> HANDLER) {
        properties.put("mouseDragHandler", new SimpleObjectProperty(HANDLER));
        return (B) this;
    }

    public final B mouseReleaseHandler(final EventHandler<MouseEvent> HANDLER) {
        properties.put("mouseReleaseHandler", new SimpleObjectProperty(HANDLER));
        return (B) this;
    }

    public final B mouseExitHandler(final EventHandler<MouseEvent> HANDLER) {
        properties.put("mouseExitHandler", new SimpleObjectProperty(HANDLER));
        return (B) this;
    }

    public final B locations(final Location... LOCATIONS) {
        properties.put("locations", new SimpleObjectProperty(LOCATIONS));
        return (B) this;
    }

    public final B showLocations(final boolean VISIBLE) {
        properties.put("showLocations", new SimpleBooleanProperty(VISIBLE));
        return (B) this;
    }

    public final B locationIconCode(final Ikon ICON_CODE) {
        properties.put("locationIconCode", new SimpleObjectProperty<>(ICON_CODE));
        return (B) this;
    }

    public final B prefSize(final double WIDTH, final double HEIGHT) {
        properties.put("prefSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B) this;
    }

    public final B minSize(final double WIDTH, final double HEIGHT) {
        properties.put("minSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B) this;
    }

    public final B maxSize(final double WIDTH, final double HEIGHT) {
        properties.put("maxSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B) this;
    }

    public final B prefWidth(final double PREF_WIDTH) {
        properties.put("prefWidth", new SimpleDoubleProperty(PREF_WIDTH));
        return (B) this;
    }

    public final B prefHeight(final double PREF_HEIGHT) {
        properties.put("prefHeight", new SimpleDoubleProperty(PREF_HEIGHT));
        return (B) this;
    }

    public final B minWidth(final double MIN_WIDTH) {
        properties.put("minWidth", new SimpleDoubleProperty(MIN_WIDTH));
        return (B) this;
    }

    public final B minHeight(final double MIN_HEIGHT) {
        properties.put("minHeight", new SimpleDoubleProperty(MIN_HEIGHT));
        return (B) this;
    }

    public final B maxWidth(final double MAX_WIDTH) {
        properties.put("maxWidth", new SimpleDoubleProperty(MAX_WIDTH));
        return (B) this;
    }

    public final B maxHeight(final double MAX_HEIGHT) {
        properties.put("maxHeight", new SimpleDoubleProperty(MAX_HEIGHT));
        return (B) this;
    }

    public final B scaleX(final double SCALE_X) {
        properties.put("scaleX", new SimpleDoubleProperty(SCALE_X));
        return (B) this;
    }

    public final B scaleY(final double SCALE_Y) {
        properties.put("scaleY", new SimpleDoubleProperty(SCALE_Y));
        return (B) this;
    }

    public final B layoutX(final double LAYOUT_X) {
        properties.put("layoutX", new SimpleDoubleProperty(LAYOUT_X));
        return (B) this;
    }

    public final B layoutY(final double LAYOUT_Y) {
        properties.put("layoutY", new SimpleDoubleProperty(LAYOUT_Y));
        return (B) this;
    }

    public final B translateX(final double TRANSLATE_X) {
        properties.put("translateX", new SimpleDoubleProperty(TRANSLATE_X));
        return (B) this;
    }

    public final B translateY(final double TRANSLATE_Y) {
        properties.put("translateY", new SimpleDoubleProperty(TRANSLATE_Y));
        return (B) this;
    }

    public final B padding(final Insets INSETS) {
        properties.put("padding", new SimpleObjectProperty<>(INSETS));
        return (B) this;
    }

    public final World build() {
        final World world = new World();
        setProperties(world);
        return world;
    }

    protected void setProperties(final World world) {
        for (final String key : properties.keySet()) {
            switch (key) {
                case "prefSize": {
                    final Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    world.setPrefSize(dim.getWidth(), dim.getHeight());
                    break;
                }
                case "minSize": {
                    final Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    world.setMinSize(dim.getWidth(), dim.getHeight());
                    break;
                }
                case "maxSize":
                    final Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    world.setMaxSize(dim.getWidth(), dim.getHeight());
                    break;
                case "prefWidth":
                    world.setPrefWidth(((DoubleProperty) properties.get(key)).get());
                    break;
                case "prefHeight":
                    world.setPrefHeight(((DoubleProperty) properties.get(key)).get());
                    break;
                case "minWidth":
                    world.setMinWidth(((DoubleProperty) properties.get(key)).get());
                    break;
                case "minHeight":
                    world.setMinHeight(((DoubleProperty) properties.get(key)).get());
                    break;
                case "maxWidth":
                    world.setMaxWidth(((DoubleProperty) properties.get(key)).get());
                    break;
                case "maxHeight":
                    world.setMaxHeight(((DoubleProperty) properties.get(key)).get());
                    break;
                case "scaleX":
                    world.setScaleX(((DoubleProperty) properties.get(key)).get());
                    break;
                case "scaleY":
                    world.setScaleY(((DoubleProperty) properties.get(key)).get());
                    break;
                case "layoutX":
                    world.setLayoutX(((DoubleProperty) properties.get(key)).get());
                    break;
                case "layoutY":
                    world.setLayoutY(((DoubleProperty) properties.get(key)).get());
                    break;
                case "translateX":
                    world.setTranslateX(((DoubleProperty) properties.get(key)).get());
                    break;
                case "translateY":
                    world.setTranslateY(((DoubleProperty) properties.get(key)).get());
                    break;
                case "padding":
                    world.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
                    break;
                case "backgroundColor":
                    world.setBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                    break;
                case "fillColor":
                    world.setFillColor(((ObjectProperty<Color>) properties.get(key)).get());
                    break;
                case "strokeColor":
                    world.setStrokeColor(((ObjectProperty<Color>) properties.get(key)).get());
                    break;
                case "hoverColor":
                    world.setHoverColor(((ObjectProperty<Color>) properties.get(key)).get());
                    break;
                case "pressedColor":
                    world.setPressedColor(((ObjectProperty<Color>) properties.get(key)).get());
                    break;
                case "selectedColor":
                    world.setSelectedColor(((ObjectProperty<Color>) properties.get(key)).get());
                    break;
                case "locationColor":
                    world.setLocationColor(((ObjectProperty<Color>) properties.get(key)).get());
                    break;
                case "hoverEnabled":
                    world.setHoverEnabled(((BooleanProperty) properties.get(key)).get());
                    break;
                case "selectionEnabled":
                    world.setSelectionEnabled(((BooleanProperty) properties.get(key)).get());
                    break;
                case "zoomEnabled":
                    world.setZoomEnabled(((BooleanProperty) properties.get(key)).get());
                    break;
                case "mouseEnterHandler":
                    world.setMouseEnterHandler(((ObjectProperty<EventHandler<MouseEvent>>) properties.get(key)).get());
                    break;
                case "mousePressHandler":
                    world.setMousePressHandler(((ObjectProperty<EventHandler<MouseEvent>>) properties.get(key)).get());
                    break;
                case "mouseDragHandler":
                    world.setMouseDragHandler(((ObjectProperty<EventHandler<MouseEvent>>) properties.get(key)).get());
                    break;
                case "mouseExitHandler":
                    world.setMouseExitHandler(((ObjectProperty<EventHandler<MouseEvent>>) properties.get(key)).get());
                    break;
                case "locations":
                    world.addLocations(((ObjectProperty<Location[]>) properties.get(key)).get());
                    break;
                case "showLocations":
                    world.showLocations(((BooleanProperty) properties.get(key)).get());
                    break;
                case "locationIconCode":
                    world.setLocationIconCode(((ObjectProperty<Ikon>) properties.get(key)).get());
                    break;
            }
        }
    }
}