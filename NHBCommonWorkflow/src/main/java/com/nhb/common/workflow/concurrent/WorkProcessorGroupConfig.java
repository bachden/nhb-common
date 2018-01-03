package com.nhb.common.workflow.concurrent;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class WorkProcessorGroupConfig {

	private int numThreads;
	private int bufferSize;
	private int threadPriority;
	private String threadNamePattern;

}
