package org.jeyzer.analyzer.output.graph.motion;

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







import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.RendererType;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.jeyzer.analyzer.config.graph.ConfigGraphExtend;
import org.jeyzer.analyzer.config.graph.ConfigGraphPlayer;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.output.graph.node.GraphNode;
import org.jeyzer.analyzer.output.template.TemplateEngine;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.SystemHelper;

public abstract class GraphPlayer {
	
	protected static final String NO_ACTIVITY_SPRITE_ID = "Graph-@@-NoActivity";  // should be unique name
	protected static final String ELEMENT_UI_LABEL_NO_ACTIVITY = "No activity detected";	
	protected static final String NODE_UI_CLASS_NO_ACTIVITY = "NoActivity";

	protected static final String NO_THREAD_DUMP_SPRITE_ID = "Graph-@@-NoThreadDump";  // should be unique name
	protected static final String ELEMENT_UI_LABEL_NO_THREAD_DUMP = "No thread dump found";
	protected static final String ELEMENT_UI_LABEL_NO_THREAD_DUMP_WITH_MSG = ELEMENT_UI_LABEL_NO_THREAD_DUMP + " - ";
	protected static final String NODE_UI_CLASS_NO_THREAD_DUMP = "NoThreadDump";
	
	protected static final String RESTART_SPRITE_ID = "Graph-@@-Restart";  // should be unique name
	protected static final String NODE_UI_CLASS_RESTART = "Restart";
	
	protected static final String MESSAGE_SPRITE_ID = "Graph-@@-NoMessage";  // should be unique name
	protected static final String NODE_UI_CLASS_MESSAGE = "Message";
	
	protected static final String GRAPH_SNAPSHOT_FILE_KEY = "graph_snapshot_file";
	
	static {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		if (SystemHelper.isWindows())
			System.setProperty("sun.java2d.directx", "True ");
		else
			System.setProperty("sun.java2d.opengl", "True ");
	}

	protected ConfigGraphPlayer playerCfg;
	protected GraphContext graphCtx;
	protected String target;
	protected boolean noThreadDump = false;
	protected boolean restart = false;

	public GraphPlayer(ConfigGraphPlayer playerCfg, String target, int period) throws JzrInitializationException{
		this.playerCfg = playerCfg;
		Graph graph = createGraph(target, playerCfg.getConfigRendering().getStyleSheetUrl());
		SpriteManager sman = new SpriteManager(graph);
		this.graphCtx = new GraphContext(
				graph, 
				sman, 
				period, 
				playerCfg.getConfigRendering(), 
				playerCfg.getConfigViewer().isEnabled());
		this.target = target;
	}
	
	public abstract String getName();
	
	public abstract void play(ThreadDump dump);
	
	public GraphSnapshot snapshot(JzrMonitorSession session){
		return snapshot(session, true);
	}
	
	public GraphSnapshot snapshot(JzrMonitorSession session, boolean generateHtml){
		try {
			SystemHelper.createDirectory(this.playerCfg.getConfigPicture().getOutputDirectory());
		} catch (JzrException ex) {
			getLogger().error("Failed to create the graph player output directory : " + this.playerCfg.getConfigPicture().getOutputDirectory(), ex);
			return null;
		}
		
		String picturePath;
		// set a max limit, otherwise graph stream loops for very long to layout equally the nodes
		if (graphCtx.getGraph().getNodeCount() > 0 && graphCtx.getGraph().getNodeCount() < 200){
			picturePath = generatePicture(this.playerCfg.getConfigPicture().getPicturePath());
			if (picturePath == null)
				return null;
		}
		else{
			// black image when there is no node or too much nodes : let the template handle it differently
			picturePath = null;
		}
		
		String htmplPath = null;
		if (generateHtml){
			htmplPath = generateHtml(session, picturePath);
			if (htmplPath == null)
				return null;
		}
		
		return new GraphSnapshot(picturePath, htmplPath);
	}
	
	public void displayMessage(String message) {
		Sprite s = graphCtx.getSpriteManager().addSprite(MESSAGE_SPRITE_ID);
		s.addAttribute(GraphNode.NODE_UI_CLASS, NODE_UI_CLASS_MESSAGE);
		s.addAttribute(GraphNode.ELEMENT_UI_LABEL, message);
	}
	
	public void displayRestart(String uiLabel) {
		
		SpriteManager sm = graphCtx.getSpriteManager();
		
		Sprite s = sm.getSprite(RESTART_SPRITE_ID);
		if (s == null){
			s = sm.addSprite(RESTART_SPRITE_ID);
			s.addAttribute(GraphNode.NODE_UI_CLASS, NODE_UI_CLASS_RESTART);
			s.addAttribute(GraphNode.ELEMENT_UI_LABEL, uiLabel);
		}else{
			s.setAttribute(GraphNode.ELEMENT_UI_LABEL, uiLabel);
		}
		
		phantomGraph();
		restart = true;
	}
	
	public void displayNoThreadDump() {
		displayNoThreadDump(null);
	}

	public void displayNoThreadDump(String message) {
		String uiLabel = message != null ? 
				ELEMENT_UI_LABEL_NO_THREAD_DUMP_WITH_MSG + message :
					ELEMENT_UI_LABEL_NO_THREAD_DUMP;
		
		SpriteManager sm = graphCtx.getSpriteManager();
		Sprite s = sm.getSprite(NO_THREAD_DUMP_SPRITE_ID);
		if (s == null){
			s = sm.addSprite(NO_THREAD_DUMP_SPRITE_ID);
			s.addAttribute(GraphNode.NODE_UI_CLASS, NODE_UI_CLASS_NO_THREAD_DUMP);
			s.addAttribute(GraphNode.ELEMENT_UI_LABEL, uiLabel);
		}else{
			s.setAttribute(GraphNode.ELEMENT_UI_LABEL, uiLabel);
		}
		
		phantomGraph();
		noThreadDump = true;
	}
	
