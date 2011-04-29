package au.com.gaiaresources.bdrs.model.user;

import java.util.Date;


public class CM3UserImpl extends User {
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String password;
    private String name;
    private String[] roles;
    private Boolean active;
    private String registrationKey;
    private String sid;

	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String[] getRoles() {
		return roles;
	}
	public void setRoles(String[] roles) {
		this.roles = roles;
	}
	public Boolean isActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public String getRegistrationKey() {
		return registrationKey;
	}
	public void setRegistrationKey(String registrationKey) {
		this.registrationKey = registrationKey;
	}

	@Override
	public Date getCreatedAt() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Integer getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getUpdatedAt() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Integer getUpdatedBy() {
		// TODO Auto-generated method stub
		return null;
	}


 }
