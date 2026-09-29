import { useEmployeeAttributes } from 'api/employee-attributes';
import { useDepartments } from 'api/departments';
import { UUID } from 'utils/io-ts';
import { SelectOption } from '../types/types';

interface Export {
  departmentsOptions: SelectOption[];
  attributeOptions: SelectOption[];
}

export const useFormOptions = ({ organizationId }: { organizationId: UUID }): Export => {
  const employeesAttributes = useEmployeeAttributes(organizationId).data;
  const { content: departments } = useDepartments(organizationId).data.departmentsResponse;

  const departmentsOptions: SelectOption[] = departments.map(dep => ({ label: dep.departmentName, value: dep.id }));
  const attributeOptions: SelectOption[] = employeesAttributes.map(({ name: label, id: value }) => ({ label, value }));

  return {
    departmentsOptions,
    attributeOptions,
  };
};
