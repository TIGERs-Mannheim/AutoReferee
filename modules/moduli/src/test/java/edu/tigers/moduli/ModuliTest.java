/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli;

import edu.tigers.moduli.exceptions.DependencyException;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.LoadModulesException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.moduli.modules.ConcreteTestModule;
import edu.tigers.moduli.modules.ConfiguredTestModule;
import edu.tigers.moduli.modules.TestModule;
import edu.tigers.moduli.modules.UnusedConcreteTestModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ModuliTest
{
	private static final String MODULE_CONFIG_PATH = "src/test/resources/";
	private static final String TEST_CONFIG_XML = "test_config.xml";
	private static final String EMPTY_CONFIG_XML = "empty_config.xml";
	private static final String CYCLIC_CONFIG_XML = "cyclic_config.xml";
	private static final String UNRESOLVED_DEPENDENCY_CONFIG_XML = "unresolved_dependency_config.xml";

	private Moduli moduli;


	@Before
	public void setUp()
	{
		moduli = new Moduli();
	}


	@After
	public void tearDown()
	{
		moduli = null;
	}


	@Test
	public void testModuliCycle() throws InitModuleException, StartModuleException
	{
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.NOT_LOADED);

		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);

		assertThat(moduli.isModuleLoaded(TestModule.class)).isTrue();
		assertThat(moduli.isModuleLoaded(ConcreteTestModule.class)).isTrue();
		assertThat(moduli.isModuleLoaded(UnusedConcreteTestModule.class)).isFalse();

		assertThat(moduli.getModule(TestModule.class).isConstructed()).isTrue();
		assertThat(moduli.getModule(ConcreteTestModule.class).isConstructed()).isTrue();

		moduli.startModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.ACTIVE);

		moduli.stopModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
	}


	@Test
	public void testModuleCycle() throws InitModuleException, StartModuleException
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		TestModule module = moduli.getModule(TestModule.class);
		assertThat(module.isConstructed()).isTrue();

		moduli.startModules();
		assertThat(module.isInitialized()).isTrue();
		assertThat(module.isStarted()).isTrue();

		moduli.stopModules();
		assertThat(module.isStopped()).isTrue();
		assertThat(module.isDeinitialized()).isTrue();
	}


	@Test
	public void testEmptyConfig() throws InitModuleException, StartModuleException
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + EMPTY_CONFIG_XML);
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
		assertThat(moduli.isModuleLoaded(TestModule.class)).isFalse();

		moduli.startModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.ACTIVE);

		moduli.stopModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
	}


	@Test
	public void testGlobalConfiguration()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + EMPTY_CONFIG_XML);
		String env = moduli.getGlobalConfiguration().getString("environment");
		assertThat(env).isEqualTo("MODULI");
	}


	@Test
	public void testModuleConfiguration() throws InitModuleException, StartModuleException
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		moduli.startModules();
		ConfiguredTestModule module = moduli.getModule(ConfiguredTestModule.class);
		assertThat(module.getConfigProperty()).isEqualTo("exists");
	}


	@Test(expected = DependencyException.class)
	public void testCyclicConfiguration() throws DependencyException, LoadModulesException
	{
		moduli.loadModules(MODULE_CONFIG_PATH + CYCLIC_CONFIG_XML);
	}


	@Test(expected = DependencyException.class)
	public void testUnresolvedDependencyConfiguration() throws DependencyException, LoadModulesException
	{
		moduli.loadModules(MODULE_CONFIG_PATH + UNRESOLVED_DEPENDENCY_CONFIG_XML);
	}


	@Test(expected = ModuleNotFoundException.class)
	public void testModuleNotFound()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);

		assertThat(moduli.isModuleLoaded(TestModule.class)).isTrue();

		moduli.getModule(UnusedConcreteTestModule.class);
	}
}
