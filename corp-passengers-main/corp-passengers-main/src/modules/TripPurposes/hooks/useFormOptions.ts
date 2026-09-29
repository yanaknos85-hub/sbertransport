import { useEmployeeActiveAttributes } from 'api/employee-attributes';
import { UUID } from 'utils/io-ts';
import { SelectOption } from '../types/types';

interface Export {
  attributeOptions: SelectOption[];
}

export const useFormOptions = ({ organizationId }: { organizationId: UUID }): Export => {
  const employeesAttributes = useEmployeeActiveAttributes(organizationId).data;

  const attributeOptions: SelectOption[] = employeesAttributes.map(({ name: label, id: value }) => ({ label, value }));

  return { attributeOptions };
};
