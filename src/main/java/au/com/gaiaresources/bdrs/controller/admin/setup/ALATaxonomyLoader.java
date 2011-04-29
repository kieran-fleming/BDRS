package au.com.gaiaresources.bdrs.controller.admin.setup;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import au.com.bytecode.opencsv.CSVReader;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;

public class ALATaxonomyLoader {

    public static final String COMMON_NAME_DESCRIPTION = "Common Names";
    public static final String COMMON_NAME_HEADER = "COMMONNAME";

    public static final String PUBLICATION_DESCRIPTION = "Publication";
    public static final String PUBLICATION_HEADER = "PUBLICATION";

    public static final String SCIENTIFIC_NAME_DESCRIPTION = "Scientific Name";
    public static final String SCIENTIFIC_NAME_HEADER = "SCIENTIFICNAME";

    private Logger log = Logger.getLogger(getClass());

    private MetadataDAO metadataDAO;
    private SpeciesProfileDAO speciesProfileDAO;
    private TaxaDAO taxaDAO;

    private Map<String, List<CommonNameRow>> commonNameRowMap = new HashMap<String, List<CommonNameRow>>(
            38412);
    private Map<String, TaxonGroup> taxonGroupMap = new HashMap<String, TaxonGroup>(
            9567);

    private Map<String, TaxonRank> taxonRankMap = new HashMap<String, TaxonRank>(
            28);

    private Map<IndicatorSpecies, Map<String, SpeciesProfile>> speciesProfileSessionCache = new HashMap<IndicatorSpecies, Map<String, SpeciesProfile>>();
    private Map<String, IndicatorSpecies> taxonSessionCache = new HashMap<String, IndicatorSpecies>();

    public ALATaxonomyLoader(TaxaDAO taxaDAO,
            SpeciesProfileDAO speciesProfileDAO, MetadataDAO metadataDAO) {
        this.taxaDAO = taxaDAO;
        this.speciesProfileDAO = speciesProfileDAO;
        this.metadataDAO = metadataDAO;
        initTaxonRanks();
    }

    private void initTaxonRanks() {
        taxonRankMap.put("kingdom", TaxonRank.KINGDOM);
        taxonRankMap.put("phylum", TaxonRank.PHYLUM);
        taxonRankMap.put("supragenericname", TaxonRank.SUPRAGENERICTAXON);
        taxonRankMap.put("unranked", TaxonRank.UNCATALOGUED_RANKS);
        taxonRankMap.put("subfamily", TaxonRank.SUBFAMILY);
        taxonRankMap.put("subgenus", TaxonRank.SUBGENUS);
        taxonRankMap.put("section", TaxonRank.SECTION);
        taxonRankMap.put("subsection", TaxonRank.SUBSECTION);
        taxonRankMap.put("subseries", TaxonRank.SUBSERIES);
        taxonRankMap.put("superorder", TaxonRank.SUPERORDER);
        taxonRankMap.put("suborder", TaxonRank.SUBORDER);
        taxonRankMap.put("series", TaxonRank.SERIES);
        taxonRankMap.put("infraspecificname", TaxonRank.INFRASPECIFICTAXON);
        taxonRankMap.put("subspecies", TaxonRank.SUBSPECIES);
        taxonRankMap.put("species", TaxonRank.SPECIES);
        taxonRankMap.put("family", TaxonRank.FAMILY);
        taxonRankMap.put("class", TaxonRank.CLASS);
        taxonRankMap.put("order", TaxonRank.ORDER);
        taxonRankMap.put("genus", TaxonRank.GENUS);
        taxonRankMap.put("variety", TaxonRank.VARIETY);
        taxonRankMap.put("cultivar", TaxonRank.CULTIVAR);
        taxonRankMap.put("tribe", TaxonRank.TRIBE);
        taxonRankMap.put("form", TaxonRank.FORM);
        taxonRankMap.put("subform", TaxonRank.SUBFORM);
        taxonRankMap.put("superfamily", TaxonRank.SUPERFAMILY);
        taxonRankMap.put("subvariety", TaxonRank.SUBVARIETY);
        taxonRankMap.put("subphylum", TaxonRank.SUBPHYLUM);
        taxonRankMap.put("infragenericname", TaxonRank.INFRAGENERICTAXON);
    }

