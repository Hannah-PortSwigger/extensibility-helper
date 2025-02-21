package repository;

import client.GitHubClient;
import data.ItemFactory;
import data.bcheck.BCheck;
import file.finder.FileFinder;
import file.temp.TempFileCreator;
import file.zip.ZipExtractor;
import settings.repository.github.GitHubSettingsReader;

import java.nio.file.Path;
import java.util.List;

public class GitHubRepository implements Repository<BCheck> {
    private final GitHubClient gitHubClient;
    private final TempFileCreator tempFileCreator;
    private final ZipExtractor zipExtractor;
    private final FileFinder bCheckFileFinder;
    private final GitHubSettingsReader gitHubSettings;

    public GitHubRepository(
            GitHubClient gitHubClient,
            TempFileCreator tempFileCreator,
            ZipExtractor zipExtractor,
            FileFinder bCheckFileFinder,
            GitHubSettingsReader gitHubSettings
    ) {
        this.gitHubClient = gitHubClient;
        this.tempFileCreator = tempFileCreator;
        this.zipExtractor = zipExtractor;
        this.bCheckFileFinder = bCheckFileFinder;
        this.gitHubSettings = gitHubSettings;
    }

    @Override
    public List<BCheck> loadAllItems(ItemFactory<BCheck> itemFactory) {
        Path bCheckDownloadLocation = tempFileCreator.createTempDirectory("bcheck-store");
        byte[] bChecksAsZip = gitHubClient.downloadRepoAsZip(
                gitHubSettings.repositoryUrl(),
                gitHubSettings.repositoryName(),
                gitHubSettings.apiKey()
        );

        zipExtractor.extractZip(bChecksAsZip, bCheckDownloadLocation);

        return bCheckFileFinder.find(bCheckDownloadLocation, BCheck.FILE_EXTENSION)
                .stream()
                .map(itemFactory::fromFile)
                .toList();
    }
}