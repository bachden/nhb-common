package com.nhb.common.workflow.impl;

import com.nhb.common.data.PuObject;

import lombok.Getter;
import lombok.Setter;

public class GenericJobContext extends BasicJobContext {

	@Setter
	@Getter
	private PuObject output;

	@Setter
	@Getter
	private PuObject input;

}
