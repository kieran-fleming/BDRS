package au.com.gaiaresources.bdrs.controller.survey;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class SurveyBaseController extends AbstractController {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;

    @Autowired
    private MetadataDAO metadataDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private TaxaDAO taxaDAO;
    
    @Autowired
    private FileService fileService;
    
    public static final Dimension TARGET_LOGO_DIMENSION = new Dimension(250,187);
    public static final String TARGET_LOGO_IMAGE_FORMAT = "PNG";

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/listing.htm", method = RequestMethod.GET)
    public ModelAndView listSurveys(HttpServletRequest request, HttpServletResponse response) {

        ModelAndView mv = new ModelAndView("surveyListing");
        mv.addObject("surveyList", surveyDAO.getSurveys(getRequestContext().getUser()));
        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/edit.htm", method = RequestMethod.GET)
    public ModelAndView editSurvey(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "surveyId", required = false) Integer surveyId,
            @RequestParam(value = "publish", required = false) String publish) {

        Survey survey;
        if (surveyId == null) {
            survey = new Survey();
        } else {
            survey = surveyDAO.getSurvey(surveyId);
        }

        boolean toPublish = publish != null;
        if (toPublish) {
            getRequestContext().addMessage("bdrs.survey.publish");
        }
        
        ModelAndView mv = new ModelAndView("surveyEdit");
        mv.addObject("survey", survey);
        mv.addObject("publish", toPublish);
        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/edit.htm", method = RequestMethod.POST)
    public ModelAndView submitSurveyEdit(
            MultipartHttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "surveyId", required = false) Integer surveyId,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "description", required = true) String description,
            @RequestParam(value = "active", defaultValue = "false") boolean active,
            @RequestParam(value = "rendererType", defaultValue="DEFAULT") String rendererType,
            @RequestParam(value = "surveyDate", required = true) Date surveyDate,
            @RequestParam(value = "surveyEndDate", required = false) String surveyEndDate) 
        throws IOException {

        Survey survey;
        boolean create;
        if (surveyId == null) {
            survey = new Survey();
            create = true;
        } else {
            survey = surveyDAO.getSurvey(surveyId);
            create = false;
        }

        boolean origPublishStatus = survey.isActive();

        survey.setName(name);
        survey.setDescription(description);
        survey.setStartDate(surveyDate);
        survey.setEndDate(surveyEndDate);
        survey.setActive(active);
        
        // A list of metadata to delete. To maintain referential integrity,
        // the link between survey and metadata must be broken before the 
        // metadata can be deleted.
        List<Metadata> metadataToDelete = new ArrayList<Metadata>();

        Metadata md;
        SurveyFormRendererType formRenderType = SurveyFormRendererType.valueOf(rendererType);
        if(formRenderType.isEligible(survey)) {
            md = survey.setFormRendererType(formRenderType);
        } else {
            md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        }
        metadataDAO.save(md);
        
        // Survey Logo
        String logoFileStr = request.getParameter(Metadata.SURVEY_LOGO);
        MultipartFile logoFile = request.getFile(Metadata.SURVEY_LOGO+"_file");
        // logoFile will always have size zero unless the file
        // is changed. If there is already a file, but the
        // record is updated, without changing the file input,
        // logoFileStr will not be empty but logoFile will
        // have size zero.
        Metadata logo = survey.getMetadataByKey(Metadata.SURVEY_LOGO);
        if(logoFileStr.isEmpty() && logo != null) {
            // The file was intentionally cleared.
            //survey.getMetadata().remove(logo);
            Set<Metadata> surveyMetadata = new HashSet(survey.getMetadata());
            surveyMetadata.remove(logo);
            survey.setMetadata(surveyMetadata);
            
            metadataToDelete.add(logo);
        }
        else if(!logoFileStr.isEmpty() && logoFile != null && logoFile.getSize() > 0) {
            if(ImageIO.getImageReadersByMIMEType(logoFile.getContentType()).hasNext()) {
                if(logo == null) {
                    logo = new Metadata();
                    logo.setKey(Metadata.SURVEY_LOGO);
                }
                logo.setValue(logoFile.getOriginalFilename());
                metadataDAO.save(logo);
                survey.getMetadata().add(logo);
                
                // Resize the image as required to fit the space
                BufferedImage sourceImage = ImageIO.read(logoFile.getInputStream());

                BufferedImage scaledImage = new BufferedImage(TARGET_LOGO_DIMENSION.width, TARGET_LOGO_DIMENSION.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2_scaled = scaledImage.createGraphics();
                // Better scaling
                g2_scaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                //g2_scaled.setBackground(new Color(0,0,0,255));
                g2_scaled.setBackground(Color.WHITE);
                g2_scaled.clearRect(0,0,TARGET_LOGO_DIMENSION.width,TARGET_LOGO_DIMENSION.height);

                int width = sourceImage.getWidth();
                int height = sourceImage.getHeight();

                double widthRatio = TARGET_LOGO_DIMENSION.getWidth()
                        / (double) width;
                double heightRatio = TARGET_LOGO_DIMENSION.getHeight()
                        / (double) height;
                if (heightRatio > widthRatio) {
                    int scaledHeight = (int) Math.round(widthRatio * height);
                    g2_scaled.drawImage(sourceImage, 0, (scaledImage.getHeight() - scaledHeight) / 2, scaledImage.getWidth(), scaledHeight, g2_scaled.getBackground(), null);
                } else {
                    int scaledWidth = (int) Math.round(heightRatio * width);
                    g2_scaled.drawImage(sourceImage, (scaledImage.getWidth() - scaledWidth) / 2, 0, scaledWidth, scaledImage.getHeight(), new Color(
                            0, 0, 0, 255), null);
                }

                File targetFile = fileService.createTargetFile(logo.getClass(), logo.getId(), logoFile.getOriginalFilename());
                //ImageIO.write(scaledImage, TARGET_LOGO_IMAGE_FORMAT, targetFile);
                FileImageOutputStream out = new FileImageOutputStream(targetFile);
                ImageWriter writer = ImageIO.getImageWritersByMIMEType(logoFile.getContentType()).next();
                writer.setOutput(out);
                writer.write(scaledImage);
                out.flush();
                out.close();
            }
            else {
                log.warn("Unable to resize logo image with content type "+logoFile.getContentType());
            }
        } 
        
        surveyDAO.save(survey);

        // Work around for the Survey DAO. The DAO automatically sets the
        // survey to active when creating the survey. This forces the survey
        // to update with the correct active state.
        if (create) {
            survey.setActive(active);
            surveyDAO.save(survey);
        }
        
        for(Metadata delMd : metadataToDelete) {
            metadataDAO.delete(delMd);
        }

        String messageKey;
        if (!origPublishStatus && survey.isActive()) {
            messageKey = "bdrs.survey.publish.success";
        } else {
            messageKey = "bdrs.survey.save.success";
        }
        getRequestContext().addMessage(messageKey,
                new Object[] { survey.getName() });

        ModelAndView mv;
        if (request.getParameter("saveAndContinue") != null) {
            mv = new ModelAndView(new RedirectView(
                    "/bdrs/admin/survey/editTaxonomy.htm", true));
            mv.addObject("surveyId", survey.getId());
        } else {
            mv = new ModelAndView(new RedirectView(
                    "/bdrs/admin/survey/listing.htm", true));
        }
        return mv;
    }

    // -------------------------------
    //  Users
    // -------------------------------

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editUsers.htm", method = RequestMethod.GET)
    public ModelAndView editSurveyUsers(HttpServletRequest request, HttpServletResponse response) {
        Survey survey = getSurvey(request.getParameter("surveyId"));
        ModelAndView mv = new ModelAndView("surveyEditUsers");
        mv.addObject("listType", survey.isPublic() ? UserSelectionType.ALL_USERS : UserSelectionType.SELECTED_USERS);
        mv.addObject("survey", survey);
        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editUsers.htm", method = RequestMethod.POST)
    public ModelAndView submitSurveyUsers(HttpServletRequest request, HttpServletResponse response) {
        Survey survey = getSurvey(request.getParameter("surveyId"));

        //List<User> userList = request.getParameter("users");
        UserSelectionType selectionType =
            UserSelectionType.valueOf(request.getParameter("userSelectionType"));

        if(UserSelectionType.SELECTED_USERS.equals(selectionType)) {
            survey.setPublic(false);
            // Add the users
            Set<User> users = new HashSet<User>();
            if(request.getParameterValues("users") != null) {
                List<Integer> pks = new ArrayList<Integer>();
                for(String rawPk : request.getParameterValues("users")) {
                    pks.add(Integer.parseInt(rawPk));
                }
                users.addAll(userDAO.get(pks.toArray(new Integer[]{})));
            }
            survey.setUsers(users);

            // Add the groups
            Set<Group> groups = new HashSet<Group>();
            if(request.getParameterValues("groups") != null) {
                List<Integer> pks = new ArrayList<Integer>();
                for(String rawPk : request.getParameterValues("groups")) {
                    pks.add(Integer.parseInt(rawPk));
                }
                groups.addAll(groupDAO.get(pks.toArray(new Integer[]{})));
            }
            survey.setGroups(groups);

        } else if(UserSelectionType.ALL_USERS.equals(selectionType)) {
            survey.setPublic(true);
        } else {
            log.error("Unknown User Selection Type: "+selectionType.toString());
            throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Update the form rendering type given the new criteria
        SurveyFormRendererType formRenderType = survey.getFormRendererType();
        if(formRenderType == null || (formRenderType != null && !formRenderType.isEligible(survey))) {
            Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
            metadataDAO.save(md);
        }
        surveyDAO.save(survey);

        getRequestContext().addMessage("bdrs.survey.users.success", new Object[]{survey.getName()});

        ModelAndView mv;
        if(request.getParameter("saveAndContinue") != null) {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/edit.htm", true));
            mv.addObject("surveyId", survey.getId());
            mv.addObject("publish", "publish");
        }
        else {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/listing.htm", true));
        }
        return mv;
    }

    // --------------------------------------
    //  Taxonomy
    // --------------------------------------

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editTaxonomy.htm", method = RequestMethod.GET)
    public ModelAndView editSurveyTaxonomy(HttpServletRequest request, HttpServletResponse response) {
        Survey survey = getSurvey(request.getParameter("surveyId"));

        Set<IndicatorSpecies> speciesSet = survey.getSpecies();
        SpeciesListType listType = null;
        Set<TaxonGroup> taxonGroupSet = new HashSet<TaxonGroup>();
        TaxonGroup taxonGroup = null;

        if(speciesSet.isEmpty()) {
            listType = SpeciesListType.ALL_SPECIES;
        } else if(speciesSet.size() == 1) {
            listType = SpeciesListType.ONE_SPECIES;
            taxonGroupSet.add(speciesSet.iterator().next().getTaxonGroup());
        } else {
            //TODO fixme, coz i don't work properly
            // Need to work out if all the species are in the same group.
            listType = SpeciesListType.SPECIES_GROUP;
            for(IndicatorSpecies species: speciesSet) {
                taxonGroupSet.add(species.getTaxonGroup());
                if(listType == null) {
                    if(taxonGroup == null) {
                        taxonGroup = species.getTaxonGroup();
                    }
                    if(!taxonGroup.equals(species.getTaxonGroup())) {
                        listType = SpeciesListType.MANY_SPECIES;
                        taxonGroup = null;
                    }
                }
            }
        }

        ModelAndView mv = new ModelAndView("surveyEditTaxonomy");
        mv.addObject("survey", survey);
        mv.addObject("taxonGroupSet", taxonGroupSet);
        mv.addObject("listType", listType);

        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editTaxonomy.htm", method = RequestMethod.POST)
    public ModelAndView submitSurveyTaxonomy(HttpServletRequest request, HttpServletResponse response) {
        Survey survey = getSurvey(request.getParameter("surveyId"));

        Set<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();

        SpeciesListType listType = SpeciesListType.valueOf(request.getParameter("speciesListType"));
        switch(listType) {
            case ONE_SPECIES:
            case MANY_SPECIES:
                if(request.getParameterValues("species") != null) {
                    Integer[] pks = new Integer[request.getParameterValues("species").length];
                    int i=0;
                    for(String rawPk : request.getParameterValues("species")) {
                        pks[i] = Integer.parseInt(rawPk);
                        i+=1;
                    }
                    speciesSet.addAll(taxaDAO.getIndicatorSpeciesById(pks));
                }
                break;
            case SPECIES_GROUP:
                if(request.getParameterValues("speciesGroup") != null) {
                    Integer[] pks = new Integer[request.getParameterValues("speciesGroup").length];
                    int i=0;
                    for(String rawPk : request.getParameterValues("speciesGroup")) {
                        pks[i] = Integer.parseInt(rawPk);
                        i+=1;
                    }
                    
                    //TODO unhardwire this constant.
                    int count = taxaDAO.countIndicatorSpecies(pks);
                    log.debug("Counted " + count + " species in groups");
                    if (count < 10000) {
                    	speciesSet.addAll(taxaDAO.getIndicatorSpecies(pks));
                    }
                    else
                    {
                    	getRequestContext().addMessage("bdrs.survey.taxonomy.tooManyTaxa", new Object[]{survey.getName()});
                    }
                }
                break;
            case ALL_SPECIES:
                break;
            default:
                log.error("Unknown Species List Type: "+listType);
                break;
        }
        survey.setSpecies(speciesSet);

        // Update the form rendering type given the new criteria
        SurveyFormRendererType formRenderType = survey.getFormRendererType();
        if(formRenderType == null || (formRenderType != null && !formRenderType.isEligible(survey))) {
            Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
            metadataDAO.save(md);
        }
        surveyDAO.save(survey);

        getRequestContext().addMessage("bdrs.survey.taxonomy.success", new Object[]{survey.getName()});

        ModelAndView mv;
        if(request.getParameter("saveAndContinue") != null) {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/editAttributes.htm", true));
            mv.addObject("surveyId", survey.getId());
        }
        else {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/listing.htm", true));
        }
        return mv;
    }

    private Survey getSurvey(String rawSurveyId) {
        if(rawSurveyId == null){
            // Do not know which survey to deal with. Bail out.
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        return surveyDAO.getSurvey(Integer.parseInt(rawSurveyId));
    }
}