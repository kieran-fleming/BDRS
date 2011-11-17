package au.com.gaiaresources.bdrs.controller.attribute.formfield;

/**
 * The Darwin Core Fields
 * @author timo
 *
 */
public enum RecordPropertyType {
	 NUMBER("Number", "Individual Count"),
	 SPECIES("Species"),
	 LOCATION("Location"),
	 POINT("Point"),
	 ACCURACY("AccuracyInMeters","Accuracy (meters)"),
	 WHEN("When","Date"),
	 TIME("Time"),
	 NOTES("Notes");
	 
	 String name;
	 String defaultDescription;
	 
	 private RecordPropertyType(String name, String description) {
	        this.name = name;
	        this.defaultDescription = description;
	    }
	 
	 private RecordPropertyType(String name) {
	        this.name = name;
	        this.defaultDescription = name;
	    }
	 
	 public String getName(){
		 return this.name;
	 }
	 
	 public String getDefaultDescription(){
		 return this.defaultDescription;
	 }
}


