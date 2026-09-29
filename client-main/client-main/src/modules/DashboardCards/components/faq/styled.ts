import styled from 'styled-components';

export const WrapperContent = styled.div`
  padding: 12px 12px 24px 12px;
  background: rgb(255, 255, 255);
  border-radius: 12px;
  margin: 0 8px;

  .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn {
    color: rgb(38, 38, 38);
    font-family: SB Sans Text;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    letter-spacing: -0.3px;
  }

  .ant-tabs-tab-btn {
    color: rgb(115, 115, 115);
    font-family: SB Sans Text;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    letter-spacing: -0.3px;
  }
`;

export const WrapperList = styled.div`
  font-family: SB Sans Text, sans-serif;
  font-weight: 400;
  letter-spacing: -0.3px;

  .ant-collapse {
    display: flex;
    flex-direction: column;
    gap: 4px;
    border: none;

    > .ant-collapse-item {
      border: 1px solid #E0E0E0;
      background: #FFF;
      border-radius: 8px;

      .ant-collapse-header {
        padding: 12px;
        font-weight: 600;
        font-size: 16px;
        line-height: 24px;
        color: #1C1C1C;
      }

      .ant-collapse-content {
        border-top: none;
        color: #1C1C1C;

        .ant-collapse-content-box {
          padding: 0 12px 12px 12px;

          p, ul, ol {
            margin: 0;
          }

          ul {
            padding-left: 12px;

            li::marker {
              font-size: 0.7em;
            }
          }

          ol {
            padding-left: 14px;

            li::marker {
              font-size: 0.8em;
            }
          }

          a {
            font-weight: 600;
            color: #10BF6A
          }

          b {
            font-weight: 600;
          }
        }
      }

      .ant-collapse-expand-icon {
        position: absolute;
        right: 0;
      }
    }

    > .ant-collapse-item-active {
      background: #F2F3F680;

      .ant-collapse-header {
        padding-bottom: 16px;
      }

      .ant-collapse-content-active {
        background: #F2F3F680;
      }
    }
  }
`;
