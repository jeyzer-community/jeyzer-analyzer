package org.jeyzer.web;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.analyzer.error.JzrEmptyRecordingException;
import org.jeyzer.analyzer.error.JzrExecutionException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrNoThreadDumpFileFound;
import org.jeyzer.analyzer.error.JzrTranslatorJFRInvalidVersionException;
import org.jeyzer.analyzer.error.JzrTranslatorJFRThreadDumpEventNotFoundException;
import org.jeyzer.analyzer.error.JzrTranslatorLimitViolationException;
import org.jeyzer.analyzer.error.JzrTranslatorMultipleJFRFilesException;
import org.jeyzer.analyzer.error.JzrTranslatorRecordingSnapshotNotFoundException;
import org.jeyzer.analyzer.error.JzrTranslatorZipInvalidFileException;
import org.jeyzer.analyzer.error.JzrTranslatorZipPasswordProtectedException;
import org.jeyzer.analyzer.output.ReportDescriptor;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.status.JeyzerStatusEventDispatcher;
import org.jeyzer.analyzer.util.AnalyzerHelper;
import org.jeyzer.analyzer.util.JFRHelper;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.analyzer.util.TimeZoneInfoHelper;
import org.jeyzer.analyzer.util.ZipHelper;
import org.jeyzer.analyzer.util.ZipParams;
import org.jeyzer.profile.master.MasterProfile;
import org.jeyzer.web.analyzer.JzrAnalysisRequest;
import org.jeyzer.web.analyzer.JzrController;
import org.jeyzer.web.analyzer.JzrDiscoveryItem;
import org.jeyzer.web.analyzer.JzrSetup;
import org.jeyzer.web.analyzer.JzrStatusListener;
import org.jeyzer.web.config.ConfigPortal;
import org.jeyzer.web.config.ConfigWeb;
import org.jeyzer.web.error.JzrWebException;
import org.jeyzer.web.error.JzrWebUploadedRecordingExpiredException;
import org.jeyzer.web.timezone.TimeZoneService;
import org.jeyzer.web.util.FileDownloadWrapper;
import org.jeyzer.web.util.ReCaptcha;
import org.jeyzer.web.util.TempFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.juchar.colorpicker.ColorPickerField;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

@Push
@HtmlImport("html/jeyzer-ui.html")
@StyleSheet("styles/styles.css")
@PageTitle("Jeyzer")
public class JeyzerUI extends Div implements PageConfigurator, RouterLayout {

	private static final long serialVersionUID = 1586619581253829826L;

	private enum NotificationLevel { INFO, WARNING, SUCCESS, ERROR }
	
	private static final Logger logger = LoggerFactory.getLogger(JeyzerUI.class);	
	
	private static final int PASSWORD_MIN_LENGTH = 8;
	private static final int PASSWORD_MAX_LENGTH = 20;
	
	private static final String FUNCTION_KEYWORD_TABLE_COLUMN = "Function Keywords";
	private static final String FUNCTION_COLOR_TABLE_COLUMN = "Report color";
	
	private static final String ERROR_PLEASE_PORTAL_MESSAGE = "<br/>Please submit the below request id in the Jeyzer analysis help <a href='@forum.url@' target='_blank'>forum</a>.";
	private static final String ERROR_PLEASE_STANDARD_MESSAGE = "<br/>Please contact your administrator.";
	
	private static final String JS_CLEAR_FILES = "this.files=[]";
	
    private static final Color[] DEFAULT_COLORS = {
        	new Color(36,182,0), 
        	new Color(65,105,225), 
        	new Color(218,112,214), 
        	new Color(189,183,107),
        	new Color(255,215,0)
        	};
    
    private static final String[] DEFAULT_KEYWORDS = {
    		"keyword1, keyword2",
    		"keyword3",
    		"",
    		"",
    		""
    		};
    
    private int colorIndex = 0;
    private int functionIndex = 0;
    
    // UI screen session - Zip file id loaded in the drop box 
    private transient String zipFileId;
    
    // Optional privacy agreement, used in portal
    private Boolean agreementAccepted = false;
    
	private final transient ProgressBar progressBar;
	private final transient Label progressStatus;
	
	private final transient FormLayout form = new FormLayout();
	private final transient VerticalLayout left = new VerticalLayout();
	private final transient VerticalLayout right = new VerticalLayout();
	private final transient Binder<JzrAnalysisRequest> binder = new Binder<>(JzrAnalysisRequest.class);
	
	// Manual binding with text validator
	private final transient TextField context = new TextField("Report id");
	
	// Manual binding with email validator
	private final transient EmailField email = new EmailField("Your email");
	
	// Manual binding with password validator
	private final transient PasswordField password = new PasswordField("Password");
	
	@PropertyId("profile")
	// Manual binding 
	private final transient ListBox<String> profileList = new ListBox<>();
	
	private final transient Checkbox periodDetection = new Checkbox("Automatic period detection");
	
	// Manual binding
	private final transient Checkbox timeZoneDetection = new Checkbox("Time zone - automatic detection");
	private final transient ComboBox<String> recordingTimeZoneList = new ComboBox<>();	
	private final transient ComboBox<String> reportTimeZoneList = new ComboBox<>();
	private final transient TimeZoneService timeZoneService = new TimeZoneService();
	
	// Manual binding with period validator
	private final transient NumberField period = new NumberField("Period");
	
	// Manual binding 
	private final transient Grid<JzrDiscoveryItem> discoveryTable = new Grid<>();
	
	private List<JzrDiscoveryItem> items = new ArrayList<>(5);
	
	@PropertyId("description")
	private final transient TextArea desc = new TextArea("Description");
	
	// Optional captcha, used in portal (online analyzer)
	private transient ReCaptcha reCaptcha = null;
	
	private transient Upload upload = null;
	
