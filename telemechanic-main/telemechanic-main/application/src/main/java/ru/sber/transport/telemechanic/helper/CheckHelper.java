package ru.sber.transport.telemechanic.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.dto.MonitorCheckTreeDto;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.CheckTypeMonitoring;
import ru.sber.transport.telemechanic.mapper.CheckMapper;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ru.sber.transport.telemechanic.enumerate.CheckStatus.*;
import static ru.sber.transport.telemechanic.enumerate.CheckTypeMonitoring.*;

@Component
@RequiredArgsConstructor
public class CheckHelper {
    
    private final CheckMapper checkMapper;
    
    private static final Predicate<Check> ATTEMPT_IS_ZERO = check -> check.getAttempt() == 0;
    private static final Predicate<Check> ATTEMPT_GREATER_THAN_ZERO = check -> check.getAttempt() > 0;
    
    private static final Comparator<Check> CHECK_TYPE_COMPARATOR = Comparator.comparing(Check::getCheckType);
    private static final Comparator<Check> CHECK_STATUS_AND_CHECK_TYPE_COMPARATOR =
            Comparator.comparing(Check::getCheckStatus).thenComparing(Check::getCheckType);
    
    public ChecksTreeDto.ChecksTree createChecksTree(Set<Check> checks) {
        return new ChecksTreeDto.ChecksTree(compilePassChecks(checks), compileFinishedChecks(checks));
    }
    
    public List<MonitorCheckTreeDto> createChecksTree(Request request) {
        var monitoringChecks = request.getChecks().stream()
                                      .filter(check -> Objects.isNull(check.getCheckType().getParent()) &&
                                                       Objects.isNull(check.getCheckType().getParentMonitoring()))
                                      .map(checkMapper::checkToMonitorCheckTreeDto)
                                      .map(el -> addChildrenForMonitor(el, request.getChecks()))
                                      .toList();
        return enrichChecksForMonitoring(monitoringChecks, request.getChecks()).stream()
                .sorted(Comparator.comparing((MonitorCheckTreeDto check) -> check.checkStatus().getMonitoringOrdinal())
                                          .thenComparing((MonitorCheckTreeDto check) -> check.checkType().ordinal()))
                .toList();
    }
    
    public static boolean isCallTelemech(Set<Check> checks) {
        for (var check : checks) {
            if (check.getCheckType().equals(CheckType.VEHICLE_NUMBER) && (!check.getCheckStatus().isFinal()) ||
                check.getAttempt() < 1) {
                return false;
            }
        }
        return true;
    }
    
    private List<ChecksTreeDto.CheckDto> compilePassChecks(Set<Check> checks) {
        var tree = new ArrayList<>(getChecksWithPredicateAndComparator(checks, ATTEMPT_IS_ZERO, CHECK_TYPE_COMPARATOR));
        tree.addAll(getChecksWithPredicateAndComparator(checks, ATTEMPT_GREATER_THAN_ZERO, CHECK_STATUS_AND_CHECK_TYPE_COMPARATOR));
        return tree;
    }
    
    private List<ChecksTreeDto.CheckDto> getChecksWithPredicateAndComparator(
            Set<Check> checks, Predicate<Check> predicate, Comparator<Check> comparator
                                                                            ) {
        return checks.stream()
                     .filter(check -> Objects.isNull(check.getCheckType().getParent()))
                     .filter(predicate)
                     .filter(check -> check.getCheckStatus().equals(CheckStatus.IN_PROGRESS))
                     .sorted(comparator)
                     .map(checkMapper::checkToChecksTreeDto)
                     .map(el -> addChildren(el, checks))
                     .toList();
    }
    
    private List<ChecksTreeDto.CheckDto> compileFinishedChecks(Set<Check> checks) {
        return checks.stream()
                     .filter(check -> Objects.isNull(check.getCheckType().getParent()))
                     .filter(check -> Stream.of(DONE, DECLINE)
                                            .anyMatch(status -> check.getCheckStatus().equals(status)))
                     .sorted(Comparator.comparing(Check::getCheckStatus).thenComparing(Check::getCheckType))
                     .map(checkMapper::checkToChecksTreeDto)
                     .map(el -> addChildren(el, checks))
                     .toList();
    }
    
