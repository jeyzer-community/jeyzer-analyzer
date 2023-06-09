package org.jeyzer.web.util;

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


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.templatemodel.TemplateModel;

@SuppressWarnings("serial")
@Tag("file-download-wrapper")
@HtmlImport("html/file-download-wrapper.html")
public class FileDownloadWrapper extends PolymerTemplate<FileDownloadWrapper.FileDownloadWrapperModel> {

    @Id("download-link")
    protected Anchor anchor;

    protected FileDownloadWrapper() {
        anchor.getElement().setAttribute("download", true);
    }

    protected FileDownloadWrapper(String fileName) {
        this();
        setFileName(fileName);

    }

    public FileDownloadWrapper(String fileName, File file) {
        this(fileName);
        setFile(file);
    }

    public FileDownloadWrapper(String fileName, DownloadBytesProvider provider) {
        this(fileName);
        setBytesProvider(fileName, provider);
    }

    public FileDownloadWrapper(StreamResource streamResource) {
        this();
        setResource(streamResource);
    }

    public void setFileName(String fileName) {
        getModel().setFileName(fileName);
    }

    public void setResource(StreamResource streamResource) {
        anchor.setHref(streamResource);
    }

    public void setBytesProvider(String fileName, DownloadBytesProvider provider) {
        setResource(new StreamResource(fileName, () -> new ByteArrayInputStream(provider.getBytes())));
    }

    public void setText(String text) {
        anchor.setText(text);
    }

    public void wrapComponent(Component component) {
        anchor.removeAll();
        if (component != null) {
            anchor.add(component);
        }
    }

    private InputStream createResource(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            throw new IllegalArgumentException(fnfe);
        }
    }

    public void setFile(File file) {
        anchor.setHref(new StreamResource(getModel().getFileName(), () -> createResource(file)));
    }

    @FunctionalInterface
    interface DownloadBytesProvider {

        byte[] getBytes();
    }

    public interface FileDownloadWrapperModel extends TemplateModel {
        String getFileName();

        void setFileName(String fileName);
    }
}
