import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import {
  StyledMyAddresses, AddButton, WrapperStyledTitle, StyledTitle, WrapperOpenMainContent, OpenMainContent, WrapperListsInfoAddresses, WrapperListItem, WrapperInfoAdresses, TitleMyAddresses, InfoAddresses, List, WrapperMainInfo, WrapperButtonOpenMainContent
} from './styled';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as ArrowUp } from 'shared/components/Images/arrowUp.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/arrowDown.svg';
import { ReactComponent as AddIcon } from 'shared/components/Images/blackIconAdd.svg';
import { ReactComponent as Hause } from 'shared/components/Images/Hause.svg';
import AddFavoriteModal from './components/AddFavoriteModal/AddFavoriteModal';
import { useModalState } from 'shared/hooks/useModal';
import { DeleteOutlined } from '@ant-design/icons';

const MyAddresses: FC = observer(() => {
  const [openWindow, setOpenWindow] = useState(false);
  const { addressStore } = useAppStoreContext();
  const { selfFavoriteList } = addressStore;
  const [modal, modalActions] = useModalState();

  const deleteFavorite = (id: string): void => {
    addressStore.deleteFavoriteAddress(id);
  };

  return (
    <>
      <StyledMyAddresses>
        <WrapperStyledTitle openWindow={openWindow}>
          <StyledTitle>Мои адреса</StyledTitle>
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
          {selfFavoriteList.map(el => (
            <WrapperListItem key={el.id}>
              <Hause />
              <WrapperMainInfo>
                <WrapperInfoAdresses>
                  <TitleMyAddresses>
                    {el?.label}
                  </TitleMyAddresses>
                  <InfoAddresses>
                    {el?.addressString}
                  </InfoAddresses>
                </WrapperInfoAdresses>
                <DeleteOutlined onClick={() => deleteFavorite(el.id)} />
              </WrapperMainInfo>
            </WrapperListItem>
          )
          )}
          <AddButton onClick={modalActions.show}>
            <AddIcon />
            Добавить адрес
          </AddButton>
        </List>
      </WrapperListsInfoAddresses>
      )}
      <AddFavoriteModal
        visible={modal}
        onOk={modalActions.hide}
        onCancel={modalActions.hide}
      />
    </>
  );
});

export default MyAddresses;
