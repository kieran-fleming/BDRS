package au.com.gaiaresources.bdrs.controller.signup;

public class UserSignUpForm {
	private String emailAddress;

	private String firstName;

	private String lastName;

	private String password;

	private String schoolName;
	
	private String schoolSuburb;
	
	private String contactPhoneNumber;
	
	private String userName;

	private String confirmPassword;
	
	private String climateWatchUserName;

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

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

    /**
     * @param schoolSuburb the schoolSuburb to set
     */
    public void setSchoolSuburb(String schoolSuburb) {
        this.schoolSuburb = schoolSuburb;
    }

    /**
     * @return the schoolSuburb
     */
    public String getSchoolSuburb() {
        return schoolSuburb;
    }

    /**
     * @param contactPhoneNumber the contactPhoneNumber to set
     */
    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    /**
     * @return the contactPhoneNumber
     */
    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param climateWatchUserName the climateWatchUserName to set
     */
    public void setClimateWatchUserName(String climateWatchUserName) {
        this.climateWatchUserName = climateWatchUserName;
    }

    /**
     * @return the climateWatchUserName
     */
    public String getClimateWatchUserName() {
        return climateWatchUserName;
    }
}
