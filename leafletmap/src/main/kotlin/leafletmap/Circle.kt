package leafletmap

import javafx.scene.paint.Color

class Circle private constructor(private var center: LatLong, private var radius: Double, private var title: String, private var zIndexOffset: Int) {
    private var color = Color(0.0, 0.0, 0.0, 0.0)
    private lateinit var map: LeafletMapView
    private var isAttached = false
    private var isDisplayed = false

    constructor(position: LatLong, radius: Double, title: String, color: Color, zIndexOffset: Int) : this(position, radius, title, zIndexOffset) {
        this.color = color
        this.title = title.replace("-", "")
        this.center = position
    }

    internal fun addToMap(map: LeafletMapView) {
        this.map = map
        if (map.execScript("typeof circle$title == 'undefined'") as Boolean) {
            map.execScript("var circle$title;")
        }
        if (!this.isAttached) {
            map.execScript("circle$title = L.circle([${center.latitude}, ${center.longitude}], $radius).addTo(myMap);")
            this.isAttached = true
            this.isDisplayed = true
        } else if (!this.isDisplayed) {
            map.execScript("circle$title.addTo(myMap)")
            this.isDisplayed = true
        }
    }

    fun modifyCircle(latLong: LatLong, radius: Double) {
        this.center = latLong
        this.radius = radius
    }

    fun uppdateMap() {
        if (this.isAttached && !this.isDisplayed) {
            map.execScript("myMap.removeLayer(circle$title);" +
                    "circle$title = L.circle([${center.latitude}, ${center.longitude}], $radius).addTo(myMap);")
            this.isDisplayed = true
        }
    }

    internal fun removeCircle(map: LeafletMapView) {
        if (this.isAttached && this.isDisplayed) {
            map.execScript("myMap.removeLayer(circle$title);")
            this.isDisplayed = false
        }
    }

}