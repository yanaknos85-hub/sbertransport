export const formatName = (employee: { lastName?: string; firstName?: string; patronymic?: string | null }): string => (
  [employee?.lastName, employee?.firstName, employee?.patronymic].filter(Boolean).join(' ')
);

export const upFirst = (str: string) => {
  if (!str) return str;

  return str.charAt(0).toUpperCase() + str.slice(1);
};
