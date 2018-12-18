# GRP

نظام ELM GRP هو نظام متكامل ومترابط يحتوي على جميع الإجراءات المتعلقة بالقطاع الحكومي كوظائف أساسية في النظام منفردا عن أنظمة تخطيط الموارد الأخرى. ولقد تم تصميمه من قبل شركة علم الرائدة في صناعة البرمجيات للقطاع الحكومي  .
يلبي نظام ELM GRP معظم مهام وإجراءات العمل المتعددة في مختلف القطاعات الحكومية مثل:
*	النظام المالي والميزانية: يتضمن إجراءات الميزانية، انشاء الارتباطات والمناقلات، تصدير أوامر الصرف والدفع واوامر القبض، الاستعلامات المالية، والتقارير المالية.
*	نظام الموارد البشرية: يتضمن إجراءات الهيكل التنظيمي والوظيفي، قرار التعيين والقرارات الحكومية (اجازات، انتداب، خارج دوام وجميع الإجراءات الحكومية الأخرى)، نظام الرواتب، نظام الخدمة الذاتية، ونظام تقييم الآداء الوظيفي.
*	نظام المشتريات والمخزون والعهد: يتضمن إجراءات المشتريات الحكومية (طلب شراء، عروض أسعار، فتح مظاريف، اوامر شراء (مباشر، منافسات)، نظام المخزون (تعريف المستودعات وهيكلتها، الأصناف، حركات المستودعات وعمليات الجرد، ونظام العهد العينة للموظفين.


ELM GRP is an integrated and coherent system contains all actions related to the government sector as core functions of the system apart from other resource planning systems. It has been designed by ELM, the leading company in the software industry for the government sector  
The ELM GRP system fulfills most of the various functions and actions in different government sectors such as:
* Financial System and Budget: Includes budget procedures, establishment of budget encumbrance and transfers, Invoices orders (Amer Serf), payment and order to receipts, financial inquiries, and financial reports.
* Human Resources System: includes organizational and functional structure procedures, Hiring decision and government decisions (vacations, Business Mission, Overtime and all other government procedures), payroll system, self-service system and employee performance appraisal system.
* Procurement, inventory and Custodies: Includes government procurement procedures (purchase order, quotations, open envelopes, direct purchase orders, competitions), inventory system (warehouse definition and structure, items, warehouse movements and inventories Operations, and employee custodies



## Getting Started

This document is intended to be followed to set up the development environment for Openbravo. This document should NOT be considered for production use of Openbravo. Moreover, this guide assumes that the OS the developers are using is Windows.

### Prerequisites

You need to install the following tools to run the software:

* [PostgreSQL](https://www.postgresql.org/).
* [JAVA JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
* [Apache Tomcat](http://tomcat.apache.org/download-70.cgi).
* [Apache Ant](http://ant.apache.org/bindownload.cgi).
* [Mercurial Clone](http://tortoisehg.sourceforge.net/).

### Installing

Go through the following steps carefully to succeed installation.

#### PostgreSQL
if you download version 9.3 or higher, you must follow the following steps. Otherwise you can skip this part. In order to avoid this problem and continue considering the period as the database decimal separator, the lc_numeric setting should be configured as follows, in postgresql.conf file:

```
lc_numeric = 'English_United States.1252' 
```
#### Java JDK
Download and install [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Then set up the JAVA_HOME Environment Variable to point to the installation directory. Here are the steps to do so for reference:
1.  Right-click on My Computer.
2.  Select Properties. The System Properties window appears.
3.  Select the Advanced tab then click Environment Variables.
4.  Click New. The New System Variable box appears.
5.  In the Variable name field, type `JAVA_HOME`.
6.  In the Variable value field type the path of the JDK installation directory. This is usually `C:\Program Files\Java\jdk1.8.0_<version>`.
7.  Select the PATH environment variable.
8.  In the Variable value field you type the following `%JAVA_HOME%\bin`. This will reuse the `JAVA_HOME` variable we just defined.
The next 2 commands should both work fine and show the version of java you have just installed.

```
java -version
javac -version
```
The next command should work and show the path in which you have installed Java.
```
echo %JAVA_HOME%
```

#### Apache Tomcat
1.  Download and install [Apache Tomcat](http://tomcat.apache.org/download-70.cgi).
2.  The download you want is the `32-bit/64-bit` Windows Service Installer, as that will integrate it as Windows Service.
    *  Run that installer.
    *  It will ask if it should install several optional components (documentation, manager application, host manager application, example application). GRP does not need any of those they should not be marked.
    *  For the port configuration the rest of the guide assumes the standard ports `8005, 8080, 8009`.
3.  Add `CATALINA_HOME`, `CATALINA_BASE` and `CATALINA_OPTS` environment variables:
    *  Right-click on My Computer.
    *  Select Properties. The system properties window appears.
    *  Select the Advanced tab.
    *  Click Environment Variables.
    *  Click New. The New System Variable box appears.
    *  In the Variable name field, type `CATALINA_HOME`.
    *  In the Variable value field, type the path of the Apache Tomcat installation directory. This is usually `C:\Apache Software Foundation\Tomcat 7.0`.
    *  Click OK.
    *  Click New. The New System Variable box appears.
    * In the Variable name field, type `CATALINA_BASE`.
    * In the Variable value field type `%CATALINA_HOME%`.
    * Click OK
    * Click New. The New System Variable box appears.
    * In the Variable name field, type `CATALINA_OPTS`.
    * In the Variable value type `-server -Djava.awt.headless=true -Xms384M -Xmx1024M`.
4.  Configure values in the tomcat service manager application. After the installation there will be a new icon in the windows `System Tray` which can be used to `start / stop` the tomcat service and also configure it.
    *  In the configure window, go to the Java tab
        *  Add `-server -Djava.awt.headless=true` to the end of the Java Options
        *  Change the Initial Memory Pool to `384M`
        *  Change the Maximum Memory Pool to `1024M`

As you will have noticed that item 4 above makes sure that the settings just done now match the ones done via the `CATALINA_OPTS` variable. If you change any one of the two later it is important to edit then in both places.
As development use here is meant a simple installation of tomcat without another Http server like Apache in front of it. In addition to the global config above the following changes should be done.
1.  In the `config/server.xml` file of tomcat comment out the Connector entry for AJP on port 8009 as that is not useful without a Apache server in front of it.
2.  For the Http Connector on port 8080 add the following attribute to it:
`URIEncoding="UTF-8"`
3.  Optionally disable the tomcat access log if you do not want to have a access log file with one line for every http request. For this comment out the following line:
```
<!-- <Valve className="org.apache.catalina.valves.AccessLogValve" 
pattern="%h %l %u %t "%r" %s %b" suffix=".txt" prefix="localhost_access_log." directory="logs"/> -->
```

#### Apache Ant
1.  Download and extract [Apache Ant](http://ant.apache.org/bindownload.cgi) onto your system (for instance, `C:\Apache-Ant`).
2.  Add ANT_HOME environment variable:
1.  Right-click on My Computer.
2.  Select Properties. The System Properties window appears.
3.  Select the Advanced tab.
4.  Click on the Environment Variables button.
5.  Click the New button.
6.  In the Variable name field, type `ANT_HOME`.
7.  In the Variable value field, type the path of the Ant directory. This is usually `C:\Apache-Ant`.
8.  Click OK.
9.  Select the PATH environment variable.
10. In the Variable value field type `%ANT_HOME%\bin`.
3.  Equally add the `ANT_OPTS` environment variable:
1.  Click the New button.
2.  In the Variable name field, type `ANT_OPTS`.
3.  In the Variable value field, type `-Xmx1024M`.
How to test the installation
In Command Prompt (cmd.exe) run all of the following commands and verify their output.
```
echo %ANT_OPTS%
ant -version
```
Both command should work without errors and the output should be the ant options you just defined above and the ant version you have installed.

### Mercurial Clone
To be able to check out Openbravo source code from [Openbravo Mercurial repository](http://code.openbravo.com/) you need to first install the [Mercurial client](http://wiki.openbravo.com/wiki/Mercurial_Manual_for_Openbravo_Developers#Installation). There are 2 options:
1.  Use the command line version and install it using a [prepackaged binary installer](http://mercurial.berkwood.com/).
2.  [TortoiseHg](http://tortoisehg.sourceforge.net/): an all-inclusive Mercurial binary installer package for Windows, which provides a windows explorer extension (shell extension), so that Mercurial commands can be executed from the context menu of the explorer.
Once the Mercurial client is installed, move to the directory to which you want to check out the source code.
To check out the latest source code tag, type the command:
```
$ hg clone http://code.openbravo.com/erp/devel/main OpenbravoERP-3.0PR16Q1.1
$ cd OpenbravoERP-3.0PR16Q1.1
$ hg up 3.0PR16Q1.1
```

# Eclipse IDE & Openbravo Installation
By now, all Environment variables (e.g. `ANT_OPTS, CATALINA_OPTS, JAVA_HOME, ANT_OPTS, etc`) should already be declared in the proper categories. This is the core of this guide so it will be covered in more detail. Assuming that you have a copy of the source code along with a newer release of Eclipse IDE, please do the following steps:

## Configure the properties
So, first of all you have to configure the Openbravo ERP by specifying some general properties, e.g. Tomcat installation directory, database connection details, etc. This can (and in fact must) be done via a console application which has to be compile first. For that, go to the directory with the working clone of the repository and execute:
```
ant setup
```
The invocation of this ant target will compile and execute automatically a console application. By going through the console application provide all requested information and at the end, select Accept to apply the changes and close the application.


## Install from sources
In order to install the Openbravo ERP three main procedures have to be accomplished:
*   the Openbravo ERP database has to be created and populated with some initial values.
*   sources have to be generated.
*   all sources have to compiled to binaries which later can be executed on a WEB server (Tomcat).
All this is done by invoking from the root of the working clone `(XXX\opensource\openbravo\erp\devel\main\)`.
```
ant install.source
```
This process can take quite long time (up to 25 min) depending on hardware configuration. It's always a good idea to redirect the output of the task execution to a log file which then can be analyzed or sent to the support team in case of problems. After the task has completed the log should not contain any error or exception massages as well as it should have `BUILD SUCCESSFUL` message at the end of the file. (if you are under Linux use grep command to check whether the file contains any exceptions). After successful installation the next step can be taken - importing to Eclipse IDE.

## Problems running install.source
If you find problems running `install.source`, check the [Installation/Troubleshooting](http://wiki.openbravo.com/wiki/Installation/Troubleshooting) article.

## Import into Eclipse IDE
Launch Eclipse.
    `After Eclipse has started go to Project menu and disable Build Automatically option there.`
Now 4 projects need to be imported in the workspace (by menu `File=>Import and then General=>Existing Projects into Workspace`). Here they are:
```
openbravo        XXX\opensource\openbravo\erp\devel\main\
OpenbravoCore    XXX\opensource\openbravo\erp\devel\main\src-core
OpenbravoTrl     XXX\opensource\openbravo\erp\devel\main\src-trl
OpenbravoWAD     XXX\opensource\openbravo\erp\devel\main\src-wad
```
## Create the Tomcat Server
Then open `Servers` view and create a new instance of Tomcat server:

![New Server](https://serving.photos.photobox.com/03058248b63dbad3b8e9f4c623ef176df18725b272c2fb97e6d67cf89fab4805615f55c2.jpg)

While going through the wizard select openbravo and add it to configured resources. Then click "Finish". The created instance should appear in the view. Double click on it to change its settings in the form depicted on the picture below:

![Server Properties](https://serving.photos.photobox.com/06259015dfc0525259f392f781a9bac6944d397a43cdf6cd620ac5434dacf12ff2bb6991.jpg)

## Change VM arguments settings
-   in **General Information**
    -   click on **Open launch configuration** link
    -   switch to **Arguments** tab
    -   add the following line at the end of **VM arguments** input:
```
-server -Djava.awt.headless=true -Xms384M -Xmx1536M -XX:MaxPermSize=256M.
 ```
-   in **Server options**
    -   check the **Serve modules without publishing**.
-   in **Timeouts**
    -   set **Start** and **Stop** timeouts to `120` seconds
After changing all, save your server configuration (press Ctrl+S).

## Import Preferences
The next step is to set the standard preferences used in the development of Openbravo.
-   The preference file is located in the `openbravo/config/eclipse` folder in the development project
-   Import workspace preferences clicking on `File > Import`, then select `General > Preferences` and click on **Next** button.

![import preferences](https://serving.photos.photobox.com/76764117ca767189a81eeb21f03d329495234da2dd0803674359c764b7c19b9a24b31e8c.jpg)

-   Browse to `openbravo/config/eclipse/openbravo-eclipse-prefs.epf` file, select Import all radio button and click on **Finish** button.

![Preferences](https://serving.photos.photobox.com/942364387ed3d6d7d129cae51b4ceda0906cb5cefe9b0d3c5e1c298986b18a6b6ae53c44.jpg)

Once finished, select all the projects, refresh them, and rebuild them (right click on one or more projects and you will find the Refresh and the Rebuild options). You should get warnings, but not errors.

## Launch from Eclipse
Now start the Tomcat server by right-clicking on the server instance in the **Servers** view and choosing **Start** option in the popup menu. Wait until the server is started (can take up to 2 minutes) and visit `http://localhost:8080/openbravo/` in your internet browser. If everything was configured and installed properly you will get to the Openbravo ERP log in page. Use these credentials to log in:
```
•   username: Openbravo
•   password: openbravo
```
Both are **case sensitive.**

### That's it, the installation is over and you're ready to start developing.
