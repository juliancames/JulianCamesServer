package julian.cames.patchs.services.uniparam.dto;

import java.util.List;

public class ResponseUniParam {
	List<PositionUni> positions;
	String bytes;
	public ResponseUniParam(List<PositionUni> positions, String bytes) {
		super();
		this.positions = positions;
		this.bytes = bytes;
	}
	public List<PositionUni> getPositions() {
		return positions;
	}
	public void setPositions(List<PositionUni> positions) {
		this.positions = positions;
	}
	public String getBytes() {
		return bytes;
	}
	public void setBytes(String bytes) {
		this.bytes = bytes;
	}
}
