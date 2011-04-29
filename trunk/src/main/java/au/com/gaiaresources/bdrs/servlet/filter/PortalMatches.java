package au.com.gaiaresources.bdrs.servlet.filter;

import java.util.List;

import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;

public class PortalMatches {
    
    private Portal matchedPortal = null;
    private PortalEntryPoint matchedEntryPoint = null;
    private Portal defaultPortal = null;
    private List<PortalEntryPoint> invalidPatternList;
    
    public PortalMatches(Portal matchedPortal,
            PortalEntryPoint matchedEntryPoint, Portal defaultPortal, List<PortalEntryPoint> invalidPatternList) {
        super();
        this.matchedPortal = matchedPortal;
        this.matchedEntryPoint = matchedEntryPoint;
        this.defaultPortal = defaultPortal;
        this.invalidPatternList = invalidPatternList;
    }
    
    public Portal getMatchedPortal() {
        return matchedPortal;
    }
    public PortalEntryPoint getMatchedEntryPoint() {
        return matchedEntryPoint;
    }
    public Portal getDefaultPortal() {
        return defaultPortal;
    }
   public List<PortalEntryPoint> getInvalidPatternList() {
        return invalidPatternList;
    }
}
