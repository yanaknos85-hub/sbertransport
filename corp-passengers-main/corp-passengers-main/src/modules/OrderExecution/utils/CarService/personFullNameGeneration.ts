import { VALUE_NOT_FOUND } from '../../constants/General';
import type { IPersonInfo } from '../../interfaces/CarService/General';

const personFullNameGeneration = ({
  firstName, lastName, patronymic,
}: IPersonInfo): string => {
  if (!lastName) {
    return VALUE_NOT_FOUND;
  }

  return `${lastName} ${firstName ?? ''} ${patronymic ?? ''}`.trim();
};

export default personFullNameGeneration;
