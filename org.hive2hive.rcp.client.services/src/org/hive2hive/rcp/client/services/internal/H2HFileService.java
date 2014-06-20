package org.hive2hive.rcp.client.services.internal;

import java.io.File;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.rcp.client.model.filetree.AccessRight;
import org.hive2hive.rcp.client.services.IFileService;
import org.hive2hive.rcp.client.services.IModelService;
import org.hive2hive.rcp.client.services.INetworkConnectionService;
import org.hive2hive.rcp.client.services.internal.process.ProcessFailureListener;
import org.hive2hive.rcp.client.services.internal.process.file.FetchFileTreeStep;
import org.hive2hive.rcp.client.services.internal.process.file.FetchFileVersionsStep;
import org.hive2hive.rcp.client.services.internal.process.file.ShareWithUserStep;

public class H2HFileService extends H2HService implements IFileService {

	private IEventBroker eventBroker;

	@Override
	public void initFileService(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
	}

	@Override
	public void updateFileTreeOfUser() {
		SequentialProcess process = new SequentialProcess();
		process.add(new FetchFileTreeStep(getFileManager(), getService(IModelService.class), eventBroker));
		runProcessAsynchronously(process);
	}

	@Override
	public void shareWithUser(String userId, File file, AccessRight accessRight) {
		SequentialProcess process = new SequentialProcess();
		process.add(new ShareWithUserStep(userId, file, accessRight, getFileManager(), eventBroker));
		process.add(new FetchFileTreeStep(getFileManager(), getService(IModelService.class), eventBroker));
		process.attachListener(new ProcessFailureListener("Failure in file sharing", String.format(
				"The sharing of the file '%s' with user '%s' could not be completed for the following reason:",
				file.getName(), userId), eventBroker));
		runProcessAsynchronously(process);
	}

	@Override
	public void getFileVersions(File file) {
		SequentialProcess process = new SequentialProcess();
		process.add(new FetchFileVersionsStep(file, getFileManager(), getService(IModelService.class), eventBroker));
		process.attachListener(new ProcessFailureListener(
				"Failure in fetching of file versions.",
				String.format(
						"A failure occurred while trying to fetch version informations for the file '{}'. The failure occurred for the following reason:",
						file.getName()), eventBroker));
		runProcessAsynchronously(process);
	}

	private IFileManager getFileManager() {
		INetworkConnectionService networkConnectionService = getService(INetworkConnectionService.class);
		return networkConnectionService.getCurrentNode().getFileManager();
	}

}
