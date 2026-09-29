import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import { Tabs } from 'antd';
import {
  OpenMainContent,
  StyledMyAddresses,
  StyledTitle,
  WrapperButtonOpenMainContent,
  WrapperListsInfoAddresses,
  WrapperOpenMainContent,
  WrapperStyledTitle
} from './styled';
import { ReactComponent as ArrowUp } from 'shared/components/Images/arrowUp.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/arrowDown.svg';
import TabPane from 'antd/es/tabs/TabPane';
import { NotificationClass } from './Notifications.interface';
import { NotificationsCargo } from './NotificationsCargo/NotificationsCargo';
import { NotificationsPassengers } from './NotificationsPassengers/NotificationsPassengers';
import { notificationClassPassengers } from './consts';

const Notifications: FC = observer(() => {
  const [openWindow, setOpenWindow] = useState(true);

  return (
    <>
      <StyledMyAddresses>
        <WrapperStyledTitle>
          <StyledTitle>Уведомления</StyledTitle>
          <WrapperOpenMainContent>
            {openWindow
              ? (
                <WrapperButtonOpenMainContent onClick={() => setOpenWindow(prev => !prev)}>
                  <OpenMainContent>
                    Cкрыть
                  </OpenMainContent>
                  <ArrowUp />
                </WrapperButtonOpenMainContent>
              )
              : (
                <WrapperButtonOpenMainContent onClick={() => setOpenWindow(prev => !prev)}>
                  <OpenMainContent>
                    Раскрыть
                  </OpenMainContent>
                  <ArrowDown />
                </WrapperButtonOpenMainContent>
              )}
          </WrapperOpenMainContent>
        </WrapperStyledTitle>
      </StyledMyAddresses>
      {openWindow && (
        <WrapperListsInfoAddresses>
          <Tabs
            defaultActiveKey={notificationClassPassengers}
          >
            <TabPane
              tab="Пассажирские перевозки"
              key={NotificationClass.REQUEST_TAXI}
            >
              <NotificationsPassengers />
            </TabPane>
            <TabPane
              tab="Грузовые перевозки"
              key={NotificationClass.REQUEST_CARGO}
            >
              <NotificationsCargo />
            </TabPane>
            <TabPane
              tab="Автосервис"
              key={NotificationClass.REQUEST_FLEET}
              disabled
            >
              Автосервис
            </TabPane>
            <TabPane
              tab="Общие"
              key={NotificationClass.REQUEST_COMMON}
              disabled
            >
              Общие
            </TabPane>
          </Tabs>
        </WrapperListsInfoAddresses>
      )}
    </>
  );
});

export default Notifications;
