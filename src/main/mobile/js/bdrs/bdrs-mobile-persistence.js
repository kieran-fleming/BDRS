if (!window.bdrs) {
	bdrs = {};
}
if (!window.bdrs.persistence) {
	bdrs.persistence = {};
}
bdrs.persistence.dbname = 'csdb';
bdrs.persistence.dbdesc = 'ALA Citizen Science Database';
bdrs.persistence.dbsize = 4 * 1024 * 1024; // 4MB

persistence.store.websql.config(persistence, bdrs.persistence.dbname, bdrs.persistence.dbdesc, bdrs.persistence.dbsize);
persistence.search.config(persistence, persistence.store.websql.sqliteDialect);
persistence.debug = false;

var Image = persistence.define('Image', {
    path: "TEXT",
    data: "TEXT",
    type: "TEXT"
	});

var Settings = persistence.define('Settings', {
	    key: "TEXT",
	    value: "TEXT",
    });

var ServerObject = persistence.defineMixin('ServerObject', {
		server_id: "INT",
	    weight: "INT",
	});
	

var AttributeValue = persistence.define('AttributeValue', {
    	value: "TEXT",
    });
AttributeValue.is(ServerObject);

var Record = persistence.define('Record', {
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
		uploadedAt: "DATE"
	});
Record.hasMany('attributeValues', AttributeValue, 'record');
Record.hasMany('children', Record, 'parent');
Record.is(ServerObject);
   
var AttributeOption = persistence.define('AttributeOption', {
		value: "TEXT",
	});
AttributeOption.is(ServerObject);
	
