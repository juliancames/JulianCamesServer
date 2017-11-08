package julian.cames.patchs.services.uniparam.dto;

public class kitConfiguration {
	String filenames[];	
	String[] colors;
	public kitConfiguration(String[] filenames, String[] colors) {
		super();
		this.filenames = filenames;
		this.colors = colors;
	}
	public String[] getFilenames() {
		return filenames;
	}
	public void setFilenames(String[] filenames) {
		this.filenames = filenames;
	}
	public String[] getColors() {
		return colors;
	}
	public void setColors(String[] colors) {
		this.colors = colors;
	}
	
}
