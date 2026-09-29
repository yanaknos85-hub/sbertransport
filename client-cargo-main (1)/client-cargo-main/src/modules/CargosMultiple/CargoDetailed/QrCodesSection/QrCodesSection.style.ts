import styled from 'styled-components';

export const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const Item = styled.div`
  box-sizing: border-box;
  border: 1px solid #ebebeb;
  border-radius: 8px;
  background: rgba(242, 243, 246, 0.6);
  padding: 8px 12px;
  word-break: break-all;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
`;
