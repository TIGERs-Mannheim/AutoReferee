# TIGERs Mannheim AutoReferee

Homepage: https://www.tigers-mannheim.de  
Mail: management@tigers-mannheim.de

This is the AutoReferee implementation of TIGERs Mannheim. It is based on Sumatra, our AI framework.
We use Maven for building the application. You can also use IntelliJ or Eclipse to build and run it. The required configuration files are included.
Dependencies will be downloaded from Maven repositories, so you need an internet connection for the build.

## System Requirements
 * Java JDK 8
 * Maven
 * Internet connection
 * no limitations on OS known

## Build
Configuration for IntelliJ and Eclipse is provided and should work out-of-the-box. Without an IDE, run `./build.sh` or `./build.bat`, depending on your system platform.

## Run from command line
Run `run.sh <mode>` or `run.bat`, where <mode> must be replaced with either `active` or `passive`. For Windows, the
mode must be changed in the script. It is passive by default.

# Usage

## GUI
The GUI is divided into different views that can be arranged dynamically. If a view is not shown, it can be added
from the menu (Views).

### AutoReferee view
In the **AutoReferee** view, you can set the mode of the autoRef to one of:
 * Off: The autoRef is completely off
 * Passive: The autoRef will detect events and show it in the Game Log, but it will not send anything to the game-controller
 * Active: The autoRef will detect events and will also send them to the game-controller

This view also allows to deactivate certain detectors.

### Game Log View
The **Game Log** view displays all events that occurred and have an impact on the behavior of the autoRef. The checkboxes below the table can be used to toggle different event types on or off.

### Ball Speed View
The **Ball Speed** view displays the velocity of the ball on the field. The slider component on the right can be used to alter the time window of the chart. If the **Pause when not RUNNING** checkbox is ticked the chart will automatically pause if the game is not in the RUNNING gamestate. This pause can be overridden by using the Pause/Resume buttons. Please note that the chart will not automatically resume after it has been manually paused by the user if the gamestate transitions back to RUNNING.

The chart will show three lines:
 * Maximum ball speed: The ball speed limit, defined by the rules.
 * initial ball speed: The estimated initial ball speed (kick speed) based on the ball trajectory. It gets more accurate over time.
 * ball speed: The current estimated ball speed

## Configuration
All configuration options are available via the **Cfg** tab. The **Cfg** tab itself contains multiple tabs to modify parameters of different parts of the application. You can then make the necessary modifications and **Apply** them. To make the changes persistent, press **Save** to write them to disk. Press **Reload** to reread all currently applied values.

### Vision port
The application will try to receive vision frames from **224.5.23.2:10006** by default. If you want to change this behavior navigate to the **user** section and select `edu.tigers.sumatra->cam->SSLVisionCam`.

## Referee port
The application will try to receive referee messages from **224.5.23.1:10003** by default. The port can be changed in [config/moduli/moduli.xml](config/moduli/moduli.xml).

### AutoRef detectors
The behavior of the autoRef can be altered through the **autoreferee** config section. It contains parameters for the
detectors.

### Rule constraints and geometry
Parameters defined by the rules can be found under **ruleConst**. 

Default geometry parameters can be found under **geom**. Most of them are overwritten on the first received geometry package from
ssl-vision.  

## Activating the build-in ssl-game-controller
The autoRef ships with the official ssl-game-controller. To activate it, change `gameController` to true in [config/moduli/moduli.xml](config/moduli/moduli.xml). The autoRef will internally launch the game-controller and connect to its websocket API
to be able to send some basic commands through the **Ref** view. This view also contains a button to launch the full
game-controller UI.

## Recording Gameplay
The autoRef can automatically start and stop recordings when a game starts/stops. To enable this feature, have a look in [run.sh](run.sh).
The recording consumes a lot of memory. That's why by default, the heap size is set to 4GB. Make sure that your system has sufficient free memory. The recordings are stored in [data/record](data/record). They can grow to several Gigabyte, as they are stored uncompressed.
Zipping the output files significantly reduces the size.

You can access the replays by right-clicking a row in the GameLog view. This will jump directly to the time, the infringement occurred (30 seconds before). After a recording was closed, it can be accessed from the menu.