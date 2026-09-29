import React, { FC } from 'react';
import { useParams } from 'react-router-dom';
import { Divider } from 'antd';
import cn from 'classnames';
import { observer } from 'mobx-react';
import { useUiContext } from 'shared/components/UI';
import { emptySign } from 'shared/constants/constants';
import { useModalState } from 'shared/hooks/useModal';

import uuid from 'utils/uuid';

import { ListItemExchange } from '../../types';
import AddressBlockExchange from '../Address/AddressBlockExchange';
import { EditableField } from '../EditableField';
import { STATUSES } from '../EditableField/constants';
import { RateModal } from '../Modals/RateModal/RateModal';
import { Header } from './Header/Header';
import { OrderControls } from './OrderControls/OrderControls';

import styles from './listItem.module.scss';

interface Props {
  request: ListItemExchange;
  setIsRefetch?: (value: boolean) => void;
}

export const ListItem: FC<Props> = observer(props => {
  const { request } = props;

  const { isMobile } = useUiContext();
  const { type } = useParams<{ type: string; id: string }>();

  const [modal, modalActions] = useModalState();

  const currentStatus = STATUSES.find(item => item.name === request.status);
  const statusName = currentStatus ? currentStatus.rusName : emptySign;
  const statusValue = currentStatus ? currentStatus.name : emptySign;

  return (
    <>
      <div className={styles.wrapper}>
        <Header request={request} />
        <Divider style={{ margin: '16px 0' }} />
        <div className={cn(styles.mainInfo, {
          [styles['isMobileDirection']]: isMobile,
          [styles['mainInfoMobile']]: isMobile,
        })}
        >
          <div className={styles.addressWrapper}>
            <AddressBlockExchange waypoints={request.waypoints} />
          </div>
          {type === 'non_terminal' && (
            <div className={styles.selectWrapper}>
              <EditableField
                key={uuid()}
                id={request.id}
                label=""
                description={statusName}
                status={statusValue}
                transportType={request.transportType as string}
                departureAddressCoordinates={{ latitude: 0, longitude: 0 }}
              />
            </div>
          )}
          <div className={styles.controlsWrapper}>
            <OrderControls
              request={request}
              showModal={modalActions.show}
            />
          </div>
        </div>
      </div>
      <RateModal
        id={request.id}
        transportType={request.transportType}
        transportTypeRus={request.transportTypeRus}
        visible={modal}
        hide={modalActions.hide}
      />
    </>
  );
});
