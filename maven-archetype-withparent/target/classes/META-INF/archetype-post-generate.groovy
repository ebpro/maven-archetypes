file = new File( request.getOutputDirectory(), request.getArtifactId()+"/.gitignore.template" );
def gitIgnorefile = new File( request.getOutputDirectory(), request.getArtifactId()+"/.gitignore" );
file.renameTo(gitIgnorefile)
