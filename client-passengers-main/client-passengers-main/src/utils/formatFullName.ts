import { Employee, EmployeeModel } from '@sber-sbertransport/mf-core';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';
import { IEmployee } from 'stores/Trip/Trip.interface';

export const formatFullName = (
  firstName: string | undefined,
  lastName: string | undefined,
  middleName: string | undefined
) => {
  const formattedFirstName: string = firstName ? `${firstName?.charAt(0)}.` : '';
  const formattedMiddleName: string = middleName ? `${middleName.charAt(0)}.` : '';
  const formattedLastNameName: string = lastName || '';
  const formattedFullName = `${formattedLastNameName} ${formattedFirstName}${formattedMiddleName}`;

  return formattedFullName;
};

export const getFullEmployeeName = (person: Employee | IDepartmentHead | EmployeeModel | IEmployee): string => formatFullName(person.firstName, person.lastName, person?.patronymic);

export const formatFullNameWithoutDots = (
  firstName: string | undefined,
  lastName: string | undefined,
  middleName: string | undefined
) => {
  const formattedFirstName: string = firstName || '';
  const formattedMiddleName: string = middleName || '';
  const formattedLastNameName: string = lastName || '';
  const formattedFullName = `${formattedLastNameName} ${formattedFirstName} ${formattedMiddleName}`;

  return formattedFullName;
};
