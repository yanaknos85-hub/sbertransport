import React, { useEffect, useState } from 'react';
import './overwrite.scss';
import { Steps } from 'antd';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  GroupTransferStatusesStepper, TTripRequestStatuses, TripRequestStatusesTitles, TripStatusesEnum, allCancelStatuses, cancelStatuses, carsharingStatusesStepper, personaStatusesStepper, personaStatusesStepperShared, publicStatusesStepper, taxiStatusesStepper
} from 'modules/EmployeeApp/TripRequestStatuses.constants';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { TripRequestModel } from 'stores/Trip/models';
import { ITripHistory } from 'stores/Trip/Trip.interface';
import Cancel from 'shared/components/Images/cancel.svg';
import { IstatusStepper } from '../StepperDetailed/StepperDetailed';
import { mergeArrays } from '../utils/mergeArrays';

const { Step } = Steps;

interface Props {
  type: TransportTypeEnum | undefined;
  currentType: TTripRequestStatuses | undefined;
  request: TripRequestModel;
  isNotSharedOwner?: boolean;
}

const StepperStatus = ({
  type, currentType, request, isNotSharedOwner,
}: Props): JSX.Element => {
  const [statuses, setStatuses] = useState<IstatusStepper[] | undefined>([]);
  const [historyTrip, sethistoryTrip] = useState<ITripHistory[]>([]);

  const onCurrentType = currentType && TripRequestStatusesTitles[currentType];
  const onCancelStatus = currentType && cancelStatuses.some(el => el === currentType);
  const indexCurrentType: number | undefined = statuses && statuses.findIndex((el: IstatusStepper) => el.title === onCurrentType) + 1;
  const {
    [StoreNames.tripStore]: tripStore,
  } = useAppStoreContext();
  const onFinalsPersonalStatuses = currentType === TripStatusesEnum.PERSONAL_PAYMENT_DONE;

  const getStatusTransportType = (tripType: TransportTypeEnum | undefined) => {
    switch (tripType) {
      case TransportTypeEnum['TAXI']:
      {
        return taxiStatusesStepper;
      }
      case TransportTypeEnum['PERSONAL']:
      {
        return isNotSharedOwner ? personaStatusesStepper : personaStatusesStepperShared;
      }
      case TransportTypeEnum['CARSHARING']:
      {
        return carsharingStatusesStepper;
      }
      case TransportTypeEnum['PUBLIC']:
      {
        return publicStatusesStepper;
      }
      case TransportTypeEnum['GROUP_TRANSFER']:
      {
        return GroupTransferStatusesStepper;
      }
    }
  };

  useEffect(() => {
    tripStore.getTripRequestHistory(request.id)
      .then(historyArray => {
        if (onCancelStatus && historyTrip) {
          const data = getStatusTransportType(type);
          const extremeSatus: TTripRequestStatuses = historyArray[historyArray.length - 2].status;
          let indexEl = data?.findIndex(el => el?.title === TripRequestStatusesTitles[extremeSatus]);
          indexEl !== undefined && indexEl++;
          const finalArrayStatuses = data?.slice(0, indexEl);
          const errorStatus = historyArray[historyArray.length - 1].status;
          data && finalArrayStatuses && finalArrayStatuses.push(allCancelStatuses[errorStatus]);
          const arrayCurrentStatuses = finalArrayStatuses && mergeArrays(historyArray, finalArrayStatuses);
          setStatuses(arrayCurrentStatuses);
          sethistoryTrip(historyArray);
        } else {
          const data = getStatusTransportType(type);
          if (type === TransportTypeEnum.PERSONAL && onFinalsPersonalStatuses) {
            let indexEl = data?.findIndex(el => el?.title === (TripRequestStatusesTitles.PERSONAL_PAYMENT_DONE || TripRequestStatusesTitles.PERSONAL_PAYMENT_DECLINED));
            const finalArrayStatuses = data?.slice(0, indexEl && ++indexEl);
            const arrayCurrentStatuses = finalArrayStatuses && mergeArrays(historyArray, finalArrayStatuses);
            setStatuses(arrayCurrentStatuses);
            sethistoryTrip(historyArray);
          } else {
            const indexEl = data?.findIndex(el => el?.title === TripRequestStatusesTitles.PUBLIC_PAYMENT_NOT_DONE);
            const finalArrayStatuses = data?.slice(0, type === TransportTypeEnum.PUBLIC && indexEl ? indexEl : -1);
            const arrayCurrentStatuses = finalArrayStatuses && mergeArrays(historyArray, finalArrayStatuses);
            setStatuses(arrayCurrentStatuses);
            sethistoryTrip(historyArray);
          }
        }
      });
  }, []);

  return (
    <Steps
      current={indexCurrentType}
      className={onCancelStatus ? 'CancelStepper' : 'Stepper'}
      direction="horizontal"
      responsive={false}
    >
      {statuses && Object.values(statuses).map((item: IstatusStepper, index: number) => (
        <Step
          className={onCancelStatus ? 'cancelItem' : index === indexCurrentType ? 'currentItem' : 'notCurrentItem'}
          key={index}
          icon={onCancelStatus ? index === statuses.length - 1 ? <div className="LastCancelItem"><img alt="Cancel" src={Cancel} /></div> : <div className="cancel" /> : index === indexCurrentType ? <div className="active" /> : indexCurrentType && index > indexCurrentType && <div />}
        />
      ))}
    </Steps>
  );
};

export default StepperStatus;
