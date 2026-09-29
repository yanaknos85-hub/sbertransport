import { Form } from 'antd';
import { Stepper } from 'components/Stepper';
import { TableStyled as TableStyledComponent } from 'shared/components/TableStyled';
import styled from 'styled-components';

export const StyledStepper = styled(Stepper)`
  margin-bottom: 40px;
`;

export const StyledHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const StyledRow = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;

  > * {
    flex: 1 1 0;
  }
`;

export const StyledFooter = styled.footer`
  margin: 8px;
`;

export const StyledGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
`;

export const StyledTableActions = styled.div`
  display: flex;
  justify-content: flex-end;
  margin: 24px 0 16px 0;
`;

export const TableStyled = styled(TableStyledComponent)`
  margin-top: 24px;
`;

export const StyledItem = styled(Form.Item)`
  margin-bottom: 0;
`;
