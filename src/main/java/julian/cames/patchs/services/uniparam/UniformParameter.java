package julian.cames.patchs.services.uniparam;


public interface UniformParameter {
	public String readUniParam(String data) throws Exception;
	public String modifyRegUniParamToReal(ModifyUniParam modifyUniParam) throws Exception;
	public String addKit(AddUniParam addUniParam)throws Exception;
	public String getConfigData(ModifyUniParam modifyUniParam) throws Exception;
}
