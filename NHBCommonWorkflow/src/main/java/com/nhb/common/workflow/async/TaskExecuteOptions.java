package com.nhb.common.workflow.async;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskExecuteOptions {

	private boolean async;

	private String workProcessorGroupName;
}
