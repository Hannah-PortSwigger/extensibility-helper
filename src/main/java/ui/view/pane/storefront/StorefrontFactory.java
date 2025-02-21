package ui.view.pane.storefront;

import burp.api.montoya.ui.UserInterface;
import data.Item;
import data.ItemFactory;
import data.ItemFilter;
import data.ItemImporter;
import file.system.FileSystem;
import logging.Logger;
import repository.Repository;
import settings.controller.SettingsController;
import ui.clipboard.ClipboardManager;
import ui.controller.StoreController;
import ui.icons.IconFactory;
import ui.model.DefaultStorefrontModel;
import ui.model.LateInitializationStorefrontModel;
import ui.model.StorefrontModel;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public class StorefrontFactory {
    private final Logger logger;
    private final SettingsController settingsController;
    private final UserInterface userInterface;
    private final Executor executor;
    private final IconFactory iconFactory;
    private final ClipboardManager clipboardManager;
    private final FileSystem fileSystem;

    public StorefrontFactory(
            Logger logger,
            UserInterface userInterface,
            SettingsController settingsController,
            Executor executor) {
        this.logger = logger;
        this.userInterface = userInterface;
        this.settingsController = settingsController;
        this.executor = executor;
        this.iconFactory = new IconFactory(userInterface);
        this.clipboardManager = new ClipboardManager();
        this.fileSystem = new FileSystem(logger);
    }

    public <T extends Item> Storefront<T> build(
            String title, ItemFilter<T> filter,
            Repository<T> repository,
            ItemImporter<T> itemImporter,
            ItemFactory<T> itemFactory) {
        AtomicReference<StorefrontModel<T>> modelReference = new AtomicReference<>();
        StorefrontModel<T> lateInitializationStorefrontModel = new LateInitializationStorefrontModel<>(modelReference::get);

        StoreController<T> storeController = new StoreController<>(
                lateInitializationStorefrontModel,
                repository,
                itemImporter,
                clipboardManager,
                fileSystem,
                filter,
                itemFactory
        );

        StorefrontModel<T> storefrontModel = new DefaultStorefrontModel<>(storeController);
        modelReference.set(storefrontModel);

        return new Storefront<>(
                title,
                storeController,
                storefrontModel,
                settingsController.defaultSaveLocationSettings(),
                executor,
                iconFactory,
                logger,
                userInterface::currentDisplayFont
        );
    }
}
