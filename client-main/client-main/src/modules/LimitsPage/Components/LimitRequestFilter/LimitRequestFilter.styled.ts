import DatePicker from 'antd/lib/date-picker';
import styled from 'styled-components';

export const FilterContainer = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 50px;

  @media (max-width: 500px) {
    flex-wrap: wrap;
    flex-direction: column-reverse;
    gap: 16px;
  }

  .ant-picker {
    &-range-arrow {
      display: none;
    }

    &-range-wrapper {
      box-shadow: 0px 4px 8px -2px rgba(0, 0, 0, 0.12), 0px 0px 1px 0px rgba(0, 0, 0, 0.12);
      border-radius: 12px;
    }

    &-focused {
      box-shadow: none;

      &::before {
        background-color: var(--jade);
      }
    }

    @media (max-width: 500px) {
      &-header {
        justify-content: center;

        &-view {
          flex: none;
          margin: 0 8px;
        }
      }

      &-range-wrapper {
        width: calc(100% - 16px);
      }

      &-panel,
      &-date-panel {
        width: 100%;
      }

      &-body,
      &-dropdown {
        display: flex;
        align-items: center;
        justify-content: center;
      }

      &-dropdown-hidden {
        display: none;
      }

      &-panels {
        flex-wrap: wrap;
      }

      &-ranges {
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
`;

export const TransportList = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
`;

export const TransportItem = styled.div<{ isActive: boolean }>`
  display: flex;
  padding: 6px 16px;
  border-radius: 18px;
  background-color: ${({ isActive }) => (isActive ? '#3A424A' : '#dfe2e4')};
  cursor: pointer;

  > span {
    font-family: 'SB Sans Text';
    font-weight: 400;
    color: ${({ isActive }) => (isActive ? 'var(--pure-white)' : 'var(--black-text)')};
    letter-spacing: -0.3px;
    white-space: nowrap;
  }
`;

export const StyledIcon = styled.div<{ src: string }>`
  display: flex;
  width: 24px;
  height: 24px;
  background: url(${props => props.src}) no-repeat no-repeat center 0;
  background-size: contain;
  background-position: center;
  margin-right: 10px;
`;

export const StyledDatePicker = styled(DatePicker.RangePicker)`
  min-width: 270px;
  height: 40px;
  border-radius: 8px;
  padding: 9px 16px;
  background-color: #dfe2e4;
  position: relative;

  @media (max-width: 500px) {
    margin-left: auto;
  }

  &::before {
    content: '';
    width: 1px;
    height: 24px;
    position: absolute;
    background-color: rgba(38, 38, 38, 0.08);
    right: 56px;
    top: 50%;
    transform: translateY(-50%);
  }

  &:hover {
    &::before {
      background-color: var(--jade);
    }
  }

  .ant-picker {
    &-input {
      width: 80px;

      > input {
        width: 80px;
        color: var(--gray9);

        &::placeholder {
          color: var(--gray9);
        }
      }
    }

    &-clear {
      width: 20px;
      height: 20px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background-color: #dfe2e4;
      right: 17px;
    }

    &-suffix {
      color: var(--dark-grey);
      margin-left: auto;
      width: 20px;
      border-color: rgba(38, 38, 38, 0.08);
      display: inline-flex;
      align-items: center;

      > svg {
        height: 20px;
      }
    }
  }
`;

export const FilterContent = styled.div`
  display: none;

  @media (max-width: 500px) {
    width: 50%;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-grow: 3;
    margin-right: 8px;

    svg path {
      fill: var(--black-text);
    }
  }
`;
