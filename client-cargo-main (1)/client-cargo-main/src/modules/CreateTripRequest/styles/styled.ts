import styled from 'styled-components';

const Item = styled.div<{ $padding?: string; $margin?: string }>`
  background-color: white;
  border-radius: 16px;

  margin: ${props => (props.$margin ? props.$margin : '0 0 12px')};
  padding: ${props => (props.$padding ? props.$padding : '16px 16px 0')};
`;

const Back = styled.div`
  background-color: white;
  border-radius: 16px;
  display: flex;
  justify-content: right;
`;

const PointLetter = styled.div<{ $backgroundColor: string }>`
  position: absolute;
  width: 18px;
  height: 18px;
  text-align: center;
  color: white;
  font-size: 10px;
  line-height: 12px;
  align-items: center;
  display: flex;
  justify-content: center;
  border-radius: 10px;
  font-weight: 600;
  margin: 20.5px 0 0 18.5px;
  padding-left: 1px;

  background-color: ${props => props.$backgroundColor};
`;

const AutocompleteText = styled.span`
  padding: 0 var(--padding-base);
  max-width: 394px;
  overflow: auto;
  text-overflow: ellipsis;
`;

const ButtonWrapper = styled.div`
  margin-top: 16px;
`;

export {
  AutocompleteText, Back, ButtonWrapper,
  Item, PointLetter
};
