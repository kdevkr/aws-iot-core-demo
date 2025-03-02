package kr.kdev.demo;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.crt.mqtt5.*;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket;
import software.amazon.awssdk.iot.AwsIotMqtt5ClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iot.model.DescribeEndpointResponse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static java.lang.System.err;
import static java.lang.System.out;

public class IotDevice {
    public static void main(String[] args) throws InterruptedException {
        Mqtt5Client client = null;

        // Setup AWS_PROFILE in Run Configuration
        try (IotClient iotClient = IotClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.AP_NORTHEAST_2)
                .build()) {

            // Check AWS Iot Data-ATS Endpoint
            DescribeEndpointResponse endpoint = iotClient.describeEndpoint(builder ->
                    builder.endpointType("iot:Data-ATS").build());

            String dataAtsEndpoint = endpoint.endpointAddress();
            String certificatePem = getPem("certificate.pem");
            String privateKey = getPem("privateKey.pem");
            String caRoot = getPem("AmazonRootCA1.pem");
            ConnectPacket.ConnectPacketBuilder connectProperties = new ConnectPacket.ConnectPacketBuilder()
                    .withClientId("PC");

            client = AwsIotMqtt5ClientBuilder
                    .newDirectMqttBuilderWithMtlsFromMemory(dataAtsEndpoint, certificatePem, privateKey)
                    .withCertificateAuthority(caRoot)
                    .withConnectProperties(connectProperties)
                    .withLifeCycleEvents(new Mqtt5ClientOptions.LifecycleEvents() {
                        private int attempt = 0;

                        @Override
                        public void onAttemptingConnect(Mqtt5Client mqtt5Client, OnAttemptingConnectReturn onAttemptingConnectReturn) {
                            out.println("[INFO] attempt...%s".formatted(++attempt));
                        }

                        @Override
                        public void onConnectionSuccess(Mqtt5Client mqtt5Client, OnConnectionSuccessReturn onConnectionSuccessReturn) {
                            out.println("[INFO] Connected!");
                        }

                        @Override
                        public void onConnectionFailure(Mqtt5Client mqtt5Client, OnConnectionFailureReturn onConnectionFailureReturn) {
                            err.println("[ERROR] connection failed: " + onConnectionFailureReturn.getErrorCode());
                        }

                        @Override
                        public void onDisconnection(Mqtt5Client mqtt5Client, OnDisconnectionReturn onDisconnectionReturn) {
                            err.println("[ERROR] disconnect : " + onDisconnectionReturn.getErrorCode());
                        }

                        @Override
                        public void onStopped(Mqtt5Client mqtt5Client, OnStoppedReturn onStoppedReturn) {
                            out.println("[ERROR] Stopped :(");
                        }
                    })
                    .build();
            client.start();

            Thread.sleep(Duration.ofMinutes(5));
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    public static String getPem(String filename) {
        try {
            return Files.readString(Path.of(ClassLoader.getSystemResource(filename).toURI()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
