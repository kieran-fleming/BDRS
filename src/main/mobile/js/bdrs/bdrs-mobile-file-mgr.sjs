
/**
 * Dumps the entirety of the current database to a JSON file on disk. This 
 * function uses the dumpToJson facility in persistence.
 */
exports.dumpToJson = function() {
    if(bdrs.mobile.fileMgrExists()) {
        var json;
        waitfor(json) {
            persistence.dumpToJson(resume);
        }
        var writeSuccess = function(evt) {
            bdrs.mobile.Debug("Write has succeeded");
        };
        var paths = navigator.fileMgr.getRootPaths();
        var writer = new FileWriter(paths[0] + "bdrs-json-db.txt");
        writer.onwrite = writeSuccess;
        writer.write(json);
    } else {
        bdrs.mobile.Debug("No file manager.");
    }
};