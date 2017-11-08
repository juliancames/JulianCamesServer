package julian.cames.patchs.services.uniparam;

public class AddUniParam {
	int indexName;
	String srcData;
	String numKit;
	//mandatory constructor
	public AddUniParam() {}
	public AddUniParam(int indexName, String srcData, String numKit) {
		super();
		this.indexName = indexName;
		this.srcData = srcData;
		this.numKit = numKit;
	}
	public int getIndexName() {
		return indexName;
	}
	public void setIndexName(int indexName) {
		this.indexName = indexName;
	}
	public String getSrcData() {
		return srcData;
	}
	public void setSrcData(String srcData) {
		this.srcData = srcData;
	}
	public String getNumKit() {
		return numKit;
	}
	public void setNumKit(String numKit) {
		this.numKit = numKit;
	}
	
}
