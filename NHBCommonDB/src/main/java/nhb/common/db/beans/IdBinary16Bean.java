package nhb.common.db.beans;

import nhb.common.annotations.Transparent;

public abstract class IdBinary16Bean extends AbstractBean {

	public static final String ID = "id";

	private static final long serialVersionUID = -7725200534909763884L;

	private byte[] id;

	public IdBinary16Bean() {
	}

	public IdBinary16Bean(byte[] id) {
		this.setId(id);
	}

	@Transparent
	public byte[] getId() {
		return id;
	}

	public void setId(byte[] id) {
		this.id = id;
	}
}
