interface Human {
  firstName?: string | null;
  lastName?: string | null;
  patronymic?: string | null;
}

export const getFullName = <T extends Human>(human: T) => [human.lastName, human.firstName, human.patronymic].filter(Boolean).join(' ');
