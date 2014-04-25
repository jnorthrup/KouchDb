import kouchdb.PrautoSpector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by jim on 4/25/14.
 */
@Mojo(name="prautobean",defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class PrautoGen extends AbstractMojo
{

    @Parameter(defaultValue = "src/main/proto")
    private File protoDir;

    @Parameter(defaultValue = "target/generated-sources")
    private File javaOutputDir;
    @Parameter(defaultValue = "target/cxx-generated-sources")
    private File cxxOutputDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            PrautoSpector.main(protoDir.getAbsolutePath(), javaOutputDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
