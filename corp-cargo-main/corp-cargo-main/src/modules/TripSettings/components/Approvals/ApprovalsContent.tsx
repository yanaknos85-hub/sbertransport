import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { useProfile } from 'api/profile';
import { Button } from 'shared/components/Button/Button';
import { Tab } from 'shared/components/Tab';
import { TabPane } from 'shared/components/Tab/TabPane';
import { NoServicesEnabledWarning } from '../NoServicesEnabledWarning';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';

import { useTripSettingsContext } from '../../context/TripSettings.context';

import Approvals from 'modules/Approvals/Approvals';

import { useServiceOptionsByOrganization } from '../../hooks/useServiceOptionsByOrganization';

import { serviceTypes } from '../../constants/tripSettings';
import { allowedTransportTypesForApprovals } from '../../constants/approvals';

import styles from '../../TripSettings.module.scss';

const ApprovalsContent: FC = () => {
  const { t } = useTranslation();
  const orgId = useProfile().data.organizationId;
  const { stepper: { nextStep, prevStep } } = useTripSettingsContext();
  const serviceOptions = useServiceOptionsByOrganization(orgId);

  const activePassengersServices = serviceOptions.filter(
    ({
      active, transportType, category,
    }) => active
    && category === serviceTypes.EMPLOYEE_TRANSPORTATION
    && allowedTransportTypesForApprovals.includes(transportType as TransportTypes)
  );
  const isNoAnyActiveService = !activePassengersServices.length;

  return (
    <>
      {isNoAnyActiveService ? (
        <NoServicesEnabledWarning />
      ) : (
        <Tab className={styles.tabs}>
          { activePassengersServices.map(({ transportType, title: rusName }) => (
            <TabPane
              tab={rusName}
              key={transportType}
              theme="card"
            >
              <Approvals selectedTransportType={transportType as TransportTypes} />
            </TabPane>
          )) }
        </Tab>
      )}

      <div className={styles.controlButtons}>
        <Button
          htmlType="button"
          size="middle"
          onClick={prevStep}
        >
          {t.global.stepBack}
        </Button>
        <Button
          htmlType="button"
          size="middle"
          type="primary"
          onClick={nextStep}
        >
          {t.global.stepForward}
        </Button>
      </div>
    </>
  );
};

export default ApprovalsContent;
