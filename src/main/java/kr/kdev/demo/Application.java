package kr.kdev.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iot.model.*;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        // Setup AWS_PROFILE in Run Configuration
        try (IotClient iotClient = IotClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.AP_NORTHEAST_2)
                .build()) {

            // Check AWS Iot Data-ATS Endpoint
            DescribeEndpointResponse endpoint = iotClient.describeEndpoint(
                    DescribeEndpointRequest.builder()
                            .endpointType("iot:Data-ATS").build());
            log.info("Data-ATS Endpoint: {}", endpoint.endpointAddress());

            // Create Thing type and Thing
            CreateThingTypeResponse thingType = iotClient.createThingType(
                    CreateThingTypeRequest.builder()
                            .thingTypeName("Computer").build());
            CreateThingResponse thing = iotClient.createThing(CreateThingRequest.builder()
                    .thingName("PC")
                    .thingTypeName(thingType.thingTypeName())
                    .build());
            log.info("thingType: {}", thingType.thingTypeName());
            log.info("thing: {}", thing.thingName());

            // Create X.509 Certificate with RSA key-pair
            CreateKeysAndCertificateResponse keysAndCertificate = iotClient.createKeysAndCertificate(
                    CreateKeysAndCertificateRequest.builder()
                            .setAsActive(false)
                            .build());

            log.info("certificateArn: {}", keysAndCertificate.certificateArn());
            log.info("certificateId: {}", keysAndCertificate.certificateId());
            log.info("privateKey:\n{}", keysAndCertificate.keyPair().privateKey());
            log.info("publicKey:\n{}", keysAndCertificate.keyPair().publicKey());

            // Attach X.509 Certificate to Thing
            AttachThingPrincipalResponse attachThingPrincipal = iotClient.attachThingPrincipal(
                    AttachThingPrincipalRequest.builder()
                            .thingName(thing.thingName())
                            .principal(keysAndCertificate.certificateArn())
                            .build());
            log.info("attachThingPrincipal: {}", attachThingPrincipal.sdkHttpResponse().isSuccessful());

            // Activate X.509 Certificate
            UpdateCertificateResponse updateCertificate = iotClient.updateCertificate(
                    UpdateCertificateRequest.builder()
                            .certificateId(keysAndCertificate.certificateId())
                            .newStatus(CertificateStatus.ACTIVE)
                            .build());
            log.info("activated: {}", updateCertificate.sdkHttpResponse().isSuccessful());

            // Detach X.509 Certificate from Thing
            DetachThingPrincipalResponse detachThingPrincipal = iotClient.detachThingPrincipal(
                    DetachThingPrincipalRequest.builder()
                            .thingName(thing.thingName())
                            .principal(keysAndCertificate.certificateArn())
                            .build());
            log.info("detachThingPrincipal: {}", detachThingPrincipal.sdkHttpResponse().isSuccessful());

            // Revoke unused X.509 Certificate
            UpdateCertificateResponse revokedCertificate = iotClient.updateCertificate(
                    UpdateCertificateRequest.builder()
                            .certificateId(keysAndCertificate.certificateId())
                            .newStatus(CertificateStatus.REVOKED)
                            .build());
            log.info("revokedCertificate: {}", revokedCertificate.sdkHttpResponse().isSuccessful());

            // Delete X.509 Certificate
            DeleteCertificateResponse deleteCertificate = iotClient.deleteCertificate(
                    DeleteCertificateRequest.builder()
                            .certificateId(keysAndCertificate.certificateId())
                            .forceDelete(true)
                            .build());
            log.info("deleteCertificate: {}", deleteCertificate.sdkHttpResponse().isSuccessful());

            // Delete Thing
            DeleteThingResponse deleteThing = iotClient.deleteThing(
                    DeleteThingRequest.builder()
                            .thingName(thing.thingName())
                            .build());
            log.info("deleteThing: {}", deleteThing.sdkHttpResponse().isSuccessful());
        }
    }
}
