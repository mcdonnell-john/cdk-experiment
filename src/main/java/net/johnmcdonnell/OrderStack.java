package net.johnmcdonnell;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.JsonSchema;
import software.amazon.awscdk.services.apigateway.JsonSchemaType;
import software.amazon.awscdk.services.apigateway.JsonSchemaVersion;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.Model;
import software.amazon.awscdk.services.apigateway.RequestValidator;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

/**
 *
 * @author John McDonnell
 */
public class OrderStack extends Stack {

    public OrderStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public OrderStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final Map<String, String> dynamoDbEnvironmentParams = DatabaseStack.getDynamoEnvironmentParams(DatabaseStack.ORDERS_TABLE_NAME);

        final Function getOrderFunction = Function.Builder.create(this, "GetOrderItemFunction")
                .functionName("GetOrderItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("getItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function getOrdersFunction = Function.Builder.create(this, "GetOrderItemsFunction")
                .functionName("GetOrderItemsFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("getItems.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function createOrderFunction = Function.Builder.create(this, "CreateOrderItemFunction")
                .functionName("CreateOrderItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("createItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function updateOrderFunction = Function.Builder.create(this, "UpdateOrderItemFunction")
                .functionName("UpdateOrderItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("updateItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function deleteOrderFunction = Function.Builder.create(this, "DeleteOrderItemFunction")
                .functionName("DeleteOrderItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("deleteItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        DatabaseStack.getOrderTable().grantReadWriteData(getOrderFunction);
        DatabaseStack.getOrderTable().grantReadWriteData(getOrdersFunction);
        DatabaseStack.getOrderTable().grantReadWriteData(createOrderFunction);
        DatabaseStack.getOrderTable().grantReadWriteData(updateOrderFunction);
        DatabaseStack.getOrderTable().grantReadWriteData(deleteOrderFunction);

        RestApi orderApi = RestApi.Builder.create(this, "orderApi")
                .restApiName("Order Service")
                .build();
        
        Model orderModel = Model.Builder.create(this, "OrderModel")
                .modelName("orderModel")
                .restApi(orderApi)
                .contentType("application/json")
                .schema(JsonSchema.builder()
                        .schema(JsonSchemaVersion.DRAFT7)
                        .description("Model for Orders")
                        .type(JsonSchemaType.OBJECT)
                        .properties(Stream.of(
                                new AbstractMap.SimpleEntry<>("userId", JsonSchema.builder().type(JsonSchemaType.STRING).build()),
                                new AbstractMap.SimpleEntry<>("productIds", JsonSchema.builder().type(JsonSchemaType.ARRAY).items(JsonSchema.builder().type(JsonSchemaType.STRING).build()).build()),
                                new AbstractMap.SimpleEntry<>("price", JsonSchema.builder().type(JsonSchemaType.NUMBER).build()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .required(Stream.of("userId", "productIds", "price").collect(Collectors.toList()))
                        .build())
                .build();
        
        RequestValidator orderBodyValidator = RequestValidator.Builder.create(this, "OrderPostValidator")
                .requestValidatorName("orderBodyValidator")
                .validateRequestBody(Boolean.TRUE)
                .validateRequestParameters(Boolean.FALSE)
                .restApi(orderApi)
                .build();

        IResource apiRoot = orderApi.getRoot();
        Resource apiIdResource = apiRoot.addResource("{id}");
        
        apiRoot.addMethod("GET", LambdaIntegration.Builder.create(getOrdersFunction)
                        .build());
        apiIdResource.addMethod("GET", LambdaIntegration.Builder.create(getOrderFunction)
                        .build());
        apiIdResource.addMethod("PUT", LambdaIntegration.Builder.create(updateOrderFunction)
                        .build());
        apiIdResource.addMethod("DELETE", LambdaIntegration.Builder.create(deleteOrderFunction)
                        .build());
        apiRoot.addMethod("POST",
                LambdaIntegration.Builder.create(createOrderFunction).build(),
                MethodOptions.builder()
                        .requestValidator(orderBodyValidator)
                        .requestModels(Stream.of(
                                new AbstractMap.SimpleEntry<>("application/json", orderModel))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .build());
    }

}
