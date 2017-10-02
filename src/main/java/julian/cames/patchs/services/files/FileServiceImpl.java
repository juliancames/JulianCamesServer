package julian.cames.patchs.services.files;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class FileServiceImpl implements FileService
{

	public static final int INDEX_NOT_FOUND = -1;
	private final char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	/**
	 * get bytes from Uint8Array JsonObject to byte[]
	 */
	public byte[] getDataBytes(String strBytes) throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode strObj = mapper.readTree(strBytes);		
		byte[] data = new byte[strObj.size()];
		for(int i=0; i<strObj.size(); i++){
			JsonNode current = strObj.get(String.valueOf(i));
			data[i] = (byte)current.asInt();
		}		
		return data;
	}
	
	public String getJsonFromBytes(byte[] bytes) throws Exception{
		ObjectMapper mapper = new ObjectMapper();		
		JsonNode jsonObj = mapper.createObjectNode();
		for(int i=0; i<bytes.length; i++){
			((ObjectNode) jsonObj).put(String.valueOf(i), bytes[i]);
		}    	 
    	return mapper.writeValueAsString(jsonObj);
	}
	
	/**
	 * bytes to Hex String
	 */
	public String bytesToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**
	 * Hex String to bytes
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	/**
	 * 16-bit signed byte[] to short to int
	 * LITTLE ENDIAN: invert. bites
	 * @param bytes16
	 * @return unsigned value
	 */
	public int bit16ToInt(byte[] bit16){
		ByteBuffer bb = ByteBuffer.wrap(bit16);
		bb.order(ByteOrder.LITTLE_ENDIAN);	
		int bit16short = bb.getShort(0);
		
		if(bit16short < 0)
			bit16short = ((int)bit16short) & 0xFFFF;
		
		return bit16short;
	}
	
	/**
	 * 32-bit byte[] to int
	 * LITTLE ENDIAN: invert. bites
	 * @param bytes16
	 * @return
	 */
	public int bit32ToInt(byte[] bit32){
		ByteBuffer bb = ByteBuffer.wrap(bit32);
		bb.order(ByteOrder.LITTLE_ENDIAN);	
		return bb.getInt(0);
	}
	
	public byte[] IntToBit32(int integer){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(integer);
		return bb.array();
	}
	
	/**
	 * indexOf byte array
	 */
	public int indexOfByteArray(byte[] array, byte valueToFind, int startIndex) {
      if (array == null) {
          return INDEX_NOT_FOUND; }
      if (startIndex < 0) {
          startIndex = 0; }
      for (int i = startIndex; i < array.length; i++) {
          if (valueToFind == array[i]) {
              return i;
          }
      }
      return INDEX_NOT_FOUND;
   }
	
	/**
	 * bytes to String
	 * @param value
	 * @return
	 */
	public String bytesToString(byte[] value){
		return new String(value);
	}
	
	/**
	 * String to Bytes
	 * @param value
	 * @return
	 */
	public byte[] StringToBytes(String value){
		return value.getBytes();
	}
	
	/**
	 * get bytes from File
	 */
	public byte[] getFileBytes(String path) throws Exception
	{
		Path pathFile = Paths.get(path);
		byte[] data = Files.readAllBytes(pathFile);
		
		return data;
	}
}
