package julian.cames.patchs.services.uniparam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import julian.cames.patchs.services.files.FileService;

@Service
public class UniformParameterImpl implements UniformParameter {
	
	@Autowired FileService fileService;
	
	@Value("${uniparam.offsetIni}") private int uniparam_offsetIni;
	@Value("${uniparam.dataSize}") private int uniparam_dataSize;
	@Value("${uniparam.numElementsOffset}") private int uniparam_numElementsOffset;
	@Value("${uniparam.numElementsSize}") private int uniparam_numElementsSize;
	@Value("${uniparam.part.posConfig.offset}") private int uniparam_part_posConfig_offset;
	@Value("${uniparam.part.posConfig.size}") private int uniparam_part_posConfig_size;
	@Value("${uniparam.part.posName.offset}") private int uniparam_part_posName_offset;
	@Value("${uniparam.part.posName.size}") private int uniparam_part_posName_size;
	@Value("${uniparam.part.lengthConfig.offset}") private int uniparam_part_lengthConfig_offset;
	@Value("${uniparam.part.lengthConfig.size}") private int uniparam_part_lengthConfig_size;
	@Value("${kit.mainName.offset}") private int kit_mainName_offset;
	@Value("${kit.mainName.size}") private int kit_mainName_size;
	
	private byte zero = 0x00;
	private final String REAL = "_realUni";
	private final String THIRD = "3rd";
	private final int LENGTH_EXT_FILE = 4;
	private final int sizeDEF = 96;
	private final int sizeREAL = 128;
	
