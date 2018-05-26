package com.nhb.common.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DisruptorWorkerPoolConfig {

	private int ringBufferSize = 1024;
	private String threadNamePattern = "worker-%d";

	private int unmarshallerSize = 1;
	private int poolSize = 1;
	private int marshallerSize = 1;
}
