package hudson.plugins.plot;

import java.io.IOException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

/**
 * The Descriptor for the plot configuration Extension
 *
 * @author Nigel Daley
 * @author Thomas Fox
 */
@Extension
public class PlotDescriptor extends BuildStepDescriptor<Publisher> {

    public PlotDescriptor() {
        super(PlotPublisher.class);
    }

    @NonNull
    public String getDisplayName() {
        return Messages.Plot_Publisher_DisplayName();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return AbstractProject.class.isAssignableFrom(jobType)
                && !MatrixProject.class.isAssignableFrom(jobType);
    }

    /**
     * Called when the user saves the project configuration.
     */
    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData)
            throws FormException {
        PlotPublisher publisher = new PlotPublisher();
        for (Object data : SeriesTransformUtil.getArray(formData.get("plots"))) {
            publisher.addPlot(bindPlot((JSONObject) data, req));
        }
        return publisher;
    }

    private static Plot bindPlot(JSONObject data, StaplerRequest req) {
        Plot p = req.bindJSON(Plot.class, data);
        p.series = SeriesTransformUtil.createSeriesList(data.get("series"), req);
        return p;
    }

    /**
     * Checks if the series file is valid.
     */
    public FormValidation doCheckSeriesFile(
            @AncestorInPath AbstractProject<?, ?> project,
            @QueryParameter String value) throws IOException {
        // we don't have a workspace while in Pipeline editor
        if (project == null || project.getRootDir() == null) {
            return FormValidation.ok();
        }
        return FilePath.validateFileMask(project.getSomeWorkspace(), value);
    }
}
