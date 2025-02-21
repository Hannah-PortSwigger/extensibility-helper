package repository;

import data.Item;
import data.ItemFactory;

import java.util.List;

public interface Repository<T extends Item> {
    List<T> loadAllItems(ItemFactory<T> itemFactory);
}
