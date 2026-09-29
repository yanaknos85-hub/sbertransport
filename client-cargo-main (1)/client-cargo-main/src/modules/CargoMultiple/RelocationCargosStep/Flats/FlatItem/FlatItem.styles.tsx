import styled from 'styled-components';

export const Wrapper = styled.div`
  display: flex;
  padding: 2px 0 6px 6px;
  flex: 1 1 calc(50% - 8px);
  height: 72px;
  border: ${props => props.isSelected ? '2px solid #10bf6a' : '1px solid #d6d6d6'};
  box-sizing: border-box;
  border-radius: 12px;
  cursor: pointer;
  transition: border 0.3s ease;
  background: #ffffff;

  &:hover {
    border: ${props => props.isSelected ? '2px solid #10bf6a' : '2px solid #10bf6a'};
  }
`;

export const Description = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
`;

export const Title = styled.div`
  font-size: 12px;
  font-weight: 600;
  color: #000000;
  font-family: SB Sans Interface, Serif, sans-serif;
`;

export const Text = styled.div`
  font-size: 10px;
  text-align: left;
  letter-spacing: -0.3px;
`;

export const Image = styled.div``;
