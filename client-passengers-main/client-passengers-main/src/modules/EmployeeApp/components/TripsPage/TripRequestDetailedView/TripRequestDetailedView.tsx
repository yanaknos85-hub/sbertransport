import { observer } from 'mobx-react';
import React, { FC } from 'react';
import { List } from 'antd';

import { SpinWrapped } from 'shared/components';
import {
  useCurrentTripRequest,
  useCurrentTripRequestCoop,
  useCurrentTripRequestApprovers,
  useTripRequestDetailedView
} from 'shared/hooks/trip';

import TripRequestContent from './TripRequestContent';
import TripRequestContentOld from './TripRequestContentOld';
import TripRequestContentCarsharing from './TripRequestContentCarsharing/TripRequestContentCarsharing';
import TripRequestContentPublic from './TripRequestContentPublic/TripRequestContentPublic';
import TripRequestContentGroupTransfer from './TripRequestContentGroupTransfer/TripRequestContentGroupTransfer';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

const TripRequestDetailedView: FC = observer(() => {
  const { currentTripRequest, inProgress } = useCurrentTripRequest();
  const { delegates, supervisor } = useCurrentTripRequestApprovers();
  const { tripFromCoop } = useCurrentTripRequestCoop(currentTripRequest?.id as any);

  const personalRequestIsApproved = currentTripRequest?.status === 'PERSONAL_APPROVED';

  const {
    goToPlanned,
  } = useTripRequestDetailedView({
    requestIsApproved: personalRequestIsApproved,
  });

  return (
    <List.Item>
      {currentTripRequest && !inProgress ? (
        <>
          {currentTripRequest.transportType === TransportTypeEnum.TAXI || currentTripRequest.transportType === TransportTypeEnum.PERSONAL
            ? (
              <TripRequestContent
                request={currentTripRequest}
                delegatesList={delegates}
                supervisor={supervisor}
                tripFromCoop={tripFromCoop}
              />
            )
            : currentTripRequest.transportType === TransportTypeEnum.CARSHARING
              ? (
                <TripRequestContentCarsharing
                  request={currentTripRequest}
                  delegatesList={delegates}
                  supervisor={supervisor}
                  tripFromCoop={tripFromCoop}
                />
              )
              : currentTripRequest.transportType === TransportTypeEnum.PUBLIC
                ? (
                  <TripRequestContentPublic
                    request={currentTripRequest}
                    delegatesList={delegates}
                    supervisor={supervisor}
                    tripFromCoop={tripFromCoop}
                  />
                )
                : currentTripRequest.transportType === TransportTypeEnum.GROUP_TRANSFER
                  ? (
                    <TripRequestContentGroupTransfer
                      request={currentTripRequest}
                      delegatesList={delegates}
                      supervisor={supervisor}
                      tripFromCoop={tripFromCoop}
                    />
                  )
                  : (
                    <TripRequestContentOld
                      request={currentTripRequest}
                      delegatesList={delegates}
                      tripFromCoop={tripFromCoop}
                      goToPlanned={goToPlanned}
                    />
                  )}
          {/* {activeStatusEntity?.cancelable && currentTripRequest?.transportType === TransportTypeEnum.TAXI && <DeclineModal id={currentTripRequest.id} cancelHandler={cancelHandler} />}
          {activeStatusEntity?.cancelable && currentTripRequest?.transportType !== TransportTypeEnum.TAXI && <DeclineModalEmpty cancelHandler={() => cancelHandler('Причина по умолчанию',currentTripRequest.id)} />} */}
        </>
      ) : (
        <SpinWrapped />
      )}
    </List.Item>
  );
});

export default TripRequestDetailedView;
