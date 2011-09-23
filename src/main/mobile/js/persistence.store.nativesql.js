try {
  if(!window) {
    window = {};
    //exports.console = console;
  }
} catch(e) {
  window = {};
  exports.console = console;
}

var persistence = (window && window.persistence) ? window.persistence : {}; 

if(!persistence.store) {
  persistence.store = {};
}

persistence.store.nativesql = {};
persistence.store.nativesql.config = function(persistence, dbname, path, onCreateCallback) {
  var conn = null;

  /**
   * Create a transaction
   * 
   * @param callback,
   *            the callback function to be invoked when the transaction
   *            starts, taking the transaction object as argument
   */
  persistence.transaction = function (callback) {
    if(!conn) {
      throw new Error("No ongoing database connection, please connect first.");
    } else {
      conn.transaction(callback);
    }
  };

  ////////// Low-level database interface, abstracting from HTML5 and Gears databases \\\\
  persistence.db = persistence.db || {};

  persistence.db.implementation = "unsupported";
  persistence.db.conn = null;

  // window object does not exist on Qt Declarative UI (http://doc.trolltech.org/4.7-snapshot/declarativeui.html)
  if(window && window.plugins.nativeDB && window.plugins.nativeDB.available) {
    persistence.db.implementation = "nativedb";
  } else {
    throw new Error("Native database access not available");
  }

  persistence.db.nativedb = {};

  persistence.db.nativedb.connect = function (dbname, path) {
    var that = {};
    
    //NativeDB.openDatabase(dbname, path,
    window.plugins.nativeDB.openDatabase(dbname, path, 
        function(db) {
            // openDatabase Success Handler
            that.conn = db;
            if(onCreateCallback !== undefined) {
                onCreateCallback();
            }
        }, 
        function(err) {
            throw new Error(err);
        }
    );
    
    that.transaction = function (fn) {
        return that.conn.transaction(function (sqlt) {
            return fn(persistence.db.nativedb.transaction(sqlt));
        });
    };
    
    return that;
  };

  persistence.db.nativedb.transaction = function (t) {
    var that = {};
    
    that.executeSql = function (query, args, successFn, errorFn) {
      if(persistence.debug) {
        console.log(query, args);
      }
      t.executeSql(query, args, function (_, result) {
          if (successFn) {
            var results = [];
            for ( var i = 0; i < result.rows.length; i++) {
              results.push(result.rows.item(i));
            }
            successFn(results);
          }
        }, errorFn);
    };
    
    that.setFlushing = function(flushing) {
        t.flushing = flushing === true;
    };
    
    that.getFlushing = function() {
        return t.flushing;
    };
    
    that.commit = function() {
        return t.commit();
    };
    
    return that;
  };

  persistence.db.connect = function (dbname, description, size) {
      // Size is not used for native DB
      if(persistence.db.implementation === 'nativedb') {
      
          return persistence.db.nativedb.connect(dbname, description);
      }
  };

  ///////////////////////// SQLite dialect

  persistence.store.nativesql.sqliteDialect = {
    // columns is an array of arrays, e.g.
    // [["id", "VARCHAR(32)", "PRIMARY KEY"], ["name", "TEXT"]]
    createTable: function(tableName, columns) {
      var tm = persistence.typeMapper;
      var sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (";
      var defs = [];
      for(var i = 0; i < columns.length; i++) {
        var column = columns[i];
        defs.push("`" + column[0] + "` " + tm.columnType(column[1]) + (column[2] ? " " + column[2] : ""));
      }
      sql += defs.join(", ");
      sql += ')';
      return sql;
    },

    // columns is array of column names, e.g.
    // ["id"]
    createIndex: function(tableName, columns, options) {
      options = options || {};
      return "CREATE "+(options.unique?"UNIQUE ":"")+"INDEX IF NOT EXISTS `" + tableName + "__" + columns.join("_") + 
             "` ON `" + tableName + "` (" + 
             columns.map(function(col) { return "`" + col + "`"; }).join(", ") + ")";
    }
  };

  // Configure persistence for generic sql persistence, using sqliteDialect
  persistence.store.sql.config(persistence, persistence.store.nativesql.sqliteDialect);

  // Make the connection
  conn = persistence.db.connect(dbname, path);
  if(!conn) {
    throw new Error("No supported database found in this browser.");
  }
};

try {
  exports.persistence = persistence;
} catch(e) {}
