package au.com.gaiaresources.bdrs.json;

import org.json.simple.JSONAware;
import org.json.simple.JSONStreamAware;

/**
 * A marker interface for all enumerations that can be serialized to JSON. 
 */
public interface JSONEnum extends JSONAware, JSONStreamAware {

}