	public JeyzerUI() {
		JzrSetup jzrSetup = JeyzerServlet.getJzrSetup();
		jzrSetup.validate();
		
		ConfigWeb cfg = JeyzerServlet.getConfigWeb();
		MasterProfile profile = jzrSetup.getProfile(jzrSetup.getDefaultProfile());

		// Borders on left and right: make it nice. But resizing to small makes it ugly. Need investigation..
//		HorizontalLayout board = new HorizontalLayout();
//		board.setWidthFull();
//		board.setMargin(false);
//		board.setSpacing(false);
//		
//		VerticalLayout leftBoard = new VerticalLayout();
//		leftBoard.setWidth("10%");
//		leftBoard.setClassName("layout-left-board");
//		
//		VerticalLayout rightBoard = new VerticalLayout();
//		rightBoard.setWidth("10%");
//		rightBoard.setClassName("layout-right-board");
		
		VerticalLayout root = new VerticalLayout();
		root.setMargin(false);
		root.setSpacing(false);
//		root.setWidth("80%");
		root.setWidthFull();
		root.setClassName(
				cfg.getConfigPortal().isTitleDisplayed() ?
						"layout-root" : "layout-root-portal");
		
		/* 
		 * -------------------------------------------------------
		 * Title
		 * -------------------------------------------------------
		 */
				
		if (cfg.getConfigPortal().isTitleDisplayed()) {
			VerticalLayout header = new VerticalLayout();
			
			HorizontalLayout titleBar = new HorizontalLayout();
			titleBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
			
			Image logo = new Image("img/jeyzer-white-logo.png", "");
			logo.setClassName("label-logo");
			titleBar.add(logo);
			
			Label product = new Label("Jeyzer");
			product.setClassName("label-product");
			titleBar.add(product);
			
			header.add(titleBar);
			header.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, titleBar);
			
			Label title = new Label("Web Analyzer");
			title.setClassName("label-title");
			header.add(title);
			header.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, title);
			
