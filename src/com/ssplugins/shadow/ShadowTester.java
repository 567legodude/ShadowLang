package com.ssplugins.shadow;

import com.ssplugins.shadow.lang.Debugger;
import com.ssplugins.shadow.lang.Shadow;
import com.ssplugins.shadow.lang.ShadowCommons;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;

public class ShadowTester extends Application {
	
	public static void main(String[] args) {
		ShadowTester.launch(ShadowTester.class);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose Shadow file");
		chooser.getExtensionFilters().add(new ExtensionFilter("Shadow File", "*.shd"));
		File file = chooser.showOpenDialog(primaryStage);
		if (file == null) {
			Platform.exit();
			return;
		}
		
		
		//Debugger.setEnabled(true);
		Shadow shadow = Shadow.parseCommons(file);
		shadow.runBlocks("test", new String[] {"first", "second", "third"}, "testy");
		shadow.end();
		
		
		Platform.exit();
	}
	
	private static void log(String msg) {
		System.out.println(msg);
	}
	
	private static void dialog(String msg) {
		JOptionPane.showMessageDialog(null, msg, "ShadowTester", JOptionPane.PLAIN_MESSAGE);
	}
}
