<%@ page contentType="text/javascript" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="db" uri="/WEB-INF/db.tld"%>

dropTablesExclRecs();
createTables();
	//var curPercentage = 0;
	//var errorPercentage = 0;
	stepSize = ${stepSize};
GR159DB.transaction(function(transaction) {


		
	transaction.executeSql("INSERT INTO userdefinition (userdefinitionid, emailaddress, firstname, lastname, registrationkey, name) VALUES (?,?,?,?,?,?);", 
		["<db:sanitise sql="${user.id}"/>","<db:sanitise sql="${user.emailAddress}"/>","<db:sanitise sql="${user.firstName}"/>", "<db:sanitise sql="${user.lastName}"/>", "<db:sanitise sql="${user.registrationKey}"/>", "<db:sanitise sql="${user.name}"/>"], 
		transactionSucces, 
		transactionError
	);


	<c:forEach var="taxon" items="${taxa}">
	
		transaction.executeSql("INSERT INTO taxongroup (taxongroupid, name, thumbnail, image) VALUES (?,?,?,?);", 
			["<db:sanitise sql="${taxon.id}"/>","<db:sanitise sql="${taxon.name}"/>","<db:sanitise sql="${taxon.thumbNail}"/>","<db:sanitise sql="${taxon.image}"/>"], 
			transactionSucces, 
			transactionError
		);
		
		<c:forEach var="attribute" items="${taxon.attributes}" varStatus="index">
		
			transaction.executeSql("INSERT INTO attribute (id, name, typecode, required, description, tag) VALUES (?,?,?,?,?,?);", 
				["<db:sanitise sql="${attribute.id}"/>", "<db:sanitise sql="${attribute.name}"/>", "<db:sanitise sql="${attribute.typeCode}"/>", "<db:sanitise sql="${attribute.required}"/>", "<db:sanitise sql="${attribute.description}"/>", "<db:sanitise sql="${attribute.tag}"/>"],  
				transactionSucces, 
				transactionError
			);
			
			transaction.executeSql("INSERT INTO taxongroup_attributes (id, attributeid, pos) VALUES (?,?,?);", 
				["<db:sanitise sql="${taxon.id}"/>", "<db:sanitise sql="${attribute.id}"/>", ${index.count}],  
				transactionSucces, 
				transactionError
			);
			
			<c:forEach var="option" items="${attribute.options}" varStatus="optionindex">
				
				/*transaction.executeSql("INSERT INTO attributeoption (id, pos, value, attributeid) VALUES (?,?,?,?);", 
					["<db:sanitise sql="${option.id}"/>", "${optionindex.count}"/>", "<db:sanitise sql="${option.value}"/>", "<db:sanitise sql="${attribute.id}"/>"], 
					transactionSucces, 
					transactionError
				);*/
				
			</c:forEach>
			
		</c:forEach>
		
	</c:forEach>
	
	
	
	
	
	<c:forEach var="survey" items="${surveys}">
		
		transaction.executeSql("INSERT INTO survey (id, name, description, public, active) VALUES (?,?,?,?,?);", 
			["<db:sanitise sql="${survey.id}"/>","<db:sanitise sql="${survey.name}"/>","<db:sanitise sql="${survey.description}"/>","${survey.public}", "${survey.active}"],  
			transactionSucces, 
			transactionError
		);
		
		<c:forEach var="location" items="${survey.locations}" varStatus="locationindex">
			
			<jsp:useBean id="location" type="au.com.gaiaresources.bdrs.model.location.Location" />
			
			transaction.executeSql("INSERT INTO location (locationid, name, latitude, longitude) VALUES (?,?,?,?);", 
				["<db:sanitise sql="${location.id}"/>","<db:sanitise sql="${location.name}"/>","<%= location.getLocation().getY() %>","<%= location.getLocation().getX() %>"],  
				transactionSucces, 
				transactionError
			);
			
			transaction.executeSql("INSERT INTO survey_location (surveyid, locationid, pos) VALUES (?,?,?);", 
				["<db:sanitise sql="${survey.id}"/>","<db:sanitise sql="${location.id}"/>", ${locationindex.count}],  
				transactionSucces, 
				transactionError
			);
		
		</c:forEach>
		
		
		<c:forEach var="attribute" items="${survey.attributes}" varStatus="index">
		
			transaction.executeSql("INSERT INTO attribute (id, name, typecode, required, description, tag) VALUES (?,?,?,?,?,?);", 
				["<db:sanitise sql="${attribute.id}"/>", "<db:sanitise sql="${attribute.name}"/>", "<db:sanitise sql="${attribute.typeCode}"/>", "<db:sanitise sql="${attribute.required}"/>", "<db:sanitise sql="${attribute.description}"/>", "<db:sanitise sql="${attribute.tag}"/>"],  
				transactionSucces, 
				transactionError
			);
			
			transaction.executeSql("INSERT INTO survey_attributes (id, attributeid, pos) VALUES (?,?,?);", 
				["<db:sanitise sql="${survey.id}"/>", "<db:sanitise sql="${attribute.id}"/>", ${index.count}],  
				transactionSucces, 
				transactionError
			);
			
			<c:forEach var="option" items="${attribute.options}" varStatus="optionindex">
			
				transaction.executeSql("INSERT INTO attributeoption (id, pos, value, attributeid) VALUES (?,?,?,?);", 
					["<db:sanitise sql="${option.id}"/>", ${optionindex.count}, "<db:sanitise sql="${option.value}"/>", "<db:sanitise sql="${attribute.id}"/>"],  
					transactionSucces, 
					transactionError
				);
			
			</c:forEach>
		
		</c:forEach>
		
	</c:forEach>
	
	
	
	<c:forEach var="species" items="${indicatorSpecies}">
	
		transaction.executeSql("INSERT OR IGNORE INTO indicatorspecies (indicatorspeciesid, scientificName, commonName, taxongroupid) VALUES (?,?,?,?);", 
			["<db:sanitise sql="${species.id}"/>", "<db:sanitise sql="${species.scientificName}"/>", "<db:sanitise sql="${species.commonName}"/>", "<db:sanitise sql="${species.taxonGroup.id}"/>"],  
			transactionSucces, 
			transactionError
		);
		
		<c:forEach var="attribute" items="${species.attributes}">
		
			transaction.executeSql("INSERT OR IGNORE INTO indicatorspeciesattribute (indicatorspeciesattributeid, numericvalue, description, datevalue, stringvalue, attributeid, indicatorspeciesid) VALUES (?,?,?,?,?,?,?);",
				["<db:sanitise sql="${attribute.id}"/>","<db:sanitise sql="${attribute.numericValue}"/>","<db:sanitise sql="${attribute.description}"/>","<db:sanitise sql="${attribute.dateValue}"/>","<db:sanitise sql="${attribute.stringValue}"/>","<db:sanitise sql="${attribute.attribute.id}"/>","<db:sanitise sql="${species.id}"/>"], 
				transactionSucces,
				transactionError
			);
		
		</c:forEach>
		
		<c:forEach var="infoItem" items="${species.infoItems}">
               
			transaction.executeSql("INSERT OR IGNORE INTO indicatorspeciesprofile (indicatorspeciesprofileid, type, content, header, description, indicatorspeciesid, weight) VALUES (?,?,?,?,?,?,?);",
					["<db:sanitise sql="${infoItem.id}"/>","<db:sanitise sql="${infoItem.type}"/>","<db:sanitise sql="${infoItem.content}"/>","<db:sanitise sql="${infoItem.header}"/>","<db:sanitise sql="${infoItem.description}"/>","<db:sanitise sql="${species.id}"/>","<db:sanitise sql="${infoItem.weight}"/>"], 
					transactionSucces,
					transactionError
				);
				
		</c:forEach>
		
	</c:forEach>
	
	// A mapping of survey to species
	<c:forEach var="entry" items="${surveySpeciesMap}">
		<c:forEach var="speciesIdForSurvey" items="${entry.value}">
		//console.log("surveyid ${entry.key}");
		//console.log("speciesid ${speciesIdForSurvey}");
		
		transaction.executeSql("INSERT INTO survey_indicatorspecies (surveyid, indicatorspeciesid) VALUES (?,?);", 
				["<db:sanitise sql="${entry.key}"/>", "<db:sanitise sql="${speciesIdForSurvey}"/>"],  
				transactionSucces, 
				transactionError
			);
		
		</c:forEach>
	</c:forEach>
	
	
	
	
	

	
	
	
	
	

transaction.executeSql("SELECT * FROM record;",[], function(){alert("finished download db");},function(transaction, error){alert("error download db");});

});