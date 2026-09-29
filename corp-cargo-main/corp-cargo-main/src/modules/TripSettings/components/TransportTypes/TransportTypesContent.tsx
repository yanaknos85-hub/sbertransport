import React, { FC, useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Switch } from 'antd';

import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile';
import { useSaveTransportTypes } from 'api/transport-types';
import { Button } from 'shared/components/Button/Button';
import { serviceTypes, NSave } from '../../constants/tripSettings';
import { useTripSettingsContext } from '../../context/TripSettings.context';

import { useServiceOptionsByOrganization } from '../../hooks/useServiceOptionsByOrganization';

import styles from './TransportTypes.module.scss';
import tripRulesStyles from '../../TripSettings.module.scss';

const OptionSwitch: React.FC<{
  serviceActive: boolean;
  onChange: (checked: boolean) => void;
  serviceTransportType: string;
  serviceRusName: string;
  disabled?: boolean;
}> = ({
  serviceActive, onChange, serviceTransportType, serviceRusName, disabled,
}) => (
  <div className={styles.SwitchGroup}>
    <span className={styles.Switch}>
      <Switch
        defaultChecked={serviceActive}
        size="default"
        onChange={onChange}
        data-attr={serviceTransportType}
        disabled={disabled}
      />
    </span>
    <span>{serviceRusName}</span>
  </div>
);

const usePrevious = (value: any) => {
  const ref = React.useRef();
  useEffect(() => {
    ref.current = value;
  });

  return ref.current;
};

const useLocationChange = (action: (a: any, b: any) => void) => {
  const location = useLocation();
  const prevLocation = usePrevious(location);
  useEffect(() => {
    action(location, prevLocation);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location]);
};

const TransportTypesContent: FC = () => {
  const { t } = useTranslation();
  const { organizationId } = useProfile().data;
  const {
    stepper: {
      step, steps, nextStep, prevStep,
    },
  } = useTripSettingsContext();

  const initialServiceOptions = useServiceOptionsByOrganization(organizationId);
  const [selectedServiceOptions, setSelectedServiceOptions] = useState(initialServiceOptions);
  const [isTransportTypesChanged, setIsTransportTypesChanged] = useState(false);
  const [saveTransportTypes, { isLoading: isSaving }] = useSaveTransportTypes(organizationId);

  const [needSave, setNeedSave] = useState<NSave>({ saveRequired: false, saveLocation: '' });

  useEffect(() => {
    if (needSave.saveRequired) {
      setNeedSave({ saveRequired: false, saveLocation: '' });
    }
  }, [needSave.saveRequired]);

  useLocationChange((location, prevLocation) => {
    if (!prevLocation) {
      return;
    }

    const prevPath = (prevLocation ? prevLocation.pathname : '').split('/');
    const currPath = location?.pathname.split('/');
    const ifNeedSave = prevPath[2] !== currPath[2];
    const locationToSave = prevPath[2];
    if (ifNeedSave) {
      setNeedSave({ saveRequired: true, saveLocation: locationToSave });
    }
  });

  const onChange = (transportTypeSwitchName: string, switchValue: boolean) => {
    const changedServiceOptions = selectedServiceOptions.map(item => ({
      ...item,
      active: item.transportType === transportTypeSwitchName ? switchValue : item.active,
    }));
    setIsTransportTypesChanged(true);
    setSelectedServiceOptions(changedServiceOptions);
    saveTransportTypes(changedServiceOptions);
  };

  const handleSave = () => {
    if (!isTransportTypesChanged) {
      return;
    }
    const transportTypesToSave = selectedServiceOptions.map(({ transportType, active }) => ({ transportType, active }));
    saveTransportTypes(transportTypesToSave);
  };

  return (
    <>
      {selectedServiceOptions.map(service => (
        service.category === serviceTypes.CARGO_TRANSPORTATION && (
          <OptionSwitch
            key={service.transportType + service.active}
            serviceActive={service.active}
            onChange={checked => {
              onChange(service.transportType, checked);
            }}
            serviceTransportType={service.transportType}
            serviceRusName={service.title}
          />
        )
      ))}
      <div className={tripRulesStyles.controlButtons}>
        <Button
          htmlType="button"
          size="middle"
          type="primary"
          onClick={() => {
            handleSave();
            nextStep();
          }}
        >
          {t.global.stepForward}
        </Button>
      </div>
    </>
  );
};

export default TransportTypesContent;
