package org.jeyzer.analyzer.output.graph.node.contention;

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








import org.graphstream.ui.spriteManager.Sprite;
import org.jeyzer.analyzer.data.tag.ContentionTypeTag;
import org.jeyzer.analyzer.output.graph.motion.GraphContext;
import org.jeyzer.analyzer.output.graph.node.GraphNode;

public class ContentionTypeNode extends GraphNode{
	
	public ContentionTypeNode(GraphContext graphCtx, String id, String name, int percent, String hexaHighlight) {
		super(graphCtx, id, name);
		node.addAttribute(NODE_UI_CLASS, getDisplayType());
		node.setAttribute(NODE_UI_SIZE, percent * PIXELS_UNIT);
		if (hexaHighlight != null)
			node.addAttribute(NODE_UI_STYLE, NODE_FILL_COLOR + hexaHighlight + ";");
		plotSpriteValue(percent);
	}

	@Override
	protected String getDisplayType() {
		return ContentionTypeTag.DISPLAY_NAME;
	}

	@Override
	protected int getGraphNodeStartSize() {
		return 5;
	}

	@Override
	public boolean phantom() {
		phantomGraphNode();
		return true;
	}
	
	protected void plotSpriteValue(int value){
		if (isNodeValueDisplayable(value)){
			String spriteName = this.node.getId().replace('.', '-');
			Sprite s = this.graphCtx.getSpriteManager().addSprite(spriteName);
			s.attachToNode(this.node.getId());
			s.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_COUNT);
			s.setAttribute(ELEMENT_UI_LABEL, value);
		}
	}

	public void remove() {
		removeGraphNode();
	}
	
}
