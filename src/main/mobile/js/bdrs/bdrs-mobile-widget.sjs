
exports.ANALOGOUS_COLOURS = ['7098BF', '70B2BF','707DBF','B6CADE','979797','FFFFFF'];

/**
 * Generates a horizontal field set of radio buttons containing the most
 * often used taxa.
 * @param limit the number of taxa to provide in the field set.
 */
exports.RecentTaxaWidget = function(limit) {

    this.getRecentTaxonomy = function() {
    	var countList;
    	waitfor(countList) {
			SpeciesCount.all().order('count', false).limit(limit).prefetch('species').list(resume);    
        }
        var taxonList = [];
        for (var i = 0; i < countList.length; i++) {
            taxonList.push(countList[i].species());
        }
        return taxonList;
    };
    
    this.shortenScientificName = function(name) {
        var split = name.split(' ');
        if(split.length === 1) {
            return name;
        } else {
            return [split[0][0] + '.', split[1]].join(' ');
        }
    };
    
    this.createFieldset = function(target) { 

        var recentTaxonomy = this.getRecentTaxonomy();
        // If we have no recent taxonomy, just pick some number from the database.
        if(recentTaxonomy.length === 0) {
            waitfor(recentTaxonomy) {
                Species.all().limit(limit).list(resume);
            }
        }

        // Create the (inner) template parameters.
        var radioTmplParams = [];        
        var taxon;
        for(var i=0; i<recentTaxonomy.length; i++) {
            taxon = recentTaxonomy[i];
            radioTmplParams.push({
                id: taxon.id,
                value: taxon.scientificName(),
                displayName: this.shortenScientificName(taxon.scientificName())
            });
        }
        
        // Generate the radio elements (appending them to a temp parent in order
        // to generate the raw html later)
        var radioElements;         
        waitfor(radioElements) { 
            bdrs.template.renderOnlyCallback("recentTaxaWidget-radio", radioTmplParams, resume); 
        }
        var tmpParent = jQuery("<fieldset></fieldset>");
        radioElements.appendTo(tmpParent);

        // Render the outer template.
        var fieldsetElem;
        waitfor(fieldsetElem) {
            bdrs.template.renderOnlyCallback("recentTaxaWidget", {radios: tmpParent.html()}, resume);
        }
        // Check the last node otherwise the change handler doesn't get called
        fieldsetElem.find("[type=radio]:last").attr("checked","checked");

        // Attach the change handler to each radio. On change, update the
        // autocomplete input. Blur the input to trigger generation of necessary
        // taxon group attributes.        
        fieldsetElem.find("[type=radio]").change(function() {
            jQuery(target).find("[type=text]").val(fieldsetElem.find(":checked").val()).blur();
        });
        
        // Set this style for correct rendering.
        fieldsetElem.find("fieldset").css({display:'inline-block'});
        
        return fieldsetElem;
    };

    this.after = function(target) {
        var fieldset = this.createFieldset(target);
        jQuery(target).after(fieldset);
        bdrs.mobile.restyle(fieldset.parent());
    };
    
    this.before = function(target) {
        var fieldset = this.createFieldset(target);
        jQuery(target).before(fieldset);
        bdrs.mobile.restyle(fieldset.parent());
    }

    return this;
};

