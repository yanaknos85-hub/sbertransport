import { observer } from 'mobx-react';
import React from 'react';
import { Button } from 'antd';
import styled from 'styled-components';
import { useProfile } from 'api/profile';
import { useAppStore, StoreNames } from 'stores';
import { Organization } from 'shared/components/Organization';

const Header = styled.div`
  min-height: 64px;
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
  const {
    [StoreNames.configStore]: { history },
  } = useAppStore();

  const { firstName, lastName } = useProfile().data;

  const toLogout = (): void => {
    history.push('/oauth/logout');
  };

  return (
    <Header>
      {children}
      <Organization />
      <div>
        <span>{`${firstName} ${lastName}`}</span>
        <Button
          block={true}
          type="link"
          onClick={toLogout}
          style={{ width: '100px' }}
        >
          Выйти
        </Button>
      </div>
    </Header>
  );
});

export default HeaderFC;
