package ru.sber.transport.request.external.web.model;

import ru.sber.transport.web.model.Assessment;

public class WebResponseAssessment extends Assessment {

    public WebResponseAssessment(ru.sber.transport.request.external.model.Assessment service) {
        setComment(service.getComment());
        setRating((int) service.getRating());
    }

}
