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




import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.output.graph.motion.GraphContext;

public class FunctionNode extends BaseNode {

	public FunctionNode(final GraphContext graphCtx, String id, String name) {
		super(graphCtx, id, name); // id is unique
	}

	@Override
	protected String getDisplayType() {
		return FunctionTag.DISPLAY_NAME;
	}

	@Override
	protected int getGraphNodeStartSize() {
		return 5;
	}

	@Override
	protected void updateSprite() {
		plotSpriteValue(this.activity * this.graphCtx.getThreadDumpPeriod());
	}

}
