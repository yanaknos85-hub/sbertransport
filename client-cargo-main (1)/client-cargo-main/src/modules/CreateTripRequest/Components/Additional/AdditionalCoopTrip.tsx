import React, { Dispatch, SetStateAction } from 'react';
import {
  Form, FormInstance, Switch, Tabs
} from 'antd';

import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';
import { CoopTrip } from '../../types/types';
import { SuitableTrips } from '../SuitableTrips';

const { TabPane } = Tabs;

const placesQuantity = [
  { label: 'Одно', value: 1 },
  { label: 'Два', value: 2 },
  { label: 'Три', value: 3 },
];

export const AdditionalCoopTrip = ({
  form,
  isCoopTrip,
  setIsCoopTrip,
  coopTripData,
  switchDisabled,
  tabsDisabled,
  submitDisabled,
  onCommonFinish,
}: {
  form: FormInstance;
  isCoopTrip: boolean;
  setIsCoopTrip: Dispatch<SetStateAction<boolean>>;
  switchDisabled: boolean;
  tabsDisabled: boolean;
  submitDisabled: boolean;
  coopTripData: CoopTrip;
  onCommonFinish: (data: any, isTripSearching?: boolean, currentJoiningTrip?: TripSuitableModel) => void;
}): JSX.Element => (
  <>
    <Form.Item name="coopTrip" label={CreateRequestLinksTitles[CreateRequestLinks.tripType]}>
      <Switch
        defaultChecked={isCoopTrip}
        checked={isCoopTrip}
        onChange={setIsCoopTrip}
        checkedChildren={CreateRequestLinksTitles[CreateRequestLinks.cooperateType]}
        unCheckedChildren={CreateRequestLinksTitles[CreateRequestLinks.individualType]}
        disabled={switchDisabled}
      />
    </Form.Item>
    <Form.Item noStyle={true} shouldUpdate={true}>
      {({ getFieldValue }): JSX.Element | null => isCoopTrip
      && getFieldValue('coopTrip') && (
      <>
        <p>{CreateRequestLinksTitles[CreateRequestLinks.suitableOpportunity]}</p>
        <Form.Item name="passengerCount">
          <Tabs>
            {placesQuantity.map(i => (
              <TabPane
                tab={i.label}
                key={i.value}
                disabled={tabsDisabled}
              >
                <SuitableTrips
                  form={form}
                  disabled={submitDisabled}
                  coopTripData={coopTripData}
                  onCommonFinish={onCommonFinish}
                />
              </TabPane>
            ))}
          </Tabs>
        </Form.Item>
      </>
      )}
    </Form.Item>
  </>
);
