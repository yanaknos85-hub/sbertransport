export const fullNameLastFirstPat = (
  employee?: {
    lastName?: string | null;
    firstName?: string | null;
    patronymic?: string | null;
  } | null
): string => {
  if (employee) {
    const {
      firstName, patronymic, lastName,
    } = employee;
    return [lastName, firstName, patronymic].filter(Boolean).join(' ');
  }
  return '-';
};
