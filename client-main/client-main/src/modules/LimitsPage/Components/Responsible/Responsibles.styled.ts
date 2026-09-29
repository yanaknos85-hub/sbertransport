import styled from 'styled-components';

export const Container = styled.div`
  width: max-content;
  display: flex;
  justify-content: space-between;
  background-color: #f7f8fa;
  padding: 8px 0 0 16px;
  border-radius: 8px;

  @media (max-width: 800px) {
    width: 100%;
  }
`;

export const ResponsiblesStyled = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
`;

export const Title = styled.span`
  font-family: SB Sans Text;
  font-size: 14px;
  font-weight: 600;
  line-height: 22px;
  letter-spacing: -0.3px;
  color: var(--black-text);
  margin-bottom: 4px;
`;

export const Responsible = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 4px 8px;
`;

export const Name = styled.span`
  font-family: SB Sans Text;
  font-size: 14px;
  line-height: 22px;
  letter-spacing: -0.3px;
  color: var(--black-text);
`;

export const Email = styled.a`
  display: inline-flex;
  font-family: 'SB Sans Interface', sans-serif;
  font-size: 12px;
  line-height: 16px;
  letter-spacing: 0.2px;
  color: var(--black-text);
  border-radius: 8px;
  padding: 2px;
  background-color: rgba(0, 0, 0, 0.04);

  &:hover {
    background-color: var(--solitude);
  }

  svg {
    width: 16px;
    height: 16px;
    margin-right: 2px;
  }
`;

export const ImageStyled = styled.img`
  @media (max-width: 500px) {
    display: none;
  }
`;
