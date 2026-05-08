package edu.tigers.sumatra.bot.params;


/**
 * @author AndreR <andre@ryll.cc>
 */
public interface IBotMovementLimits
{
	
	/**
	 * @return the velMax
	 */
	double getVelMax();
	
	
	/**
	 * @return the accMax
	 */
	double getAccMax();
	
	
	/**
	 * @return the brkMax
	 */
	double getBrkMax();


	/**
	 * @return the velMaxW
	 */
	double getVelMaxW();


	/**
	 * @return the accMaxW
	 */
	double getAccMaxW();
}