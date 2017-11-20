package julian.cames.patchs.services.uniparam.dto;

public class MergeData {
	String srcData;
	String toMergeData;
	//mandatory constructor
	public MergeData() {}
	public MergeData(String srcData, String toMergeData) {
		super();
		this.srcData = srcData;
		this.toMergeData = toMergeData;
	}
	public String getSrcData() {
		return srcData;
	}
	public void setSrcData(String srcData) {
		this.srcData = srcData;
	}
	public String getToMergeData() {
		return toMergeData;
	}
	public void setToMergeData(String toMergeData) {
		this.toMergeData = toMergeData;
	}
	
}
