package nhb.common.db.beans;

import java.io.Serializable;

import nhb.common.BaseLoggable;
import nhb.common.data.PuObject;

public class AbstractBean extends BaseLoggable implements Serializable {

	private static final long serialVersionUID = -3242520191301712269L;

	@Override
	public String toString() {
		return PuObject.fromObject(this).toJSON();
	}
}
