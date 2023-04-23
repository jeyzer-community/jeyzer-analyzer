package org.jeyzer.analyzer.output.graph.node.function;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */




import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphRendering;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphRendering.GENERATION_MODE;
import org.jeyzer.analyzer.data.stack.ThreadStackCPUInfo;
import org.jeyzer.analyzer.output.graph.motion.GraphContext;

public class RootNode extends BaseNode{

	protected static final String NODE_UI_CLASS_EXECUTOR = "Executor";
	protected static final String NODE_UI_CLASS_EXECUTOR_CPU = "ExecutorCPUOverload";
	
	private boolean cpuOverload;
	
	public RootNode(final GraphContext graphCtx, String id, String name){
		super(graphCtx, id, name); // id is unique
		node.addAttribute(NODE_UI_CLASS, getDisplayType()); // force initial root display
	}
	
	@Override
	public void growOlder(){
		if (GENERATION_MODE.ACTION_MERGED.equals(
				((ConfigFunctionGraphRendering)this.graphCtx.getDynamicGraphCfg()).getGenerationMode())
				)
			this.activity = 0; // otherwise thread counter sprite will constantly growing
		super.growOlder();
	}

	@Override
	protected String getDisplayType() {
		if (cpuOverload)
			return NODE_UI_CLASS_EXECUTOR_CPU;
		else
			return NODE_UI_CLASS_EXECUTOR;
	}
	
	@Override
	protected int getGraphNodeStartSize() {
		return 40;
	}

	@Override
	protected void updateSprite() {
		if (GENERATION_MODE.ACTION_MERGED.equals(
				((ConfigFunctionGraphRendering)this.graphCtx.getDynamicGraphCfg()).getGenerationMode())
				)
			plotSpriteValue(this.activity);
	}
	
	@Override
	protected boolean isNodeValueDisplayable(int value) {
		return this.activity >= 2;
	}

	public void updateCPU(ThreadStackCPUInfo cpuInfo) {
		cpuOverload = (cpuInfo != null) ? cpuInfo.getCpuUsage() > ((ConfigFunctionGraphRendering)this.graphCtx.getDynamicGraphCfg()).getExecutorCpuLoadThreshold() : false;
	}
	
}
