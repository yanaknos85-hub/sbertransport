import React, { FC, Suspense } from 'react';

import { AppTitles } from 'constants/app.constants';

import PageLayout from 'components/PageLayout/PageLayout';
import Panel from 'components/Panel/Panel';
import { AutoparkList } from './components/AutoparkList/AutoparkList';
import { ModalFormAutopark } from './components/ModalFormAutopark/ModalFormAutopark';
import { ActiveAutoparkProvider } from './context/ActiveAutopark';
import { ModalFormProvider } from './context/ModalForm';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

const AutoParks: FC = () => (
  <ActiveAutoparkProvider>
    <ModalFormProvider>
      <PageLayout oneColumn>
        <Panel title={AppTitles.Autoparks} contentWithoutPadding>
          <Suspense fallback={<SpinWrapped />}>
            <AutoparkList />
          </Suspense>
        </Panel>
      </PageLayout>
      <ModalFormAutopark />
    </ModalFormProvider>
  </ActiveAutoparkProvider>
);

export default AutoParks;
