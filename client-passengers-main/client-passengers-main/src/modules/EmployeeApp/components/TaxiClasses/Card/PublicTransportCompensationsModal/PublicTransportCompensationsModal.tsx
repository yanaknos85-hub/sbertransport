/* eslint-disable no-unsafe-optional-chaining */
/* eslint-disable @typescript-eslint/no-explicit-any */

import { Button, Form, Popconfirm } from 'antd';
import Modal from 'antd/lib/modal/Modal';
import moment, { Moment } from 'moment';
import React, { FC, useEffect } from 'react';

import {
  AvailablePublicTTCompensationType,
  useGetAvailablePublicTTCompensations,
  useGetAvailablePublicTransportTypes
} from 'api/trip-requests';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';
import { StoreNames } from 'stores/StoreNames.enum';
import { PublicInfoCard, TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';
import {
  PublicTripCompensation,
  SavedFileInfo,
  TTariffPublic,
  TransportCompensation
} from 'stores/Trip/Trip.interface';

import { UUID } from 'utils/io-ts';

import { endWaypointPlaceholder, startWaypointPlaceholder } from '../../../CreateTripRequest/Components/Waypoints';
import {
  CreateRequestLinks,
  CreateRequestLinksTitles
} from '../../../CreateTripRequest/constants/CreateRequest.constants';
import { getTimeZone } from '../../../CreateTripRequest/utils/utils';
import { ICardProps } from '../Card';
import { CardContent } from '../CardContent';
import { getSavingData } from './Components/additional/creatingDataUtils';
import {
  NoButton,
  YesButton,
  acceptRequestButton,
  chancelRequestButton,
  chancelRequestMessage,
  compensationFormTitle,
  errorEmptyFields
} from './Components/constants';
import styles from './modal.module.scss';
import { PublicTransportCompensationsForm } from './PublicTransportCompensationsForm';

export const PublicTransportCompensationsModal: FC<{
  props: ICardProps;
}> = ({ props }) => {
  const { form, config } = props;
  const { disabled } = config;
  const date: Moment = form?.getFieldValue('date') || moment();
  const [modal, modalActions] = useModalState();
  const [childForm] = Form.useForm();
  const { waypoints, purpose } = form?.getFieldsValue();
  const [generalSum, setGeneralSum] = React.useState(0);
  const availableTransportTypes = useGetAvailablePublicTransportTypes().data;
  const availablePublicCompensation = useGetAvailablePublicTTCompensations().data;
  const {
    [StoreNames.limitsStore]: limitStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.geoStore]: geo,
    [StoreNames.employeeStore]: employeeStore,
    logger,
  } = useAppStoreContext();

  const isNullCost = generalSum === 0;

  useEffect(() => {
    limitStore.getLimitByDepartment(selfStore.depId as UUID);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [limitStore]);
  // FIXME react-hooks/exhaustive-deps

  const cancelHandler = (): void => {
    childForm.resetFields();
    childForm.setFieldsValue({ tripsInfo: [{ ticketCount: 1 }] });
    setGeneralSum(0);
    modalActions.hide();
  };

  const createNewDate = (year: number, month: number, day: number): Date => new Date(year, month, day);

  const createStringDate = (selectedDate: string | number | Date): string => createNewDate(new Date(selectedDate).getFullYear(), new Date(selectedDate).getMonth(), 2)
    .toLocaleDateString()
    .split('.')
    .reverse()
    .join('-');

  const getSameElementTravelCompensation = (newArray: any[], selectedElement: any): any => newArray.find(y => {
    if (y.calendar) {
      const y0 = createStringDate(y.calendar[0]);
      const y1 = createStringDate(y.calendar[1]);
      const x0 = createStringDate(selectedElement.calendar[0]);
      const x1 = createStringDate(selectedElement.calendar[1]);

      return (
        y.compensationType === selectedElement.compensationType
        && y.publicTransportType === selectedElement.publicTransportType
        && JSON.stringify(y0) === JSON.stringify(x0)
        && JSON.stringify(y1) === JSON.stringify(x1)
      );
    }
    return false;
  });

  const getSameElementCityCompensation = (newArray: any[], selectedElement: any): any => newArray.find(
    y => y.compensationType === selectedElement.compensationType
    && y.publicTransportType === selectedElement.publicTransportType
  );

  const getArrayForTariff = (array: any[], newArray: any[], selectedElement: any): void => {
    const findElem
      = selectedElement.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
        ? getSameElementTravelCompensation(newArray, selectedElement)
        : getSameElementCityCompensation(newArray, selectedElement);

    if (!findElem) {
      newArray.push(selectedElement);
    } else {
      const index = newArray.indexOf(findElem);
      if (selectedElement.compensationType === TransportCompensations.CITY_TRIP_COMPENSATION) {
        // eslint-disable-next-line no-param-reassign
        newArray[index].ticketCount += selectedElement.ticketCount;
      }

      if (selectedElement.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION) {
        // eslint-disable-next-line no-param-reassign
        newArray[index].cost += selectedElement.cost;
      }

      if (selectedElement.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION) {
        // eslint-disable-next-line no-param-reassign
        newArray[index].ticketCount += selectedElement.ticketCount;
        // eslint-disable-next-line no-param-reassign
        newArray[index].cost += selectedElement.cost;
      }
    }
  };

  /** Создание нового массива, который должен будет объединить в
   себе элементы по типу компенсации и типу транспорта */
  const getArrayWithoutDuplicates = (array: any[]): any[] => {
    const newArray: any[] = [];
    array.forEach(x => {
      if (x.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION) {
        newArray.push(x);
      }

      if (
        x.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
        || x.compensationType === TransportCompensations.CITY_TRIP_COMPENSATION
      ) {
        getArrayForTariff(array, newArray, x);
      }
    });
    return newArray;
  };

  const getTransportTypeCompensationList = (arrayWithoutDuplicates: any[]): TransportCompensation[] => arrayWithoutDuplicates.map(item => ({
    compensationType: availablePublicCompensation.find(
      elem => elem.name === item.compensationType
    ) as AvailablePublicTTCompensationType,
    transportType: availableTransportTypes.find(y => y.name === item.publicTransportType),
    ticketsCost: item.cost,
    ticketsCount: item.quantity,
    ticketsExpirationStart:
        item.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
          ? createStringDate(item.calendar[0])
          : undefined,
    ticketsExpirationEnd:
        item.compensationType === TransportCompensations.TRAVEL_CARD_COMPENSATION
          ? createStringDate(item.calendar[1])
          : undefined,
  }));

  const getSavedFileData = (array: any[]): SavedFileInfo[] => {
    const savedFileData: SavedFileInfo[] = [];

    array.forEach(x => {
      const savedFileInfo = x.savedFileData;
      if (savedFileInfo) {
        savedFileData.push(savedFileInfo);
      }
    });

    return savedFileData;
  };

  // eslint-disable-next-line no-shadow
  const getData = (array: any[]): PublicTripCompensation => {
    // FIXME no-shadow
    const purposeObj = tripStore.purposesMapped[purpose];
    // eslint-disable-next-line no-shadow
    const { employee, passenger } = form?.getFieldsValue();
    // FIXME no-shadow
    const formPassenger
      = passenger === 'me'
        ? selfStore.selfEmployee
        : employeeStore.employeeListByOrg.find((x: any) => x.fullNameWithCode === employee);

    const author = selfStore.selfEmployee;

    const arrayWithoutDuplicates: any[] = getArrayWithoutDuplicates(array);
    const savedFileData: SavedFileInfo[] = getSavedFileData(array);
    const transportCompensation: TransportCompensation[] = getTransportTypeCompensationList(arrayWithoutDuplicates);

    const transportCompensationDocumentID = transportCompensation.map(item => savedFileData[transportCompensation.indexOf(item)]
      ? { ...item, attachedDocumentId: savedFileData[transportCompensation.indexOf(item)].id }
      : { ...item }
    );

    const savingData = getSavingData(
      // @ts-ignore
      formPassenger || selfStore.selfEmployee,
      author,
      purposeObj,
      generalSum,
      date,
      geo,
      transportCompensationDocumentID,
      savedFileData,
      (tripStore.actualTariff as TTariffPublic)?.id
    );

    return savingData.author.id === savingData.passenger.id ? { ...savingData, timeZone: getTimeZone() } : savingData;
  };
  // eslint-disable-next-line no-shadow, consistent-return
  const saveCompensationRequest = (array: any[]): Promise<unknown> => {
    const newData = getData(array);
    return tripStore.savePublicTripRequest(newData);
  };

  const okHandler = (): void => {
    childForm.validateFields().then(
      () => {
        // eslint-disable-next-line no-shadow
        const tripsInfo = childForm.getFieldValue('tripsInfo');

        tripsInfo.forEach((el: PublicInfoCard, index: number) => {
          if (el.compensationType === TransportCompensations.SUBURB_TRIP_COMPENSATION) {
            tripsInfo[index].cost *= 100;
          }
        });
        // FIXME no-shadow
        if (geo.calculatedRoute) {
          saveCompensationRequest(tripsInfo)
            .then(() => {
              cancelHandler();
              // history.push(routes.TRANSPORT_2_0_SUCCESS);
            })
            .catch(error => {
              if (error.response && error.response.status) {
                logger.toMessage(
                  'error',
                  error.response.status === 409
                    ? 'На данный вид транспорта, в указанном месяце, уже есть проездной билет'
                    : error.response.data.message
                );
              } else {
                logger.toMessage('error', error.message);
              }
            });
        }
      },
      () => {
        logger.toMessage('error', errorEmptyFields);
      }
    );
  };

  const footer: JSX.Element = (
    <div className={styles.footer}>
      <Button
        type="primary"
        onClick={okHandler}
        disabled={isNullCost}
      >
        {acceptRequestButton}
      </Button>

      <Popconfirm
        placement="top"
        title={chancelRequestMessage}
        onConfirm={cancelHandler}
        okText={YesButton}
        cancelText={NoButton}
      >
        <Button danger={true}>{chancelRequestButton}</Button>
      </Popconfirm>
    </div>
  );

  const showMessage = (losedParam: string): void => {
    logger.toMessage('error', `Проверьте заполнение ${losedParam}`);
  };

  const checkWaypoints = (localWaypoints: any[]): boolean => localWaypoints.every(x => x.waypoint.length);

  const checkPT = (): void => {
    if (disabled) {
      return;
    }
    if (checkWaypoints(waypoints) && purpose && config.transportType) {
      modalActions.show();
    } else if (!checkWaypoints(waypoints)) {
      showMessage(`полей  "${startWaypointPlaceholder}" и "${endWaypointPlaceholder}"!`);
    } else if (!purpose) {
      showMessage(`поля "${CreateRequestLinksTitles[CreateRequestLinks.purposePlaceholder]}"`);
    }
  };

  return (
    <>
      <div className={styles.buttonsContainer}>
        {/* eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
        <div className={styles.buttonWithCardContent} onClick={checkPT}>
          {/* FIXME jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */}
          <CardContent props={props} />
        </div>
      </div>
      <Modal
        title={compensationFormTitle}
        visible={modal}
        onCancel={cancelHandler}
        onOk={okHandler}
        footer={footer}
      >
        <PublicTransportCompensationsForm
          formRef={childForm}
          generalSum={generalSum}
          setGeneralSum={setGeneralSum}
          availableTransportTypes={availableTransportTypes}
          availablePublicCompensation={availablePublicCompensation}
          currentTariff={tripStore.actualTariff}
        />
      </Modal>
    </>
  );
};
