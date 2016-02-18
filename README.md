# TIGERs Mannheim AutoReferee implementation

## Building the Project
You can either build the application using the Maven buildtool or launch it through Eclipse. No matter which way you choose you will always need Java 8. Both the Oracle JDK and the OpenJDK will suffice. The software has been developed and tested under Linux, but should also run on Windows/Mac machines.

The two procedures to launch the application are explained below.

### Maven

Requirements:
- Maven 3 (Repository or manual installations are fine)
- Java 8 (OpenJDK/Oracle)

Procedure:
- Make sure that Java 8 is picked up by your shell and the `mvn` command is available
- Clone the repository
- Change directory into the repository and issue the following command to build the project: `mvn install -DskipTests -Dmaven.javadoc.skip=true`
- Run the `run.sh` script to start the application

### Eclipse

- Download Eclipse with EGit/Maven support (The Java EE edition already contains all necessary plugins)
- Clone the repository into a directory of your choosing
- Start Eclipse and add the Java 8 JRE to the list of known JREs. See this link for more details: [Adding a new JRE definition](http://help.eclipse.org/mars/topic/org.eclipse.jdt.doc.user/tasks/task-add_new_jre.htm)
- Open the Import dialogue under `File->Import...`
- Choose the entry `General->Existing Projects into Workspace`
- Select the repository directory and make sure that `Search for nested projects` is ticked.
- Select all listed projects and hit Finish
- Eclipse will now import all projects and instruct Maven to download all necessary dependencies. This can take some time.
- You can now launch the application by opening the drop down menu next to the green play button and selecting the `AutoReferee` entry. If Eclipse did not automatically pick up the launch configuration and the drop down menu is empty you can also launch the project by expanding the **autoreferee-main** project, right-clicking on the `AutoReferee.launch` file and selecting `Run as -> AutoReferee`

## Mode of operation
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

## Usage

### Configuration
All configuration options are available via the **Cfg** tab. The **Cfg** tab itself contains multiple tabs to modify parameters of different parts of the application. Before you can modify any parameter you need to hit the **Load** button. You can then make the necessary modifications and **Apply** them and/or **Save** them to disk.

#### Vision port
The application will try to receive vision frames from **224.5.23.2:10006** by default. If you want to change this behavior navigate to the **user** section and select `edu.tigers.sumatra->cam->SSLVisionCam`. You can change the address or the port(ROBOCUP).

#### Refbox & autoref parameter
The behavior of the AutoReferee can be altered through the **autoreferee** config section. All import configuration parameters are grouped under the `edu.tigers.autoreferee->AutoRefConfig` section. Before you hit the **Start** button make sure that the refbox hostname and port are configured correctly. They point to **localhost:10007** by default.

### AutoReferee
The autoref software has been designed to work as an extension of the refbox. It will automatically try to keep the game alive once you initiate an action(kickoff/direct/indirect). Rule infringements are currently logged directly to the LogView in the bottom right corner of the main window.
