/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref;

import edu.tigers.autoref.gui.AutoRefMainPresenter;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.ModulesState;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.wp.exporter.VisionTrackerSender;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import picocli.CommandLine;

import javax.swing.SwingUtilities;
import java.util.function.Consumer;


/**
 * Main class for auto referee.
 */
@Log4j2
public final class AutoReferee implements Runnable
{
	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-hl", "--headless" },
			defaultValue = "${env:AUTOREF_HEADLESS:-false}",
			description = "run without a UI"
	))
	private boolean headless = false;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-va", "--visionAddress" },
			defaultValue = "${env:AUTOREF_VISION_ADDRESS}",
			description = "address:port for vision")
	)
	private String visionAddress;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-ra", "--refereeAddress" },
			defaultValue = "${env:AUTOREF_REFEREE_ADDRESS}",
			description = "address:port for GC")
	)
	private String refereeAddress;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-ta", "--trackerAddress" },
			defaultValue = "${env:AUTOREF_TRACKER_ADDRESS}",
			description = "address:port for tracker")
	)
	private String trackerAddress;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-a", "--active" },
			defaultValue = "${env:AUTOREF_ACTIVE:-false}",
			description = "activate autoRef in active mode")
	)
	private boolean autoRef;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-c", "--ci" },
			defaultValue = "${env:AUTOREF_CI:-false}",
			description = "use CI mode")
	)
	private boolean ciMode;


	public static void main(final String[] args)
	{
		new CommandLine(new AutoReferee()).execute(args);
	}


	@Override
	public void run()
	{
		log.info("Starting AutoReferee {}", SumatraModel.getVersion());

		// Start the UI in a separate thread first
		runIf(!headless, this::startUi);

		ifNotNull(visionAddress, this::updateVisionAddress);
		ifNotNull(refereeAddress, this::updateRefereeAddress);
		ifNotNull(trackerAddress, this::updateTrackerAddress);

		start();

		runIf(autoRef, this::activateAutoRef);

		registerShutdownHook();
		log.trace("Started AutoReferee");
	}


	private <T> void ifNotNull(T value, Consumer<T> consumer)
	{
		if (value != null)
		{
			consumer.accept(value);
		}
	}


	private void runIf(boolean condition, Runnable runnable)
	{
		if (condition)
		{
			runnable.run();
		}
	}


	private void registerShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown, "Sumatra-shutdown"));
	}


	private void onShutdown()
	{
		log.debug("Shutting down");
		if (SumatraModel.getInstance().getModulesState().get() == ModulesState.ACTIVE)
		{
			SumatraModel.getInstance().stopModules();
		}
		SumatraModel.getInstance().saveUserProperties();
		log.debug("Shut down");
		// We have disabled the shutdown hook in log4j2.xml, so we have to shut log4j down manually
		LogManager.shutdown();
	}


	private void startUi()
	{
		SwingUtilities.invokeLater(AutoRefMainPresenter::new);
	}


	private void updateVisionAddress(String fullAddress)
	{
		log.info("Setting custom vision address: {}", fullAddress);
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


	private void updateRefereeAddress(String fullAddress)
	{
		log.info("Setting custom referee address: {}", fullAddress);
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


	private void updateTrackerAddress(String fullAddress)
	{
		log.info("Setting custom tracker address: {}", fullAddress);
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


	private void activateAutoRef()
	{
		log.info("Activating autoRef in active mode");
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
				.ifPresent(a -> a.changeMode(EAutoRefMode.ACTIVE));
	}


	private void start()
	{
		try
		{
			String config = ciMode ? "autoreferee-ci.xml" : "autoreferee.xml";
			SumatraModel.getInstance().setCurrentModuliConfig(config);
			SumatraModel.getInstance().loadModulesOfConfig(SumatraModel.getInstance().getCurrentModuliConfig());
			SumatraModel.getInstance().startModules();
		} catch (Throwable e)
		{
			log.error("Could not start Sumatra. Setting moduli config to default. Please try again.", e);
			System.exit(1);
		}
	}
}
