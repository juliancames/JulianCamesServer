package julian.cames.patchs.services.uniparam;

public class ModifyUniParam {
	int indexName;
	String srcData;
	//mandatory constructor
	public ModifyUniParam() {}
	//constructor
	public ModifyUniParam(int indexName, String srcData) {
		super();
		this.indexName = indexName;
		this.srcData = srcData;
	}
	public int getIndexName() {
		return indexName;
	}
	public void setIndexName(int indexValue) {
		this.indexName = indexValue;
	}
	public String getSrcData() {
		return srcData;
	}
	public void setSrcData(String srcData) {
		this.srcData = srcData;
	}
	
}
