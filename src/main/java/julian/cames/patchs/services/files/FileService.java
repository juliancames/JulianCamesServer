package julian.cames.patchs.services.files;

public interface FileService {
	public byte[] getFileBytes(String path) throws Exception;
	public byte[] getDataBytes(String strBytes) throws Exception;
	public String getJsonFromBytes(byte[] bytes) throws Exception;
	public String bytesToHexString(byte[] bytes);
	public int bit16ToInt(byte[] bit16);
	public int bit32ToInt(byte[] bit32);
	public int indexOfByteArray(byte[] array, byte valueToFind, int startIndex);
	public String bytesToString(byte[] value);
	public byte[] StringToBytes(String value);
	public byte[] IntToBit32(int integer);
}
