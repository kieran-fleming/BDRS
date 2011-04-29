package au.com.gaiaresources.bdrs.model.taxa;

import java.util.List;

import org.hibernate.classic.Session;

public interface SpeciesProfileDAO {
    SpeciesProfile createSpeciesProfile(String header, String content,
            String type);

    SpeciesProfile createSpeciesProfile(String header, String description,
            String content, String type);

    List<SpeciesProfile> getSpeciesProfileForSpecies(int id);

    SpeciesProfile save(SpeciesProfile profile);

    SpeciesProfile save(Session sesh, SpeciesProfile profile);

    SpeciesProfile getSpeciesProfileBySourceDataId(Session sesh,
            IndicatorSpecies species, String sourceDataIdKey,
            String sourceDataId);

    void delete(SpeciesProfile delProf);

    List<SpeciesProfile> getSpeciesProfileByType(String[] types);
    
}
