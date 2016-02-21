# TIGERs Mannheim AutoReferee implementation

# Building the Project
You can either build the application using the Maven buildtool or launch it through Eclipse. Please note that you will need an internet connection to build the project with Maven as it will download additional dependencies from the internet. No matter which way you choose you will always need Java 8. Both the Oracle JDK and the OpenJDK will suffice. The software has been developed and tested under Linux, but should also run on Windows/Mac machines.


The following guides provide detailed instructions on how to install and run the AutoReferee on Ubuntu 14.04:

## Maven

1. Installing OpenJDK 8
	
	The OpenJDK 8 has not been offically backported to Ubuntu 14.04 LTS and cannot be installed from the default repositories. The following link provides a detailed guide on how to install OpenJDK 8 from a PPA repository: [How to Install OpenJDK 8 in Ubuntu 14.04 & 12.04 LTS](http://ubuntuhandbook.org/index.php/2015/01/install-openjdk-8-ubuntu-14-04-12-04-lts/)

	Please make sure that the **java** as well as the **javac** commands point to the correct Java version by manually setting the default if you have not already done so in the installation guide above:
	
	```
	sudo update-alternatives --config java
	sudo update-alternatives --config javac
	```

2. Installing Maven

	Installing Maven from the official repository is recommended as the **mvn** command will be available without further actions.
	
	```
	sudo apt-get install maven
	```

3. You can the proceed to cloning the repository:

	```
	git clone http://gitlab.tigers-mannheim.de/open-source/AutoReferee.git
	```
4. Change directory into the repository and issue the following command to build the project:

	```
	mvn install -DskipTests -Dmaven.javadoc.skip=true
	```
	
	This step can take some time if this is the first time that Maven is used on the machine as Maven will populate its local repository with all the dependencies it requires to perform the build steps.
5. Run the `run.sh` script to start the application

### Possible issues:
- If the build fails with the following error please verify that you have correctly set up Java 8.

	```
	Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.3:compile (default-compile) on project common: Fatal error compiling: invalid target release: 1.8 -> [Help 1]
	```

## Eclipse

1. Install Java 8 as explained in the first step of the Maven guide above
2. Clone the AutoReferee repository into a directory of your choosing:

	```
	git clone http://gitlab.tigers-mannheim.de/open-source/AutoReferee.git
	```

3. Download Eclipse with EGit/Maven support (The Java EE edition already contains all necessary plugins): [Eclipse Downloads](http://www.eclipse.org/downloads/)
4. Extract and launch Eclipse
5. Eclipse will only detect the Java version that you used to run it with. If you did not set up your system to use Java 8 as default then you will have to add the Java 8 JRE manually. See this link for more details: [Adding a new JRE definition](http://help.eclipse.org/mars/topic/org.eclipse.jdt.doc.user/tasks/task-add_new_jre.htm)
6. Import the projects:
	- Open the Import dialogue under `File->Import...`
	- Choose the entry `General->Existing Projects into Workspace`
	- Select the repository directory as `root directory` and make sure that `Search for nested projects` is ticked.
	- Select all listed projects and hit Finish
7. Eclipse will now import all projects and instruct Maven to download all necessary dependencies. This can take some time.
8. You can now launch the application by opening the drop down menu next to the green play button and selecting the `AutoReferee` entry.

	If Eclipse did not automatically pick up the launch configuration and the drop down menu is empty you can also launch the project by expanding the **autoreferee-main** project, right-clicking on the `AutoReferee.launch` file and selecting `Run as -> AutoReferee`

### Possible issues:
- Eclipse will automatically compile the **.proto** files in the **moduli-cam** and **moduli-referee** projects. If you encounter errors in these projects try to refresh the configuration by selecting all projects in the Package Explorer, right clicking and choosing `Maven->Update Project..` Select `OK` in the dialogue that pops up.

	The Java files which the protobuf compiler generates do not comply with the strict compiler settings for the projects. This is why the classpath folders that contain the generated protobuf files are marked to ignore optional compiler errors. These settings are overriden by Eclipse when updating the projects from the Maven configuration. If Eclipse complains about compile errors in these classpath folders check if the **.classpath** file has been modified and **checkout** possible changes. Refresh the projects afterwards.


# Mode of operation
The autoref software has been designed to work as an extension of the refbox. It stores as few state about the game as possible. It employs a state machine that is driven by the received referee messages to determine the state of the current game. Depending on the state of the game different rules are evaluated with each vision frame. As a result of the evaluation each rule can specify a new command to be sent to the refbox or a rule violation that occurred on the field or set a new follow-up action. A follow-up action determines what action is to be taken after the game has returned to the Stopped state. Possible actions are: Kick-Off, Direct/Indirect Freekick. The follow-up action represents the only state information that is stored internally by the referee.
All rules are stored in the **edu.tigers.autoreferee.engine.rules.impl** package and possible subpackages of the **moduli-autoreferee** project. The referee is currently capable of detecting the following rule infringements:

- Defender to Ball distance during a freekick
- Indirect Goals
- 10s Timeout during a freekick/kick-off situation
- Attacker to Defense Area distance during a freekick
- Attacker touching the ball inside the defense area of the opponent
- Throw-ins/Goalkicks/Corner
- Ball velocity
- Number of bots on the field
- Bot speed during STOP
- Double Touch
- Dribbling

# Usage

## Configuration
All configuration options are available via the **Cfg** tab. The **Cfg** tab itself contains multiple tabs to modify parameters of different parts of the application. Before you can modify any parameter you need to hit the **Load** button. You can then make the necessary modifications and **Apply** them and/or **Save** them to disk.

### Vision port
The application will try to receive vision frames from **224.5.23.2:10006** by default. If you want to change this behavior navigate to the **user** section and select `edu.tigers.sumatra->cam->SSLVisionCam`. You can change the address or the port(ROBOCUP).

### Refbox & autoref parameter
The behavior of the AutoReferee can be altered through the **autoreferee** config section. All import configuration parameters are grouped under the `edu.tigers.autoreferee->AutoRefConfig` section. Before you hit the **Start** button make sure that the refbox hostname and port are configured correctly. They point to **localhost:10007** by default.

## AutoReferee
The autoref software has been designed to work as an extension of the refbox. It will automatically try to keep the game alive once you initiate an action(kickoff/direct/indirect). Rule infringements are currently logged directly to the LogView in the bottom right corner of the main window.
