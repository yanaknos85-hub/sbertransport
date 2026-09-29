package ru.sberbank.transport.oto.cargo.util;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Утилитный класс для определения уровня доступа роли для поиска грузовых доставок по организациям:
 *  RESTRICTED_ACCESS_ROLES - роли ограничееного доступа,
 *  FULL_ACCESS_ROLES - роли полного доступа.
 *
 */
public final class CheckOrganizationAccessUtils {
    public static final List<String> RESTRICTED_ACCESS_ROLES = List.of("ROLE_DISPATCHER_SUPPORT_SERVICE");
    public static final List<String> FULL_ACCESS_ROLES = List.of("ROLE_ADMIN_DATA_MASTER");

    private CheckOrganizationAccessUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean hasFullAccessRoles(Collection<? extends GrantedAuthority> authorities){
        return authorities.stream().anyMatch(a -> FULL_ACCESS_ROLES.contains(a.getAuthority()));
    }

    public static boolean hasRestrictedAccessRoles (Collection<? extends GrantedAuthority> authorities){
        return authorities.stream().anyMatch(a -> RESTRICTED_ACCESS_ROLES.contains(a.getAuthority()));
    }
    

}
