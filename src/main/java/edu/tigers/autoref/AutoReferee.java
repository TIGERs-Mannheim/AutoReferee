/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref;

import edu.tigers.autoref.gui.AutoRefMainPresenter;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.moduli.exceptions.InitModuleException;
import edu.tigers.sumatra.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.wp.exporter.VisionTrackerSender;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.swing.SwingUtilities;


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
		ifNotHasOption("hl", () -> SwingUtilities.invokeLater(AutoReferee::startUi));
		ifHasOption("va", () -> setVisionAddress(cmd.getOptionValue("va")));
		ifHasOption("ra", () -> setRefereeAddress(cmd.getOptionValue("ra")));
		ifHasOption("ta", () -> setTrackerAddress(cmd.getOptionValue("ta")));

		start();

		ifHasOption("a", AutoReferee::activateAutoRef);
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
		options.addOption("va", "visionAddress", true, "address:port for vision");
		options.addOption("ra", "refereeAddress", true, "address:port for GC");
		options.addOption("ta", "trackerAddress", true, "address:port for tracker");
		options.addOption("c", "ci", false, "Enable CI mode");
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

	private static void setVisionAddress(String fullAddress)
	{
		String[] parts = fullAddress.split(":");
		String address = parts[0];
		if (!address.isBlank())
		{
			SSLVisionCam.setCustomAddress(address);
		}
		if (parts.length > 1)
		{
			SSLVisionCam.setCustomPort(Integer.parseInt(parts[1]));
		}
	}


	private static void setRefereeAddress(String fullAddress)
	{
		String[] parts = fullAddress.split(":");
		String address = parts[0];
		if (!address.isBlank())
		{
			Referee.setCustomAddress(address);
		}
		if (parts.length > 1)
		{
			Referee.setCustomPort(Integer.parseInt(parts[1]));
		}
	}


	private static void setTrackerAddress(String fullAddress)
	{
		String[] parts = fullAddress.split(":");
		String address = parts[0];
		if (!address.isBlank())
		{
			VisionTrackerSender.setCustomAddress(address);
		}
		if (parts.length > 1)
		{
			VisionTrackerSender.setCustomPort(Integer.parseInt(parts[1]));
		}
	}


	private static void start()
	{
		String config = cmd.hasOption("c") ? "moduli-ci.xml" : "moduli.xml";
		SumatraModel.getInstance().setCurrentModuliConfig(config);
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
