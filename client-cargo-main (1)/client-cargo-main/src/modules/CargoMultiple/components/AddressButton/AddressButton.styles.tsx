import Button from 'shared/form/Button/Button';
import styled from 'styled-components';

export const SideButtons = styled('div')`
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  margin-top: 12px;
  padding: 12px;
  bottom: 0;
  width: 100%;
  background: #fff;
  border-radius: 16px;
`;

export const SideButton = styled(Button)`
  max-width: 400px;
  background-color: #10bf6a;
  color: #fff;

  .ant-btn :hover {
    background-color: #10bf6a;
    color: #fff;
  }
`;
