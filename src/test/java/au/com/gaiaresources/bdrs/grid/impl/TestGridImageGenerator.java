package au.com.gaiaresources.bdrs.grid.impl;

import java.io.File;
import java.io.OutputStream;

import org.junit.Ignore;
import org.junit.Test;

import au.com.gaiaresources.bdrs.model.grid.impl.GridImageGenerator;

public class TestGridImageGenerator {
    public TestGridImageGenerator() {
        super();
    }
    
    @Test
    public void testSimpleImage() throws Exception {
    	/*
        ApplicationContext context = createMock(ApplicationContext.class);
        setStaticField(AppContext.class, "context", context);
        
        FileService fileService = createMock(FileService.class);
        expect(context.getBeansOfType(FileService.class), CollectionUtils.createMap(new String[] {"fileService"}, 
                                                                                    new Object[] {fileService}));
        
        GridService gridService = createMock(GridService.class);
        expect(context.getBeansOfType(GridService.class), CollectionUtils.createMap(new String[] {"gridService"},
                                                                                    new Object[] {gridService}));
        
        GeometryTransformer geomTransformer = new GeometryTransformer();
        geomTransformer.init();
        expect(context.getBeansOfType(GeometryTransformer.class), 
                                      CollectionUtils.createMap(new String[] {"geomTransformer"},
                                                                new Object[] {geomTransformer}));
        
        Grid grid = createMock(Grid.class);
        expect(grid.getId(), 10);
        
        File targetFile = createMock(File.class);
        expect(targetFile.getAbsolutePath(), "/test/grid/file/grid-sm.png");
        expect(targetFile.getPath(), "/test/grid/file/grid-sm.png");
        expect(fileService.createTargetFile(Grid.class, 10, "grid-sm.png"), targetFile);
        expect(targetFile.delete(), true);
        
        GeometryBuilder geomBuilder = new GeometryBuilder();
        GridEntry entry1 = createMock(GridEntry.class);
        expect(entry1.getNumberOfRecords(), 5);
        expect(entry1.getBoundary(), geomBuilder.createSquare(146, -32, 2));
        expect(entry1.getId(), 1);
        GridEntry entry2 = createMock(GridEntry.class);
        expect(entry2.getNumberOfRecords(), 10);
        expect(entry2.getBoundary(), geomBuilder.createSquare(148, -30, 2));
        expect(entry2.getId(), 2);
        GridEntry entry3 = createMock(GridEntry.class);
        expect(entry3.getNumberOfRecords(), 15);
        expect(entry3.getBoundary(), geomBuilder.createSquare(150, -28, 2));
        expect(entry3.getId(), 3);
        
        expect(gridService.getGridEntries(grid, null), Arrays.asList(new GridEntry[] {entry1, entry2, entry3}));
        
        replayMocks();
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TestGenerator generator = new TestGenerator();
        generator.output = output;
        generator.generate(grid, null);
        
        File temp = File.createTempFile("TestOutput", ".png");
        getLogger().info(temp.getAbsolutePath());
        FileOutputStream fo = new FileOutputStream(temp);
        try {
            IOUtils.write(output.toByteArray(), fo);
        } finally {
            fo.close();
        }*/
    }
    
    @Ignore
    public static class TestGenerator extends GridImageGenerator {
        private OutputStream output;

        public TestGenerator() {
            super();
        }
        
        @Ignore
        protected OutputStream createOutputStream(File targetFile) {
            return output;
        }
    }
}
