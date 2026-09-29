import '../../../TaxiClasses/Card/override.scss';

import { observer } from 'mobx-react';
import React, { Dispatch, FC, SetStateAction } from 'react';

import { Button, Divider, Form } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { addButtonTitle } from 'modules/EmployeeApp/components/TaxiClasses/Card/PublicTransportCompensationsModal/Components/constants';
import { FormInstance } from 'antd/es/form/Form';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { errorEmptyFields, FormItemNames } from './constants';
import { PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import { FormListFieldData } from 'antd/es/form/FormList';
import { formatRublesWithoutRemainder } from 'utils/MoneyUtils';
import { useGetAvailablePublicTransportTypes, useGetAvailablePublicTTCompensations } from 'api/trip-requests';
import { TripItems } from './items/TripItems';
import { PublicApprovals } from 'api/trip-requests';
import { StoreNames } from 'stores';
import { generalSumLabel, generalSumTitle } from './constants';

interface PublicCarsProps {
  childForm: FormInstance;
  publicApprovals: PublicApprovals | null;
  setIsSaveCompensation: Dispatch<SetStateAction<boolean>>;
  setGeneralSum: Dispatch<SetStateAction<number>>;
  generalSum: number;
}

export const PublicCars: FC<PublicCarsProps> = observer(
  ({
    childForm,
    publicApprovals,
    setIsSaveCompensation,
    setGeneralSum,
    generalSum,
  }): JSX.Element => {
    const { [StoreNames.tripStore]: tripStore, logger } = useAppStoreContext();
    const countItemForForm = childForm.getFieldValue(FormItemNames.tripsInfo)?.length >= 10;

    const availableTransportTypes = useGetAvailablePublicTransportTypes().data;
    const availablePublicCompensation = useGetAvailablePublicTTCompensations().data;

    const getSum = (array: PublicInfoCard[], compensationType: TransportCompensations, fieldTitle: string): number => array
      .filter((x: PublicInfoCard) => x.compensationType === compensationType && (x.cost || x.sum))
      .reduce((total: number, element: any) => total + element[fieldTitle], 0);

    const updateGeneralSum = (): void => {
      const formTripsInfo = childForm.getFieldValue(FormItemNames.tripsInfo);
      const citySum = getSum(formTripsInfo, TransportCompensations.CITY_TRIP_COMPENSATION, 'sum');
      const suburbSum = getSum(formTripsInfo, TransportCompensations.SUBURB_TRIP_COMPENSATION, 'cost') * 100;
      const paidServices = getSum(formTripsInfo, TransportCompensations.PAID_SERVICES_COMPENSATION, 'cost') * 100;
      const travelCardSum = getSum(formTripsInfo, TransportCompensations.TRAVEL_CARD_COMPENSATION, 'cost') * 100;
      const finalSum = citySum + suburbSum + paidServices + travelCardSum;
      setGeneralSum(finalSum);
    };

    const addItemToForm = (addFunction: (defaultValue?: string, insertIndex?: number | undefined) => void): void => {
      childForm
        .validateFields()
        .then(() => {
          addFunction();
          updateGeneralSum();
        })
        .catch(() => {
          logger.toMessage('error', errorEmptyFields);
        });
    };

    const getItem = (
      index: number,
      field: FormListFieldData,
      remove: (index: number | number[]) => void
    ): JSX.Element => (
      <TripItems
        key={index}
        index={index}
        form={childForm}
        field={field}
        updateGeneralSum={updateGeneralSum}
        availableTransportTypes={availableTransportTypes}
        availablePublicCompensation={availablePublicCompensation}
        currentTariff={tripStore.actualTariff}
        setIsSaveCompensation={setIsSaveCompensation}
        publicApprovals={publicApprovals}
        remove={remove}
      />
    );

    return (
      <Form
        form={childForm}
        layout="vertical"
        name="create-request"
        size="middle"
      >
        <Form.List name={FormItemNames.tripsInfo} initialValue={[0]}>
          {(fields, { add, remove }): JSX.Element => {
            return (
              <>
                {fields.map((field, index) => (
                  <div key={field.key + index}>
                    <Form.Item
                      className="wrapper_pulicCar_item"
                      shouldUpdate={(prevValues, curValues) => {
                        return (
                          prevValues.sights !== curValues.sights
                          || prevValues.compensationType !== curValues.compensationType
                        );
                      }}
                    >
                      {getItem(index, field, remove)}
                    </Form.Item>
                  </div>
                ))}
                {!countItemForForm && (
                  <Form.Item>
                    <Button
                      icon={<PlusOutlined />}
                      type="text"
                      block
                      onClick={(): void => {
                        addItemToForm(add);
                      }}
                      className="compensation_addButton_title"
                    >
                      {addButtonTitle}
                    </Button>
                  </Form.Item>
                )}
                <div className="compensation_generalSum_wrapper">
                  <span className="compensation_generalSumTitle">{generalSumTitle}</span>
                  <Divider />
                  <div className="compensation_list_wrapper">
                    {childForm.getFieldValue(FormItemNames.tripsInfo).map((el: PublicInfoCard, index: number) => (
                      <div key={index} className="compensation_list_ticket">
                        <span>{`Билет ${index + 1}`}</span>
                        <span>
                          {el?.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION
                          || el?.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
                          || el?.compensationType === TransportCompensations.PAID_SERVICES_COMPENSATION
                            ? formatRublesWithoutRemainder(el?.cost)
                            : formatRublesWithoutRemainder(el?.sum / 100)}
                        </span>
                      </div>
                    ))}
                  </div>
                  <div className="compensation_generalSumLabel_wrapper">
                    <span className="compensation_generalSumLabel">{generalSumLabel}</span>
                    <Form.Item>
                      <span>{formatRublesWithoutRemainder(generalSum / 100)}</span>
                    </Form.Item>
                  </div>
                </div>
              </>
            );
          }}
        </Form.List>

        <Form.Item
          name={FormItemNames.transportTypes}
          initialValue={availableTransportTypes}
          noStyle
        />

        <Form.Item
          name={FormItemNames.compensationTypes}
          initialValue={availablePublicCompensation}
          noStyle
        />
      </Form>
    );
  }
);
