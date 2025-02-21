package repository;

import burp.api.montoya.http.Http;
import client.GitHubClient;
import data.bcheck.BCheck;
import file.finder.FileFinder;
import file.temp.TempFileCreator;
import file.zip.ZipExtractor;
import logging.Logger;
import network.RequestSender;
import settings.controller.SettingsController;

public class RepositoryFacadeFactory {

    public static Repository<BCheck> from(Logger logger, Http http, SettingsController settingsController) {
        RequestSender requestSender = new RequestSender(http, logger);
        GitHubClient gitHubClient = new GitHubClient(requestSender);
        TempFileCreator tempFileCreator = new TempFileCreator(logger);
        ZipExtractor zipExtractor = new ZipExtractor(logger);
        FileFinder bCheckFileFinder = new FileFinder();

        GitHubRepository gitHubRepository = new GitHubRepository(
                gitHubClient,
                tempFileCreator,
                zipExtractor,
                bCheckFileFinder,
                settingsController.gitHubSettings()
        );

        FileSystemRepository fileSystemRepository = new FileSystemRepository(
                settingsController.fileSystemRepositorySettings(),
                bCheckFileFinder,
                logger
        );

        return new RepositoryFacade(
                settingsController.repositorySettings(),
                gitHubRepository,
                fileSystemRepository
        );
    }
}
