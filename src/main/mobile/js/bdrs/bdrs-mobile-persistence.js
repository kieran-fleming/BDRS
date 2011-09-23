if (!window.bdrs) {
    bdrs = {};
}

/////////////////////////////////////////
// Database 
/////////////////////////////////////////
if(!window.bdrs.database) {
    bdrs.database = {};
}

bdrs.database.DBREADY_EVENT_TYPE = "dbready";
bdrs.database.ready = false;
bdrs.database.onDatabaseReady = function() {
    bdrs.database.ready = true;
};

bdrs.database.addDatabaseReadyListener = function(callback) {
    document.addEventListener(bdrs.database.DBREADY_EVENT_TYPE, callback, false);
};

bdrs.database.addDatabaseReadyListener(bdrs.database.onDatabaseReady);

/////////////////////////////////////////
// Persistence 
/////////////////////////////////////////
if (!window.bdrs.persistence) {
    bdrs.persistence = {};
}

bdrs.persistence.defineSchema = function() {
    window.Image = persistence.define('Image', {
        path: "TEXT",
        data: "TEXT",
        type: "TEXT"
        });
    
    window.Settings = persistence.define('Settings', {
            key: "TEXT",
            value: "TEXT",
        });
    
    window.ServerObject = persistence.defineMixin('ServerObject', {
            server_id: "INT",
            weight: "INT",
        });
        
    
    window.AttributeValue = persistence.define('AttributeValue', {
            value: "TEXT",
        });
    AttributeValue.is(ServerObject);
    
    window.Record = persistence.define('Record', {
            latitude: "NUMERIC",
            longitude: "NUMERIC",
            accuracy: "NUMERIC",
            when: "DATE",
            time: "TEXT",    
            lastDate: "DATE",
            lastTime: "TEXT",
            notes: "TEXT",
            number: "INT",
            modifiedAt: "DATE",
            uploadedAt: "DATE",
            deleted: "BOOL"
        });
    Record.hasMany('attributeValues', AttributeValue, 'record');
    Record.hasMany('children', Record, 'parent');
    Record.is(ServerObject);
       
    window.AttributeOption = persistence.define('AttributeOption', {
            value: "TEXT",
        });
    AttributeOption.is(ServerObject);
        
    window.Attribute = persistence.define('Attribute', {
            typeCode: "TEXT",
            required: "BOOL",
            name: "TEXT",
            description: "TEXT",
            tag: "BOOL",
            scope: "TEXT",
        });
    Attribute.hasMany('options', AttributeOption, 'attribute');
    Attribute.hasMany('attributeValues', AttributeValue, 'attribute');
    Attribute.is(ServerObject);
    
    window.SpeciesAttribute = persistence.define('SurveyAttribute', {
        numericValue : "NUMERIC",
        stringValue : "TEXT",
        dateValue : "DATE",
        description : "TEXT"
    });
    SpeciesAttribute.hasOne('attribute', Attribute);
    SpeciesAttribute.is(ServerObject);
    
    window.SpeciesProfile = persistence.define('SpeciesProfile', {
            content : "TEXT",
            weight : "INT",
            description : "TEXT",
            type : "TEXT",
            header : "TEXT"
    });
    SpeciesProfile.is(ServerObject);
    
    window.Species = persistence.define('Species', {
            scientificNameAndAuthor: "TEXT",
            scientificName: "TEXT",
            commonName: "TEXT",
            rank: "TEXT",
            author: "TEXT",
            year: "TEXT",
        });
    //Species.hasMany('attributes', Attribute, 'species');
    Species.hasMany('speciesAttributes', SpeciesAttribute, 'species');
    Species.hasMany('infoItems', SpeciesProfile, 'species');
    Species.hasMany('records', Record, 'species');
    Species.is(ServerObject);
    //Species.textIndex('scientificName');
//  Species.textIndex('commonName');
        
    window.TaxonGroup = persistence.define('TaxonGroup', {
            name: "TEXT",
            image: "TEXT",
            thumbNail: "TEXT",
        });
    TaxonGroup.hasMany('species', Species, 'taxonGroup');
    TaxonGroup.hasMany('attributes', Attribute, 'taxonGroup');
    TaxonGroup.is(ServerObject);
    
    window.Location = persistence.define('Location', {
            name: "TEXT",
            latitude : "NUMERIC",
            longitude : "NUMERIC"
        });
    Location.hasMany('records', Record, 'location');
    Location.is(ServerObject);
    
    window.CensusMethod = persistence.define('CensusMethod', {
        name: "TEXT",
        description: "TEXT",
        type: "TEXT",
        taxonomic: "TEXT"
    });
    CensusMethod.hasMany('attributes', Attribute, 'censusMethod');
    CensusMethod.hasMany('records', Record, 'censusMethod');
    CensusMethod.hasMany('children', CensusMethod, 'parent');
    CensusMethod.is(ServerObject);
    
    window.User = persistence.define('User', {
        name: "TEXT",
        ident: "TEXT",
        firstname: "TEXT",
        lastname: "TEXT",
        server_url: "TEXT",
        portal_id: "NUMERIC"
    });
    User.hasMany('locations', Location, 'user');
    User.is(ServerObject);
    
    window.Survey = persistence.define('Survey', {
            name: "TEXT",
            description: "TEXT",
            active: "BOOL",
            local: "BOOL",
            date: "DATE"
        });
    Survey.hasMany('locations', Location, 'survey');
    Survey.hasMany('attributes', Attribute, 'survey');
    Survey.hasMany('records', Record, 'survey');
    Survey.hasMany('censusMethods', CensusMethod, 'survey');
    Survey.hasMany('species', Species, 'survey');
    Species.hasMany('surveys', Survey, 'species');
    Survey.is(ServerObject);
    
    window.SpeciesCount = persistence.define('SpeciesCount', {
            scientificName: "TEXT",
            count: "NUMERIC",
            userCount: "NUMERIC"
        });
    SpeciesCount.hasOne('species', Species, 'count');
    SpeciesCount.hasOne('survey', Survey, 'count');
    
    /**
     *  These need to be added.
     * 
    var SupSampleType = persistence.define('SubSampleType' , {
        });
            
    
    var SubSample = persistence.define('SubSample', {
        });
    
    var SubSampleAttribute = persistence.define('SubSampleAttribute', {
        });
    */
    
    /**
     * Testing data. 
     */
    persistence.schemaSync(function () {
        persistence.flush();
    });
};

bdrs.persistence.dbname = 'csdb';
persistence.debug = false;

if(window.native_db_available === true) {
    document.addEventListener("deviceready", function() {
        bdrs.persistence.dbpath = 'bdrs.db';
        bdrs.mobile.Debug("Starting Native DB");
        
        persistence.store.nativesql.config(persistence, bdrs.persistence.dbname, bdrs.persistence.dbpath, function() {
            bdrs.persistence.defineSchema(); 
            bdrs.mobile.fireEvent(bdrs.database.DBREADY_EVENT_TYPE);
        });
    }, true);
} else {
    bdrs.persistence.dbdesc = 'ALA Citizen Science Database';
    bdrs.persistence.dbsize = 4 * 1024 * 1024; // 4MB

    bdrs.mobile.Debug("Starting WebSQL DB");
    persistence.store.websql.config(persistence, bdrs.persistence.dbname, bdrs.persistence.dbdesc, bdrs.persistence.dbsize);
    bdrs.persistence.defineSchema();
    bdrs.mobile.fireEvent(bdrs.database.DBREADY_EVENT_TYPE);
}