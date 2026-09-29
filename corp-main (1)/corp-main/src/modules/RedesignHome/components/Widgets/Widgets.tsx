import React, { FC, Suspense, useState } from 'react';
import { useTranslation } from 'i18n';
import { Radio } from 'antd';

import { Container } from '../Container/Container';
import { Filters } from '../Filters/Filters';
import { Charts } from '../Charts/Charts';
import { useForm } from 'antd/lib/form/Form';
import { OrganizationsGroup } from 'stores/OrganizationsGroup/OrganizationsGroup.interface';
import { useWidgets } from 'modules/RedesignHome/context/Widgets.context';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import { ContainerWidget } from '../ContainerWidget/ContainerWidget';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import { widgetComponents } from 'modules/RedesignHome/constants/widgets';
import { useGetExecutorGroups } from 'api/executor-group';

import styles from './Widgets.module.scss';

interface Props {
  orgGroups: OrganizationsGroup[];
}

export const Widgets: FC<Props> = ({ orgGroups }) => {
  const { t } = useTranslation();
  const [form] = useForm();
  const [radioValue, setRadioValue] = useState('BusinessMetrics');
  const executorGroup = useGetExecutorGroups({ page: 0, size: 200 }).data.content;

  const {
    activeWidgets,
  } = useWidgets();

  return (
    <Container title={t.RedesignHomePage.widgets}>
      <div className={styles.header_radioButtons}>
        <Radio.Group
          onChange={e => setRadioValue(e.target.value)}
          defaultValue={radioValue}
          buttonStyle="solid"
        >
          <Radio.Button value="BusinessMetrics">Бизнес-метрики</Radio.Button>
          <Radio.Button value="Budget">Бюджет</Radio.Button>
        </Radio.Group>
      </div>
      {radioValue === 'BusinessMetrics' ? (
        <>
          <Filters
            form={form}
            orgGroups={orgGroups}
            executorGroup={executorGroup}
          />
          <Charts form={form} />
        </>
      )
        : (
          <>
            {activeWidgets.size > 0 && (
            <>
              {Array.from(activeWidgets).map(widget => {
                const Widget = widgetComponents[widget];

                return (
                  <ErrorBoundary key={widget}>
                    <Suspense fallback={<SpinWrapped />}>
                      <ContainerWidget>
                        <Widget />
                      </ContainerWidget>
                    </Suspense>
                  </ErrorBoundary>
                );
              })}
            </>
            )}
          </>
        )}
    </Container>
  );
};