    public void loadData(Session sesh, File nameUsages, File afdCommonNames,
            File apniCommonNames) throws IOException {

        log.debug("Loading AFD Common Names");
        loadCommonNames(sesh, afdCommonNames);

        log.debug("Loading APNI Common Names");
        loadCommonNames(sesh, apniCommonNames);

        log.debug("Loading Taxonomy");
        loadTaxonomy(sesh, nameUsages);
        loadTaxonomyHierarchy(sesh, nameUsages);
    }

    private void loadTaxonomyHierarchy(Session sesh, File nameUsages)
            throws IOException {
        sesh.setFlushMode(FlushMode.MANUAL);
        Transaction tx = sesh.beginTransaction();

        String nub_id;
        String parent_nub_id;
//        String lsid;
        String accepted_id;
//        String accepted_lsid;
//        String name_id;
//        String canonical_name;
//        String author;
//        String portal_rank_id;
//        String rank;
//        String lft;
//        String rgt;
//        String kingdom_id;
//        String kingdom;
//        String phylum_id;
//        String phylum;
//        String class_id;
//        String klass;
//        String order_id;
//        String order;
//        String family_id;
//        String family;
//        String genus_id;
//        String genus;
//        String species_id;
//        String species;
//        String source;

        ReferenceMap taxonSoftRefMap = new ReferenceMap(ReferenceMap.WEAK,
                ReferenceMap.WEAK, true);
        IndicatorSpecies parent;
        IndicatorSpecies child;
        long start = System.currentTimeMillis();
        long taxaCount = 0;
        long taxaTotal = 0;
        int rowCount = 1;

        CSVReader reader = new CSVReader(new FileReader(nameUsages), '\t');

        // Discard the header
        for (int i = 0; i < rowCount; i++) {
            reader.readNext();
        }

        for (String[] line = reader.readNext(); line != null; line = reader.readNext()) {
            if ((rowCount % 1000) == 0) {
                log.debug("Reading Taxon Hierarchy Row: " + rowCount);
            }

            if (taxaCount > 49) {
                sesh.flush();
                sesh.clear();
                tx.commit();

                taxaTotal = taxaTotal + taxaCount;
                log.debug("Commited " + taxaCount
                        + " taxa parenting. Time Delta "
                        + (System.currentTimeMillis() - start)
                        + "ms. Total Taxa Count " + taxaTotal);
                start = System.currentTimeMillis();
                taxaCount = 0;

                tx = sesh.beginTransaction();
                //log.debug(taxonSoftRefMap.size());

            }

            nub_id = line[0];
            parent_nub_id = line[1];
//            lsid = line[2];
            accepted_id = line[3];
//            accepted_lsid = line[4];
//            name_id = line[5];
//            canonical_name = line[6];
//            author = line[7];
//            portal_rank_id = line[8];
//            rank = line[9];
//            lft = line[10];
//            rgt = line[11];
//            kingdom_id = line[12];
//            kingdom = line[13];
//            phylum_id = line[14];
//            phylum = line[15];
//            class_id = line[16];
//            klass = line[17];
//            order_id = line[18];
//            order = line[19];
//            family_id = line[20];
//            family = line[21];
//            genus_id = line[22];
//            genus = line[23];
//            species_id = line[24];
//            species = line[25];
//            source = line[26];

            if (accepted_id.isEmpty()) {

                child = (IndicatorSpecies) taxonSoftRefMap.get(nub_id);
                if (child == null) {
                    child = taxaDAO.getIndicatorSpeciesBySourceDataID(sesh, nub_id);
                    if (child != null) {
                        taxonSoftRefMap.put(nub_id, child);
                    }
                }

                if (child != null) {
                    if (parent_nub_id.isEmpty()) {
                        parent = null;
                    } else {
                        parent = (IndicatorSpecies) taxonSoftRefMap.get(parent_nub_id);
                        if (parent == null) {
                            parent = taxaDAO.getIndicatorSpeciesBySourceDataID(sesh, parent_nub_id);
                            if (parent != null) {
                                taxonSoftRefMap.put(parent_nub_id, parent);
                            }
                        }
                    }

                    child.setParent(parent);
                    taxaDAO.save(sesh, child);
                    taxaCount++;
                }
            }
            rowCount++;
        }

        sesh.flush();
        sesh.clear();
        tx.commit();
        sesh.setFlushMode(FlushMode.AUTO);
    }

