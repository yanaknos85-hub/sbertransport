import React, {
  FC, useState, useEffect, useRef
} from 'react';

import { useTranslation } from 'i18n';
import { useProfile } from 'api/profile';
import { useSharedRideSettings, useSaveSharedRideSettings } from 'api/shared-ride-settings';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { SliderRangeValue } from './EconomyIndicationSettings/types';
import { SharedRideSettingsByTansportType } from './SharedRideSettings/SharedRidesSettingsByTransportType';
import { EconomyIndicationSettings } from './EconomyIndicationSettings/EconomyIndicator';
import { ControlTime } from './ControlTime';
import { DEFAULT_LOWER_ECONOMY_TRESHOLD, DEFAULT_UPPER_ECONOMY_TRESHOLD } from './EconomyIndicationSettings/constants';

import styles from './styles.module.scss';

const useIsMounted = () => {
  const isMounted = useRef(false);
  useEffect(() => {
    isMounted.current = true;
    return () => {
      isMounted.current = false;
    };
  }, []);
  return () => isMounted.current;
};

const SharedRides: FC<{ transportType: TransportTypes }> = ({ transportType }) => {
  const { t } = useTranslation();
  const { organizationId } = useProfile().data;
  const mounted = useIsMounted();
  // @ts-ignore
  const { data: settingsByTransportType, isLoading } = useSharedRideSettings(organizationId);
  const initialIndicatorValue: SliderRangeValue = [
    settingsByTransportType[transportType]?.economyIndicationYellowRangeLowerBorder ?? DEFAULT_LOWER_ECONOMY_TRESHOLD,
    settingsByTransportType[transportType]?.economyIndicationYellowRangeUpperBorder ?? DEFAULT_UPPER_ECONOMY_TRESHOLD,
  ];
  const [economyTresholdRange, setEconomyTresholdRange] = useState<SliderRangeValue>(initialIndicatorValue);
  const [saveSettings, { isLoading: isSaving }] = useSaveSharedRideSettings();

  const handleSave = () => {
    saveSettings({
      // @ts-ignore
      organizationId,
      transportType,
      settings: {},
      ...settingsByTransportType[transportType],
      economyIndicationYellowRangeLowerBorder: economyTresholdRange[0],
      economyIndicationYellowRangeUpperBorder: economyTresholdRange[1],
    });
  };

  useEffect(
    () => () => {
      // save on unmount
      if (!mounted()) {
        handleSave();
      }
    },
    [handleSave, mounted]
  );

  return (
    <div className={styles.page}>
      <SharedRideSettingsByTansportType transportType={transportType} />

      <hr className={styles.headerDivider} />

      <div className={styles.bottomBlock}>
        <EconomyIndicationSettings
          title={t.SettingsTripRules.sharedRides.economyIndicator.title}
          value={economyTresholdRange}
          initialRangeValue={initialIndicatorValue}
          setRangeValue={setEconomyTresholdRange}
          busy={isLoading || isSaving}
        />
        <ControlTime />
      </div>
    </div>
  );
};

export default SharedRides;
