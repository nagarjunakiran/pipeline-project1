def build(def workspace)
{
	try
	{
		dir("${workspace}\\$env.builddirectory")
		{
			msBuildCmd = "msbuild publish.proj /p:Configuration=Release \
			/p:DeployOnBuild=true \
			/p:DeployTarget=WebPublish \
			/p:Rebuild=Rebuild \
			/p:PublishFolder=\"${workspace}\\$env.publishfolder\""

			bat msBuildCmd
		}	
	}
	catch(Exception e)
	{
		echo "-------Failed to Build--------------"
		error e.message
	}
	finally
	{
	
	}
}
return this;