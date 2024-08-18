// Set the mvnw file as executable
def file = new File(request.getOutputDirectory(), request.getArtifactId() + "/mvnw")
file.setExecutable(true, false)

// Rename .gitignore.template to .gitignore
file = new File( request.getOutputDirectory(), request.getArtifactId()+"/.gitignore.template" );
def gitIgnorefile = new File( request.getOutputDirectory(), request.getArtifactId()+"/.gitignore" );
file.renameTo(gitIgnorefile)
