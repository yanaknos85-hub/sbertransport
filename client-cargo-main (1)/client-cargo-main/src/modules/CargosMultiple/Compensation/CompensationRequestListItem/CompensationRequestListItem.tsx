import React from 'react';
import { Checkbox, Divider, Space } from 'antd';
import { CheckboxChangeEvent } from 'antd/es/checkbox';
import { observer } from 'mobx-react';
import moment from 'moment';
import CargoTariffTag from 'shared/components/Cargo/CargoTariffTag/CargoTariffTag';
import { useUiContext } from 'shared/components/UI';

import { CompensationRequestModel } from 'stores/Compensations/models/CargoRequest.model';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { CargosTabsFilters } from 'constants/Cargo.constants';
import { DATE_FORMAT } from 'constants/constants.app';

import CompensationApprovalBlock from '../CompensationApprovalBlock/CompensationApprovalBlock';
import * as S from '../styled';
import { setIcon } from '../utils.';
import { useCompensationRequestItem } from './hooks';
import * as Styled from './styled';

export interface CompensationRequestListItemProps {
  request: CompensationRequestModel;
  onClickHandler: (id: string) => void;
  accentColor: string;
  isApproval: boolean;
  activeTab: string;
  isRegular?: boolean;
}

export const RenderCargoRequestList = observer(({
  request,
  onClickHandler,
  accentColor,
  isApproval,
  activeTab,
  isRegular,
  ...rest
}: CompensationRequestListItemProps): JSX.Element | null => {
  const { isMobile } = useUiContext();
  const {
    checkedItem, isActivePage, isCheckBoxVisible, onChange,
  } = useCompensationRequestItem(request);

  return (
    <Styled.StyledListItem>
      <S.StyledCard {...{ accentColor, ...rest }}>
        <S.Header>
          <Styled.ContentWrapper $isMobile={isMobile}>
            <Space direction="vertical" size={8}>
              <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row' }}>
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  <Styled.IdWrapper>
                    <div>
                      Заявка
                      {' '}
                      {request?.humanReadableId}
                    </div>
                  </Styled.IdWrapper>
                  <Styled.ApprovalDate>
                    <div>
                      Согласовано:
                      {' '}
                      {moment(request?.approvalDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
                    </div>
                  </Styled.ApprovalDate>
                </div>
                <Styled.CreationTime>
                  от
                  {' '}
                  {moment(request?.creationTime).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
                </Styled.CreationTime>
                <Styled.CargoTariffWrapper>
                  <CargoTariffTag icon={setIcon(TransportTypeEnum.COURIER)} express="true">
                    Курьерская доставка
                  </CargoTariffTag>
                </Styled.CargoTariffWrapper>
              </div>
            </Space>
            <S.BlockInfoWrapper>
              <S.BlockInfo>
                <div>
                  <span>
                    Дата отправления:
                    {' '}
                    <Styled.DesiredDate>
                      {moment(request?.desiredDate).format(`${DATE_FORMAT.BASE_REVERTED_DOTS}`)}
                    </Styled.DesiredDate>
                  </span>
                  <Space />
                </div>
              </S.BlockInfo>
              {activeTab === CargosTabsFilters.active && isCheckBoxVisible && (
                <S.CheckboxStyled onClick={e => e.stopPropagation()}>
                  <Checkbox
                    checked={checkedItem}
                    onChange={(e: CheckboxChangeEvent) => onChange(e, request?.id as string)}
                  />
                </S.CheckboxStyled>
              )}
            </S.BlockInfoWrapper>
          </Styled.ContentWrapper>
        </S.Header>
        <Divider />
        <Styled.RecipientInfo>
          <div>
            ФИО получателя:
            {' '}
            {request.recipient.fio}
            {' '}
            (
            {request.recipient.personnelNumber}
            )
          </div>
          <div>
            Телефон:
            {' '}
            {request.recipient.phone}
          </div>
        </Styled.RecipientInfo>
        {isActivePage && (
          <Styled.CargoAddressBlockWrapper>
            <Styled.ButtonWrapper>
              <CompensationApprovalBlock
                requestId={request.id}
                request={request}
                isMobile={isMobile}
              />
            </Styled.ButtonWrapper>
          </Styled.CargoAddressBlockWrapper>
        )}
        <Divider />
      </S.StyledCard>
    </Styled.StyledListItem>
  );
});

export const CompensationRequestListItem = React.memo(RenderCargoRequestList);
