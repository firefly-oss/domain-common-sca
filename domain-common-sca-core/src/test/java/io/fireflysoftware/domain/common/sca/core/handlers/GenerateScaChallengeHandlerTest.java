package io.fireflysoftware.domain.common.sca.core.handlers;

import io.fireflysoftware.common.sca.sdk.api.ScaChallengeControllerApi;
import io.fireflysoftware.common.sca.sdk.model.SCAChallengeDTO;
import io.fireflysoftware.domain.common.sca.core.commands.ChallengeType;
import io.fireflysoftware.domain.common.sca.core.commands.GenerateScaChallengeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateScaChallengeHandlerTest {

    @Mock
    private ScaChallengeControllerApi scaChallengeApi;

    private GenerateScaChallengeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GenerateScaChallengeHandler(scaChallengeApi);
    }

    @Test
    void doHandle_createsChallenge_andReturnsDto() {
        UUID operationId = UUID.randomUUID();
        GenerateScaChallengeCommand command = new GenerateScaChallengeCommand(operationId, ChallengeType.OTP);

        SCAChallengeDTO expectedChallenge = new SCAChallengeDTO().scaOperationId(operationId);
        when(scaChallengeApi.createChallenge(eq(operationId), any(SCAChallengeDTO.class), any()))
                .thenReturn(Mono.just(expectedChallenge));

        StepVerifier.create(handler.doHandle(command))
                .expectNext(expectedChallenge)
                .verifyComplete();
    }

    @Test
    void doHandle_propagatesErrorFromApi() {
        UUID operationId = UUID.randomUUID();
        GenerateScaChallengeCommand command = new GenerateScaChallengeCommand(operationId, ChallengeType.PUSH);

        when(scaChallengeApi.createChallenge(eq(operationId), any(SCAChallengeDTO.class), any()))
                .thenReturn(Mono.error(new RuntimeException("api error")));

        StepVerifier.create(handler.doHandle(command))
                .expectErrorMessage("api error")
                .verify();
    }
}
