package leafletmap

class Zone constructor(private var title: String) {
    private lateinit var map: LeafletMapView
    private var isAttached = false
    private var isDisplayed = false
    private var positions = listOf<LatLong>()


    fun addToMap(map: LeafletMapView) {
        this.map = map

        if (map.execScript("typeof zone$title == 'undefined';") as Boolean) {
            map.execScript("var zone$title")
        }
        if (!this.isAttached) {

            map.execScript("var points$title = [];" +
                    "zone$title = L.polygon(points$title).addTo(myMap);")
            this.isAttached = true
            this.isDisplayed = true
        } else if (!this.isDisplayed) {
            map.execScript("zone$title.addTo(myMap);")
            this.isDisplayed = true
        }
    }

    private fun addPoint(latLong: LatLong) {
        map.execScript("points$title.push([${latLong.latitude}, ${latLong.longitude}]);")
    }

    fun updatePoints(positions: List<LatLong>) {
        this.positions = positions
        if (map.execScript("typeof points$title == 'undefined'") as Boolean) {
            map.execScript("var points$title = [];")
        } else {
            map.execScript("points$title = [];")
        }
        for (position in positions) {
            addPoint(position)
        }
    }

    fun updateMap() {
        if (this.isAttached) {
            map.execScript("myMap.removeLayer(zone$title);" +
                    "zone$title = L.polygon(points$title).addTo(myMap);")
            this.isDisplayed = true
        }
    }

    internal fun removeZone() {
        if (this.isAttached && this.isDisplayed) {
            map.execScript("myMap.removeLayer(zone$title);")
            this.isDisplayed = false
        }
    }

}