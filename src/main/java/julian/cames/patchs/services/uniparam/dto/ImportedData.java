package julian.cames.patchs.services.uniparam.dto;

public class ImportedData {
	int indexName;
	String srcData;
	String newData;
	//mandatory constructor
	public ImportedData() {}
	public ImportedData(int indexName, String srcData, String newData) {
		super();
		this.indexName = indexName;
		this.srcData = srcData;
		this.newData = newData;
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
	public String getNewData() {
		return newData;
	}
	public void setNewData(String newData) {
		this.newData = newData;
	}
}
