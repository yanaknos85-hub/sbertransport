import React, { FC } from 'react';
import { Typography as AntTypography } from 'antd';
import styled from 'styled-components';

const StyledParagraph = styled(AntTypography)<any>`
  font-weight: ${props => props.weight || 400};
  font-size: ${props => props.size || '14px'};
  color: ${props => props.color};
  opacity: ${props => props.opacity || 1};
  font-family: ${props => props.font};
  margin-bottom: ${props => props.mb};
`;

const TTypography: FC<any> = props => <StyledParagraph {...props} />;

export default TTypography;
