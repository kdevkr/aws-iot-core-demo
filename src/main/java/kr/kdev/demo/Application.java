package kr.kdev.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iot.model.*;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        try (IotClient iotClient = IotClient.builder()
                .credentialsProvider(ProfileCredentialsProvider.create("iot-core"))
                .region(Region.AP_NORTHEAST_2)
                .build()) {

            // 데이터 ATS 엔드포인트 확인
            String endpointAddress = iotClient.describeEndpoint(
                            DescribeEndpointRequest.builder()
                                    .endpointType("iot:Data-ATS").build())
                    .endpointAddress();
            log.info("endpointAddress: {}", endpointAddress);

            // 사물 유형 발급
            CreateThingTypeResponse thingType = iotClient.createThingType(
                    CreateThingTypeRequest.builder()
                            .thingTypeName("Computer").build());
            log.info("thingType: {}", thingType.thingTypeName());

            // 사물 생성
            CreateThingResponse thing = iotClient.createThing(CreateThingRequest.builder()
                    .thingName("PC")
                    .thingTypeName(thingType.thingTypeName())
                    .build());

            log.info("thing: {}", thing.thingName());

            // 사물에 연결할 인증서 발급
            CreateKeysAndCertificateResponse keysAndCertificate = iotClient.createKeysAndCertificate(CreateKeysAndCertificateRequest.builder()
                    .setAsActive(false)
                    .build());

            log.info("certificateArn: {}", keysAndCertificate.certificateArn());

            // 사물에 X.509 클라이언트 인증서 연결
            AttachThingPrincipalResponse attachThingPrincipal = iotClient.attachThingPrincipal(AttachThingPrincipalRequest.builder()
                    .thingName(thing.thingName())
                    .principal(keysAndCertificate.certificateArn())
                    .build());

            log.info("attachThingPrincipal: {}", attachThingPrincipal);

            // 사물에 연결된 X.509 인증서 활성화 상태로 변경
            UpdateCertificateResponse updateCertificate = iotClient.updateCertificate(UpdateCertificateRequest.builder()
                    .certificateId(keysAndCertificate.certificateId())
                    .newStatus(CertificateStatus.ACTIVE)
                    .build());

            log.info("updateCertificate: {}", updateCertificate);

            // 사물에 연결된 X.509 인증서 연결 해제
            DetachThingPrincipalResponse detachThingPrincipal = iotClient.detachThingPrincipal(DetachThingPrincipalRequest.builder()
                    .thingName(thing.thingName())
                    .principal(keysAndCertificate.certificateArn())
                    .build());
            log.info("detachThingPrincipal: {}", detachThingPrincipal.sdkHttpResponse().isSuccessful());

            // 더이상 사용되지 않는 인증서라 가정하고 회수(취소) 상태로 변경
            UpdateCertificateResponse revokedCertificate = iotClient.updateCertificate(UpdateCertificateRequest.builder()
                    .certificateId(keysAndCertificate.certificateId())
                    .newStatus(CertificateStatus.REVOKED)
                    .build());
            log.info("revokedCertificate: {}", revokedCertificate.sdkHttpResponse().isSuccessful());

            // X.509 인증서 삭제
            DeleteCertificateResponse deleteCertificate = iotClient.deleteCertificate(DeleteCertificateRequest.builder()
                    .certificateId(keysAndCertificate.certificateId())
                    .forceDelete(true)
                    .build());
            log.info("deleteCertificate: {}", deleteCertificate.sdkHttpResponse().isSuccessful());

            // 사용되지 않는 사물 삭제
            DeleteThingResponse deleteThing = iotClient.deleteThing(DeleteThingRequest.builder()
                    .thingName(thing.thingName())
                    .build());
            log.info("deleteThing: {}", deleteThing.sdkHttpResponse().isSuccessful());
        }
    }
}
