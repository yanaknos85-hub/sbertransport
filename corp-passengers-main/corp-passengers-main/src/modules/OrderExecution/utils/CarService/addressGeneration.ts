import { VALUE_NOT_FOUND } from '../../constants/General';
import type { IAddressInfo } from '../../interfaces/CarService/General';

const useAddressGeneration = ({
  city, street, building, house, structure,
}: IAddressInfo): string => {
  if (!city && !street) {
    return VALUE_NOT_FOUND;
  }

  let address = '';

  if (city) {
    address = city;
  }

  if (street) {
    address += `, ${street}`;
  }

  if (house) {
    address += `, ${house}`;
  }

  if (building) {
    address += `, ${building}`;
  }

  if (structure) {
    address += `, ${structure}`;
  }

  return address;
};

export default useAddressGeneration;
