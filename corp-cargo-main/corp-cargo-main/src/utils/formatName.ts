import { Employee } from 'stores/Employee/Employee.interface';

export const formatName = (employee: Pick<Employee, 'firstName' | 'lastName' | 'patronymic'>): string => [employee?.lastName, employee?.firstName, employee?.patronymic].filter(Boolean).join(' ');

export const formatNameShort = (
  employee: Pick<Employee, 'firstName' | 'lastName' | 'patronymic'>,
  isReverseNameOrder = false
): string => {
  if (employee) {
    const {
      lastName, firstName, patronymic,
    } = employee;
    const fio: (string | undefined)[] = [lastName];
    const initials = [firstName && `${firstName[0]}.`.toUpperCase(), patronymic && `${patronymic[0]}.`.toUpperCase()];

    fio[isReverseNameOrder ? 'unshift' : 'push'](...initials);

    return fio.filter(Boolean).join(' ');
  }

  return '';
};
