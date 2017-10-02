package julian.cames.patchs.services.uniparam;

public class kitConfiguration {
	String filenameKit;
	String filenameBack;
	String filenameChest;
	String filenameLeg;
	String filenameName;
	public kitConfiguration(String filenameKit, String filenameBack, String filenameChest, String filenameLeg,
			String filenameName) {
		super();
		this.filenameKit = filenameKit;
		this.filenameBack = filenameBack;
		this.filenameChest = filenameChest;
		this.filenameLeg = filenameLeg;
		this.filenameName = filenameName;
	}
	public String getFilenameKit() {
		return filenameKit;
	}
	public void setFilenameKit(String filenameKit) {
		this.filenameKit = filenameKit;
	}
	public String getFilenameBack() {
		return filenameBack;
	}
	public void setFilenameBack(String filenameBack) {
		this.filenameBack = filenameBack;
	}
	public String getFilenameChest() {
		return filenameChest;
	}
	public void setFilenameChest(String filenameChest) {
		this.filenameChest = filenameChest;
	}
	public String getFilenameLeg() {
		return filenameLeg;
	}
	public void setFilenameLeg(String filenameLeg) {
		this.filenameLeg = filenameLeg;
	}
	public String getFilenameName() {
		return filenameName;
	}
	public void setFilenameName(String filenameName) {
		this.filenameName = filenameName;
	}
	
}
