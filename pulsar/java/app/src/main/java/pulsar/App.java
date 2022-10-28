/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package pulsar;

import java.net.URL;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.auth.oauth2.AuthenticationFactoryOAuth2;


public class App {
    public String getGreeting() {
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new App().getGreeting());

        String issuerUrl = "https://auth.streamnative.cloud/";
        String credentialsUrl = "file://YOUR-KEY-FILE-PATH";
        String audience = "urn:sn:pulsar:o-whe2d:free";

        PulsarClient client = PulsarClient.builder()
                .serviceUrl("pulsar+ssl://free.o-whe2d.snio.cloud:6651")
                .authentication(
                        AuthenticationFactoryOAuth2.clientCredentials(new URL(issuerUrl), new URL(credentialsUrl), audience))
                .build();

        Producer producer = client.newProducer().create();

        client.close();
    }
}