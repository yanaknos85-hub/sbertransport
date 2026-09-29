import { FormInstance } from 'antd/es/form/Form';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripStore, TaxiClassEnum, TCoopTripsSettings } from 'stores/Trip/Trip.interface';

// TODO добавить поддержку не только ЛТ, если это не такси класс
const checkTaxi = (taxiClass: TaxiClassEnum): string => {
  if (taxiClass && [TaxiClassEnum.ECONOMY, TaxiClassEnum.COMFORT, TaxiClassEnum.BUSINESS].includes(taxiClass)) {
    return TransportTypeEnum.TAXI;
  }
  return TransportTypeEnum.PERSONAL;
};

export const chooseEconomyColor = ({
  economyPercent,
  tripStore,
  form,
}: {
  economyPercent: number;
  tripStore: ITripStore;
  form: FormInstance;
}): { backgroundColor: string; color: string } => {
  const { coopTripsSettings } = tripStore;
  const formValues = form.getFieldsValue();

  enum COLORS {
    green = 'var(--font-green-color)',
    yellow = 'var(--font-yellow-color)',
    red = 'var(--font-red-color)',
    backgroundGreen = 'var(--background-green-color)',
    backgroundYellow = 'var(--background-yellow-color)',
    backgroundRed = 'var(--background-red-color)',
  }

  const settings: TCoopTripsSettings
    = coopTripsSettings.find(
      settingItem => settingItem.transportType === checkTaxi(formValues.taxiClass as TaxiClassEnum)
    ) || ({} as TCoopTripsSettings);

  if (!settings.id) {
    return { backgroundColor: COLORS.backgroundGreen, color: COLORS.green };
  }

  const {
    economyIndicationYellowRangeLowerBorder: yellowLowerBorder,
    economyIndicationYellowRangeUpperBorder: yellowUpperBorder,
  } = settings;

  return (
    (economyPercent <= yellowLowerBorder && { backgroundColor: COLORS.backgroundRed, color: COLORS.red })
    || (economyPercent > yellowLowerBorder
    && economyPercent <= yellowUpperBorder && { backgroundColor: COLORS.backgroundYellow, color: COLORS.yellow })
    || (economyPercent >= yellowLowerBorder && {
      backgroundColor: COLORS.backgroundGreen,
      color: COLORS.green,
    }) || { backgroundColor: COLORS.backgroundGreen, color: COLORS.green } // green is a default color
  );
};
