import React, {
  FC, Suspense, memo, useEffect, useState, useCallback
} from 'react';
import { useTranslation } from 'i18n';

import { Button } from 'antd';

import { TransportTypes, TransportType } from 'stores/TransportTypes/TransportTypes.interface';

import { useSharedRideSettings, useSaveSharedRideSettings } from 'api/shared-ride-settings';
import { useProfile } from 'api/profile';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { SaveOutlined } from '@ant-design/icons';
import ColoredSlider from 'shared/components/ColoredSlider';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import SelectTransportType from 'shared/components/SelectTransportType';

import styles from './styles.module.scss';

const defaultTransportType = TransportTypes.TAXI;

const COLORS: [string, string, string] = ['#ff0000', '#dddd17', '#00ff00'];

export const EconomyIndicationSettings = () => (
  <ErrorBoundary>
    <Suspense fallback={<SpinWrapped />}>
      <EconomyIndicationSettingsInner />
    </Suspense>
  </ErrorBoundary>
);
const EconomyIndicationSettingsInner = memo(() => {
  const [transportType, setTransportType] = useState<TransportType['name']>(defaultTransportType);

  const { organizationId } = useProfile().data;

  // @ts-ignore
  const { data: settingsByTransportType, isLoading } = useSharedRideSettings(organizationId);

  const [colorRange, setColorRange] = useState<[number, number]>([30, 60]);

  const resetSettings = useCallback(() => {
    const settings = settingsByTransportType[transportType];
    const lowerBound = settings?.economyIndicationYellowRangeLowerBorder ?? 30;
    const upperBound = settings?.economyIndicationYellowRangeUpperBorder ?? 60;
    setColorRange([lowerBound, upperBound]);
  }, [settingsByTransportType, transportType]);

  const [saveSettings, { isLoading: isSaving }] = useSaveSharedRideSettings();

  const handleSave = () => saveSettings({
    // @ts-ignore
    organizationId,
    transportType,
    settings: {},
    ...settingsByTransportType[transportType],
    economyIndicationYellowRangeLowerBorder: colorRange[0],
    economyIndicationYellowRangeUpperBorder: colorRange[1],
  });

  useEffect(resetSettings, [resetSettings]);

  const { t } = useTranslation();

  return (
    <div className={styles.economyIndicationSettings}>
      <span className={styles.transportTypeLabel}>
        {t.SharedRides.TransportType}
        :
      </span>
      <SelectTransportType
        value={transportType}
        onChange={setTransportType}
        className={styles.transportTypeSelect}
      />
      <ColoredSlider
        value={colorRange}
        onChange={setColorRange}
        colors={COLORS}
        className={styles.coloredSlider}
      />
      <div className={styles.colorRangeDescriptions}>
        <ColorRangeDescription
          from={0}
          to={colorRange[0]}
          color={COLORS[0]}
        />
        <ColorRangeDescription
          from={colorRange[0]}
          to={colorRange[1]}
          color={COLORS[1]}
        />
        <ColorRangeDescription
          from={colorRange[1]}
          to={100}
          color={COLORS[2]}
        />
      </div>
      <div className={styles.buttonBar}>
        <Button
          icon={<SaveOutlined />}
          size="middle"
          onClick={handleSave}
        >
          {t.global.save}
        </Button>
        <Button
          size="middle"
          danger
          onClick={resetSettings}
        >
          {t.global.cancel}
        </Button>
      </div>
      {(isLoading || isSaving) && <SpinWrapped mask />}
    </div>
  );
});

export const ColorRangeDescription: FC<{ from: number; to: number; color: string }> = ({
  from, to, color,
}) => {
  const { t } = useTranslation();
  return (
    <div className={styles.colorRangeDescription}>
      <div className={styles.color} style={{ borderColor: color }} />
      {from !== to && (
        <span>
          {' '}
          {t.SharedRides.EconomyIndicationSettings.ColorRangeFrom}
          {' '}
          {from !== 100 && from !== 0 ? from + 1 : from}
          {' '}
          {t.SharedRides.EconomyIndicationSettings.ColorRangeTo}
          {' '}
          {to}
          {' '}
        </span>
      )}

      {from === to && 'цвет неактивен'}
    </div>
  );
};
