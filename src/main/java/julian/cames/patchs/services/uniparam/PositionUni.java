package julian.cames.patchs.services.uniparam;

public class PositionUni {
	int positionConfig;
	int positionName;
	int lengthConfig;
	String positionConfigHex;
	String positionNameHex;
	String lengthConfigHex;
	String filename;
	String configuration;
	int index;
	public PositionUni(int positionConfig, int positionName, int lengthConfig, String positionConfigHex,
			String positionNameHex, String lengthConfigHex, String filename, String configuration, int index) {
		super();
		this.positionConfig = positionConfig;
		this.positionName = positionName;
		this.lengthConfig = lengthConfig;
		this.positionConfigHex = positionConfigHex;
		this.positionNameHex = positionNameHex;
		this.lengthConfigHex = lengthConfigHex;
		this.filename = filename;
		this.configuration = configuration;
		this.index = index;
	}
	public int getPositionConfig() {
		return positionConfig;
	}
	public void setPositionConfig(int positionConfig) {
		this.positionConfig = positionConfig;
	}
	public int getPositionName() {
		return positionName;
	}
	public void setPositionName(int positionName) {
		this.positionName = positionName;
	}
	public int getLengthConfig() {
		return lengthConfig;
	}
	public void setLengthConfig(int lengthConfig) {
		this.lengthConfig = lengthConfig;
	}
	public String getPositionConfigHex() {
		return positionConfigHex;
	}
	public void setPositionConfigHex(String positionConfigHex) {
		this.positionConfigHex = positionConfigHex;
	}
	public String getPositionNameHex() {
		return positionNameHex;
	}
	public void setPositionNameHex(String positionNameHex) {
		this.positionNameHex = positionNameHex;
	}
	public String getLengthConfigHex() {
		return lengthConfigHex;
	}
	public void setLengthConfigHex(String lengthConfigHex) {
		this.lengthConfigHex = lengthConfigHex;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getConfiguration() {
		return configuration;
	}
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
}
