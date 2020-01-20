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
public class ProductStack extends Stack {

    public ProductStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public ProductStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final Map<String, String> dynamoDbEnvironmentParams = DatabaseStack.getDynamoEnvironmentParams(DatabaseTables.PRODUCTS);

        final Function getProductFunction = Function.Builder.create(this, "GetProductItemFunction")
                .functionName("GetProductItemFunction")
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))
                .handler("getItem.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function getProductsFunction = Function.Builder.create(this, "GetProductItemsFunction")
                .functionName("GetProductItemsFunction")
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))
                .handler("getItems.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function createProductFunction = Function.Builder.create(this, "CreateProductItemFunction")
                .functionName("CreateProductItemFunction")
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))
                .handler("createItem.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function updateProductFunction = Function.Builder.create(this, "UpdateProductItemFunction")
                .functionName("UpdateProductItemFunction")
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))
                .handler("updateItem.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function deleteProductFunction = Function.Builder.create(this, "DeleteProductItemFunction")
                .functionName("DeleteProductItemFunction")
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))
                .handler("deleteItem.handler")
                .environment(dynamoDbEnvironmentParams)
                .build();

        DatabaseStack.getProductTable().grantReadWriteData(getProductFunction);
        DatabaseStack.getProductTable().grantReadWriteData(getProductsFunction);
        DatabaseStack.getProductTable().grantReadWriteData(createProductFunction);
        DatabaseStack.getProductTable().grantReadWriteData(updateProductFunction);
        DatabaseStack.getProductTable().grantReadWriteData(deleteProductFunction);

        RestApi productApi = RestApi.Builder.create(this, "productApi")
                .restApiName("Product Service")
                .build();

        Model productModel = Model.Builder.create(this, "ProductModel")
                .modelName("ProductModel")
                .restApi(productApi)
                .contentType("application/json")
                .schema(JsonSchema.builder()
                        .schema(JsonSchemaVersion.DRAFT7)
                        .description("Model for Products")
                        .type(JsonSchemaType.OBJECT)
                        .properties(Stream.of(
                                new AbstractMap.SimpleEntry<>("name", JsonSchema.builder().type(JsonSchemaType.STRING).build()),
                                new AbstractMap.SimpleEntry<>("description", JsonSchema.builder().type(JsonSchemaType.STRING).build()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .required(Stream.of("name", "description").collect(Collectors.toList()))
                        .build())
                .build();
        
        RequestValidator productBodyValidator = RequestValidator.Builder.create(this, "ProductPostValidator")
                .requestValidatorName("productBodyValidator")
                .validateRequestBody(Boolean.TRUE)
                .validateRequestParameters(Boolean.FALSE)
                .restApi(productApi)
                .build();
        
        IResource apiRoot = productApi.getRoot();
        Resource apiIdResource = apiRoot.addResource("{id}");

        apiRoot.addMethod("GET", LambdaIntegration.Builder.create(getProductsFunction).build());
        apiIdResource.addMethod("GET", LambdaIntegration.Builder.create(getProductFunction).build());
        apiIdResource.addMethod("PUT", LambdaIntegration.Builder.create(updateProductFunction).build());
        apiIdResource.addMethod("DELETE", LambdaIntegration.Builder.create(deleteProductFunction).build());
        apiRoot.addMethod("POST",
                LambdaIntegration.Builder.create(createProductFunction).build(),
                MethodOptions.builder()
                        .requestValidator(productBodyValidator)
                        .requestModels(Stream.of(
                                new AbstractMap.SimpleEntry<>("application/json", productModel))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .build());
    }
}
