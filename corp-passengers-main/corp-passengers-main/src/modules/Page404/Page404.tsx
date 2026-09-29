/* eslint-disable @stylistic/jsx-one-expression-per-line */
import React, { FC } from 'react';
import Panel from 'components/Panel';
import { useContextRouter } from 'context/Router.context';

export const Page404: FC = () => {
  const { from, to } = useContextRouter();
  const route = from && from !== to ? ` по маршруту "${from}"` : '';

  return (
    <Panel>
      <h2>Страница{route} не найдена</h2>
    </Panel>
  );
};

export default Page404;
