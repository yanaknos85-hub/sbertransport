import React,
{
  FC, lazy, Suspense, useEffect
} from 'react';
import { useParams } from 'react-router-dom';

import { useHistory } from '@sber-sbertransport/mf-core';
import { TabsPropsOriginal } from '@sber-sbertransport/ui-kit/src';

import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

import { RELEASE_ON_LINE } from 'constants/routes.constants';
import { ReleaseOnLineTabs } from './ReleaseOnLine.constants';

const Waybill = lazy(() => import('./Waybill/Waybill'));

const tabs: TabsPropsOriginal['items'] = [
  {
    key: ReleaseOnLineTabs.Waybill,
    label: 'Путевые листы',
    children: (
      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <Waybill />
        </Suspense>
      </ErrorBoundary>
    ),
  },
  {
    key: ReleaseOnLineTabs.MedicalExamination,
    label: 'Медицинский осмотр',
    children: '',
    disabled: true,
  },
  {
    key: ReleaseOnLineTabs.TechnicalControl,
    label: 'Технический контроль',
    children: '',
    disabled: true,
  },
];

const ReleaseOnLine: FC = () => {
  const { tab } = useParams();
  const history = useHistory();

  useEffect(() => {
    // При отсутствии таба редирект на первый
    if (!tab || !tabs.some(({ key }) => key === tab)) {
      history.replace(RELEASE_ON_LINE.replace(':tab', tabs[0].key));
    }
  }, [tab, history]);

  // const onChangeTab = (key: string) => {
  //   history.push(RELEASE_ON_LINE.replace(':tab', key));
  // };

  return (
    // Временно скрываем табы, пока существует только путевой лист
    // <Tabs
    //   activeKey={tab}
    //   items={tabs}
    //   onChange={onChangeTab}
    // />
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <Waybill />
      </Suspense>
    </ErrorBoundary>
  );
};

export default ReleaseOnLine;
