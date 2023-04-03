package com.vladyslavholik.stm;

import org.multiverse.api.StmUtils;
import org.multiverse.api.references.TxnInteger;

public class Product {
    private final Integer id;
    private final TxnInteger soldItems;

    Product(Integer id, int soldItems) {
        this.id = id;
        this.soldItems = StmUtils.newTxnInteger(soldItems);
    }

    public Integer getId() {
        return id;
    }

    public TxnInteger getSoldItems() {
        return soldItems;
    }
}
