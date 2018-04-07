def vsSlnPath = 'Radian Web Applications\\MI Online\\web\\MIOnline.Web.sln'
def workspaceDir = "D:\\jenkins\\MIOnline_Work"
checkoutCredentials= 'b075b292-3f9f-4cf7-857b-5c186a1d5cbd'
def branch
def msBuildNuGetPath
def webSlnPath
def msBuildNuGetCmd
def npmPath
def msBuildPath
Properties props
File propertiesFile
def majorversion
def minorversion
def DBKey
def artifactfolder

projectUrl= 'ssh://git@radcab:2222/mionline-web/radian-applications.git'

node('windows')
{
	env.SonarQube = tool 'SonarQube'
	env.javaHome = tool 'Java'
	env.SonarMsBuild =  tool 'SonarQubeMSBuild'
	
	try
	{
		dir("$workspaceDir")
		{
			stage('Checkout')
			{
				checkoutsteps = load "${workspaceDir}\\Devops\\Classes\\Checkout.groovy"
				checkoutsteps.checkout("${env.Branch}","${checkoutCredentials}","${projectUrl}")
			}
			stage('Read Properties')
			{
				propertiesread = load "${workspaceDir}\\Devops\\Classes\\ReadPropertiesFile.groovy"
				globalpropertiesfilepath = "${workspaceDir}\\Devops\\global.properties"
				propertiesread.callreadPropertiesMIOnline("${workspaceDir}","${globalpropertiesfilepath}")
			}
			stage('Versioning')
			{
				getrevision = load "${workspaceDir}\\Devops\\Classes\\Commonmethods.groovy"
				getrevision.getRevisionID("${workspaceDir}")
				versioning = load "${workspaceDir}\\Devops\\Classes\\Versioning.groovy"
				versioning.changeVersion("${workspaceDir}")
				versioning.changeVersionCS("${workspaceDir}")
				versioning.folderVersion("${workspaceDir}")
				
			}
			stage('Deployment Properties Creation')
			{
				deploymentfile = load "${workspaceDir}\\Devops\\Classes\\DeploymentPropertiesCreation.groovy"
				deploymentfile.createDeploymentProperties("${workspaceDir}","Devops\\DeploymentLogs\\${env.Environment}\\Deploymentdetails.properties")
			}
			stage('Build Nugget')
			{
				nuggetbuild = load "${workspaceDir}\\Devops\\Classes\\NuggetBuild.groovy"
				nuggetbuild.buildNugget("${workspaceDir}")
			}
			stage('Npm Install')
			{
				npminstall = load "${workspaceDir}\\Devops\\Classes\\Installnpm.groovy"
				npminstall.npmInstall("${workspaceDir}")
			}
			stage('Build')
			{
				build = load "${workspaceDir}\\Devops\\Classes\\Build.groovy"
				build.build("${workspaceDir}")
			}
			stage('Sonar AngularJS')
			{
				sonarjs = load "${workspaceDir}\\Devops\\Classes\\SonarAngularJS.groovy"
				sonarjs.sonarAngularJS("${workspaceDir}")
			}
			stage('Sonar Analysis C#')
			{
				sonarcsharp = load "${workspaceDir}\\Devops\\Classes\\SonarCSharp.groovy"
				sonarcsharp.sonarAnalysisCSharp("${workspaceDir}","SonarPassword")
			}
			stage('Artifacts Folder Creation')
			{
				artifactfolder = load "${workspaceDir}\\Devops\\Classes\\CreateArtifacts.groovy"
				artifactfolder.createArtifactFolder("${workspaceDir}")
				artifactfolder.copyFilesToArtifacts("${workspaceDir}")
			}
			stage('Transformation')
			{
				transform = load "${workspaceDir}\\Devops\\Classes\\Transformation.groovy"
				transform.transformWebConfig("${workspaceDir}")
				transform.transformPublishXml("${workspaceDir}","DBPassKey")
				transform.transformJson("${workspaceDir}")
				transform.transformAPIWebConfig("${workspaceDir}")
				artifactfolder.zipArtifacts("${workspaceDir}")
			}
			stage('DB Deployment')
			{
				dbdeploy = load "${workspaceDir}\\Devops\\Classes\\DBDeployment.groovy"
				dbdeploy.dbDeployment("${workspaceDir}")
			}
			stage('Deployment')
			{
				deploy = load "${workspaceDir}\\Devops\\Classes\\Deploy.groovy"
				deploy.deployCode("${workspaceDir}","${env.Environment}_ServerPassword")
			}
			
			
			
			
		}//end of main dir
		
	}//end of main try
	catch(Exception e)
	{
		currentBuild.result = 'FAILURE'
		echo e.message
	}
	finally
	{
		email = load "${workspaceDir}\\Devops\\Classes\\Commonmethods.groovy"
		email.emailnotification("${currentBuild.result}")
	}

} //end of node 