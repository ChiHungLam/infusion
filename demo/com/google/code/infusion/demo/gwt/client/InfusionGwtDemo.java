package com.google.code.infusion.demo.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class InfusionGwtDemo implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		SimplePanel loginPanel = new SimplePanel();
		
		Grid grid = new Grid(3, 2);
				
		grid.setWidget(0, 0, new Label("Email Address:"));
		grid.setWidget(1, 0, new Label("Password:"));
		
		TextBox email = new TextBox();
		PasswordTextBox password = new PasswordTextBox();

		grid.setWidget(0, 1, email);
		grid.setWidget(1, 1, password);

		final Button loginButton = new Button("Login");
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.alert("Click!");
			}});
	    grid.setWidget(2, 1, loginButton);

	    loginPanel.add(grid);
	    
		RootPanel.get().add(loginPanel);
	}
}
