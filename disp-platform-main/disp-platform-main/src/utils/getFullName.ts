interface Human {
  firstName?: string | null;
  lastName?: string | null;
  patronymic?: string | null;
}

/**
 * Составляет полное имя из объекта человека (Фамилия Имя Отчество)
 * @param human - объект с полями firstName, lastName, patronymic
 * @returns строка с полным именем
 */
export const getFullName = <T extends Human>(human: T): string => [human.lastName, human.firstName, human.patronymic].filter(Boolean).join(' ');
