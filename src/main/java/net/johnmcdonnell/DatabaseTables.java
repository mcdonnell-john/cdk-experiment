/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.johnmcdonnell;

/**
 *
 * @author John
 */
public enum DatabaseTables {
    PRODUCTS("products","id"),
    REVIEWS("reviews","id"),
    ORDERS("orders","id"),
    INVENTORY("inventory","productId");
    
    private final String tableName;
    private final String primaryKey;
    
    DatabaseTables(String tableName, String primaryKey) {
        this.tableName = tableName;
        this.primaryKey = primaryKey;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }
    
    
}
