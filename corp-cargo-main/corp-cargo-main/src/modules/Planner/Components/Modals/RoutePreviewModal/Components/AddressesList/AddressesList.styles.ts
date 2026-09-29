import styled from 'styled-components';
import { SemiBoldText } from '../../RoutePreviewModal.style';

export const ListTitle = styled.h3`
  font-weight: 600;
`;

export const AddressesList = styled(SemiBoldText)`
  margin: 0;
  padding: 0;

  font-weight: 400;
  font-size: 14px;

  font-family: ${props => props.fontFamily};
  color: ${props => props.color};

  ul {
    padding: 0;
    list-style-type: none;
  }
`;
