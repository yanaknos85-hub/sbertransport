import { FireFilled as SharedFireFilled } from '@ant-design/icons';
import { Tag } from 'antd';
import { IconButton as SharedIconButton } from 'shared/form/Button/Button';
import Panel from 'shared/ui/Panel';
import styled from 'styled-components';

export const StyledCard = styled(Panel)`
  flex: 1 1 424px;
  padding: 24px;
  margin: 8px;
  display: flex;
  flex-direction: column;
  color: var(--ship-cove);
  font-size: 14px;
  line-height: 22px;
  justify-content: space-between;
  box-sizing: border-box;

  a,
  a:visited,
  a:active {
    color: inherit;
  }
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
`;

export const BlockInfoWrapper = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
`;

export const BlockInfo = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  p {
    font-family: 'SB Sans Interface', serif, sans-serif;
    font-size: 14px;
    line-height: 16px;
    color: #909090;
  }
`;

export const BlockInfoCost = styled.div`
  text-align: right;
`;

export const Cost = styled.div`
  font-size: 24px;
  font-weight: 700;
  color: #000;
  opacity: 0.9;
`;

export const CheckboxStyled = styled.div`
  margin-left: 12px;

  .ant-checkbox-wrapper {
    margin-left: 10px;
  }

  .ant-checkbox-inner {
    width: 16px;
    height: 16px;
    border: ${`1px solid #D6D6D6`};
    border-radius: 4px;
  }
`;

export const IconButton = styled(SharedIconButton)`
  border-radius: 10px;
  background-color: #F2F3F6;
  color: #4D4D4D;
  border-color: #F2F3F6;
  display: flex;
  width: 40px;
  height: 40px;
  padding: 6px 8px;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
`;

export const FireFilled = styled(SharedFireFilled)`
  color: #f5222d;
  font-size: 10px;
  background-color: #f2f3f6;
  border-radius: 5px;
`;

export const RelocationWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
`;

export const StyledTag = styled(Tag)`
  padding: 3px 14px 3px 14px;
  width: fit-content;
  height: fit-content;
  font-size: 12px;
  color: #000000;
  background: #FAFAFA;
  border-radius: 12px;
  border: 1px solid rgba(38, 38, 38, 0.08);
`;
