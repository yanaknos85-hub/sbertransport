package ru.sber.transport.request.external.web.model;

import java.util.Optional;
import ru.sber.transport.web.model.Assessments;

public class WebResponseAssessments extends Assessments {

    public WebResponseAssessments(ru.sber.transport.request.external.model.Assessments assessments) {
        setService(Optional.ofNullable(assessments).map(ru.sber.transport.request.external.model.Assessments::getService).map(WebResponseAssessment::new).orElse(null));
    }

}
