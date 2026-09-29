import { TripPurpose } from '../stores/TripPurposes/TripPurpose.interface';
import { UUID } from './io-ts';

export const convertPurposesToOptions = (purposes: TripPurpose[]) => {
  const options = new Map<string, string[]>();
  purposes.forEach(purpose => options.set(purpose.label, [...(options.get(purpose.label) ?? []), purpose.id]));
  return [...Array.from(options)].map(el => ({ label: el[0], value: el[1].join(',') }));
};

export const prepareSubmitPurposes = (purposeValues: string[]) => {
  const result: { id: string }[] = [];
  purposeValues.forEach(value => {
    const ids = value.split(',');
    ids.forEach(id => result.push({ id }));
  });
  return result;
};

export const getPurposesIds = (value?: { key?: UUID; label?: string }[]): { id: string }[] | undefined => (
  value && prepareSubmitPurposes([...value.map(el => el.key!)])
);
