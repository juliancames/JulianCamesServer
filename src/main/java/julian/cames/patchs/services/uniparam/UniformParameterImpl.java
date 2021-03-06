package julian.cames.patchs.services.uniparam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import julian.cames.patchs.services.files.FileService;
import julian.cames.patchs.services.uniparam.dto.AddUniParam;
import julian.cames.patchs.services.uniparam.dto.ImportedData;
import julian.cames.patchs.services.uniparam.dto.MergeData;
import julian.cames.patchs.services.uniparam.dto.ModifyUniParam;
import julian.cames.patchs.services.uniparam.dto.PositionUni;
import julian.cames.patchs.services.uniparam.dto.ResponseUniParam;
import julian.cames.patchs.services.uniparam.dto.kitConfiguration;

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
	@Value("${kit.firstColor.offset}") private int kit_firstColor_offset;
	@Value("${kit.color.size}") private int kit_color_size;
	
	private byte zero = 0x00;
	private final String REAL = "_realUni";
	private final int LENGTH_EXT_FILE = 4;
	private final int sizeDEF = 96;
	private final int sizeREAL = 128;
	private final int lengthREAL = 120;
	
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
	
	public String mergeUniParam(MergeData mergeData) throws Exception{
		byte[] srcBytes = fileService.getDataBytes(mergeData.getSrcData());
		byte[] numOfElementsByteSrc = Arrays.copyOfRange(srcBytes, uniparam_numElementsOffset, uniparam_numElementsOffset + uniparam_numElementsSize);
		int numOfElementsSrc = fileService.bit32ToInt(numOfElementsByteSrc);
		String[] srcNames = new String[numOfElementsSrc];
		int offsetSrc = uniparam_offsetIni;
		for(int i=0; i<numOfElementsSrc; i++){
			byte[] dataUniParam = Arrays.copyOfRange(srcBytes, offsetSrc, offsetSrc + uniparam_dataSize);
			byte[] posNameReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posName_offset, uniparam_part_posName_offset + uniparam_part_posName_size);
			//name			
			int startPositionName = fileService.bit32ToInt(posNameReg);
			int endPositionName = fileService.indexOfByteArray(srcBytes, zero, startPositionName);
			byte[] filenameBytes = Arrays.copyOfRange(srcBytes, startPositionName, endPositionName);
			String filename = fileService.bytesToString(filenameBytes);
			srcNames[i] = filename;
			
			offsetSrc += uniparam_dataSize;
		}
		
		byte[] toMergeBytes = fileService.getDataBytes(mergeData.getToMergeData());
		byte[] numOfElementsByteToMerge = Arrays.copyOfRange(toMergeBytes, uniparam_numElementsOffset, uniparam_numElementsOffset + uniparam_numElementsSize);
		int numOfElementsToMerge = fileService.bit32ToInt(numOfElementsByteToMerge);
		String[] toMergeNames = new String[numOfElementsToMerge];
		byte[][] toMergeConfig = new byte[numOfElementsToMerge][];
		int offsetToMerge = uniparam_offsetIni;
		for(int i=0; i<numOfElementsToMerge; i++){
			byte[] dataUniParam = Arrays.copyOfRange(toMergeBytes, offsetToMerge, offsetToMerge + uniparam_dataSize);
			byte[] posNameReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posName_offset, uniparam_part_posName_offset + uniparam_part_posName_size);
			byte[] posConfigReg = Arrays.copyOfRange(dataUniParam, uniparam_part_posConfig_offset, uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
			//name			
			int startPositionName = fileService.bit32ToInt(posNameReg);
			int endPositionName = fileService.indexOfByteArray(toMergeBytes, zero, startPositionName);
			byte[] filenameBytes = Arrays.copyOfRange(toMergeBytes, startPositionName, endPositionName);
			String filename = fileService.bytesToString(filenameBytes);
			toMergeNames[i] = filename;
			//ConfigUni
			int sizeConfig = sizeDEF;
			if(filename.indexOf(REAL)>0)
				sizeConfig = sizeREAL;
			int startPositionConfig = fileService.bit32ToInt(posConfigReg);
			byte[] configBytes = Arrays.copyOfRange(toMergeBytes, startPositionConfig, startPositionConfig + sizeConfig);
			toMergeConfig[i] = configBytes;
			
			offsetToMerge += uniparam_dataSize;
		}
		
		byte[] newData = new byte[srcBytes.length];
		System.arraycopy(srcBytes, 0, newData, 0, srcBytes.length);
		int offsetToInsert = uniparam_offsetIni;
		int countNew = 0;
		for(int i=0; i<numOfElementsToMerge; i++){
			String current = toMergeNames[i];
			int posIdx = Arrays.asList(srcNames).indexOf(current);
			if(posIdx == -1) {
				newData = addKit(newData, offsetToInsert, current, toMergeConfig[i]);
				countNew++;
				offsetToInsert += uniparam_dataSize;
			}else {
				offsetToInsert = uniparam_offsetIni + (posIdx * uniparam_dataSize) + (countNew * uniparam_dataSize);
			}
		}
		
	    return readUniParam(fileService.getJsonFromBytes(newData));
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
		newField = getRealConfigBytes(newFileName);			
		
		newData = updatePositions (newData, index, sizeREAL - sizeDEF, 1);
		newData = addDataToFile (newData, newField, sizeDEF, startPositionCfg);
		
		return readUniParam(fileService.getJsonFromBytes(newData));
	}
	
	public String addKit(AddUniParam addUniParam)throws Exception{
		byte[] srcData = fileService.getDataBytes(addUniParam.getSrcData());
		int index = addUniParam.getIndexName();
		String addNumKit = addUniParam.getNumKit();
		
		//current (parent) name	
		byte[] posName = Arrays.copyOfRange(srcData, index + uniparam_part_posName_offset, index + uniparam_part_posName_offset + uniparam_part_posName_size);
		int startPositionName = fileService.bit32ToInt(posName);
		int endPositionName = fileService.indexOfByteArray(srcData, zero, startPositionName);
		byte[] filenameBytes = Arrays.copyOfRange(srcData, startPositionName, endPositionName);
		String filename = fileService.bytesToString(filenameBytes);		
		//set new filename
	    String numKit = filename.split("_")[0];
	    String newFileName = String.format("%s_DEF_%s_realUni.bin", numKit, addNumKit);
	    
	    byte[] newData = addKit(srcData, index, newFileName, getRealConfigBytes(newFileName));
	    return readUniParam(fileService.getJsonFromBytes(newData));
	}
	
	//public String addKit(AddUniParam addUniParam)throws Exception
	private byte[] addKit(byte[] srcData, int index, String newFileName, byte[] newConfig)throws Exception
	{		
		//name	
		byte[] posName = Arrays.copyOfRange(srcData, index + uniparam_part_posName_offset, index + uniparam_part_posName_offset + uniparam_part_posName_size);
		int startPositionName = fileService.bit32ToInt(posName);
		int endPositionName = fileService.indexOfByteArray(srcData, zero, startPositionName);
		byte[] filenameBytes = Arrays.copyOfRange(srcData, startPositionName, endPositionName);
		String filename = fileService.bytesToString(filenameBytes);
		
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
		
		if(filename.indexOf(REAL)>0)
			startPositionCfg += sizeREAL;
		else
			startPositionCfg += sizeDEF;
		
		System.arraycopy(fileService.IntToBit32( startPositionCfg ), 0, newDataUniParam, uniparam_part_posConfig_offset, uniparam_part_posConfig_size);		
		byte[] lengthConfig = fileService.IntToBit32(lengthREAL);
		System.arraycopy(lengthConfig, 0, newDataUniParam, uniparam_part_lengthConfig_offset, uniparam_part_lengthConfig_size);
				
		byte[] newData = addDataToFile (srcData, newDataUniParam, 0, index + uniparam_dataSize);	
	    newData = updatePositions (newData, 0, uniparam_dataSize, 2);
	    
	    //Add real filename	
		byte[] newField = new byte[newFileName.length() + 1];
		System.arraycopy(fileService.StringToBytes(newFileName), 0, newField, 0,newFileName.length());	
		newstartPositionName += uniparam_dataSize;
		newData = addDataToFile (newData, newField, 0, newstartPositionName);
		newData = updatePositions (newData, index + uniparam_dataSize, newField.length, 0);
		
		//Add new real configuration	
		newField = newConfig;
		startPositionCfg += uniparam_dataSize;
		startPositionCfg += newFileName.length() + 1;
		newData = addDataToFile (newData, newField, 0, startPositionCfg);
		newData = updatePositions (newData, index + uniparam_dataSize, sizeREAL, 1);
		
		return newData;
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
	
	private byte[] getRealConfigBytes(String fileName){
		String numKit = fileName.split("_")[0];
		while(numKit.length() < 4) numKit = "0"+numKit;		
		String typeKit = "p1";
		if(fileName.indexOf("2nd") > -1) typeKit = "p2"; else
			if(fileName.indexOf("3rd") > -1) typeKit = "p3"; else
				if(fileName.indexOf("4th") > -1) typeKit = "p4"; else
					if(fileName.indexOf("GK1st") > -1) typeKit = "g1";		
		String kitName = String.format("u%s%s", numKit, typeKit);
		
		byte[] initCgf = new byte[] {
				(byte)0x01, (byte)0x90 , (byte)0x3E, (byte)0x01,
				(byte)0xFF, (byte)0xFF , (byte)0xFF, (byte)0xFF , (byte)0xFF, (byte)0xFF , (byte)0xFF, (byte)0xFF , 
				(byte)0xFF, (byte)0xFF , (byte)0xFF, (byte)0xFF , (byte)0xFF, (byte)0xFF , (byte)0xFF, (byte)0x09 ,
				(byte)0x02, (byte)0x02 , (byte)0x07, (byte)0x32 , (byte)0x12, (byte)0x13 , (byte)0x75, (byte)0x0B , 
				(byte)0x40, (byte)0x18 , (byte)0x11, (byte)0x75 , (byte)0xD4, (byte)0x51 , (byte)0x47, (byte)0x01 , 
				(byte)0x40, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , 
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 ,
				(byte)0x00, (byte)0x00 , (byte)0x00, (byte)0x00 };
		
		int offset = kit_mainName_offset;
		System.arraycopy(fileService.StringToBytes(kitName), 0, initCgf, offset, kitName.length());
		offset += kit_mainName_size;
		kitName = kitName+"_back";
		System.arraycopy(fileService.StringToBytes(kitName), 0, initCgf, offset, kitName.length());
		return initCgf;	
	}
	
	public String loadConfigImport(String data) throws Exception{		
		byte[] dataBytes = fileService.getDataBytes(data);		
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(fileService.getJsonFromBytes(dataBytes));
		return jsonInString;
	}
	
	/**
	 * set import data
	 */
	public String importCfgUniParam(ImportedData importedData)throws Exception{
		byte[] srcData = fileService.getDataBytes(importedData.getSrcData());
		int index = importedData.getIndexName();
		byte[] importData = fileService.getDataBytes(importedData.getNewData());
		
		//name	
		byte[] posName = Arrays.copyOfRange(srcData, index + uniparam_part_posName_offset, index + uniparam_part_posName_offset + uniparam_part_posName_size);
		int startPositionName = fileService.bit32ToInt(posName);
		int endPositionName = fileService.indexOfByteArray(srcData, zero, startPositionName);
		byte[] filenameBytes = Arrays.copyOfRange(srcData, startPositionName, endPositionName);
		String filename = fileService.bytesToString(filenameBytes);
		
		byte[] posConfig = Arrays.copyOfRange(srcData, index + uniparam_part_posConfig_offset, index + uniparam_part_posConfig_offset + uniparam_part_posConfig_size);
		int startPositionCfg = fileService.bit32ToInt(posConfig);		
		int cfgLength = 0;
		if(filename.indexOf(REAL)>0)
			cfgLength = lengthREAL;
		else
			cfgLength = sizeDEF;
		
		System.arraycopy(importData, 0, srcData, startPositionCfg, cfgLength);
		
		return readUniParam(fileService.getJsonFromBytes(srcData));
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
		    
		    String[] filenames  = new String[5];
		    int nameOffset = kit_mainName_offset;
		    for(int i = 0; i<5; i++)
		    {
		    	byte[] currentFilename = Arrays.copyOfRange(kitConfigBytes, nameOffset, nameOffset + kit_mainName_size);
		    	filenames[i] = fileService.bytesToString(currentFilename);
		    	nameOffset += kit_mainName_size;
		    }		    
		    
		    String[] colors = new String[5];
		    int colorsOffset = kit_firstColor_offset;
		    for(int i = 0; i<5; i++)
		    {
			    byte[] color = Arrays.copyOfRange(kitConfigBytes, colorsOffset, colorsOffset + kit_color_size);
			    colors[i] = String.format("#%s", fileService.bytesToHexString(color));
			    colorsOffset += kit_color_size;
		    }
		    
		    kitConfiguration = new kitConfiguration(filenames, colors);
		    
		}else				
			kitConfiguration = new kitConfiguration(null, null);
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(kitConfiguration);
		return jsonInString;		
	}
	
}
