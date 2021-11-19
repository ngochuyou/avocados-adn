/**
 * 
 */
package adn.model.models;

/**
 * @author Ngoc Huy
 *
 */
public class DeliveryInstructions {

	private Integer districtId;

	private String address;

	private String note;

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
