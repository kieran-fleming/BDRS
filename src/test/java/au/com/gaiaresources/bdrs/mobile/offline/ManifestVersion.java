package au.com.gaiaresources.bdrs.mobile.offline;

public class ManifestVersion {
	
	String manifestVersion;
	
	
	public ManifestVersion() {
		this.manifestVersion ="#"+ String.valueOf(System.currentTimeMillis());
	}


	public void setManifestVersion(String manifestVersion) {
		this.manifestVersion = manifestVersion;
	}



	public String getManifestVersion(){
		return  this.manifestVersion;
	}

}
