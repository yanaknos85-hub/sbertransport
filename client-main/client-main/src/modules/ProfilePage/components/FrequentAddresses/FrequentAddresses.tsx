import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import {
  StyledMyAddresses, WrapperStyledTitle, StyledTitle, WrapperOpenMainContent, OpenMainContent, WrapperListsInfoAddresses, WrapperListItem, WrapperInfoAdresses, InfoAddresses, List, WrapperMainInfo, WrapperButtonOpenMainContent
} from './styled';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as ArrowUp } from 'shared/components/Images/arrowUp.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/arrowDown.svg';
import { ReactComponent as Hause } from 'shared/components/Images/Hause.svg';
import { DeleteOutlined } from '@ant-design/icons';

const FrequentAddresses: FC = observer(() => {
  const [openWindow, setOpenWindow] = useState(false);
  const { addressStore } = useAppStoreContext();
  const { selfFrequentList } = addressStore;

  const deleteFrequent = (id: string): void => {
    addressStore.deleteFrequentAddress(id);
  };

  return (
    <>
      <StyledMyAddresses>
        <WrapperStyledTitle>
          <StyledTitle>Частые адреса</StyledTitle>
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
      {openWindow
      && (
      <WrapperListsInfoAddresses>
        <List>
          {selfFrequentList.map(el => (
            <WrapperListItem key={el.id}>
              <Hause />
              <WrapperMainInfo>
                <WrapperInfoAdresses>
                  <InfoAddresses>
                    {el?.addressString}
                  </InfoAddresses>
                </WrapperInfoAdresses>
                <DeleteOutlined onClick={() => deleteFrequent(el.id)} />
              </WrapperMainInfo>
            </WrapperListItem>
          )
          )}
        </List>
      </WrapperListsInfoAddresses>
      )}
    </>
  );
});

export default FrequentAddresses;
