import './override.scss';
import {
  Collapse, Divider, Form, Input
} from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';
import moment from 'moment';

import { FormInstance } from 'antd/es/form/Form';
import { PassengerInformation } from './components/passengerInformation/PassengerInformation';
import { AdditionalContactPerson } from './components/additionalContactPerson/AdditionalContactPerson';
import { InfoTrip } from './components/infoTrip/InfoTrip';
import { BaggageInformation } from './components/baggageInformation/BaggageInformation';
import { AdditionalInformation } from './components/additionalInformation/AdditionalInformation';
import { formatRublesWithoutRemainder, toRubles } from 'utils';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';

const { Panel } = Collapse;

interface GroupTransferProps {
  groupTransferForm: FormInstance;
  form: FormInstance;
  tariffId: string | undefined;
  setVisiblevisibleGroupTransferForm: React.Dispatch<React.SetStateAction<boolean>>;
}

export const GroupTransfer: FC<GroupTransferProps> = observer(({
  groupTransferForm, form, tariffId, setVisiblevisibleGroupTransferForm,
}) => {
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();
  const dispatcherTransport = tripStore.availableDispatcherTransport;
  const choosingBookingTransport = tripStore.availableChoosingBookingTransport;
  const cost = choosingBookingTransport
    ? choosingBookingTransport.calculated.cost
    : dispatcherTransport ? dispatcherTransport.calculated.cost
      : tripStore.classCosts.find(item => item.id === tariffId)?.cost;
  const information = tripStore.groupTransferInformation?.information;

  useEffect(() => {
    return () => setVisiblevisibleGroupTransferForm(false);
  }, []);

  useEffect(() => {
    information && groupTransferForm.setFieldsValue(information
    && {
      information: {
        ...information,
        dateFlight: information.dateFlight && moment(information.dateFlight),
        clientFullName: information.addContactFIO,
        clientInfoPhone: information.addContactPhone,
      },
      passengerCount: information.passengerCount,
    });
  }, []);

  return (
    <Form
      form={groupTransferForm}
      layout="vertical"
      name="create-request"
      size="middle"
    >
      <PassengerInformation form={form} />
      <Form.List name="information">
        {() => (
          <>
            <AdditionalContactPerson />
            <InfoTrip />
            <BaggageInformation information={information} />
            <AdditionalInformation information={information} />
          </>
        )}
      </Form.List>
      <Collapse ghost={true} expandIconPosition="right">
        <Panel
          className="collapse-comment"
          header="Комментарий для водителя"
          key="1"
        >
          <Form.Item name="commentary">
            <Input.TextArea
              rows={4}
            />
          </Form.Item>
        </Panel>
      </Collapse>
      <Divider />
      <div className="transfer_cost">
        <span>Общая стоимость</span>
        <span>{cost && formatRublesWithoutRemainder(toRubles(cost))}</span>
      </div>
    </Form>
  );
});
