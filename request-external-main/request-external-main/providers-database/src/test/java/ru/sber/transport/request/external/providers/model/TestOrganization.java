package ru.sber.transport.request.external.providers.model;

import java.util.List;
import java.util.UUID;
import ru.sber.transport.request.external.model.Organization;

public record TestOrganization(UUID getId, long getDigitId, List<String> getAvailableClasses) implements Organization {}