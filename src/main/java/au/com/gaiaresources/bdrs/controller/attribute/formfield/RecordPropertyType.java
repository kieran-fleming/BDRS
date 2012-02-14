package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import java.io.IOException;
import java.io.Writer;

import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;

/**
 * The Darwin Core Fields
 * @author timo
 *
 */
public enum RecordPropertyType implements JSONEnum {
     SPECIES("Species"),
     NUMBER("Number", "Individual Count"),
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
     
     @Override
     public void writeJSONString(Writer out) throws IOException {
         JSONEnumUtil.writeJSONString(out, this);
     }

     @Override
     public String toJSONString() {
         return JSONEnumUtil.toJSONString(this);
     }
}
