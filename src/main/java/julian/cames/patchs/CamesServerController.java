package julian.cames.patchs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import julian.cames.patchs.services.files.FileService;
import julian.cames.patchs.services.uniparam.UniformParameter;
import julian.cames.patchs.services.uniparam.dto.AddUniParam;
import julian.cames.patchs.services.uniparam.dto.ImportedData;
import julian.cames.patchs.services.uniparam.dto.ModifyUniParam;

@RestController
public class CamesServerController {

	@Autowired FileService fileService;
	@Autowired UniformParameter uniformParameter;
 
	@CrossOrigin
    @GetMapping("/checkCamesServer")
    public String checkCamesServer() {
    	return "Julian Cames Server is running...";
    }
	
    @CrossOrigin
    @PostMapping("/getTableUniParam")
    public String getDataUni(@RequestBody String strBytes) throws Exception{
    	return uniformParameter.readUniParam(strBytes);
    }
    
    @CrossOrigin
    @PostMapping("/modifyRegUniParam")
    public String modifyRegUniParam(@RequestBody ModifyUniParam modifyUniParam) throws Exception{
    	return uniformParameter.modifyRegUniParamToReal(modifyUniParam);
    }
    
    @CrossOrigin
    @PostMapping("/addRegUniParam")
    public String AddRegUniParam(@RequestBody AddUniParam addUniParam) throws Exception{
    	return uniformParameter.addKit(addUniParam);
    }
    
    @CrossOrigin
    @PostMapping("/getConfigData")
    public String getConfigData(@RequestBody ModifyUniParam modifyUniParam) throws Exception{
    	return uniformParameter.getConfigData(modifyUniParam);
    }
    
    @CrossOrigin
    @PostMapping("/loadConfigImport")
    public String getConfigImport(@RequestBody String strBytes) throws Exception{
    	return uniformParameter.loadConfigImport(strBytes);
    }
    
    @CrossOrigin
    @PostMapping("/importCfgUniParam")
    public String importCfgUniParam(@RequestBody ImportedData importedData) throws Exception{
    	return uniformParameter.importCfgUniParam(importedData);
    }
}