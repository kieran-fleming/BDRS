package au.com.gaiaresources.bdrs.servlet.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;

public class PortalSelectionFilterMatcher {

    private Logger log = Logger.getLogger(getClass());
    
    private PortalDAO portalDAO;
    
    public PortalSelectionFilterMatcher(PortalDAO portalDAO) {
        super();
        this.portalDAO = portalDAO;
    }
    
    public PortalMatches match(Session sesh, String url) {
        
        Portal matchedPortal = null;
        PortalEntryPoint matchedEntryPoint = null;
        Portal defaultPortal = null;
        
        List<PortalEntryPoint> invalidPatternList = new ArrayList<PortalEntryPoint>(); 

        Pattern pattern;
        Matcher matcher;
        
        for (Portal portal : portalDAO.getPortals(sesh)) {
            if (portal.isDefault()) {
                if (defaultPortal == null) {
                    defaultPortal = portal;
                } else {
                    log.debug("Multiple default portals located using the first.");
                }
            }

            for (PortalEntryPoint entryPoint : portalDAO.getPortalEntryPoints(sesh, portal)) {
                try {
                    pattern = Pattern.compile(entryPoint.getPattern());
                    matcher = pattern.matcher(url);
                    if (matcher.find()) {
                        // Found a regex match.
                        if (matchedPortal == null
                                && matchedEntryPoint == null) {
                            matchedPortal = portal;
                            matchedEntryPoint = entryPoint;
                        } else {
                            log.debug("Multiple portal matches. Using the first.");
                            log.debug("URL = " + url);
                            log.debug("Pattern = "
                                    + entryPoint.getPattern());
                        }
                    }
                } catch(PatternSyntaxException e) {
                    invalidPatternList.add(entryPoint);
                }
            }
        }

        return new PortalMatches(matchedPortal, matchedEntryPoint, defaultPortal, invalidPatternList);
    }
}
