package ru.sber.transport.request.external.web.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.model.Assessment;
import ru.sber.transport.request.external.model.Assessments;

@RequiredArgsConstructor
public class WebRequestAssessments implements Assessments {

    @Delegate
    private final ru.sber.transport.web.model.Assessments delegatee;

    @Override
    public Assessment getService() {
        return new WebRequestAssessment(delegatee.getService());
    }

}
