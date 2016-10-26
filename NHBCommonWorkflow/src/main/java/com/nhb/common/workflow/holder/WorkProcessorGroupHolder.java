package com.nhb.common.workflow.holder;

import com.nhb.common.workflow.concurrent.WorkProcessorGroup;

public interface WorkProcessorGroupHolder {

	void addWorkProcessorGroup(WorkProcessorGroup... workProcessorGroups);

	WorkProcessorGroup getWorkProcessorGroup(String groupName);

	WorkProcessorGroup removeWorkProcessorGroup(String groupName);
}
