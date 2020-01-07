package net.johnmcdonnell;

import java.util.Map;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

/**
 *
 * @author John McDonnell
 */
public class ReviewStack extends Stack {

    public ReviewStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public ReviewStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final Map<String, String> dynamoDbEnvironmentParams = DatabaseStack.getDynamoEnvironmentParams(DatabaseStack.REVIEWS_TABLE_NAME);

        final Function getReviewFunction = Function.Builder.create(this, "GetReviewItemFunction")
                .functionName("GetReviewItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("getItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function getReviewsFunction = Function.Builder.create(this, "GetReviewItemsFunction")
                .functionName("GetReviewItemsFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("getItems.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function createReviewFunction = Function.Builder.create(this, "CreateReviewItemFunction")
                .functionName("CreateReviewItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("createItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function updateReviewFunction = Function.Builder.create(this, "UpdateReviewItemFunction")
                .functionName("UpdateReviewItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("updateItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        final Function deleteReviewFunction = Function.Builder.create(this, "DeleteReviewItemFunction")
                .functionName("DeleteReviewItemFunction")
                .runtime(Runtime.NODEJS_12_X) 
                .code(Code.fromAsset("lambda")) 
                .handler("deleteItem.handler") 
                .environment(dynamoDbEnvironmentParams)
                .build();

        DatabaseStack.getReviewTable().grantReadWriteData(getReviewFunction);
        DatabaseStack.getReviewTable().grantReadWriteData(getReviewsFunction);
        DatabaseStack.getReviewTable().grantReadWriteData(createReviewFunction);
        DatabaseStack.getReviewTable().grantReadWriteData(updateReviewFunction);
        DatabaseStack.getReviewTable().grantReadWriteData(deleteReviewFunction);

        // Defines an API Gateway REST API resource 
        RestApi reviewApi = RestApi.Builder.create(this, "reviewApi")
                .restApiName("Review Service")
                .build();

        IResource apiRoot = reviewApi.getRoot();
        Resource apiIdResource = apiRoot.addResource("{id}");
        
        apiRoot.addMethod("GET", LambdaIntegration.Builder.create(getReviewsFunction)
                        .build());
        apiIdResource.addMethod("GET", LambdaIntegration.Builder.create(getReviewFunction)
                        .build());
        apiIdResource.addMethod("PATCH", LambdaIntegration.Builder.create(updateReviewFunction)
                        .build());
        apiIdResource.addMethod("DELETE", LambdaIntegration.Builder.create(deleteReviewFunction)
                        .build());
        apiRoot.addMethod("POST", LambdaIntegration.Builder.create(createReviewFunction)
                        .build());
    }

}
