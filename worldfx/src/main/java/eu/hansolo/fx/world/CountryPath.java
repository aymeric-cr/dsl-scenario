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

import javafx.scene.shape.Polygon;

import java.util.Locale;


/**
 * Created by hansolo on 20.09.16.
 */
public class CountryPath extends Polygon {
    private final String NAME;
    private final Locale LOCALE;

    // ******************** Constructors **************************************
    public CountryPath(final String NAME) {
        this.NAME = NAME;
        this.LOCALE = new Locale("", NAME);
    }


    // ******************** Methods *******************************************
    public String getName() {
        return NAME;
    }

    public Locale getLocale() {
        return LOCALE;
    }
}
