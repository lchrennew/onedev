package io.onedev.server.buildspec.job.trigger;

import java.util.List;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=500, name="Dependency job finished")
public class DependencyFinishedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(ProjectEvent event, Job job) {
		if (event instanceof BuildFinished) {
			BuildFinished buildFinished = (BuildFinished) event;
			Build build = buildFinished.getBuild();
			for (JobDependency dependency: job.getJobDependencies()) {
				if (dependency.getJobName().equals(build.getJobName()) 
						&& (!dependency.isRequireSuccessful() || build.getStatus() == Status.SUCCESSFUL)) {
					for (ParamSupply param: dependency.getJobParams()) {
						if (!param.isSecret()) {
							List<String> paramValue = build.getParamMap().get(param.getName());
							if (!param.getValuesProvider().getValues().contains(paramValue))
								return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "When dependency jobs finished";
	}

}