			root.add(header);
			root.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, header);
		}

		/*
		 * -------------------------------------------------------
		 * Progress bar
		 * -------------------------------------------------------
		 */
		
		progressStatus = new Label("Not running");
		progressStatus.setClassName("label-progress-status");
		progressStatus.setEnabled(true);
		progressStatus.setVisible(false);
		root.add(progressStatus);
		root.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, progressStatus);
		
		// Progress bar
		progressBar = new ProgressBar();
		progressBar.setVisible(false);
		progressBar.setWidth("750px");
		root.add(progressBar);
		root.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, progressBar);
		
		final JzrStatusListener listener = new JzrStatusListener(progressBar, progressStatus);
		
		HorizontalLayout horizWorkLayout = new HorizontalLayout();
		
		/* 
		 * -------------------------------------------------------
		 * Profile list
		 * -------------------------------------------------------
		 */ 

		HorizontalLayout profileLayout = new HorizontalLayout();

		String hint;
		if (jzrSetup.getProfile(JzrSetup.JZR_DISCOVERY_PROFILE) != null)
			hint = "The applicative master profile. Choose \""
					+ JzrSetup.JZR_DISCOVERY_PROFILE
					+"\" one if profile is unknown (or not yet set up)";
		else 
			hint ="The applicative master profile";	
		
		Label profileFieldName = new Label("Profile");
		profileFieldName.setTitle(hint);
		profileFieldName.setWidth("100px");
		profileFieldName.setClassName("label-profile");
		profileFieldName.setVisible(cfg.getConfigPortal().areProfilesDisplayed());
		
		profileLayout.add(profileFieldName);
		profileLayout.add(profileList);
		profileLayout.setWidth("350px");
		
		profileList.setMaxHeight("300px");
		profileList.setWidth("250px");
		profileList.setItems(jzrSetup.getProfiles());
		
		if (jzrSetup.getProfiles().contains(jzrSetup.getDefaultProfile()))
			profileList.setValue(jzrSetup.getDefaultProfile());
		
		// In portal mode, we may not want to display the list of profiles and use instead a generic one. 
		// This is the case with the online web analyzer.
		// All will rely on the above default profile. Usually the "portal" one.
		profileList.setVisible(cfg.getConfigPortal().areProfilesDisplayed());
		
		left.add(profileLayout);
				
		profileList.addValueChangeListener(event -> processProfileSelection(event));
	
		/* 
		 * -------------------------------------------------------
		 * Email
		 * -------------------------------------------------------
		 */
		
		email.setTitle("Your email address");
		email.setAutocomplete(Autocomplete.ON);
		email.setErrorMessage("Field is required");
		email.setClearButtonVisible(true);
		email.setWidth("350px");
		if (cfg.getDefaultSubmitterEmail() != null && !cfg.getDefaultSubmitterEmail().isEmpty())
			email.setValue(cfg.getDefaultSubmitterEmail());

		binder.forField(email)
				.withValidator(new EmailValidator("Email address is invalid"))
				.bind(JzrAnalysisRequest::getEmail, JzrAnalysisRequest::setEmail);
		left.add(email);
		
		/* 
		 * -------------------------------------------------------
		 * Context
		 * -------------------------------------------------------
		 */ 
		
		context.setTitle("JZR report name. Use for example customer and ticket id"
				+ "\nAutomatically prefixed with the profile id to set the report file name.\n");
		context.setRequired(true);
		context.setErrorMessage("Report name is mandatory");
		context.setMaxLength(50);
		context.setWidth("350px");
		context.setClearButtonVisible(true);
		binder.forField(context)
			.withValidator(string -> string != null && !string.isEmpty(), "Please enter the report name.")
			.bind(JzrAnalysisRequest::getNodeName, JzrAnalysisRequest::setNodeName);
		if (cfg.isDebugMode())	context.setValue("Test");
		left.add(context);

		/* 
		 * -------------------------------------------------------
		 * Password
		 * -------------------------------------------------------
		 */
		password.setVisible(!profile.getSecurity().isPasswordFree());
		password.setMaxLength(PASSWORD_MAX_LENGTH);
		password.setClearButtonVisible(true);
		password.setWidth("350px");
		binder.forField(password)
			.withValidator(pwd -> 
				!profile.getSecurity().isPasswordFree()?
					profile.getSecurity().isPasswordMandatory()? ((String)pwd).length() < PASSWORD_MAX_LENGTH && ((String)pwd).length() > PASSWORD_MIN_LENGTH 
					: !((String)pwd).isEmpty() && ((String)pwd).length() < PASSWORD_MAX_LENGTH && ((String)pwd).length() > PASSWORD_MIN_LENGTH
				:true, 
				"Password must contain at least " + PASSWORD_MIN_LENGTH + " characters")
			.bind("password");
		if (profile.getSecurity().isPasswordMandatory())
			activateMandatoryPassword();
		else
			activateOptionalPassword();
		
		left.add(password);
		
		/* 
		 * -------------------------------------------------------
		 * Time zone detection
		 * -------------------------------------------------------
		 */
		timeZoneDetection.setValue(true);
		timeZoneDetection.addValueChangeListener(event -> {
			Boolean checked = event.getValue();
			if (!checked){
				recordingTimeZoneList.setVisible(true);
				reportTimeZoneList.setVisible(true);
			}
			else{
				recordingTimeZoneList.setVisible(false);
				reportTimeZoneList.setVisible(false);
			}
		});
		timeZoneDetection.setWidth("350px");
		timeZoneDetection.setVisible(cfg.getConfigPortal().isTimeZoneDisplayed());
		left.add(timeZoneDetection);
		
		/* 
		 * -------------------------------------------------------
		 * Time zone
		 * -------------------------------------------------------
		 */
		recordingTimeZoneList.setLabel("Recording time zone");
		recordingTimeZoneList.setWidth("300px");
		recordingTimeZoneList.setClassName("time-zone");
		recordingTimeZoneList.setVisible(false);
		recordingTimeZoneList.setReadOnly(true);
		recordingTimeZoneList.setDataProvider(timeZoneService::fetch, timeZoneService::count);
		recordingTimeZoneList.addValueChangeListener(event -> {
			if (reportTimeZoneList.isEmpty())
				reportTimeZoneList.setValue(event.getValue());
		});
		binder.forField(recordingTimeZoneList).bind(JzrAnalysisRequest::getRecordingTimeZoneId, JzrAnalysisRequest::setRecordingTimeZoneId);
		left.add(recordingTimeZoneList);
		
		reportTimeZoneList.setLabel("Display time zone");
		reportTimeZoneList.setWidth("300px");
		reportTimeZoneList.setClassName("time-zone");
		reportTimeZoneList.setVisible(false);
		reportTimeZoneList.setReadOnly(true);
		reportTimeZoneList.setDataProvider(timeZoneService::fetch, timeZoneService::count);
		reportTimeZoneList.addValueChangeListener(event -> {
			if (recordingTimeZoneList.isEmpty())
				recordingTimeZoneList.setValue(event.getValue());
		});
		binder.forField(reportTimeZoneList).bind(JzrAnalysisRequest::getReportTimeZoneId, JzrAnalysisRequest::setReportTimeZoneId);
		left.add(reportTimeZoneList);
		
		/* 
		 * -------------------------------------------------------
		 * Period detection
		 * -------------------------------------------------------
		 */
		periodDetection.setValue(true);
		periodDetection.setLabel("Recording generation period - automatic detection");
		periodDetection.addValueChangeListener(event -> {
			Boolean checked = event.getValue();
			if (!checked){
				period.setVisible(true);
				if (period.getValue() == -1)
					period.setValue(60d); // otherwise keep last value
			}
			else{
				period.setVisible(false);
				period.setValue(-1d);
			}
		});
		periodDetection.setWidth("350px");
		// In generic portal, period must always get automatically deduced : no need to display the check box. 
		periodDetection.setVisible(cfg.getConfigPortal().isPeriodDisplayed());
		left.add(periodDetection);
		
		/* 
		 * -------------------------------------------------------
		 * Period
		 * -------------------------------------------------------
		 */
		period.setTitle("Recording period");
		period.setValue(-1d);
		period.setSuffixComponent(new Span("sec"));
		period.setWidth("300px");
		period.setClassName("number-period");
		period.setErrorMessage("Period is mandatory");
		period.setVisible(false);
		binder.forField(period)
			.withValidator(value -> period.isVisible() ? 
					   (Double)value >= 1 && (Double)value <= 300 
						: true, 
						"Period must be between 1 and 300 sec")
			.bind("period");
		left.add(period);
		
		/* 
		 * -------------------------------------------------------
		 * Function discovery
		 * ------------------------------------------------------- *
		 */
		if (cfg.isFunctionDiscoveryDisplayEnabled()){
			for (int i=0; i<5; i++) {
				items.add(new JzrDiscoveryItem());
			}
			
			colorIndex = 0;
			discoveryTable.setItems(items);
			discoveryTable.setWidth("350px"); // makes it large
			discoveryTable.setHeight("350px");
			discoveryTable.addComponentColumn(item -> createTextField(item)).setHeader(FUNCTION_KEYWORD_TABLE_COLUMN).setKey(FUNCTION_KEYWORD_TABLE_COLUMN);
			discoveryTable.addComponentColumn(item -> createColorPickerField(item)).setHeader(FUNCTION_COLOR_TABLE_COLUMN).setKey(FUNCTION_COLOR_TABLE_COLUMN);
			discoveryTable.getColumnByKey(FUNCTION_KEYWORD_TABLE_COLUMN).setWidth("65%");
			discoveryTable.getColumnByKey(FUNCTION_COLOR_TABLE_COLUMN).setWidth("35%");
			left.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, discoveryTable); // required. Default does not apply
			discoveryTable.setVisible(profile.isDiscoveryModeEnabled());
			left.add(discoveryTable);
		}
		
		/* 
		 * -------------------------------------------------------
		 * Description
		 * -------------------------------------------------------
		 */ 
		desc.setHeight("200px");
		desc.setLabel("Issue description or benchmark case");
		desc.setRequired(false);
		desc.setMaxLength(3000);
		desc.setWidth("350px"); // makes it large
		if (cfg.getDefaultIssueDescription() != null && !cfg.getDefaultIssueDescription().isEmpty())
			desc.setValue(cfg.getDefaultIssueDescription());
		left.add(desc);
		
		binder.bindInstanceFields(this);
		
		/* 
		 * -------------------------------------------------------
		 * Portal mode : add acceptance checks
		 * -------------------------------------------------------
		 */
		
		if (cfg.getConfigPortal().isAgreementDisplayed()) {
			VerticalLayout acceptLayout = new VerticalLayout();
		    acceptLayout.setClassName("layout-accept");
		    
			/* 
			 * -------------------------------------------------------
			 * Captcha
			 * Source : https://github.com/retomerz/vaadin-flow-recaptcha
			 * Admin console for key generation : https://www.google.com/recaptcha/admin/create
			 * -------------------------------------------------------
			 */ 
			
			if (cfg.getConfigPortal().isCaptchaDisplayed()) {
			    reCaptcha = new ReCaptcha(
			    		cfg.getConfigPortal().getCaptchaWebSiteKey(),
			    		cfg.getConfigPortal().getCaptchaSecretKey()
			    		);
			    acceptLayout.add(reCaptcha);
			    acceptLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, reCaptcha);
			}
			
			/* 
			 * -------------------------------------------------------
			 * Agreement check box
			 * -------------------------------------------------------
			 */
			Checkbox agreementCheckbox = new Checkbox();
			agreementCheckbox.setLabelAsHtml(
			        "Please accept the <a href='"
			        + JeyzerServlet.getConfigWeb().getConfigPortal().getPrivacyPolicyUrl() 
			        + "' target='_blank'>privacy policy & terms of service</a> to upload your recording and generate the related JZR report."
			        );
			
			agreementCheckbox.addValueChangeListener(event -> {
				agreementAccepted = event.getValue();
				if (agreementAccepted)
					if (JeyzerServlet.getConfigWeb().getConfigPortal().isCaptchaDisplayed())
						if (reCaptcha.isValid()) {
							upload.setVisible(true);
							notifyUser("Great. You can now upload your recording on the drop zone.", NotificationLevel.INFO);
						}
						else
							upload.setVisible(false);
					else {
						notifyUser("Great. You can now upload your recording on the drop zone.", NotificationLevel.INFO);
						upload.setVisible(true);
					}
				else
					upload.setVisible(false);
			});
			agreementCheckbox.setClassName("checkbox-agreement");
			
			agreementCheckbox.setWidth("302px"); // like the Captcha width
			acceptLayout.add(agreementCheckbox);
		    acceptLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, agreementCheckbox);
			
		    left.add(acceptLayout);
		}
		
		/* 
		 * -------------------------------------------------------
		 * JZR report generation generateButton
		 * -------------------------------------------------------
		 */ 

		Button generateButton = new Button("  Generate JZR report"); // spaces added to get 1 space with the picture. No other alternative.
		
		// Do not use frontend directory as it doesn't exist in Vaadin production mode :
		Image image = new Image("img/report-green_small.jpg", "");
		image.setClassName("generate-button-image");
		generateButton.setIcon(image);
		generateButton.setIconAfterText(false);
		generateButton.setClassName("generate-button");
		generateButton.setWidth("220px");
		// generateButton.addClickShortcut(Key.ENTER); // disabled : side effect on every carriage return in the issue description field. No way to prevent it
		generateButton.addClickListener(event -> processClickEvent(event, listener));
		
		left.add(generateButton);
		left.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
		
		left.setClassName(
				cfg.getConfigPortal().isTitleDisplayed() ?
						"layout-capture" : "layout-capture-portal");
		form.add(left);
		
		horizWorkLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);
		horizWorkLayout.setWidth("90%");
		horizWorkLayout.add(form);
		horizWorkLayout.setVerticalComponentAlignment(FlexComponent.Alignment.BASELINE, form);
		
		/* 
		 * -------------------------------------------------------
		 * Upload
		 * -------------------------------------------------------
		 */ 
		
		MemoryBuffer buffer = new MemoryBuffer();
		upload = new Upload(buffer);
		upload.setId("upload-tds");
		upload.setWidthFull();
		upload.setHeight("450px");
		if (cfg.getConfigPortal().isAgreementDisplayed() || cfg.getConfigPortal().isCaptchaDisplayed())
			upload.setVisible(false);
		
		Image uploadImg = new Image("./img/report-green-upload.jpg", "");
		uploadImg.setClassName("drop-icon");
		upload.setDropLabelIcon(uploadImg);  // finally hidden through css
		
		// This is super crappy workaround
		String browser = VaadinSession.getCurrent().getBrowser().getBrowserApplication();		
		// Firefox : Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/74.0
		// Edge : Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134
		
		Span dropLabel = new Span();
		if (browser.contains("Edge") || browser.contains("Explorer")) {
			// Edge doesn't display the text correctly. Text area is wider than the upload area. So need to put <BR>. 
			// Also it adds margin for the upload image (which is disabled..)
			dropLabel = new Span();
			dropLabel.add(new Html("<span><BR>Drag and drop your Jeyzer recording <BR>or JFR recording or thread dump zip/tar.gz file here</span>"));			
		}
		else {
			// other browsers
			dropLabel = new Span("Drag and drop your Jeyzer recording or JFR recording or thread dump zip/tar.gz file here");
		}
		
		dropLabel.setClassName("drop-label");
		dropLabel.setSizeFull();
		upload.setDropLabel(dropLabel);

		upload.addSucceededListener(event -> processUploadedFile(event, buffer));
		
		VerticalLayout workDropArea = new VerticalLayout();
		workDropArea.setClassName("drop-area");
		workDropArea.setWidthFull();
		workDropArea.add(upload);
		right.setWidth("410px");
		right.setClassName("layout-right");
		right.add(workDropArea);
		
		horizWorkLayout.add(right);
		horizWorkLayout.setVerticalComponentAlignment(FlexComponent.Alignment.BASELINE, right);
		horizWorkLayout.setClassName("layout-work");
		
		root.add(horizWorkLayout);
		root.setHorizontalComponentAlignment(FlexComponent.Alignment.START, horizWorkLayout);
		
		/* 
		 * -------------------------------------------------------
		 * Bottom notes
		 * -------------------------------------------------------
		 */ 
		
		Anchor jeyzerLink = new Anchor("https://jeyzer.org", cfg.getConfigPortal().isTitleDisplayed() ? "Jeyzer - Vinci Edition" : "Jeyzer - Vinci Online Edition");
		jeyzerLink.setClassName("jeyzer-link");
		root.add(jeyzerLink);
		root.setHorizontalComponentAlignment(FlexComponent.Alignment.END, jeyzerLink);
		
		Label powered = new Label("Powered by Jeyzer/Apache POI/GraphStream/Vaadin");
		powered.setSizeUndefined();
		powered.setClassName("label-powered-note");
		root.add(powered);
		root.setHorizontalComponentAlignment(FlexComponent.Alignment.END, powered);
		
