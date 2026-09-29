import React from 'react';
import styled from 'styled-components';

const CyrcleStyle = styled.span.attrs((props: { color: string }) => props)`
  display: inline-block;
  width: 14px;
  height: 14px;
  background: ${props => props.color};
  border-radius: 50%;
  text-align: center;
  margin-right: 6px;
`;

interface PropsType { color: string }
const Cyrcle = ({ color }: PropsType) => <CyrcleStyle color={color} />;

const Description = styled.h4`
  font-weight: 400;
  font-size: 14px;
  color: #747474;
  font-family: 'SB Sans Text', sans-serif;
  height: 40px;
`;

const Title = styled.h3`
  font-weight: 600;
  font-size: 18px;
`;

const Header = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: start;
`;

const CardWrapper = styled.div`
  padding: 14px;
  display: flex;
  flex-direction: column;
  min-height: 380px;
  background: #ffffff;
  border-radius: 10px;
  flex: 1 0;
  box-shadow: 0 0 1px rgba(0, 0, 0, 0.12), 0 4px 8px -2px rgba(0, 0, 0, 0.12);
`;

export {
  Cyrcle, Description, Title, Header, CardWrapper
};
