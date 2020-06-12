package leafletmap

/**
 * Creates a marker at the specified geographical position.
 *
 * @author Niklas Kellner
 *
 * @param position marker position
 * @param title marker title shown in tooltip (pass empty string when tooltip not needed)
 * @param zIndexOffset zIndexOffset (higher number means on top)
 *
 */
class Marker private constructor(private var position: LatLong, private var title: String, private var zIndexOffset: Int) {
    private var marker = "aircraftIcon"
    private var markerSmall = "aircraftSmallIcon"
    private lateinit var map: LeafletMapView
    private var attached = false
    private var clickable = false
    private var name = ""
    private var rotation = 0

    /**
     * Creates a marker at the specified geographical position.
     *
     * @param position marker position
     * @param title marker title shown in tooltip (pass empty string when tooltip not needed)
     * @param marker marker color
     * @param zIndexOffset zIndexOffset (higher number means on top)
     * @return variable name of the created marker
     */
    constructor(position: LatLong, title: String, marker: ColorMarker, zIndexOffset: Int) : this(position, title, zIndexOffset) {
        this.marker = marker.iconName
    }

    /**
     * Creates a marker at the specified geographical position.
     *
     * @param position marker position
     * @param title marker title shown in tooltip (pass empty string when tooltip not needed)
     * @param marker marker color
     * @param zIndexOffset zIndexOffset (higher number means on top)
     * @return variable name of the created marker
     */
    constructor(position: LatLong, title: String, marker: String, zIndexOffset: Int) : this(position, title, zIndexOffset) {
        this.marker = marker
    }

    /**
     * Adds the marker to a map, gets called from the mapAddMarker
     *
     * @param nextMarkerName the variable name of the marker
     * @param map the LeafetMapView
     */
    internal fun addToMap(nextMarkerName: String, map: LeafletMapView) {
        this.name = nextMarkerName
        this.map = map
        this.attached = true
        this.title = this.title.replace("'", "&apos;")
        map.execScript("""
            |var currentZoom = myMap.getZoom();
            |var $name;
                |if (currentZoom < ${map.zoomLimitSmallMarker}) {
                    |$name = L.marker([${position.latitude}, ${position.longitude}], {title: '', icon: $markerSmall, zIndexOffset: $zIndexOffset}).addTo(markersGroup);
                |} else {
                    |$name = L.marker([${position.latitude}, ${position.longitude}], {title: '', icon: $marker, zIndexOffset: $zIndexOffset}).addTo(markersGroup);
                |}
        """.trimMargin())
        map.execScript("$name.bindTooltip('<div id=\"html_c92f9552ec164f36978869550cb44ffe\" style=\"width: 100.0%; height: 100.0%;\">$title</div>');")
        if (clickable) {
            setClickable()
        }
    }

    private fun setTooltip(title: String) {
        this.title = title.replace("'", "&apos;")
        map.execScript("$name.bindTooltip('<div id=\"html_c92f9552ec164f36978869550cb44ffe\" style=\"width: 100.0%; height: 100.0%;\">${this.title}</div>');")
    }


    /**
     * Changes the icon of the marker
     *
     * @param newIcon the name of the new icon
     */
    fun changeIcon(newIcon: String) {
        this.marker = newIcon
        if (attached) {
            map.execScript("$name.setIcon($marker);")
        }
    }

    /**
     * Changes the icon of the marker
     *
     * @param newIcon the new ColorMarker
     */
    fun changeIcon(newIcon: ColorMarker) {
        this.marker = newIcon.iconName
        if (attached) {
            map.execScript("$name.setIcon(${newIcon.iconName});")
        }
    }

    /**
     * Moves the existing marker specified by the variable name to the new geographical position.
     *
     * @param position new marker position
     */
    fun move(position: LatLong, tooltip: String) {
        this.position = position
        if (attached) {
            map.execScript("$name.setLatLng([${this.position.latitude}, ${this.position.longitude}]);")
            setTooltip(tooltip)
        }
    }

    fun move(position: LatLong) {
        this.position = position
        if (attached) {
            map.execScript("$name.setLatLng([${this.position.latitude}, ${this.position.longitude}]);")
        }
    }

    fun setRotation(rotation: Int) {
        if (rotation > 360 || rotation < 0) {
            this.rotation = 0
        } else {
            this.rotation = rotation
        }
        if (attached) {
            map.execScript("$name.setRotationAngle(${this.rotation})")
        }
    }


    /**
     * Sets the marker clickable
     */
    private fun setClickable() {
        this.clickable = true
        if (attached) {
            map.execScript("$name.on('click', function(e){ document.java.markerClick($name.options.title)})")
        }
    }

    internal fun getName(): String = this.name
}