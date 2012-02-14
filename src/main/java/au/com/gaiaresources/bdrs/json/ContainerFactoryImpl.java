package au.com.gaiaresources.bdrs.json;

import java.util.List;
import java.util.Map;

/**
 * An implementation of the {@link org.json.simple.parser.ContainerFactory} to
 * register the {@link JSONObject} and {@link JSONArray} as the desired Java 
 * representations of the Javascript Object and Array types.
 */
public class ContainerFactoryImpl implements org.json.simple.parser.ContainerFactory {

    @SuppressWarnings("rawtypes")
    @Override
    public Map createObjectContainer() {
        return new JSONObject();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List creatArrayContainer() {
        return new JSONArray();
    }

}