    private void loadTaxonomy(Session sesh, File nameUsages) throws IOException {

        sesh.setFlushMode(FlushMode.MANUAL);
        Transaction tx = sesh.beginTransaction();

        String nub_id;
//        String parent_nub_id;
        String lsid;
        String accepted_id;
//        String accepted_lsid;
//        String name_id;
        String canonical_name;
        String author;
//        String portal_rank_id;
        String rank;
//        String lft;
//        String rgt;
//        String kingdom_id;
//        String kingdom;
//        String phylum_id;
//        String phylum;
//        String class_id;
//        String klass;
//        String order_id;
//        String order;
//        String family_id;
        String family;
//        String genus_id;
//        String genus;
//        String species_id;
        String species;
        String source;

        TaxonGroup life = getOrCreateTaxonGroup(sesh, "Life");
        taxonGroupMap.put("Life", life);

        long start = System.currentTimeMillis();
        long taxaCount = 0;
        long taxaTotal = 0;
        TaxonGroup taxonGroup;
        Set<SpeciesProfile> speciesProfileSet = new HashSet<SpeciesProfile>();
        SpeciesProfile commonNameProfile;
        SpeciesProfile publicationProfile;
        SpeciesProfile scientificNameProfile;
        IndicatorSpecies taxon;
        List<CommonNameRow> commonNameRowList;

        int rowCount = 1;
        CSVReader reader = new CSVReader(new FileReader(nameUsages), '\t');

        // Discard the header
        for (int i = 0; i < rowCount; i++) {
            reader.readNext();
        }

        for (String[] line = reader.readNext(); line != null; line = reader.readNext()) {
            if ((rowCount % 1000) == 0) {
                log.debug("Reading Taxon Row: " + rowCount);
            }

            if (taxaCount > 49) {
                sesh.flush();
                clearSessionCache();
                sesh.clear();
                tx.commit();

                taxaTotal = taxaTotal + taxaCount;
                log.debug("Commited " + taxaCount + " taxa. Time Delta "
                        + (System.currentTimeMillis() - start)
                        + "ms. Total Taxa Count " + taxaTotal);
                start = System.currentTimeMillis();
                taxaCount = 0;

                tx = sesh.beginTransaction();

            }

            nub_id = line[0];
//            parent_nub_id = line[1];
            lsid = line[2];
            accepted_id = line[3];
//            accepted_lsid = line[4];
//            name_id = line[5];
            canonical_name = line[6];
            author = line[7];
//            portal_rank_id = line[8];
            rank = line[9];
//            lft = line[10];
//            rgt = line[11];
//            kingdom_id = line[12];
//            kingdom = line[13];
//            phylum_id = line[14];
//            phylum = line[15];
//            class_id = line[16];
//            klass = line[17];
//            order_id = line[18];
//            order = line[19];
//            family_id = line[20];
            family = line[21];
//            genus_id = line[22];
//            genus = line[23];
//            species_id = line[24];
            species = line[25];
            source = line[26];

            if (accepted_id.isEmpty()) {

                taxon = getOrCreateTaxon(sesh, nub_id);
                taxon.setScientificName(canonical_name);
                taxon.setCommonName(canonical_name);
                taxon.setAuthor(author);
                taxon.setTaxonRank(taxonRankMap.get(rank));
                if (!species.isEmpty()) {
                    taxonGroup = getOrCreateTaxonGroup(sesh, family);
                } else {
                    taxonGroup = life;
                }
                taxon.setTaxonGroup(taxonGroup);

                if (commonNameRowMap.containsKey(lsid)) {
                    speciesProfileSet.clear();
                    speciesProfileSet.addAll(taxon.getInfoItems());

                    commonNameRowList = commonNameRowMap.get(lsid);
                    for (CommonNameRow commonNameRow : commonNameRowList) {

                        if (!commonNameRow.SCIENTIFIC_NAME_AUTHOR.isEmpty()) {
                            taxon.setAuthor(commonNameRow.SCIENTIFIC_NAME_AUTHOR);
                        }
                        if (!commonNameRow.SCIENTIFIC_NAME_YEAR.isEmpty()) {
                            taxon.setYear(commonNameRow.SCIENTIFIC_NAME_YEAR);
                        }

                        if (!commonNameRow.COMMON_NAME_STRING.isEmpty()) {
                            taxon.setCommonName(commonNameRow.COMMON_NAME_STRING);

                            if (!commonNameRow.COMMON_NAME_NAME_LSID.isEmpty()) {
                                commonNameProfile = getOrCreateCommonName(sesh, taxon, commonNameRow.COMMON_NAME_NAME_LSID);
                                commonNameProfile.setContent(commonNameRow.COMMON_NAME_STRING);
                                speciesProfileDAO.save(sesh, commonNameProfile);
                                addToSessionCache(commonNameRow.COMMON_NAME_NAME_LSID, taxon, commonNameProfile);

                                if (!speciesProfileSet.contains(commonNameProfile)) {
                                    speciesProfileSet.add(commonNameProfile);
                                    taxon.getInfoItems().add(commonNameProfile);
                                }
                            }
                        }

                        if (!commonNameRow.PUBLICATION_CITATION.isEmpty()
                                && !commonNameRow.PUBLICATION_LSID.isEmpty()) {
                            publicationProfile = getOrCreatePublicationCitation(sesh, taxon, commonNameRow.PUBLICATION_LSID);
                            publicationProfile.setContent(commonNameRow.PUBLICATION_CITATION);
                            speciesProfileDAO.save(sesh, publicationProfile);
                            addToSessionCache(commonNameRow.PUBLICATION_LSID, taxon, publicationProfile);

                            if (!speciesProfileSet.contains(publicationProfile)) {
                                speciesProfileSet.add(publicationProfile);
                                taxon.getInfoItems().add(publicationProfile);
                            }
                        }

                        if (!commonNameRow.SCIENTIFIC_NAME_STRING.isEmpty()
                                && !commonNameRow.SCIENTIFIC_NAME_NAME_LSID.isEmpty()) {
                            scientificNameProfile = getOrCreateScientificName(sesh, taxon, commonNameRow.SCIENTIFIC_NAME_NAME_LSID);
                            scientificNameProfile.setContent(commonNameRow.SCIENTIFIC_NAME_STRING);
                            speciesProfileDAO.save(sesh, scientificNameProfile);
                            addToSessionCache(commonNameRow.SCIENTIFIC_NAME_NAME_LSID, taxon, scientificNameProfile);

                            if (!speciesProfileSet.contains(scientificNameProfile)) {
                                speciesProfileSet.add(scientificNameProfile);
                                taxon.getInfoItems().add(scientificNameProfile);
                            }
                        }
                    }
                } else {
                    taxon.setCommonName(canonical_name);
                }

                setOrCreateTaxonSource(sesh, taxon, source);

                taxaDAO.save(sesh, taxon);
                addToSessionCache(lsid, taxon);
                taxaCount++;
            }
            rowCount++;
        }

        sesh.flush();
        clearSessionCache();
        sesh.clear();
        tx.commit();
        sesh.setFlushMode(FlushMode.AUTO);
    }

