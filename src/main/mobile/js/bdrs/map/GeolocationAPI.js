/**
 * @requires OpenLayers/Control.js
 * @include OpenLayers/Layer/Vector.js
 * @include OpenLayers/Geometry/Point.js
 * @include OpenLayers/Feature/Vector.js
 * @include OpenLayers/BaseTypes/Bounds.js
 * @include OpenLayers/BaseTypes/LonLat.js
 * @include OpenLayers/Projection.js
 */

/**
 * Class: OpenLayers.Control.GeolocationAPI
 *
 * Usage of the HTML5 Geolocation API specification http://dev.w3.org/geo/api/spec-source.html
 * For wider support, http://code.google.com/p/geo-location-javascript/ could be useful
 * Requires proj4js support
 *
 * Inherits from:
 *  - <OpenLayers.Control>
 */

OpenLayers.Control.GeolocationAPI = OpenLayers.Class(OpenLayers.Control, {

    /**
     * APIProperty: mode
     * {<String>}
     *    Define the usage of the Geolocation API.
     *    Possible values: "position" or "tracking"
     *    Default value: "position"
     */
    mode: "position",

    /**
     * APIProperty: zoom
     * {<Integer>}
     *    Zoom level used to recenter the map. If displayAccuracy is set to true, then the zoom extent is defined by accuracy and not by the zoom property
     *    Default value: 12
     */
    zoom: 12,

    /**
     * APIProperty: geolocationOptions
     * {<Object>}
     *    The geolocation API spec defines three optional parameters used for the position determination:
     *    - enableHighAccuracy (boolean, default false): provides a hint that the application would like to receive the best possible results.
     *    - timeout (long, default 0): denotes the maximum length of time (expressed in milliseconds) that is allowed to pass from the call to getCurrentPosition() or watchPosition() until the corresponding successCallback is invoked.
     *    - maximumAge (long, default Infinity): indicates that the application is willing to accept a cached position whose age is no greater than the specified time in milliseconds.
     *    Default value: {}
     */
    geolocationOptions: {},

    /**
     * APIProperty: errorAlert
     * {<Boolean>}
     *    Provide error alerts when geolocation API encounters error
     *    Default value: false
     */
    errorAlert: false,

    /**
     * APIProperty: vector
     * {<OpenLayers.Layer.Vector>}
     *    Vector layer used to display the position and the accuracy. A layer is created if the accuracy or the position needs to be represented and if none is provided.
     */
    vector: null,

    /**
     * APIProperty: displayPosition
     * {<Boolean>}
     *    Display the position as point in a vector layer
     *    Default value: false
     */
    displayPosition: false,

    /**
     * APIProperty: displayAccuracy
     * {<Boolean>}
     *    Display the accuracy as circle in a vector layer
     *    Default value: false
     */
    displayAccuracy: false,

    /**
     * Parameter: currentPosition
     * {<Position>}
     *    Current position determined by the Geolocation API
     */
    currentPosition: null,

    /**
     * Parameter: currentPositionMapCoordinate
     * {<OpenLayers.LonLat>}
     *    Current position in lon/lat of the map coordinate system
     */
    currentPositionMapCoordinate: null,

    /**
     * Parameter: isGeolocationAPISupported
     * {<Boolean>}
     *    Informs if the Geolocation API is supported
     */
    isGeolocationAPISupported: null,

    /**
     * Parameter: trackingId
     * {<String>}
     *    Id of the tracking made by the Geolocation API
     */
    trackingId: false,

    /**
     * Parameter: positionFeature
     * {<OpenLayers.Feature.Vector>}
     *    Feature representing the position
     */
    positionFeature: null,

    /**
     * Parameter: accuracyFeature
     * {<OpenLayers.Feature.Vector>}
     *    Feature representing the accuracy
     */
    accuracyFeature: null,

    /**
     * Constant: EVENT_TYPES
     *
     * Supported event types:
     * - positioncomputed - Triggered when position has been computed
     * - positionerror - Triggered when the geolocation API return an error
     * - geolocationapinotsupported - Triggered when the Geolocation API is not suppported
     */
    EVENT_TYPES: ["positioncomputed", "positionerror", "geolocationapinotsupported"],

    /**
     * Constructor: OpenLayers.Control.GeolocationAPI
     *
     * Parameters:
     * options - {Object}
     */
    initialize: function(options) {
        options = options || {};

        this.EVENT_TYPES =
        OpenLayers.Control.GeolocationAPI.prototype.EVENT_TYPES.concat(
                OpenLayers.Control.prototype.EVENT_TYPES
                );
        if (!options.vector && (options.displayPosition || options.displayAccuracy)) {
            options.vector = new OpenLayers.Layer.Vector("GeolocationAPIVector",
            {displayInLayerSwitcher: false});
        }

        OpenLayers.Util.applyDefaults(this, options);


        OpenLayers.Control.prototype.initialize.apply(this, [options]);
        if (navigator.geolocation) {
            this.isGeolocationAPISupported = true;
        } else {
            this.isGeolocationAPISupported = false;
            this.events.triggerEvent("geolocationapinotsupported", {control : this});
        }
    },

    /**
     * APIMethod: activate
     * Activate the control and activate the usage of the Geolocation API.
     * Adds the vector layer, if needed
     *
     * Returns:
     * {Boolean} Successfully activated the control.
     */
    activate: function () {
        if (this.isGeolocationAPISupported && this.mode === "position") {
            this.activatePosition();
        }
        if (this.isGeolocationAPISupported && this.mode === "tracking") {
            this.activateTracking();
        }
        if (this.displayPosition || this.displayAccuracy) {
            if (this.vector.name == 'GeolocationAPIVector') {
                this.map.addLayers([this.vector]);
            }
        }
        return OpenLayers.Control.prototype.activate.apply(this);
    },

    /**
     * APIMethod: deactivate
     * Deactivate the control and all handlers.
     * Stop the tracking and remove the displayed features
     *
     * Returns:
     * {Boolean} Successfully deactivated the control.
     */
    deactivate: function () {
        this.stopTracking();
        if (this.displayPosition || this.displayAccuracy) {
            this.removeFeatures();
            if (this.vector.name == 'GeolocationAPIVector') {
                this.map.removeLayer(this.vector);
            }
        }
        return OpenLayers.Control.prototype.deactivate.apply(this);
    },

    /**
     * APIMethod: stopTracking
     * Stop the tracking
     *
     * Returns:
     * {Boolean} Successfully deactivated the control.
     */
    stopTracking: function() {
        if (this.trackingId) {
            navigator.geolocation.clearWatch(this.trackingId);
            this.trackingId = false;
        }
    },

    /**
     * APIMethod: removeFeatures
     * Remove the position and accuracy features
     *
     */
    removeFeatures: function() {
        if (this.positionFeature) {
            this.vector.removeFeatures([this.positionFeature]);
        }
        if (this.positionFeature) {
            this.vector.removeFeatures([this.positionFeature]);
        }
    },

    /**
     * APIMethod: showFeatures
     * Show the position and accuracy features.
     * If accuracy feature is display, a zoom to its extent is done
     *
     */
    showFeatures: function() {
        var point = new OpenLayers.Geometry.Point(this.currentPositionMapCoordinate.lon,
                this.currentPositionMapCoordinate.lat);
        if (this.displayPosition) {
            this.positionFeature = new OpenLayers.Feature.Vector(point);
            this.vector.addFeatures([this.positionFeature]);
        }
        if (this.displayAccuracy) {
            var radius = this.currentPosition.coords.accuracy;
            var bbox = new OpenLayers.Bounds(this.currentPositionMapCoordinate.lon - radius,
                    this.currentPositionMapCoordinate.lat - radius,
                    this.currentPositionMapCoordinate.lon + radius,
                    this.currentPositionMapCoordinate.lat + radius);
            var circle = OpenLayers.Geometry.Polygon.createRegularPolygon(point, radius, 40);
            this.accuracyFeature = new OpenLayers.Feature.Vector(circle);
            this.vector.addFeatures(new OpenLayers.Feature.Vector(circle));
            this.map.zoomToExtent(bbox);
        }
    },

    /**
     * APIMethod: activatePosition
     * Use the getCurrentPosition function of the Geolocation API to determine its position
     *
     */
    activatePosition: function() {
        navigator.geolocation.getCurrentPosition(OpenLayers.Function.bind(this._successCallback, this), OpenLayers.Function.bind(this._errorCallback, this), this.geolocationOptions);
    },

    /**
     * APIMethod: activateTracking
     * Use the watchPosition function of the Geolocation API to track its position
     *
     */
    activateTracking: function() {
        this.trackingId = navigator.geolocation.watchPosition(OpenLayers.Function.bind(this._successCallback, this), OpenLayers.Function.bind(this._errorCallback, this), this.geolocationOptions);
    },

    /**
     * Method: _successCallback
     * Success callback function. Center the map and shows the features
     * positioncomputed event is triggered
     */
    _successCallback: function(position) {
        this.currentPosition = position;
        var lonLat = new OpenLayers.LonLat(position.coords.longitude, position.coords.latitude);
        this.currentPositionMapCoordinate = lonLat.transform(new OpenLayers.Projection("EPSG:4326"),
                this.map.getProjectionObject());
        this.map.setCenter(this.currentPositionMapCoordinate, this.zoom);
        this.removeFeatures();
        this.showFeatures();
        this.events.triggerEvent("positioncomputed", {position : position});
    },

    /**
     * Method: _errorCallback
     * Error callback function. Send alerts if required
     * positionerror event is triggered
     */
    _errorCallback: function(error) {
        this.events.triggerEvent("positionerror", {error : error});
        if (this.errorAlert) {
            switch (error.code) {
                case 0: alert(OpenLayers.i18n("There was an error while retrieving your location: ") + error.message); break;
                case 1: alert(OpenLayers.i18n("The user didn't accept to provide the location: ")); break;
                case 2: alert(OpenLayers.i18n("The browser was unable to determine your location: ") + error.message); break;
                case 3: alert(OpenLayers.i18n("The browser timed out before retrieving the location.")); break;
            }
        }
    },

    /**
     * APIMethod: getPositionInformation
     * Get textual position information
     *
     * Parameters:
     * separator - {<String>} a separator between the position information. Can be "<BR>", for example.
     *
     * Returns:
     * {String} All position information provided by the Geolocation API.
     */
    getPositionInformation: function(separator) {
        if (this.currentPosition) {
            var positionString = OpenLayers.i18n("Longitude: ") + this.currentPosition.coords.longitude + separator;
            positionString = positionString + OpenLayers.i18n("Latitude: ") + this.currentPosition.coords.latitude + separator;
            positionString = positionString + OpenLayers.i18n("Accuracy: ") + this.currentPosition.coords.accuracy + separator;
            positionString = positionString + OpenLayers.i18n("Altitude: ") + this.currentPosition.coords.altitude + separator;
            positionString = positionString + OpenLayers.i18n("Altitude Accuracy: ") + this.currentPosition.coords.altitudeAccuracy + separator;
            positionString = positionString + OpenLayers.i18n("Heading: ") + this.currentPosition.coords.heading + separator;
            positionString = positionString + OpenLayers.i18n("Speed: ") + this.currentPosition.coords.speed + separator;
            positionString = positionString + OpenLayers.i18n("Date: ") + this.formatDate(new Date(this.currentPosition.timestamp), '%Y/%M/%d') + separator;
            positionString = positionString + OpenLayers.i18n("Time (UTC): ") + this.formatDate(new Date(this.currentPosition.timestamp), '%H:%m:%s') + separator;
            return positionString;
        } else {
            return OpenLayers.i18n("Not available");
        }
    },

    /**
     * Method: formatDate
     * Format the date in UTC time
     * TODO: shouldn't be in this control
     */
    formatDate: function(date, fmt) {
        function pad(value) {
            return (value.toString().length < 2) ? '0' + value : value;
        }

        return fmt.replace(/%([a-zA-Z])/g, function (_, fmtCode) {
            switch (fmtCode) {
                case 'Y':
                    return date.getUTCFullYear();
                case 'M':
                    return pad(date.getUTCMonth() + 1);
                case 'd':
                    return pad(date.getUTCDate());
                case 'H':
                    return pad(date.getUTCHours());
                case 'm':
                    return pad(date.getUTCMinutes());
                case 's':
                    return pad(date.getUTCSeconds());
                default:
                    throw new Error('Unsupported format code: ' + fmtCode);
            }
        });
    }
});