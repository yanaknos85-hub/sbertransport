import React, { useEffect, useState } from 'react';
import { Steps } from 'antd';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';
import {
  GroupTransferStatusesStepper,
  TTripRequestStatuses,
  TripRequestStatusesTitles,
  TripStatusesEnum,
  allCancelStatuses,
  cancelStatuses,
  carsharingStatusesStepper,
  personaStatusesStepper,
  personaStatusesStepperShared,
  publicStatusesStepper,
  taxiStatusesStepper
} from 'modules/EmployeeApp/TripRequestStatuses.constants';

import { StoreNames } from 'stores';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { ITripHistory } from 'stores/Trip/Trip.interface';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { IDepartmentHead } from 'stores/Corporate/Corporate.interface';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import Cancel from 'shared/components/Images/cancel.svg';

import UserInformation from './UserInformation/UserInformation';
import { mergeArrays } from '../utils/mergeArrays';
import { formatFullNameWithoutDots } from 'utils/formatFullName';

import './overwrite.scss';

const { Step } = Steps;

interface Props {
  type: TransportTypeEnum | undefined;
  currentType: TTripRequestStatuses | undefined;
  request: TripRequestModel;
  delegates: Delegate[];
  supervisor?: IDepartmentHead;
  isNotSharedOwner?: boolean;
}

export interface IstatusStepper {
  title: string;
  description: string;
}

const StepperDetailed = ({
  type, currentType, request, delegates, isNotSharedOwner, supervisor,
}: Props): JSX.Element => {
  const [statuses, setStatuses] = useState<IstatusStepper[] | undefined>([]);
  const [historyTrip, sethistoryTrip] = useState<ITripHistory[]>([]);

  const onCurrentType = currentType && TripRequestStatusesTitles[currentType];
  const onCancelStatus = currentType && cancelStatuses.some(el => el === currentType);
  const indexCurrentType = statuses && statuses.findIndex(el => el.title === onCurrentType) + 1;
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

  const uniqueHistoryArray = (historyArray: ITripHistory[]) => {
    return historyArray.filter((obj, idx, arr) => {
      return arr.findIndex(t => JSON.stringify(t.status) === JSON.stringify(obj.status)) === idx;
    });
  };

  useEffect(() => {
    tripStore.getTripRequestHistory(request.id)
      .then(historyArray => {
        if (onCancelStatus && historyTrip) {
          const dataDetailed = getStatusTransportType(type);
          const extremeSatus: TTripRequestStatuses = historyArray[historyArray.length - 2].status;
          let indexEl = dataDetailed?.findIndex(el => el?.title === TripRequestStatusesTitles[extremeSatus]);
          indexEl !== undefined && indexEl++;
          const finalArrayStatuses = dataDetailed?.slice(0, indexEl);
          const errorStatus = historyArray[historyArray.length - 1].status;
          dataDetailed && finalArrayStatuses && finalArrayStatuses.push(allCancelStatuses[errorStatus]);
          const arrayCurrentStatuses = finalArrayStatuses && mergeArrays(historyArray, finalArrayStatuses);
          setStatuses(arrayCurrentStatuses);
        } else {
          const dataDetailed = getStatusTransportType(type);
          if (type === TransportTypeEnum.PERSONAL && onFinalsPersonalStatuses) {
            let indexEl = dataDetailed?.findIndex(el => el?.title === (TripRequestStatusesTitles.PERSONAL_PAYMENT_DONE
              || TripRequestStatusesTitles.PERSONAL_PAYMENT_DECLINED));
            const finalArrayStatuses = dataDetailed?.slice(0, indexEl && ++indexEl);
            const arrayCurrentStatuses = finalArrayStatuses && mergeArrays(historyArray, finalArrayStatuses);
            setStatuses(arrayCurrentStatuses);
          } else {
            const indexEl = dataDetailed?.findIndex(el => el?.title === TripRequestStatusesTitles.PUBLIC_PAYMENT_NOT_DONE);
            const finalArrayStatuses = dataDetailed?.slice(0, type === TransportTypeEnum.PUBLIC && indexEl ? indexEl : -1);
            const arrayCurrentStatuses = finalArrayStatuses && mergeArrays(historyArray, finalArrayStatuses);
            setStatuses(arrayCurrentStatuses);
          }
        }
        sethistoryTrip(uniqueHistoryArray(historyArray));
      });
  }, []);

  const historySearch = (itemTitle: string) => {
    return historyTrip?.map(el => {
      if (TripRequestStatusesTitles[el.status] === itemTitle) {
        return moment(el.changeDate).format(DATE_FORMAT.DATE_WITH_TIME_OTHER);
      }
    });
  };

  const descriptionDelegate = (itemTitle: string) => {
    if (historyTrip.length > 0) {
      if (historyTrip.length >= 2 && itemTitle === TripRequestStatusesTitles[historyTrip[1].status] && !onCancelStatus) {
        return (
          <div className="approved">
            <span>Заявка согласована:</span>
            <UserInformation id={request.approvedBy.id} fio={request.approvedBy.fullName} />
          </div>
        );
      } else if (historyTrip.length < 2 && itemTitle === TripRequestStatusesTitles[historyTrip[0].status]) {
        return (
          <div className="approved">
            <span>Вашу заявку могут согласовать:</span>
            {delegates.length === 0
              ? supervisor && (
              <UserInformation
                id={supervisor.id}
                fio={`${supervisor.lastName} ${supervisor.firstName} ${supervisor.patronymic}`}
              />
              )
              : delegates.map(
                ({
                  delegateEmployee: {
                    userId,
                    lastName,
                    firstName,
                    patronymic,
                  },
                }) => (
                  <div>
                    <UserInformation
                      id={userId}
                      fio={formatFullNameWithoutDots(firstName, lastName, patronymic)}
                    />
                  </div>
                ))}
          </div>
        );
      }
    }
  };

  return (
    <>
      <Steps
        current={indexCurrentType}
        className={onCancelStatus ? 'CancelStepperDetailed' : 'StepperDetailed'}
        direction="vertical"
      >
        {statuses && Object.values(statuses).map((item: IstatusStepper, index: number) => (
          <Step
            className={onCancelStatus
              ? 'cancelItem'
              : index === indexCurrentType
                ? 'currentItem'
                : index === (indexCurrentType && indexCurrentType - 1) ? 'notCurrentItem' : ''}
            key={index}
            title={(
              <div className="StepperDetailedTitle">
                <span>{item?.title}</span>
                <span>{historySearch(item.title)}</span>
              </div>
)}
            description={descriptionDelegate(item.title)}
            icon={onCancelStatus ? index === statuses.length - 1
              ? <div className="LastCancelItem"><img alt="Cancel" src={Cancel} /></div>
              : <div className="cancel"></div> : index === indexCurrentType
              ? <div className="active"></div>
              : indexCurrentType && index > indexCurrentType && <div></div>}
          />
        ))}
      </Steps>
      {onCancelStatus
      && <div className="statusCodeDescription">{request.statusCodeDescription}</div>}
    </>
  );
};

export default StepperDetailed;
