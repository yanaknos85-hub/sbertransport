import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Tabs } from 'antd';
import { TabsStyled } from '../LimitsPage.styled';

// Разработка данной страницы временно приостановлена
const LimitSharing: FC = () => {
  return (
    <TabsStyled>
      <Tabs.TabPane tab="Перевозка пассажиров" key="passengers">
        {null}
      </Tabs.TabPane>
      <Tabs.TabPane tab="Перевозка грузов" key="cargo">
        {null}
      </Tabs.TabPane>
    </TabsStyled>
  );
};

export default observer(LimitSharing);
