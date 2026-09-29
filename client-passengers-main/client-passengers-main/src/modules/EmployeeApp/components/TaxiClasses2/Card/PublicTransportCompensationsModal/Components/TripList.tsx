/* eslint-disable @typescript-eslint/no-explicit-any */
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import {
  Button, Form, FormInstance, Tooltip
} from 'antd';
import { FormListFieldData } from 'antd/es/form/FormList';
import { observer } from 'mobx-react';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { AvailablePublicTTCompensationType, AvailablePublicTransportType } from 'api/trip-requests';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { TTariffPublic } from 'stores/Trip/Trip.interface';
import { formatRubles } from 'utils';

import {
  addButtonTitle, deleteTooltip, errorEmptyFields, generalSumLabel, tripsInfoTitle
} from './constants';
import { TripItems } from './items/TripItems';
import styles from './style.module.scss';

export interface CityTripListProps {
  form: FormInstance;
  generalSum: number;
  updateGeneralSum: () => void;
  availableTransportTypes: AvailablePublicTransportType[];
  availablePublicCompensation: AvailablePublicTTCompensationType[];
  currentTariff?: TTariffPublic;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
}

export const TripList: FC<CityTripListProps> = observer(
  ({
    form,
    generalSum,
    updateGeneralSum,
    availableTransportTypes,
    availablePublicCompensation,
    currentTariff,
    setIsSaveCompensation,
  }) => {
    const { logger } = useAppStoreContext();

    const updateAfterMinus = (removeFunction: any, name: number): void => {
      removeFunction(name);
      updateGeneralSum();
    };

    const getItem = (index: number, field: FormListFieldData): JSX.Element => (
      <TripItems
        key={index}
        index={index}
        form={form}
        field={field}
        updateGeneralSum={updateGeneralSum}
        availableTransportTypes={availableTransportTypes}
        availablePublicCompensation={availablePublicCompensation}
        currentTariff={currentTariff}
        setIsSaveCompensation={setIsSaveCompensation}
      />
    );

    const getDeleteButton = (index: number, removeFunction: React.Dispatch<number>, name: number): JSX.Element | null => index > 0 ? (
      <Tooltip title={deleteTooltip}>
        <MinusCircleOutlined
          onClick={(): void => {
            updateAfterMinus(removeFunction, name);
          }}
          className={styles.deleteIcon}
        />
      </Tooltip>
    ) : null;

    const addItemToForm = (addFunction: any): void => {
      form
        .validateFields()
        .then(() => {
          addFunction();
          updateGeneralSum();
        })
        .catch(() => {
          logger.toMessage('error', errorEmptyFields);
        });
    };

    return (
      <Form.List name={tripsInfoTitle} initialValue={[0]}>
        {(fields, { add, remove }): JSX.Element => (
          <>
            {fields.map((field, index) => (
              // eslint-disable-next-line react/no-array-index-key
              <div key={field.key + index}>
                {/* FIXME react/no-array-index-key */}
                {getDeleteButton(index, remove, field.name)}
                <Form.Item
                  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
                  shouldUpdate={(prevValues, curValues) => {
                    // FIXME @typescript-eslint/explicit-function-return-type
                    // eslint-disable-next-line no-unused-expressions
                    return (
                      prevValues.sights !== curValues.sights
                      || prevValues.compensationType !== curValues.compensationType
                    );
                  }}
                >
                  {getItem(index, field)}
                </Form.Item>
              </div>
            ))}
            <Form.Item>
              <Button
                icon={<PlusOutlined />}
                type="dashed"
                block={true}
                onClick={(): void => {
                  addItemToForm(add);
                }}
              >
                {addButtonTitle}
              </Button>
            </Form.Item>
            <Form.Item label={generalSumLabel}>
              <span>{formatRubles(generalSum / 100)}</span>
            </Form.Item>
          </>
        )}
      </Form.List>
    );
  }
);
