# TRANSMART_VCF_FILTER_TOOL

The TranSMART VCF filter tool is a web API that enables filtering of VCF data loaded into transmart.  The application ships with a simple Javascript UI to illustrate some of the available API endpoints.

## GETTING STARTED

The TranSMART VCF filter tool ships with a Vagrantfile that has everything needed to build a virtual machine provisioned with the environment necessary to compile the source code.  
To use the VM you need to install
* VirtualBox https://www.virtualbox.org/wiki/Downloads
* Vagrant https://www.vagrantup.com/downloads.html

Once you have installed Virtualbox and Vagrant
* Clone the repository
* Navigate to the repository folder
* Open `src/main/resources/hibernate.cfg.xml` and enter the connection details to your transmart database
* Enter the command `vagrant up`
* After the VM has spun up, enter the command `vagrant ssh`
* Once you are logged into the VM, enter the command `cd ~/TRANSMART_VCF_FILTER_TOOL`
* Compile the source `gradle clean war`
* Exit the Virtual Machine by entering the command `exit`
* The WAR file should be located in <repository-folder>/build/libs
	* Please note that the VM compiles the code using Java 7.
* Deploy the WAR file to your Tomcat installation
* Please see the Documents in the Docs folder for more information on the available endpoints and the expected input formats