package au.com.gaiaresources.bdrs.model.method;

public enum Taxonomic {
	TAXONOMIC("Taxonomic"),
	NONTAXONOMIC("Non Taxonomic"),
	OPTIONALLYTAXONOMIC("Optionally Taxonomic");
	
	private String name;
	private Taxonomic(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
			
	public void setName(String name) {
		this.name = name;	
	}
}
