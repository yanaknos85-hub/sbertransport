import styled from 'styled-components';
import Panel from 'components/Panel';
import { colors } from 'shared/styles/styles';

export const PanelStyled = styled(Panel)`
  padding: 16px;
  margin: 0;
  box-shadow: none;

  .ant-tabs > .ant-tabs-nav {
    padding: 0;

    &::before {
      content: '';
      position: absolute;
      right: 0;
      left: 0;
      border-bottom: 1px solid rgba(38, 38, 38, 0.08);
      display: block !important;
    }
  }

  .ant-tabs-tab-btn {
    font-weight: 400 !important;
  }

  .ant-tabs-card > .ant-tabs-nav {
    padding: 0;

    &::before {
      display: none !important;
    }

    .ant-tabs-tab {
      border-radius: 8px;
      border: none;
      background-color: ${colors.bgDark};
      color: ${colors.gray9};
      transition: none!important;

      &:hover {
        background-color: rgba(0, 0, 0, 0.06);
      }

      &-active {
        background-color: #e7f9f0;
        color: ${colors.gray10};
        border: 2px solid ${colors.green10};

        &:hover {
          background-color: #e7f9f0;
        }
      }

      + .ant-tabs-tab {
        margin-left: 8px;
        transition: none!important;
      }
    }
  }
`;
