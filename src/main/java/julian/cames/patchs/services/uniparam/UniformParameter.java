package julian.cames.patchs.services.uniparam;

import julian.cames.patchs.services.uniparam.dto.AddUniParam;
import julian.cames.patchs.services.uniparam.dto.ImportedData;
import julian.cames.patchs.services.uniparam.dto.ModifyUniParam;

public interface UniformParameter {
	public String readUniParam(String data) throws Exception;
	public String modifyRegUniParamToReal(ModifyUniParam modifyUniParam) throws Exception;
	public String addKit(AddUniParam addUniParam)throws Exception;
	public String getConfigData(ModifyUniParam modifyUniParam) throws Exception;
	public String loadConfigImport(String data) throws Exception;
	public String importCfgUniParam(ImportedData importedData)throws Exception;
}
