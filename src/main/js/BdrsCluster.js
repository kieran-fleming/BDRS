/* Copyright (c) 2006-2010 by OpenLayers Contributors (see authors.txt for 
 * full list of contributors). Published under the Clear BSD license.  
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/**
 * @requires OpenLayers/Strategy.js
 */

// avoid file import ordering issues
if (window.bdrs === undefined) {
	window.bdrs = {};
}
if (bdrs.map === undefined) {
	bdrs.map = {};
}

/**
 * Class: bdrs.map.BdrsCluster
 * Strategy for vector feature clustering with hacks to deselect the currently selected node on reclustering (i.e. on zoom)
 *
 * Inherits from:
 *  - <OpenLayers.Strategy>
 */
bdrs.map.BdrsCluster = OpenLayers.Class(OpenLayers.Strategy.Cluster, {

    /**
     * Constructor: OpenLayers.Strategy.Cluster
     * Create a new clustering strategy.
     *
     * Parameters:
     * options - {Object} Optional object whose properties will be set on the
     *     instance.
     */
    initialize: function(options) {
        OpenLayers.Strategy.Cluster.prototype.initialize.apply(this, [options]);
    },
    
    /**
     * Overridden Method: cluster
     * Cluster features based on some threshold distance.
     *
     * Note we deselect items using the popUpControl that is duck punched onto the 
     * layers object. This closing is done on reclustering which occurs
     * on zoom.
     * 
     * Parameters:
     * event - {Object} The event received when cluster is called as a
     *     result of a moveend event.
     */
    cluster: function(event) {
        if((!event || event.zoomChanged) && this.features) {
            var resolution = this.layer.map.getResolution();
            if(resolution != this.resolution || !this.clustersExist()) {
                this.resolution = resolution;
                var clusters = [];
                var feature, clustered, cluster;

                var selected = this.layer.selectedFeatures.slice();
                var select = new OpenLayers.Control.SelectFeature(this.layer);
                var selectedFid = (selected && selected.length > 0 ) ? selected[0].fid : -1;
				
				// the last feature we add to place it on the top!
				// the last item in the 'clusters' array gets placed at the top of the layer.
				// unfortunately this does rely on an internal implementation detail of open layers.
				// we use this to bring the highlighted item to the top.
				var topCluster = null;
                
                for(var i=0; i<this.features.length; ++i) {
                    feature = this.features[i];
                    if(feature.geometry) {
						
						clustered = false;
						// only points are clustered
						if (feature.geometry.CLASS_NAME === "OpenLayers.Geometry.Point" &&
								feature.fid != this.ignoreId && 
								feature.fid !== selectedFid) {
                            for (var j = clusters.length - 1; j >= 0; --j) {
								cluster = clusters[j];
								// can only add to modifiable clusters.
								if (cluster.isModifiable && this.shouldCluster(cluster, feature)) {
									this.addToCluster(cluster, feature);
									clustered = true;
									break;
								}
							}
						} 
                        if(!clustered) {
							var newCluster = this.createCluster(feature);
							if (feature.fid != this.ignoreId) {
							    clusters.push(newCluster);	
							} else {
							    topCluster = newCluster;
							}
                        }
                    }
                }
				
				if (topCluster) {
					clusters.push(topCluster);
				}

                // Close popups and unselect features!
                if (this.layer.selectPopUpControl) {
                    // clone the selected features array
                    for (var selectedIdx=0; selectedIdx < selected.length; ++selectedIdx) {
                        this.layer.selectPopUpControl.unselect(selected[selectedIdx]);
                    }
                }
                
                this.layer.removeAllFeatures();
                if(clusters.length > 0) {
                    if(this.threshold > 1) {
                        var clone = clusters.slice();
                        clusters = [];
                        var candidate;
                        for(var i=0, len=clone.length; i<len; ++i) {
                            candidate = clone[i];
                            if(candidate.attributes.count < this.threshold) {
                                Array.prototype.push.apply(clusters, candidate.cluster);
                            } else {
                                clusters.push(candidate);
                            }
                        }
                    }
					this.clustering = true;
                    // A legitimate feature addition could occur during this
                    // addFeatures call.  For clustering to behave well, features
                    // should be removed from a layer before requesting a new batch.
                    this.layer.addFeatures(clusters);
                    
                    this.clustering = false;
                }
                this.clusters = clusters;
                
                // re-select the features here
                for (var selectedIdx=0; selectedIdx < selected.length; ++selectedIdx) {
                	var fid = selected[selectedIdx].fid;
                    if (fid) {
                        var hlFeature = this.layer.getFeatureByFid(fid);
                        if (hlFeature) {
                            select.select(hlFeature);
                        }
                    }
                }
            }
        }
    },
	
	    /**
     * Method: createCluster
     * Given a feature, create a cluster.
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>}
     *
     * Returns:
     * {<OpenLayers.Feature.Vector>} A cluster.
     */
    createCluster: function(feature) {
		var geom;
		var modifiable;
		
		// feature is already a cluster. This handles the  case where
		// we remove then add a feature in order to raise it to the top
		// of the layer
		if (feature.cluster) {
			return feature;
		}

        if (feature.geometry.CLASS_NAME === "OpenLayers.Geometry.Point") {
            var center = feature.geometry.getBounds().getCenterLonLat();
			geom = new OpenLayers.Geometry.Point(center.lon, center.lat);
			var selectedFid = (this.layer.selectedFeatures.length > 0 ? this.layer.selectedFeatures[0].fid : -1);
			modifiable = feature.fid != this.ignoreId && feature.fid != selectedFid;
        } else {
			// other geometry types are never clustered
            geom = feature.geometry;
            modifiable = false;
		}
		
        var cluster = new OpenLayers.Feature.Vector(geom, {count: 1});
		// duck punch isModifiable - can only add to modifiable clusters
		cluster.isModifiable = modifiable;
        cluster.cluster = [feature];
        cluster.fid = feature.fid;
        return cluster;
    },

    CLASS_NAME: "bdrs.map.BdrsCluster" 
});
