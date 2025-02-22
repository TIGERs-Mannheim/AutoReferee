package edu.tigers.moduli;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;


/**
 * Structure for
 */
public abstract class AModule
{
	private Class<? extends AModule> clazz;
	private SubnodeConfiguration subnodeConfiguration;
	private List<Class<? extends AModule>> dependencies = new ArrayList<>();
	private boolean startModule = true;
	
	
	/**
	 * Inits module.
	 *
	 * @throws InitModuleException if the module couldn't be initialized
	 */
	public void initModule() throws InitModuleException
	{
		// does nothing by default
	}
	
	
	/**
	 * DeInits module.
	 */
	public void deinitModule()
	{
		// does nothing by default
	}
	
	
	/**
	 * Starts module.
	 *
	 * @throws StartModuleException if the module couldn't be started
	 */
	public void startModule() throws StartModuleException
	{
		// does nothing by default
	}
	
	
	/**
	 * Stops module.
	 */
	public void stopModule()
	{
		// does nothing by default
	}
	
	
	/**
	 * @return the module clazz
	 */
	public Class<? extends AModule> getId()
	{
		return clazz;
	}
	
	
	/**
	 * @param clazz the module clazz
	 */
	public void setId(final Class<? extends AModule> clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the list of dependencies
	 */
	public List<Class<? extends AModule>> getDependencies()
	{
		return dependencies;
	}
	
	
	/**
	 * @param dependencies the new list of dependencies
	 */
	public void setDependencies(final List<Class<? extends AModule>> dependencies)
	{
		this.dependencies = dependencies;
	}
	
	
	/**
	 * @return the subnode configuration
	 */
	public SubnodeConfiguration getSubnodeConfiguration()
	{
		return subnodeConfiguration;
	}
	
	
	void setSubnodeConfiguration(final SubnodeConfiguration subnodeConfiguration)
	{
		this.subnodeConfiguration = subnodeConfiguration;
	}
	
	
	@Override
	public String toString()
	{
		return clazz.getSimpleName();
	}
	
	
	/**
	 * @return if the module should be started
	 */
	public boolean isStartModule()
	{
		return startModule;
	}
	
	
	/**
	 * @param startModule whether to start this module
	 */
	public void setStartModule(final boolean startModule)
	{
		this.startModule = startModule;
	}
}
