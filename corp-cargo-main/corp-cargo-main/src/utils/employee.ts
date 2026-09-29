export const nameWithInitials = ({
  firstName,
  patronymic,
  lastName,
}: {
  firstName: string;
  lastName: string;
  patronymic?: string;
}): string => {
  if (!firstName && !patronymic && !lastName) {
    return '';
  }

  return `${firstName.charAt(0)}. ${patronymic ? `${patronymic.charAt(0)}. ` : ''}${lastName}`;
};
// Todo:Поменял в рамках задачи чтобы заработала отчетность ждем стабильности
export const fullName = ({
  firstName,
  patronymic,
  lastName,
}: {
  firstName: string | null | undefined;
  patronymic?: string | null;
  lastName: string | null | undefined;
}): string => [firstName, patronymic, lastName].filter(Boolean).join(' ');

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
  return '—';
};

export const fullNameWithPhoneNumber = ({
  firstName,
  patronymic,
  lastName,
  contactPhone,
}: {
  firstName: string | null | undefined;
  patronymic: string | null | undefined;
  lastName: string | null | undefined;
  contactPhone: string | null | undefined;
}): string => `${[firstName, patronymic, lastName].filter(Boolean).join(' ')}, ${contactPhone ?? ''}`;

export const fullVehicleInfo = ({
  brandName,
  model,
  color,
  registrationNumber,
}: {
  brandName: string | null | undefined;
  model: string | null | undefined;
  color?: string | null;
  registrationNumber: string | null | undefined;
}): string => [brandName, model, color, registrationNumber].filter(Boolean).join(' ');

// TODO: can we remove this, looks like a `nameWithInitials` duplicate
export const shortName = ({
  firstName,
  patronymic,
  lastName,
}: {
  firstName: string;
  lastName: string;
  patronymic?: string;
}): string => `${firstName.charAt(0)}. ${patronymic ? `${patronymic.charAt(0)}. ` : ''}${lastName}`;