	protected String writeHtml(String html) {
		String htmplPath = this.playerCfg.getConfigPicture().getHtmlPath();
		
		try (
				FileWriter fstream = new FileWriter(this.playerCfg.getConfigPicture().getHtmlPath(), false);
				BufferedWriter out = new BufferedWriter(fstream);
			)
		{
			getLogger().info("Generating the graph snapshot html file : " + SystemHelper.sanitizePathSeparators(htmplPath));
			out.write(html);
		} catch (IOException ex) {
			getLogger().error("Failed to write the graph snapshot html file : " + htmplPath, ex);
			return null;
		}
		
		return htmplPath;
	}
	
	protected abstract Logger getLogger();
	
	protected abstract void phantomGraph();
	
	protected void displayNoActivity() {
		SpriteManager sm = graphCtx.getSpriteManager();
		if (sm.getSprite(NO_ACTIVITY_SPRITE_ID) == null){
			Sprite s = sm.addSprite(NO_ACTIVITY_SPRITE_ID);
			s.addAttribute(GraphNode.NODE_UI_CLASS, NODE_UI_CLASS_NO_ACTIVITY);
			s.addAttribute(GraphNode.ELEMENT_UI_LABEL, ELEMENT_UI_LABEL_NO_ACTIVITY);
		}
	}
	
	protected void removeNoThreadDump() {
		SpriteManager sm = graphCtx.getSpriteManager();
		if (sm.getSprite(NO_THREAD_DUMP_SPRITE_ID) != null)
			sm.removeSprite(NO_THREAD_DUMP_SPRITE_ID);
		noThreadDump = false;
	}

	protected void removeRestart() {
		SpriteManager sm = graphCtx.getSpriteManager();
		if (sm.getSprite(RESTART_SPRITE_ID) != null)
			sm.removeSprite(RESTART_SPRITE_ID);
		
		restart = false;
	}
	

	protected void removeNoActivity() {
		SpriteManager sm = graphCtx.getSpriteManager();
		if (sm.getSprite(NO_ACTIVITY_SPRITE_ID) != null)
			sm.removeSprite(NO_ACTIVITY_SPRITE_ID);
	}
	
	private Graph createGraph(String name, String styleSheetPath) throws JzrInitializationException {
		Graph graph = new SingleGraph(name);

		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet", styleSheetPath);
		
		return graph;
	}

	private String generatePicture(String filePath) {
		FileSinkImages fsi = new FileSinkImages(
				OutputType.JPG, 
				new FileSinkImages.CustomResolution(
						this.playerCfg.getConfigRendering().getPictureResolution().getWidth(),
						this.playerCfg.getConfigRendering().getPictureResolution().getHeight()
						)
				);
        fsi.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
        //fsi.setLayoutPolicy(LayoutPolicy.COMPUTED_ONCE_AT_NEW_IMAGE);
        fsi.setStyleSheet(this.playerCfg.getConfigRendering().getStyleSheetUrl());
        fsi.setAutofit(true);
        fsi.setRenderer(RendererType.SCALA);
        fsi.setQuality(Quality.HIGH);
        
        ConfigGraphExtend extend = this.playerCfg.getConfigRendering().getGraphExtend();
        fsi.setFramePercentBottom(extend.getBottom());
        fsi.setFramePercentLeft(extend.getLeft());
        fsi.setFramePercentRight(extend.getRight());
        fsi.setFramePercentTop(extend.getTop());
        
        try {
        	getLogger().info("Generating the graph picture : " + SystemHelper.sanitizePathSeparators(filePath));
			fsi.writeAll(graphCtx.getGraph(), filePath);
		} catch (IOException e) {
			getLogger().error("Failed to write graph picture : " + filePath, e);
			return null;
		}
        
        return filePath;
	}
	
	private String generateHtml(JzrMonitorSession session, String picturePath) {
		TemplateEngine templateEngine = new TemplateEngine(this.playerCfg.getConfigPicture().getTemplateConfiguration());

        // Add data to the context
		templateEngine.addContextEntry(
				TemplateEngine.APPLICATION_ID_KEY, 
				session instanceof JzrSession?((JzrSession)session).getApplicationId():null);
        templateEngine.addContextEntry(
        		TemplateEngine.APPLICATION_TYPE_KEY,
        		session instanceof JzrSession?((JzrSession)session).getApplicationType():null);
        templateEngine.addContextEntry(TemplateEngine.TARGET_KEY, target);
        
        // refresh period must be higher than 30 sec
        int refreshPeriod = session.getThreadDumpPeriod()>=60 ? Integer.valueOf(session.getThreadDumpPeriod()/2) : 30;
        templateEngine.addContextEntry(TemplateEngine.REFRESH_PERIOD_KEY, refreshPeriod);
        
        templateEngine.addContextEntry(TemplateEngine.GENERATION_TIME_KEY, new Date());
        if (picturePath != null)
        	templateEngine.addContextEntry(GRAPH_SNAPSHOT_FILE_KEY, this.playerCfg.getConfigPicture().getPictureFileName());
        else
        	templateEngine.addContextEntry(GRAPH_SNAPSHOT_FILE_KEY, null);
        
        // generate the html
        String html;
        try
        {
        	html = templateEngine.generate();
        }
        catch (Exception ex)
        {
        	getLogger().error("Failed to generate graph snapshot html container : failed to instanciate the Velocity template.", ex);
            return null;
        }
        
        return writeHtml(html);
	}
}
