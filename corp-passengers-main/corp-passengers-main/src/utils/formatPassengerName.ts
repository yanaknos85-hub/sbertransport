export const formatPassengerName = (passenger?: {
  lastName?: string;
  firstName?: string;
  patronymic?: string | null;
}): string => {
  if (!passenger) {
    return '';
  }
  return [passenger.lastName, passenger.firstName, passenger.patronymic].filter(x => x).join(' ');
};
