import { useBoolean } from 'ahooks';

import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import {
  NegativeEmotionsTitles, NegativeEmotionsTitlesCarsharing, NegativeEmotionsTitlesPersonal, NegativeEmotionsTitlesPublic, PositiveEmotionsTitles, PositiveEmotionsTitlesCarsharing, PositiveEmotionsTitlesPersonal, PositiveEmotionsTitlesPublic
} from './RateModal.constants';

import { useCurrentTripRequest } from 'shared/hooks/trip';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

interface useRateReturnType {
  showModal: () => void;
  hideModal: () => void;
  showModalFinishRate: () => void;
  hideModalFinishRate: () => void;
  changeShowDetailed: () => void;
  changeHideDetailed: () => void;
  isModalVisible: boolean;
  isFinishRate: boolean;
  isNotDetailedVisible: boolean;
  onFinish: (values: any) => void;
  emotions: Record<string, string> | undefined;
  isPositiveRate: boolean;
  isRated: boolean;
}

export const useRate = (rateValue: number): useRateReturnType => {
  const [isModalVisible, { setTrue: showModal, setFalse: hideModal }] = useBoolean(false);
  const [isNotDetailedVisible, { setTrue: changeShowDetailed, setFalse: changeHideDetailed }] = useBoolean(false);
  const [isFinishRate, { setTrue: showModalFinishRate, setFalse: hideModalFinishRate }] = useBoolean(false);

  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const { currentTripRequest, rateTripRequest } = tripStore;

  const isPositiveRate = rateValue >= 4;
  const isRated = rateValue > 0;

  const getPositiveEmotions = (tripType: TransportTypeEnum | undefined) => {
    switch (tripType) {
      case TransportTypeEnum['TAXI']:
      {
        return PositiveEmotionsTitles;
      }
      case TransportTypeEnum['PERSONAL']:
      {
        return PositiveEmotionsTitlesPersonal;
      }
      case TransportTypeEnum['CARSHARING']:
      {
        return PositiveEmotionsTitlesCarsharing;
      }
      case TransportTypeEnum['PUBLIC']:
      {
        return PositiveEmotionsTitlesPublic;
      }
    }
  };

  const getNegativeEmotions = (tripType: TransportTypeEnum | undefined) => {
    switch (tripType) {
      case TransportTypeEnum['TAXI']:
      {
        return NegativeEmotionsTitles;
      }
      case TransportTypeEnum['PERSONAL']:
      {
        return NegativeEmotionsTitlesPersonal;
      }
      case TransportTypeEnum['CARSHARING']:
      {
        return NegativeEmotionsTitlesCarsharing;
      }
      case TransportTypeEnum['PUBLIC']:
      {
        return NegativeEmotionsTitlesPublic;
      }
    }
  };

  const PositiveEmotions = getPositiveEmotions(currentTripRequest?.transportType);
  const NegativeEmotions = getNegativeEmotions(currentTripRequest?.transportType);

  const emotions: Record<string, string> | undefined = isPositiveRate ? PositiveEmotions : NegativeEmotions;

  useCurrentTripRequest();
  // TODO: ПЕРЕДЕЛАТЬ!!!
  function onFinish(values: any): void {
    if (currentTripRequest) {
      rateTripRequest(
        {
          rating: values.rating,
          ratingComment: values.additional,
          advantages: values.emotions,
          drawbacks: values.emotions,
        },
        currentTripRequest.id
      )
        .then(showModalFinishRate);
    }
  }

  return {
    showModal,
    hideModal,
    onFinish,
    changeShowDetailed,
    changeHideDetailed,
    showModalFinishRate,
    hideModalFinishRate,
    isNotDetailedVisible,
    isFinishRate,
    emotions,
    isPositiveRate,
    isRated,
    isModalVisible,
  };
};
