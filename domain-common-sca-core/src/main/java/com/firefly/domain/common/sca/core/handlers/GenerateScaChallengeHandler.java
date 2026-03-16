package com.firefly.domain.common.sca.core.handlers;

import com.firefly.common.sca.sdk.api.ScaChallengeControllerApi;
import com.firefly.common.sca.sdk.model.SCAChallengeDTO;
import com.firefly.domain.common.sca.core.commands.GenerateScaChallengeCommand;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import reactor.core.publisher.Mono;

/**
 * Handles {@link GenerateScaChallengeCommand} by creating a new challenge via the SCA management API.
 * Used to (re)generate a challenge for an existing operation independently of the initiation saga.
 */
@Slf4j
@CommandHandlerComponent(timeout = 15000, metrics = true, tracing = true)
public class GenerateScaChallengeHandler extends CommandHandler<GenerateScaChallengeCommand, SCAChallengeDTO> {

    private final ScaChallengeControllerApi scaChallengeApi;

    public GenerateScaChallengeHandler(ScaChallengeControllerApi scaChallengeApi) {
        this.scaChallengeApi = scaChallengeApi;
    }

    @Override
    protected Mono<SCAChallengeDTO> doHandle(GenerateScaChallengeCommand command) {
        log.debug("Generating challenge for operationId={} type={}", command.getOperationId(), command.getChallengeType());

        SCAChallengeDTO dto = new SCAChallengeDTO()
                .scaOperationId(command.getOperationId());

        return scaChallengeApi.createChallenge(
                command.getOperationId(),
                dto,
                null
        );
    }
}
