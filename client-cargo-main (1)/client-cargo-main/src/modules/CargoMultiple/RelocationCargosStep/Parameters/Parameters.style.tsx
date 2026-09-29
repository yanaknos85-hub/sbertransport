import styled from 'styled-components';

export const Wrapper = styled.div`
  padding: 16px;
  border-radius: 12px;
  background: #F2F3F6;
  font-family: 'SB Sans Interface', serif, sans-serif;
  width: 94%;
  margin: 0 auto;
`;

export const Title = styled.div`
  font-size: 14px;
  font-weight: 600;
  font-family: 'SB Sans Text', serif, sans-serif;
  margin-bottom: 8px;
`;

export const Content = styled.div`
  font-size: 12px;
`;

export const ParamWrapper = styled.div`
  display: flex;
  width: 70%;
  justify-content: space-between;
  gap: 16px;
`;

export const ParamItem = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: start;
`;

export const ParamTitle = styled.div`
  font-size: 12px;
  color: #737373;
`;

export const ParamValue = styled.div`
  font-size: 14px;
  color: #262626;
`;
