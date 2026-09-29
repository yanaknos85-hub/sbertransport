/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import styled from 'styled-components';

export const Layout = styled('div')`
  display: flex;
  position: relative;
  height: 84vh;

  @media (max-width: 767px) {
    flex-direction: column;
    gap: 16px;
  }
`;

export const Map = styled('div')`
  width: 100%;
  height: 84vh;
  margin: 0;
  z-index: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: -1px 4px 8px 0 rgba(0, 0, 0, 0.08);
  margin-left: 7px;

  @media (max-width: 767px) {
    margin-left: 0;
    min-height: 200px;
    height: auto;
  }
`;

export const SideFake = styled('div')`
  width: 460px;
  min-width: 460px;
  height: 100%;
`;

export const Side = styled('div')`
  z-index: 0;
  position: absolute;
  top: 16px;
  right: 16px;
  bottom: 16px;
  width: 480px;
  min-width: 480px;
  border-right: 16px;
  border-radius: 16px;
  overflow-y: scroll;

  @media (max-width: 767px) {
    position: relative;
    top: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    min-width: 100%;
  }
`;

export const SideInner = styled('div')`
  width: 100%;
  height: 100%;
`;

export const SideContent = styled('div')`
  display: flex;
  flex-direction: column;
  height: 100%;
  background: rgba(0, 0, 0, 0);
  border-radius: 16px;

  @media (max-width: 767px) {
    height: auto;
  }
`;

export const SideSections = styled('div')`
  overflow-x: hidden;
  overflow-y: auto;
  margin-top: 12px;
  background-color: rgba(0, 0, 0, 0);
  border-radius: 16px;
  max-height: 300px;

  @media (max-width: 767px) {
    max-height: unset;
  }
`;

export const SideButtons = styled('div')`
  margin-top: 12px;
  width: 100%;
  padding: 16px;
  border-radius: 16px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;

  @media (max-width: 767px) {
    margin-bottom: 16px;
  }
`;