	/**
	 * read file uniParam
	 * 
	 */
	public String readUniParam(String data) throws Exception{
		
		byte[] dataBytes = fileService.getDataBytes(data);		
		byte[] numOfElementsByte = Arrays.copyOfRange(dataBytes, uniparam_numElementsOffset, uniparam_numElementsOffset + uniparam_numElementsSize);
		int numOfElements = fileService.bit32ToInt(numOfElementsByte);

		List<PositionUni> positions = new ArrayList<PositionUni>();
		
		int offset = uniparam_offsetIni;
		for(int i=0; i<numOfElements; i++){
			byte[] dataUniParam = Arrays.copyOfRange(dataBytes, offset, offset + uniparam_dataSize);			
			
			byte[] posNameReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posName_offset, uniparam_part_posName_offset + uniparam_part_posName_size);
			byte[] posConfigReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posConfig_offset, uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
			byte[] lengthConfigReg = Arrays.copyOfRange(dataUniParam, uniparam_part_lengthConfig_offset, uniparam_part_lengthConfig_offset + uniparam_part_lengthConfig_size);
				
			//name			
			int startPositionName = fileService.bit32ToInt(posNameReg);
			int endPositionName = fileService.indexOfByteArray(dataBytes, zero, startPositionName);
			byte[] filenameBytes = Arrays.copyOfRange(dataBytes, startPositionName, endPositionName);
			String filename = fileService.bytesToString(filenameBytes);
			
			//ConfigUni
			int sizeConfig = sizeDEF;
			if(filename.indexOf(REAL)>0)
				sizeConfig = sizeREAL;
			int startPositionConfig = fileService.bit32ToInt(posConfigReg);
			byte[] configBytes = Arrays.copyOfRange(dataBytes, startPositionConfig, startPositionConfig + sizeConfig);
			
			//lengthConfig
			int lengthConfig = fileService.bit32ToInt(lengthConfigReg);
			
			positions.add(new PositionUni(	startPositionConfig,
											startPositionName,
											lengthConfig,
											fileService.bytesToHexString(posConfigReg),
											fileService.bytesToHexString(posNameReg),
											fileService.bytesToHexString(lengthConfigReg),
											filename,
											fileService.bytesToHexString(configBytes),
											offset) );	
			offset += uniparam_dataSize;
		}
		ResponseUniParam responseUniParam = new ResponseUniParam(positions, fileService.getJsonFromBytes(dataBytes));
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(responseUniParam);
		return jsonInString;
	}
	
	public String modifyRegUniParamToReal(ModifyUniParam modifyUniParam) throws Exception{
		byte[] srcData = fileService.getDataBytes(modifyUniParam.getSrcData());
		int index = modifyUniParam.getIndexName();
		
		//valid name	
		byte[] posName = Arrays.copyOfRange(srcData, index + uniparam_part_posName_offset, index + uniparam_part_posName_offset + uniparam_part_posName_size);
		int startPositionName = fileService.bit32ToInt(posName);
		int endPositionName = fileService.indexOfByteArray(srcData, zero, startPositionName);
		byte[] filenameBytes = Arrays.copyOfRange(srcData, startPositionName, endPositionName);
		String filename = fileService.bytesToString(filenameBytes);
		if(filename.indexOf(REAL)>0)
			return readUniParam(fileService.getJsonFromBytes(srcData));
		
		//update lengthConfig
		int lengthREAL = 120;
		byte[] lengthConfig = fileService.IntToBit32(lengthREAL);
		System.arraycopy(lengthConfig, 0, srcData, index + uniparam_part_lengthConfig_offset, uniparam_part_lengthConfig_size);
		
		//update name
		int oldLength = filename.length();		
		String newFileName = filename.substring(0, oldLength - LENGTH_EXT_FILE) + REAL + filename.substring(oldLength - LENGTH_EXT_FILE, oldLength);
		int newLength = newFileName.length();
		int diff = newLength - oldLength;		
		byte[] newField = fileService.StringToBytes(newFileName);
		byte[] newData;
		
		newData = updatePositions (srcData, index, diff, 0);
		newData = addDataToFile (newData, newField, oldLength, startPositionName);
		
		//update configuration
		byte[] posConfig = Arrays.copyOfRange(srcData, index + uniparam_part_posConfig_offset, index + uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
		int startPositionCfg = fileService.bit32ToInt(posConfig);		
		newField = getRealConfigBytes();			
		
		newData = updatePositions (newData, index, sizeREAL - sizeDEF, 1);
		newData = addDataToFile (newData, newField, sizeDEF, startPositionCfg);
		
		return readUniParam(fileService.getJsonFromBytes(newData));
	}
	
	public String addKit(ModifyUniParam modifyUniParam)throws Exception
	{
		byte[] srcData = fileService.getDataBytes(modifyUniParam.getSrcData());
		int index = modifyUniParam.getIndexName();
		
		//valid name	
		byte[] posName = Arrays.copyOfRange(srcData, index + uniparam_part_posName_offset, index + uniparam_part_posName_offset + uniparam_part_posName_size);
		int startPositionName = fileService.bit32ToInt(posName);
		int endPositionName = fileService.indexOfByteArray(srcData, zero, startPositionName);
		byte[] filenameBytes = Arrays.copyOfRange(srcData, startPositionName, endPositionName);
		String filename = fileService.bytesToString(filenameBytes);
		if(filename.indexOf(REAL)>0)
			return readUniParam(fileService.getJsonFromBytes(srcData));
		
		byte[] posConfig = Arrays.copyOfRange(srcData, index + uniparam_part_posConfig_offset, index + uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
		int startPositionCfg = fileService.bit32ToInt(posConfig);
		
		//get current number of elements
		byte[] numOfElementsByte = Arrays.copyOfRange(srcData, uniparam_numElementsOffset, uniparam_numElementsOffset + uniparam_numElementsSize);
		int numOfElements = fileService.bit32ToInt(numOfElementsByte);
		//set new number of elements
		numOfElements++;
		System.arraycopy(fileService.IntToBit32(numOfElements), 0, srcData, uniparam_numElementsOffset, uniparam_numElementsSize);
		
		//Add position register
		int oldLength = filename.length();
		int newstartPositionName = startPositionName + oldLength + 1;
		byte[] newDataUniParam = Arrays.copyOfRange(srcData, index, index + uniparam_dataSize);		
		System.arraycopy(fileService.IntToBit32(newstartPositionName), 0, newDataUniParam, uniparam_part_posName_offset, uniparam_part_posName_size);
		startPositionCfg += sizeDEF;
		System.arraycopy(fileService.IntToBit32( startPositionCfg ), 0, newDataUniParam, uniparam_part_posConfig_offset, uniparam_part_posConfig_size);		
		int lengthREAL = 120;
		byte[] lengthConfig = fileService.IntToBit32(lengthREAL);
		System.arraycopy(lengthConfig, 0, newDataUniParam, uniparam_part_lengthConfig_offset, uniparam_part_lengthConfig_size);
				
		byte[] newData = addDataToFile (srcData, newDataUniParam, 0, index + uniparam_dataSize);	
	    newData = updatePositions (newData, 0, uniparam_dataSize, 2);
	    
	    //Add real filename	THIRD	
		//String newFileName = filename.substring(0, oldLength - LENGTH_EXT_FILE) + REAL + filename.substring(oldLength - LENGTH_EXT_FILE, oldLength);
		String newFileName = filename.substring(0, oldLength - LENGTH_EXT_FILE - 3) + THIRD + REAL + filename.substring(oldLength - LENGTH_EXT_FILE, oldLength);
		byte[] newField = new byte[newFileName.length() + 1];
		System.arraycopy(fileService.StringToBytes(newFileName), 0, newField, 0,newFileName.length());	
		newstartPositionName += uniparam_dataSize;
		newData = addDataToFile (newData, newField, 0, newstartPositionName);
		newData = updatePositions (newData, index + uniparam_dataSize, newField.length, 0);
		
		//Add new real configuration				
		newField = getRealConfigBytes();					
		startPositionCfg += uniparam_dataSize;
		startPositionCfg += newFileName.length();
		newData = addDataToFile (newData, newField, 0, startPositionCfg);
		newData = updatePositions (newData, index + uniparam_dataSize, sizeREAL, 1);
		
		return readUniParam(fileService.getJsonFromBytes(newData));
	}
	
	/**
	 * update positions
	 * @param srcData
	 * @param index
	 * @param diff
	 * @param type: 0:names, 1:configuration, 2:generalAdd
	 * @return
	 */
	private byte[] updatePositions (byte[] srcData, int index, int diff, int type){
		//get num of elements
		byte[] numOfElementsByte = Arrays.copyOfRange(srcData, uniparam_numElementsOffset, uniparam_numElementsOffset + uniparam_numElementsSize);
		int numOfElements = fileService.bit32ToInt(numOfElementsByte);
		
		//update positions
		int fromIdx = index/uniparam_dataSize;
		int offset = uniparam_offsetIni;
		for(int i=0; i<numOfElements; i++){
			byte[] dataUniParam = Arrays.copyOfRange(srcData, offset, offset + uniparam_dataSize);
			//update names
			if((i>fromIdx && type==0) || type==2){
				byte[] posNameReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posName_offset, uniparam_part_posName_offset + uniparam_part_posName_size);
				int posNameRegInt = fileService.bit32ToInt(posNameReg);
				posNameRegInt += diff;			
				System.arraycopy(fileService.IntToBit32(posNameRegInt), 0, srcData, offset + uniparam_part_posName_offset, uniparam_part_posName_size);
			}
			//update configurations
			if((i>fromIdx && type==1) || type==0 || type==2){
				byte[] posConfigReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posConfig_offset, uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
				int posConfigRegInt = fileService.bit32ToInt(posConfigReg);
				posConfigRegInt += diff;
				System.arraycopy(fileService.IntToBit32(posConfigRegInt), 0, srcData, offset + uniparam_part_posConfig_offset, uniparam_part_posConfig_size);
			}
			
			offset += uniparam_dataSize;
		}
		return srcData;
	}	
	
	/**
	 * Add bytes to file
	 * @param srcData
	 * @param newField
	 * @param oldLength
	 * @param startPositionData
	 * @return
	 */
	private byte[] addDataToFile (byte[] srcData, byte[] newField, int oldLength, int startPositionData){
		byte[] newData = new byte[srcData.length + newField.length];
		int offset = 0;
		//FirstPart          
	    byte[] tempBuffer = Arrays.copyOfRange(srcData, 0, startPositionData);
	    System.arraycopy(tempBuffer, 0, newData,offset,tempBuffer.length);
	    offset += tempBuffer.length;
	    //NewField	    
	    System.arraycopy(newField, 0, newData,offset,newField.length);
	    offset += newField.length;
	    //LastPart
	    tempBuffer = Arrays.copyOfRange(srcData, startPositionData + oldLength, srcData.length);
	    System.arraycopy(tempBuffer, 0, newData,offset,tempBuffer.length);
	    
	    return newData;
	}
	
	private byte[] getRealConfigBytes(){
		byte[] newField = new byte[sizeREAL];
		for(int i = 0;i<sizeREAL;i++)
			newField[i] = 0;
		return newField;
	}
	
	/**
	 * get configuration kit data
	 */
	public String getConfigData(ModifyUniParam modifyUniParam) throws Exception
	{
		byte[] srcData = fileService.getDataBytes(modifyUniParam.getSrcData());
		int index = modifyUniParam.getIndexName();
		kitConfiguration kitConfiguration;
		
		//valid name	
		byte[] posName = Arrays.copyOfRange(srcData, index + uniparam_part_posName_offset, index + uniparam_part_posName_offset + uniparam_part_posName_size);
		int startPositionName = fileService.bit32ToInt(posName);
		int endPositionName = fileService.indexOfByteArray(srcData, zero, startPositionName);
		byte[] filenameBytes = Arrays.copyOfRange(srcData, startPositionName, endPositionName);
		String filename = fileService.bytesToString(filenameBytes);
		int offset = index;
		if(filename.indexOf(REAL)>0){
			byte[] dataUniParam = Arrays.copyOfRange(srcData, offset, offset + uniparam_dataSize);
			byte[] posConfigReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posConfig_offset, uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
			int posConfigRegInt = fileService.bit32ToInt(posConfigReg);
			
		    byte[] kitConfigBytes = Arrays.copyOfRange(srcData, posConfigRegInt, posConfigRegInt + sizeREAL);
		    
		    String filenameKit, filenameBack, filenameChest, filenameLeg, filenameName;
		    int nameOffset = kit_mainName_offset;
		    byte[] filenames = Arrays.copyOfRange(kitConfigBytes, nameOffset, nameOffset + kit_mainName_size);
		    filenameKit = fileService.bytesToString(filenames);
		    nameOffset += kit_mainName_size;
		    filenames = Arrays.copyOfRange(kitConfigBytes, nameOffset, nameOffset + kit_mainName_size);
		    filenameBack = fileService.bytesToString(filenames);
		    nameOffset += kit_mainName_size;
		    filenames = Arrays.copyOfRange(kitConfigBytes, nameOffset, nameOffset + kit_mainName_size);
		    filenameChest = fileService.bytesToString(filenames);
		    nameOffset += kit_mainName_size;
		    filenames = Arrays.copyOfRange(kitConfigBytes, nameOffset, nameOffset + kit_mainName_size);
		    filenameLeg = fileService.bytesToString(filenames);
		    nameOffset += kit_mainName_size;
		    filenames = Arrays.copyOfRange(kitConfigBytes, nameOffset, nameOffset + kit_mainName_size);
		    filenameName = fileService.bytesToString(filenames);		    
		    
		    kitConfiguration = new kitConfiguration(filenameKit, filenameBack, filenameChest, filenameLeg, filenameName);
		    
		}else				
			kitConfiguration = new kitConfiguration("DEF_NAME", "", "", "", "");
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(kitConfiguration);
		return jsonInString;		
	}
	
}
