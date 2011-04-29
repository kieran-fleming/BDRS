package au.com.gaiaresources.bdrs.service.user;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.com.gaiaresources.bdrs.config.ProfileConfig;

@Service
public class UserMetaDataService {

    private Logger log = Logger.getLogger(getClass());

    private InputStream getUserMetadataConfig(HttpServletRequest req)
            throws FileNotFoundException {

        String filename = ProfileConfig.getProfileConfig().getXmlConfigFilename();
        String baseResourcePath = req.getSession().getServletContext().getRealPath("");
        FileInputStream stream = new FileInputStream(baseResourcePath
                + filename);
        return stream;
    }

    // if we want to have profiles specific to a role (or roles), accept a string
    // argument here which we can then use to parse and choose which profile tag we want
    // to return the user meta data for
    // I don't like it that we have to pass a HttpServletRequest obj in here... but I can't
    // find another way to reliably get the servlet context which in turn gets the path for
    // our data...
    public List<UserMetaData> getMetadataMap(HttpServletRequest req)
            throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getUserMetadataConfig(req));

        List<UserMetaData> result = new ArrayList<UserMetaData>();

        NodeList profileList = doc.getElementsByTagName("profile");
        // Don't worry about roles at the moment... Just use the first profile entry...
        // would search for the profile entry with the appropriate role tag
        if (profileList.getLength() == 0) {
            throw new Exception("Can't find any profile configurations");
        }

        // note we don't actually edit the xml document here, it can cause problems with the iteration if nodes are moved or deleted.
        for (Node profileChild = profileList.item(0).getFirstChild(); profileChild.getNextSibling() != null; profileChild = profileChild.getNextSibling()) {
            if (profileChild.getNodeName() != null
                    && profileChild.getNodeName().equals("metadata")) {
                UserMetaData umd = readMetaDataNode(profileChild);
                if (StringUtils.hasLength(umd.getKey())) {
                    result.add(umd);
                } else {
                    log.warn("User profile config has a metadata entry with no key - ignoring the metadata entry");
                }
            }
        }
        return result;
    }

    private UserMetaData readMetaDataNode(Node node) throws Exception {
        UserMetaData umd = new UserMetaData();

        for (Node metaChild = node.getFirstChild(); metaChild.getNextSibling() != null; metaChild = metaChild.getNextSibling()) {
            if (isNodeValue(metaChild, "key")) {
                umd.setKey(getNodeValue(metaChild));
            }
            if (isNodeValue(metaChild, "displayName")) {
                umd.setDisplayName(getNodeValue(metaChild));
            }
            if (isNodeValue(metaChild, "validation")) {
                umd.setValidation(getNodeValue(metaChild));
            }
            if (isNodeValue(metaChild, "type")) {
                umd.setType(getNodeValue(metaChild));
            }
        }
        
        if (!StringUtils.hasLength(umd.getKey())) {
            throw new Exception("Missing field 'key' in profile configuration");
        }
        if (!StringUtils.hasLength(umd.getDisplayName())) {
            throw new Exception("Missing field 'displayName' in profile configuration");
        }
        if (umd.getType() == null) {
            throw new Exception("Missing field 'type' in profile configuration");
        }
        return umd;
    }

    private Boolean isNodeValue(Node node, String expectedNodeName) {
        return (node.getNodeName() != null
                && node.getNodeName().equals(expectedNodeName) && node.getFirstChild() != null);

    }

    private String getNodeValue(Node node) {
        return node.getFirstChild().getNodeValue();
    }
}
