export const formatFullName = (
  firstName: string | undefined | null,
  lastName: string | undefined | null,
  patronymic: string | undefined | null
) => [firstName, lastName, patronymic].filter(Boolean).join(' ');
