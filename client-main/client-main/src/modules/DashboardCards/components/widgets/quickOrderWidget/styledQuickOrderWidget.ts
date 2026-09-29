import styled from 'styled-components';

export const WrapperQuickOrderWidget = styled.div<{ fullWidth: boolean }>`
  height: 270px;
  width: 49%;
  width: ${({ fullWidth }) => (fullWidth ? '100%' : '49%')};
  border-radius: 12px;
  background: rgb(255, 255, 255);
  margin: ${({ fullWidth }) => (fullWidth ? '12px 0px 0px 0px' : '12px 16px 0px 0px')};
  padding: 12px;

  &:nth-child(2) {
    margin: 12px 0px 0px 0px;
  }
`;

export const WrapperQuickOrderWidgetTitle = styled.div`
  display: flex;
  justify-content: space-between;

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
    content: "";
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

export const QuickOrderWidgetTitle = styled.span`
  color: rgb(0, 0, 0);
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

export const WrapperService = styled.div<{ fullWidth: boolean }>`
  height: 100px;
  border: 1px solid rgb(242, 242, 242);
  border-radius: 8px;
  background: rgb(255, 255, 255);
  margin: 12px 0px;
  padding: ${({ fullWidth }) => (fullWidth ? '26px 5px 26px 5px' : '26px 16px 26px 28px')};
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
