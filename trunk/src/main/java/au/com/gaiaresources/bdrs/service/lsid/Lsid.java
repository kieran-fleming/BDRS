package au.com.gaiaresources.bdrs.service.lsid;

import java.util.Properties;

import au.com.gaiaresources.bdrs.db.Persistent;

public class Lsid {
    public static final String LSID_AUTH_KEY = "lsid.authority";

    private String authority;
    private String namespace;
    private int objectId;
    private String version;

    // URN:LSID:<Authority>:<Namespace>:<ObjectID>[:<Version>]

    public Lsid(Properties lsidProperties, Persistent persistent) {
        this.authority = lsidProperties.getProperty(LSID_AUTH_KEY, "localhost");
        this.namespace = persistent.getClass().getSimpleName();
        this.objectId = persistent.getId();
        this.version = null;
    }

    public Lsid(Properties lsidProperties, String lsid) {
        if (lsid == null) {
            throw new NullPointerException();
        }
        if (!lsid.toLowerCase().startsWith("urn:lsid:")) {
            throw new IllegalArgumentException(
                    "LSID values must start with \"URN:LSID:\"");
        }

        String[] split = lsid.split(":");
        if (split.length < 5 || split.length > 6) {
            throw new IllegalArgumentException("Malformed LSID \"" + lsid
                    + "\"");
        }

        //urn = split[0];
        //lsid = split[1];
        try {
            authority = split[2];
            namespace = split[3];
            objectId = Integer.parseInt(split[4]);
            if (split.length > 5) {
                version = split[5];
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Malformed LSID \"" + lsid
                    + "\"");
        }
    }

    public String getAuthority() {
        return authority;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getObjectId() {
        return objectId;
    }

    public String getVersion() {
        return version;
    }

    public String toString() {
        String lsid = String.format("urn:lsid:%s:%s:%d", this.authority, this.namespace, this.objectId);
        if (version != null && !version.isEmpty()) {
            lsid = lsid + ":" + this.version;
        }

        return lsid;
    }

}
