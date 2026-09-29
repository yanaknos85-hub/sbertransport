import React, { FC, useEffect, useState } from 'react';
import { Collapse } from 'antd';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';
import { AdditionalProps } from '../../types/types';
import { AdditionalCoopTrip } from './AdditionalCoopTrip';
import { AdditionalPassenger } from './AdditionalPassenger';
import { AdditionalPreferences } from './AdditionalPreferences';

export const Additional: FC<AdditionalProps> = ({
  form,
  transport,
  coopTripData,
  submitDisabled,
  onCommonFinish,
  disabledFields,
  personalCar,
  isExternal,
}) => {
  const [activeCollapsePanel, setActiveCollapsePanel] = useState<string[]>([]);
  const [isCoopTrip, setIsCoopTrip] = useState(true);

  const { TAXI, PERSONAL } = TransportTypeEnum;
  const {
    BUSINESS, COMFORT, ECONOMY,
  } = TaxiClassEnum;

  const commonTransportType = transport?.split('-')[0];
  const isPersonalTransportType = commonTransportType === PERSONAL;
  const hasOwnTransport = !!personalCar;
  const isPersonalTransportNotACar = personalCar?.transportType !== 'CAR';

  const isDisabledSwitch
    = (isPersonalTransportType && (!hasOwnTransport || isPersonalTransportNotACar))
    || !!disabledFields?.includes('coopTrip');

  useEffect(() => {
    const isCoopTripTypeSelected
      = commonTransportType
      && [TAXI, PERSONAL, BUSINESS, COMFORT, ECONOMY].includes(commonTransportType as TaxiClassEnum);

    if (isCoopTripTypeSelected) {
      setIsCoopTrip(true);
      setActiveCollapsePanel(['2']);
    } else {
      setIsCoopTrip(false);
      setActiveCollapsePanel([]);
    }
  }, [BUSINESS, COMFORT, ECONOMY, PERSONAL, TAXI, commonTransportType, transport]);

  // co-op trip on personal transport type only available for a car
  useEffect(() => {
    if (isPersonalTransportType && hasOwnTransport && isPersonalTransportNotACar) {
      setIsCoopTrip(false);
    }
  }, [commonTransportType, hasOwnTransport, isPersonalTransportNotACar, isPersonalTransportType]);

  const toggleAdditionals = (value: string | string[]): void => activeCollapsePanel.length === 1 ? setActiveCollapsePanel([]) : setActiveCollapsePanel([value[0]]);

  return (
    <Collapse
      bordered={false}
      activeKey={activeCollapsePanel}
      onChange={toggleAdditionals}
    >
      <Collapse.Panel
        header={CreateRequestLinksTitles[CreateRequestLinks.preferences]}
        key="1"
        forceRender={true}
      >
        <AdditionalPreferences disabled={disabledFields?.includes('preferences') || false} />
      </Collapse.Panel>

      {!isExternal && (
        <Collapse.Panel
          header={CreateRequestLinksTitles[CreateRequestLinks.suitableTrips]}
          key="2"
          forceRender={true}
        >
          <AdditionalCoopTrip
            form={form}
            coopTripData={coopTripData}
            isCoopTrip={isCoopTrip}
            setIsCoopTrip={setIsCoopTrip}
            submitDisabled={submitDisabled}
            switchDisabled={isDisabledSwitch}
            tabsDisabled={!!disabledFields?.includes('coopTrip')}
            onCommonFinish={onCommonFinish}
          />
        </Collapse.Panel>
      )}

      <Collapse.Panel
        header={CreateRequestLinksTitles[CreateRequestLinks.createForAnother]}
        key="3"
        forceRender={true}
      >
        <AdditionalPassenger disabled={disabledFields?.includes('passenger') || false} />
      </Collapse.Panel>
    </Collapse>
  );
};
