import { formatPhoneNumber } from 'utils';

export const parsejoinedPassengers = array => {
  return array.map(obj => {
    const {
      firstName, lastName, patronymic, mobilePhone,
    } = obj;

    return `${lastName} ${firstName} ${patronymic} ${formatPhoneNumber(mobilePhone)}`;
  }).join(',\n');
};
