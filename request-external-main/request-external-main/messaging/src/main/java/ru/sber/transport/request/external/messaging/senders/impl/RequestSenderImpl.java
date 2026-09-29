package ru.sber.transport.request.external.messaging.senders.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.transport.messages.request.external.avro.AssessmentType;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.mapper.RequestMapper;
import ru.sber.transport.request.external.messaging.message.RequestMessage;
import ru.sber.transport.request.external.messaging.senders.RequestSender;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.TripOrderData;

@RequiredArgsConstructor
public class RequestSenderImpl implements RequestSender {

    private final ObjectProvider<OutputBridge> kafkaBridge;
    private final ObjectProvider<OutputBridge> kafkaSslBridge;
    private final ObjectProvider<OutputBridge> avroBridge;
    private final RequestMapper mapper;

    @Override
    public void send(TripOrderData source) {
        var message = mapper.toRequestMessage(source, createAssessments(source.getAssessments()));
        var avroMessage = mapper.toAvroRequestMessage(source);
        avroMessage.setAssessments(createAssessmentsAvro(source.getAssessments()));

        kafkaBridge.ifAvailable(it -> it.send(message));
        kafkaSslBridge.ifAvailable(it -> it.send(message));
        avroBridge.ifAvailable(it -> it.send(avroMessage));
    }


    private List<ru.sber.transport.messages.request.external.avro.Assessment> createAssessmentsAvro(Assessments assessments) {
        final var result = new ArrayList<ru.sber.transport.messages.request.external.avro.Assessment>();
        if (assessments != null && assessments.getService() != null) {
            result.add(ru.sber.transport.messages.request.external.avro.Assessment.newBuilder()
                    .setType(AssessmentType.SERVICE)
                    .setRating(assessments.getService().getRating())
                    .build());
        }
        return result;
    }

    private List<RequestMessage.Assessment> createAssessments(
            Assessments assessments) {
        final var result = new ArrayList<RequestMessage.Assessment>();
        if (assessments != null && assessments.getService() != null) {
            result.add(RequestMessage.Assessment.builder()
                    .type(AssessmentType.SERVICE.name())
                    .rating(assessments.getService().getRating())
                    .build());
        }
        return result;
    }

}
