package ru.sberbank.ditsib.transport.request.config.publicTransport;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CustomRevision;

import java.util.UUID;

public class CustomRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof CustomRevision entity) {
            entity.setUserId(ControllerUtils.currentUser());
        }
    }
}