exports.RecordStatistics = function() {
    
    this.sum = function(values) {
        var sum = 0;
        for(var i=0; i<values.length; i++) {
            sum = sum + values[i];
        }
        return sum;
    }

    this.generateRawRecordStats = function(recordList) {
        // {
        //     taxon : { taxon.id : { instance : Species, count : int }},
        //     taxonGroup : { taxonGroup.id : { instance : TaxonGroup, count : int }},
        //     record: { taxonomic : [Record, ...], nonTaxonomic : [Record, ... ] },
        //     taxonAccumulation: {points: [[days since start, species count], [x,y], [x,y] ...], labels: [{header: String, value: int}]}
        // }
        var stats = {};
        stats.taxon = {};
        stats.taxonGroup = {};
        stats.record = { taxonomic: [], nonTaxonomic: [] };
        stats.taxonAccumulation = {points:[], labels:[]};
        
        var record;
        var taxon;
        var taxonGroup;
        var taxonCountMap;
        var taxonGroupCountMap;
        
        var earliestRecord = null;
        var uniqueTaxonCount = 0;
        var lastAccumulationPoint = null;
        var lastAccumulationLabel = null;
        
        for(var i=0; i<recordList.length; i++) {
            
            record = recordList[i];
            taxon = record.species();
            
            if(earliestRecord === null) {
                earliestRecord = record;
            }
            
            if (taxon === null) {
            	stats.record.nonTaxonomic.push(record);
            } else {
                stats.record.taxonomic.push(record);
                
	            // Taxon Statistics
	            taxonCountMap = stats.taxon[taxon.id]; 
	            if(taxonCountMap === undefined) {
	                taxonCountMap = { instance : taxon, count : 0 };
	                stats.taxon[taxon.id] = taxonCountMap;
	                uniqueTaxonCount++;
	            }
	            taxonCountMap.count++;
	            
	            // Taxon Group Statistics
	            waitfor(taxonGroup) {
	                taxon.fetch('taxonGroup', resume);
	            }
	            taxonGroupCountMap = stats.taxonGroup[taxonGroup.id];
	            if(taxonGroupCountMap === undefined) {
	                taxonGroupCountMap = { instance : taxonGroup, count : 0 };
	                stats.taxonGroup[taxonGroup.id] = taxonGroupCountMap;
	            }
	            taxonGroupCountMap.count++;
            }
            
            if(record.number() > 0) {
	            var dayOffset = bdrs.mobile.getDaysBetween(earliestRecord.when(), record.when());
	            if( (lastAccumulationPoint === null) || 
	                (lastAccumulationPoint !== null && lastAccumulationPoint[0] !== dayOffset)) {
	
	                lastAccumulationPoint = [dayOffset, uniqueTaxonCount];
	                stats.taxonAccumulation.points.push(lastAccumulationPoint);
	                
	                lastAccumulationLabel = {header: bdrs.mobile.formatDate(record.when()), value: uniqueTaxonCount};
	                stats.taxonAccumulation.labels.push(lastAccumulationLabel);               
	                 
	            } else {
	                lastAccumulationPoint[1] = uniqueTaxonCount;
	                lastAccumulationLabel.value = uniqueTaxonCount;
	            }
            } else {
                // Record with no sightings.
            }
        }
        return stats;
    };
    
    this.createEmptySpan = function(spanId) {
        var spanTargetParams = {id:spanId, content: ''};
        var spanTargetElem;
        waitfor(spanTargetElem) {
            bdrs.template.renderOnlyCallback('statisticsSpanTarget', spanTargetParams, resume);
        }
        return bdrs.mobile.toHtmlString(spanTargetElem);
        
    };
    
    this.createTableRow = function(header, content, colorCellColor) {
        var tmplParams = {headerContent: header, dataContent: content};
        var rowElem;
        waitfor(rowElem) {
            bdrs.template.renderOnlyCallback('horizontalDatatableRow', tmplParams, resume);
        }
        
        if(colorCellColor !== undefined) {
            var colorCell;
            waitfor(colorCell) {
                bdrs.template.renderOnlyCallback('statisticsPieChartColorCell', {color: colorCellColor}, resume);
            }
            colorCell.appendTo(rowElem);
        }
        return bdrs.mobile.toHtmlString(rowElem);
    };
    
    this.renderLineChart = function(target, title, points, tableData) {
        var leftSpanId = bdrs.mobile.guidGenerator();
        var rightSpanId = bdrs.mobile.guidGenerator();
        
        var tmplParams = {};
        tmplParams.leftContent = this.createEmptySpan(leftSpanId);
        tmplParams.rightContent = this.createEmptySpan(rightSpanId);
        
        waitfor() {
            bdrs.template.renderCallback('statisticsTwoColLayout', tmplParams, target, resume);
        }
    
        var label;
        var rows = [];
        for(var i=0; i<tableData.length; i++) {
            label = tableData[i];            
            rows.push(this.createTableRow(label.header, label.value));
        }
        
        jQuery("#"+leftSpanId).sparkline(points, {
            type: 'line',
            width: '100%',
            height: '20%'
        });
        
        var rightSpan = jQuery("#"+rightSpanId);
        var rightContent;
        waitfor(rightContent) {
            bdrs.template.renderOnlyCallback('horizontalDatatable', {title:title, id:'', rows:rows.join('')}, resume);
        }
        rightContent.appendTo(rightSpan);
        
        rightSpan.parents("fieldset").css("padding-top", "1em");
    }
    
    this.renderPieChart = function(target, title, pieData, tableData) {
        var leftSpanId = bdrs.mobile.guidGenerator();
        var rightSpanId = bdrs.mobile.guidGenerator();
        
        var tmplParams = {};
        tmplParams.leftContent = this.createEmptySpan(leftSpanId);
        tmplParams.rightContent = this.createEmptySpan(rightSpanId);
        
        waitfor() {
            bdrs.template.renderCallback('statisticsTwoColLayout', tmplParams, target, resume);
        }
        
        var slices;
        if(jQuery.isArray(pieData)) {
            slices = pieData;
        } else {
            slices = [];
            for(var id in pieData) {
                slices.push(pieData[id].count);
            }
        }
        
        if(slices.length === 1) {
            slices.push(0);
        }
        
        var label;
        var rows = [];
        var sliceColor;
        for(var i=0; i<tableData.length; i++) {
            label = tableData[i];
            sliceColor = i < bdrs.mobile.widget.ANALOGOUS_COLOURS.length ? bdrs.mobile.widget.ANALOGOUS_COLOURS[i] : 'FFF';
            rows.push(this.createTableRow(label.header, label.value, sliceColor));
        }
       
        // If the number of slices to be shown is larger than the number of 
        // colours that we have provided for each slice, then sum up the
        // extra slices and group them into a single slice called "other". 
        if(slices.length > bdrs.mobile.widget.ANALOGOUS_COLOURS.length) {
            var displayedSlices = slices.slice(0, bdrs.mobile.widget.ANALOGOUS_COLOURS.length - 1);
            var remainingSlices = slices.slice(displayedSlices.length, slices.length);
            var sumRemainingSlices = this.sum(remainingSlices);
            displayedSlices.push(sumRemainingSlices);
            slices = displayedSlices;
            
            rows = rows.slice(0, bdrs.mobile.widget.ANALOGOUS_COLOURS.length - 1);
            var otherSliceColor = bdrs.mobile.widget.ANALOGOUS_COLOURS[bdrs.mobile.widget.ANALOGOUS_COLOURS.length-1];
            rows.push(this.createTableRow("Other", sumRemainingSlices, otherSliceColor));
        }
        
        jQuery("#"+leftSpanId).sparkline(slices, {
            type: 'pie',
            width: '100%',
            height: '20%',
            sliceColors: bdrs.mobile.widget.ANALOGOUS_COLOURS,
            offset: 90
        });
        
        var rightSpan = jQuery("#"+rightSpanId);
        var rightContent;
        waitfor(rightContent) {
            bdrs.template.renderOnlyCallback('horizontalDatatable', {title:title, id:'', rows:rows.join('')}, resume);
        }
        rightContent.appendTo(rightSpan);
        
        rightSpan.parents("fieldset").css("padding-top", "1em");
    };

    this.appendTo = function(target) {
    
        var setting;
        waitfor(setting) {
            Settings.findBy('key', 'current-survey-id', resume);
        }
        var survey;
        waitfor(survey) {
            Survey.findBy('server_id', setting.value(), resume);
        }
        var recordList;
        waitfor(recordList) {
            survey.records().prefetch('species').order('when', true).list(resume);
        }
    
        var stats = this.generateRawRecordStats(recordList);
        
        // Recorded Species
        var countMap;
        var tableData = [];
        var substats = stats.taxon;
        for(var id in substats) {
            countMap = substats[id];
            tableData.push({header:countMap.instance.scientificName(), value:countMap.count});
        }
        if(tableData.length > 0) {
            this.renderPieChart(target, 'Recorded Species', substats, tableData);
        }
        
        // Recorded Groups
        tableData = [];
        substats = stats.taxonGroup;
        for(var id in substats) {
            countMap = substats[id];
            tableData.push({header:countMap.instance.name(), value:countMap.count});
        }
        if(tableData.length > 0) {	        
	        this.renderPieChart(target, 'Recorded Groups', substats, tableData);
        }
    
        // Record Types
        substats = stats.record;
        if((substats.taxonomic.length > 0) || (substats.nonTaxonomic.length > 0)) {
	        var recordTypeSlices = [substats.taxonomic.length, substats.nonTaxonomic.length];
	        tableData = [{ header:'Taxonomic Records', value:recordTypeSlices[0]}, 
	                     { header:'Non Taxonomic Records', value:recordTypeSlices[1]}];
	        this.renderPieChart(target, 'Record Types', recordTypeSlices, tableData);
        }
        
        // Species Accumulation Curve.
        // Only makes sense if there are at least 2 points.
        if(stats.taxonAccumulation.points.length > 1) { 
            this.renderLineChart(target, 'Species Accumulation', stats.taxonAccumulation.points, stats.taxonAccumulation.labels);    
        }
    };

    return this;
};