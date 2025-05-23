package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Persistence;
import data.bambda.*;
import data.bcheck.*;
import logging.Logger;
import repository.RepositoryFacadeFactory;
import settings.controller.SettingsController;
import ui.view.Store;
import ui.view.pane.settings.Settings;
import ui.view.pane.storefront.Storefront;
import ui.view.pane.storefront.StorefrontFactory;
import utils.Capabilities;
import utils.CloseablePooledExecutor;

import javax.swing.*;

import static data.ItemMetadata.BAMBDA;
import static data.ItemMetadata.BCHECK;
import static utils.Capabilities.Capability.BAMBDA_IMPORT;

@SuppressWarnings("unused")
public class Extension implements BurpExtension {
    private static final String EXTENSION_NAME = "Extensibility Helper";

    @Override
    public void initialize(MontoyaApi api) {
        Persistence persistence = api.persistence();

        Capabilities capabilities = new Capabilities(api.burpSuite().version());

        SettingsController settingsController = new SettingsController(persistence.preferences());

        Logger logger = new Logger(api.logging(), settingsController.debugSettings());

        CloseablePooledExecutor executor = new CloseablePooledExecutor();

        StorefrontFactory storefrontFactory = new StorefrontFactory(logger, api.userInterface(), executor);
        RepositoryFacadeFactory repositoryFacadeFactory = new RepositoryFacadeFactory(logger, api.http());

        Storefront<BCheck> bCheckStorefront = storefrontFactory.build(
                "BCheck Store",
                new BCheckFilter(),
                repositoryFacadeFactory.build(
                        settingsController.bCheckSettingsController(),
                        new BCheckFactory(logger),
                        BCHECK),
                new BCheckItemImporter(api.scanner().bChecks(), logger),
                settingsController.bCheckSettingsController(),
                BCHECK,
                new BCheckTagColors()
        );

        BambdaItemImporter bambdaItemImporter = capabilities.hasCapability(BAMBDA_IMPORT)
                ? new BambdaItemImporter(logger, api.bambda())
                : new BambdaItemImporter(logger, null);

        Storefront<Bambda> bambdaStorefront = storefrontFactory.build(
                "Bambda Store",
                new BambdaFilter(),
                repositoryFacadeFactory.build(
                        settingsController.bambdaSettingsController(),
                        new BambdaFactory(logger),
                        BAMBDA
                ),
                bambdaItemImporter,
                settingsController.bambdaSettingsController(),
                BAMBDA,
                new BambdaTagColors()
        );

        JComponent settings = new Settings(settingsController);

        Store store = new Store(settings, bCheckStorefront, bambdaStorefront);

        api.extension().setName(EXTENSION_NAME);
        api.userInterface().registerSuiteTab(EXTENSION_NAME, store);
        api.extension().registerUnloadingHandler(executor::close);
    }
}
