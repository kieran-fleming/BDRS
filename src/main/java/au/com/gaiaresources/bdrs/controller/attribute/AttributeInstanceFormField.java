package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;

/**
 * The <code>AttributeInstanceFormField</code> represents an
 * <code>Attribute</code> on a <code>Survey</code>.
 */
public class AttributeInstanceFormField extends AbstractAttributeFormField {

    private Attribute attribute = null;
    private AttributeDAO attributeDAO;
    private String weightName;

    private Collection<AttributeOption> optionsToDelete = null;

    /**
     * Creates and populates a new <code>Attribute</code>.
     * 
     * @param attributeDAO
     *            the database object to use when saving the attribute.
     * @param index
     *            the index of this field. The first added field is 1, the
     *            second is 2 and so on.
     * @param parameterMap
     *            the map of POST parameters that the form field will utilise to
     *            populate the <code>Attribute</code> that is created.
     */
    AttributeInstanceFormField(AttributeDAO attributeDAO, int index,
            Map<String, String[]> parameterMap) {
        this(index);
        this.attributeDAO = attributeDAO;

        this.attribute = new Attribute();
        this.attribute.setName(getParameter(parameterMap, String.format("add_name_%d",index)));
        this.attribute.setDescription(getParameter(parameterMap, String.format("add_description_%d",index)));
        this.attribute.setTypeCode(getParameter(parameterMap, String.format("add_typeCode_%d",index)));
        this.attribute.setRequired(getParameter(parameterMap, String.format("add_required_%d",index)) != null);
        this.attribute.setTag(Boolean.parseBoolean(getParameter(parameterMap, String.format("add_tag_%d",index))));
        String attrScopeStr = getParameter(parameterMap, String.format("add_scope_%d",index));
        if(attrScopeStr != null) {
            this.attribute.setScope(AttributeScope.valueOf(attrScopeStr));
        }
        this.attribute.setWeight(Integer.parseInt(getParameter(parameterMap, this.weightName)));

        // Options
        AttributeOption opt;
        List<AttributeOption> optList = new ArrayList<AttributeOption>();
        if (getParameter(parameterMap, String.format("add_option_%d",index)) != null) {
            for (String optValue : getParameter(parameterMap, String.format("add_option_%d",index)).split(",")) {
                optValue = optValue.trim();
                if (!optValue.isEmpty()) {
                    opt = new AttributeOption();
                    opt.setValue(optValue);
                    optList.add(opt);
                }
            }
        }
        attribute.setOptions(optList);
    }

    /**
     * Updates the specified <code>Attribute</code> using the POST parameters
     * provided.
     * 
     * @param attributeDAO
     *            the database object to use when saving the attribute.
     * @param attribute
     *            the <code>Attribute</code> that shall be updated.
     * @param parameterMap
     *            the map of POST parameters that the form field will utilise to
     *            populate the <code>Attribute</code> that is created.
     */
    AttributeInstanceFormField(AttributeDAO attributeDAO, Attribute attribute,
            Map<String, String[]> parameterMap) {
        this(attributeDAO, attribute);

        int attributePk = attribute.getId();
        this.attribute.setName(getParameter(parameterMap, String.format("name_%d",attributePk)));
        this.attribute.setDescription(getParameter(parameterMap, String.format("description_%d",attributePk)));
        this.attribute.setTypeCode(getParameter(parameterMap, String.format("typeCode_%d",attributePk)));
        this.attribute.setRequired(getParameter(parameterMap, String.format("required_%d",attributePk)) != null);
        this.attribute.setTag(Boolean.parseBoolean(getParameter(parameterMap, String.format("tag_%d",attributePk))));
        String attrScopeStr = getParameter(parameterMap, String.format("scope_%d",attributePk));
        if(attrScopeStr != null) {
            this.attribute.setScope(AttributeScope.valueOf(attrScopeStr));
        }
        this.attribute.setWeight(Integer.parseInt(getParameter(parameterMap, this.weightName)));

        // Options
        List<AttributeOption> optList = new ArrayList<AttributeOption>();
        if (getParameter(parameterMap, "option_" + attributePk) != null) {
            Map<String, AttributeOption> optMap = new HashMap<String, AttributeOption>();
            for (AttributeOption opt : this.attribute.getOptions()) {
                optMap.put(opt.getValue(), opt);
            }
            AttributeOption opt;
            for (String optValue : getParameter(parameterMap, "option_"
                    + attributePk).split(",")) {
                optValue = optValue.trim();
                if (!optValue.isEmpty()) {
                    if (optMap.containsKey(optValue)) {
                        // Preexisting option
                        opt = optMap.remove(optValue);
                    } else {
                        // New option
                        opt = new AttributeOption();
                        opt.setValue(optValue);
                        //taxaDAO.save(opt);
                    }
                    optList.add(opt);
                }
            }
            optionsToDelete = optMap.values();
        }
        this.attribute.setOptions(optList);
    }

    /**
     * Creates a new form field for a previously saved <code>Attribute</code>.
     * 
     * @param attributeDAO
     *            the database object to use when saving the attribute.
     * @param attribute
     *            the attribute where the form field value is stored.
     */
    AttributeInstanceFormField(AttributeDAO attributeDAO, Attribute attribute) {
        this.attributeDAO = attributeDAO;
        this.attribute = attribute;

        this.weightName = "weight_" + attribute.getId();
    }

    /**
     * Creates a new, blank form field that is used for adding new
     * <code>Attributes</code>.
     * 
     * Note: You cannot invoke {@link AttributeFormField#save()} on fields
     * returned by this invocation because not all parameters required to
     * construct a valid Attribute (such as description and type) are available.
     * To create a new Attribute see
     * {@link #createAttributeFormField(AttributeDAO, int, Map)}
     * 
     * @param index
     *            the index of this field. The first added field is 1, the
     *            second is 2 and so on.
     */
    AttributeInstanceFormField(int index) {
        this.weightName = "add_weight_" + index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight() {
        return attribute == null ? 0 : attribute.getWeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWeight(int weight) {
        attribute.setWeight(weight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentImpl save() {

        // Save the attribute options.
        for (int i = 0; i < attribute.getOptions().size(); i++) {
            attribute.getOptions().set(i, attributeDAO.save(attribute.getOptions().get(i)));
        }
        // Save the attribute
        Attribute attr = attributeDAO.save(this.attribute);

        // Delete any dereferenced options.
        if (optionsToDelete != null) {
            for (AttributeOption option : optionsToDelete) {
                attributeDAO.delete(option);
            }
            optionsToDelete = null;
        }

        return attr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttributeField() {
        return true;
    }

    /**
     * Returns the <code>Attribute</code> represented by this form field.
     * 
     * @return the <code>Attribute</code> represented by this form field.
     */
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Sets the <code>Sttribute</code> represented by this form field.
     * 
     * @param attribute
     *            the <code>Sttribute</code> represented by this form field.
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWeightName() {
        return this.weightName;
    }
}