    private ChecksTreeDto.CheckDto addChildren(ChecksTreeDto.CheckDto parent, Set<Check> checks) {
        if (parent.getCheckType().isMain()) {
            parent.setChildren(new ArrayList<>(getChildrenWithPredicateAndComparator(parent, checks, ATTEMPT_IS_ZERO, CHECK_TYPE_COMPARATOR)));
            parent.getChildren()
                  .addAll(getChildrenWithPredicateAndComparator(parent, checks, ATTEMPT_GREATER_THAN_ZERO, CHECK_STATUS_AND_CHECK_TYPE_COMPARATOR));
            return parent;
        }
        return parent;
    }
    
    private List<ChecksTreeDto.CheckDto> getChildrenWithPredicateAndComparator(
            ChecksTreeDto.CheckDto parent, Set<Check> checks,
            Predicate<Check> predicate, Comparator<Check> comparator
                                                                              ) {
        return checks.stream()
                     .filter(check -> Objects.nonNull(check.getCheckType().getParent()) &&
                                      check.getCheckType().getParent().equals(parent.getCheckType()))
                     .filter(predicate)
                     .sorted(comparator)
                     .map(checkMapper::checkToChecksTreeDto)
                     .toList();
    }
    
    private MonitorCheckTreeDto addChildrenForMonitor(MonitorCheckTreeDto parent, Set<Check> checks) {
        if (parent.checkType().isMain()) {
            return parent.withChildren(checks.stream()
                                             .filter(check -> Objects.nonNull(check.getCheckType().getParent()))
                                             .sorted(Comparator.comparing((Check check) -> check.getCheckStatus().getMonitoringOrdinal())
                                                               .thenComparing(check -> check.getCheckType().getOrdinal())
                                                    )
                                             .map(checkMapper::checkToMonitorCheckTreeDto)
                                             .toList());
        }
        return parent;
    }
    
    private List<MonitorCheckTreeDto> addChildrenForMonitor(CheckTypeMonitoring parent, Set<Check> requestChecks) {
        if (parent.isMain()) {
            return requestChecks.stream()
                                .filter(check -> check.getCheckType().getParentMonitoring() != null &&
                                                 check.getCheckType().getParentMonitoring().equals(parent))
                                .sorted(Comparator.comparing((Check check) -> check.getCheckStatus().getMonitoringOrdinal())
                                                  .thenComparing(check -> check.getCheckType().getOrdinal())
                                       )
                                .map(checkMapper::checkToMonitorCheckTreeDto)
                                .toList();
        }
        return Collections.emptyList();
    }
    
    private List<MonitorCheckTreeDto> enrichChecksForMonitoring(List<MonitorCheckTreeDto> tree, Set<Check> checks) {
        var splashGuards = addChildrenForMonitor(SPLASH_GUARDS, checks);
        var sideMirrors = addChildrenForMonitor(SIDE_MIRRORS, checks);
        var headlamps = addChildrenForMonitor(HEADLAMPS, checks);
        var newTree = new ArrayList<>(tree);
        newTree.addAll(List.of(
                new MonitorCheckTreeDto(null, SPLASH_GUARDS, getParentCheckStatus(splashGuards), 0, 0, null, null, splashGuards),
                new MonitorCheckTreeDto(null, SIDE_MIRRORS, getParentCheckStatus(sideMirrors), 0, 0, null, null, sideMirrors),
                new MonitorCheckTreeDto(null, HEADLAMPS, getParentCheckStatus(headlamps), 0, 0, null, null, headlamps)
                              ));
        return newTree;
    }
    
    private CheckStatus getParentCheckStatus(List<MonitorCheckTreeDto> children) {
        if (children == null) {
            return null;
        }
        if (children.stream().allMatch(child -> child.checkStatus().equals(DONE))) {
            return DONE;
        }
        if (children.stream().anyMatch(child -> child.checkStatus().equals(DECLINE))) {
            return DECLINE;
        }
        return IN_PROGRESS;
    }
}
