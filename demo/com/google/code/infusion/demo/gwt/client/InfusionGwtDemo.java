package com.google.code.infusion.demo.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class InfusionGwtDemo implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		Panel loginPanel = new VerticalPanel();
		loginPanel.add(new Label("InfusionGwtDemo"));
		RootPanel.get().add(loginPanel);
		
		final Button loginButton = new Button("Login");
	    loginPanel.add(loginButton);
	}
}
