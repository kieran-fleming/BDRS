/* Copyright (c) 2006-2010 by OpenLayers Contributors (see authors.txt for 
 * full list of contributors). Published under the Clear BSD license.  
 * See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license. */

/**
 * @requires OpenLayers/Strategy.js
 */

/**
 * Class: OpenLayers.Strategy.BdrsCluster
 * Strategy for vector feature clustering with hacks to deselect the currently selected node on reclustering (i.e. on zoom)
 *
 * Inherits from:
 *  - <OpenLayers.Strategy>
 */
OpenLayers.Strategy.BdrsCluster = OpenLayers.Class(OpenLayers.Strategy.Cluster, {

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
                for(var i=0; i<this.features.length; ++i) {
                    feature = this.features[i];
                    if(feature.geometry) {
                        clustered = false;
                        for(var j=clusters.length-1; j>=0; --j) {
                            cluster = clusters[j];
                            if(this.shouldCluster(cluster, feature)) {
                                this.addToCluster(cluster, feature);
                                clustered = true;
                                break;
                            }
                        }
                        if(!clustered) {
                            clusters.push(this.createCluster(this.features[i]));
                        }
                    }
                }
                
                // Close popups and unselect features!
                if (this.layer.selectPopUpControl) {
                    // clone the selected features array
                    var selected = this.layer.selectedFeatures.slice();
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
            }
        }
    },

    CLASS_NAME: "OpenLayers.Strategy.BdrsCluster" 
});
