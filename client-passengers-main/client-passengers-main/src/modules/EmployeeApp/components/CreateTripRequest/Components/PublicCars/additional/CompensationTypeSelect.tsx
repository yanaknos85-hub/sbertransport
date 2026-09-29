/* eslint-disable @typescript-eslint/no-explicit-any */
import '../../../styles/override.css';
import { Form, Select, Tooltip } from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { LabeledValue } from 'antd/es/select';
import { observer } from 'mobx-react';
import React, {
  Dispatch, FC, SetStateAction, useEffect, useState
} from 'react';
import ChevronSmall from 'shared/components/Images/view/menu 2.0/ChevronSmall';

import { AvailablePublicTTCompensationType } from 'api/trip-requests';

import { ValidationRules } from 'shared/fieldValidationRules';

import {
  compensationTypeLabel, compensationTypeTitle, deleteTooltip, tripsInfoTitle
} from '../constants';
import Delete from 'shared/components/Images/delete.svg';
import { FormInstance } from 'antd/es/form/Form';
import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';

interface Props {
  field: FormListFieldData;
  compensationType: TransportCompensations;
  setCompensationType: Dispatch<SetStateAction<TransportCompensations>>;
  availablePublicCompensation: AvailablePublicTTCompensationType[];
  index: number;
  resetFields: (index: number, selectedCompensationType: string) => void;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  remove: (index: number | number[]) => void;
  updateGeneralSum: () => void;
  form: FormInstance;
}

export const CompensationTypeSelect: FC<Props> = observer(
  ({
    field,
    compensationType,
    setCompensationType,
    availablePublicCompensation,
    index,
    resetFields,
    setIsSaveCompensation,
    remove,
    updateGeneralSum,
    form,
  }) => {
    const [compensations, setCompensations] = useState<LabeledValue[]>([]);

    useEffect(() => {
      const res = availablePublicCompensation.map(y => ({
        value: y.name,
        label: y.rusName,
      }));
      setCompensations(res);
    }, [availablePublicCompensation]);

    const compensationTypeChange = (e: TransportCompensations): void => {
      setIsSaveCompensation(false);
      setCompensationType(e);
      resetFields(index, e);
    };

    const updateAfterMinus = (removeFunction: React.Dispatch<number>, name: number): void => {
      if (form.getFieldValue(tripsInfoTitle).length === 1) {
        setIsSaveCompensation(false);
        setCompensationType(TransportCompensations.CITY_TRIP_COMPENSATION);
        resetFields(index, TransportCompensations.CITY_TRIP_COMPENSATION);
        updateGeneralSum();
      } else {
        removeFunction(name);
        updateGeneralSum();
      }
    };

    const getDeleteButton = (
      removeFunction: React.Dispatch<number>,
      name: number
    ): JSX.Element | null => (
      <Tooltip title={deleteTooltip}>
        <div onClick={() => updateAfterMinus(removeFunction, name)}>
          <img
            src={Delete}
            alt="delete"
          />
        </div>
      </Tooltip>
    );

    return (
      <div className="wrapper_compensation">
        <div className="compensation_header_ticket">
          <span className="ticketNumber">{`Билет ${index + 1}`}</span>
          {getDeleteButton(remove, field.name)}
        </div>
        <span className="compensation_title_item">{compensationTypeLabel}</span>
        <Form.Item
          {...field}
          key={compensationTypeTitle}
          name={[field.name, compensationTypeTitle]}
          rules={[ValidationRules.general.required]}
          initialValue={compensationType}
        >
          <Select
            suffixIcon={(
              <div style={{ borderLeft: '1px solod grey', padding: '5px 18px' }}>
                <ChevronSmall />
              </div>
            )}
            placeholder="вид компенсации"
            onChange={(e: TransportCompensations): void => compensationTypeChange(e)}
            options={compensations}
            defaultValue={compensationType}
            value={compensationType}
          />
        </Form.Item>
      </div>
    );
  }
);
