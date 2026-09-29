import styled from 'styled-components';
import Title from 'antd/lib/typography/Title';
import Text from 'antd/lib/typography/Text';
import { Button } from 'shared/components/Button/Button';
import { colors } from 'shared/styles/styles';

export const Wrapper = styled.div`
  width: 100%;
  height: 100vh;
  padding: 20px;
  background: linear-gradient(45deg, rgb(105, 121, 247), rgb(25, 177, 80));
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

export const StyledTitle = styled(Title)`
  color: ${colors.white} !important;
`;

export const StyledText = styled(Text)`
  color: ${colors.white} !important;
`;

export const StyledPhone = styled.a`
  font-size: 14px;
  font-weight: 600;
  color: ${colors.white} !important;
  margin-left: 5px;
`;

export const StyledButton = styled(Button)`
  width: 105px;
  margin-top: 20px;
`;
