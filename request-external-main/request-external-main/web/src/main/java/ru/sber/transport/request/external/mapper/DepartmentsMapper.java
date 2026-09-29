package ru.sber.transport.request.external.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import ru.sber.transport.request.external.model.Department;
import ru.sber.transport.web.model.DepartmentRegistryFilterRs;

@Mapper
public interface DepartmentsMapper {
    List<DepartmentRegistryFilterRs> toDepartmentRegistryFiltersRs(List<Department> source);

    DepartmentRegistryFilterRs toDepartmentRegistryFilterRs(Department source);
}