    private IndicatorSpecies getFromSessionCache(String lsid, Class<?> klass) {
        if (IndicatorSpecies.class.equals(klass)) {
            return taxonSessionCache.get(lsid);
        } else {
            log.error("Cannot get object with class " + klass.getSimpleName()
                    + " frmo the fake session cache");
            return null;
        }
    }

    private SpeciesProfile getFromSessionCache(String lsid,
            IndicatorSpecies taxon) {
        Map<String, SpeciesProfile> profileMapping = speciesProfileSessionCache.get(taxon);
        if (profileMapping == null) {
            return null;
        } else {
            return profileMapping.get(lsid);
        }
    }

    private void addToSessionCache(String lsid, IndicatorSpecies taxon) {
        taxonSessionCache.put(lsid, taxon);
    }

    private void addToSessionCache(String lsid, IndicatorSpecies taxon,
            SpeciesProfile profile) {
        Map<String, SpeciesProfile> profileMapping = speciesProfileSessionCache.get(taxon);
        if (profileMapping == null) {
            profileMapping = new HashMap<String, SpeciesProfile>();
            speciesProfileSessionCache.put(taxon, profileMapping);
        }
        profileMapping.put(lsid, profile);
    }

    private void clearSessionCache() {
        speciesProfileSessionCache.clear();
        taxonSessionCache.clear();
    }

