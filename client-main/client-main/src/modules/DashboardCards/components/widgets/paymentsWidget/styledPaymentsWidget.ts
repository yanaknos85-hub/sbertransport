import styled from 'styled-components';

export const WrapperPaymentsWidget = styled.div<{ fullWidth: boolean }>`
  width: 49%;
  width: ${({ fullWidth }) => (fullWidth ? '100%' : '49%')};
  border-radius: 12px;
  background: rgb(255, 255, 255);

  margin: ${({ fullWidth }) => (fullWidth ? '12px 0px 0px 0px' : '12px 16px 0px 0px')};
  padding: ${({ fullWidth }) => (fullWidth ? '12px 10px 10px 10px' : '12px')};

  &:nth-child(2) {
    margin: 12px 0px 0px 0px;
  }
`;

export const WrapperPaymentsWidgetTitle = styled.div`
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;

  .QuickOrderWidget_customSelect .ant-select-selector input {
    display: none; /* Скрываем встроенный input */
  }
  
  .QuickOrderWidget_customSelect .ant-select-selector {
    display: flex;
    align-items: center;
    cursor: pointer;
  }

  .QuickOrderWidget_customSelect .ant-select-selector::after {
    visibility: visible !important;
  }
  
  .QuickOrderWidget_customSelect .ant-select-selector .ant-select-selection-placeholder {
    flex: 1;
  }
  
  .QuickOrderWidget_customSelect .ant-select-selector .ant-select-selection-placeholder,
  .QuickOrderWidget_customSelect .ant-select-selector .ant-select-selection-item {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .QuickOrderWidget_customSelect .ant-select-selector .ant-select-selection-item {
    margin-right: 8px;
  }
  
  .ant-select-selector {
    border: none !important;
  }
  
  .QuickOrderWidget_customSelect .ant-select-arrow {
    display: none; /* Скрываем стрелку */
  }
  
  .QuickOrderWidget_customSelect .ant-select-selector:after {
    content: '';
    width: 0;
    height: 0;
    border-left: 6px solid transparent;
    border-right: 6px solid transparent;
    border-top: 6px solid #000;
    margin-left: 4px;
  }

  .ant-select-selector {
    cursor: pointer !important;
  }
  
  .custom-dropdown {
    width: 185px !important;

    .ant-select-item-option-active:not(.ant-select-item-option-disabled) {
      background-color: transparent;
    }
  }
  
  .ant-select-selection-overflow-item {
    display: none;
  }  
`;

export const QuickPaymentsWidgetTitle = styled.span`
  color: rgb(0, 0, 0);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
  margin-right: 16px;
`;

export const WrapperService = styled.div`
  height: 100px;
  border: 1px solid rgb(242, 242, 242);
  border-radius: 8px;
  background: rgb(255, 255, 255);
  margin: 12px 0px;
  padding: 26px 16px 26px 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const TransportTypeQuickOrderWidget = styled.img`
  display: inline-block;
  background-size: cover;
  width: 76px;
  height: 51px;
  transform: translateX(7px);
`;

export const ServiceType = styled.span`
  margin-left: 28px;
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: -0.3px;
`;

export const ServiceButton = styled.div`
  color: rgb(77, 77, 77);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
  border-radius: 8px;
  background: rgb(242, 243, 246);
  padding: 5px 16px 5px 16px;
`;

export const WrapperAbsenceService = styled.div`
  display: flex;
  height: 100%;
  justify-content: center;
  align-items: center;
  color: rgb(168, 171, 179);
  font-family: SB Sans Interface;
  font-size: 24px;
  font-weight: 600;
  line-height: 32px;
  letter-spacing: 0px;
`;

export const WrapperOrderWidget = styled.div`
  display: flex;
`;

export const DataValueCount = styled.span`
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 18px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: -0.3px;
  display: flex;
  justify-content: center;
`;

export const DataValueCost = styled.span`
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 18px;
  font-weight: 600;
  line-height: 24px;
  letter-spacing: -0.3px;
  display: flex;
  justify-content: flex-end;
`;

export const DataValueName = styled.span<{ title: string }>`
  color: ${props => {
    if (props.title === 'в работе') {
      return 'rgb(105, 121, 247)';
    } else if (props.title === 'Формирование приказа' || props.title === 'Ожидание выплаты') {
      return 'rgb(255, 154, 50)';
    } else if (props.title === 'Выплачено') {
      return 'rgb(16, 191, 106)';
    }
  }};
  font-family: SB Sans Interface;
  font-size: 10px;
  font-weight: 600;
  line-height: 14px;
  letter-spacing: 0.12px;
  text-transform: uppercase;
  padding: 3px 10px 5px 10px;
  border-radius: 16px;
  background: ${props => {
    if (props.title === 'в работе') {
      return 'rgba(105, 121, 247, 0.15)';
    } else if (props.title === 'Формирование приказа' || props.title === 'Ожидание выплаты') {
      return 'rgba(255, 180, 103, 0.15)';
    } else if (props.title === 'Выплачено') {
      return 'rgb(231, 249, 240)';
    }
  }};
  @media screen and (max-width: 600px) {
    background: none;
    border: none;
    color: black;
    padding: 0;
  };
`;
