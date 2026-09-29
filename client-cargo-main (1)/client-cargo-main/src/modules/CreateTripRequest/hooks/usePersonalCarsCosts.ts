import { PersonalCar } from '@sber-sbertransport/mf-core';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { ITripCalculateRequest } from 'stores/Trip/Trip.interface';

export const usePersonalCarsCosts = (): {
  getPersonalCarsCosts: (data: ITripCalculateRequest, cars: PersonalCar[]) => void;
} => {
  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();

  const getPersonalCarsCosts = (data: ITripCalculateRequest, cars: PersonalCar[]) => {
    if (!cars) {
      return;
    }
    tripStore.clearPersonalCarsCosts();

    Promise.all(cars.map(el => tripStore.getPersonalCarCost({ ...data, engineVolume: el.engineVolume })))
      .then(responses => {
        responses.forEach((response, i) => {
          if (response) {
            tripStore.addPersonalCost({ ...response[0], id: cars[i].id });
          }
        });
      })
      // eslint-disable-next-line no-console
      .catch(e => console.error(e.message));
  };
  return { getPersonalCarsCosts };
};
