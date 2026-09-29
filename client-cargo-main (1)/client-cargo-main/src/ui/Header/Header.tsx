import React from 'react';
import { MenuOutlined } from '@ant-design/icons';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { Button } from 'antd';
import { observer } from 'mobx-react';
import { useUiContext } from 'shared/components/UI';
import { StoreNames, useAppStore } from 'stores';
import styled from 'styled-components';

import { AppLinksStartPage } from 'constants/constants.app';

const Header = styled.div`
  height: 64px;
  background-color: white;
  flex: 0 0 auto;
  box-shadow: 0 6px 12px rgb(0 0 0 / 5%);
  z-index: 1;
  padding-left: 8px;
  align-items: center;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 16px;

  @media (min-width: 768px) {
    padding-left: 24px;
    padding-right: 24px;
    gap: 0;
  }
`;

interface HeaderFCProps {
  onOpenNav: () => void;
}

const HeaderFC: React.FC<HeaderFCProps> = observer(({
  children,
  onOpenNav,
}): JSX.Element => {
  const { [StoreNames.selfStore]: selfStore } = useAppStore();
  const { isMobile } = useUiContext();
  const history = History();

  const toLogout = (): void => {
    history.push(AppLinksStartPage.Logout);
  };

  return (
    <Header>
      {isMobile && <MenuOutlined onClick={onOpenNav} />}
      {children}
      <span>
        {!isMobile && 'Авторизован как:'}
        {' '}
        {selfStore?.selfEmployee?.fullNameWithCode}
      </span>
      <Button
        block={true}
        type="link"
        onClick={toLogout}
        style={{ width: '100px' }}
      >
        Выйти
      </Button>
    </Header>
  );
});

export default HeaderFC;
