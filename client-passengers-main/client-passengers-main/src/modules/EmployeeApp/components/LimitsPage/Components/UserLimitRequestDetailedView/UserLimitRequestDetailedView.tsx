/* eslint-disable @typescript-eslint/no-explicit-any */
import { Col, Row } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { EmptyRequest } from 'modules/EmployeeApp/shared/EmptyFactory';

import PageLayout from 'shared/components/PageLayout/PageLayout';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { isInitRequest } from '../../utils';
import styles from './styles.module.scss';
import UserLimitRequestContent from './UserLimitRequestContent';
import UserLimitRequestApproveModal from './UserLimitRequestModal/UserLimitRequestApproveModal';
import useMyLimitsRequestsDetailedView from './useUserLimitRequestDetailedView';

const UserLimitRequestDetailedView: FC = observer(() => {
  const { [StoreNames.limitsStore]: limits } = useAppStoreContext();

  useEffect(() => {
    limits.getLimitRequest();
  }, [limits]);

  const match = useRouteMatch<any>();
  const { reqId } = match.params;

  const request = limits.limitRequestsStats.find(x => x.id === reqId);

  const { goToList } = useMyLimitsRequestsDetailedView();

  return (
    <PageLayout
      title="Заявка на пополнение лимита"
      onBack={goToList}
      empty={<EmptyRequest back={goToList} />}
      loading={request === undefined}
    >
      {request && (
        <>
          <UserLimitRequestContent request={request} />
          {isInitRequest(request.status) && (
            <div>
              <Row className={styles.mgTop}>
                <Col className={styles.alignLeft} span={8}>
                  <UserLimitRequestApproveModal
                    isEdit={true}
                    type={request.limitType}
                    transportType={request.transportType}
                    request={request}
                    goToList={goToList}
                  />
                </Col>
                <Col className={styles.alignRight} span={8}>
                  <UserLimitRequestApproveModal
                    isEdit={false}
                    type={request.limitType}
                    transportType={request.transportType}
                    request={request}
                    goToList={goToList}
                  />
                </Col>
              </Row>
            </div>
          )}
        </>
      )}
    </PageLayout>
  );
});

export default UserLimitRequestDetailedView;
