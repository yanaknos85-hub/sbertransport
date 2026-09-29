package ru.sber.transport.notifications.services.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.model.settings.userNotification.UserNotificationSettings;
import ru.sber.transport.notifications.database.model.settings.userNotification.UserNotificationSettings_;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsSearchDto;
import ru.sber.transport.notifications.services.UserNotificationSettingsSpecService;

@Service
public class UserNotificationSettingsSpecServiceImpl implements UserNotificationSettingsSpecService<UserNotificationSettingsSearchDto> {


    @Override
    public Specification<UserNotificationSettings> getSpec(UserNotificationSettingsSearchDto searchDto) {
        return
                (root, query, builder) -> {
                    Predicate predicate = builder.and();
                    if (searchDto.getNotificationClass() != null && !searchDto.getNotificationClass().isEmpty()) {
                        predicate = builder.and(predicate, root.get(UserNotificationSettings_.NOTIFICATION_CLASS).in(searchDto.getNotificationClass()));
                    }
                    if (searchDto.getUserId() != null ) {
                        predicate = builder.and(predicate, builder.equal(root.get(UserNotificationSettings_.userId), searchDto.getUserId()));
                    }
                    if (searchDto.getParentId() != null) {
                        predicate = builder.and(predicate, builder.equal(root.get(UserNotificationSettings_.parentId), searchDto.getParentId()));
                    }

                    return predicate;
                };
    }

}

