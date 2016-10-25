package nhb.common.workflow.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nhb.common.workflow.concurrent.WorkProcessorGroup;
import nhb.common.workflow.holder.WorkProcessorGroupHolder;

public class BaseWorkProcessorGroupHolder implements WorkProcessorGroupHolder {

	private final Map<String, WorkProcessorGroup> groups = new ConcurrentHashMap<>();

	@Override
	public WorkProcessorGroup getWorkProcessorGroup(String groupName) {
		return this.groups.get(groupName);
	}

	@Override
	public void addWorkProcessorGroup(WorkProcessorGroup... workProcessorGroups) {
		if (workProcessorGroups != null && workProcessorGroups.length > 0) {
			for (WorkProcessorGroup workProcessorGroup : workProcessorGroups) {
				if (workProcessorGroup != null) {
					this.groups.put(workProcessorGroup.getName(), workProcessorGroup);
				}
			}
		}
	}

	@Override
	public WorkProcessorGroup removeWorkProcessorGroup(String groupName) {
		return this.groups.remove(groupName);
	}

}
