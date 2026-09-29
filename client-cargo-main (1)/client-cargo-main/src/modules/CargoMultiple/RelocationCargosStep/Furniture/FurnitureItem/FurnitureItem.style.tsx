import styled from 'styled-components';

export const Wrapper = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const Description = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
`;

export const Button = styled.button`
  width: 130px;
  height: 40px;
  padding: 9px 20px;
  border-radius: 8px;
  border: none;
  background: #f2f3f6;;
`;

export const TitleWrapper = styled.h3`
  display: flex;
  flex-direction: column;
`;

export const Title = styled.h3`
  font-size: 14px;
  margin: 0;
`;

export const Subtitle = styled.h3`
  font-size: 12px;
  margin: 0;
  color: rgba(38, 38, 38, 0.5);
`;
