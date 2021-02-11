/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref;

import edu.tigers.autoref.gui.AutoRefMainPresenter;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.model.SumatraModel;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;


/**
 * Main class for auto referee.
 */
public final class AutoReferee
{
	private static CommandLine cmd;


	private AutoReferee()
	{
	}


	public static void main(final String[] args)
	{
		Options options = createOptions();
		cmd = parseOptions(args, options, new DefaultParser());

		ifHasOption("h", () -> printHelp(options));
		ifHasOption("hl", () -> SumatraModel.changeLogLevel(Level.INFO));
		ifNotHasOption("hl", AutoReferee::startUi);
		ifHasOption("a", AutoReferee::activateAutoRef);
		  
		start();
	}


	private static void startUi()
	{
		AutoRefMainPresenter p = new AutoRefMainPresenter();
		String windowSize = cmd.getOptionValue("w");
		if (windowSize != null)
		{
			var parts = windowSize.split("x");
			p.setWindowSize(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
		}
	}


	private static void ifHasOption(String shortOptions, Runnable r)
	{
		if (cmd.hasOption(shortOptions))
		{
			r.run();
		}
	}


	private static void ifNotHasOption(String shortOptions, Runnable r)
	{
		if (!cmd.hasOption(shortOptions))
		{
			r.run();
		}
	}


	private static Options createOptions()
	{
		Options options = new Options();
		options.addOption("h", "help", false, "Print this help message");
		options.addOption("hl", "headless", false, "run without a UI");
		options.addOption("a", "active", false, "Start autoRef in active mode");
		options.addOption("w", "window", true, "Set window size (example: 1920x1080)");
		return options;
	}


	private static CommandLine parseOptions(final String[] args, final Options options, final CommandLineParser parser)
	{
		try
		{
			return parser.parse(options, args);
		} catch (ParseException e)
		{
			printHelp(options);
		}
		return null;
	}


	private static void printHelp(final Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("AutoReferee", options);
		System.exit(0);
	}


	private static void activateAutoRef()
	{
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
				.ifPresent(a -> a.changeMode(EAutoRefMode.ACTIVE));
	}


	private static void start()
	{
		SumatraModel.getInstance().setCurrentModuliConfig("moduli.xml");
		try
		{
			SumatraModel.getInstance().loadModulesOfConfigSafe(SumatraModel.getInstance().getCurrentModuliConfig());
			SumatraModel.getInstance().startModules();
		} catch (InitModuleException | StartModuleException e)
		{
			throw new IllegalStateException("Failed to startup modules", e);
		}
	}
}
