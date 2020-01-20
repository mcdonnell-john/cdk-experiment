/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.johnmcdonnell;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;

/**
 *
 * @author John McDonnell
 */
public class DatabaseStack extends Stack {

    private static Table productTable;
    private static Table orderTable;
    private static Table reviewTable;
    private static Table inventoryTable;

    protected static final String PRODUCTS_TABLE_NAME = "products";
    protected static final String ORDERS_TABLE_NAME = "orders";
    protected static final String REVIEWS_TABLE_NAME = "reviews";
    protected static final String INVENTORY_TABLE_NAME = "inventory";

    
    public DatabaseStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public DatabaseStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        initConstructs();
    }

    public static Table getProductTable() {
        return productTable;
    }

    public static Table getOrderTable() {
        return orderTable;
    }

    public static Table getReviewTable() {
        return reviewTable;
    }
    
    public static Table getInventoryTable() {
        return inventoryTable;
    }

    private void initConstructs() {
        productTable = Table.Builder.create(this, PRODUCTS_TABLE_NAME)
                .tableName(PRODUCTS_TABLE_NAME)
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .removalPolicy(RemovalPolicy.DESTROY) 
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        orderTable = Table.Builder.create(this, ORDERS_TABLE_NAME)
                .tableName(ORDERS_TABLE_NAME)
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        reviewTable = Table.Builder.create(this, REVIEWS_TABLE_NAME)
                .tableName(REVIEWS_TABLE_NAME)
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        
        inventoryTable = Table.Builder.create(this, INVENTORY_TABLE_NAME)
                .tableName(INVENTORY_TABLE_NAME)
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
    }
    
    protected static Map<String, String> getDynamoEnvironmentParams(String tableName) {
        return Stream.of(
                new AbstractMap.SimpleEntry<>("TABLE_NAME", tableName),
                new AbstractMap.SimpleEntry<>("PRIMARY_KEY", "id"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
