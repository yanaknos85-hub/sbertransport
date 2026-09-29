import React, { useEffect, FC, useState } from 'react';
import { observer } from 'mobx-react';
import { EmployeeDetailedModel, IDepartment } from '@sber-sbertransport/mf-core';

import { useBlockPhoneConfirmation } from 'api/confirmation';
import { StoreNames } from 'stores';
import { parseNumber } from 'utils/parseNumber';

import { PhoneModal } from './components/PhoneModal/PhoneModal';
import PhoneConfirmation from './components/PhoneConfirmation/PhoneConfirmation';
import ConfirmedSuccessModal from './components/ConfirmedSuccessModal/ConfirmedSuccessModal';
import { defaultStateConfirm, Documents, documentTitles } from './constants/mainInformation.constants';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';
import { ReactComponent as Edit } from 'shared/components/Images/Edit.svg';

import {
  BlockInformation,
  Button,
  ButtonConfirm,
  DescriptionInformation,
  MainInformationWrapper,
  NumberWrapper,
  TitleInformation
} from './styled';

export interface StateConfirm {
  resetCount: boolean;
  editPhone: boolean;
}

const MainInformation: FC<{
  selfDetailed: EmployeeDetailedModel;
  selfDepartment: IDepartment | undefined;
}> = observer(({ selfDetailed, selfDepartment }) => {
  const {
    [StoreNames.selfStore]: selfStore,
    [StoreNames.employeeStore]: empStore,
    [StoreNames.corporateStore]: corporateStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const [modalVisibility, modalActions] = useModalState(false);
  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;

  const [blockPhoneConfirmation] = useBlockPhoneConfirmation();

  useEffect(() => {
    corporateStore.loadAllPositions(selfStore.orgId);
    corporateStore.loadDepartment(selfStore.orgId, selfStore.depId);
    corporateStore.loadOrganization(selfStore.orgId);
  }, [selfStore.orgId, selfStore.depId]);

  const [confirmationModalVisibility, confirmationModalActions] = useModalState(false);
  const [successVisibility, successModalActions] = useModalState(false);

  const [phone, setPhone] = useState<string>();

  const [stateConfirm, setStateConfirm] = useState<StateConfirm>(defaultStateConfirm);

  const mobilePhone = selfDetailed?.mobilePhone;

  const onConfirmationModal = (phoneNumber: string, state: StateConfirm) => {
    blockPhoneConfirmation()
      .then(result => {
        if ('blocked' in result && !result.blocked) {
          setPhone(phoneNumber);
          setStateConfirm(state);
          confirmationModalActions.show();
        }
      });
  };

  const onPhoneConfirm = () => {
    onConfirmationModal(mobilePhone, { resetCount: false, editPhone: true });
  };

  const onNewPhoneConfirm = (phoneNumber: string) => {
    onConfirmationModal(phoneNumber, { resetCount: true, editPhone: false });
  };

  return (
    <MainInformationWrapper>
      <BlockInformation>
        <TitleInformation>{documentTitles[Documents.TELEPHONE]}</TitleInformation>
        <NumberWrapper>
          <DescriptionInformation>
            {mobilePhone ? parseNumber(mobilePhone) : <Button onClick={modalActions.show}>Добавить номер</Button>}
          </DescriptionInformation>
          {mobilePhone && (
            <>
              <Edit onClick={modalActions.show} />
              {!selfStore.selfEmployee.isPhoneConfirmed && (
                <ButtonConfirm onClick={onPhoneConfirm}>Подтвердить</ButtonConfirm>
              )}
            </>
          )}
        </NumberWrapper>
      </BlockInformation>

      <BlockInformation>
        <TitleInformation>
          {documentTitles[Documents.ROLE]}
        </TitleInformation>
        <DescriptionInformation>
          {selfDetailed?.position}
        </DescriptionInformation>
      </BlockInformation>
      {!IS_PERSONAL_DEVICE && (
        <BlockInformation>
          <TitleInformation>
            {documentTitles[Documents.DIVISION]}
          </TitleInformation>
          <DescriptionInformation>
            {selfDetailed?.department}
          </DescriptionInformation>
        </BlockInformation>
      )}
      <BlockInformation>
        <TitleInformation>
          {documentTitles[Documents.MAIL]}
        </TitleInformation>
        <DescriptionInformation>
          {selfDetailed?.email}
        </DescriptionInformation>
      </BlockInformation>
      {!IS_PERSONAL_DEVICE && (
        <>
          <BlockInformation>
            <TitleInformation>
              {documentTitles[Documents.COMPANY]}
            </TitleInformation>
            <DescriptionInformation>
              {selfDetailed?.organization}
            </DescriptionInformation>
          </BlockInformation>
          <BlockInformation>
            <TitleInformation>
              {documentTitles[Documents.ADDRESS_DEPARTMENT]}
            </TitleInformation>
            <DescriptionInformation>
              {selfDepartment?.location}
            </DescriptionInformation>
          </BlockInformation>
        </>
      )}
      <PhoneModal
        phoneNumber={selfDetailed?.mobilePhone}
        modalVisibility={modalVisibility}
        modalActions={modalActions}
        selfStore={selfStore}
        empStore={empStore}
        showConfirmationModal={onNewPhoneConfirm}
      />
      <PhoneConfirmation
        visible={confirmationModalVisibility}
        onClose={confirmationModalActions.hide}
        phone={phone}
        stateConfirm={stateConfirm}
        onSuccess={successModalActions.show}
      />
      <ConfirmedSuccessModal visible={successVisibility} onClose={successModalActions.hide} />
    </MainInformationWrapper>
  );
});

export default MainInformation;
