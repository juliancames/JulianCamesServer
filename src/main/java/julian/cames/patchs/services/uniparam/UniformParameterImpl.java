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
	@Value("${uniparam.part.part1.offset}") private int uniparam_part_part1_offset;
	@Value("${uniparam.part.part1.size}") private int uniparam_part_part1_size;
	@Value("${kit.mainName.offset}") private int kit_mainName_offset;
	@Value("${kit.mainName.size}") private int kit_mainName_size;
	
	private byte zero = 0x00;
	private final String REAL = "_realUni";
	private final int LENGTH_EXT_FILE = 4;
	
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
				
			//name			
			int startPositionName = fileService.bit32ToInt(posNameReg);
			int endPositionName = fileService.indexOfByteArray(dataBytes, zero, startPositionName);
			byte[] filenameBytes = Arrays.copyOfRange(dataBytes, startPositionName, endPositionName);
			String filename = fileService.bytesToString(filenameBytes);
			
			//ConfigUni
			int sizeConfig = 96;
			if(filename.indexOf(REAL)>0)
				sizeConfig = 128;
			int startPositionConfig = fileService.bit32ToInt(posConfigReg);
			byte[] configBytes = Arrays.copyOfRange(dataBytes, startPositionConfig, startPositionConfig + sizeConfig);
			
			positions.add(new PositionUni(	startPositionConfig,
											startPositionName,
											fileService.bytesToHexString(posConfigReg),
											fileService.bytesToHexString(posNameReg),
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
		
		//update name
		int oldLength = filename.length();		
		String newFileName = filename.substring(0, oldLength - LENGTH_EXT_FILE) + REAL + filename.substring(oldLength - LENGTH_EXT_FILE, oldLength);
		int newLength = newFileName.length();
		int diff = newLength - oldLength;		
		byte[] newField = fileService.StringToBytes(newFileName);
		byte[] newData = addDataToFile (srcData, index, diff, newField, oldLength, startPositionName, 0);
		
		//update config
		byte[] posConfig = Arrays.copyOfRange(srcData, index + uniparam_part_posConfig_offset, index + uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
		int startPositionCfg = fileService.bit32ToInt(posConfig);
		int sizeDEF = 96;
		int sizeREAL = 128;
		newField = new byte[128];
			for(int i = 0;i<128;i++)
				newField[i] = 0;
		newData = addDataToFile (newData, index, sizeREAL - sizeDEF, newField, sizeDEF, startPositionCfg, 1);
		
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
		
		//get current number of elements
		byte[] numOfElementsByte = Arrays.copyOfRange(srcData, uniparam_numElementsOffset, uniparam_numElementsOffset + uniparam_numElementsSize);
		int numOfElements = fileService.bit32ToInt(numOfElementsByte);
		//set new number of elements
		numOfElements++;
		System.arraycopy(fileService.IntToBit32(numOfElements), 0, srcData, uniparam_numElementsOffset, uniparam_numElementsSize);
		
		//Add position register
		byte[] newDataUniParam = Arrays.copyOfRange(srcData, index, index + uniparam_dataSize);
		byte[] newData = new byte[srcData.length + uniparam_dataSize];
		int offset = 0;
		//FirstPart          
	    byte[] tempBuffer = Arrays.copyOfRange(srcData, 0, index + uniparam_dataSize);
	    System.arraycopy(tempBuffer, 0, newData,offset,tempBuffer.length);
	    offset += tempBuffer.length;
	    //NewPosReg	    
	    System.arraycopy(newDataUniParam, 0, newData,offset,newDataUniParam.length);
	    offset += newDataUniParam.length;
	    //LastPart
	    tempBuffer = Arrays.copyOfRange(srcData, index + uniparam_dataSize, srcData.length);
	    System.arraycopy(tempBuffer, 0, newData,offset,tempBuffer.length);
		
	    //create real filename
		int oldLength = filename.length();
		String newFileName = filename.substring(0, oldLength - LENGTH_EXT_FILE) + REAL + filename.substring(oldLength - LENGTH_EXT_FILE, oldLength);
		
		byte[] newField = fileService.StringToBytes(newFileName);
		newData = addDataToFile (newData, index, newFileName.length(), newField, 0, startPositionName + oldLength, 0);
		
		return readUniParam(fileService.getJsonFromBytes(newData));
	}
	
	/**
	 * Add bytes to file
	 * @param srcData
	 * @param index
	 * @param diff
	 * @param newField
	 * @param oldLength
	 * @param startPositionData
	 * @param type: 0:names, 1:configuration
	 * @return
	 */
	private byte[] addDataToFile (byte[] srcData, int index, int diff, byte[] newField, int oldLength, int startPositionData, int type){
		//get num of elements
		byte[] numOfElementsByte = Arrays.copyOfRange(srcData, uniparam_numElementsOffset, uniparam_numElementsOffset + uniparam_numElementsSize);
		int numOfElements = fileService.bit32ToInt(numOfElementsByte);
		
		//update positions
		int fromIdx = index/12;
		int offset = uniparam_offsetIni;
		for(int i=0; i<numOfElements; i++){
			byte[] dataUniParam = Arrays.copyOfRange(srcData, offset, offset + uniparam_dataSize);
			//update names
			if(i>fromIdx && type==0){
				byte[] posNameReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posName_offset, uniparam_part_posName_offset + uniparam_part_posName_size);
				int posNameRegInt = fileService.bit32ToInt(posNameReg);
				posNameRegInt += diff;			
				System.arraycopy(fileService.IntToBit32(posNameRegInt), 0, srcData, offset + uniparam_part_posName_offset, uniparam_part_posName_size);
			}
			//update configurations
			if((i>fromIdx && type==1) || type==0){
				byte[] posConfigReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posConfig_offset, uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
				int posConfigRegInt = fileService.bit32ToInt(posConfigReg);
				posConfigRegInt += diff;
				System.arraycopy(fileService.IntToBit32(posConfigRegInt), 0, srcData, offset + uniparam_part_posConfig_offset, uniparam_part_posConfig_size);
			}
			
			offset += uniparam_dataSize;
		}
		
		byte[] newData = new byte[srcData.length + diff];
		offset = 0;
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
			
		    byte[] kitConfigBytes = Arrays.copyOfRange(srcData, posConfigRegInt, posConfigRegInt + 128);
		    
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