var Attribute = persistence.define('Attribute', {
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

var SurveySpecies = persistence.define('SurveySpecies',{
});

var SpeciesAttribute = persistence.define('SurveyAttribute', {
	numericValue : "NUMERIC",
	stringValue : "TEXT",
	dateValue : "DATE",
	description : "TEXT"
});
SpeciesAttribute.hasOne('attribute', Attribute);
SpeciesAttribute.is(ServerObject);

var SpeciesProfile = persistence.define('SpeciesProfile', {
		content : "TEXT",
		weight : "INT",
		description : "TEXT",
		type : "TEXT",
		header : "TEXT"
});
SpeciesProfile.is(ServerObject);

var Species = persistence.define('Species', {
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
Species.hasMany('surveySpecies', SurveySpecies, 'species');
Species.is(ServerObject);
Species.textIndex('scientificName');
Species.textIndex('commonName');
	
var TaxonGroup = persistence.define('TaxonGroup', {
		name: "TEXT",
		image: "TEXT",
		thumbNail: "TEXT",
	});
TaxonGroup.hasMany('species', Species, 'taxonGroup');
TaxonGroup.hasMany('attributes', Attribute, 'taxonGroup');
TaxonGroup.is(ServerObject);

var Location = persistence.define('Location', {
		name: "TEXT",
		latitude : "NUMERIC",
		longitude : "NUMERIC"
	});
Location.hasMany('records', Record, 'location');
Location.is(ServerObject);

var CensusMethod = persistence.define('CensusMethod', {
	name: "TEXT",
	description: "TEXT",
	type: "TEXT",
	taxonomic: "BOOL"
});
CensusMethod.hasMany('attributes', Attribute, 'censusMethod');
CensusMethod.hasMany('records', Record, 'censusMethod');
CensusMethod.hasMany('children', CensusMethod, 'parent');
CensusMethod.is(ServerObject);

var User = persistence.define('User', {
    name: "TEXT",
    ident: "TEXT",
    firstname: "TEXT",
    lastname: "TEXT",
    server_url: "TEXT",
    portal_id: "NUMERIC"
});
User.hasMany('locations', Location, 'user');
User.is(ServerObject);

var Survey = persistence.define('Survey', {
	    name: "TEXT",
	    description: "TEXT",
	    active: "BOOL",
	    local: "BOOL",
	    date: "DATE"
	});
Survey.hasMany('locations', Location, 'survey');
Survey.hasMany('surveySpecies', SurveySpecies, 'survey');
Survey.hasMany('attributes', Attribute, 'survey');
Survey.hasMany('records', Record, 'survey');
Survey.hasMany('censusMethods', CensusMethod, 'survey');
Survey.is(ServerObject);

var SpeciesCount = persistence.define('SpeciesCount', {
		scientificName: "TEXT",
		count: "NUMERIC"
	});
SpeciesCount.hasOne('species', Species, 'count');

/**
 * 	These need to be added.
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

bdrs.mobile.addExampleCensusMethods = function() {
jQuery.mobile.pageLoading(false);
bdrs.mobile.Debug('Adding census data');
	Survey.all().each(function (survey) {
		var c = new CensusMethod({
			name: 'Location Information',
			description: 'Details about the location',
			type: 'General',
			taxonomic: false
		});
		persistence.add(c);
		c.attributes().add(new Attribute({	
			typeCode: "ST",
			required: false,
			name: 'Location Name',
			description: 'Location Name',
			tag: false,
			scope: 'survey'
		}));
		
		c.attributes().add(new Attribute({
			typeCode: "ST",
			required: false,
			name: 'Region',
			description: 'Region',
			tag: false,
			scope: 'survey'
		}));
		
		var a = new Attribute({
			typeCode: "SV",
			required: false,
			name: 'State',
			description: 'State',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a);
		a.options().add(new AttributeOption({value: 'Western Australia'}));
		a.options().add(new AttributeOption({value: 'Queensland'}));
		a.options().add(new AttributeOption({value: 'ACT'}));
		a.options().add(new AttributeOption({value: 'Victoria'}));
		a.options().add(new AttributeOption({value: 'South Australia'}));
		a.options().add(new AttributeOption({value: 'Tasmania'}));
		a.options().add(new AttributeOption({value: 'Victoria'}));
		c.attributes().add(a);
		
		////////////////////////////////////
		
		var d = new CensusMethod({
			name: 'Site Information',
			description: 'Details about the site',
			type: 'General',
			taxonomic: false
		});
		persistence.add(d);
		
		d.attributes().add(new Attribute({
			typeCode: "ST",
			required: false,
			name: 'Site Information 1',
			description: 'Site Information 1',
			tag: false,
			scope: 'survey'
		}));
		
		d.attributes().add(new Attribute({
			typeCode: "ST",
			required: false,
			name: 'Site Information 1',
			description: 'Site Information 1',
			tag: false,
			scope: 'survey'
		}));
		
		////////////////////////////////////
		
		var e = new CensusMethod({
			name: 'Climatic Information',
			description: 'Details about the climate',
			type: 'General',
			taxonomic: false
		});
		persistence.add(e);

		var a2 = new Attribute({
			typeCode: "SV",
			required: false,
			name: 'Climatic Zone',
			description: 'Climatic Zone',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a2);
		a2.options().add(new AttributeOption({value: 'Temperate'}));
		a2.options().add(new AttributeOption({value: 'Hot Dry'}));
		a2.options().add(new AttributeOption({value: 'Warm Humid'}));
		e.attributes().add(a2);
		
		////////////////////////////////////
		
		var f = new CensusMethod({
			name: 'Site Description',
			description: 'Detailed Site Description',
			type: 'General',
			taxonomic: false
		});
		persistence.add(f);
		
		var a3 = new Attribute({
			typeCode: "SV",
			required: false,
			name: 'Landform Pattern',
			description: 'Landform Pattern',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a3);
		a3.options().add(new AttributeOption({value: 'ALF - Alluvial Fan'}));
		a3.options().add(new AttributeOption({value: 'ALP - Alluvial Plain'}));
		a3.options().add(new AttributeOption({value: 'DUN - Dunefield'}));
		a3.options().add(new AttributeOption({value: 'ESC - Escarpment'}));
		a3.options().add(new AttributeOption({value: 'HIL - Hills'}));
		a3.options().add(new AttributeOption({value: 'LAV - Lava Plain'}));
		a3.options().add(new AttributeOption({value: 'MOU - Mountains'}));
		a3.options().add(new AttributeOption({value: 'PLA - Plain'}));
		a3.options().add(new AttributeOption({value: 'PLT - Plateau'}));
		a3.options().add(new AttributeOption({value: 'RIS - Rises'}));
		a3.options().add(new AttributeOption({value: 'SAN - Sand Plain'}));
		a3.options().add(new AttributeOption({value: 'SHF - Sheet Flood Fan'}));
		a3.options().add(new AttributeOption({value: 'TER - Terrace (alluvial)'}));
		f.attributes().add(a3);

		var a4 = new Attribute({
			typeCode: "SV",
			required: false,
			name: 'Landform Element',
			description: 'Landform Element',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a4);
		a4.options().add(new AttributeOption({value: 'Pit'}));
		a4.options().add(new AttributeOption({value: 'Peak'}));
		a4.options().add(new AttributeOption({value: 'Channel'}));
		a4.options().add(new AttributeOption({value: 'Ridge'}));
		a4.options().add(new AttributeOption({value: 'Pass'}));
		a4.options().add(new AttributeOption({value: 'Pool'}));
		a4.options().add(new AttributeOption({value: 'Plain'}));
		f.attributes().add(a4);

		var a5 = new Attribute({
			typeCode: "SV",
			required: false,
			name: 'Slope Category',
			description: 'Slope Category',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a5);
		a5.options().add(new AttributeOption({value: 'A - 0-3%'}));
		a5.options().add(new AttributeOption({value: 'B - 3-10%'}));
		a5.options().add(new AttributeOption({value: 'C - 10-20%'}));
		a5.options().add(new AttributeOption({value: 'D - 20-30%'}));
		a5.options().add(new AttributeOption({value: 'E - 30-45%'}));
		a5.options().add(new AttributeOption({value: 'F - >45%'}));
		f.attributes().add(a5);

		var a6 = new Attribute({
			typeCode: "SV",
			required: false,
			name: 'Aspect',
			description: 'Aspect',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a6);
		a6.options().add(new AttributeOption({value: 'North'}));
		a6.options().add(new AttributeOption({value: 'NorthEast'}));
		a6.options().add(new AttributeOption({value: 'East'}));
		a6.options().add(new AttributeOption({value: 'SouthEast'}));
		a6.options().add(new AttributeOption({value: 'South'}));
		a6.options().add(new AttributeOption({value: 'SouthWest'}));
		a6.options().add(new AttributeOption({value: 'West'}));
		a6.options().add(new AttributeOption({value: 'NorthWest'}));
		f.attributes().add(a6);
		
		////////////////////////////////////
		
		persistence.add(new CensusMethod({
			name: 'Photo-point Information',
			description: 'Photo Points for this site',
			type: 'General',
			taxonomic: false
		}));
		
		////////////////////////////////////
		
		var g = new CensusMethod({
			name: 'Basal Wedge Sweeps',
			description: 'Basal Wedge Sweeps',
			type: 'Vegetation',
			taxonomic: true
		});
		persistence.add(g);
		
		var a7 = new Attribute({
			typeCode: "IN",
			required: false,
			name: 'Average Circumference',
			description: 'Average Circumference (mm)',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a7);
		g.attributes().add(a7);
		
		////////////////////////////////////
		
		var h = new CensusMethod({
			name: 'LAI Readings',
			description: 'Leaf Area Index Readings',
			type: 'Vegetation',
			taxonomic: false
		});
		persistence.add(h);
		
		////////////////////////////////////
		
		var i = new CensusMethod({
			name: 'Herbarium Specimens',
			description: 'Herbarium Specimens',
			type: 'Vegetation',
			taxonomic: true
		});
		persistence.add(i);
		
		i.attributes().add(new Attribute({
			typeCode: "ST",
			required: false,
			name: 'Specimen ID',
			description: 'Specimen ID',
			tag: false,
			scope: 'survey'
		}));

		var a8 = new Attribute({
			typeCode: "SV",
			required: false,
			name: 'Collector',
			description: 'Collector',
			tag: false,
			scope: 'survey'
		});
		persistence.add(a8);
		a8.options().add(new AttributeOption({value: 'AJ'}));
		a8.options().add(new AttributeOption({value: 'Andrew'}));
		a8.options().add(new AttributeOption({value: 'Jim'}));
		a8.options().add(new AttributeOption({value: 'Scott'}));
		a8.options().add(new AttributeOption({value: 'Piers'}));
		i.attributes().add(a8);
		
		i.attributes().add(new Attribute({
			typeCode: "ST",
			required: false,
			name: 'Storage',
			description: 'Storage',
			tag: false,
			scope: 'survey'
		}));
		
		////////////////////////////////////
		
		var j = new CensusMethod({
			name: 'Plant Genetic Samples',
			description: 'Plant Genetic Samples',
			type: 'Vegetation',
			taxonomic: true
		});
		persistence.add(j);

		var k = new CensusMethod({
			name: 'Site Structural Information',
			description: 'Dominant species and growth forms',
			type: 'Vegetation',
			taxonomic: false
		});
		persistence.add(k);

		var l = new CensusMethod({
			name: 'Bulk Density',
			description: 'Bulk Density Readings',
			type: 'Soil',
			taxonomic: false
		});
		persistence.add(l);
		var m = new CensusMethod({
			name: 'Standard Soil Samples',
			description: 'Standard sampling',
			type: 'Soil',
			taxonomic: false
		});
		persistence.add(m);
		var n = new CensusMethod({
			name: 'Soil Cores',
			description: 'Soil core and full profile description',
			type: 'Soil',
			taxonomic: false
		});
		persistence.add(n);
		var o = new CensusMethod({
			name: 'Soil Meta-genomics',
			description: 'Soil meta-genomics',
			type: 'Soil',
			taxonomic: false
		});
		persistence.add(o);
		
        ////////////////////////////////////
        
        var p = new CensusMethod({
            name: 'Point Intercept',
            description: 'Point Intercept Collection',
            type: 'Vegetation',
            taxonomic: false
        });
        persistence.add(p);
        
        p.attributes().add(new Attribute({
            typeCode: "ST",
            required: true,
            name: 'Point Intercept ID',
            description: 'Point Intercept ID',
            tag: false,
            scope: 'survey'
        }));
        
        ////////////////////////////////////
        
        var r = new CensusMethod({
            name: 'Substrate',
            description: 'The natural environment in which an organism lives',
            type: 'Vegetation',
            taxonomic: false,
            parent: p
        });
        persistence.add(p);
        p.children().add(r);
        
        r.attributes().add(new Attribute({
            typeCode: "ST",
            required: false,
            name: 'Specimen ID',
            description: 'Specimen ID',
            tag: false,
            scope: 'survey'
        }));
        
        var a9 = new Attribute({
            typeCode: "SV",
            required: false,
            name: 'Substrate',
            description: 'Substrate',
            tag: false,
            scope: 'survey'
        });
        persistence.add(a9);
        a9.options().add(new AttributeOption({value: 'Rock'}));
        a9.options().add(new AttributeOption({value: 'Bare'}));
        a9.options().add(new AttributeOption({value: 'Litter'}));
        a9.options().add(new AttributeOption({value: 'Crust'}));
        a9.options().add(new AttributeOption({value: 'Gravel'}));
        a9.options().add(new AttributeOption({value: 'Unknown'}));
        r.attributes().add(a9);
        
        var s = new CensusMethod({
            name: 'Observation',
            description: 'Receiving knowledge of the outside world through our senses, scientific tools or instruments.',
            type: 'Vegetation',
            taxonomic: true,
            parent: r
        });
        persistence.add(s);
        r.children().add(s);
        s.attributes().add(new Attribute({
            typeCode: "DE",
            required: false,
            name: 'Height',
            description: 'Height',
            tag: false,
            scope: 'record'
        }));
        
        var a10 = new Attribute({
            typeCode: "SV",
            required: false,
            name: 'Maturity',
            description: 'Maturity',
            tag: false,
            scope: 'survey'
        });
        persistence.add(a10);
        a10.options().add(new AttributeOption({value: 'Flowering'}));
        a10.options().add(new AttributeOption({value: 'Fruiting'}));
        a10.options().add(new AttributeOption({value: 'Senescent'}));
        a10.options().add(new AttributeOption({value: 'Dead'}));
        a10.options().add(new AttributeOption({value: 'In Canopy Sky'}));
        s.attributes().add(a10);
        
        ////////////////////////////////////
        
		// General Methods
		survey.censusMethods().add(c);
		survey.censusMethods().add(d);
		survey.censusMethods().add(e);
		survey.censusMethods().add(f);
		
		// Veg
		survey.censusMethods().add(g);
		survey.censusMethods().add(h);
		survey.censusMethods().add(i);
		survey.censusMethods().add(j);
		survey.censusMethods().add(k);
		survey.censusMethods().add(p);
		
		// Soil
		survey.censusMethods().add(l);
		survey.censusMethods().add(m);
		survey.censusMethods().add(n);
		survey.censusMethods().add(o);
		
		persistence.flush();
		jQuery.mobile.pageLoading(true);
	});
	//alert('Sample census methods added.');
}


//bdrs.persistence.db = openDatabase('mydb', '1.0', 'my first database', 2 * 1024 * 1024);
//db.transaction(function (tx) {
//  tx.executeSql('CREATE TABLE IF NOT EXISTS foo (id unique, text)');
//  tx.executeSql('INSERT INTO foo (id, text) VALUES (1, "synergies")');
//});