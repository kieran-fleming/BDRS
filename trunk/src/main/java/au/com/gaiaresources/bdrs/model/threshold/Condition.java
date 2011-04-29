package au.com.gaiaresources.bdrs.model.threshold;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;
import org.springframework.beans.BeanUtils;

import au.com.gaiaresources.bdrs.annotation.NoThreshold;
import au.com.gaiaresources.bdrs.annotation.Sensitive;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.service.threshold.ComplexTypeOperator;
import au.com.gaiaresources.bdrs.service.threshold.ConditionOperatorHandler;
import au.com.gaiaresources.bdrs.service.threshold.ThresholdService;

/**
 * The <code>Condition</code> represents one (of many) criteria in a threshold
 * that must be met before the threshold action is applied.
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "CONDITION")
@AttributeOverride(name = "id", column = @Column(name = "ID"))
public class Condition extends PortalPersistentImpl {

    private Logger log = Logger.getLogger(getClass());

    private String className;
    private String propertyPath;

    private String key;
    private String value;

    private Operator keyOperator;
    private Operator valueOperator;

    /**
     * Creates a new Condition.
     */
    public Condition() {
        super();
    }

    @Column(name = "PROPERTYPATH", nullable = false)
    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    @Column(name = "KEY", nullable = true)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Column(name = "VALUE", nullable = false)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public void setValue(Integer value) {
        this.value = value.toString();
    }
    
    public void setValue(Long value) {
        this.value = value.toString();
    }
    
    public void setValue(Boolean value) {
        this.value = value.toString();
    }
    
    public void setValue(Date value) {
        this.value = getDateFormat().format(value);
    }
    
    public void setValue(Double value) {
        this.value = value.toString();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "KEYOPERATOR", nullable = true)
    public Operator getKeyOperator() {
        return keyOperator;
    }

    public void setKeyOperator(Operator keyOperator) {
        this.keyOperator = keyOperator;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "VALUEOPERATOR", nullable = false)
    public Operator getValueOperator() {
        return valueOperator;
    }

    public void setValueOperator(Operator valueOperator) {
        this.valueOperator = valueOperator;
    }

    @Column(name = "CLASSNAME", nullable = false)
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the datatype of the item at the end of the property path.
     * 
     * @return the datatype of the item at the end of the property path.
     */
    @Transient
    public Class<?> getTargetClassForPath() throws ClassNotFoundException {
        Class<?> klass = Class.forName(this.className);
        Class<?> target = klass;

        if (this.propertyPath != null) {
            for (String propName : this.propertyPath.split("\\.")) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(target, propName);
                if (pd.getReadMethod() != null) {
                    target = pd.getReadMethod().getReturnType();
                }
            }
        }
        return target;
    }

    /**
     * Returns the parameterized type of the iterable at the end of the property
     * path.
     * 
     * @return the parameterized type of the iterable at the end of the property
     *         path or null if the property path does not lead to an iterable.
     * @throws ClassNotFoundException
     */
    @Transient
    public Class<?> getTargetIterableTypeForPath()
            throws ClassNotFoundException {
        Class<?> klass = Class.forName(this.className);
        Class<?> target = klass;
        Class<?> iterableType = null;

        if (this.propertyPath != null) {
            for (String propName : this.propertyPath.split("\\.")) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(target, propName);
                Method readMethod = pd.getReadMethod();
                if (readMethod != null) {
                    target = readMethod.getReturnType();
                    iterableType = extractIterableType(readMethod);
                }
            }
        }

        return iterableType;
    }

    /**
     * Returns the parametrized type of the iterable return value of the
     * specified {@link Method}.
     * 
     * @param readMethod
     *            the method that shall return the iterable.
     * @return the parametrized type of the method return value or null if the
     *         method does not return an {@link Iterable}.
     */
    private Class<?> extractIterableType(Method readMethod) {
        Class<?> iterableType = null;
        Class<?> iterable = readMethod.getReturnType();
        if (Iterable.class.isAssignableFrom(iterable)) {
            Type genericReturnType = readMethod.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                Type[] typeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                if (typeArguments.length == 0) {
                    log.error("Cannot get target iterable type because the type argument array is empty");
                } else if (typeArguments.length > 1) {
                    log.error("More than one type argument returned. Using the first.");
                    iterableType = (Class<?>) typeArguments[0];
                } else {
                    iterableType = (Class<?>) typeArguments[0];
                }
            }
        }
        return iterableType;
    }

    /**
     * Returns a {@link java.util.List} of {@link PathDescriptor}s that may be
     * appended to the end of the current property path.
     * 
     * @return a list of path descriptors that may be appended to the current
     *         property path.
     * @throws ClassNotFoundException
     */
    @Transient
    public List<PathDescriptor> getChildPathDescriptors()
            throws ClassNotFoundException {
        Class<?> target = getTargetClassForPath();
        return listChildPathDescriptors(target, this.propertyPath);
    }

    /**
     * Returns an ordered map of {@link PathDescriptor}s where the first item is
     * the first segment of the property path and the last item is the last
     * segment of the property path. The key of the map is the segment of the
     * property path and the value list is the possible path descriptors at that
     * level.
     * 
     * @return ordered map of {@link PathDescriptor}
     * @throws ClassNotFoundException
     */
    @Transient
    public Map<PathDescriptor, List<PathDescriptor>> getPathDescriptorsForPath()
            throws ClassNotFoundException {

        Map<PathDescriptor, List<PathDescriptor>> orderedMap = new LinkedHashMap<PathDescriptor, List<PathDescriptor>>();

        Class<?> klass = Class.forName(this.className);
        Class<?> target = klass;

        if (this.propertyPath != null) {
            StringBuilder pathBuilder = new StringBuilder();
            for (String propName : this.propertyPath.split("\\.")) {

                List<PathDescriptor> childPathDescriptors = listChildPathDescriptors(target, pathBuilder.toString());

                // Build the path string
                if (pathBuilder.length() > 0) {
                    pathBuilder.append(".");
                }
                pathBuilder.append(propName);

                // Selection option
                PropertyDescriptor keyPD = BeanUtils.getPropertyDescriptor(target, propName);
                PathDescriptor pathDescriptor = new PathDescriptor(
                        pathBuilder.toString(), keyPD);

                // Potential options
                orderedMap.put(pathDescriptor, childPathDescriptors);
                target = keyPD.getReadMethod().getReturnType();
            }
        } else {
            List<PathDescriptor> valueList = new ArrayList<PathDescriptor>();
            for (PropertyDescriptor pd : listPropertyDescriptors(target)) {
                valueList.add(new PathDescriptor(pd.getName(), pd));
            }
            if (!valueList.isEmpty()) {
                orderedMap.put(valueList.get(0), valueList);
            }
        }

        return orderedMap;
    }

    /**
     * Returns a {@link List} of {@link PathDescriptor}s below the parent path
     * for the specified <code>Class</code>.
     * 
     * @param klass
     * @param parentPropertyPath
     * @return a {@link List} of {@link PathDescriptor}s for the class under the
     *         property path.
     */
    @Transient
    private List<PathDescriptor> listChildPathDescriptors(Class<?> klass,
            String parentPropertyPath) {

        String pathTmpl = parentPropertyPath.isEmpty() ? "%s%s" : "%s.%s";
        List<PathDescriptor> pathDescriptorList = new ArrayList<PathDescriptor>();
        for (PropertyDescriptor pd : listPropertyDescriptors(klass)) {
            pathDescriptorList.add(new PathDescriptor(
                    String.format(pathTmpl, parentPropertyPath, pd.getName()),
                    pd));
        }

        return pathDescriptorList;
    }

    @Transient
    private List<PropertyDescriptor> listPropertyDescriptors(Class<?> klass) {
        List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
        for (PropertyDescriptor value : BeanUtils.getPropertyDescriptors(klass)) {
            if (value.getReadMethod() != null &&
                    value.getReadMethod().getAnnotation(Sensitive.class) == null &&
                    value.getReadMethod().getAnnotation(NoThreshold.class) == null) {
                Class<?> returnType = value.getReadMethod().getReturnType();
                if (PersistentImpl.class.isAssignableFrom(returnType)
                        || ThresholdService.SIMPLE_TYPE_TO_OPERATOR_MAP.containsKey(returnType)) {
                    propertyDescriptorList.add(value);
                } else if (Iterable.class.isAssignableFrom(returnType)) {
                    Class<?> iterableType = extractIterableType(value.getReadMethod());
                    if (ThresholdService.COMPLEX_TYPE_TO_OPERATOR_MAP.containsKey(iterableType)) {
                        propertyDescriptorList.add(value);
                    }
                }
            }
        }
        return propertyDescriptorList;
    }

    /**
     * Returns true if this condition applies to a simple property, false
     * otherwise.
     * 
     * @return true if this condition applies to a simple property, false
     *         otherwise.
     * @throws ClassNotFoundException
     * @see {@link ThresholdService#SIMPLE_TYPE_TO_OPERATOR_MAP}
     */
    @Transient
    public boolean isSimplePropertyType() throws ClassNotFoundException {
        Class<?> target = getTargetClassForPath();
        return ThresholdService.SIMPLE_TYPE_TO_OPERATOR_MAP.containsKey(target);
    }

    /**
     * Returns the complex type operator for this condition or null if this
     * condition does not apply to a complex property.
     * 
     * @return the complex type operator for this condition
     * @throws ClassNotFoundException
     * @see {@link ThresholdService#COMPLEX_TYPE_TO_OPERATOR_MAP}
     */
    @Transient
    public ComplexTypeOperator getComplexTypeOperator()
            throws ClassNotFoundException {
        Class<?> target = getTargetClassForPath();
        return this.getComplexTypeOperator(target);
    }

    private ComplexTypeOperator getComplexTypeOperator(Class<?> target)
            throws ClassNotFoundException {
        if (!Iterable.class.isAssignableFrom(target)) {
            return null;
        }
        return ThresholdService.COMPLEX_TYPE_TO_OPERATOR_MAP.get(getTargetIterableTypeForPath());
    }

    /**
     * Returns an array of {@link Operator}s that may be applied to the datatype
     * at the end of the property path. If this condition is for a complex
     * property type, then the operators for the key shall be returned.
     * 
     * @return an array of {@link Operator}s that may be applied to the datatype
     *         at the end of the property path.
     * @throws ClassNotFoundException
     * @see {@link ThresholdService#COMPLEX_TYPE_TO_OPERATOR_MAP}
     */
    @Transient
    public Operator[] getPossibleKeyOperators() throws ClassNotFoundException {
        Class<?> target = getTargetClassForPath();
        Operator[] operators = ThresholdService.SIMPLE_TYPE_TO_OPERATOR_MAP.get(target);
        if (operators == null) {
            // This could be null but should not be.
            ComplexTypeOperator complexOperator = ThresholdService.COMPLEX_TYPE_TO_OPERATOR_MAP.get(target);
            operators = complexOperator.getKeyOperators();
        }
        return operators;
    }

    /**
     * Returns an array of {@link Operator}s that may be applied to the datatype
     * at the end of the property path. If this condition is for a complex
     * property type, then the operators for the value shall be returned.
     * 
     * @return an array of {@link Operator}s that may be applied to the datatype
     *         at the end of the property path.
     * @throws ClassNotFoundException
     * @see {@link ThresholdService#COMPLEX_TYPE_TO_OPERATOR_MAP}
     */
    @Transient
    public Operator[] getPossibleValueOperators() throws ClassNotFoundException {
        Class<?> target = getTargetClassForPath();
        Operator[] operators = ThresholdService.SIMPLE_TYPE_TO_OPERATOR_MAP.get(target);
        if (operators == null) {
            if (Iterable.class.isAssignableFrom(target)) {
                // This could be null but should not be.
                operators = getComplexTypeOperator().getValueOperators();
            } else {
                log.warn("Target class is not a simple type or an iterable");
            }
        }

        return operators;
    }

    @Transient
    public String stringKey() {
        return this.key;
    }

    @Transient
    public String stringValue() {
        return this.value;
    }

    @Transient
    public Boolean booleanKey() {
        return booleanValue(this.key);
    }

    @Transient
    public Boolean booleanValue() {
        return booleanValue(this.value);
    }

    @Transient
    private Boolean booleanValue(String val) {
        if (val == null) {
            return null;
        } else {
            return Boolean.parseBoolean(val);
        }
    }

    @Transient
    public Integer intKey() {
        return intValue(this.key);
    }

    @Transient
    public Integer intValue() {
        return intValue(this.value);
    }

    @Transient
    private Integer intValue(String val) {
        if (val == null) {
            return null;
        } else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    @Transient
    public Long longKey() {
        return longValue(this.key);
    }

    @Transient
    public Long longValue() {
        return longValue(this.value);
    }

    @Transient
    private Long longValue(String val) {
        if (val == null) {
            return null;
        } else {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    @Transient
    public Double doubleKey() {
        return doubleValue(this.key);
    }

    @Transient
    public Double doubleValue() {
        return doubleValue(this.value);
    }

    @Transient
    private Double doubleValue(String val) {
        if (val == null) {
            return null;
        } else {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    @Transient
    public Float floatKey() {
        return floatValue(this.key);
    }

    @Transient
    public Float floatValue() {
        return floatValue(this.value);
    }

    @Transient
    private Float floatValue(String val) {
        if (val == null) {
            return null;
        } else {
            try {
                return Float.parseFloat(val);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    @Transient
    public Date dateKey() {
        return dateValue(this.key);
    }

    @Transient
    public Date dateValue() {
        return dateValue(this.value);
    }

    @Transient
    private Date dateValue(String val) {
        try {
            return getDateFormat().parse(val);
        } catch (ParseException e) {
            log.warn("Unable to parse date value for string: " + val);
            log.warn(e);
            return null;
        }
    }
    
    @Transient
    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd MMM yyyy"); 
    }

    /**
     * Returns <code>true</code> if the specified entity matches the criteria
     * defined by this <code>Condition</code>, false otherwise.
     * 
     * @param sesh
     *            the session to use if database access is required.
     * @param entity
     *            the entity to be tested.
     * @param operatorHandler
     *            the repository of handlers for possible operations.
     * @return true if the entity matches the condition, false otherwise.
     */
    @Transient
    public boolean applyCondition(Session sesh, Object entity,
            ConditionOperatorHandler operatorHandler) {

        if (!entity.getClass().getCanonicalName().equals(this.className)) {
            System.err.println(String.format("Received object with type \"%s\" but condition filter applies to type \"%s\". This should never occur.", entity.getClass().getCanonicalName(), this.className));
            log.error(String.format("Received object with type \"%s\" but condition filter applies to type \"%s\". This should never occur.", entity.getClass().getCanonicalName(), this.className));
            return false;
        }

        return operatorHandler.match(sesh, entity, this);
    }

    /**
     * Returns the object instance at the end of the property path using the
     * specified <code>instance</code> as the starting point.
     * 
     * @param instance
     *            the starting instance
     * @return the instance at the end of the property path.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Transient
    public Object getPropertyForPath(Object instance)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        Object currentInstance = instance;
        if (this.propertyPath != null) {
            for (String propName : this.propertyPath.split("\\.")) {
                if (currentInstance == null) {
                    // There is no point digging deeper because the path is now
                    // broken. 
                    return null;
                }
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(currentInstance.getClass(), propName);
                Method readMethod = pd.getReadMethod();
                currentInstance = readMethod.invoke(currentInstance, new Object[] {});
            }
            return currentInstance;
        } else {
            return instance;
        }
    }

}