    private TaxonGroup getOrCreateTaxonGroup(Session sesh, String groupName) {
        // Try memory cache first
        TaxonGroup taxonGroup = taxonGroupMap.get(groupName);
        if (taxonGroup == null) {
            // Try Database
            taxonGroup = taxaDAO.getTaxonGroup(sesh, groupName);
            if (taxonGroup == null) {
                taxonGroup = new TaxonGroup();
                taxonGroup.setName(groupName);
                taxonGroup = taxaDAO.save(sesh, taxonGroup);
            }
            taxonGroupMap.put(taxonGroup.getName(), taxonGroup);
        }
        return taxonGroup;
    }

    private SpeciesProfile getOrCreateScientificName(Session sesh,
            IndicatorSpecies taxon, String lsid) {

        SpeciesProfile scientificName = getFromSessionCache(lsid, taxon);
        if (scientificName == null) {
            scientificName = speciesProfileDAO.getSpeciesProfileBySourceDataId(sesh, taxon, Metadata.SCIENTIFIC_NAME_SOURCE_DATA_ID, lsid);
        }

        if (scientificName == null) {
            scientificName = new SpeciesProfile();
            scientificName.setType(SpeciesProfile.SPECIES_PROFILE_SCIENTIFICNAME);
            scientificName.setDescription(SCIENTIFIC_NAME_DESCRIPTION);
            scientificName.setHeader(SCIENTIFIC_NAME_HEADER);

            Metadata metadata = new Metadata();
            metadata.setKey(Metadata.SCIENTIFIC_NAME_SOURCE_DATA_ID);
            metadata.setValue(lsid);
            metadataDAO.save(sesh, metadata);

            scientificName.getMetadata().add(metadata);
        }

        return scientificName;
    }

    private SpeciesProfile getOrCreatePublicationCitation(Session sesh,
            IndicatorSpecies taxon, String lsid) {

        SpeciesProfile publicationCitation = getFromSessionCache(lsid, taxon);
        if (publicationCitation == null) {
            publicationCitation = speciesProfileDAO.getSpeciesProfileBySourceDataId(sesh, taxon, Metadata.PUBLICATION_SOURCE_DATA_ID, lsid);
        }

        if (publicationCitation == null) {
            publicationCitation = new SpeciesProfile();
            publicationCitation.setType(SpeciesProfile.SPECIES_PROFILE_PUBLICATION);
            publicationCitation.setDescription(PUBLICATION_DESCRIPTION);
            publicationCitation.setHeader(PUBLICATION_HEADER);

            Metadata metadata = new Metadata();
            metadata.setKey(Metadata.PUBLICATION_SOURCE_DATA_ID);
            metadata.setValue(lsid);
            metadataDAO.save(sesh, metadata);

            publicationCitation.getMetadata().add(metadata);
        }

        return publicationCitation;
    }

    private SpeciesProfile getOrCreateCommonName(Session sesh,
            IndicatorSpecies taxon, String lsid) {
        SpeciesProfile commonName = getFromSessionCache(lsid, taxon);
        if (commonName == null) {
            commonName = speciesProfileDAO.getSpeciesProfileBySourceDataId(sesh, taxon, Metadata.COMMON_NAME_SOURCE_DATA_ID, lsid);
        }

        if (commonName == null) {
            commonName = new SpeciesProfile();
            commonName.setType(SpeciesProfile.SPECIES_PROFILE_COMMONNAME);
            commonName.setDescription(COMMON_NAME_DESCRIPTION);
            commonName.setHeader(COMMON_NAME_HEADER);

            Metadata metadata = new Metadata();
            metadata.setKey(Metadata.COMMON_NAME_SOURCE_DATA_ID);
            metadata.setValue(lsid);
            metadataDAO.save(sesh, metadata);

            commonName.getMetadata().add(metadata);
        }

        return commonName;
    }

