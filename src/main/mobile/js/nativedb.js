
/**
 * Determines if native database access is available.
 */
window.native_db_available = false;

/**
 * The main point of entry for this plugin. The NativeDB plugin aims to provide
 * seamless access to a native database implementation as an alternative to WebSQL.
 *
 * Note: This is not a representation of a native database but more akin to a native database factory.
 */
var NativeDB = function() {
    this.PLUGIN_NAME = "nativeDB";
    this.available = window.native_db_available;

    return this;
};

PhoneGap.addConstructor(function() {
    // The following code registers this plugin with Phonegap.
    var nb = new NativeDB();
    PhoneGap.addPlugin(nb.PLUGIN_NAME, nb);
    PluginManager.addService(nb.PLUGIN_NAME,"au.com.gaiaresources.phonegap.plugin.nativedb.NativeDBPlugin");
});
