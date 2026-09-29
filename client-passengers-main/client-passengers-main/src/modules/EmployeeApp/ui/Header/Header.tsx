import { observer } from 'mobx-react';
import React from 'react';
import { Button } from 'antd';
import styled from 'styled-components';
import { useHistory } from 'react-router-dom';
import { StoreNames, useAppStore } from 'stores';
import { AppLinksStartPage } from 'constants/constants.app';

const Header = styled.div`
  height: 64px;
  background-color: white;
  flex: 0 0 auto;
  box-shadow: 0px 6px 12px rgb(0 0 0 / 5%);
  z-index: 1;
  padding-left: 24px;
  padding-right: 24px;
  align-items: center;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  ;
`;

const HeaderFC: React.FC = observer(({ children }): JSX.Element => {
  const { [StoreNames.selfStore]: selfStore } = useAppStore();

  const history = useHistory();

  const toLogout = (): void => {
    history.push(AppLinksStartPage.Logout);
  };

  return (
    <Header>
      {children}
      <span>
        Авторизован как:
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
