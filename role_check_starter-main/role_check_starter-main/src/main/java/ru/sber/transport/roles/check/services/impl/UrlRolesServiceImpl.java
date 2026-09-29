package ru.sber.transport.roles.check.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import ru.sber.transport.roles.check.data.dao.UrlRoleRepository;
import ru.sber.transport.roles.check.services.RoleProvider;
import ru.sber.transport.roles.common.model.Url;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис для работы с ролями по URL.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class UrlRolesServiceImpl implements RoleProvider {
    
    private final UrlRoleRepository urlRoleRepository;

    @Override
    public Set<String> getAllowedRoles(HttpMethod method, String requestURI) {
        if (!requestURI.endsWith("/")) {
            requestURI = requestURI + "/";
        }
        return new HashSet<>(findAllowedUrlData(method, requestURI)
                .map(Url::getRoles).orElse(Collections.emptySet()));
    }

    @Override
    public void save(String role, HttpMethod method, String url) {
        var urlInfo = findAllowedUrlData(method, url).orElseGet(() -> createUrlInfo(method, url));
        urlInfo.getRoles().add(role);
        urlRoleRepository.save(urlInfo);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<String> getUrls(String role) {
        return urlRoleRepository.findAllByRole(role).parallelStream()
                .map(url -> String.format("%s %s", url.getMethod(), url.getRestrictedUrl()))
                .toList();
    }

    @Override
    public void clearRole(String role) {
        urlRoleRepository.findAllByRole(role).parallelStream().forEach(item -> item.getRoles().remove(role));
    }

    /**
     * Создать инфо об URL. В поле `pattern` сущности `URL` записывается "жадный like" вместо динамической части URL
     * (то есть, `/organization/{id}/department/{departmentId}` будет преобразовано в `/organization/_%/department/_%`).
     *
     * @param method метод доступа к данным.
     * @param url    URL для сохранения.
     * @return данные об URL.
     */
    private Url createUrlInfo(HttpMethod method, String url) {
        var urlInfo = Url.create(method, url);
        return urlRoleRepository.save(urlInfo);
    }

    /**
     * Проверка URL.
     *
     * @param databaseUrls URL из базы данных.
     * @param requestURI   Пришедший URL.
     * @return <code>true</code> если URL проверен успешно.
     */
    private Optional<Url> checkUrl(Collection<Url> databaseUrls, String requestURI) {
        log.debug("Found %s results for url %s".formatted(databaseUrls.size(), requestURI));
        var commonMatches = new ConcurrentHashMap<Url, Match[]>();
        for (var databaseUrl : databaseUrls) {
            commonMatches.putAll(findMatches(requestURI, databaseUrl));
        }
        filterNoMatch(requestURI, commonMatches);
        if (commonMatches.isEmpty()) {
            log.debug("Found no match for %s".formatted(requestURI));
            return Optional.empty();
        } else if (commonMatches.size() == 1) {
            var url = commonMatches.keySet().iterator().next();
            log.debug("Found match for %s: %s".formatted(requestURI, url.getRestrictedUrl()));
            return Optional.of(url);
        } else {
            log.debug("""
                                        
                    Find best match for %s:
                    %s
                    """.formatted(requestURI,
                    commonMatches.entrySet().stream().map(entry -> "%s: %s".formatted(entry.getKey().getRestrictedUrl(), Arrays.stream(entry.getValue()).map(Match::name).collect(Collectors.joining(", ")))).collect(Collectors.joining(System.lineSeparator()))));
            var match = findBestMatch(commonMatches);
            if (match.isPresent()) {
                log.debug("""
                                                
                        Found best match for %s:
                         %s""".formatted(requestURI, match.get().getRestrictedUrl()));
            } else {
                log.debug("Found no match for %s".formatted(requestURI));
            }
            return match;
        }
    }

    private Map<Url, Match[]> findMatches(String requestURI, Url databaseUrl) {
        var result = new HashMap<Url, Match[]>();

        var restrictedUrl = databaseUrl.getRestrictedUrl();
        var databaseUrlParts = restrictedUrl.split("/");
        var receivedUrlParts = requestURI.split("/");
        var matches = new Match[databaseUrlParts.length - 1];

        if (databaseUrlParts.length == receivedUrlParts.length) {
            log.debug("Checking database url: %s".formatted(restrictedUrl));
            for (var i = 1; i < databaseUrlParts.length; i++) {
                var databaseUrlPart = databaseUrlParts[i];
                var receivedUrlPart = receivedUrlParts[i];

                var match = Match.NO;
                if (databaseUrlPart.equals(receivedUrlPart)) {
                    match = Match.EXACT;
                } else if (databaseUrlPart.startsWith("{") && databaseUrlPart.endsWith("}")) {
                    match = Match.PARTIAL;
                }
                matches[i - 1] = match;
            }
            result.put(databaseUrl, matches);
            log.debug("For database url %s and request %s found matches: %s".formatted(restrictedUrl, requestURI,
                    Arrays.stream(matches).map(Match::name).collect(Collectors.joining(", "))));
        }
        return result;
    }

    private void filterNoMatch(String requestURI, ConcurrentHashMap<Url, Match[]> commonMatches) {
        for (var commonMatch : commonMatches.entrySet()) {
            if (Arrays.asList(commonMatch.getValue()).contains(Match.NO)) {
                var url = commonMatch.getKey();
                log.debug("Database url %s guaranteed no match %s".formatted(requestURI, url.getRestrictedUrl()));
                commonMatches.remove(url);
            }
        }
    }

    private Optional<Url> findBestMatch(Map<Url, Match[]> commonMatches) {
        var url = Optional.<Url>empty();
        var maxMatch = 0L;
        for (var commonMatch : commonMatches.entrySet()) {
            var match = Arrays.stream(commonMatch.getValue()).filter(Match.EXACT::equals).count();
            if (maxMatch < match) {
                maxMatch = match;
                url = Optional.of(commonMatch.getKey());
            }
        }
        return url;
    }

    /**
     * Поиск доступных URL.
     *
     * @param method     метод доступа к данным.
     * @param requestURI URL для сохранения.
     * @return данные об URL.
     */
    private Optional<Url> findAllowedUrlData(HttpMethod method, String requestURI) {
        var foundUrl = urlRoleRepository.findByMethodAndRestrictedUrl(method, requestURI);
        if (foundUrl.isEmpty()) {
            foundUrl = checkUrl(urlRoleRepository.findAllByMethodAndPattern(method, requestURI), requestURI);
        }
        return foundUrl;
    }

    private enum Match {
        NO,
        PARTIAL,
        EXACT
    }
}
