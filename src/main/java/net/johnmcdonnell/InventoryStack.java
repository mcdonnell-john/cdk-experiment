package net.johnmcdonnell;

import java.util.AbstractMap;
import java.util.Map;
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
public class InventoryStack extends Stack {

    public InventoryStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public InventoryStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final Map<String, String> dynamoDbEnvironmentParams = DatabaseStack.getDynamoEnvironmentParams(DatabaseTables.INVENTORY);

        final Function getInventoryFunction = Function.Builder.create(this, "GetInventoryItemFunction")
                .functionName("GetInventoryItemFunction")
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))
                .handler("getItem.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function updateInventoryFunction = Function.Builder.create(this, "UpdateInventoryItemFunction")
                .functionName("UpdateInventoryItemFunction")
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))
                .handler("updateItem.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        DatabaseStack.getProductTable().grantReadWriteData(getInventoryFunction);
        DatabaseStack.getProductTable().grantReadWriteData(updateInventoryFunction);

        RestApi inventoryApi = RestApi.Builder.create(this, "inventoryApi")
                .restApiName("Inventory Service")
                .build();

        Model productModel = Model.Builder.create(this, "inventoryModel")
                .modelName("inventoryModel")
                .restApi(inventoryApi)
                .contentType("application/json")
                .schema(JsonSchema.builder()
                        .schema(JsonSchemaVersion.DRAFT7)
                        .description("Model for Inventory Update")
                        .type(JsonSchemaType.OBJECT)
                        .properties(Stream.of(
                                new AbstractMap.SimpleEntry<>("action", JsonSchema.builder().type(JsonSchemaType.STRING).enumValue(Stream.of("add", "remove").collect(Collectors.toList())).build()),
                                new AbstractMap.SimpleEntry<>("description", JsonSchema.builder().type(JsonSchemaType.INTEGER).build()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .required(Stream.of("name", "description").collect(Collectors.toList()))
                        .build())
                .build();
        
        RequestValidator inventoryBodyValidator = RequestValidator.Builder.create(this, "inventoryPutValidator")
                .requestValidatorName("inventoryBodyValidator")
                .validateRequestBody(Boolean.TRUE)
                .validateRequestParameters(Boolean.FALSE)
                .restApi(inventoryApi)
                .build();
        
        IResource apiRoot = inventoryApi.getRoot();
        Resource apiIdResource = apiRoot.addResource("{id}");

        apiIdResource.addMethod("GET", LambdaIntegration.Builder.create(getInventoryFunction).build());
        apiIdResource.addMethod("PUT", LambdaIntegration.Builder.create(updateInventoryFunction).build(),
                MethodOptions.builder()
                        .requestValidator(inventoryBodyValidator)
                        .requestModels(Stream.of(
                                new AbstractMap.SimpleEntry<>("application/json", productModel))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .build());
    }
}
