import * as React from 'react';

import { createGlobalStyle } from 'styled-components';
import Layout from '.';

export default {
  title: 'Layout',
  component: Layout,
};

const GlobalStyle = createGlobalStyle`
  body {
    background-color: #eff2f7;
  }

  #root { 
    height: 100%;
  }
`;

export const LayoutStory: React.FC = () => (
  <>
    <GlobalStyle />
    <Layout headerContent="Header content" drawerContent="">
      Body
    </Layout>
  </>
);