    private void setOrCreateTaxonSource(Session sesh, IndicatorSpecies taxon,
            String source) {

        Metadata md = taxon.getMetadataByKey(Metadata.TAXON_SOURCE);
        if (md == null) {
            md = new Metadata();
            md.setKey(Metadata.TAXON_SOURCE);
        }

        md.setValue(source);
        metadataDAO.save(sesh, md);
    }

    private IndicatorSpecies getOrCreateTaxon(Session sesh, String nubId) {
        IndicatorSpecies taxon = (IndicatorSpecies)getFromSessionCache(nubId, IndicatorSpecies.class);
        if (taxon == null) {
            taxon = taxaDAO.getIndicatorSpeciesBySourceDataID(sesh, nubId);
        }
        if (taxon == null) {
            taxon = new IndicatorSpecies();

            Metadata md = new Metadata();
            md.setKey(Metadata.TAXON_SOURCE_DATA_ID);
            md.setValue(nubId);
            metadataDAO.save(sesh, md);

            taxon.getMetadata().add(md);
        }
        return taxon;
    }

    private void loadCommonNames(Session sesh, File commonNames)
            throws IOException {

        List<CommonNameRow> rows;
        CommonNameRow commonNameRow;
        int rowCount = 1;
        CSVReader reader = new CSVReader(new FileReader(commonNames), ',', '"',
                rowCount);
        for (String[] line = reader.readNext(); line != null; line = reader.readNext()) {
            if ((rowCount % 1000) == 0) {
                log.debug("Reading Common Name Row: " + rowCount);
            }

            if (line.length == 10) {

                commonNameRow = new CommonNameRow();
                commonNameRow.COMMON_NAME_CONCEPT_LSID = line[0];
                commonNameRow.COMMON_NAME_NAME_LSID = line[1];
                commonNameRow.COMMON_NAME_STRING = line[2];
                commonNameRow.PUBLICATION_LSID = line[3];
                commonNameRow.PUBLICATION_CITATION = line[4];
                commonNameRow.SCIENTIFIC_NAME_CONCEPT_LSID = line[5];
                commonNameRow.SCIENTIFIC_NAME_NAME_LSID = line[6];
                commonNameRow.SCIENTIFIC_NAME_STRING = line[7];
                commonNameRow.SCIENTIFIC_NAME_AUTHOR = line[8];
                commonNameRow.SCIENTIFIC_NAME_YEAR = line[9];

                rows = commonNameRowMap.get(commonNameRow.SCIENTIFIC_NAME_CONCEPT_LSID);
                if (rows == null) {
                    rows = new ArrayList<CommonNameRow>();
                    commonNameRowMap.put(commonNameRow.SCIENTIFIC_NAME_CONCEPT_LSID, rows);
                }
                rows.add(commonNameRow);
            } else {
                log.warn("Ignoring row " + rowCount
                        + " because the line length != 10. Line length is "
                        + line.length + " The row data was");
                StringBuilder builder = new StringBuilder();
                for (String cell : line) {
                    builder.append(cell);
                    builder.append(", ");
                }
                log.warn(builder.toString());
            }

            rowCount++;
        }
    }

    private class CommonNameRow {
        @SuppressWarnings("unused")
        String COMMON_NAME_CONCEPT_LSID;
        String COMMON_NAME_NAME_LSID;
        String COMMON_NAME_STRING;
        String PUBLICATION_LSID;
        String PUBLICATION_CITATION;
        String SCIENTIFIC_NAME_CONCEPT_LSID;
        String SCIENTIFIC_NAME_NAME_LSID;
        String SCIENTIFIC_NAME_STRING;
        String SCIENTIFIC_NAME_AUTHOR;
        String SCIENTIFIC_NAME_YEAR;
    }

}
