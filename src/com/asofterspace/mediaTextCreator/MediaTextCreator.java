/**
 * Unlicensed code created by A Softer Space, 2022
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.mediaTextCreator;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.Utils;

import javax.swing.SwingUtilities;


public class MediaTextCreator {

	public final static String PROGRAM_TITLE = "MediaTextCreator";
	public final static String VERSION_NUMBER = "0.0.0.1(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "26th of August 2022 - 26th of August 2022";

	private static ConfigFile config;


	public static void main(String[] args) {

		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		if (args.length > 0) {
			if (args[0].equals("--version")) {
				System.out.println(Utils.getFullProgramIdentifierWithDate());
				return;
			}

			if (args[0].equals("--version_for_zip")) {
				System.out.println("version " + Utils.getVersionNumber());
				return;
			}
		}

		System.out.println("Loading settings...");

		// load config
		boolean onlyUseDefaultIfBroken = true;
		config = new ConfigFile("settings", true, Record.emptyObject(), onlyUseDefaultIfBroken);

		System.out.println("Loading database...");

		Database database = new Database();

		System.out.println("Saving database...");

		database.save();

		System.out.println("MediaTextCreator ready!");

		SwingUtilities.invokeLater(new GUI(database, config));

		System.out.println("Done! Have a nice day! :)");
	}

}
