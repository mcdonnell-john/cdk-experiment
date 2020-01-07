package net.johnmcdonnell;

import software.amazon.awscdk.core.App;

/**
 *
 * @author John McDonnell
 */
public final class Application {
    public static void main(final String[] args) {
        App app = new App();

        new DatabaseStack(app, "DatabaseStack");
        new ProductStack(app, "ProductStack");
        new OrderStack(app, "OrderStack");
        new ReviewStack(app, "ReviewStack");
        
        app.synth();
    }
}
