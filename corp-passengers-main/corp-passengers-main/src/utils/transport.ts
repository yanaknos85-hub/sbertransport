import { VALUE_NOT_FOUND } from 'constants/constants.app';
import { TransportType } from 'stores/TransportTypes/TransportTypes.interface';

export const getTransportTypeName = (transportTypes: TransportType[]) => {
  const transportTypeNamesMap = transportTypes.reduce(
    (map, { name, rusName }) => map.set(name, rusName),
    new Map<string, string>()
  );

  return (transportType: string) => transportTypeNamesMap.get(transportType) ?? VALUE_NOT_FOUND;
};
