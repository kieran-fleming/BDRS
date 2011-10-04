package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.gbif.dwc.record.DarwinCoreRecord;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.StarRecord;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.dwca.AbstractDwcaTest;
import au.com.gaiaresources.bdrs.dwca.RecordDwcaWriter;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;
import au.com.gaiaresources.bdrs.util.FileUtils;
import au.com.gaiaresources.bdrs.util.ZipUtils;

public class DarwinCoreArchiveServiceTest extends AbstractDwcaTest {

    @Test
    public void testDownloadArchive() throws Exception {
        request.setScheme(REQUEST_SCHEME);
        request.setServerName(REQUEST_SERVER_NAME);
        request.setContextPath(REQUEST_CONTEXT_PATH);
        request.setServerPort(REQUEST_SERVER_PORT);
        request.setRequestURI(DarwinCoreArchiveService.DOWNLOAD_ARCHIVE_URL);
        request.setMethod("GET");
        
        handle(request, response);
        
        File zipFile = File.createTempFile("archivefile", "zip");        
        FileUtils.writeBytesToFile(response.getContentAsByteArray(), zipFile);
        
        File targetDir = FileUtils.createTempDirectory("decompress_rec_dwca");
        ZipUtils.decompressToDir(zipFile, targetDir);
        
        // check for file existance
        File metaFile = new File(targetDir.getAbsolutePath() + "/" + RecordDwcaWriter.META_FILE);
        File occurrenceFile = new File(targetDir.getAbsolutePath() + "/" + RecordDwcaWriter.OCCURRENCE_FILE);
        File mofFile = new File(targetDir.getAbsolutePath() + "/" + RecordDwcaWriter.MEASUREMENT_OR_FACT_FILE);
        
        Assert.assertTrue("meta file should exist", metaFile.exists());
        Assert.assertTrue("occurrence file should exist", occurrenceFile.exists());
        Assert.assertTrue("measurement/fact file should exist", mofFile.exists());

        Archive archive = ArchiveFactory.openArchive(targetDir);

        Assert.assertTrue("should have sci name field", archive.getCore().hasTerm(DwcTerm.scientificName));
        
        Iterator<DarwinCoreRecord> dwcIter = archive.iteratorDwc();
        
        int count = 0;
        while (dwcIter.hasNext()) {
            DarwinCoreRecord dwcr = dwcIter.next();
            StarRecord starRecord = getStarRecord(archive, dwcr.getCatalogNumber());
            assertDwcRecord(dwcr, starRecord, allRecordList);
            ++count;
        }
        
        Assert.assertEquals("incorrect record count", countArchivedRecords(allRecordList), count);
    }
}
