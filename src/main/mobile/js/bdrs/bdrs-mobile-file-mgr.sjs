
/**
 * Dumps the entirety of the current database to a JSON file on disk. This 
 * function uses the dumpToJson facility in persistence.
 */
exports.dumpToJson = function() {
    if(!bdrs.mobile.fileIO.exists()) {
        bdrs.mobile.Debug("Cannot write file. No file system.");
        return;
    }

    bdrs.mobile.Debug("About to dump database to JSON");
    var json;
    waitfor(json) {
        persistence.dumpToJson(resume);
    }
    bdrs.mobile.Debug("Database dump to JSON completed.");

    var d = new Date();
    var filepath = [
        d.getFullYear(),
        bdrs.mobile.zerofill((d.getMonth()+1), 2),
        bdrs.mobile.zerofill(d.getDate(),2),
        '_',
        bdrs.mobile.zerofill(d.getHours(),2),
        bdrs.mobile.zerofill(d.getMinutes(),2),
        bdrs.mobile.zerofill(d.getSeconds(),2)
    ].join('');
    filepath = "bdrs/json-db/" + filepath + ".txt";

    bdrs.mobile.Debug("JSON file will be written to: " + filepath);
    bdrs.mobile.Debug("Writing File");
    waitfor() {
        bdrs.mobile.fileIO.writeFile(filepath, json, resume, resume);
    }
    bdrs.mobile.Debug("Writing Completed");
};