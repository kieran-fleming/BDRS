package au.com.gaiaresources.bdrs.model.grid.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import au.com.gaiaresources.bdrs.db.TransactionCallback;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.grid.Grid;
import au.com.gaiaresources.bdrs.model.grid.GridDAO;
import au.com.gaiaresources.bdrs.model.grid.GridEntry;
import au.com.gaiaresources.bdrs.model.grid.GridService;
import au.com.gaiaresources.bdrs.model.record.NewRecordEvent;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GridServiceImpl implements GridService, ApplicationListener {
    @Autowired
    private GridDAO gridDAO;
    @Autowired 
    private TaskExecutor executor;
    @Autowired
    private GeometryBuilder geometryBuilder;
    //@Autowired
    //private GeometryTransformer geometryTransformer;
//    @Autowired
//    private FileService fileService;
    
    private BlockingQueue<Record> queue;
    
    private List<BigDecimal> precisions;
    private Integer minX;
    @SuppressWarnings("unused")
    private Integer maxX;
    private Integer minY;
    @SuppressWarnings("unused")
	private Integer maxY;
    
//    private Point transformedBottomLeft;
//    private Point transformedTopRight;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    
    private Logger logger = Logger.getLogger(getClass());
    
    @PostConstruct
    public void init() {
        queue = new LinkedBlockingQueue<Record>();
        
        List<? extends Grid> grids = gridDAO.getGrids();
        for (BigDecimal p : precisions) {
            boolean found = false;
            for (Grid g : grids) {
                if (g.getPrecision().compareTo(p) == 0) {
                    found = true;
                }
            }
            if (!found) {
                gridDAO.createGrid(p);
            }
        }
        
//        Point bottomLeft = geometryBuilder.createPoint(minX, minY);
//        Point topRight = geometryBuilder.createPoint(maxX, maxY);
        
        //transformedBottomLeft = (Point) geometryTransformer.transform(bottomLeft, 900913);
        //transformedTopRight = (Point) geometryTransformer.transform(topRight, 900913);
        
        executor.execute(new GridUpdaterRunnable());
    }
    
    public void setPrecisions(List<BigDecimal> precisions) {
        this.precisions = precisions;
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof NewRecordEvent) {
            this.handleRecord(((NewRecordEvent) event).getRecord());
        }
    }
    
    @Override
    public void handleRecord(Record record) {
        // LinkedBlockingQueue is thread safe.
        this.queue.add(record);
    }
    
    public List<? extends Grid> getGrids() {
        return gridDAO.getGrids();
    }
    
    public List<? extends GridEntry> getGridEntries(Grid grid, IndicatorSpecies s) {
        return gridDAO.getGridEntries(grid, s);
    }
    
    private void generateGridKML(Grid grid, IndicatorSpecies s) throws Exception {
        new GridImageGenerator().generate(grid, s);
        new GridImageGenerator().generateKML(grid, s);
    }
    
    private class GridUpdaterRunnable implements Runnable {
        private Set<Grid> updatedGrids = new HashSet<Grid>();
        
        public void run() {
            try {
                while (true) {
                    logger.info("Awaiting new record to process.");
                    final Record r = queue.take();
                    logger.info("New record received, starting processing.");
                    
                    if (r.getSpecies() == null || r.getNumber() == null) {
                        // cancel processing, null members not handled properly.
                        continue;
                    }
                    
                    for (final Grid g : gridDAO.getGrids()) {
                        doInTransaction(new TransactionCallback<Boolean>() {
                            @Override
                            public Boolean doInTransaction(TransactionStatus status) {
                                GridEntry entry = gridDAO.getGridEntry(g, r.getLocation().getLocation(), r.getSpecies());
                                if (entry == null) {
                                    Point location = r.getLocation().getLocation();
                                    
                                    BigDecimal gridX = new BigDecimal(GridServiceImpl.this.minX);
                                    while (location.getX() > gridX.add(g.getPrecision()).doubleValue()) {
                                        gridX = gridX.add(g.getPrecision());
                                    }
                                    
                                    BigDecimal gridY = new BigDecimal(GridServiceImpl.this.minY);
                                    while (location.getY() > gridY.add(g.getPrecision()).doubleValue()) {
                                        gridY = gridY.add(g.getPrecision());
                                    }
                                    
                                    logger.info("Creating new grid entry with lower left corner: " + gridX + " " + gridY);
                                    
                                    Polygon p = geometryBuilder.createSquare(gridX.doubleValue(), gridY.doubleValue(), 
                                            g.getPrecision().doubleValue());
                                    entry = gridDAO.createGridEntry(g, p, r.getSpecies());
                                    
                                }
                                gridDAO.updateGridEntry(entry, entry.getNumberOfRecords() + 1);
                                
                                return true;
                            }
                        });
                        
                        
                        updatedGrids.add(g);
                    }
                    
                    if (queue.peek() == null) {
                        try {
                            for (Grid g : updatedGrids) {
                                generateGridKML(g, r.getSpecies());
                            }
                            updatedGrids.clear();
                        } catch (Throwable t) {
                            logger.error("Grid KML generation failed.", t);
                        }
                    }
                }
            } catch (InterruptedException ie) {
                logger.error("Interrupted", ie);
            }
            logger.info("Exiting thread.");
        }
    }

    public void setMinX(Integer minX) {
        this.minX = minX;
    }

    public void setMaxX(Integer maxX) {
        this.maxX = maxX;
    }

    public void setMinY(Integer minY) {
        this.minY = minY;
    }

    public void setMaxY(Integer maxY) {
        this.maxY = maxY;
    }
    
    protected synchronized TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null) {
            transactionTemplate = new TransactionTemplate(transactionManager);
        }
        return transactionTemplate;
    }
    
    @SuppressWarnings("unchecked")
    protected <C> C doInTransaction(TransactionCallback<C> callback) {
        return (C) getTransactionTemplate().execute(callback);
    }
    
    
}