//		board.add(leftBoard);
//		board.add(root);
//		board.add(rightBoard);		
//		this.add(board);
		
		this.add(root);
		this.setWidth("100%");
	}
	
	@Override
	public void configurePage(InitialPageSettings settings) {
	    // Open graph tags
		settings.addMetaTag("og:title", "Jeyzer");
		settings.addMetaTag("og:type", "website");
        settings.addMetaTag("og:url", "https://jeyzer.org/");

        // page icon
        settings.addLink("shortcut icon", "./frontend/icons/favicon.ico");
        settings.addFavIcon("icon", "./frontend/icons/favicon.ico", "300x300");
        
        // remove the bottom scroll bar
        settings.addInlineWithContents("body {width: 98vw; height:100vh;}",
                InitialPageSettings.WrapMode.STYLESHEET);
	 }

	
	private void processClickEvent(ClickEvent<Button> event, JzrStatusListener listener) {
		long startTime = System.currentTimeMillis();
		JzrController tdController = null;
		
		logger.info("JZR report generation request received.");
		
		// in portal mode, validate that the privacy policy has been accepted. Same for captcha if enabled.
		if (!validateAcceptance())
			return;
		
		// validate email and context and possibly password
		JzrAnalysisRequest request = new JzrAnalysisRequest();
		try {
			binder.validate();
			binder.writeBean(request);
			request.setDiscoveryItems(items);
			request.setTempZipFileId(zipFileId);
			fillTimeZoneUserSelections(request);
		} catch (ValidationException e) {
			notifyUser(e.getMessage(), NotificationLevel.WARNING);
			return;
		}
		
		// validate td files
		if (zipFileId==null){
			notifyUser("Recording file not uploaded", NotificationLevel.WARNING);
			return;
		}
		
		@SuppressWarnings("static-access")
		UI ui = this.getUI().get().getCurrent();
		
		// get IP address
		try {
			request.setClientAddresses(getRemoteAddresses(ui.getSession().getService().getCurrentRequest()));
		}
		catch(Exception ex){
			logger.warn("Failed to access client IP addresses.");
		}

		listener.setUI(ui);
		
		try {
			JeyzerStatusEventDispatcher eventDispatcher = new JeyzerStatusEventDispatcher();
			eventDispatcher.registerListener(listener);
			
			tdController = new JzrController(
					request,
					JeyzerServlet.getJzrSetup(),
					eventDispatcher,
					JeyzerServlet.getTdRoorDir(),
					JeyzerServlet.getTempFileManager(),
					JeyzerServlet.getWorkDirManager()
				);
		
			progressBar.setValue(0.0f);
			progressBar.setVisible(true);
			progressStatus.setText("Waiting for analysis slot...");
			progressStatus.setVisible(true);
			ui.setPollInterval(500);
			
			Future<ReportDescriptor> future = JeyzerServlet.getExecutorService().submit(tdController);
			
			ui.getSession().unlock();
			
			ReportDescriptor reportDescriptor = null;
			try{
				reportDescriptor = future.get();
			}catch(ExecutionException ee){
				throw ee.getCause();
			}catch (InterruptedException ie) {
	            Thread.currentThread().interrupt();
	            throw new JzrWebException("JZR report generation failed : processing got interrupted.");
	        }
			
			ui.getSession().lock();
			ui.setPollInterval(-1);
			
			generateLinks(reportDescriptor);
			
			notifyUser("JZR report generated", NotificationLevel.SUCCESS);
			
			logger.info("JZR report generation request processed.");
			long endTime = System.currentTimeMillis();
			logger.info("Generation time : " + getPrintableDuration(endTime - startTime, true));
		} catch (JzrNoThreadDumpFileFound e) {
			logger.info("JZR report generation could not complete.", e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("No valid recording snapshots found in file " + buildOriginalFileName(zipFileId) + "<br/>" + buildFileFormats(e.getSupportedFileFormats()), NotificationLevel.WARNING);
		} catch (JzrEmptyRecordingException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("Recording " + buildOriginalFileName(zipFileId) + " could not be loaded : file is empty.<br/>Please make sure it has been generated properly.", NotificationLevel.WARNING);
		} catch (JzrTranslatorLimitViolationException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR Recording " + buildOriginalFileName(zipFileId) + " could not be loaded.<br/>" + buildExceptionMessage(e), NotificationLevel.WARNING);
		} catch (JzrTranslatorJFRInvalidVersionException | JzrTranslatorJFRThreadDumpEventNotFoundException | JzrTranslatorMultipleJFRFilesException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR Recording " + buildOriginalFileName(zipFileId) + " could not be analyzed.<br/>" + buildExceptionMessage(e), NotificationLevel.WARNING);
		} catch (JzrTranslatorZipInvalidFileException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR Recording " + buildOriginalFileName(zipFileId) + " could not be loaded.<br/>" + buildExceptionMessage(e), NotificationLevel.WARNING);
		} catch (JzrTranslatorZipPasswordProtectedException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR Recording " + buildOriginalFileName(zipFileId) + " could not be loaded.<br/>" + buildExceptionMessage(e), NotificationLevel.WARNING);
		} catch (JzrTranslatorRecordingSnapshotNotFoundException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("No valid recording snapshots found in file " + buildOriginalFileName(zipFileId) + "<br/>" + buildFileFormats(e.getSupportedFileFormats()), NotificationLevel.WARNING);
		} catch (JzrExecutionException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR report generation could not complete : " + buildExceptionMessage(e) + buildRefMessage(request), NotificationLevel.ERROR);
		} catch (JzrInitializationException e) {
			logger.info("JZR report generation could not complete. Failed to load profile : " + request.getProfile() + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR report generation could not complete : failed to load profile " + request.getProfile() + "(). " + getContactErrorMessage() + buildRefMessage(request), NotificationLevel.ERROR);
		} catch (JzrWebUploadedRecordingExpiredException e) {
			logger.info(e.getMessage());
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser(buildExceptionMessage(e), NotificationLevel.WARNING);
		} catch (JzrWebException e) {
			logger.info("JZR report generation could not complete." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR report generation could not complete : " + buildExceptionMessage(e) + buildRefMessage(request), NotificationLevel.WARNING);
		} catch (java.lang.OutOfMemoryError e) {
			logger.warn("JZR report generation failed : not enough memory." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR report generation failed : volume of info to process is too high. <BR/>"
					+ "Either reduce the number of files to analyze or refine the related applicative master profile or ask the administrator to increase the Xmx of this server." 
					+ buildRefMessage(request), 
					NotificationLevel.ERROR);
		} catch (Throwable e) {
			logger.warn("JZR report generation failed." + buildRefMessage(request), e);
			ui.getSession().lock(); // mandatory for the notifyUser
			notifyUser("JZR report generation failed. " + getContactErrorMessage() + buildRefMessage(request), NotificationLevel.ERROR);
		}  finally {
			// remove the uploaded file entry from the uploader
			if (upload != null)
				upload.getElement().executeJavaScript(JS_CLEAR_FILES);
			// reset the file name to force a new upload
			zipFileId = null;
						
			// Progress bar disabling
			ui.setPollInterval(-1);
			progressBar.setVisible(false);
			progressStatus.setVisible(false);
			password.setValue("");
			recordingTimeZoneList.clear();
			recordingTimeZoneList.setReadOnly(true);
			reportTimeZoneList.clear();
			reportTimeZoneList.setReadOnly(true);
		}
	}
	
	protected java.util.Map<String, String> getRemoteAddresses(VaadinRequest vaadinRequest) {
        final java.util.Map<String, String> addressList = new java.util.HashMap<>(); 

        for (final Enumeration<String> vias = vaadinRequest.getHeaders("Via"); vias.hasMoreElements();) {
            addressList.put("Via", (String) vias.nextElement());
        }
        for (final Enumeration<String> vias = vaadinRequest.getHeaders("x-forwarded-for"); vias.hasMoreElements();) {
            addressList.put("X-forwarded-for", (String) vias.nextElement());
        }
        addressList.put("Browser", vaadinRequest.getRemoteAddr());
        return addressList; 
    }

	private void fillTimeZoneUserSelections(JzrAnalysisRequest request) {
		request.setRecordingTimeZoneUserSpecified(
				!timeZoneDetection.getValue().booleanValue() 
				&& recordingTimeZoneList.isVisible() 
				&& recordingTimeZoneList.getValue() != null
				);
		request.setReportTimeZoneUserSpecified(
				!timeZoneDetection.getValue().booleanValue() 
				&& reportTimeZoneList.isVisible() 
				&& reportTimeZoneList.getValue() != null
				);
	}

	private boolean validateAcceptance() {
		ConfigPortal portalCfg = JeyzerServlet.getConfigWeb().getConfigPortal();
		if (portalCfg.isAgreementDisplayed()) {
			if (!this.agreementAccepted && !portalCfg.isCaptchaDisplayed()) {
				notifyUser("You must accept the privacy policy & terms of service.", NotificationLevel.WARNING);
				return false;
			}
			
			if (portalCfg.isCaptchaDisplayed()) {
				if (!this.agreementAccepted && !reCaptcha.isValid()) {
					notifyUser("You must accept the privacy policy & terms of service and check the Captcha.", NotificationLevel.WARNING);
					return false;
				}
				else if (!this.agreementAccepted) {
					notifyUser("You must accept the privacy policy & terms of service.", NotificationLevel.WARNING);
					return false;
				}
				else if (!reCaptcha.isValid()) {
					notifyUser("You must check the Captcha.", NotificationLevel.WARNING);
					return false;
				}
			}
		}

		return true;
	}
	
	private String buildExceptionMessage(Exception e) {
		return e.getMessage().replace(". ", ".<BR/>"); // add carriage return
	}

	private String buildRefMessage(JzrAnalysisRequest request) {
		return "<BR/>Request id : " + request.getId();
	}
	
	private String buildOriginalFileName(String fileName) {
		return JeyzerServlet.getTempFileManager().getOriginalZipFileName(fileName);
	}

	private String buildFileFormats(Set<String> supportedFileFormats) {
		String message = "Please use one of these recording/thread dump file formats :<BR/>";
		for (String format : supportedFileFormats)
			message += " - " + format + "<BR/>";
		return message;
	}

	private void generateLinks(ReportDescriptor reportDescriptor) {
		HorizontalLayout allLinksLayout = new HorizontalLayout();
		allLinksLayout.setClassName("layout-all-links");
		allLinksLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		allLinksLayout.setWidthFull();

		// Report as a file resource
		StreamResource resource = new StreamResource(reportDescriptor.getReportName(),
	            () -> getImageInputStream(reportDescriptor.getReportPath()));
		
		// report
		createImageLink(
				getReportImage(reportDescriptor),
				resource,
				allLinksLayout);
		
		if (JeyzerServlet.getConfigWeb().isUfoStackFileLinkDisplayEnabled() && reportDescriptor.getUfoStackFileName()!=null){
			// UFO file
			StreamResource ufoResource = new StreamResource(reportDescriptor.getUfoStackFileName(),
		            () -> getImageInputStream(reportDescriptor.getUfoStackFilePath()));
			createImageLink(
					"img/ufo.png",
					ufoResource,
					allLinksLayout);
		}
		
		if (reportDescriptor.getMusicFileName()!=null){
			// Music file
			StreamResource musicResource = new StreamResource(reportDescriptor.getMusicFileName(),
		            () -> getImageInputStream(reportDescriptor.getMusicFilePath()));
			createImageLink(
					"img/music.png",
					musicResource,
					allLinksLayout);
		}
		
		createDescriptorLink(reportDescriptor, resource, allLinksLayout);
		
		right.add(allLinksLayout);
		right.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, allLinksLayout);
	}

	private String getReportImage(ReportDescriptor reportDescriptor) {
		if (!reportDescriptor.isAnalysisProductionReady())
			return "img/report-no-level.png";
		
		switch(reportDescriptor.getLevel()) {
			case CRITICAL:
				return "img/report-circle-critical.png";
			case WARNING:
				return "img/report-circle-warning.png";
			case INFO:
				return "img/report-circle-info.png";
			default:
				return "img/report-no-level.png";
		}
	}

	private void createDescriptorLink(ReportDescriptor reportDescriptor, StreamResource resource, HorizontalLayout allLinksLayout) {
		VerticalLayout descLayout = new VerticalLayout();
		descLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);

		Anchor link = new Anchor(resource, reportDescriptor.getApplicationId());
		link.setClassName("report-description");
		descLayout.add(link);
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d, HH:mm");
		TimeZoneInfo timeZoneInfo = reportDescriptor.getTimeZoneInfo();
		if (!timeZoneInfo.isUnknown())
			sdf.setTimeZone(timeZoneInfo.getZone());
		Anchor datesLink = new Anchor(resource, "Start time : " + sdf.format(reportDescriptor.getStartTime()) + formatTimeZoneInfo(timeZoneInfo));
		datesLink.setClassName("report-date");
		descLayout.add(datesLink);
		
		Anchor durationLink = new Anchor(resource, "Duration : " + getPrintableDuration(reportDescriptor.getEndTime().getTime() - reportDescriptor.getStartTime().getTime(), false));
		durationLink.setClassName("report-duration");
		descLayout.add(durationLink);
		
		allLinksLayout.add(descLayout);
	}
	
	private String formatTimeZoneInfo(TimeZoneInfo timeZoneInfo) {
		if (!timeZoneInfo.isUnknown()) {
			return " - " + timeZoneInfo.getZoneAbbreviation() + " "+ timeZoneInfo.getDisplayOrigin().toLowerCase();
		}
		else
			return "";
	}
	
	private String getPrintableDuration(long time, boolean displaySeconds){
		String duration = "";
		Duration dt;
		try {
			dt = DatatypeFactory.newInstance().newDuration(time);
			if (dt.getMinutes() == 0 || displaySeconds) 
				duration = dt.getSeconds() + " s";
			if (dt.getMinutes() > 0)
				duration = dt.getMinutes() + " mn " + duration;
			if (dt.getHours() > 0)
				duration = dt.getHours() + " h "+ duration; // add the hours if available
			if (dt.getDays() > 0)
				duration = dt.getDays() + " d "+ duration; // add the days if available
		} catch (DatatypeConfigurationException e) {
			logger.error("Failed to print duration", e);
		}	
		return duration;
	}

	private void createImageLink(String imagePath, StreamResource resource, HorizontalLayout linkLayout) {
		Image image = new Image(imagePath, "");
		image.setHeight("50px");
		image.setWidth("50px");
		
		FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(resource);
		buttonWrapper.wrapComponent(image);
		
		linkLayout.add(buttonWrapper);
	}

	private InputStream getImageInputStream(String resourcePath) {
	    File file = new File(resourcePath);
	    byte[] bytesArray = new byte[(int) file.length()]; 

		try (
			    FileInputStream fis = new FileInputStream(file);
			)
		{
		    @SuppressWarnings("unused")
			int count = 0;
		    while ((count = fis.read(bytesArray)) > 0) {
		      // continue to read
		    }
		} catch (FileNotFoundException ex) {
			logger.error("Failed to load the file : " + resourcePath, ex);
		} catch (IOException ex) {
			logger.error("Failed to read the file : " + resourcePath, ex);
		}
	    
	    return new ByteArrayInputStream(bytesArray);
	}

	private void processProfileSelection(ComponentValueChangeEvent<ListBox<String>, String> event) {
		String profileName = event.getValue();
		JzrSetup jzrSetup = JeyzerServlet.getJzrSetup();
		MasterProfile profile = jzrSetup.getProfile(profileName);
		discoveryTable.setVisible(profile.isDiscoveryModeEnabled());
		password.setVisible(!profile.getSecurity().isPasswordFree());
		if (profile.getSecurity().isPasswordMandatory())
			activateMandatoryPassword();
		else
			activateOptionalPassword();
		updateDefaultTimeZoneUponProfileSelection(profile);
	}
	
	private void processUploadedFile(SucceededEvent event, MemoryBuffer buffer) {
		logger.info("Recording file received");
		
		// Accept only jfr, zip or gzip file
		if (!ZipHelper.isCompressedFile(event.getFileName()) && !JFRHelper.isJFRFile(event.getFileName())) {
			notifyUser("Drag and drop a zip or tar.gz or jfr file", NotificationLevel.WARNING);
			logger.warn("Recording file rejected : attempt to load non supported recording file format");
			upload.getElement().executeJavaScript(JS_CLEAR_FILES);
			return;
		}
		
		if (event.getContentLength() > (JeyzerServlet.getConfigWeb().getUploadRecordingMaxSizeInBytes())) {
			notifyUser("File rejected. File must not exceed " + JeyzerServlet.getConfigWeb().getUploadRecordingMaxSize() +" Mb.", NotificationLevel.WARNING);
			logger.warn("Recording file rejected : file exceeds the maximum limit of " + JeyzerServlet.getConfigWeb().getUploadRecordingMaxSize() + " Mb");
			upload.getElement().executeJavaScript(JS_CLEAR_FILES);
			return;
		}

		TempFileManager tempFileManager = JeyzerServlet.getTempFileManager();
		try (
				InputStream fileStream	 = buffer.getInputStream();
			)
		{
			if (fileStream == null) {
				notifyUser("File upload failed. Invalid input stream.", NotificationLevel.ERROR);
				logger.error("Recording file invalid : input stream is null");
				upload.getElement().executeJavaScript(JS_CLEAR_FILES);
				return;
			}
			this.zipFileId = tempFileManager.addZipFile(event.getFileName(), fileStream);
		} catch (IOException e) {
			notifyUser("File upload failed. Invalid input stream.", NotificationLevel.ERROR);
			logger.error("Recording file invalid : input stream read error");
			upload.getElement().executeJavaScript(JS_CLEAR_FILES);
			return;
		}

		selectRelatedProfile(event.getFileName());
		
		String localPath = tempFileManager.getTempDirectory()
				+ File.separatorChar
				+ this.zipFileId;
		logger.info("Recording file loaded on disk : " 
				+ SystemHelper.sanitizePathSeparators(localPath));
		
		selectTimeZone(localPath);
	}
	
	private void updateDefaultTimeZoneUponProfileSelection(MasterProfile profile) {
		// keep track of the current emptiness state as the recording update will change its value through its trigger
		boolean reportTimeZoneEmpty = this.reportTimeZoneList.isEmpty();
		
		if (this.recordingTimeZoneList.isEmpty() && !profile.getDefaultRecordingTimeZone().isUnknown())
			this.recordingTimeZoneList.setValue(profile.getDefaultRecordingTimeZone().getZone().getID());
		if (reportTimeZoneEmpty && !profile.getDefaultReportTimeZone().isUnknown())
			this.reportTimeZoneList.setValue(profile.getDefaultReportTimeZone().getZone().getID());
	}
	
	private void selectTimeZone(String localPath) {
		// select it even if time zone detection is checked
		if (JFRHelper.isJFRFile(localPath)) {
			this.recordingTimeZoneList.setValue("UTC");
			this.recordingTimeZoneList.setReadOnly(true);
			
			this.reportTimeZoneList.setValue("UTC");
			this.reportTimeZoneList.setReadOnly(false);
		}
		else {
			JzrSetup jzrSetup = JeyzerServlet.getJzrSetup();
			MasterProfile profile = jzrSetup.getProfile(this.profileList.getValue());
			SnapshotFileNameFilter filter = new SnapshotFileNameFilter(profile.getFilePatterns());
			String snapshotFileSample = extractSnapshotSample(localPath); // can be null
			TimeZoneInfo zone = TimeZoneInfoHelper.getTimeZoneInfo(filter, snapshotFileSample);
			if (zone.isUnknown()){
				if (profile.getDefaultRecordingTimeZone().isUnknown())
					this.recordingTimeZoneList.clear();
				else 
					this.recordingTimeZoneList.setValue(profile.getDefaultRecordingTimeZone().getZone().getID());
				this.recordingTimeZoneList.setReadOnly(false);
				
				if (profile.getDefaultReportTimeZone().isUnknown())
					this.reportTimeZoneList.clear();
				else
					this.reportTimeZoneList.setValue(profile.getDefaultReportTimeZone().getZone().getID());
				this.reportTimeZoneList.setReadOnly(false);
			}
			else {
				this.recordingTimeZoneList.setValue(zone.getZone().getID());
				this.recordingTimeZoneList.setReadOnly(true);
				
				if (profile.getDefaultReportTimeZone().isUnknown())
					this.reportTimeZoneList.setValue(zone.getZone().getID()); // same as recording
				else
					this.reportTimeZoneList.setValue(profile.getDefaultReportTimeZone().getZone().getID());
				this.reportTimeZoneList.setReadOnly(false);
			}
		}
	}

	private String extractSnapshotSample(String localPath) {
		ZipParams params = JeyzerServlet.getConfigWeb().getConfigGzipParams();
		List<String> fileNames = ZipHelper.listContent(localPath, params);
		for (String fileName : fileNames) {
			if (!AnalyzerHelper.isRecordingStaticFile(new File(fileName)))
				return fileName;
		}
		return null;
	}

	private void notifyUser(String message, NotificationLevel level) {
		NativeButton buttonInside = new NativeButton("Close");
		buttonInside.setClassName("notification-close-button");
		
		int duration = NotificationLevel.SUCCESS.equals(level) || NotificationLevel.INFO.equals(level) ? 15000 : 0;
		
		Notification notification = new Notification(
				new Html("<div>" + message + "</div>"),
				buttonInside);
		notification.setPosition(Position.TOP_START);
		notification.setDuration(duration);
		buttonInside.addClickListener(event -> notification.close());
		
		notification.getElement().getThemeList().add(level.name().toLowerCase());
		notification.open();
	}

	private void selectRelatedProfile(String fileName) {
		JzrSetup jzrSetup = JeyzerServlet.getJzrSetup();
		
		for (String profile : jzrSetup.getProfiles()){
			String profileLowerCase = profile.toLowerCase();
			if (fileName.toLowerCase().contains(profileLowerCase)){
				if (!profile.equals(this.profileList.getValue()))
					this.profileList.setValue(profile); // change the profile, otherwise stay on the same
				return;
			}
		}
		
		if (!JeyzerServlet.getConfigWeb().getConfigPortal().areProfilesDisplayed())
			// fall back to default profile in portal online mode
			this.profileList.setValue(jzrSetup.getDefaultProfile());
	}

	private ColorPickerField createColorPickerField(JzrDiscoveryItem item) {
		ColorPickerField colorPickerField = new ColorPickerField("", DEFAULT_COLORS[colorIndex], "Placeholder");
		colorPickerField.setPinnedPalettes(true);
		colorPickerField.setHexEnabled(false);
		colorPickerField.setThemeName("no-input");
		colorPickerField.setPalette(Color.RED, Color.GREEN, Color.BLUE, Color.PINK, Color.CYAN);
		colorPickerField.setHistoryEnabled(false);
		colorPickerField.setWidth("40px");
		
		colorPickerField.setValue(DEFAULT_COLORS[colorIndex]);
		String defaultDiscoveryColor = "RGB-" 
				+ DEFAULT_COLORS[colorIndex].getRed() + "-" 
				+ DEFAULT_COLORS[colorIndex].getGreen() + "-"
				+ DEFAULT_COLORS[colorIndex].getBlue();
		item.setColor(defaultDiscoveryColor);
		
		// Remove the setHexEnabled if native
		// colorPickerField.setNativeInputMediaQuery("(min-width: 0px), (min-height: 0px)");
		colorPickerField.addValueChangeListener(inputEvent -> {
			String functionDiscoveryColor = "RGB-" 
					+ inputEvent.getValue().getRed() + "-" 
					+ inputEvent.getValue().getGreen() + "-"
					+ inputEvent.getValue().getBlue();
			item.setColor(functionDiscoveryColor);
		});
		colorPickerField.setChangeFormatButtonVisible(true);
		colorIndex++;
		return colorPickerField;
	}
	
	private TextField createTextField(JzrDiscoveryItem item) {
		TextField functionKeyWord = new TextField();
		functionKeyWord.setValue(DEFAULT_KEYWORDS[functionIndex]);
		functionKeyWord.setClearButtonVisible(true);
		functionKeyWord.setWidth("200px");
		functionKeyWord.addValueChangeListener(inputEvent -> item.setKeyWords(inputEvent.getValue()));
		functionIndex++;
		return functionKeyWord;
	}
    
	private void activateOptionalPassword() {
		password.setLabel("Optional password");
		password.setRequired(false);
		password.setTitle("JZR report protection password. Optional.");
	} 

	private void activateMandatoryPassword() {
		password.setLabel("Password");
		password.setRequired(true);
		password.setTitle("JZR report protection password. Mandatory.");
	}
	
	private String getContactErrorMessage() {
		// We assume agreement display means portal
		boolean portalMode = JeyzerServlet.getConfigWeb().getConfigPortal().isAgreementDisplayed();
		return portalMode ? ERROR_PLEASE_PORTAL_MESSAGE.replace("@forum.url@", JeyzerServlet.getConfigWeb().getConfigPortal().getForumUrl()) : ERROR_PLEASE_STANDARD_MESSAGE;
	}
}
