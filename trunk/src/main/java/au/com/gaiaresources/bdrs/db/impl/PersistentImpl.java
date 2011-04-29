package au.com.gaiaresources.bdrs.db.impl;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.annotation.MobileField;
import au.com.gaiaresources.bdrs.annotation.NoThreshold;
import au.com.gaiaresources.bdrs.annotation.Sensitive;
import au.com.gaiaresources.bdrs.db.Persistent;
import au.com.gaiaresources.bdrs.serialization.DataInterchangeSerializable;

@MappedSuperclass
public abstract class PersistentImpl implements Persistent,
        DataInterchangeSerializable {
    public static final int DEFAULT_WEIGHT = 0;

    private Logger log = Logger.getLogger(getClass());

    private Integer id;
    private Date createdAt;
    private Date updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    
    private int weight = DEFAULT_WEIGHT;

    /**
     * {@inheritDoc}
     */
    @Id
    @NoThreshold
    @MobileField(name = "server_id")
    @CompactAttribute
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSISTENT_ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @NoThreshold
    @Column(name = "CREATED_AT")
    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    protected void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * {@inheritDoc}
     */
    @NoThreshold
    @Column(name = "UPDATED_AT")
    @Override
    public Date getUpdatedAt() {
        return updatedAt;
    }

    protected void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * {@inheritDoc}
     */
    @NoThreshold
    @Column(name = "CREATED_BY")
    @Override
    public Integer getCreatedBy() {
        return createdBy;
    }

    protected void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * {@inheritDoc}
     */
    @NoThreshold
    @Column(name = "UPDATED_BY")
    @Override
    public Integer getUpdatedBy() {
        return updatedBy;
    }

    protected void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Equals. Check if this and the other object are representations of the
     * same instance in the database. If <code>other</code> is of the same class
     * then compare IDs.
     * 
     * @param other
     *            The <code>Object</code> to compare to.
     * @return <code>boolean</code>.
     */
    public boolean equals(Object other) {
        if (other != null && other.getClass().equals(getClass())) {
            Persistent that = (Persistent) other;
            if ((this.getId() != null) && (that.getId() != null))
                return this.getId().equals(that.getId());
            else
                return super.equals(other);
        }
        return false;
    }

    /**
     * Returns the hashCode of the class name concatenated with the id.
     * 
     * @return <code>int</code>.
     */
    public int hashCode() {
        return (getClass().getName() + getId()).hashCode();
    }

    @Override
    @Transient
    public Map<String, Object> flatten() {
        return this.flatten(0, false, false);
    }
    
    @Override
    @Transient
    public Map<String, Object> flatten(int depth) {
    	return this.flatten(depth, false, false);
    }
    
    @Override
    @Transient
    public Map<String, Object> flatten(boolean compact, boolean mobileFields) {
        return this.flatten(0, compact, mobileFields);
    }
    
    @Override
    @Transient
    public Map<String, Object> flatten(int depth, boolean compact, boolean mobileFields) {
    	Map<String, Object> map = new HashMap<String, Object>();

        try {
            Object value;
            
            Method readMethod;
            PersistentImpl persistImpl;
            
            PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(getClass());
            
            for (PropertyDescriptor pd : descriptors) {
            	
                // Skip the attributes marked as sensitive.
                readMethod = pd.getReadMethod();
                String name;
                if (mobileFields) {
                	MobileField mf = readMethod != null ? readMethod.getAnnotation(MobileField.class) : null;
                	name = mf != null ? mf.name() : pd.getName();
                } else {
                	name = pd.getName();
                }
                if (readMethod != null
                        && readMethod.getAnnotation(Sensitive.class) == null
                        && (!compact || readMethod.getAnnotation(CompactAttribute.class) != null)) {

                    Class<?> returnType = readMethod.getReturnType();
                    value = readMethod.invoke(this);
                    if (Iterable.class.isAssignableFrom(returnType)) {
                    	List<Object> list = new ArrayList<Object>();
                        if (value != null) {
                            Iterator<?> iterator = ((Iterable<?>) value).iterator();
                            Object raw;
                            while (iterator.hasNext()) {
                                raw = iterator.next();
                                if(raw == null) {
                                    list.add(null);
                                } else if (raw instanceof PersistentImpl) {
                                    Object val;
                                    persistImpl = (PersistentImpl)raw;
                                    if(depth > 0) {
                                        val = persistImpl.flatten(depth-1, compact, mobileFields);
                                    } else {
                                        val = persistImpl.getId();
                                    }
                                    list.add(val);
                                } else {
                                    list.add(raw.toString());
                                }
                            }
                        }
                        map.put(name, list);
                        
                    } else if (String.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? ""
                                : value.toString());
                    } else if (returnType.isArray()) {
                    	List<Object> list = new ArrayList<Object>();
                        if (value != null) {
                            for (int i = 0; i < Array.getLength(value); i++) {
                                Object raw = Array.get(value, i);
                                if(raw == null) {
                                    list.add(null);
                                } else if (raw instanceof PersistentImpl) {
                                    Object val;
                                    persistImpl = (PersistentImpl)raw;
                                    if(depth > 0) {
                                        val = persistImpl.flatten(depth-1, compact, mobileFields);
                                    } else {
                                        val = persistImpl.getId();
                                    }
                                    list.add(val);
                                } else {
                                    list.add(raw.toString());
                                }
                            }
                        }
                        map.put(name, list);
                    } else if (PersistentImpl.class.isAssignableFrom(returnType)) {
                        Object val;
                        if(value == null) {
                            val = null;
                        } else {
                            persistImpl = (PersistentImpl)value; 
                            if(depth > 0) {
                                val = persistImpl.flatten(depth - 1, compact, mobileFields );
                            } else {
                                val = persistImpl.getId();
                            }
                        }
                        
                        map.put(name, val);
                    } else if (Integer.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Integer) value).intValue());
                    } else if (Long.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Long) value).longValue());
                    } else if (Date.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Date) value).getTime());
                    } else if (Byte.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Byte) value).byteValue());
                    } else if (Double.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Double) value).doubleValue());
                    } else if (Float.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Float) value).floatValue());
                    }  else if (Short.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Short) value).shortValue());
                    } else if (Boolean.class.isAssignableFrom(returnType)) {
                        map.put(name, value == null ? null
                                : ((Boolean) value).booleanValue());
                    } else if (returnType.isPrimitive()) {
                        map.put(name, value);
                    } else {
                        map.put(name, value == null ? ""
                                : value.toString());
                    }
                }
            }
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }

        map.put("_class", getClass().getSimpleName());
        
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "WEIGHT")
    @CompactAttribute
    public int getWeight() {
        return weight;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }
}
